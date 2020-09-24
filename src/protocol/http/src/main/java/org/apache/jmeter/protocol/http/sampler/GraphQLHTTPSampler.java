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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLHTTPSampler extends HTTPSampler {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(GraphQLHTTPSampler.class);

    private String operationName;
    private String query;
    private String variables;

    public GraphQLHTTPSampler() {
        super();
        setMethod(HTTPConstants.POST);
    }

    @Override
    public String getMethod() {
        return super.getMethod();
    }

    @Override
    public void setMethod(String value) {
        if (StringUtils.equals(value, getMethod())) {
            return;
        }

        super.setMethod(value);
        updateHttpSamplerProperties();
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String value) {
        if (StringUtils.equals(value, getOperationName())) {
            return;
        }

        this.operationName = value;
        updateHttpSamplerProperties();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String value) {
        if (StringUtils.equals(value, getQuery())) {
            return;
        }

        this.query = value;
        updateHttpSamplerProperties();
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String value) {
        if (StringUtils.equals(value, getVariables())) {
            return;
        }

        this.variables = value;
        updateHttpSamplerProperties();
    }

    private void updateHttpSamplerProperties() {
        if (HTTPConstants.GET.equals(getMethod())) {
            updateHttpSamplerPropertiesWithGetMethod();
        } else {
            updateHttpSamplerPropertiesWithPostMethod();
        }
    }

    private void updateHttpSamplerPropertiesWithPostMethod() {
        setPostBodyRaw(true);
        setDoMultipart(false);

        final Arguments args = new Arguments();
        final String postBody = "{\"operationName\":null,\"variables\":{},"
                + "\"query\":\"{\\n  findItemsByKeyword(text: \\\"\\\", offset: 0, limit: 200) {\\n    total\\n }\\n  }\\n\"}";
        final HTTPArgument arg = new HTTPArgument("", postBody);
        arg.setUseEquals(true);
        arg.setAlwaysEncoded(false);
        args.addArgument(arg);
        setArguments(args);
    }

    private void updateHttpSamplerPropertiesWithGetMethod() {
        setPostBodyRaw(false);
    }
}
