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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * @version $Revision$
 */
public class TestActionGui extends AbstractSamplerGui {
	// Gui components
	private JComboBox targetBox;

	// private ButtonGroup actionButtons;
	private JRadioButton pauseButton;

	private JRadioButton stopButton;

	private JTextField durationField;

	// State variables
	private int target;

	private int action;

	private int duration;

	// String in the panel
	private static final String targetLabel = JMeterUtils.getResString("test_action_target");

	private static final String threadTarget = JMeterUtils.getResString("test_action_target_thread");

	private static final String testTarget = JMeterUtils.getResString("test_action_target_test");

	private static final String actionLabel = JMeterUtils.getResString("test_action_action");

	private static final String pauseAction = JMeterUtils.getResString("test_action_pause");

	private static final String stopAction = JMeterUtils.getResString("test_action_stop");

	private static final String durationLabel = JMeterUtils.getResString("test_action_duration");

	public TestActionGui() {
		super();
		target = TestAction.THREAD;
		action = TestAction.PAUSE;
		init();
	}

	public String getLabelResource() {
		return "test_action_title";
	}

	public void configure(TestElement element) {
		super.configure(element);
		TestAction ta = (TestAction) element;

		target = ta.getTarget();
		if (target == TestAction.THREAD)
			targetBox.setSelectedItem(threadTarget);
		else
			targetBox.setSelectedItem(testTarget);

		action = ta.getAction();
		if (action == TestAction.PAUSE)
			pauseButton.setSelected(true);
		else
			stopButton.setSelected(true);

		duration = ta.getDuration();
		durationField.setText(Integer.toString(duration));
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
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
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		TestAction ta = (TestAction) element;
		ta.setAction(action);
		ta.setTarget(target);
		ta.setDuration(duration);
	}

    /**
     * Implements JMeterGUIComponent.clear
     */
    public void clear() {
        super.clear();
        
        targetBox.setSelectedIndex(0);
        durationField.setText(""); //$NON-NLS-1$
        pauseButton.setSelected(true);
        stopButton.setSelected(false);
        action = TestAction.PAUSE;
        target = TestAction.THREAD;
        duration = 0;
        
    }    

	private void init() {
		setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
		setBorder(makeBorder());
		add(makeTitlePanel());

		// Target
		HorizontalPanel targetPanel = new HorizontalPanel();
		targetPanel.add(new JLabel(targetLabel));
		DefaultComboBoxModel targetModel = new DefaultComboBoxModel();
		targetModel.addElement(threadTarget);
		targetModel.addElement(testTarget);
		targetBox = new JComboBox(targetModel);
		targetBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((String) targetBox.getSelectedItem()).equals(threadTarget)) {
					target = TestAction.THREAD;
				} else {
					target = TestAction.TEST;
				}
			}
		});
		targetPanel.add(targetBox);
		add(targetPanel);

		// Action
		HorizontalPanel actionPanel = new HorizontalPanel();
		ButtonGroup actionButtons = new ButtonGroup();
		pauseButton = new JRadioButton(pauseAction, true);
		pauseButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (pauseButton.isSelected()) {
					action = TestAction.PAUSE;
					durationField.setEnabled(true);
				}

			}
		});
		stopButton = new JRadioButton(stopAction, false);
		stopButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (stopButton.isSelected()) {
					action = TestAction.STOP;
					durationField.setEnabled(false);
				}
			}
		});
		actionButtons.add(pauseButton);
		actionButtons.add(stopButton);
		actionPanel.add(new JLabel(actionLabel));
		actionPanel.add(pauseButton);
		actionPanel.add(stopButton);
		add(actionPanel);

		// Duration
		HorizontalPanel durationPanel = new HorizontalPanel();
		durationField = new JTextField(5);
		durationField.setText(Integer.toString(duration));
		durationField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				try {
					duration = Integer.parseInt(durationField.getText());
				} catch (NumberFormatException nfe) {
					duration = 0;
					// alert
					// durationField.grabFocus();
				}
			}

			public void focusGained(FocusEvent e) {
			}
		});
		durationPanel.add(new JLabel(durationLabel));
		durationPanel.add(durationField);
		add(durationPanel);
	}

}
