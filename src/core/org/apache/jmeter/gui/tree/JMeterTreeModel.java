/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.gui.tree;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class JMeterTreeModel extends DefaultTreeModel
{

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public JMeterTreeModel()
    {
        super(new JMeterTreeNode(new WorkBenchGui().createTestElement(), null));
        initTree();
    }

    /****************************************
     * Returns a list of tree nodes that
     * hold objects of the given class
     * type.  If none are found, an empty
     * list is returned 
	*************************************/
    public List getNodesOfType(Class type)
    {
        List nodeList = new LinkedList();
        traverseAndFind(type, (JMeterTreeNode) this.getRoot(), nodeList);
        return nodeList;
    }

    /**
     * Get the node for a given TestElement object.
     * @param userObject
     * @return JMeterTreeNode
     */
    public JMeterTreeNode getNodeOf(TestElement userObject)
    {
        List nodeList = new LinkedList();
        return traverseAndFind(userObject, (JMeterTreeNode) this.getRoot(), nodeList);
    }

    /****************************************
     * Adds the sub tree at the given node.  Returns a boolean indicating
     * whether the added sub tree was a full test plan.
     *
     *@param subTree                         !ToDo
     *@param current                         !ToDo
     *@exception IllegalUserActionException  !ToDo (Exception description)
     ***************************************/
    public boolean addSubTree(HashTree subTree, JMeterTreeNode current) throws IllegalUserActionException
    {
        boolean ret = false;
        Iterator iter = subTree.list().iterator();
        while (iter.hasNext())
        {
            TestElement item = (TestElement) iter.next();
            if (item instanceof TestPlan)
            {
                current = (JMeterTreeNode) ((JMeterTreeNode) getRoot()).getChildAt(0);
                ((TestElement) current.getUserObject()).addTestElement(item);
                addSubTree(subTree.getTree(item), current);
                ret = true;
            }
            else
            {
                addSubTree(subTree.getTree(item), addComponent(item, current));
            }
        }
        return ret;
    }

    /****************************************
     * !ToDo
     *
     *@param component                       !ToDo
     *@param node                            !ToDo
     *@return                                !ToDo (Return description)
     *@exception IllegalUserActionException  !ToDo (Exception description)
     ***************************************/
    public JMeterTreeNode addComponent(TestElement component, JMeterTreeNode node) throws IllegalUserActionException
    {
        if (node.getUserObject() instanceof AbstractConfigGui)
        {
            throw new IllegalUserActionException("This node cannot hold sub-elements");
        }
        JMeterTreeNode newNode = new JMeterTreeNode((TestElement) component, this);
        
        //This check the state of the TestElement and if returns false it disable the loaded node
        try{
            if(((String)component.getProperty(TestElement.ENABLED)).equals("false"))
            {
                newNode.setEnabled(false);           
            }
        }catch(Exception e){
            newNode.setEnabled(true);
        }

        this.insertNodeInto(newNode, node, node.getChildCount());
        return newNode;
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param node  !ToDo (Parameter description)
     ***************************************/
    public void removeNodeFromParent(JMeterTreeNode node)
    {
        if (!(node.getUserObject() instanceof TestPlan) && !(node.getUserObject() instanceof WorkBench))
        {
            super.removeNodeFromParent(node);
        }
    }

    private void traverseAndFind(Class type, JMeterTreeNode node, List nodeList)
    {
        if (type.isInstance(node.getUserObject()))
        {
            nodeList.add(node);
        }
        Enumeration enum = node.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode child = (JMeterTreeNode) enum.nextElement();
            traverseAndFind(type, child, nodeList);
        }
    }

    private JMeterTreeNode traverseAndFind(TestElement userObject, JMeterTreeNode node, List nodeList)
    {
        if (userObject == node.getUserObject())
        {
            return node;
        }
        Enumeration enum = node.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode child = (JMeterTreeNode) enum.nextElement();
            return traverseAndFind(userObject, child, nodeList);
        }
        return null;
    }

    public HashTree getCurrentSubTree(JMeterTreeNode node)
    {
        ListedHashTree hashTree = new ListedHashTree(node);
        Enumeration enum = node.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode child = (JMeterTreeNode) enum.nextElement();
            hashTree.add(node, getCurrentSubTree(child));
        }
        return hashTree;
    }

    public HashTree getTestPlan()
    {
        return getCurrentSubTree((JMeterTreeNode) ((JMeterTreeNode) this.getRoot()).getChildAt(0));
    }

    public void clearTestPlan()
    {
        super.removeNodeFromParent((JMeterTreeNode) getChild(getRoot(), 0));
        initTree();
    }

    private void initTree()
    {
        TestElement tp = new TestPlanGui().createTestElement();
        TestElement wb = new WorkBenchGui().createTestElement();
        this.insertNodeInto(new JMeterTreeNode(tp, this), (JMeterTreeNode) getRoot(), 0);
        try
        {
            super.removeNodeFromParent((JMeterTreeNode) getChild(getRoot(), 1));
        }
        catch (RuntimeException e)
        {}
        this.insertNodeInto(new JMeterTreeNode(wb, this), (JMeterTreeNode) getRoot(), 1);
    }
}
