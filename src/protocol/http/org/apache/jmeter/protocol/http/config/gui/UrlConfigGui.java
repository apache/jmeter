/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;

/**
 * @author Michael Stover
 */
public class UrlConfigGui extends JPanel {
	protected HTTPArgumentsPanel argsPanel;

	private static String DOMAIN = "domain"; // $NON-NLS-1$

	private static String PORT = "port"; // $NON-NLS-1$

	private static String PROTOCOL = "protocol"; // $NON-NLS-1$

	private static String PATH = "path"; // $NON-NLS-1$

	private static String FOLLOW_REDIRECTS = "follow_redirects"; // $NON-NLS-1$

	private static String AUTO_REDIRECTS = "auto_redirects"; // $NON-NLS-1$

	private static String USE_KEEPALIVE = "use_keepalive"; // $NON-NLS-1$

	private JTextField domain;

	private JTextField port;

	private JTextField protocol;

	private JTextField path;

	private JCheckBox followRedirects;

	private JCheckBox autoRedirects;

	private JCheckBox useKeepAlive;

    private JLabeledChoice method;
    
	public UrlConfigGui() {
		init();
	}

	protected void configureTestElement(TestElement mc) {
		mc.setProperty(TestElement.NAME, getName());
		mc.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		mc.setProperty(TestElement.TEST_CLASS, mc.getClass().getName());
	}

	public void clear() {
		domain.setText(""); // $NON-NLS-1$
		followRedirects.setSelected(true);
		autoRedirects.setSelected(false);
        method.setText(HTTPSamplerBase.DEFAULT_METHOD);
		path.setText(""); // $NON-NLS-1$
		port.setText(""); // $NON-NLS-1$
		protocol.setText(""); // $NON-NLS-1$
		useKeepAlive.setSelected(true);
		argsPanel.clear();

	}

