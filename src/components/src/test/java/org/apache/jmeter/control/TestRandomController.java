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

package org.apache.jmeter.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.junit.jupiter.api.Test;

public class TestRandomController extends JMeterTestCase {

    @Test
    public void testRandomOrder() {
        testLog.debug("Testing RandomController");
        RandomController roc = new RandomController();
        roc.addTestElement(new TestSampler("zero"));
        roc.addTestElement(new TestSampler("one"));
        roc.addTestElement(new TestSampler("two"));
        roc.addTestElement(new TestSampler("three"));
        TestElement sampler = null;
        List<String> usedSamplers = new ArrayList<>();
        roc.initialize();
        while ((sampler = roc.next()) != null) {
            String samplerName = sampler.getName();
            if (usedSamplers.contains(samplerName)) {
                fail("Duplicate sampler returned from next()");
            }
            usedSamplers.add(samplerName);
        }
        assertEquals(1, usedSamplers.size());
        assertTrue(Arrays.asList("zero", "one", "two", "three").contains(usedSamplers.get(0)));
    }

    @Test
    public void testRandomNoElements() {
        RandomController roc = new RandomController();
        roc.initialize();
        assertNull(roc.next());
    }

    @Test
    public void testRandomOneElement() {
        RandomController roc = new RandomController();
        roc.addTestElement(new TestSampler("zero"));
        TestElement sampler = null;
        List<String> usedSamplers = new ArrayList<>();
        roc.initialize();
        while ((sampler = roc.next()) != null) {
            String samplerName = sampler.getName();
            if (usedSamplers.contains(samplerName)) {
                fail("Duplicate sampler returned from next()");
            }
            usedSamplers.add(samplerName);
        }
        assertEquals(1, usedSamplers.size());
    }
}
