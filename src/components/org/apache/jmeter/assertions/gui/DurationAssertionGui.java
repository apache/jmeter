// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$ Last updated: $Date$
 */
public class DurationAssertionGui extends AbstractAssertionGui implements FocusListener {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	private JTextField duration;

	public DurationAssertionGui() {
		init();
	}

	public String getLabelResource() {
		return "duration_assertion_title";
	}

	public String getDurationAttributesTitle() {
		return JMeterUtils.getResString("duration_assertion_duration_test");
	}

	public TestElement createTestElement() {
		DurationAssertion el = new DurationAssertion();
		modifyTestElement(el);
		return el;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement el) {
		configureTestElement(el);
		String durationString = duration.getText();
		long assertionDuration = 0;
		try {
			assertionDuration = Long.parseLong(durationString);
		} catch (NumberFormatException e) {
			assertionDuration = Long.MAX_VALUE;
		}
		((DurationAssertion) el).setAllowedDuration(assertionDuration);
	}

	public void configure(TestElement el) {
		super.configure(el);
		DurationAssertion assertion = (DurationAssertion) el;
		duration.setText(String.valueOf(assertion.getAllowedDuration()));
	}

	private void init() {
		setLayout(new BorderLayout(0, 10));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new BorderLayout());

		// USER_INPUT
		HorizontalPanel durationPanel = new HorizontalPanel();
		durationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				getDurationAttributesTitle()));

		durationPanel.add(new JLabel(JMeterUtils.getResString("duration_assertion_label")));

		duration = new JTextField(5);
		duration.addFocusListener(this);
		durationPanel.add(duration);

		mainPanel.add(durationPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	public void focusLost(FocusEvent e) {
		boolean isInvalid = false;
		String durationString = duration.getText();
		if (durationString != null) {
			try {
				long assertionDuration = Long.parseLong(durationString);
				if (assertionDuration < 0) {
					isInvalid = true;
				}
			} catch (NumberFormatException ex) {
				isInvalid = true;
			}
			if (isInvalid) {
				log.warn("DurationAssertionGui: Not a valid number!");
				JOptionPane.showMessageDialog(null, JMeterUtils.getResString("duration_assertion_input_error"),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void focusGained(FocusEvent e) {
	}
}
