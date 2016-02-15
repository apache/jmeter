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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.junit.JMeterTestCaseJUnit3;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ComponentReferenceFunctionTest extends JMeterTestCaseJUnit3 {

    private static final Logger LOG = LoggingManager.getLoggerForClass();
    
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
        Iterator<Object> iter = JMeterTest.getObjects(Function.class).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
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
    
    /*
     * Extract titles from functions.xml
     */
    public void createFunctionSet() throws Exception {
        funcTitles = new HashMap<>(20);

        String compref = "../xdocs/usermanual/functions.xml";
        SAXBuilder bldr = new SAXBuilder();
        Document doc = bldr.build(compref);
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
                String tag = comp.getAttributeValue("tag");
                if (tag != null){
                    funcTitles.put(tag, Boolean.FALSE);                    
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
            if (// Is this a work in progress ?
            title.indexOf("(ALPHA") == -1 && title.indexOf("(EXPERIMENTAL") == -1) {// No, not a
                                                                                    // work in progress
                                                                                    // ...
                String s = "function.xml needs '" + title + "' entry for " + funcItem.getClass().getName();
                if (!ct) {
                    LOG.warn(s); // Record in log as well
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
