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

package org.apache.jmeter.protocol.jms.control.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.JLabeledRadio;
import org.apache.jorphan.gui.layout.VerticalLayout;

import org.apache.jmeter.protocol.jms.sampler.SubscriberSampler;

/**
 * This is the GUI for JMS Subscriber <br>
 * 
 */
public class JMSSubscriberGui extends AbstractSamplerGui implements java.awt.event.ActionListener, ChangeListener {

	private JCheckBox useProperties = 
        new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false); // $NON-NLS-1$

	private JLabeledTextField jndiICF = 
        new JLabeledTextField(JMeterUtils.getResString("jms_initial_context_factory")); // $NON-NLS-1$

	private JLabeledTextField urlField = 
        new JLabeledTextField(JMeterUtils.getResString("jms_provider_url")); // $NON-NLS-1$

	private JLabeledTextField jndiConnFac = 
        new JLabeledTextField(JMeterUtils.getResString("jms_connection_factory")); // $NON-NLS-1$

	private JLabeledTextField jmsTopic = 
        new JLabeledTextField(JMeterUtils.getResString("jms_topic")); // $NON-NLS-1$

	private JLabeledTextField jmsUser = 
        new JLabeledTextField(JMeterUtils.getResString("jms_user")); // $NON-NLS-1$

	private JLabeledTextField jmsPwd = 
        new JLabeledTextField(JMeterUtils.getResString("jms_pwd")); // $NON-NLS-1$

	private JLabeledTextField iterations = 
        new JLabeledTextField(JMeterUtils.getResString("jms_itertions")); // $NON-NLS-1$

	private static final String required = JMeterUtils.getResString("jms_auth_required"); // $NON-NLS-1$

	private static final String not_req = JMeterUtils.getResString("jms_auth_not_required"); // $NON-NLS-1$

	private static final String[] auth_items = { required, not_req };

	private JLabeledRadio reqAuth = 
        new JLabeledRadio(JMeterUtils.getResString("jms_authentication"), auth_items, not_req); // $NON-NLS-1$

	private JCheckBox readResponse = 
        new JCheckBox(JMeterUtils.getResString("jms_read_response"), true); // $NON-NLS-1$

	public static final String receive_str = JMeterUtils.getResString("jms_subscriber_receive"); // $NON-NLS-1$

	public static final String onmessage_str = JMeterUtils.getResString("jms_subscriber_on_message"); // $NON-NLS-1$

	private static final String[] client_items = { receive_str, onmessage_str };

	private JLabeledRadio clientChoice = 
        new JLabeledRadio(JMeterUtils.getResString("jms_client_type"), client_items, // $NON-NLS-1$
			receive_str);

	/**
	 * This is the font for the note.
	 */
	//Font plainText = new Font("plain", Font.PLAIN, 10); // $NON-NLS-1$

	private JPanel lookup = null;

	public JMSSubscriberGui() {
		init();
	}

	public String getLabelResource() {
		return "jms_subscriber_title"; // $NON-NLS-1$
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		SubscriberSampler sampler = new SubscriberSampler();
		this.configureTestElement(sampler);
		sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
		sampler.setJNDIIntialContextFactory(jndiICF.getText());
		sampler.setProviderUrl(urlField.getText());
		sampler.setConnectionFactory(jndiConnFac.getText());
		sampler.setTopic(jmsTopic.getText());
		sampler.setUsername(jmsUser.getText());
		sampler.setPassword(jmsPwd.getText());
		sampler.setUseAuth(reqAuth.getText());
		sampler.setIterations(iterations.getText());
		sampler.setReadResponse(String.valueOf(readResponse.isSelected()));
		sampler.setClientChoice(clientChoice.getText());
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement s) {
		SubscriberSampler sampler = (SubscriberSampler) s;
		this.configureTestElement(sampler);
		sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
		sampler.setJNDIIntialContextFactory(jndiICF.getText());
		sampler.setProviderUrl(urlField.getText());
		sampler.setConnectionFactory(jndiConnFac.getText());
		sampler.setTopic(jmsTopic.getText());
		sampler.setUsername(jmsUser.getText());
		sampler.setPassword(jmsPwd.getText());
		sampler.setUseAuth(reqAuth.getText());
		sampler.setIterations(iterations.getText());
		sampler.setReadResponse(String.valueOf(readResponse.isSelected()));
		sampler.setClientChoice(clientChoice.getText());
	}

	/**
	 * init() adds jndiICF to the mainPanel. The class reuses logic from
	 * SOAPSampler, since it is common.
	 */
	private void init() {
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(getStaticLabel());
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);
		// NAME
		mainPanel.add(getNamePanel());

		// Button for browsing webservice wsdl
		lookup = new JPanel();
		lookup.setLayout(new VerticalLayout(6, VerticalLayout.LEFT));
		mainPanel.add(lookup);
		lookup.add(useProperties);
		useProperties.addChangeListener(this);
		lookup.add(jndiICF);
		lookup.add(urlField);
		lookup.add(jndiConnFac);

		reqAuth.addChangeListener(this);

		JPanel commonParams = new JPanel();
		commonParams.setLayout(new VerticalLayout(6, VerticalLayout.LEFT));
		mainPanel.add(commonParams);
		commonParams.add(jmsTopic);
		commonParams.add(reqAuth);
		commonParams.add(jmsUser);
		commonParams.add(jmsPwd);
		commonParams.add(iterations);
		commonParams.add(readResponse);
		commonParams.add(clientChoice);

		// we have to add the gui to the change listener
		this.add(mainPanel);
	}

	/**
	 * the implementation loads the URL and the soap action for the request.
	 */
	public void configure(TestElement el) {
		super.configure(el);
		SubscriberSampler sampler = (SubscriberSampler) el;
		useProperties.setSelected(sampler.getUseJNDIPropertiesAsBoolean());
		jndiICF.setText(sampler.getJNDIInitialContextFactory());
		urlField.setText(sampler.getProviderUrl());
		jndiConnFac.setText(sampler.getConnectionFactory());
		jmsTopic.setText(sampler.getTopic());
		jmsUser.setText(sampler.getUsername());
		jmsPwd.setText(sampler.getPassword());
		iterations.setText(sampler.getIterations());
		reqAuth.setText(sampler.getUseAuth());
		readResponse.setSelected(sampler.getReadResponseAsBoolean());
		clientChoice.setText(sampler.getClientChoice());
	}

	public void clearGui(){
		super.clearGui();
		useProperties.setSelected(false); // $NON-NLS-1$
		jndiICF.setText(""); // $NON-NLS-1$
		urlField.setText(""); // $NON-NLS-1$
		jndiConnFac.setText(""); // $NON-NLS-1$
		jmsTopic.setText(""); // $NON-NLS-1$
		jmsUser.setText(""); // $NON-NLS-1$
		jmsPwd.setText(""); // $NON-NLS-1$
		iterations.setText(""); // $NON-NLS-1$
		reqAuth.setText(""); // $NON-NLS-1$
		readResponse.setSelected(true);
		clientChoice.setText(""); // $NON-NLS-1$
	}

	/**
	 * method from ActionListener
	 * 
	 * @param event
	 *            that occurred
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == useProperties) {
		}
	}

	/**
	 * When the state of a widget changes, it will notify the gui. the method
	 * then enables or disables certain parameters.
	 */
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == useProperties) {
			if (useProperties.isSelected()) {
				this.jndiICF.setEnabled(false);
				this.urlField.setEnabled(false);
			} else {
				this.jndiICF.setEnabled(true);
				this.urlField.setEnabled(true);
			}
		}
	}
}