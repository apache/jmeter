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

package org.apache.jmeter.timers.gui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

import net.miginfocom.swing.MigLayout;

/**
 * Abstract Random timer GUI.
 *
 */
public abstract class AbstractRandomTimerGui extends AbstractTimerGui {

    /**
     *
     */
    private static final long serialVersionUID = -322164502276145504L;

    private static final String DELAY_FIELD = "Delay Field";

    private static final String RANGE_FIELD = "Range Field";

    private JTextField delayField;

    private JTextField rangeField;

    /**
     * No-arg constructor.
     */
    protected AbstractRandomTimerGui() {
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

    /**
     * Create the test element underlying this GUI component.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        RandomTimer timer = createRandomTimer();
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
        ((RandomTimer) timer).setDelay(delayField.getText());
        ((RandomTimer) timer).setRange(rangeField.getText());
    }

    /**
     * Configure this GUI component from the underlying TestElement.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        delayField.setText(el.getPropertyAsString(ConstantTimer.DELAY));
        rangeField.setText(el.getPropertyAsString(RandomTimer.RANGE));
    }


    /**
     * Initialize this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        setBorder(makeBorder());

        add(makeTitlePanel());

        JPanel threadDelayPropsPanel = new JPanel(new MigLayout("fillx, wrap 2", "[][fill,grow]"));
        threadDelayPropsPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("thread_delay_properties")));//$NON-NLS-1$

        // DELAY DEVIATION
        rangeField = new JTextField(20);
        rangeField.setText(getDefaultRange());
        threadDelayPropsPanel.add(JMeterUtils.labelFor(rangeField, getTimerRangeLabelKey(), RANGE_FIELD));
        threadDelayPropsPanel.add(rangeField);

        // AVG DELAY
        delayField = new JTextField(20);
        delayField.setText(getDefaultDelay());
        threadDelayPropsPanel.add(JMeterUtils.labelFor(delayField, getTimerDelayLabelKey(), DELAY_FIELD));
        threadDelayPropsPanel.add(delayField);

        add(threadDelayPropsPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        rangeField.setText(getDefaultRange());
        delayField.setText(getDefaultDelay());
        super.clearGui();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getLabelResource();

    /**
     * Create implementation of RandomTimer
     * @return {@link RandomTimer}
     */
    protected abstract RandomTimer createRandomTimer();

    /**
     * @return String timer delay label key
     */
    protected abstract String getTimerDelayLabelKey();

    /**
     * @return String timer range label key
     */
    protected abstract String getTimerRangeLabelKey();

    /**
     * @return String default delay value
     */
    protected abstract String getDefaultDelay();

    /**
     * @return String default range value
     */
    protected abstract String getDefaultRange();
}
