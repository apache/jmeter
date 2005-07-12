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

package org.apache.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JCheckBox;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @version $Revision$
 */
public class HttpDefaultsGui extends AbstractConfigGui {
	JLabeledTextField protocol;

	JLabeledTextField domain;

	JLabeledTextField path;

	JLabeledTextField port;

	HTTPArgumentsPanel argPanel;

	private JCheckBox imageParser;

	public HttpDefaultsGui() {
		super();
		init();
	}

	public String getLabelResource() {
		return "url_config_title";
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
		config.setProperty(HTTPSamplerBase.PROTOCOL, protocol.getText());
		config.setProperty(HTTPSamplerBase.DOMAIN, domain.getText());
		config.setProperty(HTTPSamplerBase.PATH, path.getText());
		config.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, argPanel.createTestElement()));
		config.setProperty(HTTPSamplerBase.PORT, port.getText());
		if (imageParser.isSelected())
			config.setProperty(new BooleanProperty(HTTPSamplerBase.IMAGE_PARSER, true));
		else {
			config.removeProperty(HTTPSamplerBase.IMAGE_PARSER);
		}
	}

	public void configure(TestElement el) {
		super.configure(el);
		protocol.setText(el.getPropertyAsString(HTTPSamplerBase.PROTOCOL));
		domain.setText(el.getPropertyAsString(HTTPSamplerBase.DOMAIN));
		path.setText(el.getPropertyAsString(HTTPSamplerBase.PATH));
		port.setText(el.getPropertyAsString(HTTPSamplerBase.PORT));
		argPanel.configure((TestElement) el.getProperty(HTTPSamplerBase.ARGUMENTS).getObjectValue());
		imageParser.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSamplerBase.IMAGE_PARSER));
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		Box mainPanel = Box.createVerticalBox();

		VerticalPanel urlPanel = new VerticalPanel();
		protocol = new JLabeledTextField(JMeterUtils.getResString("url_config_protocol"));
		urlPanel.add(protocol);

		domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"));
		urlPanel.add(domain);

		path = new JLabeledTextField(JMeterUtils.getResString("path"));
		urlPanel.add(path);

		port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"));
		urlPanel.add(port);

		mainPanel.add(urlPanel);

		argPanel = new HTTPArgumentsPanel();
		mainPanel.add(argPanel);

		add(mainPanel, BorderLayout.CENTER);

		imageParser = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images"));
		add(imageParser, BorderLayout.SOUTH);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
