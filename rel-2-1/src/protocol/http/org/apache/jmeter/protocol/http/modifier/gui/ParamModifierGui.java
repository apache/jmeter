// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.protocol.http.modifier.ParamMask;
import org.apache.jmeter.protocol.http.modifier.ParamModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * A swing panel to allow UI with the ParamModifier class.
 * 
 * Created Jan 18, 2002
 * 
 * @version $Revision$ Last updated: $Date$
 */
public class ParamModifierGui extends AbstractPreProcessorGui implements FocusListener {
	private static final String NAME = "name";

	private static final String PREFIX = "prefix";

	private static final String LOWERBOUND = "lowerBound";

	private static final String UPPERBOUND = "upperBound";

	private static final String INCREMENT = "increment";

	private static final String SUFFIX = "suffix";

	private JTextField _fieldName;

	private JTextField _prefix;

	private JTextField _lowerBound;

	private JTextField _upperBound;

	private JTextField _increment;

	private JTextField _suffix;

	public ParamModifierGui() {
		init();
	}

	public String getLabelResource() {
		return "html_parameter_mask";
	}

	public void configure(TestElement el) {
		super.configure(el);
		ParamModifier model = (ParamModifier) el;
		updateGui(model);
	}

	public TestElement createTestElement() {
		ParamModifier modifier = new ParamModifier();
		modifyTestElement(modifier);
		return modifier;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement m) {
		configureTestElement(m);
		if (m instanceof ParamModifier) {
			ParamModifier modifier = (ParamModifier) m;
			ParamMask mask = modifier.getMask();
			mask.setFieldName(_fieldName.getText());
			mask.setPrefix(_prefix.getText());
			mask.setLowerBound(Long.parseLong(_lowerBound.getText()));
			mask.setIncrement(Long.parseLong(_increment.getText()));
			mask.setUpperBound(Long.parseLong(_upperBound.getText()));
			mask.setSuffix(_suffix.getText());
		}
	}

	public void focusGained(FocusEvent evt) {
	}

	public void focusLost(FocusEvent evt) {
		String name = ((Component) evt.getSource()).getName();
		if (evt.isTemporary()) {
			return;
		} else if (name.equals(LOWERBOUND)) {
			checkTextField(evt, "0");
		} else if (name.equals(UPPERBOUND)) {
			checkTextField(evt, "0");
		} else if (name.equals(INCREMENT)) {
			checkTextField(evt, "0");
		}
	}

	protected void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		add(getParameterMaskPanel(), BorderLayout.CENTER);
		// this.updateUI();
	}

	private void updateGui(ParamModifier model) {
		_fieldName.setText(model.getMask().getFieldName());
		_prefix.setText(model.getMask().getPrefix());
		_lowerBound.setText(Long.toString(model.getMask().getLowerBound()));
		_upperBound.setText(Long.toString(model.getMask().getUpperBound()));
		_increment.setText(Long.toString(model.getMask().getIncrement()));
		_suffix.setText(model.getMask().getSuffix());
	}

	private JPanel createLabeledField(String labelResName, JTextField field) {
		JLabel label = new JLabel(JMeterUtils.getResString(labelResName));
		label.setLabelFor(field);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(field, BorderLayout.CENTER);
		return panel;
	}

	private JPanel getParameterMaskPanel() {
		HorizontalPanel panel = new HorizontalPanel(10, HorizontalPanel.TOP_ALIGNMENT);
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("HTML Parameter Mask")));

		_fieldName = new JTextField(10);
		_fieldName.setName(NAME);
		panel.add(createLabeledField("Name", _fieldName));

		_prefix = new JTextField(5);
		_prefix.setName(PREFIX);
		panel.add(createLabeledField("ID Prefix", _prefix));

		_lowerBound = new JTextField("0", 5);
		_lowerBound.addFocusListener(this);
		_lowerBound.setName(LOWERBOUND);
		panel.add(createLabeledField("Lower Bound", _lowerBound));

		_upperBound = new JTextField("10", 5);
		_upperBound.addFocusListener(this);
		_upperBound.setName(UPPERBOUND);
		panel.add(createLabeledField("Upper Bound", _upperBound));

		_increment = new JTextField("1", 3);
		_increment.addFocusListener(this);
		_increment.setName(INCREMENT);
		panel.add(createLabeledField("Increment", _increment));

		_suffix = new JTextField(5);
		_suffix.setName(SUFFIX);
		panel.add(createLabeledField("ID Suffix", _suffix));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(panel, BorderLayout.NORTH);
		return mainPanel;
	}

	/**
	 * Used to validate a text field that requires a <code>long</code> input.
	 * Returns the <code>long</code> if valid, else creates a pop-up error
	 * message and throws a NumberFormatException.
	 * 
	 * @return the number entered in the text field
	 */
	private long checkTextField(FocusEvent evt, String defaultValue) {
		JTextField temp = (JTextField) evt.getSource();
		// boolean pass = true;
		long longVal = 0;

		try {
			longVal = Long.parseLong(temp.getText());
		} catch (NumberFormatException err) {
			JOptionPane.showMessageDialog(this, "This field must have a long value!", "Value Required",
					JOptionPane.ERROR_MESSAGE);
			temp.setText(defaultValue);
			temp.requestFocus();
		}
		return longVal;
	}
}
