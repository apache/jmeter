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
 */

package org.apache.jmeter.protocol.mqtt.control.gui;

import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mqtt.sampler.SubscriberSampler;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the MQTT Sibscriber Sampler GUI class. All swing components of the UI are included in this class.
 */
public class MQTTSubscriberGui extends AbstractSamplerGui implements ActionListener {

    private static final long serialVersionUID = 240L;
    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();

    private static final String[] QOS_TYPES_ITEMS = {Constants.MQTT_AT_MOST_ONCE, Constants.MQTT_AT_LEAST_ONCE, Constants.MQTT_EXACTLY_ONCE};
    private static final String[] CLIENT_TYPES_ITEMS = {Constants.MQTT_BLOCKING_CLIENT, Constants.MQTT_ASYNC_CLIENT};

    private final JLabeledTextField brokerUrlField = new JLabeledTextField(Constants.MQTT_PROVIDER_URL);
    private final JLabeledTextField clientId = new JLabeledTextField(Constants.MQTT_CLIENT_ID);
    private final JButton generateClientID = new JButton(Constants.MQTT_CLIENT_ID_GENERATOR);

    private final JLabeledTextField mqttDestination = new JLabeledTextField(Constants.MQTT_TOPIC);

    private final JCheckBox cleanSession = new JCheckBox(Constants.MQTT_CLEAN_SESSION, false);

    private final JLabeledTextField mqttKeepAlive = new JLabeledTextField(Constants.MQTT_KEEP_ALIVE);

    private final JLabeledTextField mqttUser = new JLabeledTextField(Constants.MQTT_USERNAME);
    private final JLabeledTextField mqttPwd = new JLabeledPasswordField(Constants.MQTT_PASSWORD);
    private final JButton resetUserNameAndPassword = new JButton(Constants.MQTT_RESET_USERNAME_PASSWORD);

    private final JLabeledRadioI18N typeQoSValue = new JLabeledRadioI18N(Constants.MQTT_QOS, QOS_TYPES_ITEMS, Constants.MQTT_AT_MOST_ONCE);
    private final JLabeledRadioI18N typeClientValue = new JLabeledRadioI18N(Constants.MQTT_CLIENT_TYPES, CLIENT_TYPES_ITEMS,
                                                                            Constants.MQTT_BLOCKING_CLIENT);

