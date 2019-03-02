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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Implements the Remove menu item.
 */
public class Remove extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    // Whether to skip the delete confirmation dialogue
    private static final boolean SKIP_CONFIRM = JMeterUtils.getPropDefault("confirm.delete.skip", false); // $NON-NLS-1$

    static {
        commands.add(ActionNames.REMOVE);
    }

    /**
     * Constructor for the Remove object
     */
    public Remove() {
    }

    /**
     * Gets the ActionNames attribute of the Remove object.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {

        int isConfirm = SKIP_CONFIRM ? JOptionPane.YES_OPTION :
            JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("remove_confirm_msg"),// $NON-NLS-1$
                JMeterUtils.getResString("remove_confirm_title"), // $NON-NLS-1$
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (isConfirm == JOptionPane.YES_OPTION) {
            // TODO - removes the nodes from the CheckDirty map - should it be done later, in case some can't be removed?
            ActionRouter.getInstance().actionPerformed(new ActionEvent(e.getSource(), e.getID(), ActionNames.CHECK_REMOVE));
            GuiPackage guiPackage = GuiPackage.getInstance();
            JMeterTreeNode[] nodes = guiPackage.getTreeListener().getSelectedNodes();
            TreePath newTreePath = // Save parent node for later
            guiPackage.getTreeListener().removedSelectedNode();
            for (int i = nodes.length - 1; i >= 0; i--) {
                removeNode(nodes[i]);
            }
            guiPackage.getTreeListener().getJTree().setSelectionPath(newTreePath);
            guiPackage.updateCurrentGui();
        }
    }

    private static void removeNode(JMeterTreeNode node) {
        TestElement testElement = node.getTestElement();
        if (testElement.canRemove()) {
            GuiPackage.getInstance().getTreeModel().removeNodeFromParent(node);
            GuiPackage.getInstance().removeNode(testElement);
            testElement.removed();
        } else {
            String message = testElement.getClass().getName() + " is busy";
            JOptionPane.showMessageDialog(null, message, "Cannot remove item", JOptionPane.ERROR_MESSAGE);
        }
    }
}
