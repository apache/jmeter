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

package org.apache.jmeter.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.junit.Test;

/**
 * This class represents a controller that can control the number of times that
 * it is executed, either by the total number of times the user wants the
 * controller executed (BYNUMBER) or by the percentage of time it is called
 * (BYPERCENT)
 * 
 */
public class TestThroughputController extends JMeterTestCase {

        @Test
        public void testByNumber() throws Exception {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(ThroughputController.BYNUMBER);
            sub_1.setMaxThroughput(2);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController loop = new LoopController();
            loop.setLoops(5);
            loop.addTestElement(new TestSampler("zero"));
            loop.addTestElement(sub_1);
            loop.addIterationListener(sub_1);
            loop.addTestElement(new TestSampler("three"));

            LoopController test = new LoopController();
            test.setLoops(2);
            test.addTestElement(loop);

            String[] order = new String[] { "zero", "one", "two", "three", "zero", "one", "two", "three", "zero",
                    "three", "zero", "three", "zero", "three", "zero", "three", "zero", "three", "zero", "three",
                    "zero", "three", "zero", "three", };
            sub_1.testStarted();
            test.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            loop.setRunningVersion(true);
            test.initialize();
            for (int counter = 0; counter < order.length; counter++) {
                TestElement sampler = test.next();
                assertNotNull(sampler);
                assertEquals("Counter: " + counter, order[counter], sampler.getName());
            }
            assertNull(test.next());
            sub_1.testEnded();
        }

        @Test
        public void testByNumberZero() throws Exception {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(ThroughputController.BYNUMBER);
            sub_1.setMaxThroughput(0);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController controller = new LoopController();
            controller.setLoops(5);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));

            String[] order = new String[] { "zero", "three", "zero", "three", "zero", "three", "zero", "three", "zero",
                    "three", };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_1.testStarted();
            controller.initialize();
            for (int i = 0; i < 3; i++) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("Counter: " + counter + ", i: " + i, order[counter], sampler.getName());
                    counter++;
                }
                assertEquals(counter, order.length);
                counter = 0;
            }
            sub_1.testEnded();
        }

        @Test
        public void testByPercent33() throws Exception {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(ThroughputController.BYPERCENT);
            sub_1.setPercentThroughput(33.33f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController controller = new LoopController();
            controller.setLoops(6);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));
            // Expected results established using the DDA
            // algorithm (see
            // http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/drawline.htm):
            String[] order = new String[] { "zero", // 0/1 vs. 1/1 -> 0 is
                                                    // closer to 33.33
                    "three", "zero", // 0/2 vs. 1/2 -> 50.0 is closer to
                                        // 33.33
                    "one", "two", "three", "zero", // 1/3 vs. 2/3 -> 33.33 is
                                                    // closer to 33.33
                    "three", "zero", // 1/4 vs. 2/4 -> 25.0 is closer to
                                        // 33.33
                    "three", "zero", // 1/5 vs. 2/5 -> 40.0 is closer to
                                        // 33.33
                    "one", "two", "three", "zero", // 2/6 vs. 3/6 -> 33.33 is
                                                    // closer to 33.33
                    "three",
            // etc...
            };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_1.testStarted();
            controller.initialize();
            for (int i = 0; i < 3; i++) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("Counter: " + counter + ", i: " + i, order[counter], sampler.getName());
                    counter++;
                }
                assertEquals(counter, order.length);
                counter = 0;
            }
            sub_1.testEnded();
        }

        @Test
        public void testByPercentZero() throws Exception {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(ThroughputController.BYPERCENT);
            sub_1.setPercentThroughput(0.0f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController controller = new LoopController();
            controller.setLoops(150);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));

            String[] order = new String[] { "zero", "three", };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_1.testStarted();
            controller.initialize();
            for (int i = 0; i < 3; i++) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("Counter: " + counter + ", i: " + i, order[counter % order.length], sampler.getName());
                    counter++;
                }
                assertEquals(counter, 150 * order.length);
                counter = 0;
            }
            sub_1.testEnded();
        }

        @Test
        public void testByPercent100() throws Exception {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(ThroughputController.BYPERCENT);
            sub_1.setPercentThroughput(100.0f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController controller = new LoopController();
            controller.setLoops(150);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));

            String[] order = new String[] { "zero", "one", "two", "three", };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_1.testStarted();
            controller.initialize();
            for (int i = 0; i < 3; i++) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("Counter: " + counter + ", i: " + i, order[counter % order.length], sampler.getName());
                    counter++;
                }
                assertEquals(counter, 150 * order.length);
                counter = 0;
            }
            sub_1.testEnded();
        }
}
