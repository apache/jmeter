/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.Component;
import java.awt.HeadlessException;
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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.config.gui.ObsoleteGui;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
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
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class JMeterTest extends JMeterTestCase {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static Map<String, Boolean> guiTitles;

    private static Map<String, Boolean> guiTags;

    private static Map<String, Boolean> funcTitles;

    private static Properties nameMap;
    
    private static final Locale TEST_LOCALE = Locale.ENGLISH; 
    
    private static final Locale DEFAULT_LOCALE = Locale.getDefault(); 
    
    public JMeterTest(String name) {
        super(name);
    }

    /*
     * The suite() method creates separate test suites for each of the types of
     * test. The suitexxx() methods create a list of items to be tested, and
     * create a new test instance for each.
     * 
     * Each test type has its own constructor, which saves the item to be tested
     * 
     * Note that the suite() method must be static, and the methods to run the
     * tests must be instance methods so that they can pick up the item value
     * which was saved by the constructor.
     * 
     */
    // Constructor for TestElement tests
    private TestElement testItem;

    public JMeterTest(String testName, TestElement te) {
        super(testName);// Save the method name
        testItem = te;
    }

    // Constructor for Serializable tests
    private Serializable serObj;

    public JMeterTest(String testName, Serializable ser) {
        super(testName);// Save the method name
        serObj = ser;
    }

    // Constructor for GUI tests
    private JMeterGUIComponent guiItem;

    public JMeterTest(String testName, JMeterGUIComponent gc) {
        super(testName);// Save the method name
        guiItem = gc;
    }

    // Constructor for Function tests
    private Function funcItem;

    private static volatile boolean classPathShown = false;// Only show classpath once

    public JMeterTest(String testName, Function fi) {
        super(testName);// Save the method name
        funcItem = fi;
    }

    /*
     * Use a suite to allow the tests to be generated at run-time
     */
    public static Test suite() throws Exception {
        // The Locale used to instantiate the GUI objects
        JMeterUtils.setLocale(TEST_LOCALE);
        Locale.setDefault(TEST_LOCALE);
        // Needs to be done before any GUI classes are instantiated
        
        TestSuite suite = new TestSuite("JMeterTest");
        suite.addTest(new JMeterTest("readAliases"));
        suite.addTest(new JMeterTest("createTitleSet"));
        suite.addTest(new JMeterTest("createTagSet"));
        suite.addTest(suiteGUIComponents());
        suite.addTest(suiteSerializableElements());
        suite.addTest(suiteTestElements());
        suite.addTest(suiteBeanComponents());
        suite.addTest(new JMeterTest("createFunctionSet"));
        suite.addTest(suiteFunctions());
        suite.addTest(new JMeterTest("checkGuiSet"));
        suite.addTest(new JMeterTest("checkFunctionSet"));
        
        suite.addTest(new JMeterTest("resetLocale")); // revert
        return suite;
    }

    // Restore the original Locale
    public void resetLocale(){
        JMeterUtils.setLocale(DEFAULT_LOCALE);
        Locale.setDefault(DEFAULT_LOCALE);
    }

    /*
     * Extract titles from component_reference.xml
     */
    public void createTitleSet() throws Exception {
        guiTitles = new HashMap<String, Boolean>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        SAXBuilder bldr = new SAXBuilder();
        Document doc;
        doc = bldr.build(compref);
        Element root = doc.getRootElement();
        Element body = root.getChild("body");
        @SuppressWarnings("unchecked")
        List<Element> sections = body.getChildren("section");
        for (int i = 0; i < sections.size(); i++) {
            @SuppressWarnings("unchecked")
            List<Element> components = sections.get(i).getChildren("component");
            for (int j = 0; j < components.size(); j++) {
                Element comp = components.get(j);
                String nm=comp.getAttributeValue("name");
                if (!nm.equals("SSL Manager")){// Not a true GUI component
                    guiTitles.put(nm.replace(' ','_'), Boolean.FALSE);
                }
            }
        }
        // Add titles that don't need to be documented
        //guiTitles.put("Root", Boolean.FALSE);
        guiTitles.put("Example Sampler", Boolean.FALSE);
    }

    /*
     * Extract titles from component_reference.xml
     */
    public void createTagSet() throws Exception {
        guiTags = new HashMap<String, Boolean>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        SAXBuilder bldr = new SAXBuilder();
        Document doc;
        doc = bldr.build(compref);
        Element root = doc.getRootElement();
        Element body = root.getChild("body");
        @SuppressWarnings("unchecked")
        List<Element> sections = body.getChildren("section");
        for (int i = 0; i < sections.size(); i++) {
            @SuppressWarnings("unchecked")
            List<Element> components = sections.get(i).getChildren("component");
            for (int j = 0; j < components.size(); j++) {
                Element comp = components.get(j);
                guiTags.put(comp.getAttributeValue("tag"), Boolean.FALSE);
            }
        }
    }

    /*
     * Extract titles from functions.xml
     */
    public void createFunctionSet() throws Exception {
        funcTitles = new HashMap<String, Boolean>(20);

        String compref = "../xdocs/usermanual/functions.xml";
        SAXBuilder bldr = new SAXBuilder();
        Document doc;
        doc = bldr.build(compref);
        Element root = doc.getRootElement();
        Element body = root.getChild("body");
        Element section = body.getChild("section");
        @SuppressWarnings("unchecked")
        List<Element> sections = section.getChildren("subsection");
        for (int i = 0; i < sections.size(); i++) {
            @SuppressWarnings("unchecked")
            List<Element> components = sections.get(i).getChildren("component");
            for (int j = 0; j < components.size(); j++) {
                Element comp = components.get(j);
                funcTitles.put(comp.getAttributeValue("name"), Boolean.FALSE);
            }
        }
    }

    private int scanprintMap(Map<String, Boolean> m, String t) {
        Set<String> s = m.keySet();
        int unseen = 0;
        if (s.size() == 0) {
            return 0;
        }
        Iterator<String> i = s.iterator();
        while (i.hasNext()) {
            String key = i.next();
            if (!m.get(key).equals(Boolean.TRUE)) {
                if (unseen == 0)// first time
                {
                    System.out.println("\nNames remaining in " + t + " Map:");
                }
                unseen++;
                System.out.println(key);
            }
        }
        return unseen;
    }

    public void checkGuiSet() throws Exception {
        guiTitles.remove("Example Sampler");// We don't mind if this is left over
        guiTitles.remove("Sample_Result_Save_Configuration");// Ditto, not a sampler
        assertEquals("Should not have any names left over", 0, scanprintMap(guiTitles, "GUI"));
    }

    public void checkFunctionSet() throws Exception {
        assertEquals("Should not have any names left over", 0, scanprintMap(funcTitles, "Function"));
    }

    /*
     * Test GUI elements - create the suite of tests
     */
    private static Test suiteGUIComponents() throws Exception {
        TestSuite suite = new TestSuite("GuiComponents");
        Iterator<Object> iter = getObjects(JMeterGUIComponent.class).iterator();
        while (iter.hasNext()) {
            JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
            if (item instanceof JMeterTreeNode) {
                System.out.println("INFO: JMeterGUIComponent: skipping all tests  " + item.getClass().getName());
                continue;
            }
            if (item instanceof ObsoleteGui){
                continue;
            }
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new JMeterTest("GUIComponents1", item));
            if (item instanceof TestBeanGUI) {
                System.out.println("INFO: JMeterGUIComponent: skipping some tests " + item.getClass().getName());
            } else {
                ts.addTest(new JMeterTest("GUIComponents2", item));
                ts.addTest(new JMeterTest("runGUITitle", item));
            }
            suite.addTest(ts);
        }
        return suite;
    }

    /*
     * Test Functions - create the suite of tests
     */
    private static Test suiteFunctions() throws Exception {
        TestSuite suite = new TestSuite("Functions");
        Iterator<Object> iter = getObjects(Function.class).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item.getClass().equals(CompoundVariable.class)) {
                continue;
            }
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new JMeterTest("runFunction", (Function) item));
            ts.addTest(new JMeterTest("runFunction2", (Function) item));
            suite.addTest(ts);
        }
        return suite;
    }

    /*
     * Test GUI elements - create the suite of tests
     */
    private static Test suiteBeanComponents() throws Exception {
        TestSuite suite = new TestSuite("BeanComponents");
        Iterator<Object> iter = getObjects(TestBean.class).iterator();
        while (iter.hasNext()) {
            Class<? extends Object> c = iter.next().getClass();
            try {
                JMeterGUIComponent item = new TestBeanGUI(c);
                // JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
                TestSuite ts = new TestSuite(item.getClass().getName());
                ts.addTest(new JMeterTest("GUIComponents2", item));
                ts.addTest(new JMeterTest("runGUITitle", item));
                suite.addTest(ts);
            } catch (IllegalArgumentException e) {
                System.out.println("Cannot create test for " + c.getName() + " " + e);
                e.printStackTrace(System.out);
            }
        }
        return suite;
    }

    /*
     * Test GUI elements - run the test
     */
    public void runGUITitle() throws Exception {
        if (guiTitles.size() > 0) {
            String title = guiItem.getDocAnchor();
            boolean ct = guiTitles.containsKey(title);
            if (ct) {
                guiTitles.put(title, Boolean.TRUE);// So we can detect extra entries
            }
            String name = guiItem.getClass().getName();
            if (// Is this a work in progress or an internal GUI component?
                (title != null && title.length() > 0) // Will be "" for internal components
                && (title.toUpperCase(java.util.Locale.ENGLISH).indexOf("(ALPHA") == -1) 
                && (title.toUpperCase(java.util.Locale.ENGLISH).indexOf("(BETA") == -1)
                && (!title.equals("Example1")) // Skip the example samplers ...
                && (!title.equals("Example2"))
                && (!name.startsWith("org.apache.jmeter.examples."))
                )
            {// No, not a work in progress ...
                String s = "component_reference.xml needs '" + title + "' anchor for " + name;
                if (!ct) {
                    log.warn(s); // Record in log as well
                }
                assertTrue(s, ct);
            }
        }
    }

    /*
     * run the function test
     */
    public void runFunction() throws Exception {
        if (funcTitles.size() > 0) {
            String title = funcItem.getReferenceKey();
            boolean ct = funcTitles.containsKey(title);
            if (ct) {
                funcTitles.put(title, Boolean.TRUE);// For detecting extra entries
            }
            if (// Is this a work in progress ?
            title.indexOf("(ALPHA") == -1 && title.indexOf("(EXPERIMENTAL") == -1) {// No,
                                                                                    // not
                                                                                    // a
                                                                                    // work
                                                                                    // in
                                                                                    // progress
                                                                                    // ...
                String s = "function.xml needs '" + title + "' entry for " + funcItem.getClass().getName();
                if (!ct) {
                    log.warn(s); // Record in log as well
                }
                assertTrue(s, ct);
            }
        }
    }

    /*
     * Check that function descriptions are OK
     */
    public void runFunction2() throws Exception {
        Iterator<?> i = funcItem.getArgumentDesc().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            assertTrue("Description must be a String", o instanceof String);
            assertFalse("Description must not start with [refkey", ((String) o).startsWith("[refkey"));
        }
    }

    /*
     * Test GUI elements - run for all components
     */
    public void GUIComponents1() throws Exception {
        String name = guiItem.getClass().getName();

        assertEquals("Name should be same as static label for " + name, guiItem.getStaticLabel(), guiItem.getName());
        if (name.startsWith("org.apache.jmeter.examples.")){
            return;
        }
        if (!name.endsWith("TestBeanGUI")) {
            try {
                String label = guiItem.getLabelResource();
                assertTrue("Label should not be null or empty for "+name,label != null && label.length() > 0);
                assertFalse("'" + label + "' should be in resource file for " + name, JMeterUtils.getResString(
                        label).startsWith(JMeterUtils.RES_KEY_PFX));
            } catch (UnsupportedOperationException uoe) {
                log.warn("Class has not yet implemented getLabelResource " + name);
            }
        }
        checkElementAlias(guiItem);
    }

    /*
     * Test GUI elements - not run for TestBeanGui items
     */
    public void GUIComponents2() throws Exception {
        String name = guiItem.getClass().getName();

        // TODO these assertions should be separate tests

        TestElement el = guiItem.createTestElement();
        assertNotNull(name + ".createTestElement should be non-null ", el);
        assertEquals("GUI-CLASS: Failed on " + name, name, el.getPropertyAsString(TestElement.GUI_CLASS));

        assertEquals("NAME: Failed on " + name, guiItem.getName(), el.getName());
        assertEquals("TEST-CLASS: Failed on " + name, el.getClass().getName(), el
                .getPropertyAsString(TestElement.TEST_CLASS));
        TestElement el2 = guiItem.createTestElement();
        el.setName("hey, new name!:");
        el.setProperty("NOT", "Shouldn't be here");
        if (!(guiItem instanceof UnsharedComponent)) {
            assertEquals("SHARED: Failed on " + name, "", el2.getPropertyAsString("NOT"));
        }
        log.debug("Saving element: " + el.getClass());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SaveService.saveElement(el, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        el = (TestElement) SaveService.loadElement(bis);
        bis.close();
        assertNotNull("Load element failed on: "+name,el);
        guiItem.configure(el);
        assertEquals("CONFIGURE-TEST: Failed on " + name, el.getName(), guiItem.getName());
        guiItem.modifyTestElement(el2);
        assertEquals("Modify Test: Failed on " + name, "hey, new name!:", el2.getName());
    }

    /*
     * Test serializable elements - create the suite of tests
     */
    private static Test suiteSerializableElements() throws Exception {
        TestSuite suite = new TestSuite("SerializableElements");
        Iterator<Object> iter = getObjects(Serializable.class).iterator();
        while (iter.hasNext()) {
            Serializable serObj = (Serializable) iter.next();
            if (serObj.getClass().getName().endsWith("_Stub")) {
                continue;
            }
            TestSuite ts = new TestSuite(serObj.getClass().getName());
            ts.addTest(new JMeterTest("runSerialTest", serObj));
            suite.addTest(ts);
        }
        return suite;
    }

    /*
     * Test serializable elements - test the object
     */
    public void runSerialTest() throws Exception {
        if (!(serObj instanceof Component)) {// 
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bytes);
                out.writeObject(serObj);
                out.close();
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                Object readObject = in.readObject();
                in.close();
                assertEquals("deserializing class: " + serObj.getClass().getName(), serObj.getClass(), readObject
                        .getClass());
            } catch (Throwable e) {
                fail("serialization of " + serObj.getClass().getName() + " failed: " + e);
            }
        }
    }

    /*
     * Test TestElements - create the suite
     */
    private static Test suiteTestElements() throws Exception {
        TestSuite suite = new TestSuite("TestElements");
        Iterator<Object> iter = getObjects(TestElement.class).iterator();
        while (iter.hasNext()) {
            TestElement item = (TestElement) iter.next();
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new JMeterTest("runTestElement", item));
            suite.addTest(ts);
        }
        return suite;
    }

    /*
     * Test TestElements - implement the test case
     */
    public void runTestElement() throws Exception {
        checkElementCloning(testItem);
        String name = testItem.getClass().getName();
        assertTrue(name + " must implement Serializable", testItem instanceof Serializable);
        if (name.startsWith("org.apache.jmeter.examples.")){
            return;
        }
        if (name.equals("org.apache.jmeter.control.TransactionSampler")){
            return; // Not a real sampler
        }
            
        checkElementAlias(testItem);
    }

    public void readAliases() throws Exception {
        nameMap = SaveService.loadProperties();        
        assertNotNull("SaveService nameMap (saveservice.properties) should not be null",nameMap);
    }
    
    private void checkElementAlias(Object item) {
        String name=item.getClass().getName();
        boolean contains = nameMap.values().contains(name);
        if (!contains){
            //System.out.println(name.substring(name.lastIndexOf('.')+1)+"="+name);
            fail("SaveService nameMap (saveservice.properties) should contain "+name);
        }
    }

    private static Collection<Object> getObjects(Class<?> extendsClass) throws Exception {
        String exName = extendsClass.getName();
        Object myThis = "";
        Iterator<String> classes = ClassFinder
                .findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { extendsClass }).iterator();
        List<Object> objects = new LinkedList<Object>();
        String n = "";
        boolean caughtError = true;
        Throwable caught = null;
        try {
            while (classes.hasNext()) {
                n = classes.next();
                // TODO - improve this check
                if (n.endsWith("RemoteJMeterEngineImpl")) {
                    continue; // Don't try to instantiate remote server
                }
                Class<?> c = null;
                try {
                    c = Class.forName(n);
                    try {
                        // Try with a parameter-less constructor first
                        objects.add(c.newInstance());
                    } catch (InstantiationException e) {
                        caught = e;
                        // System.out.println(e.toString());
                        try {
                            // Events often have this constructor
                            objects.add(c.getConstructor(new Class[] { Object.class }).newInstance(
                                    new Object[] { myThis }));
                        } catch (NoSuchMethodException f) {
                            // no luck. Ignore this class
                            System.out.println("WARN: " + exName + ": NoSuchMethodException  " + n);
                        }
                    }
                } catch (NoClassDefFoundError e) {
                    // no luck. Ignore this class
                    System.out.println("WARN: " + exName + ": NoClassDefFoundError " + n);
                } catch (IllegalAccessException e) {
                    caught = e;
                    System.out.println("WARN: " + exName + ": IllegalAccessException " + n);
                    // We won't test restricted-access classes.
                } catch (HeadlessException e) {
                    caught = e;
                    System.out.println("Error creating "+n+" "+e.toString());
                } catch (Exception e) {
                    caught = e;
                    if (e instanceof RemoteException) { // not thrown, so need to check here
                        System.out.println("WARN: " + "Error creating " + n + " " + e.toString());
                    } else {
                        throw new Exception("Error creating " + n, e);
                    }
                }
            }
            caughtError = false;
        } finally {
            if (caughtError) {
                System.out.println("Last class=" + n);
                System.out.println("objects.size=" + objects.size());
                System.out.println("Last error=" + caught);
            }
        }

        if (objects.size() == 0) {
            System.out.println("No classes found that extend " + exName + ". Check the following:");
            System.out.println("Search paths are:");
            String ss[] = JMeterUtils.getSearchPaths();
            for (int i = 0; i < ss.length; i++) {
                System.out.println(ss[i]);
            }
            if (!classPathShown) {// Only dump it once
                System.out.println("Class path is:");
                String cp = System.getProperty("java.class.path");
                String cpe[] = JOrphanUtils.split(cp, java.io.File.pathSeparator);
                for (int i = 0; i < cpe.length; i++) {
                    System.out.println(cpe[i]);
                }
                classPathShown = true;
            }
        }
        return objects;
    }

    private static void cloneTesting(TestElement item, TestElement clonedItem) {
        assertTrue(item != clonedItem);
        assertEquals("CLONE-SAME-CLASS: testing " + item.getClass().getName(), item.getClass().getName(), clonedItem
                .getClass().getName());
    }

    private static void checkElementCloning(TestElement item) {
        TestElement clonedItem = (TestElement) item.clone();
        cloneTesting(item, clonedItem);
        PropertyIterator iter2 = item.propertyIterator();
        while (iter2.hasNext()) {
            JMeterProperty item2 = iter2.next();
            // [sebb] assertEquals(item2,
            // clonedItem.getProperty(item2.getName()));
            assertEquals(item2.getStringValue(), clonedItem.getProperty(item2.getName()).getStringValue());
            assertTrue(item2 != clonedItem.getProperty(item2.getName()));
        }
    }
}
