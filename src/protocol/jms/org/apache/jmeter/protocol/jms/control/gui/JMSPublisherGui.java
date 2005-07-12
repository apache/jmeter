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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jorphan.gui.JLabeledRadio;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.layout.VerticalLayout;

import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;

/**
 * This is the GUI for JMS Publisher <br>
 * Created on: October 13, 2003
 * 
 * @author Peter Lin
 * @version $Id$
 */
public class JMSPublisherGui extends AbstractSamplerGui implements java.awt.event.ActionListener, ChangeListener {
	public static final String use_file = JMeterUtils.getResString("jms_use_file");

	public static final String use_random = JMeterUtils.getResString("jms_use_random_file");

	public static final String use_text = JMeterUtils.getResString("jms_use_text");

	private String[] items = { use_file, use_random, use_text };

	private String text_msg = JMeterUtils.getResString("jms_text_message");

	private String object_msg = JMeterUtils.getResString("jms_object_message");

	private String[] msgTypes = { text_msg, object_msg };

	private String required = JMeterUtils.getResString("jms_auth_required");

	private String not_req = JMeterUtils.getResString("jms_auth_not_required");

	private String[] auth_items = { required, not_req };

	JCheckBox useProperties = new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false);

	JLabeledRadio configChoice = new JLabeledRadio(JMeterUtils.getResString("jms_config"), items, use_text);

	JLabeledTextField jndiICF = new JLabeledTextField(JMeterUtils.getResString("jms_initial_context_factory"));

	JLabeledTextField urlField = new JLabeledTextField(JMeterUtils.getResString("jms_provider_url"));

	JLabeledTextField jndiConnFac = new JLabeledTextField(JMeterUtils.getResString("jms_connection_factory"));

	JLabeledTextField jmsTopic = new JLabeledTextField(JMeterUtils.getResString("jms_topic"));

	JLabeledRadio reqAuth = new JLabeledRadio(JMeterUtils.getResString("jms_authentication"), auth_items, not_req);

	JLabeledTextField jmsUser = new JLabeledTextField(JMeterUtils.getResString("jms_user"));

	JLabeledTextField jmsPwd = new JLabeledTextField(JMeterUtils.getResString("jms_pwd"));

	JLabeledTextField iterations = new JLabeledTextField(JMeterUtils.getResString("jms_itertions"));

	FilePanel messageFile = new FilePanel(JMeterUtils.getResString("jms_file"), "*.*");

	FilePanel randomFile = new FilePanel(JMeterUtils.getResString("jms_random_file"), "*.*");

	JLabeledTextArea textMessage = new JLabeledTextArea(text_msg, null);

	JLabeledRadio msgChoice = new JLabeledRadio(JMeterUtils.getResString("jms_message_type"), msgTypes, text_msg);

	/**
	 * This is the font for the note.
	 */
	Font plainText = new Font("plain", Font.PLAIN, 10);

	private JPanel lookup = null;

	private JPanel messagePanel = null;

	public JMSPublisherGui() {
		init();
	}

	/**
	 * the name of the property for the JMSPublisherGui is jms_publisher.
	 */
	public String getLabelResource() {
		return "jms_publisher";
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		PublisherSampler sampler = new PublisherSampler();
		this.configureTestElement(sampler);
		sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
		sampler.setJNDIIntialContextFactory(jndiICF.getText());
		sampler.setProviderUrl(urlField.getText());
		sampler.setConnectionFactory(jndiConnFac.getText());
		sampler.setTopic(jmsTopic.getText());
		sampler.setUsername(jmsUser.getText());
		sampler.setPassword(jmsPwd.getText());
		sampler.setTextMessage(textMessage.getText());
		sampler.setInputFile(messageFile.getFilename());
		sampler.setRandomPath(randomFile.getFilename());
		sampler.setConfigChoice(configChoice.getText());
		sampler.setMessageChoice(msgChoice.getText());
		sampler.setIterations(iterations.getText());
		sampler.setUseAuth(reqAuth.getText());
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement s) {
		PublisherSampler sampler = (PublisherSampler) s;
		this.configureTestElement(sampler);
		sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
		sampler.setJNDIIntialContextFactory(jndiICF.getText());
		sampler.setProviderUrl(urlField.getText());
		sampler.setConnectionFactory(jndiConnFac.getText());
		sampler.setTopic(jmsTopic.getText());
		sampler.setUsername(jmsUser.getText());
		sampler.setPassword(jmsPwd.getText());
		sampler.setTextMessage(textMessage.getText());
		sampler.setInputFile(messageFile.getFilename());
		sampler.setRandomPath(randomFile.getFilename());
		sampler.setConfigChoice(configChoice.getText());
		sampler.setMessageChoice(msgChoice.getText());
		sampler.setIterations(iterations.getText());
		sampler.setUseAuth(reqAuth.getText());
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

		configChoice.addChangeListener(this);
		msgChoice.addChangeListener(this);
		reqAuth.addChangeListener(this);

		JPanel commonParams = new JPanel();
		commonParams.setLayout(new VerticalLayout(6, VerticalLayout.LEFT));
		mainPanel.add(commonParams);
		commonParams.add(jmsTopic);
		commonParams.add(reqAuth);
		commonParams.add(jmsUser);
		commonParams.add(jmsPwd);
		commonParams.add(iterations);

		messagePanel = new JPanel();
		messagePanel.setLayout(new VerticalLayout(3, VerticalLayout.LEFT));
		messagePanel.add(configChoice);
		messagePanel.add(msgChoice);
		messagePanel.add(messageFile);
		messagePanel.add(randomFile);
		messagePanel.add(textMessage);
		mainPanel.add(messagePanel);

		Dimension pref = new Dimension(400, 200);
		textMessage.setPreferredSize(pref);

		// we have to add the gui to the change listener
		this.add(mainPanel);
	}

	/**
	 * the implementation loads the URL and the soap action for the request.
	 */
	public void configure(TestElement el) {
		super.configure(el);
		PublisherSampler sampler = (PublisherSampler) el;
		useProperties.setSelected(sampler.getUseJNDIPropertiesAsBoolean());
		jndiICF.setText(sampler.getJNDIInitialContextFactory());
		urlField.setText(sampler.getProviderUrl());
		jndiConnFac.setText(sampler.getConnectionFactory());
		jmsTopic.setText(sampler.getTopic());
		jmsUser.setText(sampler.getUsername());
		jmsPwd.setText(sampler.getPassword());
		textMessage.setText(sampler.getTextMessage());
		messageFile.setFilename(sampler.getInputFile());
		randomFile.setFilename(sampler.getRandomPath());
		configChoice.setText(sampler.getConfigChoice());
		msgChoice.setText(sampler.getMessageChoice());
		updateConfig(sampler.getConfigChoice());
		iterations.setText(sampler.getIterations());
		reqAuth.setText(sampler.getUseAuth());
	}

	/**
	 * method from ActionListener
	 * 
	 * @param event
	 *            that occurred
	 */
	public void actionPerformed(ActionEvent event) {
	}

	/**
	 * When a widget state changes, it will notify this class so we can
	 * enable/disable the correct items.
	 */
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == this.configChoice) {
			updateConfig(this.configChoice.getText());
		} else if (event.getSource() == this.msgChoice) {
			updateMessageType(this.msgChoice.getText());
		} else if (event.getSource() == useProperties) {
			if (useProperties.isSelected()) {
				this.jndiICF.setEnabled(false);
				this.urlField.setEnabled(false);
			} else {
				this.jndiICF.setEnabled(true);
				this.urlField.setEnabled(true);
			}
		}
	}

	/**
	 * Update config contains the actual logic for enabling or disabling text
	 * message, file or random path.
	 * 
	 * @param command
	 */
	public void updateConfig(String command) {
		if (command.equals(use_text)) {
			textMessage.setEnabled(true);
			messageFile.enableFile(false);
			randomFile.enableFile(false);
		} else if (command.equals(use_random)) {
			textMessage.setEnabled(false);
			messageFile.enableFile(false);
			randomFile.enableFile(true);
		} else {
			textMessage.setEnabled(false);
			messageFile.enableFile(true);
			randomFile.enableFile(false);
		}
	}

	/**
	 * 
	 * @param msgType
	 */
	public void updateMessageType(String msgType) {
		if (msgType.equals(object_msg)) {
			if (configChoice.getText().equals(use_text)) {
				JOptionPane.showConfirmDialog(this, JMeterUtils.getResString("jms_error_msg"), "Warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
