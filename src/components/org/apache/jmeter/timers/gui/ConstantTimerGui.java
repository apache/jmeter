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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for ConstantTimer.
 */
@GUIMenuSortOrder(1)
public class ConstantTimerGui extends AbstractTimerGui {
    private static final long serialVersionUID = 240L;

    private static final String DEFAULT_DELAY = "300";
    private static final String DELAY_FIELD = "Delay Field";

    private JTextField delayField;

    public ConstantTimerGui() {
        init();
    }

    /**
     * Handle an error.
     *
     * @param e       the Exception that was thrown.
     * @param thrower the JComponent that threw the Exception.
     */
    public static void error(Exception e, JComponent thrower) {
        JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getLabelResource() {
        return "constant_timer_title"; // $NON-NLS-1$
    }

    /**
     * Create the test element underlying this GUI component.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ConstantTimer timer = new ConstantTimer();
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
        ((ConstantTimer) timer).setDelay(delayField.getText());
    }

    /**
     * Configure this GUI component from the underlying TestElement.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        delayField.setText(((ConstantTimer) el).getDelay());
    }

    /**
     * Initialize this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));

        setBorder(makeBorder());
        add(makeTitlePanel());

        Box delayPanel = Box.createHorizontalBox();
        JLabel delayLabel = new JLabel(JMeterUtils.getResString("constant_timer_delay"));//$NON-NLS-1$
        delayPanel.add(delayLabel);

        delayField = new JTextField(6);
        delayField.setText(DEFAULT_DELAY);
        delayField.setName(DELAY_FIELD);
        delayPanel.add(delayField);
        add(delayPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        delayField.setText(DEFAULT_DELAY);
        super.clearGui();
    }
}
