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

package org.apache.jmeter.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

class TestJSONPostProcessor {

    private static final String VAR_NAME = "varName";

    @SuppressWarnings("ImmutableEnumChecker")
    private enum AccessMode {
        ALL(AbstractScopedTestElement::setScopeAll),
        PARENT(AbstractScopedTestElement::setScopeParent),
        CHILDREN(AbstractScopedTestElement::setScopeChildren);

        private final Consumer<AbstractScopedTestElement> applier;

        AccessMode(Consumer<AbstractScopedTestElement> applier) {
            this.applier = applier;
        }

        void configure(AbstractScopedTestElement element) {
            applier.accept(element);
        }
    }

    private static Stream<Arguments> provideArgumentsForScopes() {
        return Stream.of(
                Arguments.of(AccessMode.ALL, "$.a", "1", "23", "2"),
                Arguments.of(AccessMode.ALL, "$.a", "2", "42", "2"),
                Arguments.of(AccessMode.ALL, "$.b", "0", "parent_only", "1"),
                Arguments.of(AccessMode.ALL, "$.c", "0", "child_only", "1"),
                Arguments.of(AccessMode.PARENT, "$.a", "1", "23", "1"),
                Arguments.of(AccessMode.PARENT, "$.b", "0", "parent_only", "1"),
                Arguments.of(AccessMode.PARENT, "$.c", "0", "NONE", "0"),
                Arguments.of(AccessMode.CHILDREN, "$.a", "1", "42", "1"),
                Arguments.of(AccessMode.CHILDREN, "$.b", "0", "NONE", "0"),
                Arguments.of(AccessMode.CHILDREN, "$.c", "0", "child_only", "1")
            );
    }

