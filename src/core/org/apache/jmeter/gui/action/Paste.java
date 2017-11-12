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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Places a copied JMeterTreeNode under the selected node.
 */
public class Paste extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(Paste.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.PASTE);
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeNode[] draggedNodes = Copy.getCopiedNodes();
        if (draggedNodes == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        if (MenuFactory.canAddTo(currentNode, draggedNodes)) {
            Arrays.stream(draggedNodes)
                    .filter(Objects::nonNull)
                    .forEach(draggedNode -> addNode(currentNode, draggedNode));
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        GuiPackage.getInstance().getMainFrame().repaint();
    }

    private void addNode(JMeterTreeNode parent, JMeterTreeNode node) {
        try {
            // Add this node
            JMeterTreeNode newNode = GuiPackage.getInstance().getTreeModel().addComponent(node.getTestElement(), parent);
            // Add all the child nodes of the node we are adding
            for (int i = 0; i < node.getChildCount(); i++) {
                addNode(newNode, (JMeterTreeNode)node.getChildAt(i));
            }
        } catch (IllegalUserActionException iuae) {
            log.error("Illegal user action while adding a tree node.", iuae); // $NON-NLS-1$
            JMeterUtils.reportErrorToUser(iuae.getMessage());
        }
    }
}
