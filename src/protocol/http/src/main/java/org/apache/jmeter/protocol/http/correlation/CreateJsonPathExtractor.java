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

import java.util.List;

import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.JsonPathExtractorData;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;

public class CreateJsonPathExtractor implements CreateExtractorInterface {

    private static final Logger log = LoggerFactory.getLogger(CreateJsonPathExtractor.class);

    private static final String ONE = "1"; //$NON-NLS-1$

    CreateJsonPathExtractor() {
    }

    /**
     * Create JSONPath extractor
     *
     * @param extractorCreatorData ExtractorCreatorData object.
     * @return ExtractorData object.
     */
    @Override
    public ExtractorData createExtractor(ExtractorCreatorData extractorCreatorData) {
        log.debug("Create ExtractorData data from ExtractorCreatorData "+ extractorCreatorData);
        JsonPathExtractorData jsonPathExtractor = null;
        String json = extractorCreatorData.getSampleResult().getResponseDataAsString();
        String value = extractorCreatorData.getParameterValue();
        String correlationVariableName = extractorCreatorData.getParameter();
        String requestUrl = extractorCreatorData.getSampleResult().getSampleLabel();
        String contentType = extractorCreatorData.getSampleResult().getContentType();
        if (json == null || value == null) {
            throw new IllegalArgumentException("Response Data or value to be searched is null"); //$NON-NLS-1$
        }
        String jsonPathExpression = null;
        try {
            jsonPathExpression = getJsonPath(CorrelationFunction.extractVariable(correlationVariableName), value, json);
        } catch (JsonPathException e) {
            // return null
            return jsonPathExtractor;
        }
        if (jsonPathExpression != null) {
            // Match No. = 1, as we are getting first occurrence of the element
            jsonPathExtractor = new JsonPathExtractorData(correlationVariableName, jsonPathExpression, ONE, contentType,
                    requestUrl);
        }
        return jsonPathExtractor;
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
     * @param extractordata  ExtractorData object.
     * @param  testElement TestElement object.
     * @return JSONExtractor TestElement object.
     */
    @Override
    public TestElement createExtractorTestElement(ExtractorData extractordata, TestElement testElement) {
        JSONPostProcessor jsonExtractor = (JSONPostProcessor) testElement;
        JsonPathExtractorData extractor = (JsonPathExtractorData) extractordata;
        jsonExtractor.setName(extractor.getRefname());
        jsonExtractor.setRefNames(extractor.getRefname());
        jsonExtractor.setJsonPathExpressions(extractor.getExpr());
        jsonExtractor.setMatchNumbers(extractor.getMatchNumber());
        return jsonExtractor;
    }
}
