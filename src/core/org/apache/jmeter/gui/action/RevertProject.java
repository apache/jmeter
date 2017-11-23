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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Handles the Revert Project command.
 *
 */
public class RevertProject extends AbstractActionWithNoRunningTest {
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.REVERT_PROJECT);
    }

    public RevertProject() {
        super();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doActionAfterCheck(ActionEvent e) {
        // Get the file name of the current project
        String projectFile = GuiPackage.getInstance().getTestPlanFile();
        // Check if the user has loaded any file
        if (projectFile == null) {
            return;
        }

        // Check if the user wants to drop any changes
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_DIRTY));
        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            // Check if the user wants to revert
            int response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("cancel_revert_project"), // $NON-NLS-1$
                    JMeterUtils.getResString("revert_project?"),  // $NON-NLS-1$
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                // Close the current project
                Close.closeProject(e);
                // Reload the project
                Load.loadProjectFile(e, new File(projectFile), false);
            }
        }
    }
}
