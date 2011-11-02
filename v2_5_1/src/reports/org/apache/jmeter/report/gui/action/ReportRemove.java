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
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.action.ReportCheckDirty;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.testelement.TestElement;

public class ReportRemove implements Command {
    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add("remove");
    }

    /**
     * Constructor for the Remove object
     */
    public ReportRemove() {
    }

    /**
     * Gets the ActionNames attribute of the Remove object.
     *
     * @return the ActionNames value
     */
    public Set<String> getActionNames() {
        return commands;
    }

    public void doAction(ActionEvent e) {
        ReportActionRouter.getInstance().actionPerformed(
                new ActionEvent(e.getSource(), e.getID(),
                        ReportCheckDirty.REMOVE));
        ReportGuiPackage guiPackage = ReportGuiPackage.getInstance();
        ReportTreeNode[] nodes = guiPackage.getTreeListener()
                .getSelectedNodes();
        TreePath newTreePath = // Save parent node for later
        guiPackage.getTreeListener().removedSelectedNode();
        for (int i = nodes.length - 1; i >= 0; i--) {
            removeNode(nodes[i]);
        }
        guiPackage.getTreeListener().getJTree().setSelectionPath(newTreePath);
        guiPackage.updateCurrentGui();
    }

    public static void removeNode(ReportTreeNode node) {
        TestElement testElement = node.getTestElement();
        if (testElement.canRemove()) {
            ReportGuiPackage.getInstance().getTreeModel().removeNodeFromParent(
                    node);
            ReportGuiPackage.getInstance().removeNode(testElement);
        } else {
            String message = testElement.getClass().getName() + " is busy";
            JOptionPane.showMessageDialog(null, message, "Cannot remove item",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
