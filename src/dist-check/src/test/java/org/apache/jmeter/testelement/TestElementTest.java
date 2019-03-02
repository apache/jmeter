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

package org.apache.jmeter.testelement;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestElementTest extends JMeterTestCaseJUnit {

    private TestElement testItem;

    public TestElementTest(String testName, TestElement te) {
        super(testName);// Save the method name
        testItem = te;
    }

    /*
     * Test TestElements - create the suite
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("TestElements");
        for (Object o : JMeterTest.getObjects(TestElement.class)) {
            TestElement item = (TestElement) o;
            TestSuite ts = new TestSuite(item.getClass().getName());
            ts.addTest(new TestElementTest("runTestElement", item));
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

    private static void checkElementCloning(TestElement item) {
        TestElement clonedItem = (TestElement) item.clone();
        cloneTesting(item, clonedItem);
        PropertyIterator iter2 = item.propertyIterator();
        while (iter2.hasNext()) {
            JMeterProperty item2 = iter2.next();
            assertEquals(item2.getStringValue(), clonedItem.getProperty(item2.getName()).getStringValue());
            assertTrue(item2 != clonedItem.getProperty(item2.getName()));
        }
    }

    private static void cloneTesting(TestElement item, TestElement clonedItem) {
        assertTrue(item != clonedItem);
        assertEquals("CLONE-SAME-CLASS: testing " + item.getClass().getName(), item.getClass().getName(), clonedItem
                .getClass().getName());
    }

    private void checkElementAlias(Object item) throws IOException {
        //FIXME do it only once
        Properties nameMap = SaveService.loadProperties();
        assertNotNull("SaveService nameMap (saveservice.properties) should not be null",nameMap);

        String name = item.getClass().getName();
        boolean contains = nameMap.values().contains(name);
        if (!contains){
            fail("SaveService nameMap (saveservice.properties) should contain "+name);
        }
    }
}
