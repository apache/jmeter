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
//import java.awt.Dimension;

//import javax.swing.JComboBox;
//import javax.swing.JPanel;

//import org.apache.jmeter.config.Arguments;
//import org.apache.jmeter.config.gui.ArgumentsPanel;
//import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
//import org.apache.jmeter.util.JMeterUtils;
//import org.apache.jorphan.gui.JLabeledChoice;
//import org.apache.jorphan.gui.JLabeledTextArea;
//import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @author MBlankestijn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JmsTestSampleGui extends AbstractSamplerGui {
/*	private static final String DEFAULT_QUEUE = "LQ.I.PERF";
	private static final String DEFAULT_QUEUECONNECTION = "localhost";

	private JLabeledTextField queueuConnectionFactory =
		new JLabeledTextField(
			JMeterUtils.getResString("jms_queue_connection_factory"));
	private JLabeledTextField sendQueue =
		new JLabeledTextField(JMeterUtils.getResString("jms_send_queue"));
	private JLabeledTextField receiveQueue =
		new JLabeledTextField(JMeterUtils.getResString("jms_receive_queue"));
	private JLabeledTextField timeout =
		new JLabeledTextField(JMeterUtils.getResString("jms_timeout"));
	private JLabeledTextArea soapXml =
		new JLabeledTextArea(JMeterUtils.getResString("jms_msg_content"), null);
	private JLabeledTextField initialContextFactory = 
		new JLabeledTextField(JMeterUtils.getResString("initial_context_factory"));
	private JLabeledTextField providerUrl = 
		new JLabeledTextField(JMeterUtils.getResString("provider_url"));
	
	private String[] labels =
		new String[] {
			JMeterUtils.getResString("jms_request"),
			JMeterUtils.getResString("jms_requestreply")};
	private JLabeledChoice oneWay =
		new JLabeledChoice(
			JMeterUtils.getResString("jms_communication_style"),
			labels);
	private ArgumentsPanel jmsPropertiesPanel;
//	private ArgumentsPanel jndiPropertiesPanel;

	private JMSSampler sampler;
*/
	private JMSConfigGui jmsConfigGui;

	public JmsTestSampleGui() {
		init();
		
	}

	public void configure(TestElement element) {
		//System.out.println("configure " + element.getClass().getName() + ": " + element.hashCode());
		super.configure(element);
		jmsConfigGui.configure(element);
/*		jmsPropertiesPanel.configure(
			(Arguments) element
				.getProperty(JMSSampler.JMS_PROPERTIES)
				.getObjectValue());

//		jndiPropertiesPanel.configure(
//			(Arguments) element
//				.getProperty(JMSSampler.JNDI_PROPERTIES)
//				.getObjectValue());
		
		sampler = (JMSSampler) element;
		String qcf = null;
		if (sampler.getQueueConnectionFactory().length() > 0) {
			qcf = sampler.getQueueConnectionFactory();
		} else {
			qcf = DEFAULT_QUEUECONNECTION;
		}
		JComboBox box = (JComboBox) oneWay.getComponentList().get(1);
		String selected = null;
		if (sampler.getIsOneway()) {
			selected = JMeterUtils.getResString("jms_request");
		}
		else {
			selected = JMeterUtils.getResString("jms_requestreply");
		}
		box.setSelectedItem(selected);
		queueuConnectionFactory.setText(qcf);
		sendQueue.setText(sampler.getSendQueue());
		receiveQueue.setText(sampler.getReceiveQueue());
		soapXml.setText(sampler.getContent());
		initialContextFactory.setText(sampler.getInitialContextFactory());
		providerUrl.setText(sampler.getContextProvider());
		timeout.setText(String.valueOf(sampler.getTimeout()));
		jmsPropertiesPanel.configure(
			(Arguments) sampler
				.getProperty(JMSSampler.JMS_PROPERTIES)
				.getObjectValue());

//		jndiPropertiesPanel.configure(
//			(Arguments) element
//				.getProperty(JMSSampler.JNDI_PROPERTIES)
//				.getObjectValue());
*/	}

	public String getLabelResource() {
		return "jms_testing_title";
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		JMSSampler sampler = new JMSSampler();
		modifyTestElement(sampler);
/*		if (sampler == null) {
			System.out.println("createTestElement");
			sampler = new JMSSampler();
			this.configureTestElement(sampler);
			fillSampler();
		}
*/		return sampler;
	}
	private boolean isNumeric(String string) {
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				return false;
			}
		}
		return true;
	}
