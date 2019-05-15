/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save the current test plan; implements:
 * Save
 * Save TestPlan As
 * Save (Selection) As
 */
public class Save extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(Save.class);

    private static final List<File> EMPTY_FILE_LIST = Collections.emptyList();
    
    private static final String JMX_BACKUP_ON_SAVE = "jmeter.gui.action.save.backup_on_save"; // $NON-NLS-1$

    private static final String JMX_BACKUP_DIRECTORY = "jmeter.gui.action.save.backup_directory"; // $NON-NLS-1$
    
    private static final String JMX_BACKUP_MAX_HOURS = "jmeter.gui.action.save.keep_backup_max_hours"; // $NON-NLS-1$
    
    private static final String JMX_BACKUP_MAX_COUNT = "jmeter.gui.action.save.keep_backup_max_count"; // $NON-NLS-1$
    
    public static final String JMX_FILE_EXTENSION = ".jmx"; // $NON-NLS-1$

    private static final String DEFAULT_BACKUP_DIRECTORY = JMeterUtils.getJMeterHome() + "/backups"; //$NON-NLS-1$
    
    // Whether we should keep backups for save JMX files. Default is to enable backup
    private static final boolean BACKUP_ENABLED = JMeterUtils.getPropDefault(JMX_BACKUP_ON_SAVE, true);
    
    // Path to the backup directory
    private static final String BACKUP_DIRECTORY = JMeterUtils.getPropDefault(JMX_BACKUP_DIRECTORY, DEFAULT_BACKUP_DIRECTORY);
    
    // Backup files expiration in hours. Default is to never expire (zero value).
    private static final int BACKUP_MAX_HOURS = JMeterUtils.getPropDefault(JMX_BACKUP_MAX_HOURS, 0);
    
    // Max number of backup files. Default is to limit to 10 backups max.
    private static final int BACKUP_MAX_COUNT = JMeterUtils.getPropDefault(JMX_BACKUP_MAX_COUNT, 10);

    // NumberFormat to format version number in backup file names
    private static final DecimalFormat BACKUP_VERSION_FORMATER = new DecimalFormat("000000"); //$NON-NLS-1$

    private static final int MS_PER_HOUR = 60 * 60 * 1000;

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SAVE_AS); // Save (Selection) As
        commands.add(ActionNames.SAVE_AS_TEST_FRAGMENT); // Save as Test Fragment
        commands.add(ActionNames.SAVE_ALL_AS); // Save TestPlan As
        commands.add(ActionNames.SAVE); // Save
    }

    /**
     * Constructor for the Save object.
     */
    public Save() {
        super();
    }

    /**
     * Gets the ActionNames attribute of the Save object.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        HashTree subTree;
        boolean fullSave = false; // are we saving the whole tree?
        if (!commands.contains(e.getActionCommand())) {
            throw new IllegalUserActionException("Invalid user command:" + e.getActionCommand());
        }
        if (e.getActionCommand().equals(ActionNames.SAVE_AS)) {
            JMeterTreeNode[] nodes = GuiPackage.getInstance().getTreeListener().getSelectedNodes();
            if (nodes.length > 1){
                JMeterUtils.reportErrorToUser(
                        JMeterUtils.getResString("save_as_error"), // $NON-NLS-1$
                        JMeterUtils.getResString("save_as")); // $NON-NLS-1$
                return;
            }
            subTree = GuiPackage.getInstance().getCurrentSubTree();
        }
        else if (e.getActionCommand().equals(ActionNames.SAVE_AS_TEST_FRAGMENT)) {
            JMeterTreeNode[] nodes = GuiPackage.getInstance().getTreeListener().getSelectedNodes();
            if(checkAcceptableForTestFragment(nodes)) {
                // Create Test Fragment node
                subTree = createTestFragmentNode(nodes);
            } else {
                JMeterUtils.reportErrorToUser(
                        JMeterUtils.getResString("save_as_test_fragment_error"), // $NON-NLS-1$
                        JMeterUtils.getResString("save_as_test_fragment")); // $NON-NLS-1$
                return;
            }
        } else {
            fullSave = true;
            subTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
        }

        String updateFile = GuiPackage.getInstance().getTestPlanFile();
        if (!ActionNames.SAVE.equals(e.getActionCommand()) // Saving existing plan 
                // New File
                || updateFile == null) {
            boolean isNewFile = updateFile == null;
            updateFile = computeFileName();
            if(updateFile == null) {
                return;
            }
            if (e.getActionCommand().equals(ActionNames.SAVE_ALL_AS) || isNewFile) {
                GuiPackage.getInstance().setTestPlanFile(updateFile);
            }
        }
        
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_DIRTY));
        backupAndSave(e, subTree, fullSave, updateFile); 

        GuiPackage.getInstance().updateCurrentGui();
    }

    /**
     * Create TestFragment test plan from selected nodes
     * @param nodes Array of {@link JMeterTreeNode}
     * @return {@link HashTree} new test plan
     */
    private HashTree createTestFragmentNode(JMeterTreeNode[] nodes) {
        TestElement element = GuiPackage.getInstance().createTestElement(TestFragmentControllerGui.class.getName());
        HashTree hashTree = new ListedHashTree();
        HashTree tfTree = hashTree.add(new JMeterTreeNode(element, null));
        for (JMeterTreeNode node : nodes) {
            // Clone deeply current node
            TreeCloner cloner = new TreeCloner(false);
            GuiPackage.getInstance().getTreeModel().getCurrentSubTree(node).traverse(cloner);
            // Add clone to tfTree
            tfTree.add(cloner.getClonedTree());
        }
        return hashTree;
    }

    /**
     * @return String new file name or null if user want to cancel
     */
    private String computeFileName() {
        JFileChooser chooser = FileDialoger.promptToSaveFile(GuiPackage.getInstance().getTreeListener()
                .getCurrentNode().getName()
                + JMX_FILE_EXTENSION);
        if (chooser == null) {
            return null;
        }
        String updateFile = chooser.getSelectedFile().getAbsolutePath();
        // Make sure the file ends with proper extension
        if(FilenameUtils.getExtension(updateFile).isEmpty()) {
            updateFile = updateFile + JMX_FILE_EXTENSION;
        }
        // Check if the user is trying to save to an existing file
        File f = new File(updateFile);
        if(f.exists()) {
            int response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("save_overwrite_existing_file"), // $NON-NLS-1$
                    JMeterUtils.getResString("save?"),  // $NON-NLS-1$
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.NO_OPTION) {
                return null; // Do not save, user does not want to overwrite
            }
        }
        return updateFile;
    }

    /**
     * Backup existing file according to jmeter/user.properties settings
     * and save
     * @param e {@link ActionEvent}
     * @param subTree HashTree Test plan to save
     * @param fullSave Partial or full save
     * @param newFile File to save
     * @throws IllegalUserActionException
     */
    void backupAndSave(ActionEvent e, HashTree subTree, boolean fullSave, String newFile)
            throws IllegalUserActionException {
        // 
        List<File> expiredBackupFiles = EMPTY_FILE_LIST;
        if (GuiPackage.getInstance().isDirty()) {
            File fileToBackup = new File(newFile);
            log.debug("Test plan has changed, make backup of {}", fileToBackup);
            try {
                expiredBackupFiles = createBackupFile(fileToBackup);
            } catch (Exception ex) {
                log.error("Failed to create a backup for {}", fileToBackup, ex); //$NON-NLS-1$
            }
        }
        
        try {
            convertSubTree(subTree);
        } catch (Exception err) {
            if (log.isWarnEnabled()) {
                log.warn("Error converting subtree. {}", err.toString());
            }
        }

        try (FileOutputStream ostream = new FileOutputStream(newFile)){
            SaveService.saveTree(subTree, ostream);
            if (fullSave) { // Only update the stored copy of the tree for a full save
                FileServer.getFileServer().setScriptName(new File(newFile).getName());
                subTree = GuiPackage.getInstance().getTreeModel().getTestPlan(); // refetch, because convertSubTree affects it
                ActionRouter.getInstance().doActionNow(new ActionEvent(subTree, e.getID(), ActionNames.SUB_TREE_SAVED));
            }
            
            // delete expired backups : here everything went right so we can
            // proceed to deletion
            expiredBackupFiles.forEach(FileUtils::deleteQuietly);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error saving tree.", ex);
            throw new IllegalUserActionException("Couldn't save test plan to file: " + newFile, ex);
        }
    }
    
    /**
     * <p>
     * Create a backup copy of the specified file whose name will be
     * <code>{baseName}-{version}.jmx</code><br>
     * Where :<br>
     * <code>{baseName}</code> is the name of the file to backup without its
     * <code>.jmx</code> extension. For a file named <code>testplan.jmx</code>
     * it would then be <code>testplan</code><br>
     * <code>{version}</code> is the version number automatically incremented
     * after the higher version number of pre-existing backup files. <br>
     * <br>
     * Example: <code>testplan-000028.jmx</code> <br>
     * <br>
     * If <code>jmeter.gui.action.save.backup_directory</code> is <b>not</b>
     * set, then backup files will be created in
     * <code>${JMETER_HOME}/backups</code>
     * </p>
     * <p>
     * Backup process is controlled by the following jmeter/user properties :<br>
     * <table border=1>
     * <tr>
     * <th align=left>Property</th>
     * <th align=left>Type/Value</th>
     * <th align=left>Description</th>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.backup_on_save</code></td>
     * <td><code>true|false</code></td>
     * <td>Enables / Disables backup</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.backup_directory</code></td>
     * <td><code>/path/to/backup/directory</code></td>
     * <td>Set the directory path where backups will be stored upon save. If not
     * set then backups will be created in <code>${JMETER_HOME}/backups</code><br>
     * If that directory does not exist, it will be created</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.keep_backup_max_hours</code></td>
     * <td><code>integer</code></td>
     * <td>Maximum number of hours to preserve backup files. Backup files whose
     * age exceeds that limit should be deleted and will be added to this method
     * returned list</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.keep_backup_max_count</code></td>
     * <td><code>integer</code></td>
     * <td>Max number of backup files to be preserved. Exceeding backup files
     * should be deleted and will be added to this method returned list. Only
     * the most recent files will be preserved.</td>
     * </tr>
     * </table>
     * </p>
     * 
     * @param fileToBackup
     *            The file to create a backup from
     * @return A list of expired backup files selected according to the above
     *         properties and that should be deleted after the save operation
     *         has performed successfully
     */
    private List<File> createBackupFile(File fileToBackup) {
        if (!BACKUP_ENABLED || !fileToBackup.exists()) {
            return EMPTY_FILE_LIST;
        }
        char versionSeparator = '-'; //$NON-NLS-1$
        String baseName = fileToBackup.getName();
        // remove .jmx extension if any
        baseName = baseName.endsWith(JMX_FILE_EXTENSION)
                ? baseName.substring(0, baseName.length() - JMX_FILE_EXTENSION.length())
                : baseName;
        // get a file to the backup directory
        File backupDir = new File(BACKUP_DIRECTORY);
        backupDir.mkdirs();
        if (!backupDir.isDirectory()) {
            log.error(
                    "Could not backup file! Backup directory does not exist, is not a directory or could not be created ! <{}>", //$NON-NLS-1$
                    backupDir.getAbsolutePath());
            return EMPTY_FILE_LIST;
        }

        // select files matching:
        // {baseName}{versionSeparator}{version}{jmxExtension}  // NOSONAR
        // where {version} is a 6 digit number
        String backupPatternRegex = Pattern.quote(baseName + versionSeparator)
                + "([\\d]{6})" //$NON-NLS-1$
                + Pattern.quote(JMX_FILE_EXTENSION);
        Pattern backupPattern = Pattern.compile(backupPatternRegex);
        // get all backup files in the backup directory
        List<File> backupFiles = new ArrayList<>(FileUtils.listFiles(
                backupDir, new PrivatePatternFileFilter(backupPattern), null));
        // oldest to newest
        backupFiles.sort(LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
        // this should be the most recent backup
        int lastVersionNumber = getHighestVersionNumber(backupPattern, backupFiles);

        // backup name is of the form:
        // {baseName}{versionSeparator}{version}{jmxExtension}  // NOSONAR
        String backupName = baseName
                + versionSeparator
                + BACKUP_VERSION_FORMATER.format(lastVersionNumber + 1L)
                + JMX_FILE_EXTENSION;
        File backupFile = new File(backupDir, backupName);
        // create file backup
        try {
            FileUtils.copyFile(fileToBackup, backupFile);
        } catch (IOException e) {
            log.error("Failed to backup file: {}", fileToBackup.getAbsolutePath(), e); //$NON-NLS-1$
            return EMPTY_FILE_LIST;
        }
        // add the new backup file (list is still sorted here)
        backupFiles.add(backupFile);

        return backupFilesToDelete(backupFiles);
    }
    
    /**
     * Find highest version number
     * 
     * @param backupPattern
     *            {@link Pattern}
     * @param backupFiles
     *            {@link List} of {@link File}
     */
    private int getHighestVersionNumber(Pattern backupPattern, List<File> backupFiles) {
        return backupFiles.stream().map(backupFile -> backupPattern.matcher(backupFile.getName()))
                .filter(matcher -> matcher.find() && matcher.groupCount() > 0)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1))).max().orElse(0);
    }
    
    /**
     * Filters list of backup files to those which are candidates for deletion.
     * 
     * @param backupFiles
     *            list of all backup files
     * @return list of files to be deleted based upon properties described
     *         {@link #createBackupFile(File)}
     */
    private List<File> backupFilesToDelete(List<File> backupFiles) {
        List<File> filesToDelete = new ArrayList<>();
        if (BACKUP_MAX_HOURS > 0) {
            filesToDelete.addAll(expiredBackupFiles(backupFiles));
        }
        // if max backups is set, ensure that we don't keep more backups than
        // required
        if (BACKUP_MAX_COUNT > 0 && backupFiles.size() > BACKUP_MAX_COUNT) {
            // keep the most recent files within limit of the specified max
            filesToDelete.addAll(backupFiles.subList(0, backupFiles.size() - BACKUP_MAX_COUNT));
        }
        return filesToDelete.stream().distinct() // ensure no duplicates
                .collect(Collectors.toList());
    }
    
    
    /**
     * @param backupFiles {@link List} of {@link File} to filter
     * @return {@link List} of {@link File} that are expired
     */
    private List<File> expiredBackupFiles(List<File> backupFiles) {
        if (BACKUP_MAX_HOURS > 0) {
            final long expiryMillis = System.currentTimeMillis() - (BACKUP_MAX_HOURS * MS_PER_HOUR);
            return backupFiles.stream().filter(file -> file.lastModified() < expiryMillis).collect(Collectors.toList());
        } else {
            return EMPTY_FILE_LIST;
        }
    }

    /**
     * Check nodes does not contain a node of type TestPlan or ThreadGroup
     * 
     * @param nodes
     *            the nodes to check for TestPlans or ThreadGroups
     */
    private static boolean checkAcceptableForTestFragment(JMeterTreeNode[] nodes) {
        return Arrays.stream(nodes).map(DefaultMutableTreeNode::getUserObject)
                .noneMatch(o -> o instanceof ThreadGroup || o instanceof TestPlan);
    }

    // package protected to allow access from test code
    void convertSubTree(HashTree tree) {
        for (Object o : new LinkedList<>(tree.list())) {
            JMeterTreeNode item = (JMeterTreeNode) o;
            convertSubTree(tree.getTree(item));
            TestElement testElement = item.getTestElement(); // requires JMeterTreeNode
            tree.replaceKey(item, testElement);
        }
    }
    
    private static class PrivatePatternFileFilter implements IOFileFilter {
        
        private Pattern pattern;
        
        public PrivatePatternFileFilter(Pattern pattern) {
            if(pattern == null) {
                throw new IllegalArgumentException("pattern cannot be null !"); //$NON-NLS-1$
            }
            this.pattern = pattern;
        }
        
        @Override
        public boolean accept(File dir, String fileName) {
            return pattern.matcher(fileName).matches();
        }
        
        @Override
        public boolean accept(File file) {
            return accept(file.getParentFile(), file.getName());
        }
    }
    
}
