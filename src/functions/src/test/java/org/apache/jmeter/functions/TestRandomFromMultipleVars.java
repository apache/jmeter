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
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRandomFromMultipleVars extends JMeterTestCase {
    private SampleResult result;

    private AbstractFunction function;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        function = new RandomFromMultipleVars();
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
    public void testExtractionFromMultipleVars() throws Exception {
        String existingVarName1 = "var1";
        String existingVarName2 = "var2";
        vars.put(existingVarName1+"_matchNr", "1");
        vars.put(existingVarName1+"_1", "var1_value");

        vars.put(existingVarName2+"_matchNr", "2");
        vars.put(existingVarName2+"_1", "var2_value1");
        vars.put(existingVarName2+"_2", "var2_value2");

        params.add(new CompoundVariable("var1|var2"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertThat(returnValue,
                CoreMatchers.anyOf(CoreMatchers.is("var1_value"),
                        CoreMatchers.is("var2_value1"),
                        CoreMatchers.is("var2_value2")));
        Assert.assertNull(vars.get("outputVar"));
    }

    @Test
    public void test1Extraction() throws Exception {
        String existingVarName = "var1";
        vars.put(existingVarName+"_matchNr", "1");
        vars.put(existingVarName+"_1", "value1");
        params.add(new CompoundVariable("var1"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("value1", returnValue);
        Assert.assertNull(vars.get("outputVar"));
    }

    @Test
    public void test1ExtractionWithOutputVar() throws Exception {
        String existingVarName = "var1";
        vars.put(existingVarName+"_matchNr", "1");
        vars.put(existingVarName+"_1", "value1");
        params.add(new CompoundVariable("var1"));
        params.add(new CompoundVariable("outputVar"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        assertEquals("value1", returnValue);
        assertEquals("value1", vars.get("outputVar"));
    }
}
