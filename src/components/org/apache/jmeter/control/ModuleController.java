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

package org.apache.jmeter.control;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.util.JMeterStopTestException;

/**
 * The goal of ModuleController is to add modularity to JMeter. The general idea
 * is that web applications consist of small units of functionality (i.e. Logon,
 * Create Account, Logoff...) which consist of requests that implement the
 * functionality. These small units of functionality can be stored in
 * SimpleControllers as modules that can be linked together quickly to form
 * tests. ModuleController facilitates this by acting as a pointer to any
 * controller that sits under the WorkBench. The controller and it's subelements
 * will be substituted in place of the ModuleController at runtime. Config
 * elements can be attached to the ModuleController to alter the functionality
 * (which user logs in, which account is created, etc.) of the module.
 *
 */
public class ModuleController extends GenericController implements ReplaceableController {

    private static final long serialVersionUID = 240L;

    private static final String NODE_PATH = "ModuleController.node_path";// $NON-NLS-1$

    private transient JMeterTreeNode selectedNode = null;

    /**
     * No-arg constructor
     *
     * @see java.lang.Object#Object()
     */
    public ModuleController() {
        super();
    }

    @Override
    public Object clone() {
        ModuleController clone = (ModuleController) super.clone();
        if (selectedNode == null) {
            this.restoreSelected();
        }
        // TODO Should we clone instead the selectedNode?
        clone.selectedNode = selectedNode; 
        return clone;
    }

    /**
     * Sets the (@link JMeterTreeNode) which represents the controller which
     * this object is pointing to. Used for building the test case upon
     * execution.
     *
     * @param tn
     *            JMeterTreeNode
     * @see org.apache.jmeter.gui.tree.JMeterTreeNode
     */
    public void setSelectedNode(JMeterTreeNode tn) {
        selectedNode = tn;
        setNodePath();
    }

    /**
     * Gets the (@link JMeterTreeNode) for the Controller
     *
     * @return JMeterTreeNode
     */
    public JMeterTreeNode getSelectedNode() {
        if (selectedNode == null){
            restoreSelected();
        }
        return selectedNode;
    }

    private void setNodePath() {
        List<String> nodePath = new ArrayList<>();
        if (selectedNode != null) {
            TreeNode[] path = selectedNode.getPath();
            for (TreeNode node : path) {
                nodePath.add(((JMeterTreeNode) node).getName());
            }
        }
        setProperty(new CollectionProperty(NODE_PATH, nodePath));
    }

    public List<?> getNodePath() {
        JMeterProperty prop = getProperty(NODE_PATH);
        if (!(prop instanceof NullProperty)) {
            return (List<?>) ((CollectionProperty) prop).getObjectValue();
        }
        return null;
    }

    private void restoreSelected() {
        GuiPackage gp = GuiPackage.getInstance();
        if (gp != null) {
            JMeterTreeNode root = (JMeterTreeNode) gp.getTreeModel().getRoot();
            resolveReplacementSubTree(root);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveReplacementSubTree(JMeterTreeNode context) {
        if (selectedNode == null) {
            List<?> nodePathList = getNodePath();
            if (nodePathList != null && !nodePathList.isEmpty()) {
                traverse(context, nodePathList, 1);
            }

            if(hasReplacementOccured() && selectedNode == null) {
                throw new JMeterStopTestException("ModuleController:"+getName()+" has no selected Controller (did you rename some element in the path to target controller?), test was shutdown as a consequence");
            }
        }
    }

    /**
     * In GUI Mode replacement occurs when test start
     * In Non GUI Mode replacement occurs before test runs
     * @return true if replacement occurred at the time method is called
     */
    private boolean hasReplacementOccured() {
        if(GuiPackage.getInstance() != null) {
            // GUI Mode
            return isRunningVersion();
        } else {
            return true;
        }
    }

    private void traverse(JMeterTreeNode node, List<?> nodePath, int level) {
        if (node != null && nodePath.size() > level) {
            for (int i = 0; i < node.getChildCount(); i++) {
                JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
                // Bug55375 - don't allow selectedNode to be a ModuleController as can cause recursion
                if (!(cur.getTestElement() instanceof ModuleController)) {
                    if (cur.getName().equals(nodePath.get(level).toString())) {
                        if (nodePath.size() == (level + 1)) {
                            selectedNode = cur;
                        }
                        traverse(cur, nodePath, level + 1);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashTree getReplacementSubTree() {
        HashTree tree = new ListedHashTree();
        if (selectedNode != null) {
            // Use a local variable to avoid replacing reference by modified clone (see Bug 54950)
            JMeterTreeNode nodeToReplace = selectedNode;
            // We clone to avoid enabling existing node
            if (!nodeToReplace.isEnabled()) {
                nodeToReplace = cloneTreeNode(selectedNode);
                nodeToReplace.setEnabled(true);
            }
            HashTree subtree = tree.add(nodeToReplace);
            createSubTree(subtree, nodeToReplace);
        }
        return tree;
    }

    private void createSubTree(HashTree tree, JMeterTreeNode node) {
        Enumeration<JMeterTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            JMeterTreeNode subNode = e.nextElement();
            tree.add(subNode);
            createSubTree(tree.getTree(subNode), subNode);
        }
    }

    private static JMeterTreeNode cloneTreeNode(JMeterTreeNode node) {
        JMeterTreeNode treeNode = (JMeterTreeNode) node.clone();
        treeNode.setUserObject(((TestElement) node.getUserObject()).clone());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    private static void cloneChildren(JMeterTreeNode to, JMeterTreeNode from) {
        Enumeration<JMeterTreeNode> enumr = from.children();
        while (enumr.hasMoreElements()) {
            JMeterTreeNode child = enumr.nextElement();
            JMeterTreeNode childClone = (JMeterTreeNode) child.clone();
            childClone.setUserObject(((TestElement) child.getUserObject()).clone());
            to.add(childClone);
            cloneChildren((JMeterTreeNode) to.getLastChild(), child);
        }
    }
}
