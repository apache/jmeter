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

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testkit.BugId;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestIfController extends JMeterTestCase {

    @BugId("56160")
    @Test
    void testStackOverflow() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(1);
        controller.setContinueForever(false);

        IfController ifCont = new IfController("true==false");
        ifCont.setUseExpression(false);
        ifCont.setEvaluateAll(false);
        WhileController whileController = new WhileController();
        whileController.setCondition("${__javaScript(\"true\" != \"false\")}");
        whileController.addTestElement(new TestSampler("Sample1"));

        controller.addTestElement(ifCont);
        ifCont.addTestElement(whileController);

        Sampler sampler = null;
        int counter = 0;
        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);
        whileController.setRunningVersion(true);

        try {
            while ((sampler = controller.next()) != null) {
                sampler.sample(null);
                counter++;
            }
            Assertions.assertEquals(0, counter);
        } catch (StackOverflowError e) {
            throw new AssertionError("Stackoverflow occurred in testStackOverflow", e);
        }
    }

    @BugId("53768")
    @Test
    void testBug53768() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(1);
        controller.setContinueForever(false);

        Arguments arguments = new Arguments();
        arguments.addArgument("VAR1", "0", "=");

        DebugSampler debugSampler1 = new DebugSampler();
        debugSampler1.setName("VAR1 = ${VAR1}");

        IfController ifCont = new IfController("true==false");
        ifCont.setUseExpression(false);
        ifCont.setEvaluateAll(false);

        IfController ifCont2 = new IfController("true==true");
        ifCont2.setUseExpression(false);
        ifCont2.setEvaluateAll(false);

        CounterConfig counterConfig = new CounterConfig();
        counterConfig.setStart(1);
        counterConfig.setIncrement(1);
        counterConfig.setVarName("VAR1");

        DebugSampler debugSampler2 = new DebugSampler();
        debugSampler2.setName("VAR1 = ${VAR1}");

        controller.addTestElement(arguments);
        controller.addTestElement(debugSampler1);
        controller.addTestElement(ifCont);
        ifCont.addTestElement(ifCont2);
        ifCont2.addTestElement(counterConfig);
        controller.addTestElement(debugSampler2);

        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);
        ifCont2.setRunningVersion(true);
        counterConfig.setRunningVersion(true);
        arguments.setRunningVersion(true);
        debugSampler1.setRunningVersion(true);
        debugSampler2.setRunningVersion(true);
        ifCont2.addIterationListener(counterConfig);
        JMeterVariables vars = new JMeterVariables();
        JMeterContext jmctx = JMeterContextService.getContext();

        jmctx.setVariables(vars);
        vars.put("VAR1", "0");
        try {

            Sampler sampler = controller.next();
            sampler.sample(null);
            Assertions.assertEquals("0", vars.get("VAR1"));
            sampler = controller.next();
            sampler.sample(null);
            Assertions.assertEquals("0", vars.get("VAR1"));

        } catch (StackOverflowError e) {
            throw new AssertionError("Stackoverflow occurred in testStackOverflow", e);
        }
    }

    @Test
    @Disabled
    void testProcessing() throws Exception {

        GenericController controller = new GenericController();

        controller.addTestElement(new IfController("false==false"));
        controller.addTestElement(new IfController(" \"a\".equals(\"a\")"));
        controller.addTestElement(new IfController("2<100"));

        // TODO enable some proper tests!!

    }

    @Test
    void testProcessingTrue() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(2);
        controller.addTestElement(new TestSampler("Sample1"));
        IfController ifCont = new IfController("true==true");
        ifCont.setEvaluateAll(true);
        ifCont.addTestElement(new TestSampler("Sample2"));
        TestSampler sample3 = new TestSampler("Sample3");
        ifCont.addTestElement(sample3);
        controller.addTestElement(ifCont);

        String[] order = new String[] { "Sample1", "Sample2", "Sample3", "Sample1", "Sample2", "Sample3" };
        int counter = 0;
        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);

        Sampler sampler = null;
        while ((sampler = controller.next()) != null) {
            sampler.sample(null);
            Assertions.assertEquals(order[counter], sampler.getName());
            counter++;
        }
        Assertions.assertEquals(counter, 6);
    }

    @Test
    void testProcessingTrueWithExpression() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(2);
        controller.addTestElement(new TestSampler("Sample1"));
        IfController ifCont = new IfController("true");
        ifCont.setUseExpression(true);
        ifCont.setEvaluateAll(false);
        ifCont.addTestElement(new TestSampler("Sample2"));
        TestSampler sample3 = new TestSampler("Sample3");
        ifCont.addTestElement(sample3);
        controller.addTestElement(ifCont);

        String[] order = new String[] { "Sample1", "Sample2", "Sample3", "Sample1", "Sample2", "Sample3" };
        int counter = 0;
        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);

        Sampler sampler = null;
        while ((sampler = controller.next()) != null) {
            sampler.sample(null);
            Assertions.assertEquals(order[counter], sampler.getName());
            counter++;
        }
        Assertions.assertEquals(counter, 6);
    }


    /**
     * Test false return on sample3 (sample4 doesn't execute)
     */
    @Test
    void testEvaluateAllChildrenWithoutSubController() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(2);
        controller.addTestElement(new TestSampler("Sample1"));
        IfController ifCont = new IfController("true==true");
        ifCont.setEvaluateAll(true);
        controller.addTestElement(ifCont);

        ifCont.addTestElement(new TestSampler("Sample2"));
        TestSampler sample3 = new TestSampler("Sample3");
        ifCont.addTestElement(sample3);
        TestSampler sample4 = new TestSampler("Sample4");
        ifCont.addTestElement(sample4);

        String[] order = new String[] { "Sample1", "Sample2", "Sample3", "Sample1", "Sample2", "Sample3" };
        int counter = 0;
        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);

        Sampler sampler = null;
        while ((sampler = controller.next()) != null) {
            sampler.sample(null);
            if (sampler.getName().equals("Sample3")) {
                ifCont.setCondition("true==false");
            }
            Assertions.assertEquals(order[counter], sampler.getName());
            counter++;
        }
        Assertions.assertEquals(counter, 6);
    }

    /**
     * test 2 loops with a sub generic controller (sample4 doesn't execute)
     */
    @Test
    void testEvaluateAllChildrenWithSubController() throws Exception {
        LoopController controller = new LoopController();
        controller.setLoops(2);
        controller.addTestElement(new TestSampler("Sample1"));
        IfController ifCont = new IfController("true==true");
        ifCont.setEvaluateAll(true);
        controller.addTestElement(ifCont);
        ifCont.addTestElement(new TestSampler("Sample2"));

        GenericController genericCont = new GenericController();
        TestSampler sample3 = new TestSampler("Sample3");
        genericCont.addTestElement(sample3);
        TestSampler sample4 = new TestSampler("Sample4");
        genericCont.addTestElement(sample4);
        ifCont.addTestElement(genericCont);

        String[] order = new String[] { "Sample1", "Sample2", "Sample3", "Sample1", "Sample2", "Sample3" };
        int counter = 0;
        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);
        genericCont.setRunningVersion(true);

        Sampler sampler = null;
        while ((sampler = controller.next()) != null) {
            sampler.sample(null);
            if (sampler.getName().equals("Sample3")) {
                ifCont.setCondition("true==false");
            }
            Assertions.assertEquals(order[counter], sampler.getName());
            counter++;
        }
        Assertions.assertEquals(counter, 6);
    }

    @Test
    void shouldIgnoreEmptyLastLine() {
        GenericController controller = new GenericController();

        IfController ifCont = new IfController("true\n");
        ifCont.setUseExpression(true);
        ifCont.setEvaluateAll(false);

        ifCont.addTestElement(new TestSampler("sample1"));
        controller.addTestElement(ifCont);

        controller.initialize();
        controller.setRunningVersion(true);
        ifCont.setRunningVersion(true);

        Sampler sampler = controller.next();
        Assertions.assertNotNull(sampler);
    }
}
