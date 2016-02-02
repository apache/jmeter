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

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.junit.Test;

public class TestInterleaveControl extends JMeterTestCase {

        @Test
        public void testProcessing() throws Exception {
            testLog.debug("Testing Interleave Controller 1");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(InterleaveControl.IGNORE_SUB_CONTROLLERS);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
            controller.addTestElement(sub_1);
            controller.addTestElement(new TestSampler("three"));
            LoopController sub_2 = new LoopController();
            sub_2.setLoops(3);
            GenericController sub_3 = new GenericController();
            sub_2.addTestElement(new TestSampler("four"));
            sub_3.addTestElement(new TestSampler("five"));
            sub_3.addTestElement(new TestSampler("six"));
            sub_2.addTestElement(sub_3);
            sub_2.addTestElement(new TestSampler("seven"));
            controller.addTestElement(sub_2);
            String[] interleaveOrder = new String[] { "one", "two" };
            String[] order = new String[] { "dummy", "three", "four", "five", "six", "seven", "four", "five", "six",
                    "seven", "four", "five", "six", "seven" };
            int counter = 14;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            for (int i = 0; i < 4; i++) {
                assertEquals(14, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    if (counter == 0) {
                        assertEquals(interleaveOrder[i % 2], sampler.getName());
                    } else {
                        assertEquals(order[counter], sampler.getName());
                    }
                    counter++;
                }
            }
        }

        @Test
        public void testProcessing6() throws Exception {
            testLog.debug("Testing Interleave Controller 6");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            controller.addTestElement(new TestSampler("one"));
            sub_1.setStyle(InterleaveControl.IGNORE_SUB_CONTROLLERS);
            controller.addTestElement(sub_1);
            LoopController sub_2 = new LoopController();
            sub_1.addTestElement(sub_2);
            sub_2.setLoops(3);
            int counter = 1;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            controller.initialize();
            for (int i = 0; i < 4; i++) {
                assertEquals(1, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("one", sampler.getName());
                    counter++;
                }
            }
        }

        @Test
        public void testProcessing2() throws Exception {
            testLog.debug("Testing Interleave Controller 2");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(InterleaveControl.IGNORE_SUB_CONTROLLERS);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
            controller.addTestElement(sub_1);
            controller.addTestElement(new TestSampler("three"));
            LoopController sub_2 = new LoopController();
            sub_2.setLoops(3);
            GenericController sub_3 = new GenericController();
            sub_2.addTestElement(new TestSampler("four"));
            sub_3.addTestElement(new TestSampler("five"));
            sub_3.addTestElement(new TestSampler("six"));
            sub_2.addTestElement(sub_3);
            sub_2.addTestElement(new TestSampler("seven"));
            sub_1.addTestElement(sub_2);
            String[] order = new String[] { "one", "three", "two", "three", "four", "three", "one", "three", "two",
                    "three", "five", "three", "one", "three", "two", "three", "six", "three", "one", "three" };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            while (counter < order.length) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("failed on " + counter, order[counter], sampler.getName());
                    counter++;
                }
            }
        }

        @Test
        public void testProcessing3() throws Exception {
            testLog.debug("Testing Interleave Controller 3");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(InterleaveControl.USE_SUB_CONTROLLERS);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
            controller.addTestElement(sub_1);
            controller.addTestElement(new TestSampler("three"));
            LoopController sub_2 = new LoopController();
            sub_2.setLoops(3);
            GenericController sub_3 = new GenericController();
            sub_2.addTestElement(new TestSampler("four"));
            sub_3.addTestElement(new TestSampler("five"));
            sub_3.addTestElement(new TestSampler("six"));
            sub_2.addTestElement(sub_3);
            sub_2.addTestElement(new TestSampler("seven"));
            sub_1.addTestElement(sub_2);
            String[] order = new String[] { "one", "three", "two", "three", "four", "five", "six", "seven", "four",
                    "five", "six", "seven", "four", "five", "six", "seven", "three", "one", "three", "two", "three" };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            while (counter < order.length) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("failed on" + counter, order[counter], sampler.getName());
                    counter++;
                }
            }
        }

        @Test
        public void testProcessing4() throws Exception {
            testLog.debug("Testing Interleave Controller 4");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(InterleaveControl.IGNORE_SUB_CONTROLLERS);
            controller.addTestElement(sub_1);
            GenericController sub_2 = new GenericController();
            sub_2.addTestElement(new TestSampler("one"));
            sub_2.addTestElement(new TestSampler("two"));
            sub_1.addTestElement(sub_2);
            GenericController sub_3 = new GenericController();
            sub_3.addTestElement(new TestSampler("three"));
            sub_3.addTestElement(new TestSampler("four"));
            sub_1.addTestElement(sub_3);
            String[] order = new String[] { "one", "three", "two", "four" };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            while (counter < order.length) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("failed on" + counter, order[counter], sampler.getName());
                    counter++;
                }
            }
        }

        @Test
        public void testProcessing5() throws Exception {
            testLog.debug("Testing Interleave Controller 5");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(InterleaveControl.USE_SUB_CONTROLLERS);
            controller.addTestElement(sub_1);
            GenericController sub_2 = new GenericController();
            sub_2.addTestElement(new TestSampler("one"));
            sub_2.addTestElement(new TestSampler("two"));
            sub_1.addTestElement(sub_2);
            GenericController sub_3 = new GenericController();
            sub_3.addTestElement(new TestSampler("three"));
            sub_3.addTestElement(new TestSampler("four"));
            sub_1.addTestElement(sub_3);
            String[] order = new String[] { "one", "two", "three", "four" };
            int counter = 0;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            while (counter < order.length) {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals("failed on" + counter, order[counter], sampler.getName());
                    counter++;
                }
            }
        }
}
