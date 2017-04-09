/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.junit.Test;

public class TestRandomOrderController extends JMeterTestCase {


        @Test
        public void testRandomOrder() {
            testLog.debug("Testing RandomOrderController");
            RandomOrderController roc = new RandomOrderController();
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
                    assertTrue("Duplicate sampler returned from next()", false);
                }
                usedSamplers.add(samplerName);
            }
            assertEquals("All samplers were returned", 4, usedSamplers.size());
        }

        @Test
        public void testRandomOrderNoElements() {
            RandomOrderController roc = new RandomOrderController();
            roc.initialize();
            assertNull(roc.next());
        }

        @Test
        public void testRandomOrderOneElement() {
            RandomOrderController roc = new RandomOrderController();
            roc.addTestElement(new TestSampler("zero"));
            TestElement sampler = null;
            List<String> usedSamplers = new ArrayList<>();
            roc.initialize();
            while ((sampler = roc.next()) != null) {
                String samplerName = sampler.getName();
                if (usedSamplers.contains(samplerName)) {
                    assertTrue("Duplicate sampler returned from next()", false);
                }
                usedSamplers.add(samplerName);
            }
            assertEquals("All samplers were returned", 1, usedSamplers.size());
        }
}
