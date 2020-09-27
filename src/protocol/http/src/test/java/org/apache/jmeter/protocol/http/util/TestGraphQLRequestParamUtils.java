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

import org.apache.jmeter.protocol.http.config.GraphQLRequestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jknack.handlebars.internal.lang3.StringUtils;

public class TestGraphQLRequestParamUtils {

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

    private GraphQLRequestParams params;

    @BeforeEach
    public void setUp() {
        params = new GraphQLRequestParams(OPERATION_NAME, QUERY, VARIABLES);
    }

    @Test
    public void testToPostBodyString() throws Exception {
        assertEquals(EXPECTED_POST_BODY, GraphQLRequestParamUtils.toPostBodyString(params));
    }

    @Test
    public void testQueryToGetParamValue() throws Exception {
        assertEquals(EXPECTED_QUERY_GET_PARAM_VALUE, GraphQLRequestParamUtils.queryToGetParamValue(params.getQuery()));
    }

    @Test
    public void testVariablesToGetParamValue() throws Exception {
        assertEquals(EXPECTED_VARIABLES_GET_PARAM_VALUE,
                GraphQLRequestParamUtils.variablesToGetParamValue(params.getVariables()));
    }
}
