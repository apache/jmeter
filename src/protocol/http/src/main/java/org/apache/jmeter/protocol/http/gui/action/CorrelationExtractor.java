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

package org.apache.jmeter.protocol.http.gui.action;

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

import org.apache.jmeter.extractor.CreateCssSelectorExtractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.CorrelationRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorrelationExtractor {

    private static final Logger log = LoggerFactory.getLogger(CorrelationExtractor.class);

    // Initialize constants variables
    static final String CSS_SELECTOR_VARIABLE_NAME = "HtmlExtractor.refname";
    static final String CSS_SELECTOR_EXPRESSION = "HtmlExtractor.expr";
    static final String CSS_SELECTOR_ATTRIBUTE = "HtmlExtractor.attribute";
    static final String CSS_SELECTOR_MATCH_NO = "HtmlExtractor.match_number";

    static final String REGEX_EXTRACTOR_VARIABLE_NAME = "RegexExtractor.refname";
    static final String REGEX_EXTRACTOR_EXPRESSION = "RegexExtractor.expr";
    static final String REGEX_EXTRACTOR_TEST_NAME = "testname";
    static final String REGEX_EXTRACTOR_MATCH_NO = "RegexExtractor.match_number";
    static final String ONE = "1";

    static final String AMPERSAND = "&";
    static final String UNDERSCORE = "_";
    static final String EQUAL = "=";
    static final String HTTP_SAMPLER_PROXY = "HTTPSamplerProxy";
    static final String TEST_NAME = "testname";
    static final String HASHTREE = "<hashTree/>";
    static final String[] queryParameterRegex = new String[] {
            "[0-9a-z]*-[0-9a-z]*-[0-9a-z]*-[0-9a-z]*-[0-9a-z]*", "([A-Z])" + "\\" + "w+", "([0-9a-z])\\w+" };

    static final String REGEX = "(.*)[\n]*(.*)";
    static final String REGEX_EXPRESSION = "(.*?)";
    static final String ALL_CONTENT_MATCHER = "(.*)";
    static final String DELIMITER = "|";
    static final String CONTENTTYPE = "text/html";
    static final String TEXT_HTML = "text/html";
    static final String APPLICATION_JSON = "application/json";
    static List<Map<String, String>> listOfMap = new ArrayList<>();

    private CorrelationExtractor() {}

    /**
     * Read the response and create the extractor tags.
     *
     * @param arguments
     * @param bodyParameterMap
     */
    public static void readResponse(List<String> arguments,
            Map<String, String> bodyParameterMap) {

        readBufferObject(arguments, bodyParameterMap);
    }

    /**
     * Iterate the buffer object and prepare the list of extractor tag.
     *
     * @param arguments
     * @param bodyParameterMap
     */
    public static void readBufferObject(List<String> arguments,
            Map<String, String> bodyParameterMap) {
        log.debug("Start Processing sample results in buffer object.");
        for (Object sampler : CorrelationRecorder.buffer) {
            SampleResult sampleResult = (SampleResult) sampler;
            String contentType = sampleResult.getContentType();

            // create [CSS|HTML] extractor tag list
            // add later support for JSON and xpath2
            if (contentType.contains(TEXT_HTML)) {
                log.debug("Try to create HTML extractor for arguments in response of {}", sampleResult.getSampleLabel());
                createExtractor(sampleResult,arguments, bodyParameterMap);
                // create regular expression tags when
                // no content-type is present
            } else {
                log.debug("Try to create Regex extractor for arguments in response of {}", sampleResult.getSampleLabel());
                createRegularExtractor(sampleResult, arguments, bodyParameterMap);
            }

        }
        // update the JMX file with
        // extractor tags
        log.debug("Processing sample results in buffer object ended.");
        updateJmxFile(arguments, bodyParameterMap);
    }

    /**
     * create the extractor tags based on the content type. eg. {if
     * contentType:text/html then create the HTML extractor tag.}
     *
     * @param sampleResult
     * @param arguments
     * @param bodyParameterMap
     */
    public static void createExtractor(SampleResult sampleResult,List<String> arguments, Map<String, String> bodyParameterMap) {

        // TODO-support more extractors like JSON Path
        // extractor and XPath2 extractor.
        for (String argument : arguments) {
            try {
                Map<String, String> htmlExtractor = CreateCssSelectorExtractor.createCssSelectorExtractor(
                        sampleResult.getResponseDataAsString(),
                        bodyParameterMap.get(argument),
                        argument,
                        sampleResult.getSampleLabel(),
                        sampleResult.getContentType());
                if (htmlExtractor != null && htmlExtractor.size() > 0) {
                    listOfMap.add(htmlExtractor);
                    log.debug("HTML Extractor created for {} in {}", argument, sampleResult.getSampleLabel());
                }

            } catch (Exception e) {
                log.error("Unable to create HTML Extractor for argument {}, {}", argument, e.getMessage());
            }
        }
    }

    /**
     * If no content type is available then this method create the regular
     * extractor tag.
     *
     * @param sampleResult
     * @param arguments
     * @param bodyParameterMap
     */
    public static void createRegularExtractor(SampleResult sampleResult, List<String> arguments,
            Map<String, String> bodyParameterMap) {

        for (String argument : arguments) {

            StringBuilder regexBuffer = new StringBuilder();

            // create temp map from bodyParameterMap
            Map<String, String> tempMap = new HashMap<>(bodyParameterMap);

            // call modifyValuesForRegularExpression() to escape the
            // these [+,$,{,},:,%3A] characters
            tempMap = modifyValuesForRegularExpression(tempMap, argument);

            if (argument.indexOf(UNDERSCORE) >= 0) {
                String tempVariable = CorrelationFunction.extractVariable(argument);
                String regex = tempVariable + REGEX + tempMap.get(argument) + ALL_CONTENT_MATCHER;
                regexBuffer.append(regex);
            } else {
                String regex = argument + REGEX + tempMap.get(argument) + ALL_CONTENT_MATCHER;
                regexBuffer.append(regex);
            }

            // create pattern matcher
            Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.DOTALL);
            Matcher matcher = pattern.matcher(sampleResult.getResponseDataAsString());

            // find the correlation parameter value in
            // response data.
            matchExpressionInResponseBody(matcher, argument, bodyParameterMap, sampleResult);

            // find the correlation parameter value in
            // header data.
            Matcher headerMatcher = pattern.matcher(sampleResult.getResponseHeaders());
            matchExpressionInResponseHeader(headerMatcher, argument, bodyParameterMap,
                    sampleResult);

        }
    }

    /**
     * It is will match the regular expression against response body and create
     * the regular expression tag for given argument.
     *
     * @param matcher
     * @param argument
     * @param bodyParameterMap
     * @param sampleResult
     */
    private static void matchExpressionInResponseBody(Matcher matcher, String argument,
            Map<String, String> bodyParameterMap, SampleResult sampleResult) {

        String responseData = sampleResult.getResponseDataAsString();
        try {
            while (matcher.find()) {

                int startIndex = matcher.start();
                int endIndex = matcher.end();
                if (startIndex + 1 > 1) {
                    Map<String, String> regularExtractors = new HashMap<>();
                    String subStr = responseData.substring(startIndex, endIndex);

                    String replaceStr = subStr.replace(
                            java.net.URLDecoder.decode(bodyParameterMap.get(argument), StandardCharsets.UTF_8.name()),
                            REGEX_EXPRESSION);

                    String tempVariable = CorrelationFunction.extractVariable(argument);

                    // variable name might be exist multiple time
                    // in Regex String.
                    String[] argumentsArray = replaceStr.split(tempVariable);

                    if (argumentsArray.length > 1) {
                        for (int i = 0; i < argumentsArray.length; i++) {
                            if (argumentsArray[i].indexOf(REGEX_EXPRESSION) >= 0) {
                                String resultString = argument + argumentsArray[i];
                                String str = resultString.substring(0,
                                        resultString.indexOf(REGEX_EXPRESSION) + 10);

                                regularExtractors.put(REGEX_EXTRACTOR_VARIABLE_NAME, argument);
                                regularExtractors.put(REGEX_EXTRACTOR_EXPRESSION, str.trim());
                                regularExtractors.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                                regularExtractors.put(REGEX_EXTRACTOR_MATCH_NO, "1");
                                listOfMap.add(regularExtractors);
                            }
                        }
                    } else {
                        String str = replaceStr.substring(0,
                                replaceStr.indexOf(REGEX_EXPRESSION) + 10);

                        regularExtractors.put(REGEX_EXTRACTOR_VARIABLE_NAME, argument);
                        regularExtractors.put(REGEX_EXTRACTOR_EXPRESSION, str.trim());
                        regularExtractors.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                        regularExtractors.put(REGEX_EXTRACTOR_MATCH_NO, "1");
                        listOfMap.add(regularExtractors);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to create Regular Expression Extractor for {}. {}", argument, e.getMessage());
        }
    }

    /**
     * It is will match the regular expression against response header and
     * create the regular expression tag for given argument.
     *
     * @param headerMatcher
     * @param argument
     * @param bodyParameterMap
     * @param sampleResult
     */
    private static void matchExpressionInResponseHeader(Matcher headerMatcher, String argument,
            Map<String, String> bodyParameterMap, SampleResult sampleResult) {

        String responseHeader = sampleResult.getResponseHeaders();
        try {
            while (headerMatcher.find()) {
                Map<String, String> regularExtractors = new HashMap<>();
                int startIndex = headerMatcher.start();
                int endIndex = headerMatcher.end();

                if (startIndex + 1 > 1) {
                    String subStr = responseHeader.substring(startIndex, endIndex);
                    String replaceStr = subStr.replace(
                            java.net.URLDecoder.decode(bodyParameterMap.get(argument), StandardCharsets.UTF_8.name()),
                            REGEX_EXPRESSION);

                    regularExtractors.put(REGEX_EXTRACTOR_VARIABLE_NAME, argument);
                    regularExtractors.put(REGEX_EXTRACTOR_EXPRESSION, replaceStr.trim());
                    regularExtractors.put(REGEX_EXTRACTOR_TEST_NAME, sampleResult.getSampleLabel());
                    regularExtractors.put(REGEX_EXTRACTOR_MATCH_NO, "1");
                }

            }
        } catch (Exception e) {
            log.error("Unable to create the regular exptractor for {}", argument);
        }
    }

    /**
     * @param map
     * @param argument
     * @return Map Modified map with escaped or decoded values
     */

    public static Map<String, String> modifyValuesForRegularExpression(Map<String, String> map, String argument) {

        Set<Entry<String, String>> entries = map.entrySet();

        try {
            for (Entry<String, String> entry : entries) {

                String key = entry.getKey();
                String temp = map.get(argument);
                if (temp.contains("%3A")) {
                    map.put(key, temp.replace("%3A", ":"));
                } else if (temp.contains("+")
                        || java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).contains("+")) {
                    map.put(key, java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).replace("+", "\\+"));
                } else if (temp.contains("$")
                        || java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).contains("$")) {
                    map.put(key, java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).replace("$", "\\$"));
                } else if (temp.contains("{")
                        || java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).contains("{")) {
                    map.put(key, java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).replace("{", "\\{"));
                } else if (temp.contains("}")
                        || java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).contains("}")) {
                    map.put(key, java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()).replace("}", "\\}"));
                } else {
                    map.put(key, java.net.URLDecoder.decode(temp, StandardCharsets.UTF_8.name()));
                }

            }
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to parse the data.");
        }
        return map;
    }

    /**
     * create the extractor based on the extractor class.
     *
     * @param guiPackage
     * @param extractorTypeClassName
     * @param extractor
     * @return TestElement object.
     */
    static TestElement createExtractor(GuiPackage guiPackage, String extractorTypeClassName,
            Map<String, String> extractor) {

        TestElement testElement = guiPackage.createTestElement(extractorTypeClassName);

        // TODO Add more extractors here
        // create the HtmlExtractor
        if (testElement instanceof HtmlExtractor) {
            HtmlExtractor temp = (HtmlExtractor) testElement;
            temp.setName(extractor.get(CSS_SELECTOR_VARIABLE_NAME));
            temp.setRefName(extractor.get(CSS_SELECTOR_VARIABLE_NAME));
            temp.setExpression(extractor.get(CSS_SELECTOR_EXPRESSION));
            temp.setAttribute(extractor.get(CSS_SELECTOR_ATTRIBUTE));
            temp.setMatchNumber(extractor.get(CSS_SELECTOR_MATCH_NO));
            return temp;
            // create the RegexExtractor
        } else if (testElement instanceof RegexExtractor) {
            RegexExtractor regexExtractor = (RegexExtractor) testElement;
            regexExtractor.setRefName(extractor.get(REGEX_EXTRACTOR_VARIABLE_NAME));
            regexExtractor.setRegex(extractor.get(REGEX_EXTRACTOR_EXPRESSION));
            regexExtractor.setDefaultValue(ONE);
            regexExtractor.setMatchNumber(ONE);
            return regexExtractor;
        }
        return null;

    }

    /**
     * @param arguments
     * @param bodyParameterMap
     */
    private static void updateJmxFile(
            List<String> arguments, Map<String, String> bodyParameterMap) {

        try {
            if (!listOfMap.isEmpty()) {
                Correlation.updateJxmFileWithRegularExtractors(listOfMap, arguments, bodyParameterMap);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Could not update the JMX file. {}", e.getMessage());
        }

    }

}
