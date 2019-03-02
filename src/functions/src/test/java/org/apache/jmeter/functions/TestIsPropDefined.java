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
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Test {@link IsPropDefined} Function
 *
 * @see IsPropDefined
 *
 */
public class TestIsPropDefined extends JMeterTestCase {
    protected AbstractFunction isPropDefined;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        isPropDefined = new IsPropDefined();
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "dummy data";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
    }

    @Test
    public void testParameterCountIsPropDefined() throws Exception {
        checkInvalidParameterCounts(isPropDefined, 1, 1);
    }

    @Test
    public void testIsPropDefined() throws Exception {
        params.add(new CompoundVariable("file.encoding"));
        isPropDefined.setParameters(params);
        String returnValue = isPropDefined.execute(result, null);
        assertEquals("true", returnValue);
    }

    @Test
    public void testIsPropNotDefined() throws Exception {
        params.add(new CompoundVariable("emptyProperty"));
        isPropDefined.setParameters(params);
        String returnValue = isPropDefined.execute(result, null);
        assertEquals("false", returnValue);
    }

    @Test
    public void testIsPropNotDefinedOnlyVarDefined() throws Exception {
        vars.put("emptyProperty", "emptyPropertyValue");
        params.add(new CompoundVariable("emptyProperty"));
        isPropDefined.setParameters(params);
        String returnValue = isPropDefined.execute(result, null);
        assertEquals("false", returnValue);
    }

    @Test(expected = InvalidVariableException.class)
    public void testIsPropDefinedError() throws Exception {
        isPropDefined.setParameters(params);
        isPropDefined.execute(result, null);
    }

}
