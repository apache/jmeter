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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.TextAreaCellRenderer;
import org.apache.jmeter.gui.util.TextAreaTableCellEditor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;

/**
 * GUI interface for a {@link ResponseAssertion}.
 * 
 * @version $Revision$ on $Date$
 */
public class AssertionGui extends AbstractAssertionGui {
	/** The name of the table column in the list of patterns. */
	private static final String COL_NAME = JMeterUtils.getResString("assertion_patterns_to_test");

	/** Radio button indicating that the text response should be tested. */
	private JRadioButton responseStringButton;

	/** Radio button indicating that the URL should be tested. */
	private JRadioButton urlButton;

	/** Radio button indicating that the responseMessage should be tested. */
	private JRadioButton responseMessageButton;

	/** Radio button indicating that the responseCode should be tested. */
	private JRadioButton responseCodeButton;

	/**
	 * Checkbox to indicate whether the response should be forced successful
	 * before testing. This is intended for use when checking the status code or
	 * status message.
	 */
	private JCheckBox assumeSuccess;

	/**
	 * Radio button indicating to test if the field contains one of the
	 * patterns.
	 */
	private JRadioButton containsBox;

	/**
	 * Radio button indicating to test if the field matches one of the patterns.
	 */
	private JRadioButton matchesBox;

	/**
	 * Radio button indicating if the field equals the first pattern.
	 */
	private JRadioButton equalsBox;

    /**
	 * Checkbox indicating to test that the field does NOT contain/match the
	 * patterns.
	 */
	private JCheckBox notBox;

	/** A table of patterns to test against. */
	private JTable stringTable;

	/** Button to add a new pattern. */
	private JButton addPattern;

	/** Button to delete a pattern. */
	private JButton deletePattern;

	/** Table model for the pattern table. */
	private PowerTableModel tableModel;

	/**
	 * Create a new AssertionGui panel.
	 */
	public AssertionGui() {
		init();
	}

	public String getLabelResource() {
		return "assertion_title";
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		ResponseAssertion el = new ResponseAssertion();
		modifyTestElement(el);
		return el;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement el) {
		configureTestElement(el);
		if (el instanceof ResponseAssertion) {
			ResponseAssertion ra = (ResponseAssertion) el;

			ra.clearTestStrings();
			String[] testStrings = tableModel.getData().getColumn(COL_NAME);
			for (int i = 0; i < testStrings.length; i++) {
				ra.addTestString(testStrings[i]);
			}

			if (urlButton.isSelected()) {
				ra.setTestFieldURL();
			} else if (responseCodeButton.isSelected()) {
				ra.setTestFieldResponseCode();
			} else if (responseMessageButton.isSelected()) {
				ra.setTestFieldResponseMessage();
			} else {
				ra.setTestFieldResponseData();
			}

			ra.setAssumeSuccess(assumeSuccess.isSelected());

			if (containsBox.isSelected()) {
				ra.setToContainsType();
			} else if (equalsBox.isSelected()) {
                ra.setToEqualsType();
			} else {
				ra.setToMatchType();
			}

			if (notBox.isSelected()) {
				ra.setToNotType();
			} else {
				ra.unsetNotType();
			}
		}
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param el
	 *            the TestElement to configure
	 */
	public void configure(TestElement el) {
		super.configure(el);
		ResponseAssertion model = (ResponseAssertion) el;

		if (model.isContainsType()) {
			containsBox.setSelected(true);
			matchesBox.setSelected(false);
            equalsBox.setSelected(false);
        } else if (model.isEqualsType()) {
			containsBox.setSelected(false);
			matchesBox.setSelected(false);
            equalsBox.setSelected(true);
		} else {
			containsBox.setSelected(false);
			matchesBox.setSelected(true);
            equalsBox.setSelected(false);
		}

		if (model.isNotType()) {
			notBox.setSelected(true);
		} else {
			notBox.setSelected(false);
		}

		if (model.isTestFieldResponseData()) {
			responseStringButton.setSelected(true);
		} else if (model.isTestFieldResponseCode()) {
			responseCodeButton.setSelected(true);
		} else if (model.isTestFieldResponseMessage()) {
			responseMessageButton.setSelected(true);
		} else // Assume it is the URL
		{
			urlButton.setSelected(true);
		}

		assumeSuccess.setSelected(model.getAssumeSuccess());

		tableModel.clearData();
		PropertyIterator tests = model.getTestStrings().iterator();
		while (tests.hasNext()) {
			tableModel.addRow(new Object[] { tests.next().getStringValue() });
		}

		if (model.getTestStrings().size() == 0) {
			deletePattern.setEnabled(false);
		} else {
			deletePattern.setEnabled(true);
		}

		tableModel.fireTableDataChanged();
	}

	/**
	 * Initialize the GUI components and layout.
	 */
	private void init() {
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		setBorder(makeBorder());

		box.add(makeTitlePanel());
		box.add(createFieldPanel());
		box.add(createTypePanel());
		add(box, BorderLayout.NORTH);
		add(createStringPanel(), BorderLayout.CENTER);
	}

	/**
	 * Create a panel allowing the user to choose which response field should be
	 * tested.
	 * 
	 * @return a new panel for selecting the response field
	 */
	private JPanel createFieldPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_resp_field")));

		responseStringButton = new JRadioButton(JMeterUtils.getResString("assertion_text_resp"));
		urlButton = new JRadioButton(JMeterUtils.getResString("assertion_url_samp"));
		responseCodeButton = new JRadioButton(JMeterUtils.getResString("assertion_code_resp"));
		responseMessageButton = new JRadioButton(JMeterUtils.getResString("assertion_message_resp"));

		ButtonGroup group = new ButtonGroup();
		group.add(responseStringButton);
		group.add(urlButton);
		group.add(responseCodeButton);
		group.add(responseMessageButton);

		panel.add(responseStringButton);
		panel.add(urlButton);
		panel.add(responseCodeButton);
		panel.add(responseMessageButton);

		responseStringButton.setSelected(true);

		assumeSuccess = new JCheckBox(JMeterUtils.getResString("assertion_assume_success"));
		panel.add(assumeSuccess);

		return panel;
	}

