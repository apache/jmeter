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
package org.apache.jmeter.gui.tree;
import java.util.*;
import javax.swing.tree.*;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.ListedHashTree;
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
		super(new JMeterTreeNode(new NamePanel()));
		this.insertNodeInto(new JMeterTreeNode(new TestPlanGui()),
				(JMeterTreeNode)getRoot(), 0);
		this.insertNodeInto(new JMeterTreeNode(new WorkBenchGui()),
				(JMeterTreeNode)getRoot(), 1);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param type  !ToDo (Parameter description)
	 *@return      !ToDo (Return description)
	 ***************************************/
	public List getNodesOfType(Class type)
	{
		List nodeList = new LinkedList();
		traverseAndFind(type, (JMeterTreeNode)this.getRoot(), nodeList);
		return nodeList;
	}

	/****************************************
	 * !ToDo
	 *
	 *@param subTree                         !ToDo
	 *@param current                         !ToDo
	 *@exception IllegalUserActionException  !ToDo (Exception description)
	 ***************************************/
	public void addSubTree(ListedHashTree subTree, JMeterTreeNode current)
			 throws IllegalUserActionException
	{
		Iterator iter = subTree.list().iterator();
		while(iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent)iter.next();
			if(item instanceof TestPlanGui)
			{
				current = (JMeterTreeNode)((JMeterTreeNode)getRoot()).getChildAt(0);
				current.configure(item.createTestElement());
				addSubTree(subTree.get(item), current);
			}
			else
			{
				addSubTree(subTree.get(item), addComponent(item, current));
			}
		}
	}

	/****************************************
	 * !ToDo
	 *
	 *@param component                       !ToDo
	 *@param node                            !ToDo
	 *@return                                !ToDo (Return description)
	 *@exception IllegalUserActionException  !ToDo (Exception description)
	 ***************************************/
	public JMeterTreeNode addComponent(Object component, JMeterTreeNode node)
			 throws IllegalUserActionException
	{
		if(node.getUserObject() instanceof AbstractConfigGui)
		{
			throw new IllegalUserActionException("This node cannot hold sub-elements");
		}
		JMeterTreeNode newNode = new JMeterTreeNode((JMeterGUIComponent)component);
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
		if(!(node.getUserObject() instanceof TestPlan) &&
				!(node.getUserObject() instanceof WorkBench))
		{
			super.removeNodeFromParent(node);
		}
	}

	private void traverseAndFind(Class type, JMeterTreeNode node, List nodeList)
	{
		if(type.isInstance(node.getUserObject()))
		{
			nodeList.add(node);
		}
		Enumeration enum = node.children();
		while(enum.hasMoreElements())
		{
			JMeterTreeNode child = (JMeterTreeNode)enum.nextElement();
			traverseAndFind(type, child, nodeList);
		}
	}

	public ListedHashTree getCurrentSubTree(JMeterTreeNode node)
	{
		ListedHashTree hashTree = new ListedHashTree(node);
		Enumeration enum = node.children();
		while(enum.hasMoreElements())
		{
			JMeterTreeNode child = (JMeterTreeNode)enum.nextElement();
			hashTree.add(node,getCurrentSubTree(child));
		}
		return hashTree;
	}

	public ListedHashTree getTestPlan()
	{
		return getCurrentSubTree((JMeterTreeNode)((JMeterTreeNode)this.getRoot()).getChildAt(0));
	}
	
	public void clearTestPlan()
	{
		super.removeNodeFromParent((JMeterTreeNode)getChild(getRoot(), 0));
		this.insertNodeInto(new JMeterTreeNode(new TestPlanGui()),
				(JMeterTreeNode)getRoot(), 0);
		super.removeNodeFromParent((JMeterTreeNode)getChild(getRoot(), 1));
		this.insertNodeInto(new JMeterTreeNode(new WorkBenchGui()),
				(JMeterTreeNode)getRoot(), 1);
	}
}
