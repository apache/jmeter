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

package org.apache.jmeter.timers.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.GaussianRandomTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * Implementation of a gaussian random timer.
 *
 */
public class GaussianRandomTimerGui extends AbstractTimerGui {

    private static final long serialVersionUID = 240L;

    private static final String DELAY_FIELD = "Delay Field";

    private static final String RANGE_FIELD = "Range Field";

    private static final String DEFAULT_DELAY = "300"; // $NON-NLS-1$

    private static final String DEFAULT_RANGE = "100.0"; // $NON-NLS-1$

    private JTextField delayField;

    private JTextField rangeField;

    /**
     * No-arg constructor.
     */
    public GaussianRandomTimerGui() {
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
    public TestElement createTestElement() {
        RandomTimer timer = new GaussianRandomTimer();
        modifyTestElement(timer);
        return timer;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement timer) {
        this.configureTestElement(timer);
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
        delayField.setText(el.getPropertyAsString(RandomTimer.DELAY));
        rangeField.setText(el.getPropertyAsString(RandomTimer.RANGE));
    }

    public String getLabelResource() {
        return "gaussian_timer_title";//$NON-NLS-1$
    }

    /**
     * Initialize this component.
     */
    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        setBorder(makeBorder());

        add(makeTitlePanel());

        JPanel threadDelayPropsPanel = new JPanel();
        threadDelayPropsPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
        threadDelayPropsPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("thread_delay_properties")));//$NON-NLS-1$

        // DELAY DEVIATION
        Box delayDevPanel = Box.createHorizontalBox();
        delayDevPanel.add(new JLabel(JMeterUtils.getResString("gaussian_timer_range")));//$NON-NLS-1$
        delayDevPanel.add(Box.createHorizontalStrut(5));

        rangeField = new JTextField(6);
        rangeField.setText(DEFAULT_RANGE);
        rangeField.setName(RANGE_FIELD);
        delayDevPanel.add(rangeField);

        threadDelayPropsPanel.add(delayDevPanel);

        // AVG DELAY
        Box avgDelayPanel = Box.createHorizontalBox();
        avgDelayPanel.add(new JLabel(JMeterUtils.getResString("gaussian_timer_delay")));//$NON-NLS-1$
        avgDelayPanel.add(Box.createHorizontalStrut(5));

        delayField = new JTextField(6);
        delayField.setText(DEFAULT_DELAY);
        delayField.setName(DELAY_FIELD);
        avgDelayPanel.add(delayField);

        threadDelayPropsPanel.add(avgDelayPanel);
        threadDelayPropsPanel.setMaximumSize(new Dimension(threadDelayPropsPanel.getMaximumSize().width,
                threadDelayPropsPanel.getPreferredSize().height));
        add(threadDelayPropsPanel);

        // Set the initial focus to the delay field
        new FocusRequester(rangeField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        rangeField.setText(DEFAULT_RANGE);
        delayField.setText(DEFAULT_DELAY);
        super.clearGui();
    }
}
