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
import java.util.List;
import java.util.Map;

import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.testelement.TestElement;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

public class CreateJsonPathExtractor {

    public static final String JSONPATH_EXTRACTOR_EXPRESSION = "JSONPostProcessor.jsonPathExprs"; //$NON-NLS-1$
    public static final String JSONPATH_EXTRACTOR_MATCH_NO = "JSONPostProcessor.match_numbers"; //$NON-NLS-1$
    public static final String JSONPATH_EXTRACTOR_VARIABLE_NAME = "JSONPostProcessor.referenceNames"; //$NON-NLS-1$

    private static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
    private static final String ONE = "1"; //$NON-NLS-1$
    private static final String TEST_NAME = "testname"; //$NON-NLS-1$

    private CreateJsonPathExtractor() {}

    /**
     * Create JSONPath extractor
     *
     * @param json                    response string
     * @param value                   of Attribute/Text content in XML to create
     *                                XPath
     * @param correlationVariableName alias of the correlated variable
     * @param requestUrl              URL of the request whose response yields the
     *                                parameter required to correlate
     * @param contentType             responseData content type
     * @return JSONPath extractor in a map
     */
    public static Map<String, String> createJsonPathExtractor(String json, String value, String correlationVariableName,
            String requestUrl, String contentType) {
        Map<String, String> jsonPathExtractor = new HashMap<>();
        if (json == null || value == null) {
            throw new IllegalArgumentException("Response Data or value to be searched is null"); //$NON-NLS-1$
        }
        String jsonPathExpression = null;
        try {
            jsonPathExpression = getJsonPath(CorrelationFunction.extractVariable(correlationVariableName), value, json);
        } catch (PathNotFoundException e) {
            // return empty map
            return jsonPathExtractor;
        }
        if (jsonPathExpression == null) {
            // return empty map
            return jsonPathExtractor;
        } else {
            jsonPathExtractor.put(JSONPATH_EXTRACTOR_VARIABLE_NAME, correlationVariableName);
            jsonPathExtractor.put(JSONPATH_EXTRACTOR_EXPRESSION, jsonPathExpression);
            // Match No. = 1, as we are getting first occurrence of the element
            jsonPathExtractor.put(JSONPATH_EXTRACTOR_MATCH_NO, ONE);
            jsonPathExtractor.put(CONTENT_TYPE, contentType);
            jsonPathExtractor.put(TEST_NAME, requestUrl);
            return jsonPathExtractor;
        }

    }

    /**
     * Get JSONPath expression to get the key, value from json
     *
     * @param key   parameter key
     * @param value parameter value
     * @param json  response string
     * @return JSONPath expression or null
     */
    private static String getJsonPath(String key, String value, String json) {
        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
        // No need to provide charset here as json string is already converted according
        // to encoding
        List<String> pathList = JsonPath.using(conf).parse(json).read("$..[?(@." + key + " == '" + value + "')]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String jsonPath = ""; //$NON-NLS-1$
        if (!pathList.isEmpty()) {
            jsonPath = pathList.get(0);
        } else {
            return null;
        }
        jsonPath += "['" + key + "']"; //$NON-NLS-1$ //$NON-NLS-2$
        // check if the created jsonPath is able to parse the expected value
        if (JsonPath.parse(json).read(jsonPath).toString().equals(value)) {
            return jsonPath;
        } else {
            return null;
        }
    }

    /**
     * Create the JSONExtractor TestElement
     *
     * @param extractor Map containing extractor data
     * @param testElement empty testElement object
     * @return JSONExtractor TestElement
     */
    public static TestElement createJsonExtractorTestElement(Map<String, String> extractor, TestElement testElement) {
        JSONPostProcessor jsonExtractor = (JSONPostProcessor) testElement;
        jsonExtractor.setName(extractor.get(JSONPATH_EXTRACTOR_VARIABLE_NAME));
        jsonExtractor.setRefNames(extractor.get(JSONPATH_EXTRACTOR_VARIABLE_NAME));
        jsonExtractor.setJsonPathExpressions(extractor.get(JSONPATH_EXTRACTOR_EXPRESSION));
        jsonExtractor.setMatchNumbers(extractor.get(JSONPATH_EXTRACTOR_MATCH_NO));
        return jsonExtractor;
    }
}
