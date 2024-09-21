/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.testelement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TestElementTest extends JMeterTestCase {
    /*
     * Test TestElements - create the suite
     */
    public static Collection<Object> testElements() throws Throwable {
        return JMeterTest.getObjects(TestElement.class);
    }

    @ParameterizedTest
    @MethodSource("testElements")
    public void runTestElement(TestElement testItem) throws Exception {
        checkElementCloning(testItem);
        String name = testItem.getClass().getName();
        assertInstanceOf(Serializable.class, testItem, () -> name + " must implement Serializable");
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
            assertNotSame(item2, clonedItem.getProperty(item2.getName()));
        }
    }

    private static void cloneTesting(TestElement item, TestElement clonedItem) {
        assertNotSame(item, clonedItem, "Cloned element must be a different instance");
        assertSame(item.getClass(), clonedItem.getClass(), "Cloned element should have the same class");
    }

    private void checkElementAlias(Object item) throws IOException {
        //FIXME do it only once
        Properties nameMap = SaveService.loadProperties();
        assertNotNull(nameMap, "SaveService nameMap (saveservice.properties) should not be null");

        String name = item.getClass().getName();
        boolean contains = nameMap.values().contains(name);
        if (!contains){
            fail("SaveService nameMap (saveservice.properties) should contain "+name);
        }
    }
}
