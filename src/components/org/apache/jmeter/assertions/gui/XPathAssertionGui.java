/*
 * Copyright 2005 The Apache Software Foundation.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * author <a href="mailto:jspears@astrology.com">Justin Spears </a>
 * 
 */

public class XPathAssertionGui extends AbstractAssertionGui implements
		FocusListener, ActionListener {
	
	private static transient Logger log = LoggingManager.getLoggerForClass();

	private JTextField xpath;

	private JCheckBox validation, whitespace, tidy, negated;

	private JButton checkXPath;

	private static Document testDoc = null; // Used to validate XPath expressions

	/**
	 * The constructor.
	 */
	public XPathAssertionGui() {
		init();
	}

	/**
	 * Returns the label to be shown within the JTree-Component.
	 */
	public String getLabelResource() {
		return "xpath_assertion_title";
	}

	/**
	 * Create test element
	 */
	public TestElement createTestElement() {

		XPathAssertion el = new XPathAssertion();
		modifyTestElement(el);
		return el;
	}

	public String getXPathAttributesTitle() {
		return JMeterUtils.getResString("xpath_assertion_test");
	}

	public void focusGained(FocusEvent e) {
		log.debug("XPathAssertionGui.focusGained() called");
	}

	public void focusLost(FocusEvent e) {

	}

	public void configure(TestElement el) {
		super.configure(el);
		XPathAssertion assertion = (XPathAssertion) el;
		xpath.setText(assertion.getXPathString());
		whitespace.setSelected(assertion.isWhitespace());
		validation.setSelected(assertion.isValidating());
		negated.setSelected(assertion.isNegated());
		tidy.setSelected(assertion.isJTidy());
		tidySelected();
	}

	private void init() {
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		setBorder(makeBorder());

		add(makeTitlePanel());

		// USER_INPUT
		JPanel sizePanel = new JPanel(new BorderLayout());
		sizePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), getXPathAttributesTitle()));

		negated = new JCheckBox(JMeterUtils
				.getResString("xpath_assertion_negate"), false);
		xpath = new JTextField(50);
		xpath.setText(XPathAssertion.DEFAULT_XPATH);
		checkXPath = new JButton(JMeterUtils
				.getResString("xpath_assertion_button"));
		checkXPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				validXPath(xpath.getText());
			}
		});

		xpath.addFocusListener(this);
		sizePanel.add(xpath, BorderLayout.WEST);
		sizePanel.add(checkXPath, BorderLayout.EAST);
		sizePanel.add(negated, BorderLayout.PAGE_END);

		JPanel optionPanel = new JPanel();
		optionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), JMeterUtils
				.getResString("xpath_assertion_option")));

		validation = new JCheckBox(JMeterUtils
				.getResString("xpath_assertion_validation"), false);
		whitespace = new JCheckBox(JMeterUtils
				.getResString("xpath_assertion_whitespace"), false);
		tidy = new JCheckBox(JMeterUtils.getResString("xpath_assertion_tidy"),
				false);
		tidy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tidySelected();
			}
		});

		optionPanel.add(validation);
		optionPanel.add(whitespace);
		optionPanel.add(tidy);

		add(sizePanel);
		add(optionPanel);
	}

	public void actionPerformed(ActionEvent e) {
		// Actions are handled by individual listeners
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement el) {
		super.configureTestElement(el);
		String xpathString = xpath.getText();

		XPathAssertion assertion = (XPathAssertion) el;
		assertion.setValidating(validation.isSelected());
		assertion.setWhitespace(whitespace.isSelected());
		assertion.setJTidy(tidy.isSelected());
		assertion.setNegated(negated.isSelected());
		/*
		 * Should I really check this? If someone really wants an invalid xpath,
		 * why should i stop them.... I am commenting it out. I offer them a
		 * button to do the validation, so if they don't use it, it ain't my
		 * problem. if (validXPath(xpathString, false))
		 * 
		 * An XPath might need to be feed through a function, in which case it is
		 * invalid now, but perhaps valid when executed.
		 */
		if (xpathString == null || xpathString.length() == 0)
			xpathString = XPathAssertion.DEFAULT_XPATH;
		assertion.setXPathString(xpathString);
	}

	/*
	 * Set the options according to validness, let the user know that tidy is
	 * not compatible with validation.
	 */
	private void tidySelected() {
		if (tidy.isSelected()) {
			validation.setEnabled(false);
			whitespace.setEnabled(false);
		} else {
			validation.setEnabled(true);
			whitespace.setEnabled(true);
		}
	}

	/**
	 * Test weather an XPath is valid. It seems the Xalan has no easy way to tes
	 * this. so it creates a test document, then tries to evaluate the xpath.
	 * 
	 * @param xpathString
	 *            XPath String to validate
	 * @param showDialog
	 *            weather to show a dialog
	 * @return returns true if valid, valse otherwise.
	 */
	private boolean validXPath(String xpathString) {
		String ret = null;
		boolean success = true;
		try {
			if (testDoc == null) {

				testDoc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().newDocument();
				Element el = testDoc.createElement("root");
				testDoc.appendChild(el);
			}
			if (XPathAPI.eval(testDoc, xpathString) == null) {
				// We really should never get here
				// because eval will throw an exception
				// if xpath is invalid, but whatever, better
				// safe
				log.warn("xpath eval was null ");
				ret = "xpath eval was null";
				success = false;
			}

		} catch (ParserConfigurationException e) {
			success = false;
			ret = e.getLocalizedMessage();
		} catch (TransformerException e) {
			success = false;
			ret = e.getLocalizedMessage();
		}
		JOptionPane.showMessageDialog(null, (success) ? JMeterUtils
				.getResString("xpath_assertion_valid") : ret,
				(success) ? JMeterUtils.getResString("xpath_assertion_valid")
						: JMeterUtils.getResString("xpath_assertion_failed"),
				(success) ? JOptionPane.INFORMATION_MESSAGE
						: JOptionPane.ERROR_MESSAGE);

		return success;

	}

}
