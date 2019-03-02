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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Add Parent menu command
 */
public class AddParent extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(AddParent.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.ADD_PARENT);
    }

    public AddParent() {
    }

    @Override
    public void doAction(ActionEvent e) {
        String name = ((Component) e.getSource()).getName();
        GuiPackage guiPackage = GuiPackage.getInstance();
        try {
            guiPackage.updateCurrentNode();
            TestElement controller = guiPackage.createTestElement(name);
            addParentToTree(controller);
        } catch (Exception err) {
            log.error("Exception while adding a TestElement.", err);
        }

    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    protected void addParentToTree(TestElement newParent) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode newNode = new JMeterTreeNode(newParent, guiPackage.getTreeModel());
        JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        int index = parentNode.getIndex(currentNode);
        guiPackage.getTreeModel().insertNodeInto(newNode, parentNode, index);
        JMeterTreeNode[] nodes = guiPackage.getTreeListener().getSelectedNodes();
        for (JMeterTreeNode node : nodes) {
            moveNode(guiPackage, node, newNode);
        }
    }

    private void moveNode(GuiPackage guiPackage, JMeterTreeNode node, JMeterTreeNode newParentNode) {
        guiPackage.getTreeModel().removeNodeFromParent(node);
        guiPackage.getTreeModel().insertNodeInto(node, newParentNode, newParentNode.getChildCount());
    }
}
