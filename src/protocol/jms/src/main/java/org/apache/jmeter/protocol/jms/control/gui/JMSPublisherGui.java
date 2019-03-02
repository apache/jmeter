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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jms.sampler.JMSProperties;
import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * This is the GUI for JMS Publisher
 */
public class JMSPublisherGui extends AbstractSamplerGui implements ChangeListener {

    private static final long serialVersionUID = 241L;

    //++ These names are used in the JMX files, and must not be changed
    /** Take source from the named file */
    public static final String USE_FILE_RSC   = "jms_use_file"; //$NON-NLS-1$
    /** Take source from a random file */
    public static final String USE_RANDOM_RSC = "jms_use_random_file"; //$NON-NLS-1$
    /** Take source from the text area */
    public static final String USE_TEXT_RSC   = "jms_use_text"; //$NON-NLS-1$

    /** Create a TextMessage */
    public static final String TEXT_MSG_RSC = "jms_text_message"; //$NON-NLS-1$
    /** Create a MapMessage */
    public static final String MAP_MSG_RSC = "jms_map_message"; //$NON-NLS-1$
    /** Create an ObjectMessage */
    public static final String OBJECT_MSG_RSC = "jms_object_message"; //$NON-NLS-1$
    /** Create a BytesMessage */
    public static final String BYTES_MSG_RSC = "jms_bytes_message"; //$NON-NLS-1$
    //-- End of names used in JMX files

    // Button group resources when Bytes Message is selected
    private static final String[] CONFIG_ITEMS_BYTES_MSG = { USE_FILE_RSC, USE_RANDOM_RSC};

    // Button group resources
    private static final String[] CONFIG_ITEMS = { USE_FILE_RSC, USE_RANDOM_RSC, USE_TEXT_RSC };

    private static final String[] MSGTYPES_ITEMS = { TEXT_MSG_RSC, MAP_MSG_RSC, OBJECT_MSG_RSC, BYTES_MSG_RSC };

