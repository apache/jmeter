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

package org.apache.jmeter.protocol.jms.control.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;


/**
 * User interface for point-to-point messaging with JMS.
 * <br>
 * Created on:  October 28, 2004
 *
 * @author Martijn Blankestijn
 * @version $Id$ 
 */
public class JmsTestSampleGui extends AbstractSamplerGui {
	/** The user interface. */
	private JMSConfigGui jmsConfigGui;

	/**
	 * Default constructor.
	 */
	public JmsTestSampleGui() {
		init();
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
	 */
	public void configure(TestElement element) {
		super.configure(element);
		jmsConfigGui.configure(element);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	public String getLabelResource() {
		return "jms_testing_title";
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		JMSSampler sampler = new JMSSampler();
		modifyTestElement(sampler);
		return sampler;
	}
	
	/**
	 * Is the supplied <code>String</code> numeric?
	 * TODO Somewhere there must be duplicate function, but where?
	 * @param string the string to check
	 * @return true if the <code>string</code>is numeric.
	 */
	private static boolean isNumeric(String string) {
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				return false;
			}
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		TestElement e1 = jmsConfigGui.createTestElement();
		element.clear();
		element.addTestElement(e1);
		this.configureTestElement(element);
	}

	/**
	 * Initializes the component.
	 *
	 */
	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		jmsConfigGui = new JMSConfigGui();
		add(jmsConfigGui, BorderLayout.CENTER);
	}
}