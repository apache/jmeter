package org.apache.jmeter.junit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
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
    
    private static Set guiTitles;

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
    
	/*
	 * Use a suite to allow the tests to be generated at run-time
	 */
    public static Test suite() throws Exception{
    	TestSuite suite = new TestSuite();
    	suite.addTest(new JMeterTest("createTitleSet"));
    	suite.addTest(suiteGUIComponents());
		suite.addTest(suiteSerializableElements());
		suite.addTest(suiteTestElements());
        return suite;
    }
    
    /*
     * Extract titles from component_reference.xml
     */
    public void createTitleSet() throws Exception
    {
		guiTitles = new HashSet(90);
		
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
				guiTitles.add(comp.getAttributeValue("name")); 
			}
		}
		guiTitles.add("Root");// Not documented ...
    }
	/*
	 * Test GUI elements - create the suite of tests
	 */
	public static Test suiteGUIComponents() throws Exception
	{
		TestSuite suite = new TestSuite("GuiComponents");
		Iterator iter = getObjects(JMeterGUIComponent.class).iterator();
		while (iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
			if (item instanceof JMeterTreeNode)
			{
				continue;
			}
			TestSuite ts = new TestSuite(item.getClass().getName());
			ts.addTest(new JMeterTest("runGUIComponents",item));
			ts.addTest(new JMeterTest("runGUITitle",item));
			suite.addTest(ts);
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
			assertTrue("Component ref should contain entry for "+title,
			    guiTitles.contains(title));
		}
	}
	
	/*
	 * Test GUI elements - run the test
	 */
    public void runGUIComponents() throws Exception
    {
    	String name = guiItem.getClass().getName();
    	
    	//TODO these assertions could be separate tests
    	
        assertEquals(
            "Name should be same as static label for " + name,
            guiItem.getStaticLabel(),
            guiItem.getName());
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
                "GUI-CLASS: Failed on " + name,
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
	public static Test suiteSerializableElements() throws Exception
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

    /*
     * Test TestElements - create the suite
     */	
	public static Test suiteTestElements() throws Exception
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
						System.out.println(exName+": could not construct "+n);
                    }
                }
            }
            catch (IllegalAccessException e)
            {
				caught=e;
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
    catch (Exception t)
    {
    	caught = t;
    	throw t;
	}
	catch (Error t)
	{
		caught = t;
		throw t;
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
    	System.out.println("No classes found. Check the following:");
    	System.out.println("Search paths are:");
	    String ss[] = JMeterUtils.getSearchPaths();
	    for (int i=0;i<ss.length;i++){
		    System.out.println(ss[i]);
		}
        System.out.println("Class path is:");
	    System.out.println(System.getProperty("java.class.path"));
	}

        assertTrue("Expected to find some classes that extend "+exName,objects.size() > 0);
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
