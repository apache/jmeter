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

/*
 * Created on Apr 9, 2003
 *
 * Clones a JMeterTreeNode
 */
package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;

/**
 * Implements the Copy menu command
 */
public class Copy extends AbstractAction {
    private static JMeterTreeNode copiedNode = null;

    private static JMeterTreeNode copiedNodes[] = null;

    private static final HashSet<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.COPY);
    }

    /*
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
        JMeterTreeNode[] nodes = treeListener.getSelectedNodes();
        setCopiedNodes(nodes);
    }

    public static JMeterTreeNode[] getCopiedNodes() {
        if (copiedNodes == null) { // can be null if Copy has yet to be used
            return null;
        }
        for (int i = 0; i < copiedNodes.length; i++) {
            if (copiedNodes[i] == null) {
                return null;
            }
        }
        return cloneTreeNodes(copiedNodes);
    }

    public static JMeterTreeNode getCopiedNode() {
        if (copiedNode == null) {
            return null;
        }
        return cloneTreeNode(copiedNode);
    }

    public static void setCopiedNode(JMeterTreeNode node) {
        copiedNode = cloneTreeNode(node);
    }

    public static JMeterTreeNode cloneTreeNode(JMeterTreeNode node) {
        JMeterTreeNode treeNode = (JMeterTreeNode) node.clone();
        treeNode.setUserObject(((TestElement) node.getUserObject()).clone());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    public static void setCopiedNodes(JMeterTreeNode nodes[]) {
        copiedNodes = new JMeterTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            copiedNodes[i] = cloneTreeNode(nodes[i]);
        }
    }

    public static JMeterTreeNode[] cloneTreeNodes(JMeterTreeNode nodes[]) {
        JMeterTreeNode treeNodes[] = new JMeterTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            treeNodes[i] = cloneTreeNode(nodes[i]);
        }
        return treeNodes;
    }

    private static void cloneChildren(JMeterTreeNode to, JMeterTreeNode from) {
        Enumeration<?> enumFrom = from.children();
        while (enumFrom.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumFrom.nextElement();
            JMeterTreeNode childClone = (JMeterTreeNode) child.clone();
            childClone.setUserObject(((TestElement) child.getUserObject()).clone());
            to.add(childClone);
            cloneChildren((JMeterTreeNode) to.getLastChild(), child);
        }
    }
}