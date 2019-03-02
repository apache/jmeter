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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.Document;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test element to handle Response Assertions.
 * See {@link AssertionGui} for GUI.
 */
public class ResponseAssertion extends AbstractScopedAssertion implements Serializable, Assertion {
    private static final Logger log = LoggerFactory.getLogger(ResponseAssertion.class);

    private static final long serialVersionUID = 242L;

    private static final String TEST_FIELD = "Assertion.test_field";  // $NON-NLS-1$

    // Values for TEST_FIELD
    // N.B. we cannot change the text value as it is in test plans
    private static final String SAMPLE_URL = "Assertion.sample_label"; // $NON-NLS-1$
    private static final String RESPONSE_DATA = "Assertion.response_data"; // $NON-NLS-1$
    private static final String RESPONSE_DATA_AS_DOCUMENT = "Assertion.response_data_as_document"; // $NON-NLS-1$
    private static final String RESPONSE_CODE = "Assertion.response_code"; // $NON-NLS-1$
    private static final String RESPONSE_MESSAGE = "Assertion.response_message"; // $NON-NLS-1$
    private static final String RESPONSE_HEADERS = "Assertion.response_headers"; // $NON-NLS-1$
    private static final String REQUEST_HEADERS = "Assertion.request_headers"; // $NON-NLS-1$
    private static final String REQUEST_DATA = "Assertion.request_data"; // $NON-NLS-1$
    private static final String ASSUME_SUCCESS = "Assertion.assume_success"; // $NON-NLS-1$
    private static final String TEST_STRINGS = "Asserion.test_strings"; // $NON-NLS-1$
    private static final String TEST_TYPE = "Assertion.test_type"; // $NON-NLS-1$
    private static final String CUSTOM_MESSAGE = "Assertion.custom_message"; // $NON-NLS-1$

    /**
     * Mask values for TEST_TYPE
     * they are mutually exclusive
     */
    private static final int MATCH = 1; // 1 << 0; // NOSONAR We want this comment
    private static final int CONTAINS = 1 << 1;
    private static final int NOT = 1 << 2;
    private static final int EQUALS = 1 << 3;
    private static final int SUBSTRING = 1 << 4;
    private static final int OR = 1 << 5;

    // Mask should contain all types (but not NOT nor OR)
    private static final int TYPE_MASK = CONTAINS | EQUALS | MATCH | SUBSTRING;

    private static final int  EQUALS_SECTION_DIFF_LEN
            = JMeterUtils.getPropDefault("assertion.equals_section_diff_len", 100);

    /** Signifies truncated text in diff display. */
    private static final String EQUALS_DIFF_TRUNC = "...";

    private static final String RECEIVED_STR = "****** received  : ";
    private static final String COMPARISON_STR = "****** comparison: ";
    private static final String DIFF_DELTA_START
            = JMeterUtils.getPropDefault("assertion.equals_diff_delta_start", "[[[");
    private static final String DIFF_DELTA_END
            = JMeterUtils.getPropDefault("assertion.equals_diff_delta_end", "]]]");

