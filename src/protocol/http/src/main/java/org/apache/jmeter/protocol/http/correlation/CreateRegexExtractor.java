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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class CreateRegexExtractor {

    private static final String END_OF_LINE = "$"; //$NON-NLS-1$
    private static final String ONE = "1"; //$NON-NLS-1$
    private static final String GROUP_NUMBER = "$1$"; //$NON-NLS-1$
    private static final String REGEX_EXPRESSION = "(.*?)"; //$NON-NLS-1$
    private static final String TRUE = "true"; //$NON-NLS-1$
    private static final String PARANTHESES_OPEN = "("; //$NON-NLS-1$
    private static final String START_OF_LINE = "^"; //$NON-NLS-1$

    public static final String REGEX_EXTRACTOR_VARIABLE_NAME = "RegexExtractor.refname"; //$NON-NLS-1$
    public static final String REGEX_EXTRACTOR_EXPRESSION = "RegexExtractor.expr"; //$NON-NLS-1$
    public static final String REGEX_EXTRACTOR_TEST_NAME = "testname"; //$NON-NLS-1$
    public static final String REGEX_EXTRACTOR_MATCH_NO = "RegexExtractor.match_number"; //$NON-NLS-1$
    public static final String REGEX_EXTRACTOR_USE_HEADER = "RegexExtractor.useHeaders"; //$NON-NLS-1$
    public static final String REGEX_EXTRACTOR_TEMPLATE = "RegexExtractor.template"; //$NON-NLS-1$

    private CreateRegexExtractor() {}

    /**
     * Create Regular Expression Extractor for body parameter
     *
     * @param sampleResult object to get Response data
     * @param parameter    parameter to be correlated
     * @param parameterMap Map containing correlation candidates and their values
     * @return Map for Regex Extractor
     */
    public static Map<String, String> createRegularExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        StringBuilder regexBuffer = new StringBuilder();
        // Create a regex to find the parameter with name and value
        // e.g (name, value) = (_csrf, tokenvalue)
        // regex = ^(.*?)_csrf(.*?)tokenvalue(.*?)$
        if (parameter.contains(PARANTHESES_OPEN)) {
            // get parameter's real name if its alias is provided
            String parameterName = CorrelationFunction.extractVariable(parameter);
            String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameterName) + REGEX_EXPRESSION
                    + Pattern.quote(parameterMap.get(parameter)) + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        } else {
            String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameter) + REGEX_EXPRESSION
                    + Pattern.quote(parameterMap.get(parameter)) + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        }
        // create pattern matcher to match the regex created above in MULTILINE mode
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sampleResult.getResponseDataAsString());
        // Create Regular expression extractor if pattern is matched
        return matchExpressionInResponseBody(matcher, parameter, parameterMap, sampleResult);
    }

    /**
     * Create Regular Expression Extractor for header parameter
     *
     * @param sampleResult object to get Response data
     * @param parameter    parameter to be correlated
     * @param parameterMap Map containing correlation candidates and their values
     * @return Map for Regex Extractor
     */
    public static Map<String, String> createRegularExtractorForHeaderParameter(SampleResult sampleResult,
            String parameter, Map<String, String> parameterMap) {
        StringBuilder regexBuffer = new StringBuilder();
        // Create Pattern matcher for header
        String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameterMap.get(parameter)) + REGEX_EXPRESSION
                + END_OF_LINE;
        regexBuffer.append(regex);
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher headerMatcher = pattern.matcher(sampleResult.getResponseHeaders());
        return matchExpressionInResponseHeader(headerMatcher, parameter, parameterMap, sampleResult);
    }

    /**
     * Match the regular expression against response body and create the regular
     * expression tag for given argument.
     *
     * @param matcher      Regex pattern matcher object
     * @param parameter    Parameter for which extractor is to be created
     * @param parameterMap Map containing correlation candidates and their values
     * @param sampleResult Result of the sampler containing response data
     * @return Map for Regex Extractor for arguments found in Response Body
     */
    private static Map<String, String> matchExpressionInResponseBody(Matcher matcher, String parameter,
            Map<String, String> parameterMap, SampleResult sampleResult) {
        String responseData = sampleResult.getResponseDataAsString();
        Map<String, String> regularExtractor = new HashMap<>();
        if (!matcher.find()) {
            return regularExtractor;
        }
        int startIndex = matcher.start();
        int endIndex = matcher.end();
        if (startIndex >= 0) {
            String matchedData = responseData.substring(startIndex, endIndex);
            String replacedData = null;
            // replace the parameter value in the matched data by (.*?)
            replacedData = matchedData.replace(parameterMap.get(parameter), REGEX_EXPRESSION);
            String parameterName = CorrelationFunction.extractVariable(parameter);
            // parameter name might be exist multiple time in Regex String.
            // e.g replacedData = <input name="_csrf" id="_csrf" value="(.*?)">
            // so check if there are multiple occurrences of parameter name
            // and modify the regex accordingly
            String[] parametersArray = replacedData.split(Pattern.quote(parameterName));
            // intentional reverse loop
            for (int i = parametersArray.length - 1; i >= 0; i--) {
                if (parametersArray[i].contains(REGEX_EXPRESSION)) {
                    String resultString = parameterName + parametersArray[i];
                    regularExtractor.put(REGEX_EXTRACTOR_EXPRESSION, resultString.trim());
                    regularExtractor.put(REGEX_EXTRACTOR_VARIABLE_NAME, parameter);
                    regularExtractor.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                    regularExtractor.put(REGEX_EXTRACTOR_MATCH_NO, ONE);
                    regularExtractor.put(REGEX_EXTRACTOR_TEMPLATE, GROUP_NUMBER);
                    return regularExtractor;
                }
            }
        }
        return regularExtractor;
    }

    /**
     * Match the regular expression against response header and create the regular
     * expression tag for given argument.
     *
     * @param matcher      Regex pattern matcher object
     * @param parameter    Parameter for which extractor is to be created
     * @param parameterMap Map containing correlation candidates and their values
     * @param sampleResult Result of the sampler containing response data
     * @return Map for Regex Extractor for arguments found in Response Header
     */
    private static Map<String, String> matchExpressionInResponseHeader(Matcher headerMatcher, String parameter,
            Map<String, String> parameterMap, SampleResult sampleResult) {
        String responseHeader = sampleResult.getResponseHeaders();
        Map<String, String> regularExtractor = new HashMap<>();
        if (!headerMatcher.find()) {
            return regularExtractor;
        }
        int startIndex = headerMatcher.start();
        int endIndex = headerMatcher.end();
        if (startIndex >= 0) {
            String matchedHeader = responseHeader.substring(startIndex, endIndex);
            String parameterValue = parameterMap.get(parameter);
            String replacedHeader = "";
            if (matchedHeader.endsWith(parameterValue)) {
                // if there is no end boundary
                replacedHeader = matchedHeader.replace(parameterValue, REGEX_EXPRESSION + END_OF_LINE); // $NON-NLS-1$
            } else {
                // take the immediate next character as the end boundary
                replacedHeader = matchedHeader
                        .substring(0, matchedHeader.indexOf(parameterValue) + parameterValue.length() + 1)
                        .replace(parameterValue, REGEX_EXPRESSION);
            }
            regularExtractor.put(REGEX_EXTRACTOR_VARIABLE_NAME, parameter);
            regularExtractor.put(REGEX_EXTRACTOR_EXPRESSION, replacedHeader.trim());
            regularExtractor.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
            regularExtractor.put(REGEX_EXTRACTOR_MATCH_NO, ONE);
            regularExtractor.put(REGEX_EXTRACTOR_USE_HEADER, TRUE);
            regularExtractor.put(REGEX_EXTRACTOR_TEMPLATE, GROUP_NUMBER);
            return regularExtractor;
        }
        return regularExtractor;
    }

    /**
     * Create the Regex Extractor TestElement
     *
     * @param extractor Map containing extractor data
     * @param testElement empty testElement object
     * @return Regex Extractor TestElement
     */
    public static TestElement createRegexExtractorTestElement(Map<String, String> extractor, TestElement testElement) {
        RegexExtractor regexExtractor = (RegexExtractor) testElement;
        regexExtractor.setName(extractor.get(REGEX_EXTRACTOR_VARIABLE_NAME));
        regexExtractor.setRefName(extractor.get(REGEX_EXTRACTOR_VARIABLE_NAME));
        regexExtractor.setRegex(extractor.get(REGEX_EXTRACTOR_EXPRESSION));
        regexExtractor.setMatchNumber(extractor.get(REGEX_EXTRACTOR_MATCH_NO));
        regexExtractor.setTemplate(extractor.get(REGEX_EXTRACTOR_TEMPLATE));
        // use response header if the parameter is found in response header
        if (extractor.get(REGEX_EXTRACTOR_USE_HEADER) != null
                && extractor.get(REGEX_EXTRACTOR_USE_HEADER).equals(TRUE)) {
            regexExtractor.setUseField(TRUE);
        }
        return regexExtractor;
    }

}
