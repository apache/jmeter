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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Processes the collapse and expand of a tree branch
 */
public class CollapseExpandTreeBranch extends AbstractAction {
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.COLLAPSE);
        commands.add(ActionNames.EXPAND);
    }

    /**
     * Constructor
     */
    public CollapseExpandTreeBranch() {}

    /**
     * Gets the ActionNames attribute of the CollapseExpandTreeBranch object.
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
     * @param e the generic UI action event
     */
    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();

        JTree jTree = GuiPackage.getInstance().getMainFrame().getTree();
        JMeterTreeNode[] selectedNodes = treeListener.getSelectedNodes();
        for (JMeterTreeNode currentNode : selectedNodes) {
            if (!currentNode.isLeaf()) {
                TreeNode[] nodes = GuiPackage.getInstance().getTreeModel().getPathToRoot(currentNode);
                TreePath path = new TreePath(nodes);
                boolean collapse = ActionNames.COLLAPSE.equals(e.getActionCommand());

                expandCollapseNode(jTree, path, collapse);
            }
        }
    }

    private void expandCollapseNode(JTree jTree, TreePath parent, boolean collapse) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.isLeaf()) {
            return;
        }

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            TreeNode child = (TreeNode) children.nextElement();
            TreePath path = parent.pathByAddingChild(child);
            expandCollapseNode(jTree, path, collapse);
        }

        if (collapse) {
            jTree.collapsePath(parent);
        } else {
            jTree.expandPath(parent);
        }
    }
}
