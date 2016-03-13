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

package org.apache.jmeter.modifiers.gui;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.jmeter.modifiers.SampleTimeout;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for SampleTimeout.
 */
public class SampleTimeoutGui extends AbstractPreProcessorGui {

    private static final long serialVersionUID = 240L;

    /**
     * The default value for the timeout.
     */
    private static final String DEFAULT_TIMEOUT = "10000";

    private JTextField timeoutField;

    /**
     * No-arg constructor.
     */
    public SampleTimeoutGui() {
        init();
    }

    /**
     * Handle an error.
     *
     * @param e
     *            the Exception that was thrown.
     * @param thrower
     *            the JComponent that threw the Exception.
     */
    public static void error(Exception e, JComponent thrower) {
        JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getLabelResource() {
        return "sample_timeout_title"; // $NON-NLS-1$
    }

    /**
     * Create the test element underlying this GUI component.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        SampleTimeout timer = new SampleTimeout();
        modifyTestElement(timer);
        return timer;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement timer) {
        super.configureTestElement(timer);
        ((SampleTimeout) timer).setTimeout(timeoutField.getText());
    }

    /**
     * Configure this GUI component from the underlying TestElement.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        timeoutField.setText(((SampleTimeout) el).getTimeout());
    }

    /**
     * Initialize this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));

        setBorder(makeBorder());
        add(makeTitlePanel());

        Box timeoutPanel = Box.createHorizontalBox();
        JLabel timeoutLabel = new JLabel(JMeterUtils.getResString("sample_timeout_timeout"));//$NON-NLS-1$
        timeoutPanel.add(timeoutLabel);

        timeoutField = new JTextField(6);
        timeoutField.setText(DEFAULT_TIMEOUT);
        timeoutPanel.add(timeoutField);

        add(timeoutPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        timeoutField.setText(DEFAULT_TIMEOUT);
        super.clearGui();
    }
}
