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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Implements the Add Parent menu command
 */
public class ChangeParent implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.CHANGE_PARENT);
    }

    public ChangeParent() {
    }

    @Override
    public void doAction(ActionEvent e) {
        String name = ((Component) e.getSource()).getName();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
        if (!(currentNode.getUserObject() instanceof Controller)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            guiPackage.updateCurrentNode();
            TestElement controller = guiPackage.createTestElement(name);
            changeParent(controller, guiPackage, currentNode);
        } catch (Exception err) {
            Toolkit.getDefaultToolkit().beep();
            log.error("Failed to change parent", err);
        }

    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    private void changeParent(TestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        JMeterTreeModel treeModel = guiPackage.getTreeModel();
        JMeterTreeNode newNode = new JMeterTreeNode(newParent, treeModel);
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        int index = parentNode.getIndex(currentNode);
        treeModel.insertNodeInto(newNode, parentNode, index);
        treeModel.removeNodeFromParent(currentNode);
        int childCount = currentNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            // Using index 0 is voluntary as child is removed in next step and added to new parent
            JMeterTreeNode node = (JMeterTreeNode) currentNode.getChildAt(0);
            treeModel.removeNodeFromParent(node);
            treeModel.insertNodeInto(node, newNode, newNode.getChildCount());
        }
    }

}
