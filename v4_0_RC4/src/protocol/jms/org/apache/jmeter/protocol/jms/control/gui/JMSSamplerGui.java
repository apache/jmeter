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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.jms.sampler.JMSProperties;
import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Configuration screen for Java Messaging Point-to-Point requests.
 */
public class JMSSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 240L;

    private JLabeledTextField queueConnectionFactory = new JLabeledTextField(
            JMeterUtils.getResString("jms_queue_connection_factory")); //$NON-NLS-1$

    private JLabeledTextField sendQueue = new JLabeledTextField(JMeterUtils.getResString("jms_send_queue")); //$NON-NLS-1$

    private JLabeledTextField receiveQueue = new JLabeledTextField(JMeterUtils.getResString("jms_receive_queue")); //$NON-NLS-1$

    private JLabeledTextField timeout = new JLabeledTextField(JMeterUtils.getResString("jms_timeout"), 10); //$NON-NLS-1$

    private JLabeledTextField expiration = new JLabeledTextField(JMeterUtils.getResString("jms_expiration"), 10); //$NON-NLS-1$

    private JLabeledTextField priority = new JLabeledTextField(JMeterUtils.getResString("jms_priority"), 1); //$NON-NLS-1$

    private JLabeledTextField jmsSelector = new JLabeledTextField(JMeterUtils.getResString("jms_selector")); //$NON-NLS-1$

    private JLabeledTextField numberOfSamplesToAggregate = new JLabeledTextField("Number of samples to aggregate"); //$NON-NLS-1$

    private JSyntaxTextArea messageContent = JSyntaxTextArea.getInstance(10, 50); // $NON-NLS-1$

    private JLabeledTextField initialContextFactory = new JLabeledTextField(
            JMeterUtils.getResString("jms_initial_context_factory")); //$NON-NLS-1$

    private JLabeledTextField providerUrl = new JLabeledTextField(JMeterUtils.getResString("jms_provider_url")); //$NON-NLS-1$

    private static final String[] JMS_COMMUNICATION_STYLE_LABELS = new String[] { 
            "request_only", // $NON-NLS-1$
            "request_reply", // $NON-NLS-1$
            "read", // $NON-NLS-1$
            "browse", // $NON-NLS-1$
            "clear" // $NON-NLS-1$
    };

    private JLabeledChoice jmsCommunicationStyle = new JLabeledChoice(
            JMeterUtils.getResString("jms_communication_style"), // $NON-NLS-1$
            JMS_COMMUNICATION_STYLE_LABELS);

    private JMSPropertiesPanel jmsPropertiesPanel;

    private ArgumentsPanel jndiPropertiesPanel;

    private JCheckBox useNonPersistentDelivery;

    private JCheckBox useReqMsgIdAsCorrelId;

    private JCheckBox useResMsgIdAsCorrelId;

    public JMSSamplerGui() {
        init();
    }

    /**
     * Clears all fields.
     */
    @Override
    public void clearGui() {// renamed from clear
        super.clearGui();
        queueConnectionFactory.setText(""); // $NON-NLS-1$
        sendQueue.setText(""); // $NON-NLS-1$
        receiveQueue.setText(""); // $NON-NLS-1$
        jmsCommunicationStyle.setSelectedIndex(0);
        timeout.setText(""); // $NON-NLS-1$
        expiration.setText(""); // $NON-NLS-1$
        priority.setText(""); // $NON-NLS-1$
        jmsSelector.setText(""); // $NON-NLS-1$
        numberOfSamplesToAggregate.setText(""); // $NON-NLS-1$
        messageContent.setInitialText(""); // $NON-NLS-1$
        initialContextFactory.setText(""); // $NON-NLS-1$
        providerUrl.setText(""); // $NON-NLS-1$
        jmsPropertiesPanel.clearGui();
        jndiPropertiesPanel.clear();
    }

    @Override
    public TestElement createTestElement() {
        JMSSampler sampler = new JMSSampler();
        super.configureTestElement(sampler);
        transfer(sampler);
        return sampler;
    }

    private void transfer(JMSSampler element) {
        element.setQueueConnectionFactory(queueConnectionFactory.getText());
        element.setSendQueue(sendQueue.getText());
        element.setReceiveQueue(receiveQueue.getText());

        element.setProperty(JMSSampler.JMS_COMMUNICATION_STYLE, jmsCommunicationStyle.getSelectedIndex());
        element.removeProperty(JMSSampler.IS_ONE_WAY);
        element.setNonPersistent(useNonPersistentDelivery.isSelected());
        element.setUseReqMsgIdAsCorrelId(useReqMsgIdAsCorrelId.isSelected());
        element.setUseResMsgIdAsCorrelId(useResMsgIdAsCorrelId.isSelected());
        element.setTimeout(timeout.getText());
        element.setExpiration(expiration.getText());
        element.setPriority(priority.getText());
        element.setJMSSelector(jmsSelector.getText());
        element.setNumberOfSamplesToAggregate(numberOfSamplesToAggregate.getText());
        element.setContent(messageContent.getText());

        element.setInitialContextFactory(initialContextFactory.getText());
        element.setContextProvider(providerUrl.getText());
        Arguments jndiArgs = (Arguments) jndiPropertiesPanel.createTestElement();
        element.setJNDIProperties(jndiArgs);

        JMSProperties args = (JMSProperties) jmsPropertiesPanel.createTestElement();
        element.setJMSProperties(args);

    }

    /**
     *
     * @param element
     *            the test element being created
     */
    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (!(element instanceof JMSSampler)) {
            return;
        }
        JMSSampler sampler = (JMSSampler) element;
        transfer(sampler);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (!(el instanceof JMSSampler)) {
            return;
        }
        JMSSampler sampler = (JMSSampler) el;
        queueConnectionFactory.setText(sampler.getQueueConnectionFactory());
        sendQueue.setText(sampler.getSendQueue());
        receiveQueue.setText(sampler.getReceiveQueue());
        JMeterProperty oneWay = el.getProperty(JMSSampler.IS_ONE_WAY); // NOSONAR
        if(oneWay instanceof NullProperty) {
            jmsCommunicationStyle.setSelectedIndex(el.getPropertyAsInt(JMSSampler.JMS_COMMUNICATION_STYLE));
        } else {
            jmsCommunicationStyle.setSelectedIndex(
                    ((BooleanProperty)oneWay).getBooleanValue() ? 
                            JMSSampler.COMMUNICATION_STYLE.ONE_WAY.getValue() 
                            : JMSSampler.COMMUNICATION_STYLE.REQUEST_REPLY.getValue());
        }

        useNonPersistentDelivery.setSelected(sampler.isNonPersistent());
        useReqMsgIdAsCorrelId.setSelected(sampler.isUseReqMsgIdAsCorrelId());
        useResMsgIdAsCorrelId.setSelected(sampler.isUseResMsgIdAsCorrelId());

        timeout.setText(sampler.getTimeout());
        expiration.setText(sampler.getExpiration());
        priority.setText(sampler.getPriority());
        jmsSelector.setText(sampler.getJMSSelector());
        numberOfSamplesToAggregate.setText(sampler.getNumberOfSamplesToAggregate());
        messageContent.setInitialText(sampler.getContent());
        initialContextFactory.setText(sampler.getInitialContextFactory());
        providerUrl.setText(sampler.getContextProvider());

        jmsPropertiesPanel.configure(sampler.getJMSProperties());
        jndiPropertiesPanel.configure(sampler.getJNDIProperties());
    }

    /**
     * Initializes the configuration screen.
     *
     */
    private void init() { // WARNING: called from ctor so must not be overridden
                          // (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel jmsQueueingPanel = new JPanel(new BorderLayout());
        jmsQueueingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_queueing"))); //$NON-NLS-1$

        JPanel qcfPanel = new JPanel(new BorderLayout(5, 0));
        qcfPanel.add(queueConnectionFactory, BorderLayout.CENTER);
        jmsQueueingPanel.add(qcfPanel, BorderLayout.NORTH);

        JPanel sendQueuePanel = new JPanel(new BorderLayout(5, 0));
        sendQueuePanel.add(sendQueue);
        jmsQueueingPanel.add(sendQueuePanel, BorderLayout.CENTER);

        JPanel receiveQueuePanel = new JPanel(new BorderLayout(5, 0));
        receiveQueuePanel.add(jmsSelector, BorderLayout.SOUTH);
        receiveQueuePanel.add(numberOfSamplesToAggregate, BorderLayout.CENTER);
        receiveQueuePanel.add(receiveQueue, BorderLayout.NORTH);
        jmsQueueingPanel.add(receiveQueuePanel, BorderLayout.SOUTH);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_message_title"))); //$NON-NLS-1$

        JPanel correlationPanel = new HorizontalPanel();
        correlationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_correlation_title"))); //$NON-NLS-1$

        useReqMsgIdAsCorrelId = new JCheckBox(JMeterUtils.getResString("jms_use_req_msgid_as_correlid"), false); //$NON-NLS-1$

        useResMsgIdAsCorrelId = new JCheckBox(JMeterUtils.getResString("jms_use_res_msgid_as_correlid"), false); //$NON-NLS-1$

        correlationPanel.add(useReqMsgIdAsCorrelId);
        correlationPanel.add(useResMsgIdAsCorrelId);

        JPanel messageNorthPanel = new JPanel(new BorderLayout());
        JPanel onewayPanel = new HorizontalPanel();
        onewayPanel.add(jmsCommunicationStyle);
        onewayPanel.add(correlationPanel);
        messageNorthPanel.add(onewayPanel, BorderLayout.NORTH);

        useNonPersistentDelivery = new JCheckBox(JMeterUtils.getResString("jms_use_non_persistent_delivery"), false); //$NON-NLS-1$

        JPanel timeoutPanel = new HorizontalPanel();
        timeoutPanel.add(timeout);
        timeoutPanel.add(expiration);
        timeoutPanel.add(priority);
        timeoutPanel.add(useNonPersistentDelivery);
        messageNorthPanel.add(timeoutPanel, BorderLayout.SOUTH);

        messagePanel.add(messageNorthPanel, BorderLayout.NORTH);

        JPanel messageContentPanel = new JPanel(new BorderLayout());
        messageContentPanel.add(new JLabel(JMeterUtils.getResString("jms_msg_content")), BorderLayout.NORTH);
        messageContentPanel.add(JTextScrollPane.getInstance(messageContent), BorderLayout.CENTER);
        messagePanel.add(messageContentPanel, BorderLayout.CENTER);

        jmsPropertiesPanel = new JMSPropertiesPanel(); // $NON-NLS-1$
        messagePanel.add(jmsPropertiesPanel, BorderLayout.SOUTH);

        Box mainPanel = Box.createVerticalBox();
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(jmsQueueingPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        JPanel jndiPanel = createJNDIPanel();
        mainPanel.add(jndiPanel, BorderLayout.SOUTH);

    }

    /**
     * Creates the panel for the JNDI configuration.
     *
     * @return the JNDI Panel
     */
    private JPanel createJNDIPanel() {
        JPanel jndiPanel = new JPanel(new BorderLayout());
        jndiPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_jndi_props"))); //$NON-NLS-1$

        JPanel contextPanel = new JPanel(new BorderLayout(10, 0));
        contextPanel.add(initialContextFactory);
        jndiPanel.add(contextPanel, BorderLayout.NORTH);

        JPanel providerPanel = new JPanel(new BorderLayout(10, 0));
        providerPanel.add(providerUrl);
        jndiPanel.add(providerPanel, BorderLayout.SOUTH);

        jndiPropertiesPanel = new ArgumentsPanel(JMeterUtils.getResString("jms_jndi_props")); //$NON-NLS-1$
        jndiPanel.add(jndiPropertiesPanel);
        return jndiPanel;
    }

    @Override
    public String getLabelResource() {
        return "jms_point_to_point"; //$NON-NLS-1$ 
    }

}
