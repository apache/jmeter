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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ReportAddParent implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add("Add Parent");
    }

    public ReportAddParent() {
    }

    public void doAction(ActionEvent e) {
        String name = ((Component) e.getSource()).getName();
        try {
            TestElement controller = ReportGuiPackage.getInstance()
                    .createTestElement(name);
            addParentToTree(controller);
        } catch (Exception err) {
            log.error("", err);
        }

    }

    public Set<String> getActionNames() {
        return commands;
    }

    protected void addParentToTree(TestElement newParent) {
        ReportGuiPackage guiPackage = ReportGuiPackage.getInstance();
        ReportTreeNode newNode = new ReportTreeNode(newParent, guiPackage
                .getTreeModel());
        ReportTreeNode currentNode = guiPackage.getTreeListener()
                .getCurrentNode();
        ReportTreeNode parentNode = (ReportTreeNode) currentNode.getParent();
        int index = parentNode.getIndex(currentNode);
        guiPackage.getTreeModel().insertNodeInto(newNode, parentNode, index);
        ReportTreeNode[] nodes = guiPackage.getTreeListener()
                .getSelectedNodes();
        for (int i = 0; i < nodes.length; i++) {
            moveNode(guiPackage, nodes[i], newNode);
        }
    }

    private void moveNode(ReportGuiPackage guiPackage, ReportTreeNode node,
            ReportTreeNode newParentNode) {
        guiPackage.getTreeModel().removeNodeFromParent(node);
        guiPackage.getTreeModel().insertNodeInto(node, newParentNode,
                newParentNode.getChildCount());
    }
}