	public TestElement createTestElement() {
		ConfigTestElement element = new ConfigTestElement();

		this.configureTestElement(element);
		Arguments args = (Arguments) argsPanel.createTestElement();

		HTTPArgument.convertArgumentsToHTTP(args);
		element.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, args));
		element.setProperty(HTTPSamplerBase.DOMAIN, domain.getText());
		element.setProperty(HTTPSamplerBase.PORT, port.getText());
		element.setProperty(HTTPSamplerBase.PROTOCOL, protocol.getText());
		element.setProperty(HTTPSamplerBase.METHOD, method.getText());
		element.setProperty(HTTPSamplerBase.PATH, path.getText());
		element.setProperty(new BooleanProperty(HTTPSamplerBase.FOLLOW_REDIRECTS, followRedirects.isSelected()));
		element.setProperty(new BooleanProperty(HTTPSamplerBase.AUTO_REDIRECTS, autoRedirects.isSelected()));
		element.setProperty(new BooleanProperty(HTTPSamplerBase.USE_KEEPALIVE, useKeepAlive.isSelected()));
		return element;
	}

	/**
	 * Set the text, etc. in the UI.
	 * 
	 * @param el
	 *            contains the data to be displayed
	 */
	public void configure(TestElement el) {
		setName(el.getPropertyAsString(TestElement.NAME));
		argsPanel.configure((TestElement) el.getProperty(HTTPSamplerBase.ARGUMENTS).getObjectValue());
		domain.setText(el.getPropertyAsString(HTTPSamplerBase.DOMAIN));

		String portString = el.getPropertyAsString(HTTPSamplerBase.PORT);

		// Only display the port number if it is meaningfully specified
		if (portString.equals(HTTPSamplerBase.UNSPECIFIED_PORT_AS_STRING)) {
			port.setText(""); // $NON-NLS-1$
		} else {
			port.setText(portString);
		}
		protocol.setText(el.getPropertyAsString(HTTPSamplerBase.PROTOCOL));
        method.setText(el.getPropertyAsString(HTTPSamplerBase.METHOD));
		path.setText(el.getPropertyAsString(HTTPSamplerBase.PATH));
		followRedirects.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSamplerBase.FOLLOW_REDIRECTS));

		autoRedirects.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSamplerBase.AUTO_REDIRECTS));
		useKeepAlive.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSamplerBase.USE_KEEPALIVE));
	}

	protected void init() {
		this.setLayout(new BorderLayout());

		JPanel webServerPanel = new JPanel();

		webServerPanel.setLayout(new BorderLayout());
		webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("web_server"))); // $NON-NLS-1$
		webServerPanel.add(getDomainPanel(), BorderLayout.NORTH);
		webServerPanel.add(getPortPanel(), BorderLayout.WEST);

		JPanel webRequestPanel = new JPanel();

		webRequestPanel.setLayout(new BorderLayout());
		webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("web_request"))); // $NON-NLS-1$
		JPanel northPanel = new JPanel(new BorderLayout());

		northPanel.add(getProtocolAndMethodPanel(), BorderLayout.NORTH);
		northPanel.add(getPathPanel(), BorderLayout.SOUTH);
		webServerPanel.add(northPanel, BorderLayout.SOUTH);
		webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

		this.add(webServerPanel, BorderLayout.NORTH);
		this.add(webRequestPanel, BorderLayout.CENTER);
	}

	protected JPanel getPortPanel() {
		port = new JTextField(6);
		port.setName(PORT);

		JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
		label.setLabelFor(port);

		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(label, BorderLayout.WEST);
		panel.add(port, BorderLayout.CENTER);

		return panel;
	}

	protected JPanel getDomainPanel() {
		domain = new JTextField(20);
		domain.setName(DOMAIN);

		JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
		label.setLabelFor(domain);

		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(label, BorderLayout.WEST);
		panel.add(domain, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * This method defines the Panel for the HTTP path, 'Follow Redirects' and
	 * 'Use KeepAlive' elements.
	 * 
	 * @return JPanel The Panel for the path, 'Follow Redirects' and 'Use
	 *         KeepAlive' elements.
	 */
	protected Component getPathPanel() {
		path = new JTextField(15);
		path.setName(PATH);

		JLabel label = new JLabel(JMeterUtils.getResString("path"));
		label.setLabelFor(path);

		autoRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects_auto"));
		autoRedirects.setName(AUTO_REDIRECTS);
		autoRedirects.setSelected(false);// will be reset by
											// configure(TestElement)

		followRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects")); // $NON-NLS-1$
		followRedirects.setName(FOLLOW_REDIRECTS);
		followRedirects.setSelected(true);

		useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive")); // $NON-NLS-1$
		useKeepAlive.setName(USE_KEEPALIVE);
		useKeepAlive.setSelected(true);

		JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
		pathPanel.add(label, BorderLayout.WEST);
		pathPanel.add(path, BorderLayout.CENTER);
		pathPanel.setMinimumSize(pathPanel.getPreferredSize());

		JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		optionPanel.add(autoRedirects);
		optionPanel.add(followRedirects);
		optionPanel.add(useKeepAlive);
		optionPanel.setMinimumSize(optionPanel.getPreferredSize());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(pathPanel);
		panel.add(optionPanel);
		return panel;
	}

	protected JPanel getProtocolAndMethodPanel() {
		// PROTOCOL
		protocol = new JTextField(20);
		protocol.setName(PROTOCOL);

		JLabel protocolLabel = new JLabel(JMeterUtils.getResString("protocol")); // $NON-NLS-1$
		protocolLabel.setLabelFor(protocol);
        method = new JLabeledChoice(JMeterUtils.getResString("method"), // $NON-NLS-1$
                HTTPSamplerBase.getValidMethodsAsArray());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		panel.add(protocolLabel);
		panel.add(protocol);
		panel.add(Box.createHorizontalStrut(5));

        panel.add(method);
		panel.setMinimumSize(panel.getPreferredSize());
		return panel;
	}

	protected JPanel getParameterPanel() {
		argsPanel = new HTTPArgumentsPanel();

		return argsPanel;
	}
}
