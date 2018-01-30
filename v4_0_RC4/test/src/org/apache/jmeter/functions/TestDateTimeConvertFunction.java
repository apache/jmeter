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
 * Test {@link DateTimeConvertFunction}
 * 
 * @since 4.0
 */
public class TestDateTimeConvertFunction extends JMeterTestCase {

    protected AbstractFunction dateConvert;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        dateConvert = new DateTimeConvertFunction();
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
    public void testParameterCount512() throws Exception {
        checkInvalidParameterCounts(dateConvert, 3, 4);
    }

    @Test
    public void testDateTimeConvert() throws Exception {
        params.add(new CompoundVariable("2017-01-02 21:00:21"));
        params.add(new CompoundVariable("yyyy-MM-dd HH:mm:ss"));
        params.add(new CompoundVariable("dd-MM-yyyy hh:mm"));
        dateConvert.setParameters(params);
        String returnValue = dateConvert.execute(result, null);
        assertEquals("02-01-2017 09:00", returnValue);
    }
    
    @Test
    public void testDateConvert() throws Exception {
        params.add(new CompoundVariable("2017-01-02"));
        params.add(new CompoundVariable("yyyy-MM-dd"));
        params.add(new CompoundVariable("dd-MM-yyyy"));
        dateConvert.setParameters(params);
        String returnValue = dateConvert.execute(result, null);
        assertEquals("02-01-2017", returnValue);
    }

    @Test
    public void testDateConvertWithVariable() throws Exception {
        params.add(new CompoundVariable("2017-01-02"));
        params.add(new CompoundVariable("yyyy-MM-dd"));
        params.add(new CompoundVariable("dd-MM-yyyy"));
        params.add(new CompoundVariable("varName"));
        dateConvert.setParameters(params);
        dateConvert.execute(result, null);
        assertEquals("02-01-2017", vars.get("varName"));
    }

    @Test(expected = InvalidVariableException.class)
    public void testDateConvertError() throws Exception {
        params.add(new CompoundVariable("2017-01-02"));
        params.add(new CompoundVariable("yyyy-MM-dd"));
        dateConvert.setParameters(params);
        dateConvert.execute(result, null);
    }

    @Test
    public void testDateConvertErrorFormat() throws Exception {
        params.add(new CompoundVariable("2017-01-02"));
        params.add(new CompoundVariable("yyyy-MM-dd"));
        params.add(new CompoundVariable("abcd"));
        dateConvert.setParameters(params);
        assertEquals(dateConvert.execute(result, null), "");
    }
    
    @Test
    public void testDateConvertDateError() throws Exception {
        params.add(new CompoundVariable("a2017-01-02"));
        params.add(new CompoundVariable("yyyy-MM-dd"));
        params.add(new CompoundVariable("dd-MM-yyyy HH:mm:ss"));
        dateConvert.setParameters(params);
        assertEquals(dateConvert.execute(result, null), "");
    }
}
