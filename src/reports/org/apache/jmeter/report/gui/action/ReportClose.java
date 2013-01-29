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

package org.apache.jmeter.report.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This command clears the existing test plan, allowing the creation of a New
 * test plan.
 *
 */
public class ReportClose implements Command {

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add("close");
    }

    /**
     * Constructor for the Close object.
     */
    public ReportClose() {
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
    public void doAction(ActionEvent e) {
        ReportActionRouter.getInstance().doActionNow(
                new ActionEvent(e.getSource(), e.getID(),
                        ReportCheckDirty.CHECK_DIRTY));
        ReportGuiPackage guiPackage = ReportGuiPackage.getInstance();
        if (guiPackage.isDirty()) {
            if (JOptionPane.showConfirmDialog(ReportGuiPackage.getInstance()
                    .getMainFrame(), JMeterUtils
                    .getResString("cancel_new_to_save"), JMeterUtils // $NON-NLS-1$
                    .getResString("save?"), JOptionPane.YES_NO_OPTION, // $NON-NLS-1$
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                ReportActionRouter.getInstance().doActionNow(
                        new ActionEvent(e.getSource(), e.getID(), ActionNames.SAVE));
            }
        }
        guiPackage.getTreeModel().clearTestPlan();
        guiPackage.getTreeListener().getJTree().setSelectionRow(1);

        // Clear the name of the test plan file
        ReportGuiPackage.getInstance().setReportPlanFile(null);

        ReportActionRouter.getInstance().actionPerformed(
                new ActionEvent(e.getSource(), e.getID(), ActionNames.ADD_ALL));
    }
}
