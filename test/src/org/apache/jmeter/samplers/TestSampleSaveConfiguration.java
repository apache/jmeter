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

import junit.framework.TestCase;

public class TestSampleSaveConfiguration extends TestCase {    
    public TestSampleSaveConfiguration(String name) {
        super(name);
    }

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
        assertEquals(a.saveUrl(), b.saveUrl());
        assertEquals(a.saveAssertions(), b.saveAssertions());
        assertEquals(a.getDelimiter(), b.getDelimiter());
        assertEquals(a.saveDataType(), b.saveDataType());
        
        a.setAssertions(false);
        // a and b should not be equal
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.saveAssertions() == b.saveAssertions());
    }
}

