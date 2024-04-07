/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Allows to change Config element implementation
 */
@AutoService(Command.class)
public class ChangeConfigElement extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(ChangeConfigElement.class);
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CHANGE_CONFIG_ELEMENT);
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        String name = ((Component) e.getSource()).getName();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
        if (!(currentNode.getUserObject() instanceof ConfigElement)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            guiPackage.updateCurrentNode();
            ConfigElement configElement = (ConfigElement) guiPackage.createTestElement(name);
            changeConfigElement(configElement, guiPackage, currentNode);

        } catch (Exception err) {
            Toolkit.getDefaultToolkit().beep();
            log.error("Failed to change parent", err);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    private static void changeConfigElement(ConfigElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {

        // keep the old name if it was not the default one
        ConfigElement currentConfigElement = (ConfigElement) currentNode.getUserObject();
        JMeterGUIComponent currentGui = guiPackage.getCurrentGui();
        String defaultName = JMeterUtils.getResString(currentGui.getLabelResource());
        if(StringUtils.isNotBlank(currentConfigElement.getClass().getName())
                && !currentConfigElement.getClass().getName().equals(defaultName)){
//            newParent.setName(currentConfigElement.getClass().getName());
            log.info("Name change done");
        }

        JMeterTreeModel treeModel = guiPackage.getTreeModel();
        JMeterTreeNode newNode = new JMeterTreeNode((TestElement) newParent, treeModel);
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

        // select the node
        TreeNode[] nodes = treeModel.getPathToRoot(newNode);
        JTree tree = guiPackage.getTreeListener().getJTree();
        tree.setSelectionPath(new TreePath(nodes));
    }
}
