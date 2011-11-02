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

package org.apache.jmeter.report.gui.tree;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.ReportGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ReportPlan;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.util.NameUpdater;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

public class ReportTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 240L;

    public ReportTreeModel() {
        super(new ReportTreeNode(new ReportGui().createTestElement(), null));
        initTree();
    }

    /**
     * Returns a list of tree nodes that hold objects of the given class type.
     * If none are found, an empty list is returned.
     */
    public List<ReportTreeNode> getNodesOfType(Class<?> type) {
        List<ReportTreeNode> nodeList = new LinkedList<ReportTreeNode>();
        traverseAndFind(type, (ReportTreeNode) this.getRoot(), nodeList);
        return nodeList;
    }

    /**
     * Get the node for a given TestElement object.
     */
    public ReportTreeNode getNodeOf(TestElement userObject) {
        return traverseAndFind(userObject, (ReportTreeNode) getRoot());
    }

    /**
     * Adds the sub tree at the given node. Returns a boolean indicating whether
     * the added sub tree was a full test plan.
     */
    public HashTree addSubTree(HashTree subTree, ReportTreeNode current)
            throws IllegalUserActionException {
        Iterator<Object> iter = subTree.list().iterator();
        while (iter.hasNext()) {
            TestElement item = (TestElement) iter.next();
            if (item instanceof ReportPlan) {
                current = (ReportTreeNode) ((ReportTreeNode) getRoot())
                        .getChildAt(0);
                ((TestElement) current.getUserObject()).addTestElement(item);
                ((ReportPlan) current.getUserObject()).setName(item.getName());
                addSubTree(subTree.getTree(item), current);
            } else {
                if (subTree.getTree(item) != null) {
                    addSubTree(subTree.getTree(item), addComponent(item, current));
                }
            }
        }
        return getCurrentSubTree(current);
    }

    public ReportTreeNode addComponent(TestElement component,
            ReportTreeNode node) throws IllegalUserActionException {
        if (node.getUserObject() instanceof AbstractConfigGui) {
            throw new IllegalUserActionException(
                    "This node cannot hold sub-elements");
        }
        component.setProperty(TestElement.GUI_CLASS, NameUpdater
                .getCurrentName(component
                        .getPropertyAsString(TestElement.GUI_CLASS)));
        ReportGuiPackage.getInstance().updateCurrentNode();
        JMeterGUIComponent guicomp = ReportGuiPackage.getInstance().getGui(component);
        guicomp.configure(component);
        guicomp.modifyTestElement(component);
        ReportGuiPackage.getInstance().getCurrentGui(); // put the gui object back
        // to the way it was.
        ReportTreeNode newNode = new ReportTreeNode(component, this);

        // This check the state of the TestElement and if returns false it
        // disable the loaded node
        try {
            if (component.getProperty(TestElement.ENABLED) instanceof NullProperty
                    || component.getPropertyAsBoolean(TestElement.ENABLED)) {
                newNode.setEnabled(true);
            } else {
                newNode.setEnabled(false);
            }
        } catch (Exception e) {
            newNode.setEnabled(true);
        }

        this.insertNodeInto(newNode, node, node.getChildCount());
        return newNode;
    }

    public void removeNodeFromParent(ReportTreeNode node) {
        if (!(node.getUserObject() instanceof ReportPlan)) {
            super.removeNodeFromParent(node);
        }
    }

    private void traverseAndFind(Class<?> type, ReportTreeNode node, List<ReportTreeNode> nodeList) {
        if (type.isInstance(node.getUserObject())) {
            nodeList.add(node);
        }
        @SuppressWarnings("unchecked") // OK
        Enumeration<ReportTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            ReportTreeNode child = enumNode.nextElement();
            traverseAndFind(type, child, nodeList);
        }
    }

    private ReportTreeNode traverseAndFind(TestElement userObject,
            ReportTreeNode node) {
        if (userObject == node.getUserObject()) {
            return node;
        }
        @SuppressWarnings("unchecked") // OK
        Enumeration<ReportTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            ReportTreeNode child = enumNode.nextElement();
            ReportTreeNode result = traverseAndFind(userObject, child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public HashTree getCurrentSubTree(ReportTreeNode node) {
        ListedHashTree hashTree = new ListedHashTree(node);
        @SuppressWarnings("unchecked") // OK
        Enumeration<ReportTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            ReportTreeNode child = enumNode.nextElement();
            hashTree.add(node, getCurrentSubTree(child));
        }
        return hashTree;
    }

    public HashTree getReportPlan() {
        return getCurrentSubTree((ReportTreeNode) ((ReportTreeNode) this
                .getRoot()).getChildAt(0));
    }

    public void clearTestPlan() {
        super.removeNodeFromParent((ReportTreeNode) getChild(getRoot(), 0));
        initTree();
    }

    private void initTree() {
        TestElement rp = new ReportGui().createTestElement();
        this.insertNodeInto(new ReportTreeNode(rp, this),
                (ReportTreeNode) getRoot(), 0);
    }
}
