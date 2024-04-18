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

package org.apache.jmeter.gui.util;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;

public final class ChangeElement {

    private static void updateElement(AbstractTestElement newElement, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        AbstractTestElement currentElement = (AbstractTestElement) currentNode.getUserObject();
        if (StringUtils.isNotBlank(currentElement.getName())) {
            newElement.setName(currentElement.getName());
        }

        JMeterTreeModel treeModel = guiPackage.getTreeModel();
        JMeterTreeNode newNode = new JMeterTreeNode(newElement, treeModel);
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        int index = parentNode.getIndex(currentNode);
        treeModel.insertNodeInto(newNode, parentNode, index);
        treeModel.removeNodeFromParent(currentNode);

        int childCount = currentNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            JMeterTreeNode node = (JMeterTreeNode) currentNode.getChildAt(0);
            treeModel.removeNodeFromParent(node);
            treeModel.insertNodeInto(node, newNode, newNode.getChildCount());
        }

        // select the new node
        TreeNode[] nodes = treeModel.getPathToRoot(newNode);
        JTree tree = guiPackage.getTreeListener().getJTree();
        tree.setSelectionPath(new TreePath(nodes));
    }

    public static void configElement(AbstractTestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(newParent, guiPackage, currentNode);
    }

    public static void threadGroup(AbstractThreadGroup threadGroup, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(threadGroup, guiPackage, currentNode);
    }

    public static void controller(GenericController newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(newParent, guiPackage, currentNode);
    }

    public static void sampler(Sampler newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement((AbstractTestElement) newParent, guiPackage, currentNode);
    }

    public static void timer(AbstractTestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(newParent, guiPackage, currentNode);
    }

    public static void listener(TestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement((AbstractTestElement) newParent, guiPackage, currentNode);
    }

    public static void assertion(TestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement((AbstractTestElement) newParent, guiPackage, currentNode);
    }

    public static void preProcessor(AbstractTestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(newParent, guiPackage, currentNode);
    }

    public static void postProcessor(AbstractTestElement newParent, GuiPackage guiPackage, JMeterTreeNode currentNode) {
        updateElement(newParent, guiPackage, currentNode);
    }
}
