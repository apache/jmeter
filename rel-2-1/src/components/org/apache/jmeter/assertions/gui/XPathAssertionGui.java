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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * 
 * author <a href="mailto:jspears@astrology.com">Justin Spears </a>
 * 
 */

public class XPathAssertionGui extends AbstractAssertionGui {

	private XPathPanel xpath;

	private XMLConfPanel xml;

	public XPathAssertionGui() {
		init();
	}

	/**
	 * Returns the label to be shown within the JTree-Component.
	 */
	public String getLabelResource() {
		return "xpath_assertion_title"; //$NON-NLS-1$
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
		return JMeterUtils.getResString("xpath_assertion_test"); //$NON-NLS-1$
	}

	public void configure(TestElement el) {
		super.configure(el);
		XPathAssertion assertion = (XPathAssertion) el;
		xpath.setXPath(assertion.getXPathString());
		xpath.setNegated(assertion.isNegated());

		xml.setWhitespace(assertion.isWhitespace());
		xml.setValidate(assertion.isValidating());
		xml.setTolerant(assertion.isTolerant());
		xml.setNamespace(assertion.isNamespace());

	}

	private void init() {
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		setBorder(makeBorder());

		add(makeTitlePanel());

		// USER_INPUT
		JPanel sizePanel = new JPanel(new BorderLayout());
		sizePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				getXPathAttributesTitle()));
		xpath = new XPathPanel();
		sizePanel.add(xpath);

		xml = new XMLConfPanel();
		xml.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("xpath_assertion_option"))); //$NON-NLS-1$
		add(xml);

		add(sizePanel);
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement el) {
		super.configureTestElement(el);
		if (el instanceof XPathAssertion) {
			XPathAssertion assertion = (XPathAssertion) el;
			assertion.setValidating(xml.isValidate());
			assertion.setWhitespace(xml.isWhitespace());
			assertion.setTolerant(xml.isTolerant());
			assertion.setNamespace(xml.isNamespace());
			assertion.setNegated(xpath.isNegated());
			assertion.setXPathString(xpath.getXPath());
		}
	}
}
