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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class TestGraphQLRequestParamUtils {

    private static final String OPERATION_NAME = "";

    private static final String QUERY =
            "query($id: ID!) {\n"
            + "  droid(id: $id) {\n"
            + "    id\n"
            + "    name\n"
            + "    friends {\n"
            + "      id\n"
            + "      name\n"
            + "      appearsIn\n"
            + "    }\n"
            + "  }\n"
            + "}\n";

    private static final String VARIABLES =
            "{\n"
            + "  \"id\": \"2001\"\n"
            + "}\n";

    private static final String EXPECTED_QUERY_GET_PARAM_VALUE =
            "query($id: ID!) { droid(id: $id) { id name friends { id name appearsIn } } }";

    private static final String EXPECTED_VARIABLES_GET_PARAM_VALUE = "{\"id\":\"2001\"}";

    private static final String EXPECTED_POST_BODY =
            "{"
            + "\"operationName\":null,"
            + "\"variables\":" + EXPECTED_VARIABLES_GET_PARAM_VALUE + ","
            + "\"query\":\"" + StringUtils.replace(QUERY.trim(), "\n", "\\n") + "\""
            + "}";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GraphQLRequestParams params;

    @BeforeEach
    void setUp() {
        params = new GraphQLRequestParams(OPERATION_NAME, QUERY, VARIABLES);
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/json", "application/json;charset=utf-8", "application/json; charset=utf-8" })
    void testIsGraphQLContentType(String contentType) {
        assertTrue(GraphQLRequestParamUtils.isGraphQLContentType(contentType));
    }

    // null can't be used in a ValueSource directly, so we need to use a MethodSource
    static Stream<String> invalidContentTypes() {
        return Stream.of("application/vnd.api+json", "application/json-patch+json", "",
            null);
    }

    @ParameterizedTest
    @MethodSource("invalidContentTypes")
    void testInvalidGraphQLContentType(String contentType) {
        assertFalse(GraphQLRequestParamUtils.isGraphQLContentType(contentType));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> postBodyFieldNameAndJsonNodes() throws Exception {
        final JsonNode expectedPostBodyJson = objectMapper.readTree(EXPECTED_POST_BODY);
        final JsonNode actualPostBodyJson = objectMapper.readTree(
                GraphQLRequestParamUtils.toPostBodyString(new GraphQLRequestParams(OPERATION_NAME, QUERY, VARIABLES)));
        return Stream.of(
                arguments(GraphQLRequestParamUtils.OPERATION_NAME_FIELD, expectedPostBodyJson, actualPostBodyJson),
                arguments(GraphQLRequestParamUtils.VARIABLES_FIELD, expectedPostBodyJson, actualPostBodyJson),
                arguments(GraphQLRequestParamUtils.QUERY_FIELD, expectedPostBodyJson, actualPostBodyJson));
    }

    @ParameterizedTest
    @MethodSource("postBodyFieldNameAndJsonNodes")
    void testFieldInJsonFromToPostBodyString(String fieldName, JsonNode expectedNode, JsonNode actualNode) {
        assertEquals(expectedNode.get(fieldName), actualNode.get(fieldName),
                "The value of the '" + fieldName + "' field doesn't match in " + actualNode);
    }

    @Test
    void testQueryToGetParamValue() {
        assertEquals(EXPECTED_QUERY_GET_PARAM_VALUE, GraphQLRequestParamUtils.queryToGetParamValue(params.getQuery()));
    }

    @Test
    void testVariablesToGetParamValue() throws Exception {
        assertEquals(objectMapper.readTree(EXPECTED_VARIABLES_GET_PARAM_VALUE),
                objectMapper.readTree(GraphQLRequestParamUtils.variablesToGetParamValue(params.getVariables())));
    }

    @Test
    void testToGraphQLRequestParamsWithPostData() throws Exception {
        GraphQLRequestParams params = GraphQLRequestParamUtils
                .toGraphQLRequestParams(EXPECTED_POST_BODY.getBytes(StandardCharsets.UTF_8), null);
        assertNull(params.getOperationName());
        assertEquals(QUERY.trim(), params.getQuery());
        assertEquals(EXPECTED_VARIABLES_GET_PARAM_VALUE, params.getVariables());

        params = GraphQLRequestParamUtils.toGraphQLRequestParams(
                "{\"operationName\":\"op1\",\"variables\":{\"id\":123},\"query\":\"query { droid { id }}\"}"
                        .getBytes(StandardCharsets.UTF_8),
                null);
        assertEquals("op1", params.getOperationName());
        assertEquals("query { droid { id }}", params.getQuery());
        assertEquals("{\"id\":123}", params.getVariables());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "{}"})
    void testInvalidJsonData(String postDataAsString) {
        byte[] postData = postDataAsString.getBytes(StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class,
                () -> GraphQLRequestParamUtils.toGraphQLRequestParams(postData, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"query\":\"select * from emp\"}",
            "{\"operationName\":{\"id\":123},\"query\":\"query { droid { id }}\"}",
            "{\"variables\":\"r2d2\",\"query\":\"query { droid { id }}\"}" })
    void testInvalidGraphQueryParam(String postDataAsString) {
        byte[] postData = postDataAsString.getBytes(StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class,
                () -> GraphQLRequestParamUtils.toGraphQLRequestParams(postData, null));
    }

    @Test
    void testToGraphQLRequestParamsWithHttpArguments() throws Exception {
        Arguments args = new Arguments();
        args.addArgument(new HTTPArgument("query", "query { droid { id }}", "=", false));
        GraphQLRequestParams params = GraphQLRequestParamUtils.toGraphQLRequestParams(args, null);
        assertNull(params.getOperationName());
        assertEquals("query { droid { id }}", params.getQuery());
        assertNull(params.getVariables());

        args = new Arguments();
        args.addArgument(new HTTPArgument("operationName", "op1", "=", false));
        args.addArgument(new HTTPArgument("query", "query { droid { id }}", "=", false));
        args.addArgument(new HTTPArgument("variables", "{\"id\":123}", "=", false));
        params = GraphQLRequestParamUtils.toGraphQLRequestParams(args, null);
        assertEquals("op1", params.getOperationName());
        assertEquals("query { droid { id }}", params.getQuery());
        assertEquals("{\"id\":123}", params.getVariables());

        args = new Arguments();
        args.addArgument(new HTTPArgument("query", "query+%7B+droid+%7B+id+%7D%7D", "=", true));
        params = GraphQLRequestParamUtils.toGraphQLRequestParams(args, null);
        assertNull(params.getOperationName());
        assertEquals("query { droid { id }}", params.getQuery());
        assertNull(params.getVariables());

        args = new Arguments();
        args.addArgument(new HTTPArgument("query", "query%20%7B%20droid%20%7B%20id%20%7D%7D", "=", true));
        params = GraphQLRequestParamUtils.toGraphQLRequestParams(args, null);
        assertNull(params.getOperationName());
        assertEquals("query { droid { id }}", params.getQuery());
        assertNull(params.getVariables());
    }

    @Test
    void testMissingParams() {
        Arguments args = new Arguments();
        assertThrows(IllegalArgumentException.class,
                () -> GraphQLRequestParamUtils.toGraphQLRequestParams(args, null));
    }

    @Test
    void testInvalidQueryParam() {
        Arguments args = new Arguments();
        args.addArgument(new HTTPArgument("query", "select * from emp", "=", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphQLRequestParamUtils.toGraphQLRequestParams(args, null));
    }

    @Test
    void testInvalidQueryParamVariables() {
        Arguments args = new Arguments();
        args.addArgument(new HTTPArgument("query", "query { droid { id }}", "=", false));
        args.addArgument(new HTTPArgument("variables", "r2d2", "=", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphQLRequestParamUtils.toGraphQLRequestParams(args, null));
    }
}
