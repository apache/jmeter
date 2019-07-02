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

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench; // NOSONAR We need this for backward compatibility
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

public class JMeterTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 240L;

    /**
     * Deprecated after remove WorkBench
     * @param tp - Test Plan
     * @param wb - WorkBench
     * @deprecated since 4.0
     */
    @Deprecated
    public JMeterTreeModel(TestElement tp, TestElement wb) {
        this(tp);
    }

    public JMeterTreeModel(TestElement tp) {
        super(new JMeterTreeNode(tp, null));
        initTree(tp);
    }

    public JMeterTreeModel() {
        this(new TestPlanGui().createTestElement());
    }

    /**
     * Hack to allow TreeModel to be used in non-GUI and headless mode.
     *
     * @deprecated - only for use by JMeter class!
     * @param o - dummy
     */
    @Deprecated
    public JMeterTreeModel(Object o) {
        this(new TestPlan());
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
     *            Will be overridden, when an instance of {@link TestPlan}
     * @return newly created sub tree now found at <code>current</code>
     * @throws IllegalUserActionException
     *             when <code>current</code> is not an instance of
     *             {@link AbstractConfigGui} and no instance of {@link TestPlan}
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
                //Move item from WorkBench to TestPlan
                HashTree workbenchTree = subTree.getTree(item);
                if (!workbenchTree.isEmpty()) {
                    moveWorkBenchToTestPlan(current, workbenchTree);
                }
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
        if (!(node.getUserObject() instanceof TestPlan)) {
            super.removeNodeFromParent(node);
        }
    }

    private void traverseAndFind(Class<?> type, JMeterTreeNode node, List<JMeterTreeNode> nodeList) {
        if (type.isInstance(node.getUserObject())) {
            nodeList.add(node);
        }
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode)enumNode.nextElement();
            traverseAndFind(type, child, nodeList);
        }
    }

    private JMeterTreeNode traverseAndFind(TestElement userObject, JMeterTreeNode node) {
        if (userObject == node.getUserObject()) {
            return node;
        }
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode)enumNode.nextElement();
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
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode)enumNode.nextElement();
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
     * Clear the test plan, and use default node for test plan.
     *
     * N.B. Should only be called by {@link GuiPackage#clearTestPlan()}
     */
    public void clearTestPlan() {
        TestElement tp = new TestPlanGui().createTestElement();
        clearTestPlan(tp);
    }

    /**
     * Clear the test plan, and use specified node for test plan
     *
     * N.B. Should only be called by {@link GuiPackage#clearTestPlan(TestElement)}
     *
     * @param testPlan the node to use as the testplan top node
     */
    public void clearTestPlan(TestElement testPlan) {
        // Remove testplan nodes
        int children = getChildCount(getRoot());
        while (children > 0) {
            JMeterTreeNode child = (JMeterTreeNode)getChild(getRoot(), 0);
            super.removeNodeFromParent(child);
            children = getChildCount(getRoot());
        }
        // Init the tree
        initTree(testPlan); // Assumes this is only called from GUI mode
    }

    /**
     * Initialize the model with nodes for testplan.
     *
     * @param tp the element to use as testplan
     */
    private void initTree(TestElement tp) {
        // Insert the test plan node
        insertNodeInto(new JMeterTreeNode(tp, this), (JMeterTreeNode) getRoot(), 0);
        // Let others know that the tree content has changed.
        // This should not be necessary, but without it, nodes are not shown when the user
        // uses the Close menu item
        nodeStructureChanged((JMeterTreeNode)getRoot());
    }


    /**
     * Move all Non-Test Elements from WorkBench to TestPlan root.
     * Other Test Elements will be move to WorkBench Test Fragment in TestPlan
     * @param current - TestPlan root
     * @param workbenchTree - WorkBench hash tree
     */
    private void moveWorkBenchToTestPlan(JMeterTreeNode current, HashTree workbenchTree) throws IllegalUserActionException {
        Object[] workbenchTreeArray = workbenchTree.getArray();
        if (GuiPackage.getInstance() != null) {
            for (Object node : workbenchTreeArray) {
                if (isNonTestElement(node)) {
                    HashTree subtree = workbenchTree.getTree(node);
                    workbenchTree.remove(node);
                    HashTree tree = new HashTree();
                    tree.add(node);
                    tree.add(node, subtree);
                    ((TestElement) node).setProperty(TestElement.ENABLED, false);
                    addSubTree(tree, current);
                }
            }
        }

        if (!workbenchTree.isEmpty()) {
            HashTree testFragmentTree = new HashTree();
            TestFragmentController testFragmentController = new TestFragmentController();
            testFragmentController.setProperty(TestElement.NAME, "WorkBench Test Fragment");
            testFragmentController.setProperty(TestElement.GUI_CLASS, TestFragmentControllerGui.class.getName());
            testFragmentController.setProperty(TestElement.ENABLED, false);
            testFragmentTree.add(testFragmentController);
            testFragmentTree.add(testFragmentController, workbenchTree);
            addSubTree(testFragmentTree, current);
        }
    }

    /**
     * Is element :
     * <ul>
     *  <li>HTTP(S) Test Script Recorder</li>
     *  <li>Mirror Server</li>
     *  <li>Property Display</li>
     * </ul>
     * @param node
     */
    private boolean isNonTestElement(Object node) {
        JMeterTreeNode treeNode = new JMeterTreeNode((TestElement) node, null);
        Collection<String> categories = treeNode.getMenuCategories();
        if (categories != null) {
            for (String category : categories) {
                if (MenuFactory.NON_TEST_ELEMENTS.equals(category)) {
                    return true;
                }
            }
        }
        return false;
    }
}
