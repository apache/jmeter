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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFileToString extends JMeterTestCase {
    protected AbstractFunction function;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        function = new FileToString();
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
        checkInvalidParameterCounts(function, 1, 3);
    }

    @Test
    public void testReadError() throws Exception {
        params.add(new CompoundVariable("nofile"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("**ERR**", returnValue);
    }

    @Test
    public void testRead() throws Exception {
        File file = new File(JMeterUtils.getJMeterBinDir(), "jmeter.properties");
        params.add(new CompoundVariable(file.getAbsolutePath()));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue(returnValue.indexOf("language=")>0);
    }

    @Test
    public void testReadWithEncoding() throws Exception {
        File file = new File(JMeterUtils.getJMeterBinDir(), "jmeter.properties");
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable("UTF-8"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue(returnValue.indexOf("language=")>0);
    }

    @Test
    public void testReadWithEncodingAndVar() throws Exception {
        File file = new File(JMeterUtils.getJMeterBinDir(), "jmeter.properties");
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable("UTF-8"));
        params.add(new CompoundVariable("MY_FILE_AS_TEXT"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue(returnValue.indexOf("language=")>0);
        Assert.assertTrue(vars.get("MY_FILE_AS_TEXT").indexOf("language=")>0);
    }
}
