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
package org.apache.jmeter.gui.action;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   February 13, 2001
 *@version   1.0
 ***************************************/

public class Save implements Command
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	private final static String SAVE_ALL = "save_all";
	private final static String SAVE = "save";

	private static Set commands = new HashSet();
	static
	{
		commands.add(SAVE);
		commands.add(SAVE_ALL);
	}


	/****************************************
	 * Constructor for the Save object
	 ***************************************/
	public Save() { }


	/****************************************
	 * Gets the ActionNames attribute of the Save object
	 *
	 *@return   The ActionNames value
	 ***************************************/
	public Set getActionNames()
	{
		return commands;
	}


	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		ListedHashTree subTree = null;
		if(e.getActionCommand().equals(SAVE))
		{
			subTree = GuiPackage.getInstance().getCurrentSubTree();
		}
		else if(e.getActionCommand().equals(SAVE_ALL))
		{
			subTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
		}
		try
		{
			convertSubTree(subTree);
		}catch(Exception err)
		{}
		JFileChooser chooser = FileDialoger.promptToSaveFile(
				GuiPackage.getInstance().getTreeListener().getCurrentNode().getName() + ".jmx");
		if(chooser == null)
		{
			return;
		}
		OutputStream writer = null;
		try
		{
			writer = new FileOutputStream(chooser.getSelectedFile());
			SaveService.saveSubTree(subTree,writer);
		}
		catch(Throwable ex)
		{
			log.error("",ex);
		}
		finally
		{
			closeWriter(writer);
			GuiPackage.getInstance().getMainFrame().repaint();
		}
	}

	private void convertSubTree(ListedHashTree tree)
	{
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent)iter.next();
			convertSubTree(tree.get(item));
			TestElement testElement = item.createTestElement();
			tree.replace(item,testElement);
		}
	}

	public static class Test extends junit.framework.TestCase
	{
		Save save;
		public Test(String name)
		{
			super(name);
		}

		public void setUp()
		{
			save = new Save();
		}

		public void testTreeConversion() throws Exception
		{
			ListedHashTree tree = new ListedHashTree();
			JMeterGUIComponent root = new org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui();
			tree.add(root,root);
			tree.get(root).add(root,root);
			save.convertSubTree(tree);
			assertEquals(tree.getArray()[0].getClass().getName(),root.createTestElement().getClass().getName());
			tree = tree.get(tree.getArray()[0]);
			assertEquals(tree.getArray()[0].getClass().getName(),
					root.createTestElement().getClass().getName());
			assertEquals(tree.get(tree.getArray()[0]).getArray()[0].getClass().getName(),
					root.createTestElement().getClass().getName());
		}
	}


	/****************************************
	 * Description of the Method
	 *
	 *@param writer  Description of Parameter
	 ***************************************/
	private void closeWriter(OutputStream writer)
	{
		if(writer != null)
		{
			try
			{
				writer.close();
			}
			catch(Exception ex)
			{
				log.error("",ex);
			}
		}
	}
}