/*
	private void fillSampler() {
		sampler.setQueueConnectionFactory(
			queueuConnectionFactory.getText().trim());
		sampler.setSendQueue(sendQueue.getText().trim());
		sampler.setReceiveQueue(receiveQueue.getText().trim());
		sampler.setContent(soapXml.getText().trim());
		sampler.setInitialContextFactory(initialContextFactory.getText().trim());
		sampler.setContextProvider(providerUrl.getText().trim());
		sampler.setArguments((Arguments) jmsPropertiesPanel.createTestElement());
//		sampler.setJndiProperties((Arguments) jndiPropertiesPanel.createTestElement());
				
		if (isNumeric(timeout.getText())) {
			sampler.setTimeout(Integer.parseInt(timeout.getText()));
		}	
		
		System.out.println(
			"ONEWAY: "
				+ oneWay.getText());
		sampler.setIsOneway(
			oneWay.getText().equals(JMeterUtils.getResString("jms_request")));

	}
*/
	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		TestElement e1 = jmsConfigGui.createTestElement();
		element.clear();
		element.addTestElement(e1);
		this.configureTestElement(element);
/*		
		sampler = (JMSSampler) element;
		this.configureTestElement(sampler);
		fillSampler();
*/
	}

	/**
	 * @param sampler
	private void printSampler(JMSSampler sampler) {
		System.out.println(
			"SAM...QCF="
				+ sampler.getQueueConnectionFactory()
				+ ", queue="
				+ sampler.getSendQueue());

	}
	 */

	/**
	 * 
	private void printGUI() {
		//		System.out.println("GUI...QCF=" + queueuConnectionFactoryField.getText() + ", queue=" + sendQueueField.getText());
		System.out.println(
			"GUI...QCF="
				+ queueuConnectionFactory.getText()
				+ ", queue="
				+ sendQueue.getText());

	}
	 */

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

//		JPanel mainPanel = new JPanel(new BorderLayout(0, 5));

		//mainPanel.add(createJMSPanel());
//		mainPanel.add(jmsConfigGui, BorderLayout.CENTER);

		jmsConfigGui = new JMSConfigGui();
		add(jmsConfigGui, BorderLayout.CENTER);

//		System.out.println("End of Init of JmsTestSampleGui");


//		add(mainPanel, BorderLayout.CENTER);
	}
/*
 	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
*/
/*
	private JPanel createJMSPanel() {
		JPanel qcfPanel = new JPanel(new BorderLayout(5, 0));
		qcfPanel.add(queueuConnectionFactory, BorderLayout.CENTER);

		JPanel onewayPanel = new JPanel(new BorderLayout(5, 0));
		onewayPanel.add(oneWay);

		JPanel sendQueuePanel = new JPanel(new BorderLayout(5, 0));
		sendQueuePanel.add(sendQueue);

		JPanel receiveQueuePanel = new JPanel(new BorderLayout(5, 0));
		receiveQueuePanel.add(receiveQueue);

		JPanel timeoutPanel = new JPanel(new BorderLayout(5, 0));
		timeoutPanel.add(timeout);

		JPanel soapXmlPanel = new JPanel(new BorderLayout(10, 0));
		soapXmlPanel.add(soapXml);

		JPanel contextPanel = new JPanel(new BorderLayout(10, 0));
		contextPanel.add(initialContextFactory);
		
		JPanel providerPanel = new JPanel(new BorderLayout(10, 0));
		providerPanel.add(providerUrl);


		VerticalPanel jmsPanel = new VerticalPanel();
		jmsPanel.add(qcfPanel);
		jmsPanel.add(oneWay);
		jmsPanel.add(sendQueuePanel);
		jmsPanel.add(receiveQueuePanel);
		jmsPanel.add(timeoutPanel);
		jmsPanel.add(soapXmlPanel);
		jmsPropertiesPanel = new ArgumentsPanel(JMeterUtils.getResString("jms_props"));
		jmsPanel.add(jmsPropertiesPanel);
		jmsPanel.add(contextPanel);
		jmsPanel.add(providerPanel);
//		jndiPropertiesPanel = new ArgumentsPanel(JMeterUtils.getResString("jms_jndi_props"));
//		jmsPanel.add(jndiPropertiesPanel);		

		return jmsPanel;
	}
*/
}