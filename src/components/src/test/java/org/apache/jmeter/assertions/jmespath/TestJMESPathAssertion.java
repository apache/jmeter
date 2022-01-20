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

package org.apache.jmeter.assertions.jmespath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.stream.Stream;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestJMESPathAssertion {
    private enum InvertType {
        USE_NO_INVERT, USE_INVERT
    }
    private enum ValidationType {
        USE_NO_VALIDATION, USE_VALIDATION
    }
    private enum ComparisonType {
        USE_NO_REXEG, USE_REGEX
    }
    private enum ResultNullity {
        EXPECT_NOT_NULL, EXPECT_NULL
    }
    private enum ResultType {
        SUCCESS, ERROR, FAILURE
    }

    private static final String JSON_ARRAY =
            "{\"people\": [ {\"name\": \"b\", \"age\": 30},"
                    + " {\"name\": \"a\", \"age\": 50},"
                    + " {\"name\": \"c\", \"age\": 40}"
                    + "  ]"
                    + "}";

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(InvertType.USE_INVERT, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", "[6:6]", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "[]", ResultType.FAILURE,
                        "Value expected not to be equal to []"),
                Arguments.of(InvertType.USE_NO_INVERT, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", "[6:6]",
                        ValidationType.USE_VALIDATION, ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "[]", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", "[6:6]",
                        ValidationType.USE_VALIDATION, ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "[1]", ResultType.FAILURE, "Value expected to be equal to [1]"),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"1\",\"two\": \"2\"}", "[one,two]",
                        ValidationType.USE_VALIDATION, ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "[\"1\",\"2\"]", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"a\": \"foo\", \"b\": \"bar\", \"c\": \"baz\"}", "a",
                        ValidationType.USE_VALIDATION, ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "foo", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"a\": \"123\"}", "a", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_REGEX, ResultNullity.EXPECT_NOT_NULL, "123|456", ResultType.SUCCESS,
                        ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"a\": \"123\"}", "a", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_REGEX, ResultNullity.EXPECT_NOT_NULL, "789|012", ResultType.FAILURE,
                        "Value expected to match 789|012"),
                Arguments.of(InvertType.USE_INVERT, "{\"a\": \"123\"}", "a", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_REGEX, ResultNullity.EXPECT_NOT_NULL, "123|012", ResultType.FAILURE,
                        "Value expected not to match 123|012"),
                Arguments.of(InvertType.USE_NO_INVERT, JSON_ARRAY, "max_by(people, &age).name", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "a", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "two", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NULL, null, ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NULL, "1", ResultType.FAILURE,
                        "Value expected to be null"),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"1\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "2", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NULL, "", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"\"}", "two", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NULL, "", ResultType.FAILURE,
                        "Value expected not to be null"),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"1\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "2", ResultType.FAILURE,
                        "Value expected to be equal to 2" ),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"1\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "1", ResultType.FAILURE,
                        "Value expected not to be equal to 1"),
                Arguments.of(InvertType.USE_INVERT, "{'one': '1'}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "2", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{'one': '1'}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "2", ResultType.ERROR,
                        "Unexpected character (''' (code 39)): was expecting double-quote to start field name\n at"
                                + " [Source: (String)\"{'one': '1'}\"; line: 1, column: 3]"),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "1", ResultType.FAILURE,
                        "Value expected to be equal to 1"),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"\":\"\"}", "foo", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NULL, null, ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_NO_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"one\": \"\"}", "two", ValidationType.USE_NO_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.FAILURE,
                        "JMESPATH two expected to exist"),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"\"}", "one", ValidationType.USE_NO_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.FAILURE,
                        "JMESPATH one expected not to exist"),
                Arguments.of(InvertType.USE_INVERT, "{\"one\": \"\"}", "two", ValidationType.USE_NO_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "", "two", ValidationType.USE_NO_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL, "", ResultType.FAILURE,
                        AssertionResult.RESPONSE_WAS_NULL),
                Arguments.of(InvertType.USE_NO_INVERT,
                        "{\n" + "  \"reservations\": [\n" + "    {\n" + "      \"instances\": [\n"
                                + "        {\"state\": \"running\"},\n" + "        {\"state\": \"stopped\"}\n"
                                + "      ]\n" + "    },\n" + "    {\n" + "      \"instances\": [\n"
                                + "        {\"state\": \"terminated\"},\n" + "        {\"state\": \"running\"}\n"
                                + "      ]\n" + "    }\n" + "  ]\n" + "}",
                        "reservations[*].instances[*].state", ValidationType.USE_VALIDATION,
                        ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "[[\"running\",\"stopped\"],[\"terminated\",\"running\"]]", ResultType.SUCCESS, ""),
                Arguments.of(InvertType.USE_NO_INVERT, "{\"x\": {\"a\": 23, \"b\": 42, \"c\": \"something\"}}", "x",
                        ValidationType.USE_VALIDATION, ComparisonType.USE_NO_REXEG, ResultNullity.EXPECT_NOT_NULL,
                        "{\n\t\"a\": 23,\n\t\"b\": 42,\n\t\"c\": \"something\"\n}", ResultType.SUCCESS,
                        "" ));
    }

    @ParameterizedTest(
            name = "index:{index} => data({1}, jmespath={2}, invert={0}, validation:{3}, regex:{4}, nullability:{5}, "
                    + "expected value:{6}, expected result type:{7}, expected failure message:{8})")
    @MethodSource("data")
    void test(InvertType isInverted, String responseData, String jmesPath, ValidationType isValidation,
            ComparisonType isRegex, ResultNullity isExpectedNull, String expectedValue, ResultType resultType,
            String failureMessage) {
        SampleResult samplerResult = new SampleResult();
        samplerResult.setResponseData(responseData, null);
        JMESPathAssertion instance = new JMESPathAssertion();
        instance.setJmesPath(jmesPath);
        instance.setJsonValidationBool(isValidation == ValidationType.USE_VALIDATION);
        instance.setInvert(isInverted == InvertType.USE_INVERT);
        instance.setIsRegex(isRegex == ComparisonType.USE_REGEX);
        instance.setExpectNull(isExpectedNull == ResultNullity.EXPECT_NULL);
        instance.setExpectedValue(expectedValue);
        AssertionResult expResult = new AssertionResult("");
        AssertionResult result = instance.getResult(samplerResult);
        assertEquals(expResult.getName(), result.getName());
        if (result.isError() && !result.isFailure()) {
            assertEquals(ResultType.ERROR, resultType);
        } else if (result.isFailure() && !result.isError()) {
            assertEquals(ResultType.FAILURE, resultType);
        } else if (!result.isError() && !result.isFailure()) {
            assertEquals(ResultType.SUCCESS, resultType);
        } else {
            fail("Got unexpected state where AssertionResult is in error and in failure");
        }
        assertEquals(failureMessage, result.getFailureMessage());
    }
}
