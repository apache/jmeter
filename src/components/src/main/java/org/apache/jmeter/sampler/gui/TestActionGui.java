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

package org.apache.jmeter.sampler.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

@GUIMenuSortOrder(1)
public class TestActionGui extends AbstractSamplerGui { // NOSONAR Ignore hierarchy error
    private static final long serialVersionUID = 240L;

    // Gui components
    private JComboBox<String> targetBox;

    private JRadioButton pauseButton;

    private JRadioButton stopButton;

    private JRadioButton stopNowButton;

    private JRadioButton breakLoopButton;

    private JRadioButton restartNextThreadLoopButton;

    private JRadioButton startNextIterationOfCurrentLoopButton;

    private JLabeledTextField durationField;

    // State variables
    private int target;

    private int action;

    // String in the panel
    // Do not make these static, otherwise language changes don't work
    private static final String TARGET_LABEL = JMeterUtils.getResString("test_action_target"); // $NON-NLS-1$

    private static final String THREAD_TARGET_LABEL = JMeterUtils.getResString("test_action_target_thread"); // $NON-NLS-1$

    private static final String TEST_TARGET_LABEL = JMeterUtils.getResString("test_action_target_test"); // $NON-NLS-1$

    private static final String ACTION_ON_THREAD_LABEL = JMeterUtils.getResString("test_action_action_thread"); // $NON-NLS-1$

    private static final String ACTION_ON_THREAD_TEST_LABEL = JMeterUtils.getResString("test_action_action_test_thread"); // $NON-NLS-1$

    private static final String PAUSE_ACTION_LABEL = JMeterUtils.getResString("test_action_pause"); // $NON-NLS-1$

    private static final String STOP_ACTION_LABEL = JMeterUtils.getResString("test_action_stop"); // $NON-NLS-1$

    private static final String STOP_NOW_ACTION_LABEL = JMeterUtils.getResString("test_action_stop_now"); // $NON-NLS-1$

    private static final String RESTART_NEXT_THREAD_LOOP_LABEL = JMeterUtils.getResString("test_action_restart_next_loop"); // $NON-NLS-1$

    private static final String START_NEXT_ITERATION_CURRENT_LOOP_ACTION = JMeterUtils.getResString("test_action_continue_current_loop"); // $NON-NLS-1$

    private static final String BREAK_CURRENT_LOOP_ACTION = JMeterUtils.getResString("test_action_break_current_loop"); // $NON-NLS-1$

    private static final String DURATION_LABEL = JMeterUtils.getResString("test_action_duration"); // $NON-NLS-1$

    public TestActionGui() {
        super();
        target = TestAction.THREAD;
        action = TestAction.PAUSE;
        init();
    }

