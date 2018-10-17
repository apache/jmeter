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

package org.apache.jmeter.gui;

import java.io.Serializable;

import org.apache.jmeter.engine.TreeCloner;
import org.apache.jorphan.collections.HashTree;

/**
 * Undo history item
 * @since 2.12
 */
public class UndoHistoryItem implements Serializable {

    private static final long serialVersionUID = -8683007040160205040L;
    private final HashTree tree;
    // TODO: find a way to show this comment in menu item and toolbar tooltip
    private final String comment;
    private final TreeState treeState;
    private final boolean dirty;

    /**
     * This constructor is for Unit test purposes only
     * @deprecated DO NOT USE
     */
    @Deprecated
    public UndoHistoryItem() {
        this(null, null, null, false);
    }

    /**
     * @param copy {@link HashTree}
     * @param acomment String 
     * @param treeState {@link TreeState}
     * @param dirty boolean
     */
    public UndoHistoryItem(HashTree copy, String acomment, TreeState treeState, boolean dirty) {
        tree = copy;
        comment = acomment;
        this.treeState = treeState;
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public TreeState getTreeState() {
        return treeState;
    }

    /**
     * @return {@link org.apache.jorphan.collections.HashTree}
     */
    public HashTree getTree() {
        //EMI: It's important we return a clone here and not the actual tree because
        // the history item might still be part of some undo action and we don't
        // want to expose (and corrupt then via edits) internal data
        TreeCloner cloner = new TreeCloner(false);
        tree.traverse(cloner);

        return cloner.getClonedTree();
    }

    /**
     * @return String comment
     */
    public String getComment() {
        return comment;
    }
}
