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

package org.apache.jmeter.visualizers;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;

/**
 * TreeNode that holds flags for:
 * <ul>
 *      <li>nodeHasMatched : It matches a search</li>
 *      <li>childrenNodesHaveMatched : One of its children matches a search</li>
 * </ul>
 * @since 3.0
 */
public class SearchableTreeNode extends DefaultMutableTreeNode {
    /**
     *
     */
    private static final long serialVersionUID = 5222625456347899544L;

    private boolean nodeHasMatched;

    private boolean childrenNodesHaveMatched;

    private transient DefaultTreeModel treeModel;

    public SearchableTreeNode() {
        this((SampleResult) null, null);
    }

    public SearchableTreeNode(SampleResult userObj, DefaultTreeModel treeModel) {
        super(userObj);
        this.treeModel = treeModel;
    }

    public SearchableTreeNode(AssertionResult userObj, DefaultTreeModel treeModel) {
        super(userObj);
        this.treeModel = treeModel;
    }

    public void reset() {
        nodeHasMatched = false;
        childrenNodesHaveMatched = false;
    }

    public void updateState() {
        if(treeModel != null) {
            treeModel.nodeChanged(this);
        }
    }

    /**
     * @return the nodeHasMatched
     */
    public boolean isNodeHasMatched() {
        return nodeHasMatched;
    }

    /**
     * @param nodeHasMatched the nodeHasMatched to set
     */
    public void setNodeHasMatched(boolean nodeHasMatched) {
        this.nodeHasMatched = nodeHasMatched;
    }

    /**
     * @return the childrenNodesHaveMatched
     */
    public boolean isChildrenNodesHaveMatched() {
        return childrenNodesHaveMatched;
    }

    /**
     * @param childrenNodesHaveMatched the childrenNodesHaveMatched to set
     */
    public void setChildrenNodesHaveMatched(boolean childrenNodesHaveMatched) {
        this.childrenNodesHaveMatched = childrenNodesHaveMatched;
    }
}