    public ResponseAssertion() {
        setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList<String>()));
    }

    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList<String>()));
    }

    private void setTestField(String testField) {
        setProperty(TEST_FIELD, testField);
    }

    public void setTestFieldURL(){
        setTestField(SAMPLE_URL);
    }

    public void setTestFieldResponseCode(){
        setTestField(RESPONSE_CODE);
    }

    public void setTestFieldResponseData(){
        setTestField(RESPONSE_DATA);
    }

    public void setTestFieldResponseDataAsDocument(){
        setTestField(RESPONSE_DATA_AS_DOCUMENT);
    }

    public void setTestFieldResponseMessage(){
        setTestField(RESPONSE_MESSAGE);
    }

    public void setTestFieldResponseHeaders(){
        setTestField(RESPONSE_HEADERS);
    }

    public void setTestFieldRequestHeaders() {
        setTestField(REQUEST_HEADERS);
    }

    public void setTestFieldRequestData() {
        setTestField(REQUEST_DATA);
    }

    public void setCustomFailureMessage(String customFailureMessage) {
        setProperty(CUSTOM_MESSAGE, customFailureMessage);
    }

    public String getCustomFailureMessage() {
        return getPropertyAsString(CUSTOM_MESSAGE);
    }

    public boolean isTestFieldURL(){
        return SAMPLE_URL.equals(getTestField());
    }

    public boolean isTestFieldResponseCode(){
        return RESPONSE_CODE.equals(getTestField());
    }

    public boolean isTestFieldResponseData(){
        return RESPONSE_DATA.equals(getTestField());
    }

    public boolean isTestFieldResponseDataAsDocument() {
        return RESPONSE_DATA_AS_DOCUMENT.equals(getTestField());
    }

    public boolean isTestFieldResponseMessage(){
        return RESPONSE_MESSAGE.equals(getTestField());
    }

    public boolean isTestFieldResponseHeaders(){
        return RESPONSE_HEADERS.equals(getTestField());
    }

    public boolean isTestFieldRequestHeaders(){
        return REQUEST_HEADERS.equals(getTestField());
    }

    public boolean isTestFieldRequestData(){
        return REQUEST_DATA.equals(getTestField());
    }

    private void setTestType(int testType) {
        setProperty(new IntegerProperty(TEST_TYPE, testType));
    }

    private void setTestTypeMasked(int testType) {
        int value = getTestType() & ~TYPE_MASK | testType;
        setProperty(new IntegerProperty(TEST_TYPE, value));
    }

    public void addTestString(String testString) {
        getTestStrings().addProperty(new StringProperty(String.valueOf(testString.hashCode()), testString));
    }

    public void clearTestStrings() {
        getTestStrings().clear();
    }

    @Override
    public AssertionResult getResult(SampleResult response) {
        return evaluateResponse(response);
    }

    public String getTestField() {
        return getPropertyAsString(TEST_FIELD);
    }

    public int getTestType() {
        JMeterProperty type = getProperty(TEST_TYPE);
        if (type instanceof NullProperty) {
            return CONTAINS;
        }
        return type.getIntValue();
    }

    public CollectionProperty getTestStrings() {
        return (CollectionProperty) getProperty(TEST_STRINGS);
    }

    public boolean isEqualsType() {
        return (getTestType() & EQUALS) != 0;
    }

    public boolean isSubstringType() {
        return (getTestType() & SUBSTRING) != 0;
    }

    public boolean isContainsType() {
        return (getTestType() & CONTAINS) != 0;
    }

    public boolean isMatchType() {
        return (getTestType() & MATCH) != 0;
    }

    public boolean isNotType() {
        return (getTestType() & NOT) != 0;
    }

    public boolean isOrType() {
        return (getTestType() & OR) != 0;
    }

    public void setToContainsType() {
        setTestTypeMasked(CONTAINS);
    }

    public void setToMatchType() {
        setTestTypeMasked(MATCH);
    }

    public void setToEqualsType() {
        setTestTypeMasked(EQUALS);
    }

    public void setToSubstringType() {
        setTestTypeMasked(SUBSTRING);
    }

    public void setToNotType() {
        setTestType(getTestType() | NOT);
    }

    public void unsetNotType() {
        setTestType(getTestType() & ~NOT);
    }

    public void setToOrType() {
        setTestType(getTestType() | OR);
    }

    public void unsetOrType() {
        setTestType(getTestType() & ~OR);
    }

    public boolean getAssumeSuccess() {
        return getPropertyAsBoolean(ASSUME_SUCCESS, false);
    }

    public void setAssumeSuccess(boolean b) {
        setProperty(ASSUME_SUCCESS, b);
    }

    /**
     * Make sure the response satisfies the specified assertion requirements.
     *
     * @param response an instance of SampleResult
     * @return an instance of AssertionResult
     */
    private AssertionResult evaluateResponse(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());

        if (getAssumeSuccess()) {
            response.setSuccessful(true);// Allow testing of failure codes
        }

        String toCheck = getStringToCheck(response);

        result.setFailure(false);
        result.setError(false);
        boolean notTest = (NOT & getTestType()) > 0;
        boolean orTest = (OR & getTestType()) > 0;
        boolean contains = isContainsType(); // do it once outside loop
        boolean equals = isEqualsType();
        boolean substring = isSubstringType();
        boolean matches = isMatchType();

        log.debug("Test Type Info: contains={}, notTest={}, orTest={}", contains, notTest, orTest);

        if (StringUtils.isEmpty(toCheck)) {
            if (notTest) { // Not should always succeed against an empty result
                return result;
            }
            if (log.isDebugEnabled()) {
                log.debug("Not checking empty response field in: {}", response.getSampleLabel());
            }
            return result.setResultForNull();
        }

        try {
            // Get the Matcher for this thread
            Perl5Matcher localMatcher = JMeterUtils.getMatcher();
            boolean hasTrue = false;
            List<String> allCheckMessage = new ArrayList<>();
            for (JMeterProperty jMeterProperty : getTestStrings()) {
                String stringPattern = jMeterProperty.getStringValue();
                Pattern pattern = null;
                if (contains || matches) {
                    pattern = JMeterUtils.getPatternCache().getPattern(stringPattern, Perl5Compiler.READ_ONLY_MASK);
                }
                boolean found;
                if (contains) {
                    found = localMatcher.contains(toCheck, pattern);
                } else if (equals) {
                    found = toCheck.equals(stringPattern);
                } else if (substring) {
                    found = toCheck.contains(stringPattern);
                } else {
                    found = localMatcher.matches(toCheck, pattern);
                }
                boolean pass = notTest ? !found : found;
                if (orTest) {
                    if (!pass) {
                        log.debug("Failed: {}", stringPattern);
                        allCheckMessage.add(getFailText(stringPattern, toCheck));
                    } else {
                        hasTrue=true;
                        break;
                    }
                } else {
                    if (!pass) {
                        log.debug("Failed: {}", stringPattern);
                        result.setFailure(true);
                        String customMsg = getCustomFailureMessage();
                        if (StringUtils.isEmpty(customMsg)) {
                            result.setFailureMessage(getFailText(stringPattern, toCheck));
                        } else {
                            result.setFailureMessage(customMsg);
                        }
                        break;
                    }
                    log.debug("Passed: {}", stringPattern);
                }
            }
            if (orTest && !hasTrue){
                result.setFailure(true);
                String customMsg = getCustomFailureMessage();
                if (StringUtils.isEmpty(customMsg)) {
                    result.setFailureMessage(allCheckMessage.stream().collect(Collectors.joining("\t", "", "\t")));
                } else {
                    result.setFailureMessage(customMsg);
                }
            }
        } catch (MalformedCachePatternException e) {
            result.setError(true);
            result.setFailure(false);
            result.setFailureMessage("Bad test configuration " + e);
        }
        return result;
    }

    private String getStringToCheck(SampleResult response) {
        String toCheck; // The string to check (Url or data)
        // What are we testing against?
        if (isScopeVariable()){
            toCheck = getThreadContext().getVariables().get(getVariableName());
        } else if (isTestFieldResponseData()) {
            toCheck = response.getResponseDataAsString(); // (bug25052)
        } else if (isTestFieldResponseDataAsDocument()) {
            toCheck = Document.getTextFromDocument(response.getResponseData());
        } else if (isTestFieldResponseCode()) {
            toCheck = response.getResponseCode();
        } else if (isTestFieldResponseMessage()) {
            toCheck = response.getResponseMessage();
        } else if (isTestFieldRequestHeaders()) {
            toCheck = response.getRequestHeaders();
        } else if (isTestFieldRequestData()) {
            toCheck = response.getSamplerData();
        } else if (isTestFieldResponseHeaders()) {
            toCheck = response.getResponseHeaders();
        } else { // Assume it is the URL
            toCheck = "";
            final URL url = response.getURL();
            if (url != null){
                toCheck = url.toString();
            }
        }
        return toCheck;
    }

    /**
     * Generate the failure reason from the TestType
     *
     * @param stringPattern
     * @return the message for the assertion report
     */
    private String getFailText(String stringPattern, String toCheck) {

        StringBuilder sb = new StringBuilder(200);
        sb.append("Test failed: ");

        if (isScopeVariable()){
            sb.append("variable(").append(getVariableName()).append(')');
        } else if (isTestFieldResponseData()) {
            sb.append("text");
        } else if (isTestFieldResponseCode()) {
            sb.append("code");
        } else if (isTestFieldResponseMessage()) {
            sb.append("message");
        } else if (isTestFieldRequestHeaders()) {
            sb.append("request headers");
        } else if (isTestFieldRequestData()) {
            sb.append("request data");
        } else if (isTestFieldResponseHeaders()) {
            sb.append("headers");
        } else if (isTestFieldResponseDataAsDocument()) {
            sb.append("document");
        } else // Assume it is the URL
        {
            sb.append("URL");
        }

        switch (getTestType()) {
        case CONTAINS:
        case SUBSTRING:
            sb.append(" expected to contain ");
            break;
        case NOT | CONTAINS:
        case NOT | SUBSTRING:
            sb.append(" expected not to contain ");
            break;
        case MATCH:
            sb.append(" expected to match ");
            break;
        case NOT | MATCH:
            sb.append(" expected not to match ");
            break;
        case EQUALS:
            sb.append(" expected to equal ");
            break;
        case NOT | EQUALS:
            sb.append(" expected not to equal ");
            break;
        default:// should never happen...
            sb.append(" expected something using ");
        }

        sb.append("/");

        if (isEqualsType()){
            sb.append(equalsComparisonText(toCheck, stringPattern));
        } else {
            sb.append(stringPattern);
        }

        sb.append("/");
        return sb.toString();
    }

    private static String trunc(final boolean right, final String str) {
        if (str.length() <= EQUALS_SECTION_DIFF_LEN) {
            return str;
        } else if (right) {
            return str.substring(0, EQUALS_SECTION_DIFF_LEN) + EQUALS_DIFF_TRUNC;
        } else {
            return EQUALS_DIFF_TRUNC + str.substring(str.length() - EQUALS_SECTION_DIFF_LEN, str.length());
        }
    }

    /**
     *   Returns some helpful logging text to determine where equality between two strings
     * is broken, with one pointer working from the front of the strings and another working
     * backwards from the end.
     *
     * @param received      String received from sampler.
     * @param comparison    String specified for "equals" response assertion.
     * @return  Two lines of text separated by newlines, and then forward and backward pointers
     *      denoting first position of difference.
     */
    private static StringBuilder equalsComparisonText(final String received, final String comparison)
    {
        final int recLength = received.length();
        final int compLength = comparison.length();
        final int minLength = Math.min(recLength, compLength);

        final StringBuilder text = new StringBuilder(Math.max(recLength, compLength) * 2);
        int firstDiff;
        for (firstDiff = 0; firstDiff < minLength; firstDiff++) {
            if (received.charAt(firstDiff) != comparison.charAt(firstDiff)){
                break;
            }
        }
        final String            startingEqSeq;
        if (firstDiff == 0) {
            startingEqSeq = "";
        } else {
            startingEqSeq = trunc(false, received.substring(0, firstDiff));
        }

        int lastRecDiff = recLength - 1;
        int lastCompDiff = compLength - 1;

        while ((lastRecDiff > firstDiff) && (lastCompDiff > firstDiff)
                && received.charAt(lastRecDiff) == comparison.charAt(lastCompDiff))
        {
            lastRecDiff--;
            lastCompDiff--;
        }
        String compDeltaSeq;
        String endingEqSeq = trunc(true, received.substring(lastRecDiff + 1, recLength));
        String                  recDeltaSeq;
        if (endingEqSeq.length() == 0) {
            recDeltaSeq = trunc(true, received.substring(firstDiff, recLength));
            compDeltaSeq = trunc(true, comparison.substring(firstDiff, compLength));
        }
        else {
            recDeltaSeq = trunc(true, received.substring(firstDiff, lastRecDiff + 1));
            compDeltaSeq = trunc(true, comparison.substring(firstDiff, lastCompDiff + 1));
        }
        final StringBuilder pad = new StringBuilder(Math.abs(recDeltaSeq.length() - compDeltaSeq.length()));
        for (int i = 0; i < pad.capacity(); i++){
            pad.append(' ');
        }

        if (recDeltaSeq.length() > compDeltaSeq.length()){
            compDeltaSeq += pad.toString();
        } else {
            recDeltaSeq += pad.toString();
        }

        text.append("\n\n");
        text.append(RECEIVED_STR);
        text.append(startingEqSeq);
        text.append(DIFF_DELTA_START);
        text.append(recDeltaSeq);
        text.append(DIFF_DELTA_END);
        text.append(endingEqSeq);
        text.append("\n\n");
        text.append(COMPARISON_STR);
        text.append(startingEqSeq);
        text.append(DIFF_DELTA_START);
        text.append(compDeltaSeq);
        text.append(DIFF_DELTA_END);
        text.append(endingEqSeq);
        text.append("\n\n");
        return text;
    }
}