    public MQTTSubscriberGui() {
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStaticLabel() {
        return Constants.MQTT_SUBSCRIBER_TITLE;
    }

    /**
     * Creates a test element for MQTT subscriber
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        SubscriberSampler sampler = new SubscriberSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement s) {
        SubscriberSampler sampler = (SubscriberSampler) s;
        this.configureTestElement(sampler);
        sampler.setBrokerUrl(brokerUrlField.getText());
        sampler.setClientId(clientId.getText());
        sampler.setTopicName(mqttDestination.getText());
        sampler.setCleanSession(cleanSession.isSelected());
        sampler.setKeepAlive(mqttKeepAlive.getText());
        sampler.setUsername(mqttUser.getText());
        sampler.setPassword(mqttPwd.getText());
        sampler.setQOS(typeQoSValue.getText());
        sampler.setClientType(typeClientValue.getText());

    }

    /**
     * Initializes all the UI elements
     */
    private void init() {
        brokerUrlField.setText(Constants.MQTT_URL_DEFAULT);
                               setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        JPanel mainPanel = new VerticalPanel();
        add(mainPanel, BorderLayout.CENTER);
        JPanel DPanel = new JPanel();
        DPanel.setLayout(new BoxLayout(DPanel, BoxLayout.X_AXIS));
        DPanel.add(brokerUrlField);
        DPanel.add(clientId);
        DPanel.add(generateClientID);
        JPanel ControlPanel = new VerticalPanel();
        ControlPanel.add(DPanel);
        ControlPanel.add(createDestinationPane());
        ControlPanel.add(cleanSession);
        ControlPanel.add(createKeepAlivePane());
        ControlPanel.add(createAuthPane());
        ControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray),
                "Connection Info"));
        mainPanel.add(ControlPanel);
        JPanel TPanel = new VerticalPanel();
        TPanel.setLayout(new BoxLayout(TPanel, BoxLayout.X_AXIS));
        typeQoSValue.setLayout(new BoxLayout(typeQoSValue, BoxLayout.X_AXIS));
        typeClientValue.setLayout(new BoxLayout(typeClientValue, BoxLayout.X_AXIS));
        TPanel.add(typeQoSValue);
        TPanel.add(typeClientValue);
        TPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Option"));
        mainPanel.add(TPanel);

        generateClientID.setActionCommand(Constants.GENERATE_CLIENT_ID_COMMAND);
        resetUserNameAndPassword.setActionCommand(Constants.RESET_CREDENTIALS);
        generateClientID.addActionListener(this);
        resetUserNameAndPassword.addActionListener(this);
        brokerUrlField.setText(Constants.MQTT_URL_DEFAULT);

    }

    /**
     * Creates the panel for user authentication. Username and password are included.
     * @return JPanel Panel with checkbox to choose  user and password
     */
    private Component createAuthPane() {
        mqttUser.setText(Constants.MQTT_USER_USERNAME);
        mqttPwd.setText(Constants.MQTT_USER_PASSWORD);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(mqttUser);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(mqttPwd);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(resetUserNameAndPassword);
        return panel;
    }

    /**
     * {@inheritDoc}. </br>.
     * Loads fields from an existing sampler file.
     *
     * @param el The test element
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SubscriberSampler sampler = (SubscriberSampler) el;
        brokerUrlField.setText(sampler.getBrokerUrl());
        clientId.setText(sampler.getClientId());
        mqttDestination.setText(sampler.getTopicName());
        cleanSession.setSelected(sampler.isCleanSession());
        mqttKeepAlive.setText(Integer.toString(sampler.getKeepAlive()));
        mqttUser.setText(sampler.getUsername());
        mqttPwd.setText(sampler.getPassword());
        typeQoSValue.setText(sampler.getQOS());
        typeClientValue.setText(sampler.getClientType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
    }

    /**
     * Creates the topic destination panel.
     *
     * @return The topic destination panel.
     */
    private JPanel createDestinationPane() {
        JPanel panel = new VerticalPanel(); //new BorderLayout(3, 0)
        this.mqttDestination.setLayout((new BoxLayout(mqttDestination, BoxLayout.X_AXIS)));
        panel.add(mqttDestination);
        JPanel TPanel = new JPanel();
        TPanel.setLayout(new BoxLayout(TPanel, BoxLayout.X_AXIS));
        TPanel.add(Box.createHorizontalStrut(100));
        panel.add(TPanel);        return panel;
    }

    /**
     * Creates the mqtt client keep alive panel.
     *
     * @return The mqtt client keep alive panel.
     */
    private JPanel createKeepAlivePane() {
        JPanel panel = new VerticalPanel(); //new BorderLayout(3, 0)
        this.mqttKeepAlive.setLayout((new BoxLayout(mqttKeepAlive, BoxLayout.X_AXIS)));
        panel.add(mqttKeepAlive);
        JPanel TPanel = new JPanel();
        TPanel.setLayout(new BoxLayout(TPanel, BoxLayout.X_AXIS));
        TPanel.add(Box.createHorizontalStrut(100));
        panel.add(TPanel);
        mqttKeepAlive.setText(Constants.MQTT_KEEP_ALIVE_DEFAULT);
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.GENERATE_CLIENT_ID_COMMAND.equals(e.getActionCommand())) {
            clientId.setText(Utils.generateClientID());
        } else if(Constants.RESET_CREDENTIALS.equals(e.getActionCommand())){
            mqttUser.setText(Constants.MQTT_USER_USERNAME);
            mqttPwd.setText(Constants.MQTT_USER_PASSWORD);
        }
    }
}
