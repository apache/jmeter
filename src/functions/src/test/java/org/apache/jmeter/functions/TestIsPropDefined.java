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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestIsPropDefined extends JMeterTestCase {

    private AbstractFunction isPropDefined;
    private SampleResult result;
    private Collection<CompoundVariable> params;
    private JMeterVariables vars;
    private JMeterContext jmctx;

    @BeforeEach
    public void setUp() {
        isPropDefined = new IsPropDefined();
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "dummy data";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new ArrayList<>();
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

    @Test
    public void testIsPropDefinedError() {
        Assertions.assertThrows(
                InvalidVariableException.class,
                () -> isPropDefined.setParameters(params));
    }

}
