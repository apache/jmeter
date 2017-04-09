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

package org.apache.jmeter.functions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ComponentReferenceFunctionTest extends JMeterTestCaseJUnit {

    private static final Logger log = LoggerFactory.getLogger(ComponentReferenceFunctionTest.class);
    
    private static Map<String, Boolean> funcTitles;
    
    // Constructor for Function tests
    private Function funcItem;
    
    public ComponentReferenceFunctionTest(String name) {
        super(name);
    }
    
    public ComponentReferenceFunctionTest(String testName, Function fi) {
        super(testName);// Save the method name
        funcItem = fi;
    }
    
    /*
     * Test Functions - create the suite of tests
     */
    private static Test suiteFunctions() throws Exception {
        TestSuite suite = new TestSuite("Functions");
        for (Object item : JMeterTest.getObjects(Function.class)) {
            if (item.getClass().equals(CompoundVariable.class)) {
                continue;
            }
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new ComponentReferenceFunctionTest("runFunction", (Function) item));
            ts.addTest(new ComponentReferenceFunctionTest("runFunction2", (Function) item));
            suite.addTest(ts);
        }
        return suite;
    }
    
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
     * Extract titles from functions.xml
     */
    public void createFunctionSet() throws Exception {
        funcTitles = new HashMap<>(20);
        String compref = "../xdocs/usermanual/functions.xml";
        try (InputStream stream = new FileInputStream(compref)) {
            Element body = getBodyFromXMLDocument(stream);
            Element section = (Element) body.getElementsByTagName("section").item(0);
            NodeList subSections = section.getElementsByTagName("subsection");
            for (int i = 0; i < subSections.getLength(); i++) {
                NodeList components = ((Element)subSections.item(i)).getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element) 
                            components.item(j);
                    funcTitles.put(comp.getAttribute("name"), Boolean.FALSE);
                    String tag = comp.getAttribute("tag");
                    if (!StringUtils.isEmpty(tag)){
                        funcTitles.put(tag, Boolean.FALSE);                    
                    }
                }
            }
        }
    }
    
    public void checkFunctionSet() throws Exception {
        assertEquals("Should not have any names left over", 0, JMeterTest.scanprintMap(funcTitles, "Function"));
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
            // Is this a work in progress ?
            if (!title.contains("(ALPHA") && !title.contains("(EXPERIMENTAL")) {
                // No, not a work in progress ...
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
        for (Object o : funcItem.getArgumentDesc()) {
            assertTrue("Description must be a String", o instanceof String);
            assertFalse("Description must not start with [refkey", ((String) o).startsWith("[refkey"));
        }
    }
    
    /*
     * Use a suite to allow the tests to be generated at run-time
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("ComponentReferenceFunctionTest");
        suite.addTest(new ComponentReferenceFunctionTest("createFunctionSet"));
        suite.addTest(suiteFunctions());
        suite.addTest(new ComponentReferenceFunctionTest("checkFunctionSet"));
        return suite;
    }
}
