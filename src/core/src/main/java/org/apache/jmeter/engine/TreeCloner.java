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

import org.apache.jmeter.engine.util.LightweightClone;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Clones the test tree,  skipping test elements that implement {@link NoThreadClone} by default.
 * Elements implementing {@link LightweightClone} will share properties instead of deep cloning.
 */
public class TreeCloner implements HashTreeTraverser {

    /**
     * Property to enable/disable lightweight cloning for LightweightClone elements.
     * Can be disabled by setting {@code jmeter.clone.lightweight.enabled=false} in jmeter.properties.
     */
    private static final boolean LIGHTWEIGHT_CLONE_ENABLED =
            JMeterUtils.getPropDefault("jmeter.clone.lightweight.enabled", true);

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
        if (node instanceof TestElement testElement) {
            if (honourNoThreadClone && node instanceof NoThreadClone) {
                // Share completely - no clone
                newTree.add(objects, node);
                return node;
            } else if (LIGHTWEIGHT_CLONE_ENABLED
                    && node instanceof LightweightClone
                    && testElement instanceof AbstractTestElement abstractTestElement
                    && !abstractTestElement.hasVariableProperties()) {
                // Share properties, new instance with fresh transient state
                // Only use lightweight clone for elements WITHOUT variables
                // Elements with variables need full cloning for proper per-thread evaluation
                Object newNode = abstractTestElement.lightweightClone();
                newTree.add(objects, newNode);
                return newNode;
            } else {
                // Full deep clone
                Object newNode = testElement.clone();
                newTree.add(objects, newNode);
                return newNode;
            }
        }
        newTree.add(objects, node);
        return node;
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
