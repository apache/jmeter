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
import java.awt.Dimension;

import javax.swing.Box;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Default JNDI screen. <br>
 * Created on: October 28, 2004
 * 
 * @author Martijn Blankestijn
 * @version $Id$
 */
public class JndiDefaultsGui extends AbstractConfigGui {
	private JLabeledTextField providerUrl;

	private JLabeledTextField initialContextFactory;

	public JndiDefaultsGui() {
		super();
		init();
	}

	public String getLabelResource() {
		return "jms_jndi_defaults_title";
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		ConfigTestElement config = new ConfigTestElement();
		modifyTestElement(config);
		return config;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement config) {
		super.configureTestElement(config);
		config.setProperty(JMSSampler.JNDI_CONTEXT_PROVIDER_URL, providerUrl.getText());
		config.setProperty(JMSSampler.JNDI_INITIAL_CONTEXT_FACTORY, initialContextFactory.getText());
		// config.setProperty(
		// new TestElementProperty(
		// HTTPSampler.ARGUMENTS,
		// argPanel.createTestElement()));
	}

	public void configure(TestElement el) {
		super.configure(el);
		providerUrl.setText(el.getPropertyAsString(JMSSampler.JNDI_CONTEXT_PROVIDER_URL));
		initialContextFactory.setText(el.getPropertyAsString(JMSSampler.JNDI_INITIAL_CONTEXT_FACTORY));
		// argPanel.configure(
		// (TestElement) el
		// .getProperty(HTTPSampler.ARGUMENTS)
		// .getObjectValue());
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		Box mainPanel = Box.createVerticalBox();

		VerticalPanel urlPanel = new VerticalPanel();
		providerUrl = new JLabeledTextField(JMeterUtils.getResString("initial_context_factory"));
		urlPanel.add(providerUrl);

		initialContextFactory = new JLabeledTextField(JMeterUtils.getResString("provider_url"));
		urlPanel.add(initialContextFactory);

		mainPanel.add(urlPanel);

		add(mainPanel, BorderLayout.CENTER);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
