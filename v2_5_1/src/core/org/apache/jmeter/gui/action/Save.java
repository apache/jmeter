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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Save the current test plan; implements:
 * Save
 * Save TestPlan As
 * Save (Selection) As
 */
public class Save implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String JMX_FILE_EXTENSION = ".jmx"; // $NON-NLS-1$

    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.SAVE_AS); // Save (Selection) As
        commands.add(ActionNames.SAVE_ALL_AS); // Save TestPlan As
        commands.add(ActionNames.SAVE); // Save
    }

    /**
     * Constructor for the Save object.
     */
    public Save() {
    }

    /**
     * Gets the ActionNames attribute of the Save object.
     *
     * @return the ActionNames value
     */
    public Set<String> getActionNames() {
        return commands;
    }

    public void doAction(ActionEvent e) throws IllegalUserActionException {
        HashTree subTree = null;
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
        } else {
            subTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
        }

        String updateFile = GuiPackage.getInstance().getTestPlanFile();
        if (!ActionNames.SAVE.equals(e.getActionCommand()) || updateFile == null) {
            JFileChooser chooser = FileDialoger.promptToSaveFile(GuiPackage.getInstance().getTreeListener()
                    .getCurrentNode().getName()
                    + JMX_FILE_EXTENSION);
            if (chooser == null) {
                return;
            }
            updateFile = chooser.getSelectedFile().getAbsolutePath();
            // Make sure the file ends with proper extension
            if(FilenameUtils.getExtension(updateFile).equals("")) {
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
        // TODO: doesn't putting this here mark the tree as
        // saved even though a failure may occur later?

        ActionRouter.getInstance().doActionNow(new ActionEvent(subTree, e.getID(), ActionNames.SUB_TREE_SAVED));
        try {
            convertSubTree(subTree);
        } catch (Exception err) {
        }
        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(updateFile);
            SaveService.saveTree(subTree, ostream);
        } catch (Throwable ex) {
            GuiPackage.getInstance().setTestPlanFile(null);
            log.error("", ex);
            if (ex instanceof Error){
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException){
                throw (RuntimeException) ex;
            }
            throw new IllegalUserActionException("Couldn't save test plan to file: " + updateFile);
        } finally {
            JOrphanUtils.closeQuietly(ostream);
        }
        GuiPackage.getInstance().updateCurrentGui();
    }

    // package protected to allow access from test code
    void convertSubTree(HashTree tree) {
        Iterator<Object> iter = new LinkedList<Object>(tree.list()).iterator();
        while (iter.hasNext()) {
            JMeterTreeNode item = (JMeterTreeNode) iter.next();
            convertSubTree(tree.getTree(item));
            TestElement testElement = item.getTestElement(); // requires JMeterTreeNode
            tree.replace(item, testElement);
        }
    }
}
