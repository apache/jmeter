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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import junit.framework.TestCase;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.save.old.JMeterNameSpaceHandler;
import org.apache.jmeter.save.old.xml.XmlHandler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.ListedHashTree;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/
public class Load implements Command
{
	private static Set commands = new HashSet();

	static
	{
		commands.add(JMeterUtils.getResString("open"));
	}

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public Load() { }

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Set getActionNames()
	{
		return commands;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		JFileChooser chooser = FileDialoger.promptToOpenFile(new String[]{".jmx"});
		if(chooser == null)
		{
			return;
		}
		InputStream reader = null;
		File f = null;
		try
		{
			f = chooser.getSelectedFile();
			if(f != null)
			{
				reader = new FileInputStream(f);
				ListedHashTree tree = SaveService.loadSubTree(reader);
				insertLoadedTree(e.getID(), tree);
			}
		}
		catch(IllegalUserActionException ex)
		{
			JMeterUtils.reportErrorToUser(ex.getMessage());
		}
		catch(Throwable ex)
		{
			try
			{
				legacyLoad(f);
			}
			catch(Throwable err)
			{
				//ex.printStackTrace();
				err.printStackTrace();
				JMeterUtils.reportErrorToUser("Couldn't load JMX file.  May have been corrupted");
			}
		}
		finally
		{
			GuiPackage.getInstance().getMainFrame().repaint();
		}
	}

	private void insertLoadedTree(int id, ListedHashTree tree) throws Exception, IllegalUserActionException {
		convertTree(tree);
		GuiPackage.getInstance().addSubTree(tree);
		tree = GuiPackage.getInstance().getCurrentSubTree();				
		ActionRouter.getInstance().actionPerformed(new ActionEvent(
			tree.get(tree.getArray()[tree.size()-1]),id,CheckDirty.SUB_TREE_LOADED));
	}

