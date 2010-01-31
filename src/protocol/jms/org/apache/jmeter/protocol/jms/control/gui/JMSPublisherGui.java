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
import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.layout.VerticalLayout;

import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;

/**
 * This is the GUI for JMS Publisher <br>
 * Created on: October 13, 2003
 *
 */
public class JMSPublisherGui extends AbstractSamplerGui implements java.awt.event.ActionListener, ChangeListener {

    private static final long serialVersionUID = 240L;

    private static final String ALL_FILES = "*.*"; //$NON-NLS-1$

    //++ These names are used in the JMX files, and must not be changed
    public static final String USE_FILE_RSC   = "jms_use_file"; //$NON-NLS-1$
    public static final String USE_RANDOM_RSC = "jms_use_random_file"; //$NON-NLS-1$
    private static final String USE_TEXT_RSC   = "jms_use_text"; //$NON-NLS-1$

    private static final String TEXT_MSG_RSC = "jms_text_message"; //$NON-NLS-1$
    private static final String OBJECT_MSG_RSC = "jms_object_message"; //$NON-NLS-1$
    //--
    
    // Button group resources
    private static final String[] CONFIG_ITEMS = { USE_FILE_RSC, USE_RANDOM_RSC, USE_TEXT_RSC };

    private static final String[] MSGTYPES_ITEMS = { TEXT_MSG_RSC, OBJECT_MSG_RSC };

    private final JCheckBox useProperties = new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false); //$NON-NLS-1$

    private final JLabeledRadioI18N configChoice = new JLabeledRadioI18N("jms_config", CONFIG_ITEMS, USE_TEXT_RSC); //$NON-NLS-1$

    private final JLabeledTextField jndiICF = new JLabeledTextField(JMeterUtils.getResString("jms_initial_context_factory")); //$NON-NLS-1$

    private final JLabeledTextField urlField = new JLabeledTextField(JMeterUtils.getResString("jms_provider_url")); //$NON-NLS-1$

    private final JLabeledTextField jndiConnFac = new JLabeledTextField(JMeterUtils.getResString("jms_connection_factory")); //$NON-NLS-1$

    private final JLabeledTextField jmsTopic = new JLabeledTextField(JMeterUtils.getResString("jms_topic")); //$NON-NLS-1$

    private final JCheckBox useAuth = new JCheckBox(JMeterUtils.getResString("jms_use_auth"), false); //$NON-NLS-1$

    private final JLabeledTextField jmsUser = new JLabeledTextField(JMeterUtils.getResString("jms_user")); //$NON-NLS-1$

    private final JLabeledTextField jmsPwd = new JLabeledTextField(JMeterUtils.getResString("jms_pwd")); //$NON-NLS-1$

    private final JLabeledTextField iterations = new JLabeledTextField(JMeterUtils.getResString("jms_itertions")); //$NON-NLS-1$

    private final FilePanel messageFile = new FilePanel(JMeterUtils.getResString("jms_file"), ALL_FILES); //$NON-NLS-1$

    private final FilePanel randomFile = new FilePanel(JMeterUtils.getResString("jms_random_file"), ALL_FILES); //$NON-NLS-1$

    private final JLabeledTextArea textMessage = new JLabeledTextArea(TEXT_MSG_RSC);

    private final JLabeledRadioI18N msgChoice = new JLabeledRadioI18N("jms_message_type", MSGTYPES_ITEMS, TEXT_MSG_RSC); //$NON-NLS-1$

    private final JPanel lookup = new JPanel();

    private final JPanel messagePanel = new JPanel();

    public JMSPublisherGui() {
        init();
    }

    /**
     * the name of the property for the JMSPublisherGui is jms_publisher.
     */
    public String getLabelResource() {
        return "jms_publisher"; //$NON-NLS-1$
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
        sampler.setUseAuth(useAuth.isSelected());
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
        sampler.setUseAuth(useAuth.isSelected());
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
        
        lookup.setLayout(new VerticalLayout(6, VerticalLayout.LEFT));
        mainPanel.add(lookup);
        lookup.add(useProperties);
        useProperties.addChangeListener(this);
        lookup.add(jndiICF);
        lookup.add(urlField);
        lookup.add(jndiConnFac);

        configChoice.addChangeListener(this);
        msgChoice.addChangeListener(this);

        JPanel commonParams = new JPanel();
        commonParams.setLayout(new VerticalLayout(6, VerticalLayout.LEFT));
        mainPanel.add(commonParams);
        commonParams.add(jmsTopic);
        commonParams.add(useAuth);
        commonParams.add(jmsUser);
        commonParams.add(jmsPwd);
        commonParams.add(iterations);

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

    @Override
    public void clearGui(){
        super.clearGui();
        useProperties.setSelected(false);
        jndiICF.setText(""); // $NON-NLS-1$
        urlField.setText(""); // $NON-NLS-1$
        jndiConnFac.setText(""); // $NON-NLS-1$
        jmsTopic.setText(""); // $NON-NLS-1$
        jmsUser.setText(""); // $NON-NLS-1$
        jmsPwd.setText(""); // $NON-NLS-1$
        textMessage.setText(""); // $NON-NLS-1$
        messageFile.setFilename(""); // $NON-NLS-1$
        randomFile.setFilename(""); // $NON-NLS-1$
        msgChoice.setText(""); // $NON-NLS-1$
        configChoice.setText(USE_TEXT_RSC);
        updateConfig(USE_TEXT_RSC);
        iterations.setText(""); // $NON-NLS-1$
        useAuth.setSelected(false);
    }

    /**
     * the implementation loads the URL and the soap action for the request.
     */
    @Override
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
        useAuth.setSelected(sampler.isUseAuth());
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
    private void updateConfig(String command) {
        if (command.equals(USE_TEXT_RSC)) {
            textMessage.setEnabled(true);
            messageFile.enableFile(false);
            randomFile.enableFile(false);
        } else if (command.equals(USE_RANDOM_RSC)) {
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
    private void updateMessageType(String msgType) {
        if (msgType.equals(OBJECT_MSG_RSC)) {
            if (configChoice.getText().equals(USE_TEXT_RSC)) {
                JOptionPane.showConfirmDialog(this,
                        JMeterUtils.getResString("jms_error_msg"),  //$NON-NLS-1$
                        "Warning",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
