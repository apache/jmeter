package org.apache.jmeter.engine.util;

import java.util.LinkedList;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class DisabledComponentRemover implements HashTreeTraverser
{
    HashTree tree;
    LinkedList stack = new LinkedList();
    
    public DisabledComponentRemover(HashTree tree)
    {
        this.tree = tree;
    }

    /**
     * @see org.apache.jorphan.collections.HashTreeTraverser#addNode(java.lang.Object, org.apache.jorphan.collections.HashTree)
     */
    public void addNode(Object node, HashTree subTree)
    {
        stack.addLast(node);
    }

    /**
     * @see org.apache.jorphan.collections.HashTreeTraverser#subtractNode()
     */
    public void subtractNode()
    {
        TestElement lastNode = (TestElement)stack.removeLast();
        if(!lastNode.getPropertyAsBoolean(TestElement.ENABLED))
        {
            tree.getTree(stack).remove(lastNode);
        }
    }

    /**
     * @see org.apache.jorphan.collections.HashTreeTraverser#processPath()
     */
    public void processPath()
    {}

}
