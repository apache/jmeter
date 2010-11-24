/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * HTTP Sampler GUI for Apache HTTPClient HTTP implementation
 */
public class HttpTestSampleGui2 extends HttpTestSampleGui {

    private static final long serialVersionUID = 240L;

    private JLabeledTextField sourceIpAddr;

    public HttpTestSampleGui2() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance(HTTPSamplerFactory.HTTP_SAMPLER_APACHE);
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * {@inheritDoc}
     */
    // Use this instead of getLabelResource() otherwise getDocAnchor() below does not work
    @Override
    public String getStaticLabel() {
        return JMeterUtils.getResString("web_testing2_title"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    // Documentation is shared with our parent
    @Override
    public String getDocAnchor() {
        return super.getStaticLabel().replace(' ', '_'); // $NON-NLS-1$  // $NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JPanel createOptionalTasksPanel() {
        JPanel optionalTasksPanel = super.createOptionalTasksPanel();
        // Add a new field source ip address
        sourceIpAddr = new JLabeledTextField(JMeterUtils
                .getResString("web_testing2_source_ip")); // $NON-NLS-1$
        optionalTasksPanel.add(sourceIpAddr, BorderLayout.EAST);

        return optionalTasksPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        sourceIpAddr.setText(""); // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) element;
        sourceIpAddr.setText(samplerBase.getIpSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        // TODO Auto-generated method stub
        super.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        samplerBase.setIpSource(sourceIpAddr.getText());
    }
}
