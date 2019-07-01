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

public class TestOnceOnlyController extends JMeterTestCase {

    @Test
    public void testProcessing() throws Exception {
        GenericController controller = new GenericController();
        GenericController sub_1 = new OnceOnlyController();
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
        String[] order = new String[] { "", "", "three", "four", "five", "six", "seven", "four", "five", "six",
            "seven", "four", "five", "six", "seven" };
        int counter = 15;
        controller.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        controller.initialize();
        for (int i = 0; i < 4; i++) {
        assertEquals(15, counter);
        counter = 0;
        if (i > 0) {
            counter = 2;
        }
        TestElement sampler = null;
        while ((sampler = controller.next()) != null) {
            if (i == 0 && counter < 2) {
            assertEquals(interleaveOrder[counter], sampler.getName());
            } else {
            assertEquals(order[counter], sampler.getName());
            }
            counter++;
        }
        }
    }

    @Test
    public void testProcessing2() throws Exception {
        GenericController controller = new GenericController();
        GenericController sub_1 = new OnceOnlyController();
        sub_1.addTestElement(new TestSampler("one"));
        sub_1.addTestElement(new TestSampler("two"));
        controller.addTestElement(sub_1);
        controller.addTestElement(new TestSampler("three"));
        LoopController sub_2 = new LoopController();
        sub_2.setLoops(3);
        OnceOnlyController sub_3 = new OnceOnlyController();
        sub_2.addTestElement(new TestSampler("four"));
        sub_3.addTestElement(new TestSampler("five"));
        sub_3.addTestElement(new TestSampler("six"));
        sub_2.addTestElement(sub_3);
        sub_2.addIterationListener(sub_3);
        sub_2.addTestElement(new TestSampler("seven"));
        controller.addTestElement(sub_2);
        String[] interleaveOrder = new String[] { "one", "two" };
        String[] order = new String[] { "", "", "three", "four", "five", "six", "seven", "four", "seven", "four",
            "seven" };
        int counter = 11;
        controller.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        controller.initialize();
        for (int i = 0; i < 4; i++) {
        assertEquals(11, counter);
        counter = 0;
        if (i > 0) {
            counter = 2;
        }
        TestElement sampler = null;
        while ((sampler = controller.next()) != null) {
            if (i == 0 && counter < 2) {
            assertEquals(interleaveOrder[counter], sampler.getName());
            } else {
            assertEquals(order[counter], sampler.getName());
            }
            counter++;
        }
        }
    }

    @Test
    public void testInOuterLoop() throws Exception {
        // Set up the test plan
        LoopController controller = new LoopController();
        final int outerLoopCount = 4;
        controller.setLoops(outerLoopCount);
        // OnlyOnce samples
        OnceOnlyController sub_1 = new OnceOnlyController();
        sub_1.addTestElement(new TestSampler("one"));
        sub_1.addTestElement(new TestSampler("two"));
        controller.addTestElement(sub_1);
        controller.addIterationListener(sub_1);
        // Outer sample
        controller.addTestElement(new TestSampler("three"));
        // Inner loop
        LoopController sub_2 = new LoopController();
        final int innerLoopCount = 3;
        sub_2.setLoops(innerLoopCount);
        GenericController sub_3 = new GenericController();
        sub_2.addTestElement(new TestSampler("four"));
        sub_3.addTestElement(new TestSampler("five"));
        sub_3.addTestElement(new TestSampler("six"));
        sub_2.addTestElement(sub_3);
        // Sample in inner loop
        sub_2.addTestElement(new TestSampler("seven"));
        controller.addTestElement(sub_2);

        // Compute the expected sample names
        String[] onlyOnceOrder = new String[] { "one", "two" };
        String[] order = new String[] { "three", "four", "five", "six", "seven", "four", "five", "six",
                "seven", "four", "five", "six", "seven" };
        // Outer only once + ("three" + ("four" + "five" + "six" + "seven") * innerLoopCount) * outerLoopCount;
        int expectedNoSamples = 2 + (1 + (3 + 1) * innerLoopCount) * outerLoopCount;
        String[] expectedSamples = new String[expectedNoSamples];
        // The only once samples
        System.arraycopy(onlyOnceOrder, 0, expectedSamples, 0, onlyOnceOrder.length);
        // The outer sample and the inner loop samples
        final int onceOnlySamples = onlyOnceOrder.length;
        for(int i = 0; i < order.length * outerLoopCount; i++) {
            expectedSamples[onceOnlySamples + i] = order[i % order.length];
        }

        // Execute the test pan
        controller.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        controller.initialize();

        int counter = 0;
        TestElement sampler = null;
        while ((sampler = controller.next()) != null) {
            assertEquals(expectedSamples[counter], sampler.getPropertyAsString(TestElement.NAME));

            counter++;
        }
        assertEquals(expectedNoSamples, counter);
    }

    @Test
    public void testInsideInnerLoop() throws Exception {
        // Test plan with OnlyOnceController inside inner loop
        // Set up the test plan
        LoopController controller = new LoopController();
        final int outerLoopCount = 4;
        controller.setLoops(outerLoopCount);
        // OnlyOnce samples
        OnceOnlyController sub_1 = new OnceOnlyController();
        sub_1.addTestElement(new TestSampler("one"));
        sub_1.addTestElement(new TestSampler("two"));
        controller.addTestElement(sub_1);
        controller.addIterationListener(sub_1);
        // Outer sample
        controller.addTestElement(new TestSampler("three"));
        // Inner loop
        LoopController sub_2 = new LoopController();
        final int innerLoopCount = 3;
        sub_2.setLoops(innerLoopCount);
        // Sample in inner loop
        sub_2.addTestElement(new TestSampler("four"));
        // OnlyOnce inside inner loop
        OnceOnlyController sub_3 = new OnceOnlyController();
        sub_3.addTestElement(new TestSampler("five"));
        sub_3.addTestElement(new TestSampler("six"));
        sub_2.addTestElement(sub_3);
        sub_2.addIterationListener(sub_3);
        // Sample in inner loop
        sub_2.addTestElement(new TestSampler("seven"));
        controller.addTestElement(sub_2);

        // Compute the expected sample names
        String[] onlyOnceOrder = new String[] { "one", "two" };
        String[] order = new String[] { "three", "four", "five", "six", "seven", "four", "seven", "four", "seven" };
        // Outer only once + ("three" + "only once five and six" + ("four" + "seven") * innerLoopCount) * outerLoopCount;
        int expectedNoSamples = 2 + (1 + 2 + (1 + 1) * innerLoopCount) * outerLoopCount;
        String[] expectedSamples = new String[expectedNoSamples];
        // The only once samples
        System.arraycopy(onlyOnceOrder, 0, expectedSamples, 0, onlyOnceOrder.length);

        // The outer sample and the inner loop samples
        final int onceOnlySamples = onlyOnceOrder.length;
        for(int i = 0; i < order.length * outerLoopCount; i++) {
            expectedSamples[onceOnlySamples + i] = order[i % order.length];
        }

        // Execute the test pan
        controller.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        controller.initialize();

        int counter = 0;
        TestElement sampler = null;
        while ((sampler = controller.next()) != null) {
            assertEquals(expectedSamples[counter], sampler.getPropertyAsString(TestElement.NAME));

            counter++;
        }
        assertEquals(expectedNoSamples, counter);
    }

}
