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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This command clears the existing test plan, allowing the creation of a New
 * test plan.
 *
 */
public class Close extends AbstractActionWithNoRunningTest {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CLOSE);
    }

    /**
     * Constructor for the Close object.
     */
    public Close() {
    }

    /**
     * Gets the ActionNames attribute of the Close object.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * This method performs the actual command processing.
     *
     * @param e
     *            the generic UI action event
     */
    @Override
    public void doActionAfterCheck(ActionEvent e) {
        performAction(e);
    }

    /**
     * Helper routine to allow action to be shared by LOAD.
     *
     * @param e event
     * @return true if Close was not cancelled
     */
    static boolean performAction(ActionEvent e){
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_DIRTY));
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            int response;
            if ((response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("cancel_new_to_save"), // $NON-NLS-1$
                    JMeterUtils.getResString("save?"),  // $NON-NLS-1$
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) == JOptionPane.YES_OPTION) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SAVE));
                // the user might cancel the file chooser dialog
                // in this case we should not close the test plan
                if (guiPackage.isDirty()) {
                    return false;
                }
            }
            if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION) {
                return false; // Don't clear the plan
            }
        }
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.STOP_THREAD));
        closeProject(e);
        return true;
    }

    static void closeProject(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();

        guiPackage.clearTestPlan();
        JTree tree = guiPackage.getTreeListener().getJTree();
        tree.setSelectionRow(0);
        FocusRequester.requestFocus(tree);
        FileServer.getFileServer().setScriptName(null);
        ActionRouter.getInstance().actionPerformed(new ActionEvent(e.getSource(), e.getID(), ActionNames.ADD_ALL));
    }
}
