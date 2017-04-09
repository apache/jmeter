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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.protocol.java.config.JavaConfig;
import org.apache.jmeter.protocol.java.config.gui.JavaConfigGui;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * The <code>JavaTestSamplerGui</code> class provides the user interface for
 * the {@link JavaSampler}.
 *
 */
public class JavaTestSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    /** Panel containing the configuration options. */
    private JavaConfigGui javaPanel = null;

    /**
     * Constructor for JavaTestSamplerGui
     */
    public JavaTestSamplerGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "java_request"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        javaPanel = new JavaConfigGui(false);

        add(javaPanel, BorderLayout.CENTER);
    }

    /* Implements JMeterGuiComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        JavaSampler sampler = new JavaSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /* Implements JMeterGuiComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        JavaConfig config = (JavaConfig) javaPanel.createTestElement();
        configureTestElement(sampler);
        sampler.addTestElement(config);
    }

    /* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        javaPanel.configure(el);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#clearGui()
     */
    @Override
    public void clearGui() {
        super.clearGui();
        javaPanel.clearGui();
    }
}
