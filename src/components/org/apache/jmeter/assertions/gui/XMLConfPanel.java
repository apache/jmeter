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

package org.apache.jmeter.assertions.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.util.JMeterUtils;

public class XMLConfPanel extends JPanel {
	private JCheckBox validate, tolerant, whitespace, namespace;

	private JCheckBox quiet; // Should Tidy be quiet?
	
	private JCheckBox reportErrors; // Report Tidy errors as Assertion failure?
	
	private JCheckBox showWarnings; // Show Tidy warnings ?

	/**
	 * 
	 */
	public XMLConfPanel() {
		super();
		init();
	}

	/**
	 * @param isDoubleBuffered
	 */
	public XMLConfPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		init();
	}

	private void init() {
		Box tidyOptions = Box.createHorizontalBox();
		tidyOptions.setBorder(BorderFactory.createEtchedBorder());
		tidyOptions.add(getTolerant());
		quiet = new JCheckBox(JMeterUtils.getResString("xpath_tidy_quiet"),true);//$NON-NLS-1$
		reportErrors = new JCheckBox(JMeterUtils.getResString("xpath_tidy_report_errors"),true);//$NON-NLS-1$
		showWarnings = new JCheckBox(JMeterUtils.getResString("xpath_tidy_show_warnings"),true);//$NON-NLS-1$
		tidyOptions.add(quiet);
		tidyOptions.add(reportErrors);
		tidyOptions.add(showWarnings);
		add(tidyOptions);
		add(getNamespace());
		add(getValidate());
		add(getWhitespace());
		setDefaultValues();
	}

    public void setDefaultValues() {
        setWhitespace(false);
        setValidate(false);
        setTolerant(false);
        setNamespace(false);
        quiet.setSelected(true);
        reportErrors.setSelected(false);
        showWarnings.setSelected(false);
        tolerant();
    }

	/**
	 * @return Returns the namespace.
	 */
    private JCheckBox getNamespace() {
		if (namespace == null) {
			namespace = new JCheckBox(JMeterUtils.getResString("xml_namespace_button")); //$NON-NLS-1$
		}
		return namespace;
	}

	/**
	 * @return Returns the tolerant.
	 */
    private JCheckBox getTolerant() {
		if (tolerant == null) {
			tolerant = new JCheckBox(JMeterUtils.getResString("xml_tolerant_button")); //$NON-NLS-1$
			tolerant.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					tolerant();
				}
			});
		}
		return tolerant;
	}

	/**
	 * @return Returns the validate.
	 */
	private JCheckBox getValidate() {
		if (validate == null) {
			validate = new JCheckBox(JMeterUtils.getResString("xml_validate_button")); //$NON-NLS-1$
		}
		return validate;
	}

	/**
	 * @return Returns the whitespace.
	 */
	private JCheckBox getWhitespace() {
		if (whitespace == null) {
			whitespace = new JCheckBox(JMeterUtils.getResString("xml_whitespace_button")); //$NON-NLS-1$
		}
		return whitespace;
	}

	private boolean isNamespace() {
		return getNamespace().isSelected();
	}

	private void setNamespace(boolean namespace) {
		getNamespace().setSelected(namespace);
	}

	private boolean isTolerant() {
		return getTolerant().isSelected();
	}

	private void setTolerant(boolean tolerant) {
		getTolerant().setSelected(tolerant);
	}

	private boolean isWhitespace() {
		return getWhitespace().isSelected();
	}

	private void setWhitespace(boolean whitespace) {
		getWhitespace().setSelected(whitespace);
	}

	private boolean isValidate() {
		return getValidate().isSelected();
	}

	private void setValidate(boolean validating) {
		getValidate().setSelected(validating);
	}

	private void tolerant() {
		final boolean isTolerant = isTolerant();
		getValidate().setEnabled(!isTolerant);
		getWhitespace().setEnabled(!isTolerant);
		getNamespace().setEnabled(!isTolerant);
		quiet.setEnabled(isTolerant);
		reportErrors.setEnabled(isTolerant);
		showWarnings.setEnabled(isTolerant);
	}

	// Called by XPathAssertionGui
	public void modifyTestElement(XPathAssertion assertion) {
		assertion.setValidating(isValidate());
		assertion.setWhitespace(isWhitespace());
		assertion.setTolerant(isTolerant());
		assertion.setNamespace(isNamespace());
		assertion.setShowWarnings(showWarnings.isSelected());
		assertion.setReportErrors(reportErrors.isSelected());
		assertion.setQuiet(quiet.isSelected());		
	}

	// Called by XPathAssertionGui
	public void configure(XPathAssertion assertion) {
		setWhitespace(assertion.isWhitespace());
		setValidate(assertion.isValidating());
		setTolerant(assertion.isTolerant());
		setNamespace(assertion.isNamespace());
		quiet.setSelected(assertion.isQuiet());
		showWarnings.setSelected(assertion.showWarnings());
		reportErrors.setSelected(assertion.reportErrors());
		tolerant();
	}
}
