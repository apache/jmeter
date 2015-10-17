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

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

/**
 * Handles the extractions
 * https://github.com/jayway/JsonPath/blob/master/json-path/src/test/java/com/jayway/jsonpath/ComplianceTest.java
 * @since 2.14
 */
public class JSONManager {
    /**
     * This Map can hardly grow above 10 elements as it is used within JSONPostProcessor to 
     * store the computed JsonPath for the set of JSON Path Expressions.
     * Usually there will be 1 to Maximum 10 elements
     */
    private Map<String, JsonPath> expressionToJsonPath = new HashMap<String, JsonPath>(
            2);

    private JsonPath getJsonPath(String jsonPathExpression) {
        JsonPath jsonPath = expressionToJsonPath.get(jsonPathExpression);
        if (jsonPath == null) {
            jsonPath = JsonPath.compile(jsonPathExpression);
            expressionToJsonPath.put(jsonPathExpression, jsonPath);
        }

        return jsonPath;
    }
    
    public void reset() {
        expressionToJsonPath.clear();
    }

    /**
     * 
     * @param jsonString JSON String from which data is extracted
     * @param jsonPath JSON-PATH expression
     * @return List of String extracted data
     * @throws ParseException
     */
    public List<String> extractWithJsonPath(String jsonString, String jsonPath)
            throws ParseException {
        JsonPath jsonPathParser = getJsonPath(jsonPath);
        return jsonPathParser.read(jsonString);
    }
}
