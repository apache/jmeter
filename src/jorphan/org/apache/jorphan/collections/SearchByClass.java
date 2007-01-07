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
 * StringBuffer objects, you can use the SearchByClass traverser to find all the
 * StringBuffer objects in your tree.
 * <p>
 * Usage is simple. Given a {@link HashTree} object "tree", and a SearchByClass
 * object:
 * 
 * <pre>
 * HashTree tree = new HashTree();
 * // ... tree gets filled with objects
 * SearchByClass searcher = new SearchByClass(StringBuffer.class);
 * tree.traverse(searcher);
 * Iterator iter = searcher.getSearchResults().iterator();
 * while (iter.hasNext()) {
 * 	StringBuffer foundNode = (StringBuffer) iter.next();
 * 	HashTree subTreeOfFoundNode = searcher.getSubTree(foundNode);
 * 	//  .... do something with node and subTree...
 * }
 * </pre>
 * 
 * @see HashTree
 * @see HashTreeTraverser
 * 
 * @author Michael Stover (mstover1 at apache.org)
 * @version $Revision$
 */
public class SearchByClass implements HashTreeTraverser {
	List objectsOfClass = new LinkedList();

	Map subTrees = new HashMap();

	Class searchClass = null;

	/**
	 * Creates an instance of SearchByClass. However, without setting the Class
	 * to search for, it will be a useless object.
	 */
	public SearchByClass() {
	}

	/**
	 * Creates an instance of SearchByClass, and sets the Class to be searched
	 * for.
	 * 
	 * @param searchClass
	 */
	public SearchByClass(Class searchClass) {
		this.searchClass = searchClass;
	}

	/**
	 * After traversing the HashTree, call this method to get a collection of
	 * the nodes that were found.
	 * 
	 * @return Collection All found nodes of the requested type
	 */
	public Collection getSearchResults() {
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
		return (HashTree) subTrees.get(root);
	}

	public void addNode(Object node, HashTree subTree) {
		if (searchClass.isAssignableFrom(node.getClass())) {
			objectsOfClass.add(node);
			ListedHashTree tree = new ListedHashTree(node);
			tree.set(node, subTree);
			subTrees.put(node, tree);
		}
	}

	public void subtractNode() {
	}

	public void processPath() {
	}
}