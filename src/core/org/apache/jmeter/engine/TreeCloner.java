package org.apache.jmeter.engine;

import java.util.LinkedList;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.testelement.PerThreadClonable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class TreeCloner implements HashTreeTraverser
{
	ListedHashTree newTree;
	LinkedList objects = new LinkedList();
    boolean forThread = true;
    
    public TreeCloner()
    {
        this(true);
    }

	public TreeCloner(boolean forThread)
	{
		newTree = new ListedHashTree();
        this.forThread = forThread;
	}
	public void addNode(Object node,HashTree subTree)
	{
		if(node instanceof PerThreadClonable || (!forThread && node instanceof TestElement))
		{
			node = ((TestElement)node).clone();
			newTree.add(objects,node);
		}
		else
		{
			newTree.add(objects,node);
		}
		objects.addLast(node);
	}
	public void subtractNode()
	{
		objects.removeLast();
	}

	public ListedHashTree getClonedTree()
	{
		return newTree;
	}

	public void processPath()
	{
	}

	public static class Test extends junit.framework.TestCase
	{
		public Test(String name)
		{
			super(name);
		}

		public void testCloning() throws Exception
		{
			ListedHashTree original = new ListedHashTree();
			GenericController controller = new GenericController();
			controller.setName("controller");
			Arguments args = new Arguments();
			args.setName("args");
			original.add(controller,args);
			TreeCloner cloner = new TreeCloner();
			original.traverse(cloner);
			ListedHashTree newTree = cloner.getClonedTree();
			assertTrue(original != newTree);
			assertEquals(original.size(),newTree.size());
			assertEquals(original.getTree(original.getArray()[0]).size(),
					newTree.getTree(newTree.getArray()[0]).size());
			assertTrue(original.getArray()[0] != newTree.getArray()[0]);
			assertEquals(((GenericController)original.getArray()[0]).getName(),
					((GenericController)newTree.getArray()[0]).getName());
			assertSame(original.getTree(original.getArray()[0]).getArray()[0],
							newTree.getTree(newTree.getArray()[0]).getArray()[0]);
		}

	}
}