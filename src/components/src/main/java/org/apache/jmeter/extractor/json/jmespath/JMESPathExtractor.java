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
package org.apache.jmeter.extractor.json.jmespath;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;

/**
 * JMESPATH based extractor
 * 
 * @since 5.2
 */
public class JMESPathExtractor extends AbstractScopedTestElement implements Serializable, PostProcessor, ThreadListener {

	private static final long serialVersionUID = 3849270294526207081L;
	
	private static final Logger log = LoggerFactory.getLogger(JMESPathExtractor.class);
    private static final String JMES_PATH_EXPRESSION = "JMESExtractor.jmesPathExpr"; // $NON-NLS-1$
    private static final String REFERENCE_NAME = "JMESExtractor.referenceName"; // $NON-NLS-1$
    private static final String DEFAULT_VALUE = "JMESExtractor.defaultValue"; // $NON-NLS-1$
    private static final String MATCH_NUMBER = "JMESExtractor.matchNumber"; // $NON-NLS-1$
    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    private static final LoadingCache<String, Expression<JsonNode>> JMES_EXTRACTOR_CACHE;
    
    static {
        final int cacheSize = JMeterUtils.getPropDefault("JMESExtractor.parser.cache.size", 400);
        JMES_EXTRACTOR_CACHE = Caffeine.newBuilder().maximumSize(cacheSize).build(new JMESCacheLoader());
    }

    private static final class JMESCacheLoader implements CacheLoader<String, Expression<JsonNode>> {
    	final JmesPath<JsonNode> runtime;
    	public JMESCacheLoader() {
    		runtime = new JacksonRuntime();
    	}
    	
        @Override
        public Expression<JsonNode> load(String jmesPathExpression) throws Exception {
            return runtime.compile(jmesPathExpression);
        }
    }
    
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        JMeterVariables vars = context.getVariables();
        String jsonResponse;
        if (isScopeVariable()) {
            jsonResponse = vars.get(getVariableName());
            if (log.isDebugEnabled()) {
                log.debug("JMESExtractor is using variable: {}, which content is: {}", getVariableName(),
                        jsonResponse);
            }
        } else {
            SampleResult previousResult = context.getPreviousResult();
            if (previousResult == null) {
                return;
            }
            jsonResponse = previousResult.getResponseDataAsString();
            if (log.isDebugEnabled()) {
                log.debug("JMESExtractor {} working on Response: {}", getName(), jsonResponse);
            }
        }
        String refName = getRefName();
        String defaultValue = getDefaultValue();
        int matchNumber = Integer.parseInt(getMatchNumber());
        final String jsonPathExpression = getJmesPathExpression().trim();
        clearOldRefVars(vars, refName);
        try {
            if (StringUtils.isEmpty(jsonResponse)) {
                if (log.isDebugEnabled()) {
                    log.debug("Response or source variable is null or empty for {}", getName());
                }
                vars.put(refName, defaultValue);
            } else {
                JsonNode result = null;
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readValue(jsonResponse, JsonNode.class);
                result = JMES_EXTRACTOR_CACHE.get(jsonPathExpression).search(actualObj);
                if (result.isNull()) {
                    vars.put(refName, defaultValue);
                    vars.put(refName + REF_MATCH_NR, "0"); //$NON-NLS-1$
                    if (matchNumber < 0) {
                        log.debug("No value extracted, storing empty in: {}", refName);
                    }
                } else {
                    List<String> resultList = splitJson(result);
                    // if more than one value extracted, suffix with "_index"
                    if (resultList.size() > 1) {
                        if (matchNumber < 0) {
                            // Extract all
                            int index = 1;
                            for (String extractedString : resultList) {
                                vars.put(refName + "_" + index, extractedString); // $NON-NLS-1$
                                index++;
                            }
                        } else if (matchNumber == 0) {
                            // Random extraction
                            int matchSize = resultList.size();
                            int matchNr = JMeterUtils.getRandomInt(matchSize);
                            placeObjectIntoVars(vars, refName, resultList, matchNr);
                        } else {
                            // extract at position
                            if (matchNumber > resultList.size()) {
                                if (log.isDebugEnabled()) {
                                    log.debug(
                                            "matchNumber({}) exceeds number of items found({}), default value will be used",
                                            matchNumber, resultList.size());
                                }
                                vars.put(refName, defaultValue);
                            } else {
                                placeObjectIntoVars(vars, refName, resultList, matchNumber - 1);
                            }
                        }
                    } else {
                        // else just one value extracted
                        String suffix = (matchNumber < 0) ? "_1" : "";
                        placeObjectIntoVars(vars, refName + suffix, resultList, 0);
                    }
                    vars.put(refName + REF_MATCH_NR, Integer.toString(resultList.size()));
                }
            }
        } catch (Exception e) {
            // if something wrong, default value added
            if (log.isDebugEnabled()) {
                log.debug("Error processing JSON content in {}, message: {}", getName(), e.getLocalizedMessage(), e);
            } else {
                log.debug("Error processing JSON content in {}, message: {}", getName(), e.getLocalizedMessage());
            }
            vars.put(refName, defaultValue);
        }
    }

    public List<String> splitJson(JsonNode jsonNode) 
    		throws JsonParseException, JsonMappingException, IOException {
        List<String> splittedJsonElements = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode individualElement = arrayNode.get(i);
                splittedJsonElements.add(mapper.writeValueAsString(individualElement));
            }
        } else {
            splittedJsonElements.add(mapper.writeValueAsString(jsonNode));
        }
        return splittedJsonElements;
    }

    private void clearOldRefVars(JMeterVariables vars, String refName) {
        vars.remove(refName + REF_MATCH_NR);
        for (int i = 1; vars.get(refName + "_" + i) != null; i++) {
            vars.remove(refName + "_" + i);
        }
    }

    private void placeObjectIntoVars(JMeterVariables vars, String refName, List<String> extractedValues, int matchNr) {
        vars.put(refName, extractedValues.get(matchNr));
    }

    public String getJmesPathExpression() {
        return getPropertyAsString(JMES_PATH_EXPRESSION);
    }

    public void setJmesPathExpression(String jsonPath) {
        setProperty(JMES_PATH_EXPRESSION, jsonPath);
    }

    public String getRefName() {
        return getPropertyAsString(REFERENCE_NAME);
    }

    public void setRefName(String refName) {
        setProperty(REFERENCE_NAME, refName);
    }

    public String getDefaultValue() {
        return getPropertyAsString(DEFAULT_VALUE);
    }

    public void setDefaultValue(String defaultValue) {
        setProperty(DEFAULT_VALUE, defaultValue, ""); // $NON-NLS-1$
    }

    @Override
    public void threadStarted() {
        // NOOP
    }

    @Override
    public void threadFinished() {
        JMES_EXTRACTOR_CACHE.cleanUp();
    }

    public void setMatchNumbers(String matchNumber) {
        setProperty(MATCH_NUMBER, matchNumber);
    }

    public String getMatchNumber() {
        return getPropertyAsString(MATCH_NUMBER);
    }
}
