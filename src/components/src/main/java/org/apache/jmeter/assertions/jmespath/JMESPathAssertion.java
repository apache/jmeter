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

import java.io.Serializable;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.extractor.json.jmespath.JMESPathCache;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import io.burt.jmespath.Expression;

/**
 * This is main class for JSON JMESPath Assertion which verifies assertion on
 * previous sample result using JMESPath expression </br>
 * <a href="https://github.com/burtcorp/jmespath-java">JMESPath-java sources and
 * doc</a> </br>
 * <a href="http://jmespath.org/">JMESPath tutorial</a>
 *
 * @since 5.2
 */
public class JMESPathAssertion extends AbstractTestElement implements Serializable, Assertion, TestStateListener {
    private static final long serialVersionUID = -6448744108529796508L;
    private static final Logger log = LoggerFactory.getLogger(JMESPathAssertion.class);
    private static final String JMESPATH = "JMES_PATH";
    private static final String EXPECTEDVALUE = "EXPECTED_VALUE";
    private static final String JSONVALIDATION = "JSONVALIDATION";
    private static final String EXPECT_NULL = "EXPECT_NULL";
    private static final String INVERT = "INVERT";
    private static final String ISREGEX = "ISREGEX";

    /**
     * Used to do a JMESPath query and compute result if the expectedValue matches
     * with the JMESPath query result
     *
     * @param assertionResult          {@link AssertionResult}
     * @param responseDataAsJsonString the response data from the sender
     * @throws Exception
     */
    private void doAssert(AssertionResult assertionResult, String responseDataAsJsonString, boolean invert)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // cast the response data to JsonNode
        JsonNode input = mapper.readValue(responseDataAsJsonString, JsonNode.class);
        // get the JMESPath expression from the cache
        // if it does not exist, compile it.
        // Expression does not compile if JMESPath expression is empty or null
        Expression<JsonNode> expression = JMESPathCache.getInstance().get(getJmesPath());
        // get the result from the JMESPath query
        JsonNode currentValue = expression.search(input);
        log.debug("JMESPath query {} invoked on response {}. Query result is {}. ", expression,
                responseDataAsJsonString, currentValue);
        checkResult(mapper, currentValue, assertionResult, invert);
    }

    private void checkResult(ObjectMapper mapper, JsonNode jsonNode, AssertionResult result, boolean invert)
            throws JsonProcessingException {
        if (isJsonValidationBool()) {
            if (jsonNode.isArray()) {
                if (arrayMatched(mapper, (ArrayNode) jsonNode)) {
                    if (invert) {
                        failAssertion(result,
                                "JMESPath '" + getJmesPath() + "' expected not to match " + getExpectedValue());
                    }
                    return;
                }
            } else {
                if (isExpectNull()) {
                    if (jsonNode instanceof NullNode) {
                        return;
                    }
                    if (invert) {
                        failAssertion(result, "Failed JMESPath '" + getJmesPath() + "' not to match null");
                    }
                } else if (isEquals(mapper, jsonNode)) {
                    if (invert) {
                        failAssertion(result,
                                "Failed JMESPath '" + getJmesPath() + "' not to match " + getExpectedValue());
                    }
                    return;
                }
            }

            if (isExpectNull()) {
                if (!invert) {
                    failAssertion(result, String.format("Value expected to be null, but found '%s'",
                            objectToString(mapper, jsonNode)));
                }
                return;
            } else {
                if (!invert) {
                    String msg;
                    if (isUseRegex()) {
                        msg = "Value expected to match regexp '%s', but it did not match: '%s'";
                    } else {
                        msg = "Value expected to be '%s', but found '%s'";
                    }
                    failAssertion(result, String.format(msg, getExpectedValue(), objectToString(mapper, jsonNode)));
                }
            }
        } else {
            if (jsonNode instanceof NullNode) {
                if (!invert) {
                    failAssertion(result, "JMESPath " + getJmesPath() + " does not exist");
                }                
            } else {
                if (invert) {
                    failAssertion(result, "JMESPath " + getJmesPath() + " expected not to exist");
                }
            }
        }
    }

    private AssertionResult failAssertion(AssertionResult assertionResult, String message) {
        assertionResult.setFailure(true);
        assertionResult.setFailureMessage(message);
        return assertionResult;
    }

    @Override
    public AssertionResult getResult(SampleResult samplerResult) {
        AssertionResult result = new AssertionResult(getName());
        String responseData = samplerResult.getResponseDataAsString();
        if (responseData.isEmpty()) {
            return result.setResultForNull();
        }

        result.setFailure(false);
        result.setFailureMessage("");

        try {
            doAssert(result, responseData, isInvert());
        } catch (Exception e) {
            if (!isInvert()) {
                result.setError(true);
                result.setFailureMessage(e.getMessage());
            }
        }
        return result;
    }

    public static String objectToString(ObjectMapper mapper, JsonNode element) throws JsonProcessingException {
        if (element.isTextual()) {
            return element.asText();
        } else {
            return mapper.writeValueAsString(element);
        }
    }

    private boolean arrayMatched(ObjectMapper mapper, ArrayNode value) throws JsonProcessingException {
        if (value.size() == 0 && "[]".equals(getExpectedValue())) {
            return true;
        }

        for (JsonNode element : value) {
            if (isExpectNull()) {
                if (element instanceof NullNode) {
                    return true;
                }
            } else {
                if (isEquals(mapper, element)) {
                    return true;
                }
            }
        }

        return isEquals(mapper, value);
    }

    private boolean isEquals(ObjectMapper mapper, JsonNode jsonNode) throws JsonProcessingException {
        String str = objectToString(mapper, jsonNode);
        if (isUseRegex()) {
            Pattern pattern = JMeterUtils.getPatternCache().getPattern(getExpectedValue());
            return JMeterUtils.getMatcher().matches(str, pattern);
        } else {
            return str.equals(getExpectedValue());
        }
    }

    @Override
    public void testStarted() {
        testStarted("");
    }

    @Override
    public void testStarted(String host) {
        // NOOP
    }

    @Override
    public void testEnded() {
        testEnded("");
    }

    @Override
    public void testEnded(String host) {
        JMESPathCache.getInstance().cleanUp();
    }
    
    /*
     * ------------------------ GETTER/SETTER ------------------------
     */
    public String getJmesPath() {
        return getPropertyAsString(JMESPATH);
    }

    public void setJmesPath(String jmesPath) {
        setProperty(JMESPATH, jmesPath);
    }

    public String getExpectedValue() {
        return getPropertyAsString(EXPECTEDVALUE);
    }

    public void setExpectedValue(String expectedValue) {
        setProperty(EXPECTEDVALUE, expectedValue);
    }

    public void setJsonValidationBool(boolean jsonValidation) {
        setProperty(JSONVALIDATION, jsonValidation);
    }

    public void setExpectNull(boolean val) {
        setProperty(EXPECT_NULL, val);
    }

    public boolean isExpectNull() {
        return getPropertyAsBoolean(EXPECT_NULL);
    }

    public boolean isJsonValidationBool() {
        return getPropertyAsBoolean(JSONVALIDATION);
    }

    public void setInvert(boolean invert) {
        setProperty(INVERT, invert);
    }

    public boolean isInvert() {
        return getPropertyAsBoolean(INVERT);
    }

    public void setIsRegex(boolean flag) {
        setProperty(ISREGEX, flag);
    }

    public boolean isUseRegex() {
        return getPropertyAsBoolean(ISREGEX, true);
    }
}
