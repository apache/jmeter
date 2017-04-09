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

package org.apache.jmeter.gui.tree;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

public class JMeterTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 240L;

    public JMeterTreeModel(TestElement tp, TestElement wb) {
        super(new JMeterTreeNode(wb, null));
        initTree(tp,wb);
    }

    public JMeterTreeModel() {
        this(new TestPlanGui().createTestElement(),new WorkBenchGui().createTestElement());
    }

    /**
     * Hack to allow TreeModel to be used in non-GUI and headless mode.
     *
     * @deprecated - only for use by JMeter class!
     * @param o - dummy
     */
    @Deprecated
    public JMeterTreeModel(Object o) {
        this(new TestPlan(),new WorkBench());
    }

    /**
     * Returns a list of tree nodes that hold objects of the given class type.
     * If none are found, an empty list is returned.
     * @param type The type of nodes, which are to be collected
     * @return a list of tree nodes of the given <code>type</code>, or an empty list
     */
    public List<JMeterTreeNode> getNodesOfType(Class<?> type) {
        List<JMeterTreeNode> nodeList = new LinkedList<>();
        traverseAndFind(type, (JMeterTreeNode) this.getRoot(), nodeList);
        return nodeList;
    }

    /**
     * Get the node for a given TestElement object.
     * @param userObject The object to be found in this tree
     * @return the node corresponding to the <code>userObject</code>
     */
    public JMeterTreeNode getNodeOf(TestElement userObject) {
        return traverseAndFind(userObject, (JMeterTreeNode) getRoot());
    }

    /**
     * Adds the sub tree at the given node. Returns a boolean indicating whether
     * the added sub tree was a full test plan.
     * 
     * @param subTree
     *            The {@link HashTree} which is to be inserted into
     *            <code>current</code>
     * @param current
     *            The node in which the <code>subTree</code> is to be inserted.
     *            Will be overridden, when an instance of {@link TestPlan} or
     *            {@link WorkBench} is found in the subtree.
     * @return newly created sub tree now found at <code>current</code>
     * @throws IllegalUserActionException
     *             when <code>current</code> is not an instance of
     *             {@link AbstractConfigGui} and no instance of {@link TestPlan}
     *             or {@link WorkBench} could be found in the
     *             <code>subTree</code>
     */
    public HashTree addSubTree(HashTree subTree, JMeterTreeNode current) throws IllegalUserActionException {
        for (Object o : subTree.list()) {
            TestElement item = (TestElement) o;
            if (item instanceof TestPlan) {
                TestPlan tp = (TestPlan) item;
                current = (JMeterTreeNode) ((JMeterTreeNode) getRoot()).getChildAt(0);
                final TestPlan userObject = (TestPlan) current.getUserObject();
                userObject.addTestElement(item);
                userObject.setName(item.getName());
                userObject.setFunctionalMode(tp.isFunctionalMode());
                userObject.setSerialized(tp.isSerialized());
                addSubTree(subTree.getTree(item), current);
            } else if (item instanceof WorkBench) {
                current = (JMeterTreeNode) ((JMeterTreeNode) getRoot()).getChildAt(1);
                final TestElement testElement = (TestElement) current.getUserObject();
                testElement.addTestElement(item);
                testElement.setName(item.getName());
                addSubTree(subTree.getTree(item), current);
            } else {
                addSubTree(subTree.getTree(item), addComponent(item, current));
            }
        }
        return getCurrentSubTree(current);
    }

    /**
     * Add a {@link TestElement} to a {@link JMeterTreeNode}
     * @param component The {@link TestElement} to be used as data for the newly created node
     * @param node The {@link JMeterTreeNode} into which the newly created node is to be inserted
     * @return new {@link JMeterTreeNode} for the given <code>component</code>
     * @throws IllegalUserActionException
     *             when the user object for the <code>node</code> is not an instance
     *             of {@link AbstractConfigGui}
     */
    public JMeterTreeNode addComponent(TestElement component, JMeterTreeNode node) throws IllegalUserActionException {
        if (node.getUserObject() instanceof AbstractConfigGui) {
            throw new IllegalUserActionException("This node cannot hold sub-elements");
        }

        GuiPackage guiPackage = GuiPackage.getInstance();
        if (guiPackage != null) {
            // The node can be added in non GUI mode at startup
            guiPackage.updateCurrentNode();
            JMeterGUIComponent guicomp = guiPackage.getGui(component);
            guicomp.clearGui();
            guicomp.configure(component);
            guicomp.modifyTestElement(component);
            guiPackage.getCurrentGui(); // put the gui object back
                                        // to the way it was.
        }
        JMeterTreeNode newNode = new JMeterTreeNode(component, this);

        // This check the state of the TestElement and if returns false it
        // disable the loaded node
        try {
            newNode.setEnabled(component.isEnabled());
        } catch (Exception e) { // TODO - can this ever happen?
            newNode.setEnabled(true);
        }

        this.insertNodeInto(newNode, node, node.getChildCount());
        return newNode;
    }

    public void removeNodeFromParent(JMeterTreeNode node) {
        if (!(node.getUserObject() instanceof TestPlan) && !(node.getUserObject() instanceof WorkBench)) {
            super.removeNodeFromParent(node);
        }
    }

    private void traverseAndFind(Class<?> type, JMeterTreeNode node, List<JMeterTreeNode> nodeList) {
        if (type.isInstance(node.getUserObject())) {
            nodeList.add(node);
        }
        Enumeration<JMeterTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = enumNode.nextElement();
            traverseAndFind(type, child, nodeList);
        }
    }

    private JMeterTreeNode traverseAndFind(TestElement userObject, JMeterTreeNode node) {
        if (userObject == node.getUserObject()) {
            return node;
        }
        Enumeration<JMeterTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = enumNode.nextElement();
            JMeterTreeNode result = traverseAndFind(userObject, child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Get the current sub tree for a {@link JMeterTreeNode}
     * @param node The {@link JMeterTreeNode} from which the sub tree is to be taken 
     * @return newly copied sub tree
     */
    public HashTree getCurrentSubTree(JMeterTreeNode node) {
        ListedHashTree hashTree = new ListedHashTree(node);
        Enumeration<JMeterTreeNode> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = enumNode.nextElement();
            hashTree.add(node, getCurrentSubTree(child));
        }
        return hashTree;
    }

    /**
     * Get the {@link TestPlan} from the root of this tree
     * @return The {@link TestPlan} found at the root of this tree
     */
    public HashTree getTestPlan() {
        return getCurrentSubTree((JMeterTreeNode) ((JMeterTreeNode) this.getRoot()).getChildAt(0));
    }

    /**
     * Get the {@link WorkBench} from the root of this tree
     * @return The {@link WorkBench} found at the root of this tree
     */
    public HashTree getWorkBench() {
        return getCurrentSubTree((JMeterTreeNode) ((JMeterTreeNode) this.getRoot()).getChildAt(1));
    }

    /**
     * Clear the test plan, and use default node for test plan and workbench.
     *
     * N.B. Should only be called by {@link GuiPackage#clearTestPlan()}
     */
    public void clearTestPlan() {
        TestElement tp = new TestPlanGui().createTestElement();
        clearTestPlan(tp);
    }

    /**
     * Clear the test plan, and use specified node for test plan and default node for workbench
     *
     * N.B. Should only be called by {@link GuiPackage#clearTestPlan(TestElement)}
     *
     * @param testPlan the node to use as the testplan top node
     */
    public void clearTestPlan(TestElement testPlan) {
        // Remove the workbench and testplan nodes
        int children = getChildCount(getRoot());
        while (children > 0) {
            JMeterTreeNode child = (JMeterTreeNode)getChild(getRoot(), 0);
            super.removeNodeFromParent(child);
            children = getChildCount(getRoot());
        }
        // Init the tree
        initTree(testPlan,new WorkBenchGui().createTestElement()); // Assumes this is only called from GUI mode
    }

    /**
     * Initialize the model with nodes for testplan and workbench.
     *
     * @param tp the element to use as testplan
     * @param wb the element to use as workbench
     */
    private void initTree(TestElement tp, TestElement wb) {
        // Insert the test plan node
        insertNodeInto(new JMeterTreeNode(tp, this), (JMeterTreeNode) getRoot(), 0);
        // Insert the workbench node
        insertNodeInto(new JMeterTreeNode(wb, this), (JMeterTreeNode) getRoot(), 1);
        // Let others know that the tree content has changed.
        // This should not be necessary, but without it, nodes are not shown when the user
        // uses the Close menu item
        nodeStructureChanged((JMeterTreeNode)getRoot());
    }
}