    @ParameterizedTest()
    @MethodSource("provideArgumentsForScopes")
    void testAssertionWithScope(AccessMode accessMode, String path, String matchNumber, String resultObject,
            String resultCount) {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, matchNumber, false);
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValues("NONE");
        processor.setJsonPathExpressions(path);
        processor.setRefNames("result");
        accessMode.configure(processor);
        SampleResult sampleResult = createSampleResult("""
                {"a": 23, "b": "parent_only"}""");
        sampleResult.addSubResult(createSampleResult("""
                {"a": 42, "c": "child_only"}"""));
        context.setPreviousResult(sampleResult);
        context.setVariables(vars);
        processor.process();
        assertEquals(resultObject, vars.get("result"));
    }

    @Test
    void testProcessAllElementsOneMatch() {
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
        assertNull(vars.get("varname"));
        assertEquals("one", vars.get("varname_1"));
        assertEquals("1", vars.get("varname_matchNr"));
    }

    @Test
    void testProcessAllElementsMultipleMatches() {
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
        assertEquals("one", vars.get("varname_1"));
        assertEquals("two", vars.get("varname_2"));
        assertEquals("2", vars.get("varname_matchNr"));
    }

    @Test
    void testProcessRandomElementMultipleMatches() {
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
        String varname = vars.get("varname");
        if (!"one".equals(varname) && !"two".equals(varname)) {
            fail("vars.get(\"varname\") should be one or two, got " + varname);
        }
        assertNull(vars.get("varname_1"));
        assertNull(vars.get("varname_2"));
        assertNull(vars.get("varname_matchNr"));
    }

    @Test
    void testPR235CaseEmptyResponse() {
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
        assertEquals("one", vars.get("varname_1"));
        assertEquals("two", vars.get("varname_2"));
        assertEquals("2", vars.get("varname_matchNr"));
        vars.put("contentvar", "");
        processor.process();
        assertNull(vars.get("varname_matchNr"));
        assertNull(vars.get("varname_1"));
        assertNull(vars.get("varname_2"));
    }

    @Test
    void testCaseEmptyVarBug62860() {
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
        assertEquals("NONE", vars.get("varname"));
        assertNull(vars.get("varname_matchNr"));

        vars.put("contentvar", "");
        processor.process();
        assertEquals("NONE", vars.get("varname"));
        assertNull(vars.get("varname_matchNr"));
    }

    @Test
    void testPR235CaseMatchOneWithZero() {
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
        assertEquals("one", vars.get("varname_1"));
        assertEquals("two", vars.get("varname_2"));
        assertEquals("2", vars.get("varname_matchNr"));
        vars.put("contentvar", "[\"A\", \"B\"]");
        processor.setMatchNumbers("0");
        processor.process();
        String varname = vars.get("varname");
        if (!"A".equals(varname) && !"B".equals(varname)) {
            fail("vars.get(\"varname\") should be A or B, got " + varname);
        }
        assertNull(vars.get("varname_matchNr"));
        assertNull(vars.get("varname_1"));
        assertNull(vars.get("varname_2"));
    }

    private static Stream<Arguments> provideEmptyOrNullResultArgs() {
        return Stream.of(
                Arguments.of("{\"context\": null}", "$.context", "0", null, "NONE"), // bug 65681
                Arguments.of("[{\"context\": null}, {\"context\": \"\"}]", "$[*].context", "1", "2", "NONE"),
                Arguments.of("[{\"context\": null}, {\"context\": \"\"}]", "$[*].context", "2", "2", ""),
                Arguments.of("{\"context\": \"\"}", "$.context", "0", null, ""),
                Arguments.of("", "$.context", "0", null, "NONE"));
    }

    @ParameterizedTest
    @MethodSource("provideEmptyOrNullResultArgs")
    void testEmptyOrNullResult(String contextValue, String jsonPath, String matchNumber, String expectedMatchNumber,
            String expectedResult) throws ParseException {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, matchNumber, false);

        SampleResult result = new SampleResult();
        result.setResponseData(contextValue.getBytes(StandardCharsets.UTF_8));

        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);

        processor.setJsonPathExpressions(jsonPath);
        processor.setDefaultValues("NONE");
        processor.setScopeAll();
        processor.process();

        assertEquals(expectedResult, vars.get(VAR_NAME));
        assertEquals(expectedMatchNumber, vars.get(VAR_NAME + "_matchNr"));
        assertNull(vars.get(VAR_NAME + "_1"));
    }

    @Test
    void testBug59609() throws ParseException {
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
        assertEquals(expectedValue, parser.parse(vars.get(VAR_NAME)));
        assertNull(vars.get(VAR_NAME + "_matchNr"));
        assertNull(vars.get(VAR_NAME + "_1"));
    }

    @Test
    void testExtractSimpleArrayElements() {
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

        assertEquals("1,2,3", vars.get(VAR_NAME+ "_ALL"));
        for (int i = 1; i <= 3; i++) {
            String v = Integer.toString(i);
            assertEquals(v, vars.get(VAR_NAME + "_" + v));
        }

        assertEquals("3", vars.get(VAR_NAME + "_matchNr"));
    }

    @Test
    void testExtractComplexElements() {
        JMeterContext context = JMeterContextService.getContext();
        JSONPostProcessor processor = setupProcessor(context, "-1");
        String data = """
                [{"a":[1,{"d":2},3]},["b",{"h":23}],3]""";
        SampleResult result = new SampleResult();
        result.setResponseData(data.getBytes(StandardCharsets.UTF_8));
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);

        processor.setJsonPathExpressions("$[*]");
        processor.process();

        String jsonWithoutOuterParens = data.substring(1, data.length() - 1);
        assertEquals(jsonWithoutOuterParens, vars.get(VAR_NAME + "_ALL"));

        assertEquals("""
                {"a":[1,{"d":2},3]}""", vars.get(VAR_NAME + "_1"));
        assertEquals("""
                ["b",{"h":23}]""", vars.get(VAR_NAME + "_2"));
        assertEquals("3", vars.get(VAR_NAME + "_3"));

        assertEquals("3", vars.get(VAR_NAME + "_matchNr"));
    }

    @Test
    void testProcessDefaultsWithAllEmptyStrings() {
        JMeterContext context = JMeterContextService.getContext();
        // Use match number 1 for simplicity
        JSONPostProcessor processor = setupProcessor(context, "1", false);
        JMeterVariables vars = new JMeterVariables();
        context.setVariables(vars);

        // Set up sample result that won't match JSON Paths
        SampleResult sampleResult = createSampleResult("{}"); // Empty JSON
        context.setPreviousResult(sampleResult);

        // Configure the processor
        // Three paths and names, corresponding to the three empty defaults
        processor.setJsonPathExpressions("$.key1;$.key2;$.key3");
        processor.setRefNames("var1;var2;var3");
        // *** Default values string containing only separators ***
        // This should split into ["", "", ""] with the fix
        processor.setDefaultValues(";;"); // Represents 3 empty default values
        processor.setScopeAll(); // Ensure it processes the main sample

        // Process the sample
        processor.process();

        // Assertions: Verify all variables received an empty string default
        assertEquals("", vars.get("var1"), "Variable var1 should get the first (empty) default value");
        assertEquals("", vars.get("var2"), "Variable var2 should get the second (empty) default value");
        assertEquals("", vars.get("var3"), "Variable var3 should get the third (empty) default value");
    }

    private static JSONPostProcessor setupProcessor(JMeterContext context, String matchNumbers) {
        return setupProcessor(context, matchNumbers, true);
    }

    private static SampleResult createSampleResult(String data) {
        SampleResult result = new SampleResult();
        result.setResponseData(data, null);
        return result;
    }

    private static JSONPostProcessor setupProcessor(JMeterContext context,
            String matchNumbers, boolean computeConcatenation) {
        JSONPostProcessor processor = new JSONPostProcessor();
        processor.setThreadContext(context);
        processor.setRefNames(VAR_NAME);
        processor.setMatchNumbers(matchNumbers);
        processor.setComputeConcatenation(computeConcatenation);
        return processor;
    }


}
