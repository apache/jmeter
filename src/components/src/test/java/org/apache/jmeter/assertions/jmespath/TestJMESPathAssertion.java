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
package org.apache.jmeter.assertions.jmespath;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class TestJMESPathAssertion {
    @RunWith(Parameterized.class)
    public static class TestAssertion {
        private static final String JSON_ARRAY = 
                "{\n" + "  \"people\": [\n" + "    {\n" + "      \"name\": \"b\",\n" + "      \"age\": 30\n"
                + "    },\n" + "    {\n" + "      \"name\": \"a\",\n" + "      \"age\": 50\n" + "    },\n" + "    {\n"
                + "      \"name\": \"c\",\n" + "      \"age\": 40\n" + "    }\n" + "  ]\n" + "}";

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                // {INVERT,         RESPONSE DATA,                                JMESPATH,     VALIDATION, REGEX,      EXPECT NULL,  RESULT,    FAILURE,         ERROR,      FAILURE_MSG}
                {Boolean.TRUE, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]",                 "[6:6]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "[]", Boolean.TRUE, Boolean.FALSE, "JMESPath '[6:6]' expected not to match []"},
                {Boolean.FALSE, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]",                 "[6:6]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "[]", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]",                 "[6:6]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "[1]", Boolean.TRUE, Boolean.FALSE, "Value expected to be '[1]', but found '[]'"},
                {Boolean.FALSE, "{\"one\": \"1\",\"two\": \"2\"}",                 "[one,two]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "[\"1\",\"2\"]", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"a\": \"foo\", \"b\": \"bar\", \"c\": \"baz\"}", "a",        Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "foo",     Boolean.FALSE, Boolean.FALSE, "" },
                {Boolean.FALSE, "{\"a\": \"123\"}",                                 "a",        Boolean.TRUE, Boolean.TRUE,  Boolean.FALSE, "123|456", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"a\": \"123\"}",                                 "a",        Boolean.TRUE, Boolean.TRUE,  Boolean.FALSE, "789|012", Boolean.TRUE, Boolean.FALSE, "Value expected to match regexp '789|012', but it did not match: '123'"},
                {Boolean.FALSE, JSON_ARRAY,                 "max_by(people, &age).name", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "a", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "two", Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.TRUE,  "1", Boolean.TRUE,  Boolean.FALSE, "Value expected to be null, but found ''"},
                {Boolean.TRUE, "{\"one\": \"1\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "2", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.TRUE, "{\"one\": \"\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, "", Boolean.TRUE, Boolean.FALSE, "Failed JMESPath 'one' not to match null"},
                {Boolean.FALSE, "{\"one\": \"1\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "2", Boolean.TRUE, Boolean.FALSE, "Value expected to be '2', but found '1'"},
                {Boolean.TRUE, "{\"one\": \"1\"}",                                 "one", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "1", Boolean.TRUE, Boolean.FALSE, "Failed JMESPath 'one' not to match 1"},
                {Boolean.TRUE, "{'one': '1'}",                                 "one",     Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "2", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{'one': '1'}",                                 "one",     Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "2", Boolean.FALSE, Boolean.TRUE, "Unexpected character (''' (code 39)): was expecting double-quote to start field name\n at [Source: (String)\"{'one': '1'}\"; line: 1, column: 3]"},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "one",     Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, "1", Boolean.TRUE, Boolean.FALSE, "Value expected to be '1', but found ''"},
                {Boolean.FALSE, "{\"\":\"\"}",                                 "foo",     Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "one",     Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "{\"one\": \"\"}",                                 "two",     Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "", Boolean.TRUE, Boolean.FALSE, "JMESPath two does not exist"},            
                {Boolean.TRUE, "{\"one\": \"\"}",                                 "one",     Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "", Boolean.TRUE, Boolean.FALSE, "JMESPath one expected not to exist"},
                {Boolean.TRUE, "{\"one\": \"\"}",                                 "two",     Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "", Boolean.FALSE, Boolean.FALSE, ""},
                {Boolean.FALSE, "",                                             "two",     Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, "", Boolean.TRUE, Boolean.FALSE, AssertionResult.RESPONSE_WAS_NULL},
            });
        }
    
        private Boolean isInverted;
        private String responseData;
        private String jmesPath;
        private Boolean isValidation;
        private Boolean isRegex;
        private Boolean isExpectedNull;
        private String expectedValue;
        private Boolean isFailure;
        private Boolean isError;
        private String failureMessage;
    
        public TestAssertion(Boolean isInverted, String responseData, String jmesPath, Boolean isValidation, Boolean isRegex,
                Boolean isExpectedNull, String expectedValue, Boolean isFailure, Boolean isError, String failureMessage) {
            super();
            this.isInverted = isInverted;
            this.responseData = responseData;
            this.jmesPath = jmesPath;
            this.isValidation = isValidation;
            this.isRegex = isRegex;
            this.isExpectedNull = isExpectedNull;
            this.expectedValue = expectedValue;
            this.isFailure = isFailure;
            this.isError = isError;
            this.failureMessage = failureMessage;
        }
    
        @Test
        public void test() {
            SampleResult samplerResult = new SampleResult();
            samplerResult.setResponseData(responseData, null);
            JMESPathAssertion instance = new JMESPathAssertion();
            instance.setJmesPath(jmesPath);
            instance.setJsonValidationBool(isValidation);
            instance.setInvert(isInverted);
            instance.setIsRegex(isRegex);
            instance.setExpectNull(isExpectedNull);
            instance.setExpectedValue(expectedValue);
            AssertionResult expResult = new AssertionResult("");
            AssertionResult result = instance.getResult(samplerResult);
            assertEquals(expResult.getName(), result.getName());
            assertEquals(isFailure, result.isFailure());
            assertEquals(isError, result.isError());
            assertEquals(failureMessage, result.getFailureMessage());
        }
    }
}
