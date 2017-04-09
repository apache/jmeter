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

/*
 * Example Sampler GUI (non-beans version)
 */

package org.apache.jmeter.examples.sampler.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.apache.jmeter.examples.sampler.ExampleSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Example Sampler (non-Bean version)
 *
 * This class is responsible for ensuring that the Sampler data is kept in step
 * with the GUI.
 *
 * The GUI class is not invoked in non-GUI mode, so it should not perform any
 * additional setup that a test would need at run-time
 *
 */
public class ExampleSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 240L;

    private JTextArea data;

    public ExampleSamplerGui() {
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "example_title"; // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        data.setText(element.getPropertyAsString(ExampleSampler.DATA));
        super.configure(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        ExampleSampler sampler = new ExampleSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement te) {
        te.clear();
        configureTestElement(te);
        te.setProperty(ExampleSampler.DATA, data.getText());
    }

    /*
     * Helper method to set up the GUI screen
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // Standard setup
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title

        // Specific setup
        add(createDataPanel(), BorderLayout.CENTER);
    }

    /*
     * Create a data input text field
     *
     * @return the panel for entering the data
     */
    private Component createDataPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("example_data")); //$NON-NLS-1$

        data = new JTextArea();
        data.setName(ExampleSampler.DATA);
        label.setLabelFor(data);

        JPanel dataPanel = new JPanel(new BorderLayout(5, 0));
        dataPanel.add(label, BorderLayout.WEST);
        dataPanel.add(data, BorderLayout.CENTER);

        return dataPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        data.setText(""); // $NON-NLS-1$

    }
}