    private final JCheckBox useProperties = new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false); //$NON-NLS-1$

    private final JLabeledRadioI18N configChoice = new JLabeledRadioI18N("jms_config", CONFIG_ITEMS, USE_TEXT_RSC); //$NON-NLS-1$

    private final JLabeledTextField jndiICF = new JLabeledTextField(JMeterUtils.getResString("jms_initial_context_factory")); //$NON-NLS-1$

    private final JLabeledTextField urlField = new JLabeledTextField(JMeterUtils.getResString("jms_provider_url")); //$NON-NLS-1$

    private final JLabeledTextField jndiConnFac = new JLabeledTextField(JMeterUtils.getResString("jms_connection_factory")); //$NON-NLS-1$

    private final JLabeledTextField jmsDestination = new JLabeledTextField(JMeterUtils.getResString("jms_topic")); //$NON-NLS-1$

    private final JLabeledTextField expiration = new JLabeledTextField(JMeterUtils.getResString("jms_expiration"),10); //$NON-NLS-1$

    private final JLabeledTextField jmsErrorReconnectOnCodes =
            new JLabeledTextField(JMeterUtils.getResString("jms_error_reconnect_on_codes")); // $NON-NLS-1$

    private final JLabeledTextField priority = new JLabeledTextField(JMeterUtils.getResString("jms_priority"),1); //$NON-NLS-1$

    private final JCheckBox useAuth = new JCheckBox(JMeterUtils.getResString("jms_use_auth"), false); //$NON-NLS-1$

    private final JLabeledTextField jmsUser = new JLabeledTextField(JMeterUtils.getResString("jms_user")); //$NON-NLS-1$

    private final JLabeledTextField jmsPwd = new JLabeledPasswordField(JMeterUtils.getResString("jms_pwd")); //$NON-NLS-1$

    private final JLabeledTextField iterations = new JLabeledTextField(JMeterUtils.getResString("jms_itertions")); //$NON-NLS-1$

    private final FilePanel messageFile = new FilePanel(JMeterUtils.getResString("jms_file")); //$NON-NLS-1$

    private final FilePanel randomFile = new FilePanel(JMeterUtils.getResString("jms_random_file"), true); //$NON-NLS-1$

    private final JSyntaxTextArea textMessage = JSyntaxTextArea.getInstance(10, 50); // $NON-NLS-1$

    private final JLabeledRadioI18N msgChoice = new JLabeledRadioI18N("jms_message_type", MSGTYPES_ITEMS, TEXT_MSG_RSC); //$NON-NLS-1$

    private JLabeledChoice fileEncoding;

    private final JCheckBox useNonPersistentDelivery = new JCheckBox(JMeterUtils.getResString("jms_use_non_persistent_delivery"),false); //$NON-NLS-1$

    // These are the names of properties used to define the labels
    private static final String DEST_SETUP_STATIC = "jms_dest_setup_static"; // $NON-NLS-1$

    private static final String DEST_SETUP_DYNAMIC = "jms_dest_setup_dynamic"; // $NON-NLS-1$
    // Button group resources
    private static final String[] DEST_SETUP_ITEMS = { DEST_SETUP_STATIC, DEST_SETUP_DYNAMIC };

    private final JLabeledRadioI18N destSetup =
        new JLabeledRadioI18N("jms_dest_setup", DEST_SETUP_ITEMS, DEST_SETUP_STATIC); // $NON-NLS-1$

    private JMSPropertiesPanel jmsPropertiesPanel;

    public JMSPublisherGui() {
        init();
    }

    /**
     * the name of the property for the JMSPublisherGui is jms_publisher.
     */
    @Override
    public String getLabelResource() {
        return "jms_publisher"; //$NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
      PublisherSampler sampler = new PublisherSampler();
      setupSamplerProperties(sampler);

      return sampler;
  }
    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement s) {
        PublisherSampler sampler = (PublisherSampler) s;
        setupSamplerProperties(sampler);
        sampler.setDestinationStatic(destSetup.getText().equals(DEST_SETUP_STATIC));
    }

    /**
     * Initialize the provided {@link PublisherSampler} with all the values as configured in the GUI.
     *
     * @param sampler {@link PublisherSampler} instance
     */
    private void setupSamplerProperties(final PublisherSampler sampler) {
      super.configureTestElement(sampler);
      sampler.setUseJNDIProperties(String.valueOf(useProperties.isSelected()));
      sampler.setJNDIIntialContextFactory(jndiICF.getText());
      sampler.setProviderUrl(urlField.getText());
      sampler.setConnectionFactory(jndiConnFac.getText());
      sampler.setDestination(jmsDestination.getText());
      sampler.setExpiration(expiration.getText());
      sampler.setReconnectionErrorCodes(jmsErrorReconnectOnCodes.getText());
      sampler.setPriority(priority.getText());
      sampler.setUsername(jmsUser.getText());
      sampler.setPassword(jmsPwd.getText());
      sampler.setTextMessage(textMessage.getText());
      sampler.setInputFile(messageFile.getFilename());
      sampler.setRandomPath(randomFile.getFilename());
      sampler.setConfigChoice(configChoice.getText());
      sampler.setFileEncoding(fileEncoding.getText());
      sampler.setMessageChoice(msgChoice.getText());
      sampler.setIterations(iterations.getText());
      sampler.setUseAuth(useAuth.isSelected());
      sampler.setUseNonPersistentDelivery(useNonPersistentDelivery.isSelected());

      JMSProperties args = (JMSProperties) jmsPropertiesPanel.createTestElement();
      sampler.setJMSProperties(args);
    }

    /**
     * init() adds jndiICF to the mainPanel. The class reuses logic from
     * SOAPSampler, since it is common.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new VerticalPanel();
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(useProperties);
        mainPanel.add(jndiICF);
        mainPanel.add(urlField);
        mainPanel.add(jndiConnFac);
        mainPanel.add(createDestinationPane());
        mainPanel.add(createAuthPane());
        mainPanel.add(createPriorityAndExpiration());
        mainPanel.add(jmsErrorReconnectOnCodes);
        mainPanel.add(iterations);

        jmsPropertiesPanel = new JMSPropertiesPanel(); //$NON-NLS-1$
        mainPanel.add(jmsPropertiesPanel);

        configChoice.setLayout(new BoxLayout(configChoice, BoxLayout.X_AXIS));
        mainPanel.add(configChoice);
        msgChoice.setLayout(new BoxLayout(msgChoice, BoxLayout.X_AXIS));
        mainPanel.add(msgChoice);

        String nonBreakingSpace = "\u00A0"; // CHECKSTYLE IGNORE AvoidEscapedUnicodeCharacters
        fileEncoding = new JLabeledChoice(JMeterUtils.getResString("content_encoding") + nonBreakingSpace + nonBreakingSpace, // $NON-NLS-1$
                PublisherSampler.getSupportedEncodings(), true, false);
        fileEncoding.setLayout(new BoxLayout(fileEncoding, BoxLayout.X_AXIS));
        fileEncoding.add(Box.createHorizontalGlue());
        mainPanel.add(fileEncoding);

        mainPanel.add(messageFile);
        mainPanel.add(randomFile);

        JPanel messageContentPanel = new JPanel(new BorderLayout());
        messageContentPanel.add(new JLabel(JMeterUtils.getResString("jms_text_area")), BorderLayout.NORTH);
        messageContentPanel.add(JTextScrollPane.getInstance(textMessage), BorderLayout.CENTER);

        mainPanel.add(messageContentPanel);
        useProperties.addChangeListener(this);
        useAuth.addChangeListener(this);
        configChoice.addChangeListener(this);
        msgChoice.addChangeListener(this);
    }

    @Override
    public void clearGui(){
        super.clearGui();
        useProperties.setSelected(false);
        jndiICF.setText(""); // $NON-NLS-1$
        urlField.setText(""); // $NON-NLS-1$
        jndiConnFac.setText(""); // $NON-NLS-1$
        jmsDestination.setText(""); // $NON-NLS-1$
        expiration.setText(""); // $NON-NLS-1$
        jmsErrorReconnectOnCodes.setText("");
        priority.setText(""); // $NON-NLS-1$
        jmsUser.setText(""); // $NON-NLS-1$
        jmsPwd.setText(""); // $NON-NLS-1$
        textMessage.setInitialText(""); // $NON-NLS-1$
        messageFile.setFilename(""); // $NON-NLS-1$
        randomFile.setFilename(""); // $NON-NLS-1$
        msgChoice.setText(""); // $NON-NLS-1$
        fileEncoding.setSelectedIndex(0);
        configChoice.setText(USE_TEXT_RSC);
        updateConfig(USE_TEXT_RSC);
        msgChoice.setText(TEXT_MSG_RSC);
        iterations.setText("1"); // $NON-NLS-1$
        useAuth.setSelected(false);
        jmsUser.setEnabled(false);
        jmsPwd.setEnabled(false);
        destSetup.setText(DEST_SETUP_STATIC);
        useNonPersistentDelivery.setSelected(false);
        jmsPropertiesPanel.clearGui();
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
        jmsDestination.setText(sampler.getDestination());
        jmsUser.setText(sampler.getUsername());
        jmsPwd.setText(sampler.getPassword());
        textMessage.setInitialText(sampler.getTextMessage());
        textMessage.setCaretPosition(0);
        messageFile.setFilename(sampler.getInputFile());
        randomFile.setFilename(sampler.getRandomPath());
        configChoice.setText(sampler.getConfigChoice());
        msgChoice.setText(sampler.getMessageChoice());
        fileEncoding.setText(sampler.getFileEncoding());
        iterations.setText(sampler.getIterations());
        expiration.setText(sampler.getExpiration());
        jmsErrorReconnectOnCodes.setText(sampler.getReconnectionErrorCodes());
        priority.setText(sampler.getPriority());
        useAuth.setSelected(sampler.isUseAuth());
        jmsUser.setEnabled(useAuth.isSelected());
        jmsPwd.setEnabled(useAuth.isSelected());
        destSetup.setText(sampler.isDestinationStatic() ? DEST_SETUP_STATIC : DEST_SETUP_DYNAMIC);
        useNonPersistentDelivery.setSelected(sampler.getUseNonPersistentDelivery());
        jmsPropertiesPanel.configure(sampler.getJMSProperties());
        updateChoice(msgChoice.getText());
        updateConfig(sampler.getConfigChoice());
    }

    /**
     * When a widget state changes, it will notify this class so we can
     * enable/disable the correct items.
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == configChoice) {
            updateConfig(configChoice.getText());
        } else if (event.getSource() == msgChoice) {
            updateChoice(msgChoice.getText());
        } else if (event.getSource() == useProperties) {
            final boolean isUseProperties = useProperties.isSelected();
            jndiICF.setEnabled(!isUseProperties);
            urlField.setEnabled(!isUseProperties);
            useAuth.setEnabled(!isUseProperties);
        } else if (event.getSource() == useAuth) {
            jmsUser.setEnabled(useAuth.isSelected() && useAuth.isEnabled());
            jmsPwd.setEnabled(useAuth.isSelected()  && useAuth.isEnabled());
        }
    }

    private void updateFileEncoding() {
        boolean isTextMode = USE_TEXT_RSC.equals(configChoice.getText());
        boolean isObjectType = OBJECT_MSG_RSC.equals(msgChoice.getText());
        fileEncoding.setChoiceListEnabled(!isTextMode && !isObjectType);
    }
    /**
     * Update choice contains the actual logic for hiding or showing Textarea if Bytes message
     * is selected
     *
     * @param command
     * @since 2.9
     */
    private void updateChoice(String command) {
        String oldChoice = configChoice.getText();
        if (BYTES_MSG_RSC.equals(command)) {
            String newChoice = USE_TEXT_RSC.equals(oldChoice) ?
                    USE_FILE_RSC : oldChoice;
            configChoice.resetButtons(CONFIG_ITEMS_BYTES_MSG, newChoice);
            textMessage.setEnabled(false);
        } else {
            configChoice.resetButtons(CONFIG_ITEMS, oldChoice);
            textMessage.setEnabled(true);
        }
        updateFileEncoding();
        validate();
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
        updateFileEncoding();
    }

    /**
     * @return JPanel that contains destination infos
     */
    private JPanel createDestinationPane() {
        JPanel pane = new JPanel(new BorderLayout(3, 0));
        pane.add(jmsDestination, BorderLayout.WEST);
        destSetup.setLayout(new BoxLayout(destSetup, BoxLayout.X_AXIS));
        pane.add(destSetup, BorderLayout.CENTER);
        pane.add(useNonPersistentDelivery, BorderLayout.EAST);
        return pane;
    }

    /**
     * @return JPanel Panel with checkbox to choose auth , user and password
     */
    private JPanel createAuthPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(useAuth);
        pane.add(Box.createHorizontalStrut(10));
        pane.add(jmsUser);
        pane.add(Box.createHorizontalStrut(10));
        pane.add(jmsPwd);
        return pane;
    }

    /**
     * @return JPanel Panel for priority and expiration
     */
    private JPanel createPriorityAndExpiration() {
        JPanel panel = new HorizontalPanel();
        panel.add(expiration);
        panel.add(priority);
        return panel;
    }
}
