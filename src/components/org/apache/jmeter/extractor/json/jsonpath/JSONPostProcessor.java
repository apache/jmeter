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

package org.apache.jmeter.extractor.json.jsonpath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * JSON-PATH based extractor
 * @since 2.14
 */
public class JSONPostProcessor extends AbstractScopedTestElement implements Serializable, PostProcessor, ThreadListener{

    private static final long serialVersionUID = 1320798545214331506L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String JSON_PATH_EXPRESSIONS = "ULP.json.jsonPathExprs"; // $NON-NLS-1$
    private static final String REFERENCE_NAMES = "ULP.json.referenceNames"; // $NON-NLS-1$
    private static final String DEFAULT_VALUES = "ULP.json.defaultValues"; // $NON-NLS-1$
    private static final String MATCH_NUMBERS = "ULP.json.match_numbers"; // $NON-NLS-1$
    private static final String COMPUTE_CONCATENATION = "ULP.json.compute_concat"; // $NON-NLS-1$
    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    private static final String ALL_SUFFIX = "_ALL"; // $NON-NLS-1$
    private static final String SEPARATOR = ";"; // $NON-NLS-1$
    public static final boolean COMPUTE_CONCATENATION_DEFAULT_VALUE = false;
    
    private static final ThreadLocal<JSONManager> localMatcher = new ThreadLocal<JSONManager>() {
        @Override
        protected JSONManager initialValue() {
            return new JSONManager();
        }
    };
    public void process() {
        JMeterContext context = getThreadContext();
        JMeterVariables vars = context.getVariables();
        String jsonResponse = "";
        if (isScopeVariable()){ 
            jsonResponse = vars.get(getVariableName());
            if(log.isDebugEnabled()) {
                log.debug("JSON Extractor is using variable:"+getVariableName()+" which content is:"+jsonResponse);
            }
        } else {
            SampleResult previousResult = context.getPreviousResult();
            if (previousResult == null) {
                return;
            }
            jsonResponse = previousResult.getResponseDataAsString();
            if(log.isDebugEnabled()) {
                log.debug("JSON Extractor processing result:"+jsonResponse);
            }
        }
        String[] refNames = getRefNames().split(SEPARATOR);
        String[] jsonPathExpressions = getJsonPathExpressions().split(SEPARATOR); 
        String[] defaultValues = getDefaultValues().split(SEPARATOR);
        int[] matchNumbers = getMatchNumbersAsInt(defaultValues.length);

        //jsonResponse = jsonResponse.replaceAll("'", "\""); // $NON-NLS-1$  $NON-NLS-2$

        if (refNames.length != jsonPathExpressions.length || 
                refNames.length != defaultValues.length) {
            log.error("Number of JSON Path variables must match number of default values and json-path expressions, check you use separator ';' if you have many values"); // $NON-NLS-1$
            throw new IllegalArgumentException(JMeterUtils.getResString("jsonpp_error_number_arguments_mismatch_error")); // $NON-NLS-1$
        } else {
            for (int i = 0; i < jsonPathExpressions.length; i++) {
                int matchNumber = matchNumbers[i];
                String refNameFromRefNames = refNames[i].trim();
                String jsonPathFromPathes = jsonPathExpressions[i].trim();
                try {
                    if(jsonResponse.isEmpty()) {
                        vars.put(refNameFromRefNames, defaultValues[i]);
                    } else {
                        
                        List<String> valuesExtracted = localMatcher.get().extractWithJsonPath(jsonResponse, jsonPathFromPathes);
                        // if no values extracted, default value added
                        if (valuesExtracted.isEmpty()) {
                            vars.put(refNameFromRefNames, defaultValues[i]);
                            vars.put(refNameFromRefNames+REF_MATCH_NR, "0");
                            if(matchNumber<0 && getComputeConcatenation()) {
                                log.debug("No value extracted, storing empty in :"+refNameFromRefNames + ALL_SUFFIX);
                                vars.put(refNameFromRefNames + ALL_SUFFIX, "");
                            }
                        } else {
                            // if more than one value extracted, suffix with
                            // "_index"
                            if (valuesExtracted.size() > 1) {
                                if(matchNumber<0) {
                                    int index = 0;
                                    StringBuilder concat = new StringBuilder(getComputeConcatenation()?valuesExtracted.size()*20:1);
                                    for (String stringExtracted : valuesExtracted) {
                                        vars.put(refNameFromRefNames + "_" + (index+1), stringExtracted);
                                        if(getComputeConcatenation()) {
                                            concat.append(stringExtracted).append(",");
                                        }
                                        index++;
                                    }
                                    if(getComputeConcatenation()) {
                                        concat.setLength(concat.length()-1);
                                        vars.put(refNameFromRefNames + ALL_SUFFIX, concat.toString());
                                    }
                                } else if (matchNumber == 0) {
                                    // Random
                                    int matchSize = valuesExtracted.size();
                                    vars.put(refNameFromRefNames, valuesExtracted.get(JMeterUtils.getRandomInt(matchSize)));
                                } else {
                                    // 
                                    if(matchNumber>valuesExtracted.size()) {
                                        if(log.isDebugEnabled()) {
                                            log.debug("matchNumber("+matchNumber + ") exceeds number of items found("+valuesExtracted.size()+"), default value will be used");
                                        }
                                        vars.put(refNameFromRefNames, defaultValues[i]);
                                    } else {
                                        vars.put(refNameFromRefNames, valuesExtracted.get(matchNumber-1));
                                    }
                                }
                            }
                            else {
                                // else just one value extracted
                                vars.put(refNameFromRefNames, valuesExtracted.get(0));
                                if(matchNumber<0 && getComputeConcatenation()) {
                                    vars.put(refNameFromRefNames + ALL_SUFFIX, valuesExtracted.get(0)); 
                                }
                            }
                            vars.put(refNameFromRefNames+REF_MATCH_NR, Integer.toString(valuesExtracted.size()));
                        }
                    }
                } catch (Exception e) {
                    // if something wrong, default value added
                    if (log.isDebugEnabled()) {
                        log.error("Error processing JSON content in "+ getName()+", message:"+e.getLocalizedMessage(),e);
                    } else {
                        log.error("Error processing JSON content in "+ getName()+", message:"+e.getLocalizedMessage());
                        
                    }
                    vars.put(refNameFromRefNames, defaultValues[i]);
                    continue;
                }
            }
        }
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
        localMatcher.get().reset();
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
        if(JOrphanUtils.isBlank(matchNumbersAsString)) {
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
