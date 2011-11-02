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
import org.apache.jmeter.report.gui.action.ReportActionRouter;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.report.gui.action.ReportSave;
import org.apache.jmeter.util.JMeterUtils;

public class ReportExitCommand implements Command {

    public static final String EXIT = "exit";

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add(EXIT);
    }

    /**
     * Constructor for the ExitCommand object
     */
    public ReportExitCommand() {
    }

    /**
     * Gets the ActionNames attribute of the ExitCommand object
     *
     * @return The ActionNames value
     */
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * Description of the Method
     *
     * @param e
     *            Description of Parameter
     */
    public void doAction(ActionEvent e) {
        ReportActionRouter.getInstance().doActionNow(
                new ActionEvent(e.getSource(), e.getID(),
                        ReportCheckDirty.CHECK_DIRTY));
        if (ReportGuiPackage.getInstance().isDirty()) {
            int chosenOption = JOptionPane.showConfirmDialog(ReportGuiPackage
                    .getInstance().getMainFrame(), JMeterUtils
                    .getResString("cancel_exit_to_save"), JMeterUtils
                    .getResString("Save?"), JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.NO_OPTION) {
                System.exit(0);
            } else if (chosenOption == JOptionPane.YES_OPTION) {
                ReportActionRouter.getInstance().doActionNow(
                        new ActionEvent(e.getSource(), e.getID(),
                                ReportSave.SAVE_ALL_AS));
                if (!ReportGuiPackage.getInstance().isDirty()) {
                    System.exit(0);
                }
            }
        } else {
            System.exit(0);
        }
    }
}
