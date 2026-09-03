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

package org.apache.jmeter.extractor.json.jmespath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestJMESPathExtractor {
    private static final String DEFAULT_VALUE = "NONE"; // $NON-NLS-1$
    private static final String REFERENCE_NAME = "varname"; // $NON-NLS-1$
    private static final String REFERENCE_NAME_MATCH_NUMBER = "varname_matchNr"; // $NON-NLS-1$

    private static JMESPathExtractor setupProcessor(JMeterVariables vars, SampleResult sampleResult, String data, boolean isSourceVars, String matchNumbers) {
        JMeterContext jmctx = JMeterContextService.getContext();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(sampleResult);
        JMESPathExtractor processor = new JMESPathExtractor();
        processor.setThreadContext(jmctx);
        processor.setRefName(REFERENCE_NAME);
        processor.setMatchNumber(matchNumbers);
        processor.setDefaultValue(DEFAULT_VALUE);
        if (isSourceVars) {
            vars.put("contentvar", data);
            processor.setScopeVariable("contentvar");
        } else {
            sampleResult.setResponseData(data, null);
            processor.setScopeAll();
        }
        return processor;
    }

    @Test
    void testNoMatchNumberSet() {
        JMeterVariables vars = new JMeterVariables();
        SampleResult sampleResult = new SampleResult();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "[1]", false, "");
        processor.setJmesPathExpression("[*]");
        processor.process();
        assertEquals("1", vars.get(REFERENCE_NAME));
    }

    private static Stream<Arguments> dataOneMatch() {
        return Stream.of(
            Arguments.of("[\"one\"]", "[*]", "one", "1"),
            Arguments.of("{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}", "a.b.c.d", "value", "1"),
            Arguments.of("""
                            {\r
                              "people": [\r
                                {"first": "James", "last": "d"},\r
                                {"first": "Jacob", "last": "e"},\r
                                {"first": "Jayden", "last": "f"},\r
                                {"missing": "different"}\r
                              ],\r
                              "foo": {"bar": "baz"}\r
                            }""", "people[2]",
                    "{\"first\":\"Jayden\",\"last\":\"f\"}",
                    "1")
        );
    }

    @ParameterizedTest(name = "TestFromVars: {index} Extract from {0} with path {1} should result in {2} for match {3}")
    @MethodSource("dataOneMatch")
    void testOneMatchFromVars(String data, String jmesPath, String expectedResult, String expectedMatchNumber) {
        testOneMatchOnAllExtractedValues(true, data, jmesPath, expectedResult, expectedMatchNumber);
    }

    @ParameterizedTest(name = "TestFromSampleResult: {index} Extract from {0} with path {1} should result in {2} for match {3}")
    @MethodSource("dataOneMatch")
    void testOneFromSampleResult(String data, String jmesPath, String expectedResult, String expectedMatchNumber) {
        testOneMatchOnAllExtractedValues(false, data, jmesPath, expectedResult, expectedMatchNumber);
    }

    private static void testOneMatchOnAllExtractedValues(boolean fromVars, String data, String jmesPath, String expectedResult, String expectedMatchNumber) {
        JMeterVariables vars = new JMeterVariables();
        SampleResult sampleResult = new SampleResult();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, data, fromVars, "-1");
        processor.setJmesPathExpression(jmesPath);
        processor.process();
        assertNull(vars.get(REFERENCE_NAME));
        assertEquals(expectedResult, vars.get(REFERENCE_NAME + "_1"));
        assertEquals(expectedMatchNumber, vars.get(REFERENCE_NAME_MATCH_NUMBER));

        processor.clearOldRefVars(vars, REFERENCE_NAME);
        assertNull(vars.get(REFERENCE_NAME + "_1"));
        assertNull(vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    private static Stream<Arguments> dataMultipleMatches() {
        return Stream.of(
            Arguments.of("[\"one\", \"two\"]", "[*]", new String[] {"one", "two"}, "2"),
            Arguments.of("[\"a\", \"b\", \"c\", \"d\", \"e\", \"f\"]", "[0:3]", new String[] {"a", "b","c"}, "3"),
            Arguments.of("""
                    {\r
                      "people": [\r
                        {"first": "James", "last": "d"},\r
                        {"first": "Jacob", "last": "e"},\r
                        {"first": "Jayden", "last": "f"},\r
                        {"missing": "different"}\r
                      ],\r
                      "foo": {"bar": "baz"}\r
                    }""", "people[:2].first", new String[] {"James", "Jacob"}, "2")
        );
    }

    @ParameterizedTest
    @MethodSource("dataMultipleMatches")
    void testMultipleMatchesOnAllExtractedValuesFromVars(String data, String jmesPath, String[] expectedResults, String expectedMatchNumber) {
        testMultipleMatchesOnAllExtractedValues(true, data, jmesPath, expectedResults, expectedMatchNumber);
    }

    @ParameterizedTest
    @MethodSource("dataMultipleMatches")
    void testMultipleMatchesOnAllExtractedValuesFromSampleResult(String data, String jmesPath, String[] expectedResults, String expectedMatchNumber) {
        testMultipleMatchesOnAllExtractedValues(false, data, jmesPath, expectedResults, expectedMatchNumber);
    }

    private static void testMultipleMatchesOnAllExtractedValues(boolean fromVars, String data, String jmesPath, String[] expectedResults, String expectedMatchNumber) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, data, fromVars, "-1");
        // test1
        processor.setJmesPathExpression(jmesPath);
        processor.process();
        assertNull(vars.get(REFERENCE_NAME));
        for (int i = 0; i < expectedResults.length; i++) {
            assertEquals(expectedResults[i], vars.get(REFERENCE_NAME + "_"+(i+1)));
        }
        assertEquals(expectedMatchNumber, vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    private static final String TEST_DATA = """
            {\r
              "people": [\r
                {"first": "James", "last": "d", "age":10},\r
                {"first": "Jacob", "last": "e", "age":20},\r
                {"first": "Jayden", "last": "f", "age":30},\r
                {"missing": "different"}\r
              ],\r
              "foo": {"bar": "baz"}\r
            }""";

    private static Stream<Arguments> dataMatchNumberMoreThanZero() {
        return Stream.of(
            Arguments.of(TEST_DATA, "people[:3].first", "1", "James", "3"),
            Arguments.of(TEST_DATA, "people[:3].first", "2", "Jacob", "3"),
            Arguments.of(TEST_DATA, "people[:3].first", "3", "Jayden", "3"),
            Arguments.of(TEST_DATA, "people[:3].age", "3", "30", "3"),
            Arguments.of(TEST_DATA, "people[:3].first", "4", DEFAULT_VALUE, "3")
        );
    }

    @ParameterizedTest
    @MethodSource("dataMatchNumberMoreThanZero")
    void testFromVars(String data, String jmesPath,
            String matchNumber, String expectedResult, String expectedMatchNumber) {
        testMatchNumberMoreThanZeroOn1ExtractedValue(true, data, jmesPath, matchNumber, expectedResult, expectedMatchNumber);
    }

    @ParameterizedTest
    @MethodSource("dataMatchNumberMoreThanZero")
    void testFromSampleResult(String data, String jmesPath,
            String matchNumber, String expectedResult, String expectedMatchNumber) {
        testMatchNumberMoreThanZeroOn1ExtractedValue(false, data, jmesPath, matchNumber, expectedResult, expectedMatchNumber);
    }

    private static void testMatchNumberMoreThanZeroOn1ExtractedValue(boolean fromVars, String data, String jmesPath,
                                                                     String matchNumber, String expectedResult, String expectedMatchNumber) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, data, fromVars, "1");
        processor.setMatchNumber(matchNumber);
        processor.setJmesPathExpression(jmesPath);
        processor.process();
        assertEquals(expectedResult, vars.get(REFERENCE_NAME));
        assertEquals(expectedMatchNumber, vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    enum AccessMode {
        ALL(AbstractScopedTestElement::setScopeAll),
        PARENT(AbstractScopedTestElement::setScopeParent),
        CHILDREN(AbstractScopedTestElement::setScopeChildren);

        @SuppressWarnings("ImmutableEnumChecker")
        private final Consumer<AbstractScopedTestElement> applier;

        AccessMode(Consumer<AbstractScopedTestElement> applier) {
            this.applier = applier;
        }

        void configure(AbstractScopedTestElement element) {
            applier.accept(element);
        }
    }

    private static Stream<Arguments> dataScopedSamples() {
        return Stream.of(
                Arguments.of(AccessMode.ALL, "a", "1", "23", "2"),
                Arguments.of(AccessMode.ALL, "a", "2", "42", "2"),
                Arguments.of(AccessMode.ALL, "b", "0", "parent_only", "1"),
                Arguments.of(AccessMode.ALL, "c", "0", "child_only", "1"),
                Arguments.of(AccessMode.PARENT, "a", "1", "23", "1"),
                Arguments.of(AccessMode.PARENT, "b", "0", "parent_only", "1"),
                Arguments.of(AccessMode.PARENT, "c", "0", "NONE", "0"),
                Arguments.of(AccessMode.CHILDREN, "a", "1", "42", "1"),
                Arguments.of(AccessMode.CHILDREN, "b", "0", "NONE", "0"),
                Arguments.of(AccessMode.CHILDREN, "c", "0", "child_only", "1")
        );
    }

    @ParameterizedTest(name = "{index}: Mode: {0} Path: {1} MatchNr: {2} Result: {3} Count: {4}")
    @MethodSource("dataScopedSamples")
    void testRandomElementAllMatches(AccessMode accessMode, String path, String matchNumber, String resultObject, String resultCount) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "{\"a\": 23, \"b\": \"parent_only\"}", false, matchNumber);
        SampleResult subSample = new SampleResult();
        subSample.setResponseData("{\"a\": 42, \"c\": \"child_only\"}", null);
        sampleResult.addSubResult(subSample);

        processor.setJmesPathExpression(path);
        accessMode.configure(processor);

        processor.process();
        assertEquals(resultObject, vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME + "_1"));
        assertEquals(resultCount, vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    private static Stream<Arguments> dataSourceVarOrResponse() {
        return Stream.of(Arguments.of(true), Arguments.of(false));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testRandomElementOneMatch(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}", fromVariables, "0");

        processor.setJmesPathExpression("a.b.c.d");
        processor.process();
        assertEquals("value", vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME + "_1"));
        assertEquals("1", vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testRandomElementMultipleMatches(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "[\"one\", \"two\"]", fromVariables, "0");

        processor.setJmesPathExpression("[*]");
        processor.process();
        String varValue = vars.get(REFERENCE_NAME);
        if (!"one".equals(varValue) && !"two".equals(varValue)) {
            fail("vars.get(REFERENCE_NAME) should be one or two, got " + varValue);
        }
        assertNull(vars.get(REFERENCE_NAME + "_1"));
        assertNull(vars.get(REFERENCE_NAME + "_2"));
        assertEquals("2", vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testEmptySourceData(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "", fromVariables, "-1");

        processor.setJmesPathExpression("[*]");
        processor.process();
        assertEquals(DEFAULT_VALUE, vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testErrorInJMESPath(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}", fromVariables, "-1");

        processor.setJmesPathExpression("$.k");
        processor.process();
        assertEquals(DEFAULT_VALUE, vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME+ "_1"));
        assertNull(vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testNoMatch(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "{\"a\": {\"b\": {\"c\": {\"d\": \"value\"}}}}", fromVariables, "-1");

        processor.setJmesPathExpression("a.b.c.f");
        processor.process();
        assertEquals(DEFAULT_VALUE, vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME+ "_1"));
        assertEquals("0", vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }

    @ParameterizedTest
    @MethodSource("dataSourceVarOrResponse")
    void testNoInput(boolean fromVariables) {
        SampleResult sampleResult = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        JMESPathExtractor processor = setupProcessor(vars, sampleResult, "", fromVariables, "0");

        processor.setJmesPathExpression("a.b");
        processor.process();
        assertEquals(DEFAULT_VALUE, vars.get(REFERENCE_NAME));
        assertNull(vars.get(REFERENCE_NAME+ "_1"));
        assertNull(vars.get(REFERENCE_NAME_MATCH_NUMBER));
    }
}
