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

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.action.AbstractAction;
import org.apache.jmeter.report.gui.tree.ReportTreeListener;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;

/**
 * Places a copied JMeterTreeNode under the selected node.
 *
 */
public class ReportPaste extends AbstractAction {

    public static final String PASTE = "Paste"; //$NON-NLS-1$

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add(PASTE);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        ReportTreeNode draggedNodes[] = ReportCopy.getCopiedNodes();
        ReportTreeListener treeListener = ReportGuiPackage.getInstance().getTreeListener();
        ReportTreeNode currentNode = treeListener.getCurrentNode();
        if (ReportDragNDrop.canAddTo(currentNode)) {
            for (int i = 0; i < draggedNodes.length; i++) {
                if (draggedNodes[i] != null) {
                    ReportGuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNodes[i], currentNode,
                            currentNode.getChildCount());
                }
            }
        }
        ReportGuiPackage.getInstance().getMainFrame().repaint();
    }
}