	/**
	 * Create a panel allowing the user to choose what type of test should be
	 * performed.
	 * 
	 * @return a new panel for selecting the type of assertion test
	 */
	private JPanel createTypePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_pattern_match_rules")));

		ButtonGroup group = new ButtonGroup();

		containsBox = new JRadioButton(JMeterUtils.getResString("assertion_contains"));
		group.add(containsBox);
		containsBox.setSelected(true);
		panel.add(containsBox);

		matchesBox = new JRadioButton(JMeterUtils.getResString("assertion_matches"));
		group.add(matchesBox);
		panel.add(matchesBox);

		equalsBox = new JRadioButton(JMeterUtils.getResString("assertion_equals"));
		group.add(equalsBox);
		panel.add(equalsBox);

		notBox = new JCheckBox(JMeterUtils.getResString("assertion_not"));
		panel.add(notBox);

		return panel;
	}

	/**
	 * Create a panel allowing the user to supply a list of string patterns to
	 * test against.
	 * 
	 * @return a new panel for adding string patterns
	 */
	private JPanel createStringPanel() {
		tableModel = new PowerTableModel(new String[] { COL_NAME }, new Class[] { String.class });
		stringTable = new JTable(tableModel);

		TextAreaCellRenderer renderer = new TextAreaCellRenderer();
		stringTable.setRowHeight(renderer.getPreferredHeight());
		stringTable.setDefaultRenderer(String.class, renderer);
		stringTable.setDefaultEditor(String.class, new TextAreaTableCellEditor());
		stringTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_patterns_to_test")));

		panel.add(new JScrollPane(stringTable), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * Create a panel with buttons to add and delete string patterns.
	 * 
	 * @return the new panel with add and delete buttons
	 */
	private JPanel createButtonPanel() {
		addPattern = new JButton(JMeterUtils.getResString("add"));
		addPattern.addActionListener(new AddPatternListener());

		deletePattern = new JButton(JMeterUtils.getResString("delete"));
		deletePattern.addActionListener(new ClearPatternsListener());
		deletePattern.setEnabled(false);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addPattern);
		buttonPanel.add(deletePattern);
		return buttonPanel;
	}

	/**
	 * An ActionListener for deleting a pattern.
	 * 
	 * @author
	 * @version $Revision$ Last updated: $Date$
	 */
	private class ClearPatternsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int index = stringTable.getSelectedRow();
			if (index > -1) {
				stringTable.getCellEditor(index, stringTable.getSelectedColumn()).cancelCellEditing();
				tableModel.removeRow(index);
				tableModel.fireTableDataChanged();
			}
			if (stringTable.getModel().getRowCount() == 0) {
				deletePattern.setEnabled(false);
			}
		}
	}

	/**
	 * An ActionListener for adding a pattern.
	 * 
	 * @version $Revision$ Last updated: $Date$
	 */
	private class AddPatternListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			tableModel.addNewRow();
			deletePattern.setEnabled(true);
			tableModel.fireTableDataChanged();
		}
	}
}
