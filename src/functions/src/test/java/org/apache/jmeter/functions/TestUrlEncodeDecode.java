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

public class TestUrlEncodeDecode extends JMeterTestCase {
    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
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
        AbstractFunction function = new UrlEncode();
        checkInvalidParameterCounts(function, 1, 1);

        function = new UrlDecode();
        checkInvalidParameterCounts(function, 1, 1);
    }

    @Test
    public void testUrlEncode() throws Exception {
        AbstractFunction function = new UrlEncode();
        params.add(new CompoundVariable("Veni, vidi, vici ?"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("Veni%2C+vidi%2C+vici+%3F", returnValue);
    }


    @Test
    public void testUrlDecode() throws Exception {
        AbstractFunction function = new UrlDecode();
        params.add(new CompoundVariable("Veni%2C+vidi%2C+vici+%3F"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("Veni, vidi, vici ?", returnValue);
    }
}
