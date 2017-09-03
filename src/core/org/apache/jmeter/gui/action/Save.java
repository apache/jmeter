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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
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
import org.apache.jmeter.testelement.WorkBench;
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
    public Save() {}

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
                subTree = GuiPackage.getInstance().getCurrentSubTree();
                // Create Test Fragment node
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
                                
                subTree = hashTree;
                
            } else {
                JMeterUtils.reportErrorToUser(
                        JMeterUtils.getResString("save_as_test_fragment_error"), // $NON-NLS-1$
                        JMeterUtils.getResString("save_as_test_fragment")); // $NON-NLS-1$
                return;
            }
        } else {
            fullSave = true;
            HashTree testPlan = GuiPackage.getInstance().getTreeModel().getTestPlan();
            // If saveWorkBench 
            if (isWorkbenchSaveable()) {
                HashTree workbench = GuiPackage.getInstance().getTreeModel().getWorkBench();
                testPlan.add(workbench);
            }
            subTree = testPlan;
        }

        String updateFile = GuiPackage.getInstance().getTestPlanFile();
        if (!ActionNames.SAVE.equals(e.getActionCommand()) || updateFile == null) {
            JFileChooser chooser = FileDialoger.promptToSaveFile(updateFile == null ? GuiPackage.getInstance().getTreeListener()
                    .getCurrentNode().getName()
                    + JMX_FILE_EXTENSION : updateFile);
            if (chooser == null) {
                return;
            }
            updateFile = chooser.getSelectedFile().getAbsolutePath();
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
                    return ; // Do not save, user does not want to overwrite
                }
            }

            if (!e.getActionCommand().equals(ActionNames.SAVE_AS)) {
                GuiPackage.getInstance().setTestPlanFile(updateFile);
            }
        }
        
        // backup existing file according to jmeter/user.properties settings
        List<File> expiredBackupFiles = EMPTY_FILE_LIST;
        File fileToBackup = new File(updateFile);
        try {
            expiredBackupFiles = createBackupFile(fileToBackup);
        } catch (Exception ex) {
            log.error("Failed to create a backup for {}", fileToBackup, ex); //$NON-NLS-1$
        }
        
        try {
            convertSubTree(subTree);
        } catch (Exception err) {
            if (log.isWarnEnabled()) {
                log.warn("Error converting subtree. {}", err.toString());
            }
        }

        try (FileOutputStream ostream = new FileOutputStream(updateFile)){
            SaveService.saveTree(subTree, ostream);
            if (fullSave) { // Only update the stored copy of the tree for a full save
                FileServer.getFileServer().setScriptName(new File(updateFile).getName());
                subTree = GuiPackage.getInstance().getTreeModel().getTestPlan(); // refetch, because convertSubTree affects it
                if (isWorkbenchSaveable()) {
                    HashTree workbench = GuiPackage.getInstance().getTreeModel().getWorkBench();
                    subTree.add(workbench);
                }
                ActionRouter.getInstance().doActionNow(new ActionEvent(subTree, e.getID(), ActionNames.SUB_TREE_SAVED));
            }
            
            // delete expired backups : here everything went right so we can
            // proceed to deletion
            for (File expiredBackupFile : expiredBackupFiles) {
                try {
                    FileUtils.deleteQuietly(expiredBackupFile);
                } catch (Exception ex) {
                    log.warn("Failed to delete backup file, {}", expiredBackupFile); //$NON-NLS-1$
                }
            }
        } catch(RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            log.error("Error saving tree.", ex);
            throw new IllegalUserActionException("Couldn't save test plan to file: " + updateFile, ex);
        } 

        GuiPackage.getInstance().updateCurrentGui();
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
        baseName = baseName.endsWith(JMX_FILE_EXTENSION) ? baseName.substring(0, baseName.length() - JMX_FILE_EXTENSION.length()) : baseName;
        // get a file to the backup directory
        File backupDir = new File(BACKUP_DIRECTORY);
        backupDir.mkdirs();
        if (!backupDir.isDirectory()) {
            log.error(
                    "Could not backup file ! Backup directory does not exist, is not a directory or could not be created ! <{}>", //$NON-NLS-1$
                    backupDir.getAbsolutePath()); //$NON-NLS-2$
        }

        /**
         *  select files matching
         * {baseName}{versionSeparator}{version}{jmxExtension}
         * where {version} is a 6 digits number
         */
        String backupPatternRegex = Pattern.quote(baseName + versionSeparator) + "([\\d]{6})" + Pattern.quote(JMX_FILE_EXTENSION); //$NON-NLS-1$
        Pattern backupPattern = Pattern.compile(backupPatternRegex);
        // create a file filter that select files matching a given regex pattern
        IOFileFilter patternFileFilter = new PrivatePatternFileFilter(backupPattern);
        // get all backup files in the backup directory
        List<File> backupFiles = new ArrayList<>(FileUtils.listFiles(backupDir, patternFileFilter, null));
        // find the highest version number among existing backup files (this
        // should be the more recent backup)
        int lastVersionNumber = 0;
        for (File backupFile : backupFiles) {
            Matcher matcher = backupPattern.matcher(backupFile.getName());
            if (matcher.find() && matcher.groupCount() > 0) {
                // parse version number from the backup file name
                // should never fail as it matches the regex
                int version = Integer.parseInt(matcher.group(1));
                lastVersionNumber = Math.max(lastVersionNumber, version);
            }
        }
        // find expired backup files
        List<File> expiredFiles = new ArrayList<>();
        if (BACKUP_MAX_HOURS > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -BACKUP_MAX_HOURS);
            long expiryDate = cal.getTime().getTime();
            // select expired files that should be deleted
            IOFileFilter expiredFileFilter = FileFilterUtils.ageFileFilter(expiryDate, true);
            expiredFiles.addAll(FileFilterUtils.filterList(expiredFileFilter, backupFiles));
        }
        // sort backups from by their last modified time
        Collections.sort(backupFiles, (o1, o2) -> {
            long diff = o1.lastModified() - o2.lastModified();
            // convert the long to an int in order to comply with the method
            // contract
            return diff < 0 ? -1 : diff > 0 ? 1 : 0;
        });
        /**
         *  backup name is of the form 
         * {baseName}{versionSeparator}{version}{jmxExtension}
         */
        String backupName = baseName + versionSeparator + BACKUP_VERSION_FORMATER.format(lastVersionNumber + 1L) + JMX_FILE_EXTENSION;
        File backupFile = new File(backupDir, backupName);
        // create file backup
        try {
            FileUtils.copyFile(fileToBackup, backupFile);
        } catch (IOException e) {
            log.error("Failed to backup file : {}", fileToBackup.getAbsolutePath(), e); //$NON-NLS-1$
            return EMPTY_FILE_LIST;
        }
        // add the fresh new backup file (list is still sorted here)
        backupFiles.add(backupFile);
        // unless max backups is not set, ensure that we don't keep more backups
        // than required
        if (BACKUP_MAX_COUNT > 0 && backupFiles.size() > BACKUP_MAX_COUNT) {
            // keep the most recent files in the limit of the specified max
            // count
            expiredFiles.addAll(backupFiles.subList(0, backupFiles.size() - BACKUP_MAX_COUNT));
        }
        return expiredFiles;
    }
    
    /**
     * check if the workbench should be saved
     */
    private boolean isWorkbenchSaveable() {
        JMeterTreeNode workbenchNode = (JMeterTreeNode) ((JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot()).getChildAt(1);
        return ((WorkBench) workbenchNode.getUserObject()).getSaveWorkBench();
    }

    /**
     * Check nodes does not contain a node of type TestPlan or ThreadGroup
     * @param nodes
     */
    private static boolean checkAcceptableForTestFragment(JMeterTreeNode[] nodes) {
        for (JMeterTreeNode node : nodes) {
            Object userObject = node.getUserObject();
            if (userObject instanceof ThreadGroup ||
                    userObject instanceof TestPlan) {
                return false;
            }
        }
        return true;
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