	private void convertTree(ListedHashTree tree) throws Exception
	{
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext())
		{
			TestElement item = (TestElement)iter.next();
			convertTree(tree.get(item));
			JMeterGUIComponent comp = generateGUIComponent(item);
			tree.replace(item,comp);
		}
	}

	private JMeterGUIComponent generateGUIComponent(TestElement item) throws Exception
	{
			JMeterGUIComponent gui = null;
			try {
				gui = (JMeterGUIComponent)Class.forName((String)item.getProperty(TestElement.GUI_CLASS)).newInstance();
			} catch(Exception e) {
				System.out.println("Couldn't get gui for "+item);
				e.printStackTrace();
				gui = new WorkBenchGui();
			} 
			gui.configure(item);
			return gui;
	}
	
	private void legacyLoad(File f) throws Exception
	{
		FileInputStream reader = new FileInputStream(f);
				XmlHandler handler = new XmlHandler(new JMeterNameSpaceHandler());
				XMLReader parser = JMeterUtils.getXMLParser();
				parser.setContentHandler(handler);
				parser.setErrorHandler(handler);
				parser.parse(new InputSource(reader));
				ListedHashTree tree = handler.getDataTree();
				updateTree(tree);
				insertLoadedTree(443,tree);
	}

	
	/**
	 * For loading a 1.6 version test tree
	 * */
	private void updateTree(ListedHashTree tree) {
			List items = new LinkedList(tree.list());
			Iterator iter = items.iterator();

			while (iter.hasNext()) {
				Object item = iter.next();
				if (item instanceof HTTPSampler) {
					List subItems = new LinkedList(tree.list(item));
					boolean replaced = false;
					Iterator iter2 = subItems.iterator();
					while (iter2.hasNext()) {
						TestElement config = (TestElement)iter2.next();
						if (config.getPropertyAsString(TestElement.TEST_CLASS).equals(
								"org.apache.jmeter.protocol.http.config.UrlConfig")) {
							replaced = true;
							HTTPSampler newControl = new HTTPSampler();
							newControl.setName((String)((TestElement)item).getProperty(TestElement.NAME));
							newControl.addTestElement( config);	
							newControl.setProperty(TestElement.GUI_CLASS,
									"org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui");
							tree.get(item).replace(config, newControl);
						}
					}

					if (replaced) {
						((TestElement)item).setProperty(TestElement.GUI_CLASS,
								"org.apache.jmeter.protocol.http.config.gui.UrlConfigGui");
						((TestElement)item).setProperty(TestElement.NAME,"HTTP Request Defaults");
						GenericController newControl = new GenericController();
						newControl.setProperty(TestElement.GUI_CLASS,
								"org.apache.jmeter.control.gui.LogicControllerGui");
						newControl.setName("Simple Controller");
						tree.replace(item, newControl);
						tree.add(newControl, item);
					}
				} else {
					updateTree(tree.get(item));
				}
			}
		}
		
	/************************************************************
	 *  !ToDo (Class description)
	 *
	 *@author     $Author$
	 *@created    $Date$
	 *@version    $Revision$
	 ***********************************************************/
	public static class Test extends TestCase {
		File testFile1, testFile2, testFile3,testFile4,testFile5,testFile6,testFile7,
				testFile8,testFile9,testFile10,testFile11,testFile12,testFile13;
		static Load loader = new Load();

		public Test(String name) {
			super(name);
		}

		/************************************************************
		 *  !ToDo
		 ***********************************************************/
		public void setUp() {
			testFile1 =
				new File(System.getProperty("user.dir") + "/testfiles", "Test Plan.jmx");
			testFile2 =
				new File(
					System.getProperty("user.dir") + "/testfiles",
					"Modification Manager.jmx");
			testFile3 =
				new File(System.getProperty("user.dir") + "/testfiles", "proxy.jmx");
			testFile4 =
				new File(System.getProperty("user.dir") + "/testfiles", "AssertionTestPlan.jmx");
			testFile5 =
				new File(System.getProperty("user.dir") + "/testfiles", "AuthManagerTestPlan.jmx");
			testFile6 =
				new File(System.getProperty("user.dir") + "/testfiles", "HeaderManagerTestPlan.jmx");
			testFile7 =
				new File(System.getProperty("user.dir") + "/testfiles", "InterleaveTestPlan.jmx");
			testFile8 =
				new File(System.getProperty("user.dir") + "/testfiles", "InterleaveTestPlan2.jmx");
			testFile9 =
				new File(System.getProperty("user.dir") + "/testfiles", "LoopTestPlan.jmx");
			testFile10 =
				new File(System.getProperty("user.dir") + "/testfiles", "OnceOnlyTestPlan.jmx");
			testFile11 =
				new File(System.getProperty("user.dir") + "/testfiles", "ProxyServerTestPlan.jmx");
			testFile12 =
				new File(System.getProperty("user.dir") + "/testfiles", "SimpleTestPlan.jmx");
			testFile13 =
				new File(System.getProperty("user.dir") + "/testfiles", "URLRewritingExample.jmx");
		}

		/************************************************************
		 *  !ToDo
		 *
		 *@exception  Exception  !ToDo (Exception description)
		 ***********************************************************/
		public void testUpdateTree() throws Exception {
			ListedHashTree tree = getTree(testFile2);
			loader.updateTree(tree);
			assertTrue(tree.list(tree.list().get(0)).get(0) instanceof GenericController);
			loader.convertTree(tree);
			assertEquals(new LogicControllerGui().getStaticLabel(),
					((JMeterGUIComponent)tree.list(tree.list().get(0)).get(0)).getStaticLabel());
		}

		public void testFile3() throws Exception {
			ListedHashTree tree = getTree(testFile3);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.threads.ThreadGroup);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.threads.gui.ThreadGroupGui);
		}
		
		public void testFile4() throws Exception {
			ListedHashTree tree = getTree(testFile4);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile5() throws Exception {
			ListedHashTree tree = getTree(testFile5);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile6() throws Exception {
			ListedHashTree tree = getTree(testFile6);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile7() throws Exception {
			ListedHashTree tree = getTree(testFile7);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile8() throws Exception {
			ListedHashTree tree = getTree(testFile8);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile9() throws Exception {
			ListedHashTree tree = getTree(testFile9);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile10() throws Exception {
			ListedHashTree tree = getTree(testFile10);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile11() throws Exception {
			ListedHashTree tree = getTree(testFile11);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}
		
		public void testFile12() throws Exception {
			ListedHashTree tree = getTree(testFile12);
			//loader.updateTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.testelement.TestPlan);
			loader.convertTree(tree);
			assertTrue(tree.list().get(0) instanceof org.apache.jmeter.control.gui.TestPlanGui);
		}

		private ListedHashTree getTree(File f) throws Exception {
				FileInputStream reader = new FileInputStream(f);
				XmlHandler handler = new XmlHandler(new JMeterNameSpaceHandler());
				XMLReader parser = JMeterUtils.getXMLParser();
				parser.setContentHandler(handler);
				parser.setErrorHandler(handler);
				parser.parse(new InputSource(reader));
				ListedHashTree tree = handler.getDataTree();
				return tree;
		}
	}
}
