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

package org.apache.jmeter.protocol.http.util;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities to (de)serialize GraphQL request parameters.
 */
public final class GraphQLRequestParamUtils {

    private static Logger log = LoggerFactory.getLogger(GraphQLRequestParamUtils.class);

    private static Pattern WHITESPACES_PATTERN = Pattern.compile("\\p{Space}+");

    private GraphQLRequestParamUtils() {
    }

    /**
     * Convert the GraphQL request parameters input data to an HTTP POST body string.
     * @param params GraphQL request parameter input data
     * @return an HTTP POST body string converted from the GraphQL request parameters input data
     * @throws RuntimeException if JSON serialization fails for some reason due to any runtime environment issues
     */
    public static String toPostBodyString(final GraphQLRequestParams params) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode postBodyJson = mapper.createObjectNode();
        postBodyJson.put("operationName", StringUtils.trimToNull(params.getOperationName()));

        if (StringUtils.isNotBlank(params.getVariables())) {
            try {
                final ObjectNode variablesJson = mapper.readValue(params.getVariables(), ObjectNode.class);
                postBodyJson.set("variables", variablesJson);
            } catch (JsonProcessingException e) {
                log.error("Ignoring the GraphQL query variables content due to the syntax error: {}",
                        e.getLocalizedMessage());
            }
        }

        postBodyJson.put("query", StringUtils.trim(params.getQuery()));

        try {
            return mapper.writeValueAsString(postBodyJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize JSON for POST body string", e);
        }
    }

    /**
     * Convert the GraphQL Query input string into an HTTP GET request parameter value.
     * @param query the GraphQL Query input string
     * @return an HTTP GET request parameter value converted from the GraphQL Query input string
     */
    public static String queryToGetParamValue(final String query) {
        return RegExUtils.replaceAll(StringUtils.trim(query), WHITESPACES_PATTERN, " ");
    }

    /**
     * Convert the GraphQL Variables JSON input string into an HTTP GET request parameter value.
     * @param variables the GraphQL Variables JSON input string
     * @return an HTTP GET request parameter value converted from the GraphQL Variables JSON input string
     */
    public static String variablesToGetParamValue(final String variables) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            final ObjectNode variablesJson = mapper.readValue(variables, ObjectNode.class);
            return mapper.writeValueAsString(variablesJson);
        } catch (JsonProcessingException e) {
            log.error("Ignoring the GraphQL query variables content due to the syntax error: {}",
                    e.getLocalizedMessage());
        }

        return null;
    }
}
