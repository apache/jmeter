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

import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.NamePanel;

public class ReportEditCommand implements Command {
    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add("edit");
    }

    public ReportEditCommand() {
    }

    public void doAction(ActionEvent e) {
        ReportGuiPackage guiPackage = ReportGuiPackage.getInstance();
        guiPackage.getMainFrame().setMainPanel((javax.swing.JComponent) guiPackage.getCurrentGui());
        guiPackage.getMainFrame().setEditMenu(guiPackage.getTreeListener().getCurrentNode().createPopupMenu());
        // TODO: I believe the following code (to the end of the method) is
        // obsolete,
        // since NamePanel no longer seems to be the GUI for any component:
        if (!(guiPackage.getCurrentGui() instanceof NamePanel)) {
            guiPackage.getMainFrame().setFileLoadEnabled(true);
            guiPackage.getMainFrame().setFileSaveEnabled(true);
        } else {
            guiPackage.getMainFrame().setFileLoadEnabled(false);
            guiPackage.getMainFrame().setFileSaveEnabled(false);
        }
    }

    public Set<String> getActionNames() {
        return commands;
    }
}
