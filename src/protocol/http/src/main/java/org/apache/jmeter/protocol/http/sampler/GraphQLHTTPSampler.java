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

package org.apache.jmeter.protocol.http.sampler;

import org.apache.jmeter.protocol.http.util.HTTPConstants;

public class GraphQLHTTPSampler extends HTTPSampler {

    private static final long serialVersionUID = 1L;

    public static final String OPERATION_NAME = "GraphQLHTTPSampler.operationName";

    public static final String QUERY = "GraphQLHTTPSampler.query";

    public static final String VARIABLES = "GraphQLHTTPSampler.variables";

    private String operationName;
    private String query;
    private String variables;

    public GraphQLHTTPSampler() {
        super();
        setMethod(HTTPConstants.POST);
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String value) {
        this.operationName = value;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String value) {
        this.query = value;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String value) {
        this.variables = value;
    }
}
