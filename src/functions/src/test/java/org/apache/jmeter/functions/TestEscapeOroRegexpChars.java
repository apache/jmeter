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

public class TestEscapeOroRegexpChars extends JMeterTestCase {
    protected AbstractFunction function;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        function = new EscapeOroRegexpChars();
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
        checkInvalidParameterCounts(function, 1, 2);
    }

    @Test
    public void testNOEscape() throws Exception {
        params.add(new CompoundVariable("toto1titi"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("toto1titi", ret);
    }

    @Test
    public void testEscapeSpace() throws Exception {
        params.add(new CompoundVariable("toto1 titi"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("toto1\\ titi", ret);
    }

    @Test
    public void testEscape() throws Exception {
        params.add(new CompoundVariable("toto(.+?)titi"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("toto\\(\\.\\+\\?\\)titi", ret);
    }

    @Test
    public void testEscapeWithVars() throws Exception {
        params.add(new CompoundVariable("toto(.+?)titi"));
        params.add(new CompoundVariable("exportedVar"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("toto\\(\\.\\+\\?\\)titi", ret);
        assertEquals("toto\\(\\.\\+\\?\\)titi", vars.get("exportedVar"));
    }

    @Test
    public void testEscape2() throws Exception {
        params.add(new CompoundVariable("[^\"].+?"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("\\[\\^\\\"\\]\\.\\+\\?", ret);
    }
}
