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

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Basic URL / HTTP Request configuration:
 * <ul>
 * <li>host and port</li>
 * <li>connect and response timeouts</li>
 * <li>path, method, encoding, parameters</li>
 * <li>redirects and keepalive</li>
 * </ul>
 */
public class GraphQLUrlConfigGui extends UrlConfigGui {

    private static final long serialVersionUID = 1L;

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
     * Constructor which is setup to show HTTP implementation, raw body pane and
     * sampler fields.
     */
    public GraphQLUrlConfigGui() {
        super(true, false, false);
    }

    @Override
    protected UrlConfigDefaults getUrlConfigDefaults() {
        return URL_CONFIG_DEFAULTS;
    }

    @Override
    protected JTabbedPane getParameterPanel() {
        final JTabbedPane paramPanel = super.getParameterPanel();
        paramPanel.removeAll();

        queryContent = JSyntaxTextArea.getInstance(30, 50);
        queryContent.setInitialText("");
        paramPanel.add(JMeterUtils.getResString("graphql_query"), JTextScrollPane.getInstance(queryContent));

        variablesContent = JSyntaxTextArea.getInstance(30, 50);
        variablesContent.setLanguage("json");
        variablesContent.setInitialText("");
        paramPanel.add(JMeterUtils.getResString("graphql_variables"), JTextScrollPane.getInstance(variablesContent));

        return paramPanel;
    }
}
