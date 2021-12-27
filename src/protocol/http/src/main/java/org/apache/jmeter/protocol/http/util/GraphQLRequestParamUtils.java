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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.apache.jmeter.testelement.property.JMeterProperty;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities to (de)serialize GraphQL request parameters.
 */
public final class GraphQLRequestParamUtils {

    public static final String VARIABLES_FIELD = "variables";

    public static final String OPERATION_NAME_FIELD = "operationName";

    public static final String QUERY_FIELD = "query";

    private static final Pattern WHITESPACES_PATTERN = Pattern.compile("\\p{Space}+");

    private static final JsonFactory jsonFactory = new JsonFactory();

    private GraphQLRequestParamUtils() {
    }

    /**
     * Return true if the content type is GraphQL content type (i.e. 'application/json').
     * @param contentType Content-Type value
     * @return true if the content type is GraphQL content type
     */
    public static boolean isGraphQLContentType(final String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return false;
        }
        final ContentType type = ContentType.parse(contentType);
        return ContentType.APPLICATION_JSON.getMimeType().equals(type.getMimeType());
    }

    /**
     * Convert the GraphQL request parameters input data to an HTTP POST body string.
     * @param params GraphQL request parameter input data
     * @return an HTTP POST body string converted from the GraphQL request parameters input data
     * @throws RuntimeException if JSON serialization fails for some reason due to any runtime environment issues
     */
    public static String toPostBodyString(final GraphQLRequestParams params) {
        final StringWriter writer = new StringWriter();

        try (JsonGenerator gen = jsonFactory.createGenerator(writer)) {
            gen.writeStartObject();

            gen.writeStringField(OPERATION_NAME_FIELD, StringUtils.trimToNull(params.getOperationName()));

            if (StringUtils.isNotBlank(params.getVariables())) {
                gen.writeFieldName(VARIABLES_FIELD);
                gen.writeRawValue(StringUtils.trim(params.getVariables()));
            }

            gen.writeStringField(QUERY_FIELD, StringUtils.trim(params.getQuery()));

            gen.writeEndObject();
        } catch (IOException e) {
            throw new IllegalStateException("Error while writing graphql post body " + params, e);
        }

        return writer.toString();
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
        return StringUtils.trimToNull(variables);
    }

    /**
     * Parse {@code postData} and convert it to a {@link GraphQLRequestParams} object if it is a valid GraphQL post data.
     * @param postData post data
     * @param contentEncoding content encoding
     * @return a converted {@link GraphQLRequestParams} object form the {@code postData}
     * @throws IllegalArgumentException if {@code postData} is not a GraphQL post JSON data or not a valid JSON
     * @throws JsonProcessingException if it fails to serialize a parsed JSON object to string
     * @throws UnsupportedEncodingException if it fails to decode parameter value
     */
    public static GraphQLRequestParams toGraphQLRequestParams(byte[] postData, final String contentEncoding)
            throws JsonProcessingException, UnsupportedEncodingException {
        final String encoding = StringUtils.isNotEmpty(contentEncoding) ? contentEncoding
                : EncoderCache.URL_ARGUMENT_ENCODING;

        final ObjectMapper mapper = new ObjectMapper();
        ObjectNode data;

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(postData), encoding)) {
            data = mapper.readValue(reader, ObjectNode.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid json data: " + e.getLocalizedMessage(), e);
        }

        String operationName = null;
        String query;
        String variables = null;

        final JsonNode operationNameNode = data.has(OPERATION_NAME_FIELD) ? data.get(OPERATION_NAME_FIELD) : null;
        if (operationNameNode != null) {
            operationName = getJsonNodeTextContent(operationNameNode, true);
        }

        if (!data.has(QUERY_FIELD)) {
            throw new IllegalArgumentException("Not a valid GraphQL query.");
        }
        final JsonNode queryNode = data.get(QUERY_FIELD);
        query = getJsonNodeTextContent(queryNode, false);
        final String trimmedQuery = StringUtils.trim(query);
        if (!StringUtils.startsWith(trimmedQuery, QUERY_FIELD) && !StringUtils.startsWith(trimmedQuery, "mutation")) {
            throw new IllegalArgumentException("Not a valid GraphQL query.");
        }

        final JsonNode variablesNode = data.has(VARIABLES_FIELD) ? data.get(VARIABLES_FIELD) : null;
        if (variablesNode != null) {
            final JsonNodeType nodeType = variablesNode.getNodeType();
            if (nodeType != JsonNodeType.NULL) {
                if (nodeType == JsonNodeType.OBJECT) {
                    variables = mapper.writeValueAsString(variablesNode);
                } else {
                    throw new IllegalArgumentException("Not a valid object node for GraphQL variables.");
                }
            }
        }

        return new GraphQLRequestParams(operationName, query, variables);
    }

    /**
     * Parse {@code arguments} and convert it to a {@link GraphQLRequestParams} object if it has valid GraphQL HTTP arguments.
     * @param arguments arguments
     * @param contentEncoding content encoding
     * @return a converted {@link GraphQLRequestParams} object form the {@code arguments}
     * @throws IllegalArgumentException if {@code arguments} does not contain valid GraphQL request arguments
     * @throws UnsupportedEncodingException if it fails to decode parameter value
     */
    public static GraphQLRequestParams toGraphQLRequestParams(final Arguments arguments, final String contentEncoding)
            throws UnsupportedEncodingException {
        final String encoding = StringUtils.defaultIfEmpty(contentEncoding, EncoderCache.URL_ARGUMENT_ENCODING);

        String operationName = null;
        String query = null;
        String variables = null;

        for (JMeterProperty prop : arguments) {
            final Argument arg = (Argument) prop.getObjectValue();
            if (!(arg instanceof HTTPArgument)) {
                continue;
            }

            final String name = arg.getName();
            final String metadata = arg.getMetaData();
            final String value = StringUtils.trimToNull(arg.getValue());

            if ("=".equals(metadata) && value != null) {
                final boolean alwaysEncoded = ((HTTPArgument) arg).isAlwaysEncoded();

                if (OPERATION_NAME_FIELD.equals(name)) {
                    operationName = encodedField(value, encoding, alwaysEncoded);
                } else if (QUERY_FIELD.equals(name)) {
                    query = encodedField(value, encoding, alwaysEncoded);
                } else if (VARIABLES_FIELD.equals(name)) {
                    variables = encodedField(value, encoding, alwaysEncoded);
                }
            }
        }

        if (isNoQueryOrMutation(query)) {
            throw new IllegalArgumentException("Not a valid GraphQL query.");
        }

        if (isNoJsonObject(variables)) {
            throw new IllegalArgumentException("Not a valid object node for GraphQL variables.");
        }

        return new GraphQLRequestParams(operationName, query, variables);
    }

    private static String encodedField(final String value, final String encoding, final boolean isEncoded)
            throws UnsupportedEncodingException {
        if (isEncoded) {
            return value;
        }
        return URLDecoder.decode(value, encoding);
    }

    private static boolean isNoJsonObject(String variables) {
        return StringUtils.isNotEmpty(variables)
                && (!StringUtils.startsWith(variables, "{") || !StringUtils.endsWith(variables, "}"));
    }

    private static boolean isNoQueryOrMutation(String query) {
        return StringUtils.isEmpty(query)
                || (!StringUtils.startsWith(query, QUERY_FIELD) && !StringUtils.startsWith(query, "mutation"));
    }

    private static String getJsonNodeTextContent(final JsonNode jsonNode, final boolean nullable) {
        final JsonNodeType nodeType = jsonNode.getNodeType();

        if (nodeType == JsonNodeType.NULL) {
            if (nullable) {
                return null;
            }

            throw new IllegalArgumentException("Not a non-null value node.");
        }

        if (nodeType == JsonNodeType.STRING) {
            return jsonNode.asText();
        }

        throw new IllegalArgumentException("Not a string value node.");
    }
}
