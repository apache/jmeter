/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 

package org.apache.jorphan.collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * Useful for finding all nodes in the tree that represent objects of a particular
 * type.  For instance, if your tree contains all strings, and a few StringBuffer objects, 
 * you can use the SearchByClass traverser to find all the StringBuffer objects in your
 * tree.  
 * <p>
 * Usage is simple.  Given a {@link HashTree} object "tree", and a SearchByClass object:
 * <p>
 * <pre>
 * 	HashTree tree = new HashTree();
 * 	// ... tree gets filled with objects
 * 	SearchByClass searcher = new SearchByClass(StringBuffer.class);
 * 	tree.traverse(searcher);
 * 	Iterator iter = searcher.getSearchResults().iterator();
 * 	while(iter.hasNext())
 * 	{
 * 		StringBuffer foundNode = (StringBuffer)iter.next();
 * 		HashTree subTreeOfFoundNode = searcher.getSubTree(foundNode);
 * 		//  .... do something with node and subTree...
 * 	}
 * </pre>
 * @author Michael Stover (mstover1 at apache.org)
 * @see HashTree
 * @see HashTreeTraverser
 */
public class SearchByClass implements HashTreeTraverser {
	List objectsOfClass = new LinkedList();
	Map subTrees = new HashMap();
	Class searchClass = null;
	/**
	 * Creates an instance of SearchByClass.  However, without setting the Class to search
	 * for, it will be a useless object.
	 * @see java.lang.Object#Object()
	 */
	public SearchByClass() {}
	/**
	 * Creates an instance of SearchByClass, and sets the Class to be searched for.
	 * @param searchClass
	 */
	public SearchByClass(Class searchClass) {
		this.searchClass = searchClass;
	}
	/**
	 * After traversing the HashTree, call this method to get a collection of the
	 * nodes that were found.
	 * @return Collection  All found nodes of the requested type
	 */
	public Collection getSearchResults() {
		return objectsOfClass;
	}
	/**
	 * Given a specific found node, this method will return the sub tree of that node.
	 * @param root The node for which the sub tree is requested.
	 * @return HashTree
	 */
	public HashTree getSubTree(Object root) {
		return (HashTree)subTrees.get(root);
	}
	public void addNode(Object node, HashTree subTree) {
		if (searchClass.isAssignableFrom(node.getClass())) {
			objectsOfClass.add(node);
			ListedHashTree tree = new ListedHashTree(node);
			tree.set(node, subTree);
			subTrees.put(node, tree);
		}
	}
	public static class Test extends junit.framework.TestCase {
		public Test(String name) {
			super(name);
		}
		public void testSearch() throws Exception {
			ListedHashTree tree = new ListedHashTree();
			SearchByClass searcher = new SearchByClass(Integer.class);
			String one = "one";
			String two = "two";
			Integer o = new Integer(1);
			tree.add(one, o);
			tree.getTree(one).add(o, two);
			tree.traverse(searcher);
			assertEquals(1, searcher.getSearchResults().size());
		}
	}
	public void subtractNode() {}
	public void processPath() {}
}