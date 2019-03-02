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

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

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
    public AbstractRandomTimerGui() {
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

        JPanel threadDelayPropsPanel = new JPanel();
        threadDelayPropsPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
        threadDelayPropsPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("thread_delay_properties")));//$NON-NLS-1$

        // DELAY DEVIATION
        Box delayDevPanel = Box.createHorizontalBox();
        delayDevPanel.add(new JLabel(getTimerRangeLabelKey()));//$NON-NLS-1$
        delayDevPanel.add(Box.createHorizontalStrut(5));

        rangeField = new JTextField(20);
        rangeField.setText(getDefaultRange());
        rangeField.setName(RANGE_FIELD);
        delayDevPanel.add(rangeField);

        threadDelayPropsPanel.add(delayDevPanel);

        // AVG DELAY
        Box avgDelayPanel = Box.createHorizontalBox();
        avgDelayPanel.add(new JLabel(getTimerDelayLabelKey()));//$NON-NLS-1$
        avgDelayPanel.add(Box.createHorizontalStrut(5));

        delayField = new JTextField(20);
        delayField.setText(getDefaultDelay());
        delayField.setName(DELAY_FIELD);
        avgDelayPanel.add(delayField);

        threadDelayPropsPanel.add(avgDelayPanel);
        threadDelayPropsPanel.setMaximumSize(new Dimension(threadDelayPropsPanel.getMaximumSize().width,
                threadDelayPropsPanel.getPreferredSize().height));
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
    abstract public String getLabelResource();

    /**
     * Create implementation of RandomTimer
     * @return {@link RandomTimer}
     */
    protected abstract RandomTimer createRandomTimer();

    /**
     * @return String timer delay label key
     */
    abstract protected String getTimerDelayLabelKey();

    /**
     * @return String timer range label key
     */
    abstract protected String getTimerRangeLabelKey();

    /**
     * @return String default delay value
     */
    abstract protected String getDefaultDelay();

    /**
     * @return String default range value
     */
    abstract protected String getDefaultRange();
}
