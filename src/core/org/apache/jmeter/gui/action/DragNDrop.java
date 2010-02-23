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

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;

public class DragNDrop extends AbstractAction {
    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.DRAG_ADD);
        commands.add(ActionNames.INSERT_BEFORE);
        commands.add(ActionNames.INSERT_AFTER);
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String action = e.getActionCommand();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode[] draggedNodes = guiPackage.getTreeListener().getDraggedNodes();
        JMeterTreeListener treeListener = guiPackage.getTreeListener();
        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        TestElement te = currentNode.getTestElement();
        if (te instanceof TestPlan || te instanceof WorkBench) {
            parentNode = null; // So elements can only be added as children
        }

        if (ActionNames.DRAG_ADD.equals(action) && canAddTo(currentNode,draggedNodes)) {
            removeNodesFromParents(draggedNodes);
            for (int i = 0; i < draggedNodes.length; i++) {
                GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], currentNode,
                        currentNode.getChildCount());
            }
        } else if (parentNode != null) {
            if (ActionNames.INSERT_BEFORE.equals(action) && canAddTo(parentNode,draggedNodes)) {
                removeNodesFromParents(draggedNodes);
                for (int i = 0; i < draggedNodes.length; i++) {
                    int index = parentNode.getIndex(currentNode);
                    GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
                }
            } else if (ActionNames.INSERT_AFTER.equals(action) && canAddTo(parentNode,draggedNodes)) {
                removeNodesFromParents(draggedNodes);
                for (int i = 0; i < draggedNodes.length; i++) {
                    int index = parentNode.getIndex(currentNode) + 1;
                    GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
                }
            }
        }
        GuiPackage.getInstance().getMainFrame().repaint();
    }

    private static boolean canAddTo(JMeterTreeNode parentNode, JMeterTreeNode[] draggedNodes) {
        boolean ok = MenuFactory.canAddTo(parentNode, draggedNodes);
        if (!ok){
            Toolkit.getDefaultToolkit().beep();
        }
        return ok;
    }

    private void removeNodesFromParents(JMeterTreeNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            GuiPackage.getInstance().getTreeModel().removeNodeFromParent(nodes[i]);
        }
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
