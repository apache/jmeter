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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.JLabeledRadioI18N;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.jms.sampler.JMSProperties;
import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;

import net.miginfocom.swing.MigLayout;

/**
 * This is the GUI for JMS Publisher
 */
@TestElementMetadata(labelResource = "jms_publisher")
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
    private static final String[] CONFIG_ITEMS = { USE_TEXT_RSC, USE_FILE_RSC, USE_RANDOM_RSC };

    private static final String[] MSGTYPES_ITEMS = { TEXT_MSG_RSC, MAP_MSG_RSC, OBJECT_MSG_RSC, BYTES_MSG_RSC };

    private final JCheckBox useProperties = new JCheckBox(JMeterUtils.getResString("jms_use_properties_file"), false); //$NON-NLS-1$

    private final JLabeledRadioI18N configChoice = new JLabeledRadioI18N("jms_config", CONFIG_ITEMS, USE_TEXT_RSC); //$NON-NLS-1$

    private final JTextField jndiICF = new JTextField();

    private final JTextField urlField = new JTextField();

    private final JTextField jndiConnFac = new JTextField();

    private final JTextField jmsDestination = new JTextField();

    private final JLabeledTextField expiration = new JLabeledTextField(JMeterUtils.getResString("jms_expiration"),10); //$NON-NLS-1$

    private final JTextField jmsErrorReconnectOnCodes = new JTextField();

    private final JLabeledTextField priority = new JLabeledTextField(JMeterUtils.getResString("jms_priority"),1); //$NON-NLS-1$

    private final JCheckBox useAuth = new JCheckBox(JMeterUtils.getResString("jms_use_auth"), false); //$NON-NLS-1$

    private final JLabeledTextField jmsUser = new JLabeledTextField(JMeterUtils.getResString("jms_user")); //$NON-NLS-1$

    private final JLabeledTextField jmsPwd = new JLabeledPasswordField(JMeterUtils.getResString("jms_pwd")); //$NON-NLS-1$

    private final JTextField iterations = new JTextField();

    private final FilePanel messageFile = new FilePanel(JMeterUtils.getResString("jms_file")); //$NON-NLS-1$

    private final FilePanel randomFile = new FilePanel(JMeterUtils.getResString("jms_random_file"), true); //$NON-NLS-1$

    private final JSyntaxTextArea textMessage = JSyntaxTextArea.getInstance(10, 50);

    private final JLabeledRadioI18N msgChoice = new JLabeledRadioI18N("jms_message_type", MSGTYPES_ITEMS, TEXT_MSG_RSC); //$NON-NLS-1$

    private JComboBox<String> fileEncoding;

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
      Object item = fileEncoding.getSelectedItem();
      if (item == null) {
          sampler.setFileEncoding("");
      } else {
          sampler.setFileEncoding((String) item);
      }
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

        JPanel mainPanel = new JPanel(new MigLayout("fillx, wrap 3, insets 0", "[][fill,grow]"));
        add(mainPanel, BorderLayout.CENTER);

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

        mainPanel.add(useNonPersistentDelivery);
        mainPanel.add(expiration);
        mainPanel.add(priority);


        jmsPropertiesPanel = new JMSPropertiesPanel();
        mainPanel.add(jmsPropertiesPanel, "span, growx");

        mainPanel.add(msgChoice, "span");
        fileEncoding = new JComboBox<>(PublisherSampler.getSupportedEncodings());
        fileEncoding.setEditable(true);
        mainPanel.add(JMeterUtils.labelFor(fileEncoding, "content_encoding"));
        mainPanel.add(fileEncoding, "span, growx");

        mainPanel.add(configChoice, "span");
        mainPanel.add(JMeterUtils.labelFor(textMessage, "jms_text_area"), "span");
        mainPanel.add(JTextScrollPane.getInstance(textMessage), "span, growx");
        mainPanel.add(messageFile, "span, growx");
        mainPanel.add(randomFile, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(jmsErrorReconnectOnCodes, "jms_error_reconnect_on_codes"));
        mainPanel.add(jmsErrorReconnectOnCodes, "span, growx");

        mainPanel.add(JMeterUtils.labelFor(iterations, "jms_itertions"));
        mainPanel.add(iterations, "span, growx");

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
        fileEncoding.setSelectedItem(sampler.getFileEncoding());
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
        fileEncoding.setEnabled(!isTextMode && !isObjectType);
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
}
