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

/**
 * By implementing this interface, a class can easily traverse a HashTree object, and be
 * notified via callbacks of certain events.  There are three such events:
 * <ol><li>When a node is first encountered, the traverser's 
 * {@link addNode(Object,HashTree)} method
 *  is called.  It is handed the object at that node, and the entire sub-tree of the node.</li>
 * <li>When a leaf node is encountered, the traverser is notified that a full path has been
 * finished via the {@link processPath()} method.  It is the traversing class's responsibility
 * to know the path that has just finished (this can be done by keeping a simple stack of
 * all added nodes).</li>
 * <li>When a node is retraced, the traverser's {@link subtractNode()} is called.  Again, it is the
 * traverser's responsibility to know which node has been retraced.  </li>
 * </ol>
 * To summarize, as the traversal goes down a tree path, nodes are added.  When the
 * end of the path is reached, the {@link processPath()} call is sent.  As the traversal backs up,
 * nodes are subtracted.
 * <p>
 * The traversal is a depth-first traversal. 
 * @author Michael Stover (mstover1 at apache.org)
 * @see HashTree
 * @see SearchByClass
 */

public interface HashTreeTraverser
{

	/**
	 * The tree traverses itself depth-first, calling addNode for each object
	 * it encounters as it goes.  This is a callback method, and should not be called except
	 * by a HashTree during traversal.
	 * 
	 * @param node The node currently encountered
	 * @param subTree The HashTree under the node encountered.
	 */
	public void addNode(Object node,HashTree subTree);

	/**
	 * Indicates traversal has moved up a step, and the visitor should remove the
	 * top node from it's stack structure.  This is a callback method, and should not be called except
	 * by a HashTree during traversal.
	 */
	public void subtractNode();

	/**
	 * Process path is called when a leaf is reached.  If a visitor wishes to generate
	 * Lists of path elements to each leaf, it should keep a Stack data structure of
	 * nodes passed to it with addNode, and removing top items for every subtractNode()
	 * call.  This is a callback method, and should not be called except
	 * by a HashTree during traversal.
	 */
	public void processPath();
}