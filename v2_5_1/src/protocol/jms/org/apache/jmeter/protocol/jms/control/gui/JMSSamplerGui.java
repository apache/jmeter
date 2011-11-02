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
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.jms.sampler.JMSSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Configuration screen for Java Messaging Point-to-Point requests. <br>
 * Created on: October 28, 2004
 *
 */
public class JMSSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 240L;

    private JLabeledTextField queueuConnectionFactory = new JLabeledTextField(
            JMeterUtils.getResString("jms_queue_connection_factory")); //$NON-NLS-1$

    private JLabeledTextField sendQueue = new JLabeledTextField(JMeterUtils.getResString("jms_send_queue")); //$NON-NLS-1$

    private JLabeledTextField receiveQueue = new JLabeledTextField(JMeterUtils.getResString("jms_receive_queue")); //$NON-NLS-1$

    private JLabeledTextField timeout = new JLabeledTextField(JMeterUtils.getResString("jms_timeout")); //$NON-NLS-1$

    private JLabeledTextArea soapXml = new JLabeledTextArea(JMeterUtils.getResString("jms_msg_content")); //$NON-NLS-1$

    private JLabeledTextField initialContextFactory = new JLabeledTextField(
            JMeterUtils.getResString("jms_initial_context_factory")); //$NON-NLS-1$

    private JLabeledTextField providerUrl = new JLabeledTextField(JMeterUtils.getResString("jms_provider_url")); //$NON-NLS-1$

    private String[] labels = new String[] { JMeterUtils.getResString("jms_request"), //$NON-NLS-1$
            JMeterUtils.getResString("jms_requestreply") }; //$NON-NLS-1$

    private JLabeledChoice oneWay = new JLabeledChoice(JMeterUtils.getResString("jms_communication_style"), labels); //$NON-NLS-1$

    private ArgumentsPanel jmsPropertiesPanel;

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
        queueuConnectionFactory.setText(""); // $NON-NLS-1$
        sendQueue.setText(""); // $NON-NLS-1$
        receiveQueue.setText(""); // $NON-NLS-1$
        ((JComboBox) oneWay.getComponentList().get(1)).setSelectedItem(JMeterUtils.getResString("jms_request")); //$NON-NLS-1$
        timeout.setText("");  // $NON-NLS-1$
        soapXml.setText(""); // $NON-NLS-1$
        initialContextFactory.setText(""); // $NON-NLS-1$
        providerUrl.setText(""); // $NON-NLS-1$
        jmsPropertiesPanel.clear();
        jndiPropertiesPanel.clear();
    }

    public TestElement createTestElement() {
        JMSSampler sampler = new JMSSampler();
        this.configureTestElement(sampler);
        transfer(sampler);
        return sampler;
    }

    private void transfer(JMSSampler element) {
        element.setQueueConnectionFactory(queueuConnectionFactory.getText());
        element.setSendQueue(sendQueue.getText());
        element.setReceiveQueue(receiveQueue.getText());

        boolean isOneway = oneWay.getText().equals(JMeterUtils.getResString("jms_request")); //$NON-NLS-1$
        element.setIsOneway(isOneway);

        element.setNonPersistent(useNonPersistentDelivery.isSelected());
        element.setUseReqMsgIdAsCorrelId(useReqMsgIdAsCorrelId.isSelected());
        element.setUseResMsgIdAsCorrelId(useResMsgIdAsCorrelId.isSelected());
        element.setTimeout(timeout.getText());
        element.setContent(soapXml.getText());

        element.setInitialContextFactory(initialContextFactory.getText());
        element.setContextProvider(providerUrl.getText());
        Arguments jndiArgs = (Arguments) jndiPropertiesPanel.createTestElement();
        element.setJNDIProperties(jndiArgs);

        Arguments args = (Arguments) jmsPropertiesPanel.createTestElement();
        element.setJMSProperties(args);

    }

    /**
     *
     * @param element
     */
    public void modifyTestElement(TestElement element) {
        this.configureTestElement(element);
        if (!(element instanceof JMSSampler)) return;
        JMSSampler sampler = (JMSSampler) element;
        transfer(sampler);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (!(el instanceof JMSSampler)) return;
        JMSSampler sampler = (JMSSampler) el;
        queueuConnectionFactory.setText(sampler.getQueueConnectionFactory());
        sendQueue.setText(sampler.getSendQueue());
        receiveQueue.setText(sampler.getReceiveQueue());

        JComboBox box = (JComboBox) oneWay.getComponentList().get(1);
        String selected = null;
        if (sampler.isOneway()) {
            selected = JMeterUtils.getResString("jms_request"); //$NON-NLS-1$
        } else {
            selected = JMeterUtils.getResString("jms_requestreply"); //$NON-NLS-1$
        }
        box.setSelectedItem(selected);

        useNonPersistentDelivery.setSelected(sampler.isNonPersistent());
        useReqMsgIdAsCorrelId.setSelected(sampler.isUseReqMsgIdAsCorrelId());
        useResMsgIdAsCorrelId.setSelected(sampler.isUseResMsgIdAsCorrelId());

        timeout.setText(sampler.getTimeout());
        soapXml.setText(sampler.getContent());
        initialContextFactory.setText(sampler.getInitialContextFactory());
        providerUrl.setText(sampler.getContextProvider());

        jmsPropertiesPanel.configure(sampler.getJMSProperties());
        // (TestElement)
        // el.getProperty(JMSSampler.JMS_PROPERTIES).getObjectValue());

        jndiPropertiesPanel.configure(sampler.getJNDIProperties());
        // (TestElement)
        // el.getProperty(JMSSampler.JNDI_PROPERTIES).getObjectValue());
    }

    /**
     * Initializes the configuration screen.
     *
     */
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel jmsQueueingPanel = new JPanel(new BorderLayout());
        jmsQueueingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_queueing"))); //$NON-NLS-1$

        JPanel qcfPanel = new JPanel(new BorderLayout(5, 0));
        qcfPanel.add(queueuConnectionFactory, BorderLayout.CENTER);
        jmsQueueingPanel.add(qcfPanel, BorderLayout.NORTH);

        JPanel sendQueuePanel = new JPanel(new BorderLayout(5, 0));
        sendQueuePanel.add(sendQueue);
        jmsQueueingPanel.add(sendQueuePanel, BorderLayout.CENTER);

        JPanel receiveQueuePanel = new JPanel(new BorderLayout(5, 0));
        receiveQueuePanel.add(receiveQueue);
        jmsQueueingPanel.add(receiveQueuePanel, BorderLayout.SOUTH);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_message_title"))); //$NON-NLS-1$

        JPanel correlationPanel = new HorizontalPanel();
        correlationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_correlation_title"))); //$NON-NLS-1$

        useReqMsgIdAsCorrelId = new JCheckBox(JMeterUtils.getResString("jms_use_req_msgid_as_correlid"),false); //$NON-NLS-1$

        useResMsgIdAsCorrelId = new JCheckBox(JMeterUtils.getResString("jms_use_res_msgid_as_correlid"),false); //$NON-NLS-1$

        correlationPanel.add(useReqMsgIdAsCorrelId);
        correlationPanel.add(useResMsgIdAsCorrelId);

        JPanel messageNorthPanel = new JPanel(new BorderLayout());
        JPanel onewayPanel = new HorizontalPanel();
        onewayPanel.add(oneWay);
        onewayPanel.add(correlationPanel);
        messageNorthPanel.add(onewayPanel, BorderLayout.NORTH);

        useNonPersistentDelivery = new JCheckBox(JMeterUtils.getResString("jms_use_non_persistent_delivery"),false); //$NON-NLS-1$

        JPanel timeoutPanel = new HorizontalPanel();
        timeoutPanel.add(timeout);
        timeoutPanel.add(useNonPersistentDelivery);
        messageNorthPanel.add(timeoutPanel, BorderLayout.SOUTH);

        messagePanel.add(messageNorthPanel, BorderLayout.NORTH);

        JPanel soapXmlPanel = new JPanel(new BorderLayout());
        soapXmlPanel.add(soapXml);
        messagePanel.add(soapXmlPanel, BorderLayout.CENTER);

        jmsPropertiesPanel = new ArgumentsPanel(JMeterUtils.getResString("jms_props")); //$NON-NLS-1$
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

    public String getLabelResource() {
        return "jms_point_to_point"; //$NON-NLS-1$ // TODO - probably wrong
    }

}