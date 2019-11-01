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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRegexExtractor {

    private static final Logger log = LoggerFactory.getLogger(CreateRegexExtractor.class);

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
     * @param parameter parameter to be correlated
     * @param parameterMap Map containing correlation candidates and their
     *                         values
     * @return List of Maps for Regex Extractor
     */
    public static List<Map<String, String>> createRegularExtractor(SampleResult sampleResult, String parameter,
            Map<String, String> parameterMap) {
        List<Map<String, String>> listOfMap = new ArrayList<>();
        StringBuilder regexBuffer = new StringBuilder();
        // create modified map from parameterMap
        Map<String, String> modifiedMap = new HashMap<>(parameterMap);
        // escape [+,$,{,},:,%3A] characters
        modifyValuesForRegularExpression(modifiedMap, parameter);
        // Create a regex to find the parameter with name and value
        // e.g (name, value) = (_csrf, tokenvalue)
        // regex = ^(.*?)_csrf(.*?)tokenvalue(.*?)$
        if (parameter.contains(PARANTHESES_OPEN)) {
            // get argument's real name if its alias is provided
            String argumentName = CorrelationFunction.extractVariable(parameter);
            String regex = START_OF_LINE + REGEX_EXPRESSION + argumentName + REGEX_EXPRESSION
                    + modifiedMap.get(parameter) + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        } else {
            String regex = START_OF_LINE + REGEX_EXPRESSION + parameter + REGEX_EXPRESSION + modifiedMap.get(parameter)
                    + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        }
        // create pattern matcher to match the regex created above in MULTILINE mode
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sampleResult.getResponseDataAsString());
        // Create Regular expression extractor if pattern is matched
        listOfMap.addAll(matchExpressionInResponseBody(matcher, parameter, parameterMap, sampleResult));
        return listOfMap;
    }

    /**
     * Create Regular Expression Extractor for header parameter
     *
     * @param sampleResult object to get Response data
     * @param parameter parameter to be correlated
     * @param parameterMap Map containing correlation candidates and their
     *                         values
     * @return List of Maps for Regex Extractor
     */
    public static List<Map<String, String>> createRegularExtractorForHeaderParameter(SampleResult sampleResult,
            String parameter, Map<String, String> parameterMap) {
        List<Map<String, String>> listOfMap = new ArrayList<>();
        StringBuilder regexBuffer = new StringBuilder();
        // Create Pattern matcher for header
        String regex = START_OF_LINE + REGEX_EXPRESSION + parameterMap.get(parameter) + REGEX_EXPRESSION + END_OF_LINE;
        regexBuffer.append(regex);
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher headerMatcher = pattern.matcher(sampleResult.getResponseHeaders());
        listOfMap.addAll(matchExpressionInResponseHeader(headerMatcher, parameter, parameterMap, sampleResult));
        return listOfMap;
    }

    /**
     * Match the regular expression against response body and create the regular
     * expression tag for given argument.
     *
     * @param matcher      Regex pattern matcher object
     * @param parameter    Parameter for which extractor is to be created
     * @param parameterMap Map containing correlation candidates and their values
     * @param sampleResult Result of the sampler containing response data
     * @return List of Maps for Regex Extractor for arguments found in Response Body
     */
    private static List<Map<String, String>> matchExpressionInResponseBody(Matcher matcher, String parameter,
            Map<String, String> parameterMap, SampleResult sampleResult) {
        List<Map<String, String>> listOfMap = new ArrayList<>();
        String responseData = sampleResult.getResponseDataAsString();
        while (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            if (startIndex >= 0) {
                Map<String, String> regularExtractors = new HashMap<>();
                String matchedData = responseData.substring(startIndex, endIndex);
                String replacedData = null;
                // replace the parameter value in the matched data by (.*?)
                try {
                    replacedData = matchedData.replace(
                            java.net.URLDecoder.decode(parameterMap.get(parameter), StandardCharsets.UTF_8.name()),
                            REGEX_EXPRESSION);
                } catch (UnsupportedEncodingException e) {
                    log.error("Unable to create Regular Expression Extractor for {}. {}", parameter, e.getMessage());
                    continue;
                }
                String argumentName = CorrelationFunction.extractVariable(parameter);
                // parameter name might be exist multiple time in Regex String.
                // e.g replacedData = <input name="_csrf" id="_csrf" value="(.*?)">
                // so check if there are multiple occurrences of parameter name
                // and modify the regex accordingly
                String[] parametersArray = replacedData.split(argumentName);
                if (parametersArray.length > 1) {
                    for (int i = 0; i < parametersArray.length; i++) {
                        if (parametersArray[i].contains(REGEX_EXPRESSION)) {
                            String resultString = parameter + parametersArray[i];
                            regularExtractors.put(REGEX_EXTRACTOR_EXPRESSION, resultString.trim());
                            regularExtractors.put(REGEX_EXTRACTOR_VARIABLE_NAME, parameter);
                            regularExtractors.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                            regularExtractors.put(REGEX_EXTRACTOR_MATCH_NO, ONE);
                            regularExtractors.put(REGEX_EXTRACTOR_TEMPLATE, GROUP_NUMBER);
                            listOfMap.add(regularExtractors);
                            break;
                        }
                    }
                }
            }
        }
        return listOfMap;
    }

    /**
     * Match the regular expression against response header and create the regular
     * expression tag for given argument.
     *
     * @param matcher      Regex pattern matcher object
     * @param parameter    Parameter for which extractor is to be created
     * @param parameterMap Map containing correlation candidates and their values
     * @param sampleResult Result of the sampler containing response data
     * @return List of Maps for Regex Extractor for arguments found in Response
     *         Header
     */
    private static List<Map<String, String>> matchExpressionInResponseHeader(Matcher headerMatcher, String parameter,
            Map<String, String> parameterMap, SampleResult sampleResult) {
        List<Map<String, String>> listOfMap = new ArrayList<>();
        String responseHeader = sampleResult.getResponseHeaders();
        while (headerMatcher.find()) {
            Map<String, String> regularExtractors = new HashMap<>();
            int startIndex = headerMatcher.start();
            int endIndex = headerMatcher.end();
            if (startIndex >= 0) {
                String matchedHeader = responseHeader.substring(startIndex, endIndex);
                String decodedParameterValue = null;
                try {
                    decodedParameterValue = java.net.URLDecoder.decode(parameterMap.get(parameter),
                            StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    log.error("Unable to create Regular Expression Extractor for {}. {}", parameter, e.getMessage());
                    continue;
                }
                String replacedHeader = null;
                if (matchedHeader.endsWith(decodedParameterValue)) {
                    // if there is no end boundary
                    replacedHeader = matchedHeader.replace(decodedParameterValue, REGEX_EXPRESSION + END_OF_LINE); // $NON-NLS-1$
                } else {
                    // take the immediate next character as the end boundary
                    replacedHeader = matchedHeader
                            .substring(0,
                                    matchedHeader.indexOf(decodedParameterValue) + decodedParameterValue.length() + 1)
                            .replace(decodedParameterValue, REGEX_EXPRESSION);
                }
                regularExtractors.put(REGEX_EXTRACTOR_VARIABLE_NAME, parameter);
                regularExtractors.put(REGEX_EXTRACTOR_EXPRESSION, replacedHeader.trim());
                regularExtractors.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                regularExtractors.put(REGEX_EXTRACTOR_MATCH_NO, ONE);
                regularExtractors.put(REGEX_EXTRACTOR_USE_HEADER, TRUE);
                regularExtractors.put(REGEX_EXTRACTOR_TEMPLATE, GROUP_NUMBER);
                listOfMap.add(regularExtractors);
            }
        }
        return listOfMap;
    }

    /**
     * Method to escape and decode values
     *
     * @param map      containing argument name and value to modify
     * @param argument Parameter name whose value will be modified
     * @return Map Modified map with escaped or decoded values
     */
    public static Map<String, String> modifyValuesForRegularExpression(Map<String, String> map, String argument) {
        Set<Entry<String, String>> entries = map.entrySet();
        try {
            for (Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String valueToModify = map.get(argument);
                if (valueToModify.contains("%3A")) { //$NON-NLS-1$
                    map.put(key, valueToModify.replace("%3A", ":")); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (valueToModify.contains("+") //$NON-NLS-1$
                        || java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).contains("+")) { //$NON-NLS-1$
                    map.put(key, java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).replace("+", "\\+")); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (valueToModify.contains("$") //$NON-NLS-1$
                        || java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).contains("$")) { //$NON-NLS-1$
                    map.put(key, java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).replace("$", "\\$")); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (valueToModify.contains("{") //$NON-NLS-1$
                        || java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).contains("{")) { //$NON-NLS-1$
                    map.put(key, java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).replace("{", "\\{")); //$NON-NLS-1$ //$NON-NLS-2$
                } else if (valueToModify.contains("}") //$NON-NLS-1$
                        || java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).contains("}")) { //$NON-NLS-1$
                    map.put(key, java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()).replace("}", "\\}")); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    map.put(key, java.net.URLDecoder.decode(valueToModify, StandardCharsets.UTF_8.name()));
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to modify response data for regular expression.");
        }
        return map;
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
