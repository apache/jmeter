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

package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.testelement.OnErrorTestElement;
import org.apache.jmeter.util.JMeterUtils;

public class OnErrorPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    // Sampler error action buttons
    private JRadioButton continueBox;

    private JRadioButton breakLoopBox;

    private JRadioButton startNextThreadLoopBox;

    private JRadioButton startNextIterationOfCurrentLoopBox;

    private JRadioButton stopThrdBox;

    private JRadioButton stopTestBox;

    private JRadioButton stopTestNowBox;


    private JPanel createOnErrorPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sampler_on_error_action"))); //$NON-NLS-1$

        ButtonGroup group = new ButtonGroup();

        continueBox = addRadioButton("sampler_on_error_continue", group, panel); //$NON-NLS-1$
        breakLoopBox = addRadioButton("sampler_on_error_break_loop", group, panel); //$NON-NLS-1$
        startNextThreadLoopBox = addRadioButton("sampler_on_error_start_next_loop", group, panel); //$NON-NLS-1$
        startNextIterationOfCurrentLoopBox = addRadioButton("sampler_on_error_start_next_iteration_current_loop", group, panel); //$NON-NLS-1$
        stopTestBox = addRadioButton("sampler_on_error_stop_test", group, panel); //$NON-NLS-1$
        stopTestNowBox = addRadioButton("sampler_on_error_stop_test_now", group, panel); //$NON-NLS-1$
        stopThrdBox = addRadioButton("sampler_on_error_stop_thread", group, panel); //$NON-NLS-1$

        continueBox.setSelected(true);
        return panel;
    }

    private JRadioButton addRadioButton(String labelKey, ButtonGroup group, JPanel panel) {
        JRadioButton radioButton = new JRadioButton(JMeterUtils.getResString(labelKey));
        group.add(radioButton);
        panel.add(radioButton);
        return radioButton;
    }

    /**
     * Create a new NamePanel with the default name.
     */
    public OnErrorPanel() {
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));
        add(createOnErrorPanel());
    }

    public void configure(int errorAction) {
        stopTestNowBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTEST_NOW);
        startNextThreadLoopBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_START_NEXT_THREAD_LOOP);
        startNextIterationOfCurrentLoopBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_START_NEXT_ITERATION_OF_CURRENT_LOOP);
        stopTestBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTEST);
        stopThrdBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTHREAD);
        continueBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_CONTINUE);
        breakLoopBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_BREAK_CURRENT_LOOP);
    }

    public int getOnErrorSetting() {
        if (stopTestNowBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_STOPTEST_NOW;
        }
        if (stopTestBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_STOPTEST;
        }
        if (stopThrdBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_STOPTHREAD;
        }
        if (startNextThreadLoopBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_START_NEXT_THREAD_LOOP;
        }
        if (startNextIterationOfCurrentLoopBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_START_NEXT_ITERATION_OF_CURRENT_LOOP;
        }
        if(breakLoopBox.isSelected()) {
            return OnErrorTestElement.ON_ERROR_BREAK_CURRENT_LOOP;
        }

        // Defaults to continue
        return OnErrorTestElement.ON_ERROR_CONTINUE;
    }
}
