package org.apache.jmeter.util;
import java.util.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class SearchByClass implements ListedHashTreeVisitor {
	List objectsOfClass = new LinkedList();
	Map subTrees = new HashMap();
	Class searchClass = null;
	public SearchByClass() {}
	public SearchByClass(Class searchClass) {
		this.searchClass = searchClass;
	}
	public Collection getSearchResults() {
		return objectsOfClass;
	}
	public ListedHashTree getSubTree(Object root) {
		return (ListedHashTree) subTrees.get(root);
	}
	public void addNode(Object node, ListedHashTree subTree) {
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
			tree.get(one).add(o, two);
			tree.traverse(searcher);
			assertEquals(1, searcher.getSearchResults().size());
		}
	}
	public void subtractNode() {}
	public void processPath() {}
}