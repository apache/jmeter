package org.apache.jmeter.util;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface ListedHashTreeVisitor
{

	/**
	 * The tree traverses itself depth-first, calling processNode for each object
	 * it encounters as it goes.
	 */
	public void addNode(Object node,ListedHashTree subTree);

	/**
	 * Indicates traversal has moved up a step, and the visitor should remove the
	 * top node from it's stack structure.
	 */
	public void subtractNode();

	/**
	 * Process path is called when a leaf is reached.  If a visitor wishes to generate
	 * Lists of path elements to each leaf, it should keep a Stack data structure of
	 * nodes passed to it with addNode, and removing top items for every subtractNode()
	 * call.
	 */
	public void processPath();
}