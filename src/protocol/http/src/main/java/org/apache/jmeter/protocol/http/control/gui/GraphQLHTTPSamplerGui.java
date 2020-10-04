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

import javax.swing.JPanel;

import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.protocol.http.config.gui.GraphQLUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.util.JMeterUtils;

/**
 * GraphQL HTTP Sampler GUI which extends {@link HttpTestSampleGui} in order to provide more convenient UI elements for
 * GraphQL query, variables and operationName.
 */
@TestElementMetadata(labelResource = "graphql_http_sampler_title")
public class GraphQLHTTPSamplerGui extends HttpTestSampleGui {

    private static final long serialVersionUID = 1L;

    public GraphQLHTTPSamplerGui() {
        super();
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

    /**
     * {@inheritDoc}
     * <P>
     * Overridden to hide the HTML embedded resource handling section as GraphQL responses are always in JSON.
     */
    @Override
    protected JPanel createEmbeddedRsrcPanel() {
        final JPanel panel = super.createEmbeddedRsrcPanel();
        // No need to consider embedded resources in HTML as the GraphQL responses are always in JSON.
        panel.setVisible(false);
        return panel;
    }

    /**
     * {@inheritDoc}
     * <P>
     * Overridden to create a {@link GraphQLUrlConfigGui} which extends {@link UrlConfigGui} for GraphQL specific UI elements.
     */
    @Override
    protected UrlConfigGui createUrlConfigGui() {
        final GraphQLUrlConfigGui configGui = new GraphQLUrlConfigGui();
        configGui.setBorder(makeBorder());
        return configGui;
    }
}
