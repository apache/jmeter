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

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.jmeter.util.JMeterUtils;

public class XMLConfPanel extends JPanel {
	private JCheckBox validate, tolerant, whitespace, namespace;

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
		add(getTolerant());
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
    }

	/**
	 * @return Returns the namespace.
	 */
	public JCheckBox getNamespace() {
		if (namespace == null) {
			namespace = new JCheckBox(JMeterUtils.getResString("xml_namespace_button")); //$NON-NLS-1$
		}
		return namespace;
	}

	/**
	 * @return Returns the tolerant.
	 */
	public JCheckBox getTolerant() {
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
	public JCheckBox getValidate() {
		if (validate == null) {
			validate = new JCheckBox(JMeterUtils.getResString("xml_validate_button")); //$NON-NLS-1$
		}
		return validate;
	}

	/**
	 * @return Returns the whitespace.
	 */
	public JCheckBox getWhitespace() {
		if (whitespace == null) {
			whitespace = new JCheckBox(JMeterUtils.getResString("xml_whitespace_button")); //$NON-NLS-1$
		}
		return whitespace;
	}

	public boolean isNamespace() {
		return getNamespace().isSelected();
	}

	public void setNamespace(boolean namespace) {
		getNamespace().setSelected(namespace);
	}

	public boolean isTolerant() {
		return getTolerant().isSelected();
	}

	public void setTolerant(boolean tolerant) {
		getTolerant().setSelected(tolerant);
	}

	public boolean isWhitespace() {
		return getWhitespace().isSelected();
	}

	public void setWhitespace(boolean whitespace) {
		getWhitespace().setSelected(whitespace);
	}

	public boolean isValidate() {
		return getValidate().isSelected();
	}

	public void setValidate(boolean validating) {
		getValidate().setSelected(validating);
	}

	private void tolerant() {
		getValidate().setEnabled(!isTolerant());
		getWhitespace().setEnabled(!isTolerant());
		getNamespace().setEnabled(!isTolerant());
	}

	public static void main(String[] args) {
		JPanel comb = new JPanel();

		XMLConfPanel xml = new XMLConfPanel();
		XPathPanel xpath = new XPathPanel();
		JFrame frame = new JFrame(xml.getClass().getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		comb.add(xpath);
		comb.add(xml);
		frame.add(comb);

		frame.pack();
		frame.setVisible(true);

	}
}
