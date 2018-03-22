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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.ObsoleteGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JMeterTest extends JMeterTestCaseJUnit {
    private static final Logger log = LoggerFactory.getLogger(JMeterTest.class);

    private static Map<String, Boolean> guiTitles;

    private static Map<String, Boolean> guiTags;

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

    private static volatile boolean classPathShown = false;// Only show classpath once

    /*
     * Use a suite to allow the tests to be generated at run-time
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("JMeterTest");

        // The Locale used to instantiate the GUI objects
        JMeterUtils.setLocale(TEST_LOCALE);
        Locale.setDefault(TEST_LOCALE);
        // Needs to be done before any GUI classes are instantiated
        
        suite.addTest(new JMeterTest("readAliases"));
        suite.addTest(new JMeterTest("createTitleSet"));
        suite.addTest(new JMeterTest("createTagSet"));
        suite.addTest(suiteGUIComponents());
        suite.addTest(suiteSerializableElements());
        suite.addTest(suiteBeanComponents());
        suite.addTest(new JMeterTest("checkGuiSet"));
        
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
        guiTitles = new HashMap<>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        try (InputStream stream = new FileInputStream(findTestFile(compref))) {
            org.w3c.dom.Element body = getBodyFromXMLDocument(stream);
            NodeList sections = body.getElementsByTagName("section");
            for (int i = 0; i < sections.getLength(); i++) {
                org.w3c.dom.Element section = (org.w3c.dom.Element) sections.item(i);
                NodeList components = section.getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element) 
                            components.item(j);
                    String nm = comp.getAttribute("name");
                    if (!nm.equals("SSL Manager")) {// Not a true GUI component
                        guiTitles.put(nm.replace(' ', '_'), Boolean.FALSE);
                    }
                }
            }
        }
        // Add titles that don't need to be documented
        guiTitles.put("Example Sampler", Boolean.FALSE);
    }

    /**
     * @return
     * @throws ParserConfigurationException
     * @throws IOException 
     * @throws SAXException 
     * @throws FileNotFoundException 
     */
    private Element getBodyFromXMLDocument(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(stream));
        org.w3c.dom.Element root = doc.getDocumentElement();
        org.w3c.dom.Element body = (org.w3c.dom.Element) root.getElementsByTagName("body").item(0);
        return body;
    }

    /*
     * Extract titles from component_reference.xml
     */
    public void createTagSet() throws Exception {
        guiTags = new HashMap<>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        try (InputStream stream = new FileInputStream(findTestFile(compref))) {
            org.w3c.dom.Element body = getBodyFromXMLDocument(stream);
            NodeList sections = body.getElementsByTagName("section");
            
            for (int i = 0; i < sections.getLength(); i++) {
                org.w3c.dom.Element section = (org.w3c.dom.Element) sections.item(i);
                NodeList components = section.getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element) 
                            components.item(j);
                    String tag = comp.getAttribute("tag");
                    if (!StringUtils.isEmpty(tag)){
                        guiTags.put(tag, Boolean.FALSE);
                    }
                }
            }
        }
    }


    public static int scanprintMap(Map<String, Boolean> m, String t) {
        Set<String> s = m.keySet();
        if (s.isEmpty()) {
            return 0;
        }

        int unseen = 0;
        for (String key : s) {
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
        assertEquals(
                "Should not have any names left over, check name of components in EN (default) Locale, "
                + "which must match name attribute of component, check java.awt.HeadlessException errors before, we are running with '-Djava.awt.headless="
                + System.getProperty("java.awt.headless")+"'",
                0, scanprintMap(guiTitles, "GUI"));
    }

    /*
     * Test GUI elements - create the suite of tests
     */
    private static Test suiteGUIComponents() throws Exception {
        TestSuite suite = new TestSuite("GuiComponents");
        for (Object o : getObjects(JMeterGUIComponent.class)) {
            JMeterGUIComponent item = (JMeterGUIComponent) o;
            if (item instanceof JMeterTreeNode) {
                System.out.println("o.a.j.junit.JMeterTest INFO: JMeterGUIComponent: skipping all tests  " + item.getClass().getName());
                continue;
            }
            if (item instanceof ObsoleteGui) {
                continue;
            }
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new JMeterTest("GUIComponents1", item));
            if (item instanceof TestBeanGUI) {
                System.out.println("o.a.j.junit.JMeterTest INFO: JMeterGUIComponent: skipping some tests " + item.getClass().getName());
            } else {
                ts.addTest(new JMeterTest("GUIComponents2", item));
                ts.addTest(new JMeterTest("runGUITitle", item));
            }
            suite.addTest(ts);
        }
        return suite;
    }


    /*
     * Test GUI elements - create the suite of tests
     */
    private static Test suiteBeanComponents() throws Exception {
        TestSuite suite = new TestSuite("BeanComponents");
        for (Object o : getObjects(TestBean.class)) {
            Class<?> c = o.getClass();
            try {
                JMeterGUIComponent item = new TestBeanGUI(c);
                TestSuite ts = new TestSuite(item.getClass().getName());
                ts.addTest(new JMeterTest("GUIComponents2", item));
                ts.addTest(new JMeterTest("runGUITitle", item));
                suite.addTest(ts);
            } catch (IllegalArgumentException e) {
                System.out.println("o.a.j.junit.JMeterTest Cannot create test for " + c.getName() + " " + e);
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
                title != null && title.length() > 0 // Will be "" for internal components
                && !title.toUpperCase(Locale.ENGLISH).contains("(ALPHA")
                && !title.toUpperCase(Locale.ENGLISH).contains("(BETA")
                && !title.toUpperCase(Locale.ENGLISH).contains("(DEPRECATED")
                && !title.matches("Example\\d+") // Skip the example samplers ...
                && !name.startsWith("org.apache.jmeter.examples.")) 
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
                assertNotNull("Label should not be null for "+name, label);
                assertTrue("Label should not be empty for "+name, label.length() > 0);
                assertFalse("'" + label + "' should be in resource file for " + name, JMeterUtils.getResString(
                        label).startsWith(JMeterUtils.RES_KEY_PFX));
            } catch (UnsupportedOperationException uoe) {
                log.warn("Class has not yet implemented getLabelResource {}", name);
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
        log.debug("Saving element: {}", el.getClass());
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
        for (Object o : getObjects(Serializable.class)) {
            Serializable serObj = (Serializable) o;
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
            } catch (Exception e) {
                fail("serialization of " + serObj.getClass().getName() + " failed: " + e);
            }
        }
    }


    public void readAliases() throws Exception {
        nameMap = SaveService.loadProperties();
        assertNotNull("SaveService nameMap (saveservice.properties) should not be null",nameMap);
    }
    
    private void checkElementAlias(Object item) {
        String name=item.getClass().getName();
        boolean contains = nameMap.values().contains(name);
        if (!contains){
            fail("SaveService nameMap (saveservice.properties) should contain "+name);
        }
    }

    public static Collection<Object> getObjects(Class<?> extendsClass) throws Exception {
        String exName = extendsClass.getName();
        Object myThis = "";
        Iterator<String> classes = ClassFinder
                .findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { extendsClass }).iterator();
        List<Object> objects = new LinkedList<>();
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
                caught = instantiateClass(exName, myThis, objects, n, caught);
            }
            caughtError = false;
        } finally {
            if (caughtError) {
                System.out.println("Last class=" + n);
                System.out.println("objects.size=" + objects.size());
                System.out.println("Last error=" + caught);
            }
        }

        if (objects.isEmpty()) {
            System.out.println("No classes found that extend " + exName + ". Check the following:");
            System.out.println("Search paths are:");
            String[] ss = JMeterUtils.getSearchPaths();
            for (String s : ss) {
                System.out.println(s);
            }
            if (!classPathShown) {// Only dump it once
                System.out.println("Class path is:");
                String cp = System.getProperty("java.class.path");
                String[] classPathElements = JOrphanUtils.split(cp, java.io.File.pathSeparator);
                for (String classPathElement : classPathElements) {
                    System.out.println(classPathElement);
                }
                classPathShown = true;
            }
        }
        return objects;
    }

    private static Throwable instantiateClass(final String extendsClassName, final Object myThis,
            final List<Object> objects, final String className, final Throwable oldCaught) throws Exception {
        Throwable caught = oldCaught;
        try {
            Class<?> c = Class.forName(className);
            try {
                // Try with a parameter-less constructor first
                objects.add(c.newInstance());
            } catch (InstantiationException e) {
                caught = e;
                try {
                    // Events often have this constructor
                    objects.add(c.getConstructor(new Class[] { Object.class }).newInstance(
                            new Object[] { myThis }));
                } catch (NoSuchMethodException f) {
                    // no luck. Ignore this class
                    if (!Enum.class.isAssignableFrom(c)) { // ignore enums
                        System.out.println("o.a.j.junit.JMeterTest WARN: " + extendsClassName + ": NoSuchMethodException  " + 
                            className + ", missing empty Constructor or Constructor with Object parameter");
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            // no luck. Ignore this class
            System.out.println("o.a.j.junit.JMeterTest WARN: " + extendsClassName + ": NoClassDefFoundError " + className + ":" + e.getMessage());
            e.printStackTrace(System.out);
        } catch (IllegalAccessException e) {
            caught = e;
            System.out.println("o.a.j.junit.JMeterTest WARN: " + extendsClassName + ": IllegalAccessException " + className + ":" + e.getMessage());
            e.printStackTrace(System.out);
            // We won't test restricted-access classes.
        } catch (HeadlessException|ExceptionInInitializerError e) {// EIIE can be caused by Headless
            caught = e;
            System.out.println("o.a.j.junit.JMeterTest Error creating " + className + " " + e.toString());
        } catch (Exception e) {
            caught = e;
            if (e instanceof RemoteException) { // not thrown, so need to check here
                System.out.println("o.a.j.junit.JMeterTest WARN: " + "Error creating " + className + " " + e.toString());
            } else {
                throw new Exception("Error creating " + className, e);
            }
        }
        return caught;
    }
    
}
