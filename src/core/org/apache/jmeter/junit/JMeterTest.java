// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.junit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * @author    Michael Stover
 * @author    sebb at apache dot org (refactor into suites)
 * @version   $Revision$ Last update $Date$
 */
public class JMeterTest extends JMeterTestCase
{
    private static Logger log = LoggingManager.getLoggerForClass();
    
    private static Map guiTitles;
	private static Map funcTitles;

    public JMeterTest(String name)
    {
        super(name);
    }

/*
 * The suite() method creates separate test suites for each of the types of test.
 * The suitexxx() methods create a list of items to be tested, and create a new test
 * instance for each.
 * 
 * Each test type has its own constructor, which saves the item to be tested
 * 
 * Note that the suite() method must be static, and the methods
 * to run the tests must be instance methods so that they can pick up the item value
 * which was saved by the constructor.
 * 
 */
    // Constructor for TestElement tests
    private TestElement testItem;
	public JMeterTest(String testName, TestElement te)
	{
		super(testName);// Save the method name
		testItem=te;
	}

    // Constructor for Serializable tests
    private Serializable serObj;
	public JMeterTest(String testName, Serializable ser)
	{
		super(testName);// Save the method name
		serObj=ser;
	}

	// Constructor for GUI tests
    private JMeterGUIComponent guiItem;
	public JMeterTest(String testName, JMeterGUIComponent gc)
	{
		super(testName);// Save the method name
		guiItem=gc;
	}
    
	// Constructor for Function tests
	private Function funcItem;
	public JMeterTest(String testName, Function fi)
	{
		super(testName);// Save the method name
		funcItem=fi;
	}
    
	/*
	 * Use a suite to allow the tests to be generated at run-time
	 */
    public static Test suite() throws Exception{
        // ensure the GuiPackage is initialized.
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        treeLis.setActionHandler(ActionRouter.getInstance());
        GuiPackage.getInstance(treeLis, treeModel);
		try {
			// The GuiPackage needs a MainFrame to work:
			org.apache.jmeter.gui.MainFrame main =
				new org.apache.jmeter.gui.MainFrame(
					ActionRouter.getInstance(),
					treeModel,
					treeLis);
        }
        catch (RuntimeException e)
        {
        	System.out.println("Cannot create MainFrame: "+e);
        }

    	TestSuite suite = new TestSuite();
    	suite.addTest(new JMeterTest("createTitleSet"));
    	suite.addTest(suiteGUIComponents());
		suite.addTest(suiteSerializableElements());
		suite.addTest(suiteTestElements());
		suite.addTest(suiteBeanComponents());
		suite.addTest(new JMeterTest("createFunctionSet"));
		suite.addTest(suiteFunctions());
		suite.addTest(new JMeterTest("checkGuiSet"));
		suite.addTest(new JMeterTest("checkFunctionSet"));
        return suite;
    }
    
    /*
     * Extract titles from component_reference.xml
     */
    public void createTitleSet() throws Exception
    {
		guiTitles = new HashMap(90);
		
    	String compref = "../xdocs/usermanual/component_reference.xml";
		SAXBuilder bldr = new SAXBuilder();
		Document doc;
        doc = bldr.build(compref);
		Element root = doc.getRootElement();
		Element body = root.getChild("body");
		List sections = body.getChildren("section");
		for (int i = 0; i< sections.size();i++){
			List components = ((Element) sections.get(i)).getChildren("component");
			for (int j = 0; j <components.size();j++){
				Element comp = (Element) components.get(j);
				guiTitles.put(comp.getAttributeValue("name"),Boolean.FALSE); 
			}
		}
		// Add titles that don't need to be documented
		guiTitles.put("Root",Boolean.FALSE);
		guiTitles.put("Example Sampler",Boolean.FALSE);
    }

	/*
	 * Extract titles from functions.xml
	 */
	public void createFunctionSet() throws Exception
	{
		funcTitles = new HashMap(20);
		
		String compref = "../xdocs/usermanual/functions.xml";
		SAXBuilder bldr = new SAXBuilder();
		Document doc;
		doc = bldr.build(compref);
		Element root = doc.getRootElement();
		Element body = root.getChild("body");
		Element section = body.getChild("section");
		List sections = section.getChildren("subsection");
		for (int i = 0; i< sections.size();i++){
			List components = ((Element) sections.get(i)).getChildren("component");
			for (int j = 0; j <components.size();j++){
				Element comp = (Element) components.get(j);
				funcTitles.put(comp.getAttributeValue("name"),Boolean.FALSE);
			}
		}
	}

