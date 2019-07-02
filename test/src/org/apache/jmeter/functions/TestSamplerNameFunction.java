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

package org.apache.jmeter.functions;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestSamplerNameFunction extends JMeterTestCase {
    private Function variable;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx = null;

    private String value;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
        result = new SampleResult();
        variable = new SamplerName();
    }

    @Test
    public void testSamplerName() throws Exception {
        variable.setParameters(params);
        TestSampler sampler = new TestSampler("UnitTestSampler");
        value = variable.execute(result, sampler);
        assertEquals("UnitTestSampler", value);
    }

    @Test
    public void testSamplerNameWithVar() throws Exception {
        variable.setParameters(params);
        TestSampler sampler = new TestSampler("UnitTestSampler");
        variable.setParameters(FunctionTestHelper.makeParams("var1", null, null));
        value = variable.execute(result, sampler);

        assertEquals("UnitTestSampler", value);
        assertEquals("UnitTestSampler", vars.get("var1"));
    }
}
