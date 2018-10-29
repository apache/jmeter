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

package org.apache.jmeter.extractor;

import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;

import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class TestJSONPostProcessor {

    private static final String VAR_NAME = "varName";

    @Test
    public void testProcessAllElementsOneMatch() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1", true);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\"]");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }

    @Test
    public void testProcessAllElementsMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1", true);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
    }

    @Test
    public void testProcessRandomElementMultipleMatches() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "0", true);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("one"), CoreMatchers.is("two"))));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testPR235CaseEmptyResponse() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1", true);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
        vars.put("contentvar", "");
        processor.process();
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
    }
    
    @Test
    public void testCaseEmptyVarBug62860() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "0", false);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.remove("contentvar");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is("NONE"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
        
        vars.put("contentvar", "");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is("NONE"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));

    }

    @Test
    public void testPR235CaseMatchOneWithZero() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1", true);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions("$[*]");
        processor.setRefNames("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "[\"one\", \"two\"]");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
        vars.put("contentvar", "[\"A\", \"B\"]");
        processor.setMatchNumbers("0");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("A"), CoreMatchers.is("B"))));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testBug59609() throws ParseException {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "0", false);

        String innerValue = "{\"a\":\"one\",\"b\":\"two\"}";
        String data = "{\"context\":" + innerValue + "}";
        SampleResult result = new SampleResult();
        result.setResponseData(data.getBytes(StandardCharsets.UTF_8));

        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);

        processor.setJsonPathExpressions("$.context");
        processor.process();

        JSONParser parser = new JSONParser(0);
        Object expectedValue = parser.parse(innerValue);
        assertThat(parser.parse(vars.get(VAR_NAME)), CoreMatchers.is(expectedValue));
        assertThat(vars.get(VAR_NAME + "_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get(VAR_NAME + "_1"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testExtractSimpleArrayElements() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1");
        String data = "[1,2,3]";
        SampleResult result = new SampleResult();
        result.setResponseData(data.getBytes(StandardCharsets.UTF_8));
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);

        processor.setJsonPathExpressions("$[*]");
        processor.process();

        Assert.assertEquals("1,2,3", vars.get(VAR_NAME+ "_ALL"));
        for (int i = 1; i <= 3; i++) {
            String v = Integer.toString(i);
            Assert.assertEquals(v, vars.get(VAR_NAME + "_" + v));
        }

        Assert.assertEquals("3", vars.get(VAR_NAME + "_matchNr"));
    }

    @Test
    public void testExtractComplexElements() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1");
        String data = "[{\"a\":[1,{\"d\":2},3]},[\"b\",{\"h\":23}],3]";
        SampleResult result = new SampleResult();
        result.setResponseData(data.getBytes(StandardCharsets.UTF_8));
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);

        processor.setJsonPathExpressions("$[*]");
        processor.process();

        String jsonWithoutOuterParens = data.substring(1, data.length() - 1);
        Assert.assertEquals(jsonWithoutOuterParens, vars.get(VAR_NAME + "_ALL"));

        Assert.assertEquals("{\"a\":[1,{\"d\":2},3]}", vars.get(VAR_NAME + "_1"));
        Assert.assertEquals("[\"b\",{\"h\":23}]", vars.get(VAR_NAME + "_2"));
        Assert.assertEquals("3", vars.get(VAR_NAME + "_3"));

        Assert.assertEquals("3", vars.get(VAR_NAME + "_matchNr"));
    }

    private JSONPostProcessor setupProcessor(JMeterContext context,
    String matchNumbers) {
        return setupProcessor(context, matchNumbers, true);
    }

    private JSONPostProcessor setupProcessor(JMeterContext context,
            String matchNumbers, boolean computeConcatenation) {
        JSONPostProcessor processor = new JSONPostProcessor();
        processor.setThreadContext(context);
        processor.setRefNames(VAR_NAME);
        processor.setMatchNumbers(matchNumbers);
        processor.setComputeConcatenation(computeConcatenation);
        return processor;
    }

}
