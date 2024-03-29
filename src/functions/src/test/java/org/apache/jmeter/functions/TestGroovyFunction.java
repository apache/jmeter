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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestGroovyFunction extends JMeterTestCase implements JMeterSerialTest {

    private AbstractFunction function;
    private SampleResult result;
    private Collection<CompoundVariable> params;
    private JMeterVariables vars;
    private JMeterContext jmctx;

    @AfterEach
    public void tearDown() {
        JMeterUtils.getJMeterProperties().remove("groovy.utilities");
    }

    @BeforeEach
    public void setUp() {
        function = new Groovy();
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new ArrayList<>();
    }

    @Test
    public void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 1, 2);
    }

    @Test
    public void testSum() throws Exception {
        params.add(new CompoundVariable("1+2+3"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("6", ret);
    }

    @Test
    public void testSumVar() throws Exception {
        params.add(new CompoundVariable("1+2+3"));
        params.add(new CompoundVariable("TOTAL"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("6", ret);
        assertEquals("6", vars.get("TOTAL"));
    }

    @Test
    public void testReplace1() throws Exception {
        params.add(new CompoundVariable(
                "prev.getResponseDataAsString().replaceAll('T','t')"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("the quick brown fox", ret);
    }

    @Test
    public void testReplace2() throws Exception {
        vars.put("URL", "/query.cgi?s1=1&amp;s2=2&amp;s3=3");
        params.add(new CompoundVariable("vars.get('URL').replaceAll('&amp;','&')"));
        params.add(new CompoundVariable("URL"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("/query.cgi?s1=1&s2=2&s3=3", ret);
        assertEquals(ret, vars.getObject("URL"));
    }

    @Test
    public void testFileLoading() throws Exception {
        JMeterUtils.setProperty("groovy.utilities", "bin/utility.groovy");
        params.add(new CompoundVariable("factorial(10)"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("3628800", ret);
    }

    @Test
    public void testInvalidFileLoading() throws Exception {
        JMeterUtils.setProperty("groovy.utilities", "bin/missing.groovy");
        params.add(new CompoundVariable("factorial(10)"));
        Assertions.assertThrows(
                InvalidVariableException.class,
                () -> function.setParameters(params)
        );
    }
}