    private int scanprintMap(Map m, String t)
    {
    	Set s = m.keySet();
    	int unseen = 0;
    	if (s.size() == 0) return 0;
    	Iterator i = s.iterator();
    	while (i.hasNext())
    	{   
    		Object key = i.next();
    		if (!m.get(key).equals(Boolean.TRUE)) 
            {
				if(key.equals("SSL Manager"))// Not a true GUI component
				{
					continue;
				}
            	if (unseen == 0)// first time
            	{
					System.out.println("\nNames remaining in "+t+" Map:");
            	}
            	unseen++;
				System.out.println(key);
    		}
    	}
    	return unseen;
    }
    public void checkGuiSet() throws Exception
    {
		assertEquals("Should not have any names left over",0,scanprintMap(guiTitles,"GUI"));
    }

	public void checkFunctionSet() throws Exception
	{
		assertEquals("Should not have any names left over",0,scanprintMap(funcTitles,"Function"));
	}
	/*
	 * Test GUI elements - create the suite of tests
	 */
	private static Test suiteGUIComponents() throws Exception
	{
		TestSuite suite = new TestSuite("GuiComponents");
		Iterator iter = getObjects(JMeterGUIComponent.class).iterator();
		while (iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
			if (item instanceof JMeterTreeNode)
			{
				System.out.println("JMeterGUIComponent: skipping all tests  "+item.getClass().getName());
				continue;
			}
			TestSuite ts = new TestSuite(item.getClass().getName());
			ts.addTest(new JMeterTest("GUIComponents1",item));
			if (item instanceof TestBeanGUI)
			{
				System.out.println("JMeterGUIComponent: skipping some tests "+item.getClass().getName());
			}
			else
			{
				ts.addTest(new JMeterTest("GUIComponents2",item));
				ts.addTest(new JMeterTest("runGUITitle",item));
			} 
			suite.addTest(ts);
		}
		return suite;
	}

