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

package org.apache.jmeter.protocol.http.correlation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.RegexExtractorData;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.CorrelationFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRegexExtractor implements CreateExtractorInterface {

    private static final Logger log = LoggerFactory.getLogger(CreateRegexExtractor.class);

    private static final String END_OF_LINE = "$"; //$NON-NLS-1$
    private static final String ONE = "1"; //$NON-NLS-1$
    private static final String GROUP_NUMBER = "$1$"; //$NON-NLS-1$
    private static final String REGEX_EXPRESSION = "(.*?)"; //$NON-NLS-1$
    private static final String PARANTHESES_OPEN = "("; //$NON-NLS-1$
    private static final String START_OF_LINE = "^"; //$NON-NLS-1$

    CreateRegexExtractor() {
    }

    /**
     * Create Regular Expression Extractor for header parameter
     *
     * @param sampleResult   object to get Response data
     * @param parameter      parameter to be correlated
     * @param parameterValue Map containing correlation candidates and their values
     * @return Map for Regex Extractor
     */
    public static RegexExtractorData createRegularExtractorForHeaderParameter(SampleResult sampleResult,
            String parameter, String parameterValue) {
        StringBuilder regexBuffer = new StringBuilder();
        // Create Pattern matcher for header
        String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameterValue) + REGEX_EXPRESSION
                + END_OF_LINE;
        regexBuffer.append(regex);
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher headerMatcher = pattern.matcher(sampleResult.getResponseHeaders());
        return matchExpressionInResponseHeader(headerMatcher, parameter, parameterValue, sampleResult);
    }

    /**
     * Match the regular expression against response body and create the regular
     * expression tag for given argument.
     *
     * @param matcher        Regex pattern matcher object
     * @param parameter      Parameter for which extractor is to be created
     * @param parameterValue Map containing correlation candidates and their values
     * @param sampleResult   Result of the sampler containing response data
     * @return Map for Regex Extractor for arguments found in Response Body
     */
    private static RegexExtractorData matchExpressionInResponseBody(Matcher matcher, String parameter,
            String parameterValue, SampleResult sampleResult) {
        String responseData = sampleResult.getResponseDataAsString();
        RegexExtractorData regularExtractor = null;
        if (!matcher.find()) {
            return regularExtractor;
        }
        int startIndex = matcher.start();
        int endIndex = matcher.end();
        if (startIndex >= 0) {
            String matchedData = responseData.substring(startIndex, endIndex);
            String replacedData = null;
            // replace the parameter value in the matched data by (.*?)
            replacedData = matchedData.replace(parameterValue, REGEX_EXPRESSION);
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
                    regularExtractor = new RegexExtractorData(resultString.trim(), parameter,
                            sampleResult.getSampleLabel(), ONE, GROUP_NUMBER, false);
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
     * @param matcher        Regex pattern matcher object
     * @param parameter      Parameter for which extractor is to be created
     * @param parameterValue Map containing correlation candidates and their values
     * @param sampleResult   Result of the sampler containing response data
     * @return Map for Regex Extractor for arguments found in Response Header
     */
    private static RegexExtractorData matchExpressionInResponseHeader(Matcher headerMatcher, String parameter,
            String parameterValue, SampleResult sampleResult) {
        String responseHeader = sampleResult.getResponseHeaders();
        RegexExtractorData regularExtractor = null;
        if (!headerMatcher.find()) {
            return regularExtractor;
        }
        int startIndex = headerMatcher.start();
        int endIndex = headerMatcher.end();
        if (startIndex >= 0) {
            String matchedHeader = responseHeader.substring(startIndex, endIndex);
            String replacedHeader = "";
            if (matchedHeader.endsWith(parameterValue)) {
                // if there is no end boundary
                replacedHeader = matchedHeader.replace(parameterValue, REGEX_EXPRESSION + END_OF_LINE);
            } else {
                // take the immediate next character as the end boundary
                replacedHeader = matchedHeader
                        .substring(0, matchedHeader.indexOf(parameterValue) + parameterValue.length() + 1)
                        .replace(parameterValue, REGEX_EXPRESSION);
            }
            regularExtractor = new RegexExtractorData(replacedHeader.trim(), parameter, sampleResult.getSampleLabel(),
                    ONE, GROUP_NUMBER, true);
            return regularExtractor;
        }
        return regularExtractor;
    }

    /**
     * Create Regular Expression Extractor for body parameter
     *
     * @param extractorCreatorData ExtractorCreatorData object.
     * @return ExtractorData object.
     */
    @Override
    public ExtractorData createExtractor(ExtractorCreatorData extractorCreatorData) {
        log.debug("Create ExtractorData data from ExtractorCreatorData "+ extractorCreatorData);
        StringBuilder regexBuffer = new StringBuilder();
        SampleResult sampleResult = extractorCreatorData.getSampleResult();
        String parameter = extractorCreatorData.getParameter();
        String parameterValue = extractorCreatorData.getParameterValue();
        // Create a regex to find the parameter with name and value
        // e.g (name, value) = (_csrf, tokenvalue)
        // regex = ^(.*?)_csrf(.*?)tokenvalue(.*?)$
        if (parameter.contains(PARANTHESES_OPEN)) {
            // get parameter's real name if its alias is provided
            String parameterName = CorrelationFunction.extractVariable(parameter);
            String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameterName) + REGEX_EXPRESSION
                    + Pattern.quote(parameterValue) + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        } else {
            String regex = START_OF_LINE + REGEX_EXPRESSION + Pattern.quote(parameter) + REGEX_EXPRESSION
                    + Pattern.quote(parameterValue) + REGEX_EXPRESSION + END_OF_LINE;
            regexBuffer.append(regex);
        }
        // create pattern matcher to match the regex created above in MULTILINE mode
        Pattern pattern = Pattern.compile(regexBuffer.toString(), Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sampleResult.getResponseDataAsString());
        // Create Regular expression extractor if pattern is matched
        return matchExpressionInResponseBody(matcher, parameter, parameterValue, sampleResult);
    }

    /**
     * Create the Regex Extractor TestElement
     *
     * @param extractordata   ExtractorData object.
     * @param testElement TestElement object.
     * @return Regex Extractor TestElement object.
     */
    @Override
    public TestElement createExtractorTestElement(ExtractorData extractordata, TestElement testElement) {
        RegexExtractor regexExtractor = (RegexExtractor) testElement;
        RegexExtractorData extractor = (RegexExtractorData) extractordata;
        regexExtractor.setName(extractor.getRefname());
        regexExtractor.setRefName(extractor.getRefname());
        regexExtractor.setRegex(extractor.getExpr());
        regexExtractor.setMatchNumber(extractor.getMatchNumber());
        regexExtractor.setTemplate(extractor.getTemplate());
        // use response header if the parameter is found in response header
        if (extractor.getUseHeaders()) {
            regexExtractor.setUseField(extractor.getUseHeaders().toString());
        }
        return regexExtractor;
    }

}
