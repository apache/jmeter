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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSetProperty extends JMeterTestCase implements JMeterSerialTest {
    protected AbstractFunction function;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        function = new SetProperty();
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
    }

    @Test
    public void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 2, 3);
    }

    @Test
    public void testSetPropertyNoReturn() throws Exception {
        params.add(new CompoundVariable("prop1"));
        params.add(new CompoundVariable("value1"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("value1", JMeterUtils.getProperty("prop1"));
        assertEquals("", returnValue);
    }

    @Test
    public void testSetPropertyWithReturn() throws Exception {
        params.add(new CompoundVariable("prop1"));
        params.add(new CompoundVariable("value1"));
        params.add(new CompoundVariable("true"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("value1", JMeterUtils.getProperty("prop1"));
        Assert.assertNull(returnValue);

        params.clear();
        params.add(new CompoundVariable("prop1"));
        params.add(new CompoundVariable("value2"));
        params.add(new CompoundVariable("true"));
        function.setParameters(params);
        returnValue = function.execute(result, null);
        assertEquals("value2", JMeterUtils.getProperty("prop1"));
        assertEquals("value1", returnValue);
    }

}
