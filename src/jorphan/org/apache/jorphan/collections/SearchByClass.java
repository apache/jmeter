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

package org.apache.jorphan.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Useful for finding all nodes in the tree that represent objects of a
 * particular type. For instance, if your tree contains all strings, and a few
 * StringBuilder objects, you can use the SearchByClass traverser to find all
 * the StringBuilder objects in your tree.
 * <p>
 * Usage is simple. Given a {@link HashTree} object "tree", and a SearchByClass
 * object:
 *
 * <pre>
 * HashTree tree = new HashTree();
 * // ... tree gets filled with objects
 * SearchByClass searcher = new SearchByClass(StringBuilder.class);
 * tree.traverse(searcher);
 * Iterator iter = searcher.getSearchResults().iterator();
 * while (iter.hasNext()) {
 *     StringBuilder foundNode = (StringBuilder) iter.next();
 *     HashTree subTreeOfFoundNode = searcher.getSubTree(foundNode);
 *     // .... do something with node and subTree...
 * }
 * </pre>
 *
 * @see HashTree
 * @see HashTreeTraverser
 *
 * @param <T>
 *            Class that should be searched for
 */
public class SearchByClass<T> implements HashTreeTraverser {
    private final List<T> objectsOfClass = new LinkedList<>();

    private final Map<Object, ListedHashTree> subTrees = new HashMap<>();

    private final Class<T> searchClass;

    /**
     * Creates an instance of SearchByClass, and sets the Class to be searched
     * for.
     *
     * @param searchClass
     *            class to be searched for
     */
    public SearchByClass(Class<T> searchClass) {
        this.searchClass = searchClass;
    }

    /**
     * After traversing the HashTree, call this method to get a collection of
     * the nodes that were found.
     *
     * @return Collection All found nodes of the requested type
     */
    public Collection<T> getSearchResults() { // TODO specify collection type without breaking callers
        return objectsOfClass;
    }

    /**
     * Given a specific found node, this method will return the sub tree of that
     * node.
     *
     * @param root
     *            the node for which the sub tree is requested
     * @return HashTree
     */
    public HashTree getSubTree(Object root) {
        return subTrees.get(root);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void addNode(Object node, HashTree subTree) {
        if (searchClass.isAssignableFrom(node.getClass())) {
            objectsOfClass.add((T) node);
            ListedHashTree tree = new ListedHashTree(node);
            tree.set(node, subTree);
            subTrees.put(node, tree);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void subtractNode() {
    }

    /** {@inheritDoc} */
    @Override
    public void processPath() {
    }
}
