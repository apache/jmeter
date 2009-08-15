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

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.action.AbstractAction;
import org.apache.jmeter.report.gui.tree.ReportTreeListener;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;

public class ReportDragNDrop extends AbstractAction {
    public static final String ADD = "drag_n_drop.add";//$NON-NLS-1$

    public static final String INSERT_BEFORE = "drag_n_drop.insert_before";//$NON-NLS-1$

    public static final String INSERT_AFTER = "drag_n_drop.insert_after";//$NON-NLS-1$

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add(ADD);
        commands.add(INSERT_BEFORE);
        commands.add(INSERT_AFTER);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String action = e.getActionCommand();
        ReportGuiPackage guiPackage = ReportGuiPackage.getInstance();
        ReportTreeNode[] draggedNodes = guiPackage.getTreeListener().getDraggedNodes();
        ReportTreeListener treeListener = guiPackage.getTreeListener();
        ReportTreeNode currentNode = treeListener.getCurrentNode();
        ReportTreeNode parentNode = (ReportTreeNode) currentNode.getParent();
        TestElement te = currentNode.getTestElement();
        if (te instanceof TestPlan || te instanceof WorkBench) {
            parentNode = null; // So elements can only be added as children
        }
        // System.out.println(action+" "+te.getClass().getName());

        if (ADD.equals(action) && canAddTo(currentNode)) {
            removeNodesFromParents(draggedNodes);
            for (int i = 0; i < draggedNodes.length; i++) {
                ReportGuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], currentNode,
                        currentNode.getChildCount());
            }
        } else if (INSERT_BEFORE.equals(action) && canAddTo(parentNode)) {
            removeNodesFromParents(draggedNodes);
            for (int i = 0; i < draggedNodes.length; i++) {
                @SuppressWarnings("null")
                int index = parentNode.getIndex(currentNode); // can't be null - this is checked by canAddTo
                ReportGuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
            }
        } else if (INSERT_AFTER.equals(action) && canAddTo(parentNode)) {
            removeNodesFromParents(draggedNodes);
            for (int i = 0; i < draggedNodes.length; i++) {
                @SuppressWarnings("null")
                int index = parentNode.getIndex(currentNode) + 1; // can't be null - this is checked by canAddTo
                ReportGuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], parentNode, index);
            }
        }
        ReportGuiPackage.getInstance().getMainFrame().repaint();
    }

    /**
     * Determine whether or not dragged nodes can be added to this parent. Also
     * used by Paste TODO tighten rules TODO move to MenuFactory?
     *
     * @param parentNode
     * @return whether it is OK to add the dragged nodes to this parent
     */
    static boolean canAddTo(ReportTreeNode parentNode) {
        if (null == parentNode) {
            return false;
        }
        TestElement te = parentNode.getTestElement();
        // System.out.println("Add to: "+te.getClass().getName());
        if (te instanceof Controller) {
            return true;
        }
        if (te instanceof Sampler) {
            return true;
        }
        if (te instanceof WorkBench) {
            return true;
        }
        if (te instanceof TestPlan) {
            return true;
        }
        return false;
    }

    protected void removeNodesFromParents(ReportTreeNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ReportGuiPackage.getInstance().getTreeModel().removeNodeFromParent(nodes[i]);
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
