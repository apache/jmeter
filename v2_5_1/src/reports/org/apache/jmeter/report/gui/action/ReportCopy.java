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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.action.AbstractAction;
import org.apache.jmeter.report.gui.tree.ReportTreeListener;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.testelement.TestElement;

public class ReportCopy extends AbstractAction {
    private static ReportTreeNode copiedNode = null;

    private static ReportTreeNode copiedNodes[] = null;

    private static final String COPY = "Copy";

    private static final HashSet<String> commands = new HashSet<String>();
    static {
        commands.add(COPY);
    }

    /*
     * @see org.apache.jmeter.report.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        ReportTreeListener treeListener = ReportGuiPackage.getInstance()
                .getTreeListener();
        ReportTreeNode[] nodes = treeListener.getSelectedNodes();
        setCopiedNodes(nodes);
    }

    public static ReportTreeNode[] getCopiedNodes() {
        for (int i = 0; i < copiedNodes.length; i++) {
            if (copiedNodes[i] == null) {
                return null;
            }
        }
        return cloneTreeNodes(copiedNodes);
    }

    public static ReportTreeNode getCopiedNode() {
        if (copiedNode == null) {
            return null;
        }
        return cloneTreeNode(copiedNode);
    }

    public static void setCopiedNode(ReportTreeNode node) {
        copiedNode = cloneTreeNode(node);
    }

    public static ReportTreeNode cloneTreeNode(ReportTreeNode node) {
        ReportTreeNode treeNode = (ReportTreeNode) node.clone();
        treeNode.setUserObject(((TestElement) node.getUserObject()).clone());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    public static void setCopiedNodes(ReportTreeNode nodes[]) {
        copiedNodes = new ReportTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            copiedNodes[i] = cloneTreeNode(nodes[i]);
        }
    }

    public static ReportTreeNode[] cloneTreeNodes(ReportTreeNode nodes[]) {
        ReportTreeNode treeNodes[] = new ReportTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            treeNodes[i] = cloneTreeNode(nodes[i]);
        }
        return treeNodes;
    }

    private static void cloneChildren(ReportTreeNode to, ReportTreeNode from) {
        @SuppressWarnings("unchecked") // OK
        Enumeration<ReportTreeNode> enumFrom = from.children();
        while (enumFrom.hasMoreElements()) {
            ReportTreeNode child = enumFrom.nextElement();
            ReportTreeNode childClone = (ReportTreeNode) child.clone();
            childClone.setUserObject(((TestElement) child.getUserObject())
                    .clone());
            to.add(childClone);
            cloneChildren((ReportTreeNode) to.getLastChild(), child);
        }
    }
}