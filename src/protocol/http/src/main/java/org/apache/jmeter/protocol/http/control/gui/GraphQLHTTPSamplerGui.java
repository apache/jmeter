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

package org.apache.jmeter.protocol.http.control.gui;

import javax.swing.JTabbedPane;

import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.protocol.http.config.gui.GraphQLUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.util.JMeterUtils;

@TestElementMetadata(labelResource = "graphql_http_sampler_title")
public class GraphQLHTTPSamplerGui extends HttpTestSampleGui {

    private static final long serialVersionUID = 1L;

    public GraphQLHTTPSamplerGui() {
        super(true);
    }

    // Use this instead of getLabelResource() otherwise getDocAnchor() below does not work
    @Override
    public String getStaticLabel() {
        return JMeterUtils.getResString("graphql_http_sampler_title"); // $NON-NLS-1$
    }

    @Override
    public String getDocAnchor() {// reuse documentation
        return super.getStaticLabel().replace(' ', '_'); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected JTabbedPane createTabbedConfigPane() {
        final JTabbedPane tabbedPane = super.createTabbedConfigPane();
        // Remove other tabs than the first 'Basic' tab as they are unnecessary in GraphQL requests.
        final int tabCount = tabbedPane.getTabCount();
        for (int i = tabCount - 1; i > 0; i--) {
            tabbedPane.remove(i);
        }
        return tabbedPane;
    }

    @Override
    protected UrlConfigGui createUrlConfigGui() {
        final GraphQLUrlConfigGui configGui = new GraphQLUrlConfigGui();
        configGui.setBorder(makeBorder());
        return configGui;
    }
}
