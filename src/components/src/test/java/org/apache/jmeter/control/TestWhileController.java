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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestWhileController extends JMeterTestCase {

        private JMeterContext jmctx;
        private JMeterVariables jmvars;

        @Before
        public void setUp() {
            jmctx = JMeterContextService.getContext();
            jmctx.setVariables(new JMeterVariables());
            jmvars = jmctx.getVariables();
        }

        private void setLastSampleStatus(boolean status){
            jmvars.put(JMeterThread.LAST_SAMPLE_OK,Boolean.toString(status));
        }

        private void setRunning(TestElement el){
            PropertyIterator pi = el.propertyIterator();
            while(pi.hasNext()){
                pi.next().setRunningVersion(true);
            }
        }

        // Get next sample and its name
        private String nextName(GenericController c) {
            Sampler s = c.next();
            if (s == null) {
                return null;
            }
            return s.getName();
        }

        // While (blank), previous sample OK - should loop until false
        @Test
        public void testBlankPrevOK() throws Exception {
            runtestPrevOK("");
        }

        // While (LAST), previous sample OK - should loop until false
        @Test
        public void testLastPrevOK() throws Exception {
            runtestPrevOK("LAST");
        }

        private static final String OTHER = "X"; // Dummy for testing functions

        // While (LAST), previous sample OK - should loop until false
        @Test
        public void testOtherPrevOK() throws Exception {
            runtestPrevOK(OTHER);
        }

        private void runtestPrevOK(String type) throws Exception {
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
            setLastSampleStatus(true);
            while_cont.setCondition(type);
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            while_cont.addTestElement(new TestSampler("three"));
            controller.addTestElement(while_cont);
            controller.addTestElement(new TestSampler("four"));
            controller.initialize();
            assertEquals("one", nextName(controller));
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            assertEquals("one", nextName(controller));
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            assertEquals("one", nextName(controller));
            setLastSampleStatus(false);
            if (type.equals(OTHER)){
                while_cont.setCondition("false");
            }
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            setLastSampleStatus(true);
            if (type.equals(OTHER)) {
                while_cont.setCondition(OTHER);
            }
            assertEquals("one", nextName(controller));
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            setLastSampleStatus(false);
            if (type.equals(OTHER)) {
                while_cont.setCondition("false");
            }
            assertEquals("four", nextName(controller));
            assertNull(nextName(controller));
            setLastSampleStatus(true);
            if (type.equals(OTHER)) {
                while_cont.setCondition(OTHER);
            }
            assertEquals("one", nextName(controller));
        }

        // While (blank), previous sample failed - should run once
        @Test
        public void testBlankPrevFailed() throws Exception {
            GenericController controller = new GenericController();
            controller.setRunningVersion(true);
            WhileController while_cont = new WhileController();
            setLastSampleStatus(false);
            while_cont.setCondition("");
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);
            controller.addTestElement(new TestSampler("three"));
            controller.initialize();
            assertEquals("one", nextName(controller));
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            assertNull(nextName(controller));
            // Run entire test again
            assertEquals("one", nextName(controller));
            assertEquals("two", nextName(controller));
            assertEquals("three", nextName(controller));
            assertNull(nextName(controller));
        }

        /*
         * Generic Controller
         * - before
         * - While Controller ${VAR}
         * - - one
         * - - two
         * - - Simple Controller
         * - - - three
         * - - - four
         * - after
         */
        @Test
        public void testVariable1() throws Exception {
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
            setLastSampleStatus(false);
            while_cont.setCondition("${VAR}");
            jmvars.put("VAR", "");
            ValueReplacer vr = new ValueReplacer();
            vr.replaceValues(while_cont);
            setRunning(while_cont);
            controller.addTestElement(new TestSampler("before"));
            controller.addTestElement(while_cont);
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            GenericController simple = new GenericController();
            while_cont.addTestElement(simple);
            simple.addTestElement(new TestSampler("three"));
            simple.addTestElement(new TestSampler("four"));
            controller.addTestElement(new TestSampler("after"));
            controller.initialize();
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                assertEquals("Loop: "+i,"one", nextName(controller));
                assertEquals("Loop: "+i,"two", nextName(controller));
                assertEquals("Loop: "+i,"three", nextName(controller));
                assertEquals("Loop: "+i,"four", nextName(controller));
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
            jmvars.put("VAR", "LAST"); // Should not enter the loop
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
            jmvars.put("VAR", "");
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                if (i==1) {
                    assertEquals("Loop: "+i,"one", nextName(controller));
                    assertEquals("Loop: "+i,"two", nextName(controller));
                    assertEquals("Loop: "+i,"three", nextName(controller));
                    jmvars.put("VAR", "LAST"); // Should not enter the loop next time
                    assertEquals("Loop: "+i,"four", nextName(controller));
                }
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
        }

        // Test with SimpleController as first item
        @Test
        public void testVariable2() throws Exception {
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
            setLastSampleStatus(false);
            while_cont.setCondition("${VAR}");
            jmvars.put("VAR", "");
            ValueReplacer vr = new ValueReplacer();
            vr.replaceValues(while_cont);
            setRunning(while_cont);
            controller.addTestElement(new TestSampler("before"));
            controller.addTestElement(while_cont);
            GenericController simple = new GenericController();
            while_cont.addTestElement(simple);
            simple.addTestElement(new TestSampler("one"));
            simple.addTestElement(new TestSampler("two"));
            while_cont.addTestElement(new TestSampler("three"));
            while_cont.addTestElement(new TestSampler("four"));
            controller.addTestElement(new TestSampler("after"));
            controller.initialize();
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                assertEquals("Loop: "+i,"one", nextName(controller));
                assertEquals("Loop: "+i,"two", nextName(controller));
                assertEquals("Loop: "+i,"three", nextName(controller));
                assertEquals("Loop: "+i,"four", nextName(controller));
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
            jmvars.put("VAR", "LAST"); // Should not enter the loop
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
            jmvars.put("VAR", "");
            for (int i = 1; i <= 3; i++) {
                assertEquals("Loop: "+i,"before", nextName(controller));
                if (i==1){
                    assertEquals("Loop: "+i,"one", nextName(controller));
                    assertEquals("Loop: "+i,"two", nextName(controller));
                    jmvars.put("VAR", "LAST"); // Should not enter the loop next time
                    // But should continue to the end of the loop
                    assertEquals("Loop: "+i,"three", nextName(controller));
                    assertEquals("Loop: "+i,"four", nextName(controller));
                }
                assertEquals("Loop: "+i,"after", nextName(controller));
                assertNull("Loop: "+i,nextName(controller));
            }
        }

        // While LAST, previous sample failed - should not run
        @Test
        public void testLASTPrevFailed() throws Exception {
            runTestPrevFailed("LAST");
        }

        // While False, previous sample failed - should not run
        @Test
        public void testfalsePrevFailed() throws Exception {
            runTestPrevFailed("False");
        }

        private void runTestPrevFailed(String s) throws Exception {
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
            setLastSampleStatus(false);
            while_cont.setCondition(s);
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);
            controller.addTestElement(new TestSampler("three"));
            controller.initialize();
            assertEquals("three", nextName(controller));
            assertNull(nextName(controller));
            assertEquals("three", nextName(controller));
            assertNull(nextName(controller));
        }

        @Test
        public void testLastFailedBlank() throws Exception{
            runTestLastFailed("");
        }

        @Test
        public void testLastFailedLast() throws Exception{
            runTestLastFailed("LAST");
        }

        // Should behave the same for blank and LAST because success on input
        private void runTestLastFailed(String s) throws Exception {
            GenericController controller = new GenericController();
            controller.addTestElement(new TestSampler("1"));
            WhileController while_cont = new WhileController();
            controller.addTestElement(while_cont);
            while_cont.setCondition(s);
            GenericController sub = new GenericController();
            while_cont.addTestElement(sub);
            sub.addTestElement(new TestSampler("2"));
            sub.addTestElement(new TestSampler("3"));

            controller.addTestElement(new TestSampler("4"));

            setLastSampleStatus(true);
            controller.initialize();
            assertEquals("1", nextName(controller));
            assertEquals("2", nextName(controller));
            setLastSampleStatus(false);
            assertEquals("3", nextName(controller));
            assertEquals("4", nextName(controller));
            assertNull(nextName(controller));
        }

        // Tests for Stack Overflow (bug 33954)
        @Test
        public void testAlwaysFailOK() throws Exception {
            runTestAlwaysFail(true); // Should be OK
        }

        @Test
        public void testAlwaysFailBAD() throws Exception {
            runTestAlwaysFail(false);
        }

        private void runTestAlwaysFail(boolean other) {
            LoopController controller = new LoopController();
            controller.setContinueForever(true);
            controller.setLoops(-1);
            WhileController while_cont = new WhileController();
            setLastSampleStatus(false);
            while_cont.setCondition("false");
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);
            if (other) {
                controller.addTestElement(new TestSampler("three"));
            }
            controller.initialize();
            try {
                if (other) {
                    assertEquals("three", nextName(controller));
                } else {
                    assertNull(nextName(controller));
                }
            } catch (StackOverflowError e) {
                fail(e.toString());
            }
        }
}
