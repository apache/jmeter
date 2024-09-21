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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.engine.util.ReplaceStringWithFunctions;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.Test;

public class TestLoopController extends JMeterTestCase {

    @Test
    public void testProcessing() throws Exception {
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables variables = new JMeterVariables();
        jmctx.setVariables(variables);

        GenericController controller = new GenericController();
        GenericController sub_1 = new GenericController();
        sub_1.addTestElement(new TestSampler("one"));
        sub_1.addTestElement(new TestSampler("two"));
        controller.addTestElement(sub_1);
        controller.addTestElement(new TestSampler("three"));
        LoopController sub_2 = new LoopController();
        String lcName = "LC";
        sub_2.setName(lcName);
        sub_2.setLoops(3);
        GenericController sub_3 = new GenericController();
        sub_2.addTestElement(new TestSampler("four"));
        sub_3.addTestElement(new TestSampler("five"));
        sub_3.addTestElement(new TestSampler("six"));
        sub_2.addTestElement(sub_3);
        sub_2.addTestElement(new TestSampler("seven"));
        controller.addTestElement(sub_2);
        String[] order = new String[]{"one", "two", "three", "four", "five", "six", "seven", "four", "five",
                "six", "seven", "four", "five", "six", "seven"};
        int counter = 15;
        controller.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        controller.initialize();
        int loopControl = 0;
        for (int i = 0; i < 2; i++) {
            loopControl = 0;
            assertEquals(15, counter);
            counter = 0;
            TestElement sampler = null;
            while ((sampler = controller.next()) != null) {
                assertEquals(order[counter++], sampler.getName());
                if ("four".equals(sampler.getName())) {
                    Integer value = (Integer) variables.getObject(
                            JMeterUtils.formatJMeterExportedVariableName(lcName + LoopController.INDEX_VAR_NAME_SUFFIX));
                    assertNotNull(value);
                    assertEquals(Integer.valueOf(loopControl++), value);
                }
            }
        }
    }

    @Test
    public void testLoopZeroTimes() throws Exception {
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables variables = new JMeterVariables();
        jmctx.setVariables(variables);

        LoopController loop = new LoopController();
        String lcName = "LC";
        loop.setName(lcName);
        loop.setLoops(0);
        loop.addTestElement(new TestSampler("never run"));
        loop.initialize();
        assertNull(loop.next());
        Integer value = (Integer) variables.getObject(
                JMeterUtils.formatJMeterExportedVariableName(lcName + LoopController.INDEX_VAR_NAME_SUFFIX));
        assertNotNull(value);
        assertEquals(Integer.valueOf(0), value);
    }

    @Test
    public void testInfiniteLoop() throws Exception {
        JMeterContext jmctx = JMeterContextService.getContext();
        LoopController loop = new LoopController();
        String lcName = "LC";
        loop.setName(lcName);
        loop.setLoops(LoopController.INFINITE_LOOP_COUNT);
        loop.addTestElement(new TestSampler("never run"));
        JMeterVariables variables = new JMeterVariables();
        jmctx.setVariables(variables);
        loop.setRunningVersion(true);
        loop.initialize();
        for (int i = 0; i < 42; i++) {
            assertNotNull(loop.next());
            Integer value = (Integer) variables.getObject(
                    JMeterUtils.formatJMeterExportedVariableName(lcName + LoopController.INDEX_VAR_NAME_SUFFIX));
            assertNotNull(value);
            assertEquals(Integer.valueOf(i), value);
        }
    }

    @Test
    public void testBug54467() throws Exception {
        JMeterContext jmctx = JMeterContextService.getContext();
        String lcName = "LC";
        LoopController loop = new LoopController();
        loop.setName(lcName);
        Map<String, String> variables = new HashMap<>();
        ReplaceStringWithFunctions transformer = new ReplaceStringWithFunctions(new CompoundVariable(), variables);
        jmctx.setVariables(new JMeterVariables());

        StringProperty prop =
                new StringProperty(LoopControllerSchema.INSTANCE.getLoops().getName(), "${__Random(1,12,)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);

        loop.setProperty(newProp);
        loop.addTestElement(new TestSampler("random run"));
        loop.setRunningVersion(true);
        loop.initialize();
        int loops = loop.getLoops();
        for (int i = 0; i < loops; i++) {
            Sampler s = loop.next();
            assertNotNull(s);
            Integer value = (Integer) jmctx.getVariables().getObject(
                    JMeterUtils.formatJMeterExportedVariableName(lcName + LoopController.INDEX_VAR_NAME_SUFFIX));
            assertNotNull(value);
            assertEquals(Integer.valueOf(i), value);
        }
        assertNull(loop.next());
    }
}
