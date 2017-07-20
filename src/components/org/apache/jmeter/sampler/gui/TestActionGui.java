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

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class TestActionGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    // Gui components
    private JComboBox<String> targetBox;

    private JRadioButton pauseButton;

    private JRadioButton stopButton;

    private JRadioButton stopNowButton;

    private JRadioButton restartNextLoopButton;
    
    private JTextField durationField;

    // State variables
    private int target;

    private int action;

    // String in the panel
    // Do not make these static, otherwise language changes don't work
    private final String targetLabel = JMeterUtils.getResString("test_action_target"); // $NON-NLS-1$

    private final String threadTarget = JMeterUtils.getResString("test_action_target_thread"); // $NON-NLS-1$

    private final String testTarget = JMeterUtils.getResString("test_action_target_test"); // $NON-NLS-1$

    private final String actionLabel = JMeterUtils.getResString("test_action_action"); // $NON-NLS-1$

    private final String pauseAction = JMeterUtils.getResString("test_action_pause"); // $NON-NLS-1$

    private final String stopAction = JMeterUtils.getResString("test_action_stop"); // $NON-NLS-1$

    private final String stopNowAction = JMeterUtils.getResString("test_action_stop_now"); // $NON-NLS-1$

    private final String restartNextLoopAction = JMeterUtils.getResString("test_action_restart_next_loop"); // $NON-NLS-1$

    private final String durationLabel = JMeterUtils.getResString("test_action_duration"); // $NON-NLS-1$

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
            targetBox.setSelectedItem(threadTarget);
        } else {
            targetBox.setSelectedItem(testTarget);
        }
        action = ta.getAction();
        if (action == TestAction.PAUSE) {
            pauseButton.setSelected(true);
        } else if (action == TestAction.STOP_NOW) {
            stopNowButton.setSelected(true);
        } else if(action == TestAction.STOP ){
            stopButton.setSelected(true);
        } else {
            restartNextLoopButton.setSelected(true);
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

        // Target
        HorizontalPanel targetPanel = new HorizontalPanel();
        targetPanel.add(new JLabel(targetLabel));
        DefaultComboBoxModel<String> targetModel = new DefaultComboBoxModel<>();
        targetModel.addElement(threadTarget);
        targetModel.addElement(testTarget);
        targetBox = new JComboBox<>(targetModel);
        targetBox.addActionListener(evt -> {
            if (((String) targetBox.getSelectedItem()).equals(threadTarget)) {
                target = TestAction.THREAD;
            } else {
                target = TestAction.TEST;
            }
        });
        targetPanel.add(targetBox);
        add(targetPanel);

        // Action
        HorizontalPanel actionPanel = new HorizontalPanel();
        ButtonGroup actionButtons = new ButtonGroup();
        pauseButton = new JRadioButton(pauseAction, true);
        pauseButton.addChangeListener(evt -> {
            if (pauseButton.isSelected()) {
                action = TestAction.PAUSE;
                durationField.setEnabled(true);
                targetBox.setEnabled(true);
            }
        });
        stopButton = new JRadioButton(stopAction, false);
        stopButton.addChangeListener(evt -> {
            if (stopButton.isSelected()) {
                action = TestAction.STOP;
                durationField.setEnabled(false);
                targetBox.setEnabled(true);
            }
        });
        stopNowButton = new JRadioButton(stopNowAction, false);
        stopNowButton.addChangeListener(evt -> {
            if (stopNowButton.isSelected()) {
                action = TestAction.STOP_NOW;
                durationField.setEnabled(false);
                targetBox.setEnabled(true);
            }
        });
        
        restartNextLoopButton = new JRadioButton(restartNextLoopAction, false);
        restartNextLoopButton.addChangeListener(evt -> {
            if (restartNextLoopButton.isSelected()) {
                action = TestAction.RESTART_NEXT_LOOP;
                durationField.setEnabled(false);
                targetBox.setSelectedIndex(TestAction.THREAD);
                targetBox.setEnabled(false);
            }
        });
        
        actionButtons.add(pauseButton);
        actionButtons.add(stopButton);
        actionButtons.add(stopNowButton);
        actionButtons.add(restartNextLoopButton);

        actionPanel.add(new JLabel(actionLabel));
        actionPanel.add(pauseButton);
        actionPanel.add(stopButton);
        actionPanel.add(stopNowButton);
        actionPanel.add(restartNextLoopButton);
        add(actionPanel);

        // Duration
        HorizontalPanel durationPanel = new HorizontalPanel();
        durationField = new JTextField(15);
        durationField.setText(""); // $NON-NLS-1$
        durationPanel.add(new JLabel(durationLabel));
        durationPanel.add(durationField);
        add(durationPanel);
    }

}
