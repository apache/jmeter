package org.apache.jmeter.engine.util;

import java.util.LinkedList;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * @version $Revision$
 */
public class DisabledComponentRemover implements HashTreeTraverser
{
    HashTree tree;
    LinkedList stack = new LinkedList();
    
    public DisabledComponentRemover(HashTree tree)
    {
        this.tree = tree;
    }

    public void addNode(Object node, HashTree subTree)
    {
        stack.addLast(node);
    }

    public void subtractNode()
    {
        TestElement lastNode = (TestElement)stack.removeLast();
        if(!lastNode.getPropertyAsBoolean(TestElement.ENABLED))
        {
            tree.getTree(stack).remove(lastNode);
        }
    }

    public void processPath()
    {
    }
}
