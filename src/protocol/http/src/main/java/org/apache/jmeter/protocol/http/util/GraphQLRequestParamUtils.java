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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

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
     */
    public static String toPostBodyString(final GraphQLRequestParams params) {
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final JsonObject postBodyJson = new JsonObject();
        postBodyJson.addProperty("operationName", StringUtils.trimToNull(params.getOperationName()));

        if (StringUtils.isNotBlank(params.getVariables())) {
            try {
                final JsonObject variablesJson = gson.fromJson(params.getVariables(), JsonObject.class);
                postBodyJson.add("variables", variablesJson);
            } catch (JsonSyntaxException e) {
                log.error("Ignoring the GraphQL query variables content due to the syntax error: {}",
                        e.getLocalizedMessage());
            }
        }

        postBodyJson.addProperty("query", StringUtils.trim(params.getQuery()));
        return gson.toJson(postBodyJson);
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
        final Gson gson = new GsonBuilder().serializeNulls().create();

        try {
            final JsonObject variablesJson = gson.fromJson(variables, JsonObject.class);
            return gson.toJson(variablesJson);
        } catch (JsonSyntaxException e) {
            log.error("Ignoring the GraphQL query variables content due to the syntax error: {}",
                    e.getLocalizedMessage());
        }

        return null;
    }
}
