/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.jms.control.gui;

import java.awt.BorderLayout;

import javax.naming.Context;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.protocol.jms.sampler.SubscriberSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;

import net.miginfocom.swing.MigLayout;

/**
 * This is the GUI for JMS Subscriber <br>
 *
 */
@TestElementMetadata(labelResource = "jms_subscriber_title")
public class JMSSubscriberGui extends AbstractSamplerGui implements ChangeListener {

    private static final long serialVersionUID = 240L;

    private final JCheckBox useProperties =
        new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false); // $NON-NLS-1$

    private final JTextField jndiICF = new JTextField();

    private final JTextField urlField = new JTextField();

    private final JTextField jndiConnFac = new JTextField();

    private final JTextField jmsDestination = new JTextField();

    private final JTextField jmsDurableSubscriptionId = new JTextField();

    private final JTextField jmsClientId = new JTextField();

    private final JTextField jmsSelector = new JTextField();

    private final JCheckBox useAuth =
            new JCheckBox(JMeterUtils.getResString("jms_use_auth"), false); //$NON-NLS-1$

    private final JLabeledTextField jmsUser =
        new JLabeledTextField(JMeterUtils.getResString("jms_user")); // $NON-NLS-1$

    private final JLabeledTextField jmsPwd =
        new JLabeledPasswordField(JMeterUtils.getResString("jms_pwd")); // $NON-NLS-1$

    private final JTextField samplesToAggregate = new JTextField();

    private final JCheckBox storeResponse =
        new JCheckBox(JMeterUtils.getResString("jms_store_response"), true); // $NON-NLS-1$

    private final JTextField timeout = new JTextField();

    private final JTextField jmsErrorPauseBetween = new JTextField();

    private final JTextField jmsErrorReconnectOnCodes = new JTextField();

    private final JTextField separator = new JTextField();

    //++ Do not change these strings; they are used in JMX files to record the button settings
    public static final String RECEIVE_RSC = "jms_subscriber_receive"; // $NON-NLS-1$

    public static final String ON_MESSAGE_RSC = "jms_subscriber_on_message"; // $NON-NLS-1$
    //--

    // Button group resources
    private static final String[] CLIENT_ITEMS = { RECEIVE_RSC, ON_MESSAGE_RSC };

    private final JLabeledRadioI18N clientChoice =
        new JLabeledRadioI18N("jms_client_type", CLIENT_ITEMS, RECEIVE_RSC); // $NON-NLS-1$

    private final JCheckBox stopBetweenSamples =
        new JCheckBox(JMeterUtils.getResString("jms_stop_between_samples"), true); // $NON-NLS-1$

    // These are the names of properties used to define the labels
    private static final String DEST_SETUP_STATIC = "jms_dest_setup_static"; // $NON-NLS-1$

    private static final String DEST_SETUP_DYNAMIC = "jms_dest_setup_dynamic"; // $NON-NLS-1$
    // Button group resources
    private static final String[] DEST_SETUP_ITEMS = { DEST_SETUP_STATIC, DEST_SETUP_DYNAMIC };

    private final JLabeledRadioI18N destSetup =
        new JLabeledRadioI18N("jms_dest_setup", DEST_SETUP_ITEMS, DEST_SETUP_STATIC); // $NON-NLS-1$

    public JMSSubscriberGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "jms_subscriber_title"; // $NON-NLS-1$
    }

    /**
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
        super.configureTestElement(sampler);
        sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
        sampler.setJNDIIntialContextFactory(jndiICF.getText());
        sampler.setProviderUrl(urlField.getText());
        sampler.setConnectionFactory(jndiConnFac.getText());
        sampler.setDestination(jmsDestination.getText());
        sampler.setDurableSubscriptionId(jmsDurableSubscriptionId.getText());
        sampler.setClientID(jmsClientId.getText());
        sampler.setJmsSelector(jmsSelector.getText());
        sampler.setUsername(jmsUser.getText());
        sampler.setPassword(jmsPwd.getText());
        sampler.setUseAuth(useAuth.isSelected());
        sampler.setIterations(samplesToAggregate.getText());
        sampler.setReadResponse(String.valueOf(storeResponse.isSelected()));
        sampler.setClientChoice(clientChoice.getText());
        sampler.setStopBetweenSamples(stopBetweenSamples.isSelected());
        sampler.setTimeout(timeout.getText());
        sampler.setReconnectionErrorCodes(jmsErrorReconnectOnCodes.getText());
        sampler.setPauseBetweenErrors(jmsErrorPauseBetween.getText());
        sampler.setDestinationStatic(destSetup.getText().equals(DEST_SETUP_STATIC));
        sampler.setSeparator(separator.getText());
    }

    /**
     * init() adds jndiICF to the mainPanel. The class reuses logic from
     * SOAPSampler, since it is common.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new MigLayout("fillx, wrap 3, insets 0", "[][fill,grow]"));
        add(mainPanel, BorderLayout.CENTER);

        jndiICF.setToolTipText(Context.INITIAL_CONTEXT_FACTORY);
        urlField.setToolTipText(Context.PROVIDER_URL);
        jmsUser.setToolTipText(Context.SECURITY_PRINCIPAL);
        jmsPwd.setToolTipText(Context.SECURITY_CREDENTIALS);

        mainPanel.add(useProperties, "span");

        mainPanel.add(JMeterUtils.labelFor(jndiICF, "jms_initial_context_factory"));
        mainPanel.add(jndiICF, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(urlField, "jms_provider_url"));
        mainPanel.add(urlField, "span, growx");

        mainPanel.add(useAuth);
        mainPanel.add(jmsUser);
        mainPanel.add(jmsPwd);

        mainPanel.add(JMeterUtils.labelFor(jndiConnFac, "jms_connection_factory"));
        mainPanel.add(jndiConnFac, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(jmsDestination, "jms_topic"));
        mainPanel.add(jmsDestination);
        mainPanel.add(destSetup);

        mainPanel.add(JMeterUtils.labelFor(jmsDurableSubscriptionId, "jms_durable_subscription_id"));
        mainPanel.add(jmsDurableSubscriptionId, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(jmsClientId, "jms_client_id"));
        mainPanel.add(jmsClientId, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(jmsSelector, "jms_selector"));
        mainPanel.add(jmsSelector);

        mainPanel.add(storeResponse, "span");

        mainPanel.add(JMeterUtils.labelFor(timeout, "jms_timeout"));
        mainPanel.add(timeout, "span");

        mainPanel.add(clientChoice, "span 2");
        mainPanel.add(stopBetweenSamples);

        mainPanel.add(JMeterUtils.labelFor(jmsErrorReconnectOnCodes, "jms_error_reconnect_on_codes"));
        mainPanel.add(jmsErrorReconnectOnCodes, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(jmsErrorPauseBetween, "jms_error_pause_between"));
        mainPanel.add(jmsErrorPauseBetween, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(samplesToAggregate, "jms_itertions"));
        mainPanel.add(samplesToAggregate, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(separator, "jms_separator"));
        mainPanel.add(separator, "span, growx");

        useProperties.addChangeListener(this);
        useAuth.addChangeListener(this);
    }

    /**
     * the implementation loads the URL and the soap action for the request.
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SubscriberSampler sampler = (SubscriberSampler) el;
        useProperties.setSelected(sampler.getUseJNDIPropertiesAsBoolean());
        jndiICF.setText(sampler.getJNDIInitialContextFactory());
        urlField.setText(sampler.getProviderUrl());
        jndiConnFac.setText(sampler.getConnectionFactory());
        jmsDestination.setText(sampler.getDestination());
        jmsDurableSubscriptionId.setText(sampler.getDurableSubscriptionId());
        jmsClientId.setText(sampler.getClientId());
        jmsSelector.setText(sampler.getJmsSelector());
        jmsUser.setText(sampler.getUsername());
        jmsPwd.setText(sampler.getPassword());
        samplesToAggregate.setText(sampler.getIterations());
        useAuth.setSelected(sampler.isUseAuth());
        jmsUser.setEnabled(useAuth.isSelected());
        jmsPwd.setEnabled(useAuth.isSelected());
        storeResponse.setSelected(sampler.getReadResponseAsBoolean());
        clientChoice.setText(sampler.getClientChoice());
        stopBetweenSamples.setSelected(sampler.isStopBetweenSamples());
        timeout.setText(sampler.getTimeout());
        separator.setText(sampler.getSeparator());
        destSetup.setText(sampler.isDestinationStatic() ? DEST_SETUP_STATIC : DEST_SETUP_DYNAMIC);
        jmsErrorReconnectOnCodes.setText(sampler.getReconnectionErrorCodes());
        jmsErrorPauseBetween.setText(sampler.getPauseBetweenErrors());
    }

    @Override
    public void clearGui(){
        super.clearGui();
        useProperties.setSelected(false); // $NON-NLS-1$
        jndiICF.setText(""); // $NON-NLS-1$
        urlField.setText(""); // $NON-NLS-1$
        jndiConnFac.setText(""); // $NON-NLS-1$
        jmsDestination.setText(""); // $NON-NLS-1$
        jmsDurableSubscriptionId.setText(""); // $NON-NLS-1$
        jmsClientId.setText(""); // $NON-NLS-1$
        jmsSelector.setText(""); // $NON-NLS-1$
        jmsUser.setText(""); // $NON-NLS-1$
        jmsPwd.setText(""); // $NON-NLS-1$
        samplesToAggregate.setText("1"); // $NON-NLS-1$
        timeout.setText(""); // $NON-NLS-1$
        separator.setText(""); // $NON-NLS-1$
        useAuth.setSelected(false);
        jmsUser.setEnabled(false);
        jmsPwd.setEnabled(false);
        storeResponse.setSelected(true);
        clientChoice.setText(RECEIVE_RSC);
        stopBetweenSamples.setSelected(false);
        destSetup.setText(DEST_SETUP_STATIC);
        jmsErrorReconnectOnCodes.setText("");
        jmsErrorPauseBetween.setText("");
    }

    /**
     * When the state of a widget changes, it will notify the gui. the method
     * then enables or disables certain parameters.
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == useProperties) {
            final boolean isUseProperties = useProperties.isSelected();
            jndiICF.setEnabled(!isUseProperties);
            urlField.setEnabled(!isUseProperties);
            useAuth.setEnabled(!isUseProperties);
        } else if (event.getSource() == useAuth) {
            jmsUser.setEnabled(useAuth.isSelected() && useAuth.isEnabled());
            jmsPwd.setEnabled(useAuth.isSelected()  && useAuth.isEnabled());
        }
    }
}