	/*
	 * Test Functions - create the suite of tests
	 */
	private static Test suiteFunctions() throws Exception
	{
		TestSuite suite = new TestSuite("Functions");
		Iterator iter = getObjects(Function.class).iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			if (item.getClass().equals(CompoundVariable.class))
			{
				continue;
			}
			TestSuite ts = new TestSuite(item.getClass().getName());
			ts.addTest(new JMeterTest("runFunction",(Function) item));
			suite.addTest(ts);
		}
		return suite;
	}


		/*
		 * Test GUI elements - create the suite of tests
		 */
		private static Test suiteBeanComponents() throws Exception
		{
			TestSuite suite = new TestSuite("BeanComponents");
			Iterator iter = getObjects(TestBean.class).iterator();
	        while (iter.hasNext())
	        {
	        	Class c = iter.next().getClass();
	        	try {
					JMeterGUIComponent item = new TestBeanGUI(c);
					//JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
		            TestSuite ts = new TestSuite(item.getClass().getName());
					ts.addTest(new JMeterTest("GUIComponents2",item));
					ts.addTest(new JMeterTest("runGUITitle",item));
					suite.addTest(ts);
				}
				catch(IllegalArgumentException e)
				{
					System.out.println("Cannot create test for "+c.getName()+" "+e);
					e.printStackTrace(System.out);
				}
	        }
		return suite;
	}
	
	/*
	 * Test GUI elements - run the test
	 */
	public void runGUITitle() throws Exception
	{
		if (guiTitles.size() > 0) {
			String title = guiItem.getStaticLabel();
			boolean ct =guiTitles.containsKey(title); 
			if (ct) guiTitles.put(title,Boolean.TRUE);// So we can detect extra entries
			if (// Is this a work in progress ?
			    (title.indexOf("(ALPHA") == -1)
			    &&
			    (title.indexOf("(BETA")  == -1)
			)
			{// No, not a work in progress ...
				assertTrue("component_reference.xml needs '"+title+"' anchor for "+guiItem.getClass().getName(),ct);
			}
		}
	}
	
	/*
	 * run the function test
	 */
	public void runFunction() throws Exception
	{
		if (funcTitles.size() > 0) {
			String title = funcItem.getReferenceKey();
			boolean ct =funcTitles.containsKey(title); 
			if (ct) funcTitles.put(title,Boolean.TRUE);// For detecting extra entries
			if (// Is this a work in progress ?
				(title.indexOf("(ALPHA") == -1)
				&&
				(title.indexOf("(BETA")  == -1)
			)
			{// No, not a work in progress ...
				assertTrue("function.xml needs '"+title+"' entry for "+funcItem.getClass().getName(),ct);
			}
		}
	}
	
	/*
	 * Test GUI elements - run the test
	 */
    public void GUIComponents1() throws Exception
    {
    	String name = guiItem.getClass().getName();

        assertEquals(
            "Name should be same as static label for " + name,
            guiItem.getStaticLabel(),
            guiItem.getName());
    }


	/*
	 * Test GUI elements - run the test
	 */
	public void GUIComponents2() throws Exception
	{
		String name = guiItem.getClass().getName();
    	
		//TODO these assertions should be separate tests
    	
		TestElement el = guiItem.createTestElement();
		assertNotNull(
		name+".createTestElement should be non-null ", el);
		assertEquals(
			"GUI-CLASS: Failed on " + name,
			name,
			el.getPropertyAsString(TestElement.GUI_CLASS));

		assertEquals(
			"NAME: Failed on " + name,
			guiItem.getName(),
			el.getPropertyAsString(TestElement.NAME));
		assertEquals(
			"TEST-CLASS: Failed on " + name,
			el.getClass().getName(),
			el.getPropertyAsString(TestElement.TEST_CLASS));
		TestElement el2 = guiItem.createTestElement();
		el.setProperty(TestElement.NAME, "hey, new name!:");
		el.setProperty("NOT", "Shouldn't be here");
		if (!(guiItem instanceof UnsharedComponent))
		{
			assertEquals(
				"SHARED: Failed on " + name,
				"",
				el2.getPropertyAsString("NOT"));
		}
		log.debug("Saving element: " + el.getClass());
		el =
			SaveService.createTestElement(
				SaveService.getConfigForTestElement(null, el));
		log.debug("Successfully saved");
		guiItem.configure(el);
		assertEquals(
			"CONFIGURE-TEST: Failed on " + name,
			el.getPropertyAsString(TestElement.NAME),
			guiItem.getName());
		guiItem.modifyTestElement(el2);
		assertEquals(
			"Modify Test: Failed on " + name,
			"hey, new name!:",
			el2.getPropertyAsString(TestElement.NAME));
	}


    /*
     * Test serializable elements - create the suite of tests
     */
	private static Test suiteSerializableElements() throws Exception
	{
		TestSuite suite = new TestSuite("SerializableElements");
		Iterator iter = getObjects(Serializable.class).iterator();
		while (iter.hasNext())
		{
			Serializable serObj = (Serializable) iter.next();
			if (serObj.getClass().getName().endsWith("_Stub"))
			{
				continue;
			}
			TestSuite ts = new TestSuite(serObj.getClass().getName());
			ts.addTest(new JMeterTest("runSerialTest",serObj));
			suite.addTest(ts);
		}
		return suite;
	}

    /*
     * Test serializable elements - test the object
     */
    public void runSerialTest() throws Exception
    {
        try
        {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(serObj);
            out.close();
            ObjectInputStream in =
                new ObjectInputStream(
                    new ByteArrayInputStream(bytes.toByteArray()));
            Object readObject = in.readObject();
            in.close();
            assertEquals(
                "deserializing class: " + serObj.getClass().getName(),
                serObj.getClass(),
                readObject.getClass());
        }
        catch (Throwable e)
        {
            fail("serialization of "+serObj.getClass().getName()+" failed: "+e);
        }
    }

    /*
     * Test TestElements - create the suite
     */	
	private static Test suiteTestElements() throws Exception
	{
		TestSuite suite = new TestSuite("TestElements");
		Iterator iter = getObjects(TestElement.class).iterator();
		while (iter.hasNext())
		{
			TestElement item = (TestElement) iter.next();
			TestSuite ts = new TestSuite(item.getClass().getName());
			ts.addTest(new JMeterTest("runTestElement",item));
			suite.addTest(ts);
		}
		return suite;
	}
	
	/*
	 * Test TestElements - implement the test case
	 */	
	public void runTestElement() throws Exception
	{
		checkElementCloning(testItem);
		assertTrue(
		    testItem.getClass().getName() + " must implement Serializable",
			testItem instanceof Serializable);
	}

    private static Collection getObjects(Class extendsClass) throws Exception
    {
    	String exName = extendsClass.getName();
    	Object myThis = new String();
        Iterator classes =
            ClassFinder
                .findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] { extendsClass })
                .iterator();
        List objects = new LinkedList();
        String n="";
        boolean caughtError=true;
        Throwable caught=null;
    try {
        while (classes.hasNext())
        {
        	n = (String) classes.next();
        	//TODO - improve this check
        	if (n.endsWith("RemoteJMeterEngineImpl"))
        	{
        		continue; // Don't try to instantiate remote server
        	}
            Class c = Class.forName(n);
            try
            {
                try
                {
                    // Try with a parameter-less constructor first
                    objects.add(c.newInstance());
                }
                catch (InstantiationException e)
                {
                	caught=e;
					//System.out.println(e.toString());
                    try
                    {
                        // Events often have this constructor
                        objects.add(
                            c.getConstructor(
                                new Class[] { Object.class }).newInstance(
                                new Object[] { myThis }));
                    }
                    catch (NoSuchMethodException f)
                    {
                        // no luck. Ignore this class
						System.out.println(exName+": NoSuchMethodException  "+n);
                    }
                }
            }
            catch (NoClassDefFoundError e)
            {
				// no luck. Ignore this class
				System.out.println(exName+": NoClassDefFoundError "+n);
            }
            catch (IllegalAccessException e)
            {
				caught=e;
				System.out.println(exName+": IllegalAccessException "+n);
                // We won't test restricted-access classes.
            }
			//JDK1.4: catch (java.awt.HeadlessException e)
			//JDK1.4: {
			//JDK1.4: 	System.out.println("Error creating "+n+" "+e.toString());
			//JDK1.4: }
            catch (Exception e)
            {
				caught=e;
            	if ((e instanceof RemoteException)
            	   ||e.getClass().getName().equals("java.awt.HeadlessException")//for JDK1.3
            	   )
				{
					System.out.println("Error creating "+n+" "+e.toString());
				}
				else
				{
					throw new Exception("Error creating "+n+" "+e.toString());
				}
            }
        }
        caughtError=false;
    } 
    finally 
    {
    	if (caughtError)
    	{
			System.out.println("Last class="+n);
			System.out.println("objects.size="+objects.size());
			System.out.println("Last error="+caught);
    	}
    }
    
	    if (objects.size() == 0){
	    	System.out.println("No classes found that extend "+exName+". Check the following:");
	    	System.out.println("Search paths are:");
		    String ss[] = JMeterUtils.getSearchPaths();
		    for (int i=0;i<ss.length;i++){
			    System.out.println(ss[i]);
			}
	        System.out.println("Class path is:");
		    System.out.println(System.getProperty("java.class.path"));
		}
        return objects;
    }

    private static void cloneTesting(TestElement item, TestElement clonedItem)
    {
        assertTrue(item != clonedItem);
        assertEquals(
            "CLONE-SAME-CLASS: testing " + item.getClass().getName(),
            item.getClass().getName(),
            clonedItem.getClass().getName());
    }

    private static void checkElementCloning(TestElement item)
    {
        TestElement clonedItem = (TestElement) item.clone();
        cloneTesting(item, clonedItem);
        PropertyIterator iter2 = item.propertyIterator();
        while (iter2.hasNext())
        {
            JMeterProperty item2 = iter2.next();
            assertEquals(item2, clonedItem.getProperty(item2.getName()));
            assertTrue(item2 != clonedItem.getProperty(item2.getName()));
        }
    }
}
