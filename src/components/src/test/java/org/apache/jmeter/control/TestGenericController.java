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

public class TestGenericController extends JMeterTestCase {

        @Test
        public void testProcessing() throws Exception {
            testLog.debug("Testing Generic Controller");
            GenericController controller = new GenericController();
            GenericController sub_1 = new GenericController();
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
            controller.addTestElement(sub_1);
            controller.addTestElement(new TestSampler("three"));
            GenericController sub_2 = new GenericController();
            GenericController sub_3 = new GenericController();
            sub_2.addTestElement(new TestSampler("four"));
            sub_3.addTestElement(new TestSampler("five"));
            sub_3.addTestElement(new TestSampler("six"));
            sub_2.addTestElement(sub_3);
            sub_2.addTestElement(new TestSampler("seven"));
            controller.addTestElement(sub_2);
            String[] order = new String[] { "one", "two", "three", "four", "five", "six", "seven" };
            int counter = 7;
            controller.initialize();
            for (int i = 0; i < 2; i++) {
                assertEquals(7, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null) {
                    assertEquals(order[counter++], sampler.getName());
                }
            }
        }
}
