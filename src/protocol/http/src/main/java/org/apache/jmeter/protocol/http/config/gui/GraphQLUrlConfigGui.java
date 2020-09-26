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

package org.apache.jmeter.protocol.http.config.gui;

import javax.swing.JTabbedPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * GraphQL over HTTP Request configuration:
 * <ul>
 * <li>host and port</li>
 * <li>connect and response timeouts</li>
 * <li>path, method, encoding, parameters</li>
 * <li>redirects and keepalive</li>
 * <li>GraphQL query and query variables</li>
 * </ul>
 */
public class GraphQLUrlConfigGui extends UrlConfigGui {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(GraphQLUrlConfigGui.class);

    public static final String OPERATION_NAME = "GraphQLHTTPSampler.operationName";

    public static final String QUERY = "GraphQLHTTPSampler.query";

    public static final String VARIABLES = "GraphQLHTTPSampler.variables";

    private JSyntaxTextArea queryContent;
    private JSyntaxTextArea variablesContent;

    private static final UrlConfigDefaults URL_CONFIG_DEFAULTS = new UrlConfigDefaults();
    static {
        URL_CONFIG_DEFAULTS.setValidMethods(new String[] { HTTPConstants.POST, HTTPConstants.GET });
        URL_CONFIG_DEFAULTS.setDefaultMethod(HTTPConstants.POST);
        URL_CONFIG_DEFAULTS.setAutoRedirects(false);
        URL_CONFIG_DEFAULTS.setFollowRedirects(false);
        URL_CONFIG_DEFAULTS.setUseBrowserCompatibleMultipartMode(false);
        URL_CONFIG_DEFAULTS.setUseKeepAlive(true);
        URL_CONFIG_DEFAULTS.setUseMultipart(false);
        URL_CONFIG_DEFAULTS.setUseMultipartVisible(false);
    }

    /**
     * Constructor which is setup to show the sampler fields for GraphQL over HTTP request.
     */
    public GraphQLUrlConfigGui() {
        super(true, false, false);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final String query = element.getPropertyAsString(QUERY, "");
        queryContent.setText(query);
        final String variables = element.getPropertyAsString(VARIABLES, "");
        variablesContent.setText(variables);
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);

        final String method = element.getPropertyAsString(HTTPSamplerBase.METHOD);
        final String operationName = StringUtils.trim(null);
        final String query = StringUtils.trim(queryContent.getText());
        final String variables = StringUtils.trim(variablesContent.getText());

        element.setProperty(OPERATION_NAME, operationName);
        element.setProperty(QUERY, query);
        element.setProperty(VARIABLES, variables);

        if (HTTPConstants.GET.equals(method)) {
            element.setProperty(HTTPSamplerBase.POST_BODY_RAW, false);
            // TODO
        } else {
            element.setProperty(HTTPSamplerBase.POST_BODY_RAW, true);
            final Arguments postArgs = createGraphQLPostArguments(operationName, query, variables);
            element.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, postArgs));
        }
    }

    @Override
    protected UrlConfigDefaults getUrlConfigDefaults() {
        return URL_CONFIG_DEFAULTS;
    }

    @Override
    protected JTabbedPane getParameterPanel() {
        final AbstractValidationTabbedPane paramPanel = (AbstractValidationTabbedPane) super.getParameterPanel();
        paramPanel.removeAll();
        paramPanel.setValidationEnabled(false);

        queryContent = JSyntaxTextArea.getInstance(30, 50);
        queryContent.setInitialText("");
        paramPanel.add(JMeterUtils.getResString("graphql_query"), JTextScrollPane.getInstance(queryContent));

        variablesContent = JSyntaxTextArea.getInstance(30, 50);
        variablesContent.setLanguage("json");
        variablesContent.setInitialText("");
        paramPanel.add(JMeterUtils.getResString("graphql_variables"), JTextScrollPane.getInstance(variablesContent));

        return paramPanel;
    }

    private Arguments createGraphQLPostArguments(final String operationName, final String query, final String variables) {
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final JsonObject postBodyJson = new JsonObject();
        postBodyJson.addProperty("operationName", operationName);

        if (variables != null && !variables.isEmpty()) {
            try {
                final JsonObject variablesJson = gson.fromJson(variables, JsonObject.class);
                postBodyJson.add("variables", variablesJson);
            } catch (JsonSyntaxException e) {
                log.error("Ignoring the GraphQL query variables content due to the syntax error: {}", e.getLocalizedMessage());
            }
        }

        postBodyJson.addProperty("query", query);

        final HTTPArgument arg = new HTTPArgument("", gson.toJson(postBodyJson));
        arg.setUseEquals(true);
        arg.setAlwaysEncoded(false);
        final Arguments args = new Arguments();
        args.addArgument(arg);

        return args;
    }
}
