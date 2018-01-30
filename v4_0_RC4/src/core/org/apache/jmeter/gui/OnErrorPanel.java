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

    private JRadioButton startNextThreadLoopBox;

    private JRadioButton stopThrdBox;

    private JRadioButton stopTestBox;

    private JRadioButton stopTestNowBox;

    private JPanel createOnErrorPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sampler_on_error_action"))); //$NON-NLS-1$

        ButtonGroup group = new ButtonGroup();

        continueBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_continue")); //$NON-NLS-1$
        group.add(continueBox);
        continueBox.setSelected(true);
        panel.add(continueBox);

        startNextThreadLoopBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_start_next_loop")); //$NON-NLS-1$
        group.add(startNextThreadLoopBox);
        panel.add(startNextThreadLoopBox);

        stopThrdBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_thread")); //$NON-NLS-1$
        group.add(stopThrdBox);
        panel.add(stopThrdBox);

        stopTestBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test")); //$NON-NLS-1$
        group.add(stopTestBox);
        panel.add(stopTestBox);

        stopTestNowBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test_now")); //$NON-NLS-1$
        group.add(stopTestNowBox);
        panel.add(stopTestNowBox);

        return panel;
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
        stopTestBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTEST);
        stopThrdBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_STOPTHREAD);
        continueBox.setSelected(errorAction == OnErrorTestElement.ON_ERROR_CONTINUE);
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

        // Defaults to continue
        return OnErrorTestElement.ON_ERROR_CONTINUE;
    }
}
