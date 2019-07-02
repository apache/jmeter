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

package org.apache.jmeter.samplers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.Test;

// Extends JMeterTest case because it needs access to JMeter properties
public class TestSampleSaveConfiguration extends JMeterTestCase {

    @Test
    public void testClone() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration();
        a.setUrl(false);
        a.setAssertions(true);
        a.setDefaultDelimiter();
        a.setDefaultTimeStampFormat();
        a.setDataType(true);
        assertFalse(a.saveUrl());
        assertNotNull(a.getDelimiter());
        assertTrue(a.saveAssertions());
        assertTrue(a.saveDataType());

        // Original and clone should be equal
        SampleSaveConfiguration cloneA = (SampleSaveConfiguration) a.clone();
        assertNotSame(a, cloneA);
        assertEquals(a, cloneA);
        assertTrue(a.equals(cloneA));
        assertTrue(cloneA.equals(a));
        assertEquals(a.hashCode(), cloneA.hashCode());

        // Change the original
        a.setUrl(true);
        assertFalse(a.equals(cloneA));
        assertFalse(cloneA.equals(a));
        assertFalse(a.hashCode() == cloneA.hashCode());

        // Change the original back again
        a.setUrl(false);
        assertEquals(a, cloneA);
        assertTrue(a.equals(cloneA));
        assertTrue(cloneA.equals(a));
        assertEquals(a.hashCode(), cloneA.hashCode());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration();
        a.setUrl(false);
        a.setAssertions(true);
        a.setDefaultDelimiter();
        a.setDefaultTimeStampFormat();
        a.setDataType(true);
        SampleSaveConfiguration b = new SampleSaveConfiguration();
        b.setUrl(false);
        b.setAssertions(true);
        b.setDefaultDelimiter();
        b.setDefaultTimeStampFormat();
        b.setDataType(true);

        // a and b should be equal
        assertEquals(a, b);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        assertPrimitiveEquals(a.saveUrl(), b.saveUrl());
        assertPrimitiveEquals(a.saveAssertions(), b.saveAssertions());
        assertEquals(a.getDelimiter(), b.getDelimiter());
        assertPrimitiveEquals(a.saveDataType(), b.saveDataType());

        a.setAssertions(false);
        // a and b should not be equal
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.saveAssertions() == b.saveAssertions());
    }

    @Test
    public void testFalse() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration(false);
        SampleSaveConfiguration b = new SampleSaveConfiguration(false);
        assertEquals("Hash codes should be equal",a.hashCode(), b.hashCode());
        assertTrue("Objects should be equal",a.equals(b));
        assertTrue("Objects should be equal",b.equals(a));
    }

    @Test
    public void testTrue() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration(true);
        SampleSaveConfiguration b = new SampleSaveConfiguration(true);
        assertEquals("Hash codes should be equal",a.hashCode(), b.hashCode());
        assertTrue("Objects should be equal",a.equals(b));
        assertTrue("Objects should be equal",b.equals(a));
    }
    @Test
    public void testFalseTrue() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration(false);
        SampleSaveConfiguration b = new SampleSaveConfiguration(true);
        assertFalse("Hash codes should not be equal",a.hashCode() == b.hashCode());
        assertFalse("Objects should not be equal",a.equals(b));
        assertFalse("Objects should not be equal",b.equals(a));
    }

    @Test
    public void testFormatter() throws Exception {
        SampleSaveConfiguration a = new SampleSaveConfiguration(false);
        SampleSaveConfiguration b = new SampleSaveConfiguration(false);
        assertEquals("Hash codes should be equal",a.hashCode(), b.hashCode());
        assertTrue("Objects should be equal",a.equals(b));
        assertTrue("Objects should be equal",b.equals(a));
        assertTrue(a.strictDateFormatter() == null);
        assertTrue(b.strictDateFormatter() == null);
        assertTrue(a.threadSafeLenientFormatter() == null);
        assertTrue(b.threadSafeLenientFormatter() == null);
        a.setDateFormat(null);
        b.setDateFormat(null);
        assertEquals("Hash codes should be equal",a.hashCode(), b.hashCode());
        assertTrue("Objects should be equal",a.equals(b));
        assertTrue("Objects should be equal",b.equals(a));
        assertTrue(a.strictDateFormatter() == null);
        assertTrue(b.strictDateFormatter() == null);
        assertTrue(a.threadSafeLenientFormatter() == null);
        assertTrue(b.threadSafeLenientFormatter() == null);
        a.setDateFormat("dd/MM/yyyy");
        b.setDateFormat("dd/MM/yyyy");
        assertEquals("Hash codes should be equal",a.hashCode(), b.hashCode());
        assertTrue("Objects should be equal",a.equals(b));
        assertTrue("Objects should be equal",b.equals(a));
        assertTrue("Objects should be equal",a.strictDateFormatter().equals(b.strictDateFormatter()));
        assertTrue("Objects should be equal",a.threadSafeLenientFormatter().equals(b.threadSafeLenientFormatter()));
    }

    @Test
    // Checks that all the saveXX() and setXXX(boolean) methods are in the list
    public void testSaveConfigNames() throws Exception {
        List<String> getMethodNames = new ArrayList<>();
        List<String> setMethodNames = new ArrayList<>();
        Method[] methods = SampleSaveConfiguration.class.getMethods();
        for(Method method : methods) {
            String name = method.getName();
            if (name.startsWith(SampleSaveConfiguration.CONFIG_GETTER_PREFIX) && method.getParameterTypes().length == 0) {
                name = name.substring(SampleSaveConfiguration.CONFIG_GETTER_PREFIX.length());
                getMethodNames.add(name);
                assertTrue("SAVE_CONFIG_NAMES should contain save" + name, SampleSaveConfiguration.SAVE_CONFIG_NAMES.contains(name));
            }
            if (name.startsWith(SampleSaveConfiguration.CONFIG_SETTER_PREFIX)
                    && method.getParameterTypes().length == 1
                    && boolean.class.equals(method.getParameterTypes()[0])) {
                name = name.substring(
                        SampleSaveConfiguration.CONFIG_SETTER_PREFIX.length());
                setMethodNames.add(name);
                assertTrue("SAVE_CONFIG_NAMES should contain set" + name,
                        SampleSaveConfiguration.SAVE_CONFIG_NAMES
                                .contains(name));
            }
        }
        for (String name : SampleSaveConfiguration.SAVE_CONFIG_NAMES) {
            assertTrue("SAVE_CONFIG_NAMES should NOT contain save" + name, getMethodNames.contains(name));
            assertTrue("SAVE_CONFIG_NAMES should NOT contain set" + name, setMethodNames.contains(name));
        }
    }

 }