    @Override
    public String getLabelResource() {
        return "test_action_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        TestAction ta = (TestAction) element;

        target = ta.getTarget();
        if (target == TestAction.THREAD) {
            targetBox.setSelectedItem(THREAD_TARGET_LABEL);
        } else {
            targetBox.setSelectedItem(TEST_TARGET_LABEL);
        }
        action = ta.getAction();
        switch (action) {
            case TestAction.PAUSE:
                pauseButton.setSelected(true);
                break;
            case TestAction.STOP_NOW:
                stopNowButton.setSelected(true);
                break;
            case TestAction.STOP:
                stopButton.setSelected(true);
                break;
            case TestAction.RESTART_NEXT_LOOP:
                restartNextThreadLoopButton.setSelected(true);
                break;
            case TestAction.START_NEXT_ITERATION_CURRENT_LOOP:
                startNextIterationOfCurrentLoopButton.setSelected(true);
                break;
            case TestAction.BREAK_CURRENT_LOOP:
                breakLoopButton.setSelected(true);
                break;
            default:
                break;
        }

        durationField.setText(ta.getDurationAsString());
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        TestAction ta = new TestAction();
        modifyTestElement(ta);
        return ta;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        TestAction ta = (TestAction) element;
        ta.setAction(action);
        ta.setTarget(target);
        ta.setDuration(durationField.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        targetBox.setSelectedIndex(0);
        durationField.setText("0"); //$NON-NLS-1$
        pauseButton.setSelected(true);
        action = TestAction.PAUSE;
        target = TestAction.THREAD;

    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        ButtonGroup actionButtons = new ButtonGroup();
        pauseButton = new JRadioButton(PAUSE_ACTION_LABEL, true);
        pauseButton.addChangeListener(evt -> {
            if (pauseButton.isSelected()) {
                action = TestAction.PAUSE;
                durationField.setEnabled(true);
                targetBox.setEnabled(false);
            }
        });
        stopButton = new JRadioButton(STOP_ACTION_LABEL, false);
        stopButton.addChangeListener(evt -> {
            if (stopButton.isSelected()) {
                action = TestAction.STOP;
                durationField.setEnabled(false);
                targetBox.setEnabled(true);
            }
        });
        stopNowButton = new JRadioButton(STOP_NOW_ACTION_LABEL, false);
        stopNowButton.addChangeListener(evt -> {
            if (stopNowButton.isSelected()) {
                action = TestAction.STOP_NOW;
                durationField.setEnabled(false);
                targetBox.setEnabled(true);
            }
        });

        restartNextThreadLoopButton = new JRadioButton(RESTART_NEXT_THREAD_LOOP_LABEL, false);
        restartNextThreadLoopButton.addChangeListener(evt -> {
            if (restartNextThreadLoopButton.isSelected()) {
                action = TestAction.RESTART_NEXT_LOOP;
                durationField.setEnabled(false);
                targetBox.setSelectedIndex(TestAction.THREAD);
                targetBox.setEnabled(false);
            }
        });

        startNextIterationOfCurrentLoopButton = new JRadioButton(START_NEXT_ITERATION_CURRENT_LOOP_ACTION, false);
        startNextIterationOfCurrentLoopButton.addChangeListener(evt -> {
            if (startNextIterationOfCurrentLoopButton.isSelected()) {
                action = TestAction.START_NEXT_ITERATION_CURRENT_LOOP;
                durationField.setEnabled(false);
                targetBox.setSelectedIndex(TestAction.THREAD);
                targetBox.setEnabled(false);
            }
        });

        breakLoopButton = new JRadioButton(BREAK_CURRENT_LOOP_ACTION, false);
        breakLoopButton.addChangeListener(evt -> {
            if (breakLoopButton.isSelected()) {
                action = TestAction.BREAK_CURRENT_LOOP;
                durationField.setEnabled(false);
                targetBox.setSelectedIndex(TestAction.THREAD);
                targetBox.setEnabled(false);
            }
        });

        // Duration
        durationField = new JLabeledTextField(DURATION_LABEL, 15);
        durationField.setText(""); // $NON-NLS-1$


        actionButtons.add(pauseButton);
        actionButtons.add(stopButton);
        actionButtons.add(stopNowButton);
        actionButtons.add(restartNextThreadLoopButton);
        actionButtons.add(startNextIterationOfCurrentLoopButton);
        actionButtons.add(breakLoopButton);

        // Action
        JPanel actionOnThreadPanel = new JPanel(new GridLayout(3, 2));
        actionOnThreadPanel.setBorder(BorderFactory.createTitledBorder(ACTION_ON_THREAD_LABEL)); //$NON-NLS-1$
        actionOnThreadPanel.add(pauseButton);
        actionOnThreadPanel.add(durationField);
        actionOnThreadPanel.add(restartNextThreadLoopButton);
        actionOnThreadPanel.add(startNextIterationOfCurrentLoopButton);
        actionOnThreadPanel.add(breakLoopButton);


        // Action
        JPanel actionOnTestOrThreadPanel = new JPanel(new GridLayout(2, 2));
        actionOnTestOrThreadPanel.setBorder(BorderFactory.createTitledBorder(ACTION_ON_THREAD_TEST_LABEL)); //$NON-NLS-1$
        actionOnTestOrThreadPanel.add(stopButton);
        actionOnTestOrThreadPanel.add(stopNowButton);
        actionOnTestOrThreadPanel.add(new JLabel(TARGET_LABEL));
        DefaultComboBoxModel<String> targetModel = new DefaultComboBoxModel<>();
        targetModel.addElement(THREAD_TARGET_LABEL);
        targetModel.addElement(TEST_TARGET_LABEL);
        targetBox = new JComboBox<>(targetModel);
        targetBox.addActionListener(evt -> {
            if (((String) targetBox.getSelectedItem()).equals(THREAD_TARGET_LABEL)) {
                target = TestAction.THREAD;
            } else {
                target = TestAction.TEST;
            }
        });
        actionOnTestOrThreadPanel.add(targetBox);

        add(actionOnThreadPanel);
        add(actionOnTestOrThreadPanel);
    }

}
