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
package org.apache.jmeter.gui;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.ValueReplacer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/**
 * GuiPackage is a static class that provides convenient access to information about the 
 * current state of JMeter's GUI.  Any GUI class can grab a handle to GuiPackage by 
 * calling the static method 'getInstance()' and then use it to query the GUI about
 * it's state.  When actions, for instance, need to affect the GUI, they
 * typically use GuiPackage to get access to different parts of the GUI.
 * 
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */
public class GuiPackage
{
	transient private static Logger log =
		Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
	private static GuiPackage guiPack;
	private boolean dirty = false;
	private Map nodesToGui = new HashMap();
	private Map guis = new HashMap();
	private JMeterTreeNode currentNode = null;
	/**
	 * GuiPackage is a Singleton class.
	 * @see java.lang.Object#Object()
	 */
	private GuiPackage()
	{}
	private JMeterTreeModel treeModel;
	private org.apache.jmeter.gui.MainFrame mainFrame;
	private org.apache.jmeter.gui.tree.JMeterTreeListener treeListener;
	public JMeterGUIComponent getGui(TestElement node, String guiClass)
	{
		log.debug("Getting gui for "+ node);
		try
		{
			JMeterGUIComponent comp = (JMeterGUIComponent) nodesToGui.get(node);
			log.debug("Gui retrieved = " + comp);
			if (comp == null)
			{
				comp = (JMeterGUIComponent) guis.get(guiClass);
				if (comp == null || comp instanceof UnsharedComponent)
				{
					comp = (JMeterGUIComponent) Class.forName(guiClass).newInstance();
					if(!(comp instanceof UnsharedComponent))
					{
						guis.put(guiClass, comp);
					}
				}
				nodesToGui.put(node, comp);
			}
			return comp;
		}
		catch (Exception e)
		{
			log.error("Problem retrieving gui", e);
			return null;
		}
	}
	
	public void removeNode(TestElement node)
	{
		nodesToGui.remove(node);
	}
	/**
	 * Convenience method for grabbing the gui for the current node
	 */
	public JMeterGUIComponent getCurrentGui()
	{
		try
		{
			JMeterGUIComponent comp = getGui(treeListener.getCurrentNode().createTestElement());
			comp.configure(treeListener.getCurrentNode().createTestElement());
			return comp;
		}
		catch (Exception e)
		{
			log.error("Problem retrieving gui", e);
			return null;
		}
	}
	public TestElement createTestElement(String guiClass)
	{
		try
		{
			JMeterGUIComponent comp = (JMeterGUIComponent) guis.get(guiClass);
			if (comp == null || comp instanceof UnsharedComponent)
			{
				comp = (JMeterGUIComponent) Class.forName(guiClass).newInstance();
				guis.put(guiClass, comp);
			}
			TestElement node = ((JMeterGUIComponent) Class.forName(guiClass).newInstance()).createTestElement();
			nodesToGui.put(node, comp);
			return node;
		}
		catch (Exception e)
		{
			log.error("Problem retrieving gui", e);
			return null;
		}
	}
	public JMeterGUIComponent getGui(TestElement node)
	{
		try
		{
			return getGui(node, node.getPropertyAsString(TestElement.GUI_CLASS));
		}
		catch (Exception e)
		{
			log.error("Problem retrieving gui", e);
			return null;
		}
	}
    
    public void updateCurrentGui()
    {
        if(currentNode != null)
        {
            JMeterGUIComponent comp = getGui(currentNode.createTestElement());
            comp.configure(currentNode.createTestElement());
        }
    }
    
	public void updateCurrentNode()
	{
		try
		{
			if(currentNode != null)
			{
				log.debug("Updating current node " + currentNode.createTestElement());
				JMeterGUIComponent comp = getGui(currentNode.createTestElement());
				TestElement el = currentNode.createTestElement();
				nodesToGui.remove(el);
				currentNode.setUserObject(comp.createTestElement());
				el = currentNode.createTestElement();
				nodesToGui.put(el,comp);
			}
			currentNode = treeListener.getCurrentNode();
		}
		catch (Exception e)
		{
			log.error("Problem retrieving gui", e);
		}
	}
	/**
	 * When GuiPackage is requested for the first time, it should be given handles to
	 * JMeter's Tree Listener and TreeModel.  
	 * @param listener The TreeListener for JMeter's test tree.
	 * @param treeModel The model for JMeter's test tree.
	 * @return GuiPackage
	 */
	public static GuiPackage getInstance(
		JMeterTreeListener listener,
		JMeterTreeModel treeModel)
	{
		if (guiPack == null)
		{
			guiPack = new GuiPackage();
			guiPack.setTreeListener(listener);
			guiPack.setTreeModel(treeModel);
		}
		return guiPack;
	}
	/**
	 * The dirty property is a flag that indicates whether there are parts of JMeter's test tree
	 * that the user has not saved since last modification.  Various (@link Command actions) set
	 * this property when components are modified/created/saved.
	 * @param d
	 */
	public void setDirty(boolean d)
	{
		dirty = d;
	}
	/**
	 * Retrieves the state of the 'dirty' property, a flag that indicates if there are test
	 * tree components that have been modified since they were last saved.
	 * @return boolean
	 */
	public boolean isDirty()
	{
		return dirty;
	}
	public boolean addSubTree(HashTree subTree)
		throws IllegalUserActionException
	{
		return treeModel.addSubTree(subTree, treeListener.getCurrentNode());
	}
	public HashTree getCurrentSubTree()
	{
		return treeModel.getCurrentSubTree(treeListener.getCurrentNode());
	}
	public static GuiPackage getInstance()
	{
		return guiPack;
	}
	public JMeterTreeModel getTreeModel()
	{
		return treeModel;
	}
	public ValueReplacer getReplacer()
	{
		ValueReplacer replacer =
			new ValueReplacer(
				((TestPlan) ((JMeterGUIComponent) getTreeModel()
					.getTestPlan()
					.getArray()[0])
					.createTestElement())
					.getUserDefinedVariables());
		return replacer;
	}
	public void setTreeModel(JMeterTreeModel newTreeModel)
	{
		treeModel = newTreeModel;
	}
	public void setMainFrame(org.apache.jmeter.gui.MainFrame newMainFrame)
	{
		mainFrame = newMainFrame;
	}
	public org.apache.jmeter.gui.MainFrame getMainFrame()
	{
		return mainFrame;
	}
	public void setTreeListener(
		org.apache.jmeter.gui.tree.JMeterTreeListener newTreeListener)
	{
		treeListener = newTreeListener;
	}
	public org.apache.jmeter.gui.tree.JMeterTreeListener getTreeListener()
	{
		return treeListener;
	}
}