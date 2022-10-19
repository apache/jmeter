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

package org.apache.jmeter.assertions;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TestJSONPathAssertion {

    @Test
    void testGetJsonPath() {
        JSONPathAssertion instance = new JSONPathAssertion();
        String expResult = "";
        String result = instance.getJsonPath();
        assertEquals(expResult, result);
    }

    @Test
    void testSetJsonPath() {
        String jsonPath = "$.someVar";
        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath(jsonPath);
        assertEquals(jsonPath, instance.getJsonPath());
    }

    @Test
    void testGetExpectedValue() {
        JSONPathAssertion instance = new JSONPathAssertion();
        String expResult = "";
        String result = instance.getExpectedValue();
        assertEquals(expResult, result);
    }

    @Test
    void testSetExpectedValue() {
        String expectedValue = "some value";
        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setExpectedValue(expectedValue);
        assertEquals(expectedValue, instance.getExpectedValue());
    }

    @Test
    void testSetJsonValidationBool() {
        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonValidationBool(false);
        assertFalse(instance.isJsonValidationBool());
    }

    @Test
    void testIsJsonValidationBool() {
        JSONPathAssertion instance = new JSONPathAssertion();
        boolean result = instance.isJsonValidationBool();
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource(value={
        "{\"myval\": 123}; $.myval; 123",
        "{\"myval\": [{\"test\":1},{\"test\":2},{\"test\":3}]}; $.myval[*].test; 2",
        "{\"myval\": []}; $.myval; []",
        "{\"myval\": {\"key\": \"val\"}}; $.myval; \\{\"key\":\"val\"\\}"
    }, delimiterString=";")
    void testGetResult_pathsWithOneResult(String data, String jsonPath, String expectedResult) {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData(data.getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath(jsonPath);
        instance.setJsonValidationBool(true);
        instance.setExpectedValue(expectedResult);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertFalse(result.isFailure());
    }

    @Test
    void testGetResult_positive_regexp() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": 123}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("(123|456)");
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertFalse(result.isFailure());

        samplerResult.setResponseData("{\"myval\": 456}".getBytes(Charset.defaultCharset()));
        AssertionResult result2 = instance.getResult(samplerResult);
        assertFalse(result2.isFailure());
    }

    @Test
    void testGetResult_positive_invert() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": 123}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("123");
        instance.setInvert(true);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertTrue(result.isFailure());
        assertEquals(expResult.getName(), result.getName());
    }

    @Test
    void testGetResult_not_regexp() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": \"some complicated value\"}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("some.+");
        AssertionResult result = instance.getResult(samplerResult);
        assertFalse(result.isFailure());

        instance.setIsRegex(false);
        AssertionResult result2 = instance.getResult(samplerResult);
        assertTrue(result2.isFailure());
    }

    @Test
    void testGetResult_complex_map() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData(
                "{\"myval\": { \"a\": 23, \"b\": 42, \"c\": \"something\" } }".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setIsRegex(false);
        instance.setExpectedValue("{\n\t\"a\": 23,\n\"b\": 42,\n\t\"c\": \"something\"\n}");
        AssertionResult result = instance.getResult(samplerResult);
        assertFalse(result.isFailure());
    }

    @Test
    void testGetResult_negative() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": 123}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("1234");
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_negative_invert() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": 123}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("1234");
        instance.setInvert(true);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertFalse(result.isFailure());
        assertEquals(expResult.getName(), result.getName());
    }

    @Test
    void testGetResult_null() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": null}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setExpectNull(true);
        instance.setJsonValidationBool(true);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertFalse(result.isFailure());
    }

    @Test
    void testGetResult_null_not_found() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": 123}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setExpectNull(true);
        instance.setJsonValidationBool(true);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_null_novalidate() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": null}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval");
        instance.setJsonValidationBool(false);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertFalse(result.isFailure());
    }

    @Test
    void testGetResult_no_such_path() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": null}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.notexist");
        instance.setJsonValidationBool(false);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_list_negative() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData(
                "{\"myval\": [{\"test\":1},{\"test\":2},{\"test\":3}]}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval[*].test");
        instance.setJsonValidationBool(true);
        instance.setExpectedValue("5");
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_list_empty_novalidate() {
        // With bug 65794 the outcome of this test has changed
        // we now consider an indefinite path with no assertion value
        // an error and set the AssertionResult to failure
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": []}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval[*]");
        instance.setJsonValidationBool(false);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_inverted_null() {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData("{\"myval\": [{\"key\": null}]}".getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.myval[*].key");
        instance.setJsonValidationBool(true);
        instance.setExpectNull(true);
        instance.setInvert(true);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
    }

    @Test
    void testGetResult_match_msg_problem() {
        SampleResult samplerResult = new SampleResult();
        String str = "{\"execution\":[{\"scenario\":{\"requests\":[{\"headers\":{\"headerkey\":\"header value\"}}]}}]}";
        samplerResult.setResponseData(str.getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        String jsonPath = "$.execution[0].scenario.requests[0].headers";
        String expectedValue = "\\{headerkey=header value\\}";
        instance.setJsonPath(jsonPath);
        instance.setJsonValidationBool(true);
        instance.setExpectNull(false);
        instance.setExpectedValue(expectedValue);
        instance.setInvert(false);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertTrue(result.isFailure());
        assertEquals(String.format(
                "Value in json path '%s' expected to match regexp '%s', but it did not match: '{\"headerkey\":\"header value\"}'",
                        jsonPath, expectedValue), result.getFailureMessage());
    }

    @Test
    void testGetResult_match_msg_problem2() {
        SampleResult samplerResult = new SampleResult();
        String str = "{\n" +
                " \"code\":200,\n" +
                " \"contact\":{\n" +
                "   \"id\":28071,\n" +
                "   \"description\":\"test description\",\n" +
                "   \"info\":{\n" +
                "       \"ngn_number\":[\n" +
                "           \"2003\",\n" +
                "           \"2004\"\n" +
                "       ]\n" +
                "   }\n" +
                " }\n" +
                "}";
        samplerResult.setResponseData(str.getBytes(Charset.defaultCharset()));

        JSONPathAssertion instance = new JSONPathAssertion();
        instance.setJsonPath("$.contact.info.ngn_number");
        instance.setJsonValidationBool(true);
        instance.setExpectNull(false);
        instance.setExpectedValue("[\"2003\",\"2004\"]");
        instance.setInvert(false);
        instance.setIsRegex(false);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        assertFalse(result.isFailure());
    }

    @Test
    void testGetResultFloat() {
        Locale prevLocale = Locale.getDefault();
        try {
            // 0.0000123456789 is locale-dependent
            Locale.setDefault(Locale.US);
            SampleResult samplerResult = new SampleResult();

            samplerResult.setResponseData(
                    "{\"myval\": [{\"test\":0.0000123456789}]}".getBytes(Charset.defaultCharset()));

            JSONPathAssertion instance = new JSONPathAssertion();
            instance.setJsonPath("$.myval[*].test");
            instance.setJsonValidationBool(true);
            instance.setIsRegex(false);
            instance.setExpectedValue("0.0000123456789");

            AssertionResult expResult = new AssertionResult("");
            AssertionResult result = instance.getResult(samplerResult);
            assertEquals(expResult.getName(), result.getName());
            assertFalse(result.isFailure());
        } finally {
            Locale.setDefault(prevLocale);
        }
    }
}
