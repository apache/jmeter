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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;

/**
 * Move a node up/down/left/right 
 *
 */
public class Move extends AbstractAction {
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.MOVE_DOWN);
        commands.add(ActionNames.MOVE_UP);
        commands.add(ActionNames.MOVE_LEFT);
        commands.add(ActionNames.MOVE_RIGHT);
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeListener treeListener = GuiPackage.getInstance()
                .getTreeListener();

        if (treeListener.getSelectedNodes().length != 1) {
            // we can only move a single node
            return;
        }

        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        JMeterTreeNode parentNode = getParentNode(currentNode);

        if (parentNode != null) {
            String action = e.getActionCommand();
            int index = parentNode.getIndex(currentNode);

            if (ActionNames.MOVE_UP.equals(action)) {
                if (index > 0) {
                    // we stay within the same parent node
                    int newIndx = index - 1;
                    moveAndSelectNode(currentNode, parentNode, newIndx);
                }
            } else if (ActionNames.MOVE_DOWN.equals(action)) {
                if (index < parentNode.getChildCount() - 1) {
                    // we stay within the same parent node
                    int newIndx = index + 1;
                    moveAndSelectNode(currentNode, parentNode, newIndx);
                }
            } else if (ActionNames.MOVE_LEFT.equals(action)) {
                JMeterTreeNode parentParentNode = getParentNode(parentNode);
                // move to the parent
                if (parentParentNode != null
                        && canAddTo(parentParentNode, currentNode)) {
                    moveAndSelectNode(currentNode, parentParentNode,
                            parentParentNode.getIndex(parentNode));
                }
            } else if (ActionNames.MOVE_RIGHT.equals(action)) {
                JMeterTreeNode after = (JMeterTreeNode) parentNode
                        .getChildAfter(currentNode);
                if (after != null && canAddTo(after, currentNode)) {
                    // move as a child of the next sibling
                    moveAndSelectNode(currentNode, after, 0);
                }
                // Commented as per sebb 
                // http://mail-archives.apache.org/mod_mbox/jmeter-dev/201307.mbox/%3CCAOGo0VZ0z3GMbfsq_gSB%2Bp7nTUqLng6Gy2ecvYbD8_AKb-Dt5w%40mail.gmail.com%3E
                /*
                else {
                    // move as a sibling of the parent
                    JMeterTreeNode parentParentNode = getParentNode(parentNode);
                    after = (JMeterTreeNode) parentParentNode
                            .getChildAfter(parentNode);
                    if (after != null
                            && canAddTo(parentParentNode, currentNode)) {
                        moveAndSelectNode(currentNode, parentParentNode,
                                parentParentNode.getIndex(after));
                    }
                }
                */
            }
        }

        GuiPackage.getInstance().getMainFrame().repaint();
    }

    private JMeterTreeNode getParentNode(JMeterTreeNode currentNode) {
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        TestElement te = currentNode.getTestElement();
        if (te instanceof TestPlan || te instanceof WorkBench) {
            parentNode = null; // So elements can only be added as children
        }
        return parentNode;
    }

    private static boolean canAddTo(JMeterTreeNode parentNode,
            JMeterTreeNode node) {
        boolean ok = MenuFactory.canAddTo(parentNode,
                new JMeterTreeNode[] { node });
        if (!ok) {
            Toolkit.getDefaultToolkit().beep();
        }
        return ok;
    }

    private static void moveAndSelectNode(JMeterTreeNode currentNode,
            JMeterTreeNode parentNode, int newIndx) {
        GuiPackage guiInstance = GuiPackage.getInstance();
        guiInstance.getTreeModel().removeNodeFromParent(currentNode);
        guiInstance.getTreeModel().insertNodeInto(currentNode, parentNode,
                newIndx);

        // select the node
        TreeNode[] nodes = guiInstance.getTreeModel()
                .getPathToRoot(currentNode);
        JTree jTree = guiInstance.getMainFrame().getTree();
        jTree.setSelectionPath(new TreePath(nodes));
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
