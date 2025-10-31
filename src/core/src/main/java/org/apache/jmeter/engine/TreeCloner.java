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

package org.apache.jmeter.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Clones the test tree,  skipping test elements that implement {@link NoThreadClone} by default.
 */
public class TreeCloner implements HashTreeTraverser {

    private final ListedHashTree newTree;

    private final List<Object> objects = new ArrayList<>();

    private final boolean honourNoThreadClone;

    /**
     * Clone the test tree, honouring NoThreadClone markers.
     *
     */
    public TreeCloner() {
        this(true);
    }

    /**
     * Clone the test tree.
     *
     * @param honourNoThreadClone set false to clone NoThreadClone nodes as well
     */
    public TreeCloner(boolean honourNoThreadClone) {
        newTree = new ListedHashTree();
        this.honourNoThreadClone = honourNoThreadClone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addNode(Object node, HashTree subTree) {
        Object newNode = addNodeToTree(node);
        addLast(newNode);
    }

    /**
     * @param node Node to add to tree or not
     * @return Object node (clone or not)
     */
    protected Object addNodeToTree(Object node) {
        if ( (node instanceof TestElement testElement) // Check can cast for clone
           // Don't clone NoThreadClone unless honourNoThreadClone == false
          && !(honourNoThreadClone && node instanceof NoThreadClone)
        ) {
            Object newNode = testElement.clone();
            newTree.add(objects, newNode);
            return newNode;
        } else {
            newTree.add(objects, node);
            return node;
        }
    }

    /**
     * add node to objects LinkedList
     * @param node Object
     */
    private void addLast(Object node) {
        objects.add(node);
    }

    @Override
    public void subtractNode() {
        objects.remove(objects.size() - 1);
    }

    public ListedHashTree getClonedTree() {
        return newTree;
    }

    @Override
    public void processPath() {
        // NOOP
    }

}
