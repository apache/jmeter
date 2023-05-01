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

package org.apache.jmeter.extractor.json.jsonpath;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON-PATH based extractor
 * @since 3.0
 */
public class JSONPostProcessor
        extends AbstractScopedTestElement
        implements Serializable, PostProcessor, ThreadListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(JSONPostProcessor.class);

    private static final String JSON_PATH_EXPRESSIONS = "JSONPostProcessor.jsonPathExprs"; // $NON-NLS-1$
    private static final String REFERENCE_NAMES = "JSONPostProcessor.referenceNames"; // $NON-NLS-1$
    private static final String DEFAULT_VALUES = "JSONPostProcessor.defaultValues"; // $NON-NLS-1$
    private static final String MATCH_NUMBERS = "JSONPostProcessor.match_numbers"; // $NON-NLS-1$
    private static final String COMPUTE_CONCATENATION = "JSONPostProcessor.compute_concat"; // $NON-NLS-1$
    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    private static final String ALL_SUFFIX = "_ALL"; // $NON-NLS-1$

    private static final String JSON_CONCATENATION_SEPARATOR = ","; //$NON-NLS-1$
    private static final String SEPARATOR = ";"; // $NON-NLS-1$
    public static final boolean COMPUTE_CONCATENATION_DEFAULT_VALUE = false;

    private static final ThreadLocal<JSONManager> localMatcher = ThreadLocal.withInitial(JSONManager::new);

    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        JMeterVariables vars = context.getVariables();
        List<String> jsonResponses = extractJsonResponse(context, vars);
        String[] refNames = getRefNames().split(SEPARATOR);
        String[] jsonPathExpressions = getJsonPathExpressions().split(SEPARATOR);
        String[] defaultValues = getDefaultValues().split(SEPARATOR);
        int[] matchNumbers = getMatchNumbersAsInt(defaultValues.length);

        validateSameLengthOfArguments(refNames, jsonPathExpressions, defaultValues);

        for (int i = 0; i < jsonPathExpressions.length; i++) {
            int matchNumber = matchNumbers[i];
            String currentRefName = refNames[i].trim();
            String currentJsonPath = jsonPathExpressions[i].trim();
            clearOldRefVars(vars, currentRefName);
            try {
                if (jsonResponses.isEmpty()) {
                    handleEmptyResponse(vars, defaultValues[i], currentRefName);
                } else {
                    List<Object> extractedValues = extractValues(jsonResponses, currentJsonPath);
                    handleResult(vars, defaultValues[i], matchNumber, currentRefName, extractedValues);
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.error("Error processing JSON content in {}, message: {}", getName(), e.getLocalizedMessage(), e);
                } else {
                    log.error("Error processing JSON content in {}, message: {}", getName(), e.getLocalizedMessage());
                }
                // if something goes wrong, add default value
                vars.put(currentRefName, defaultValues[i]);
            }
        }
    }

    private void handleResult(JMeterVariables vars, String defaultValue, int matchNumber, String currentRefName,
            List<Object> extractedValues) {
        // if no values extracted, default value added
        if (extractedValues.isEmpty()) {
            handleEmptyResult(vars, defaultValue, matchNumber, currentRefName);
        } else {
            handleNonEmptyResult(vars, defaultValue, matchNumber, currentRefName, extractedValues);
        }
    }

    private static List<Object> extractValues(List<String> jsonResponses, String currentJsonPath) throws ParseException {
        List<Object> extractedValues = new ArrayList<>();
        for (String jsonResponse: jsonResponses) {
            extractedValues.addAll(localMatcher.get().extractWithJsonPath(jsonResponse, currentJsonPath));
        }
        return extractedValues;
    }

    private void handleNonEmptyResult(JMeterVariables vars, String defaultValue, int matchNumber, String currentRefName,
            List<Object> extractedValues) {
        // if more than one value extracted, suffix with "_index"
        if (extractedValues.size() > 1) {
            handleListResult(vars, defaultValue, matchNumber, currentRefName, extractedValues);
        } else {
            // else just one value extracted
            handleSingleResult(vars, defaultValue, matchNumber, currentRefName, extractedValues);
        }
        if (matchNumber != 0) {
            vars.put(currentRefName + REF_MATCH_NR, Integer.toString(extractedValues.size()));
        }
    }

    private static void validateSameLengthOfArguments(String[] refNames, String[] jsonPathExpressions,
            String[] defaultValues) {
        if (refNames.length != jsonPathExpressions.length ||
                refNames.length != defaultValues.length) {
            log.error(
                    "Number of JSON Path variables must match number of default values and json-path expressions,"
                    + " check you use separator ';' if you have many values"); // $NON-NLS-1$
            throw new IllegalArgumentException(JMeterUtils
                    .getResString("jsonpp_error_number_arguments_mismatch_error")); // $NON-NLS-1$
        }
    }

    private void handleSingleResult(JMeterVariables vars, String defaultValue,  final int matchNumber, String currentRefName,
            List<Object> extractedValues) {
        String suffix = (matchNumber < 0) ? "_1" : "";
        placeObjectIntoVars(vars, currentRefName + suffix, extractedValues, 0, defaultValue);
        if (matchNumber < 0 && getComputeConcatenation()) {
            vars.put(currentRefName + ALL_SUFFIX, vars.get(currentRefName + suffix));
        }
    }

    private void handleListResult(JMeterVariables vars, String defaultValue, final int matchNumber, String currentRefName,
            List<Object> extractedValues) {
        if (matchNumber < 0) {
            // Extract all
            int index = 1;
            StringBuilder concat =
                    new StringBuilder(getComputeConcatenation()
                            ? extractedValues.size() * 20
                            : 1);
            for (Object extractedObject : extractedValues) {
                String extractedString = StringUtils.defaultString(stringify(extractedObject), defaultValue);
                vars.put(currentRefName + "_" + index,
                        extractedString); //$NON-NLS-1$
                if (getComputeConcatenation()) {
                    concat.append(extractedString)
                            .append(JSONPostProcessor.JSON_CONCATENATION_SEPARATOR);
                }
                index++;
            }
            if (getComputeConcatenation()) {
                concat.setLength(concat.length() - 1);
                vars.put(currentRefName + ALL_SUFFIX, concat.toString());
            }
            return;
        }
        if (matchNumber == 0) {
            // Random extraction
            int matchSize = extractedValues.size();
            int matchNr = JMeterUtils.getRandomInt(matchSize);
            placeObjectIntoVars(vars, currentRefName,
                    extractedValues, matchNr, defaultValue);
            return;
        }
        // extract at position
        if (matchNumber > extractedValues.size()) {
            if(log.isDebugEnabled()) {
                log.debug(
                    "matchNumber({}) exceeds number of items found({}), default value will be used",
                        matchNumber, extractedValues.size());
            }
            vars.put(currentRefName, defaultValue);
        } else {
            placeObjectIntoVars(vars, currentRefName, extractedValues, matchNumber - 1, defaultValue);
        }
    }

    private void handleEmptyResult(JMeterVariables vars, String defaultValue, int matchNumber, String currentRefName) {
        vars.put(currentRefName, defaultValue);
        vars.put(currentRefName + REF_MATCH_NR, "0"); //$NON-NLS-1$
        if (matchNumber < 0 && getComputeConcatenation()) {
            log.debug("No value extracted, storing empty in: {}{}", currentRefName, ALL_SUFFIX);
            vars.put(currentRefName + ALL_SUFFIX, "");
        }
    }

    private void handleEmptyResponse(JMeterVariables vars, String defaultValue, String currentRefName) {
        if(log.isDebugEnabled()) {
            log.debug("Response or source variable is null or empty for {}", getName());
        }
        vars.put(currentRefName, defaultValue);
    }

    private List<String> extractJsonResponse(JMeterContext context, JMeterVariables vars) {
        String jsonResponse = "";
        if (isScopeVariable()) {
            if (log.isDebugEnabled()) {
                log.debug("JSON Extractor is using variable: {}, which content is: {}", getVariableName(), jsonResponse);
            }
            return Arrays.asList(vars.get(getVariableName()));
        } else {
            SampleResult previousResult = context.getPreviousResult();
            if (previousResult != null) {
                List<String> results = getSampleList(previousResult).stream()
                        .map(SampleResult::getResponseDataAsString)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    log.debug("JSON Extractor {} working on Responses: {}", getName(), results);
                }
                return results;
            }
        }
        return Collections.emptyList();
    }

    private static void clearOldRefVars(JMeterVariables vars, String refName) {
        vars.remove(refName + REF_MATCH_NR);
        for (int i=1; vars.get(refName + "_" + i) != null; i++) {
            vars.remove(refName + "_" + i);
        }
    }

    private static void placeObjectIntoVars(JMeterVariables vars, String currentRefName,
            List<Object> extractedValues, int matchNr, String defaultValue) {
        vars.put(currentRefName,
                StringUtils.defaultString(stringify(extractedValues.get(matchNr)), defaultValue));
    }

    private static String stringify(Object obj) {
        return obj == null ? null : obj.toString();
    }

    public String getJsonPathExpressions() {
        return getPropertyAsString(JSON_PATH_EXPRESSIONS);
    }

    public void setJsonPathExpressions(String jsonPath) {
        setProperty(JSON_PATH_EXPRESSIONS, jsonPath);
    }

    public String getRefNames() {
        return getPropertyAsString(REFERENCE_NAMES);
    }

    public void setRefNames(String refName) {
        setProperty(REFERENCE_NAMES, refName);
    }

    public String getDefaultValues() {
        return getPropertyAsString(DEFAULT_VALUES);
    }

    public void setDefaultValues(String defaultValue) {
        setProperty(DEFAULT_VALUES, defaultValue, ""); // $NON-NLS-1$
    }

    public boolean getComputeConcatenation() {
        return getPropertyAsBoolean(COMPUTE_CONCATENATION, COMPUTE_CONCATENATION_DEFAULT_VALUE);
    }

    public void setComputeConcatenation(boolean computeConcatenation) {
        setProperty(COMPUTE_CONCATENATION, computeConcatenation, COMPUTE_CONCATENATION_DEFAULT_VALUE);
    }

    @Override
    public void threadStarted() {
        // NOOP
    }

    @Override
    public void threadFinished() {
        localMatcher.remove();
    }

    public void setMatchNumbers(String matchNumber) {
        setProperty(MATCH_NUMBERS, matchNumber);
    }

    public String getMatchNumbers() {
        return getPropertyAsString(MATCH_NUMBERS);
    }

    public int[] getMatchNumbersAsInt(int arraySize) {

        String matchNumbersAsString = getMatchNumbers();
        int[] result = new int[arraySize];
        if (JOrphanUtils.isBlank(matchNumbersAsString)) {
            Arrays.fill(result, 0);
        } else {
            String[] matchNumbersAsStringArray =
                    matchNumbersAsString.split(SEPARATOR);
            for (int i = 0; i < matchNumbersAsStringArray.length; i++) {
                result[i] = Integer.parseInt(matchNumbersAsStringArray[i].trim());
            }
        }
        return result;
    }
}
