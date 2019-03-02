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

package org.apache.jmeter.protocol.smtp.sampler.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.smtp.sampler.SmtpSampler;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Class to build gui-components for SMTP-sampler. Getter-methods serve the
 * input-data to the sampler-object, which provides them to the
 * SendMailCommand-object.
 */
public class SmtpPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // local vars
    private JTextField tfMailFrom;
    private JTextField tfMailReplyTo;
    private JButton browseButton;
    private JButton emlBrowseButton;
    private JCheckBox cbUseAuth;
    private JTextField tfMailServer;
    private JTextField tfMailServerPort;
    private JTextField tfMailServerTimeout;
    private JTextField tfMailServerConnectionTimeout;
    private JTextField tfMailTo;
    private JTextField tfMailToCC;
    private JTextField tfMailToBCC;
    private JTextField tfAttachment;
    private JTextField tfEmlMessage;
    private JTextArea taMessage;
    private JCheckBox cbPlainBody;

    private JLabel jlAddressFrom;
    private JLabel jlAddressReplyTo;
    private JLabel jlAddressTo;
    private JLabel jlAddressToCC;
    private JLabel jlAddressToBCC;
    private JLabel jlMailServerPort;
    private JLabel jlMailServerTimeout;
    private JLabel jlMailServerConnectionTimeout;
    private JLabel jlMailServer;
    private JLabel jlAttachFile;
    private JLabel jlDutPortStandard;
    private JLabel jlPassword;
    private JLabel jlSubject;
    private JLabel jlUsername;
    private JLabel jlMessage;

    private JFileChooser attachmentFileChooser;
    private JFileChooser emlFileChooser;
    private JTextField tfAuthPassword;
    private JTextField tfAuthUsername;
    private JTextField tfSubject;
    private JCheckBox cbSuppressSubject;
    private JCheckBox cbIncludeTimestamp;
    private JCheckBox cbMessageSizeStats;
    private JCheckBox cbEnableDebug;
    private JCheckBox cbUseEmlMessage;

    private JPanel headerFieldsPanel;
    private JButton addHeaderFieldButton;
    private JLabel headerFieldName;
    private JLabel headerFieldValue;
    private Map<JTextField, JTextField> headerFields = new HashMap<>();
    private Map<JButton,JTextField> removeButtons = new HashMap<>();
    private int headerGridY = 0;

    private SecuritySettingsPanel securitySettingsPanel;

    /**
     * Creates new form SmtpPanel, standard constructer. Calls
     * initComponents();.
     */
    public SmtpPanel() {
        initComponents();
    }

    /**
     * Returns sender-address for e-mail from textfield
     *
     * @return Sender
     */
    public String getMailFrom() {
        return tfMailFrom.getText();
    }

    /**
     * Returns receiver in field "to" from textfield
     *
     * @return Receiver "to"
     */
    public String getReceiverTo() {
        return tfMailTo.getText();
    }

    /**
     * Returns receiver in field "cc" from textfield
     *
     * @return Receiver "cc"
     */
    public String getReceiverCC() {
        return tfMailToCC.getText();
    }

    /**
     * Returns receiver in field "bcc" from textfield
     *
     * @return Receiver "bcc"
     */
    public String getReceiverBCC() {
        return tfMailToBCC.getText();
    }

    /**
     * Returns message body, i.e. main-mime-part of message (from textfield)
     *
     * @return Message body
     */
    public String getBody() {
        return taMessage.getText();
    }

    /**
     * Sets message body, i.e. main-mime-part of message in textfield
     *
     * @param messageBodyText
     *            Message body
     */
    public void setBody(String messageBodyText) {
        taMessage.setText(messageBodyText);
    }

    /**
     * Sets sender-address of e-mail in textfield
     *
     * @param mailFrom
     *            Sender
     */
    public void setMailFrom(String mailFrom) {
        tfMailFrom.setText(mailFrom);
    }

    /**
     * Sets receiver in textfield "to"
     *
     * @param mailTo
     *            Receiver "to"
     */
    public void setReceiverTo(String mailTo) {
        tfMailTo.setText(mailTo);
    }

    /**
     * Sets receiver in textfield "cc"
     *
     * @param mailToCC
     *            Receiver "cc"
     */
    public void setReceiverCC(String mailToCC) {
        tfMailToCC.setText(mailToCC);
    }

    /**
     * Sets receiver in textfield "bcc"
     *
     * @param mailToBCC
     *            Receiver "bcc"
     */
    public void setReceiverBCC(String mailToBCC) {
        tfMailToBCC.setText(mailToBCC);
    }

    /**
     * Returns path of file(s) to be attached in e-mail from textfield
     *
     * @return File to attach
     */
    public String getAttachments() {
        return tfAttachment.getText();
    }

    /**
     * Sets path of file to be attached in e-mail in textfield
     *
     * @param attachments
     *            File to attach
     */
    public void setAttachments(String attachments) {
        tfAttachment.setText(attachments);
    }

    /**
     * Returns port of mail-server (standard 25 for SMTP/SMTP with StartTLS, 465
     * for SSL) from textfield
     *
     * @return Mail-server port
     */
    public String getPort() {
        return tfMailServerPort.getText();
    }

    /**
     * Sets port of mail-server
     *
     * @param port
     *            Mail-server port
     */
    public void setPort(String port) {
        tfMailServerPort.setText(port);
    }

    /**
     * Returns mail-server to be used to send message (from textfield)
     *
     * @return FQDN or IP of mail-server
     */
    public String getServer() {
        return tfMailServer.getText();
    }

    /**
     * Sets mail-server to be used to send message in textfield
     *
     * @param server
     *            FQDN or IP of mail-server
     */
    public void setServer(String server) {
        tfMailServer.setText(server);
    }

    /**
     * Returns timeout for SMTP connection from textfield
     *
     * @return Smtp timeout
     */
    public String getTimeout() {
        return tfMailServerTimeout.getText();
    }

    /**
     * Sets timeout (ms) for SMTP connection
     *
     * @param timeout
     *            SMTP Timeout (ms)
     */
    public void setTimeout(String timeout) {
        tfMailServerTimeout.setText(timeout);
    }

    /**
     * Returns connection timeout for SMTP connection from textfield
     *
     * @return SMTP connection timeout
     */
    public String getConnectionTimeout() {
        return tfMailServerConnectionTimeout.getText();
    }

    /**
     * Sets connection timeout (ms) for SMTP connection
     *
     * @param connectionTimeout
     *            SMTP Connection Timeout (ms)
     */
    public void setConnectionTimeout(String connectionTimeout) {
        tfMailServerConnectionTimeout.setText(connectionTimeout);
    }

    /**
     * Returns subject of the e-mail from textfield
     *
     * @return Subject of e-mail
     */
    public String getSubject() {
        return tfSubject.getText();
    }

    /**
     * Sets subject of the e-mail in textfield
     *
     * @param subject
     *            Subject of e-mail
     */
    public void setSubject(String subject) {
        tfSubject.setText(subject);
    }

    /**
     * Returns true if subject header should be suppressed
     *
     * @return true if subject header should be suppressed
     */
    public boolean isSuppressSubject() {
        return cbSuppressSubject.isSelected();
    }

    /**
     * Sets the property that defines if the subject header should be suppressed
     *
     * @param emptySubject flag whether subject header should be suppressed
     *
     */
    public void setSuppressSubject(boolean emptySubject) {
        cbSuppressSubject.setSelected(emptySubject);
    }

    /**
     * Returns true if message body should be plain (i.e. not multipart/mixed)
     *
     * @return true if using plain message body (i.e. not multipart/mixed)
     */
    public boolean isPlainBody() {
        return cbPlainBody.isSelected();
    }

    /**
     * Sets the property that defines if the body should be plain (i.e. not multipart/mixed)
     *
     * @param plainBody whether to use a plain body (i.e. not multipart/mixed)
     */
    public void setPlainBody(boolean plainBody) {
        cbPlainBody.setSelected(plainBody);
    }

    /**
     * Returns if mail-server needs authentication (checkbox)
     *
     * @return true if authentication is used
     */
    public boolean isUseAuth() {
        return cbUseAuth.isSelected();
    }

    /**
     * Set whether mail server needs auth.
     *
     * @param selected flag whether mail server needs auth
     */
    public void setUseAuth(boolean selected){
        cbUseAuth.setSelected(selected);
        tfAuthPassword.setEditable(selected); // ensure correctly set on initial display
        tfAuthUsername.setEditable(selected); // ensure correctly set on initial display
    }

    public boolean isEnableDebug() {
        return cbEnableDebug.isSelected();
    }

    public void setEnableDebug(boolean selected){
        cbEnableDebug.setSelected(selected);
    }

    /**
     * Returns if an .eml-message is sent instead of the content of message-text
     * area
     *
     * @return true if .eml is sent, false if text area content is sent in
     *         e-mail
     */
    public boolean isUseEmlMessage() {
        return cbUseEmlMessage.isSelected();
    }

    /**
     * Set the use of an .eml-message instead of the content of message-text
     * area
     *
     * @param useEmlMessage
     *            Use eml message
     */
    public void setUseEmlMessage(boolean useEmlMessage) {
        cbUseEmlMessage.setSelected(useEmlMessage);
    }

    /**
     * Returns path to eml message to be sent
     *
     * @return path to eml message to be sent
     */
    public String getEmlMessage() {
        return tfEmlMessage.getText();
    }

    /**
     * Set path to eml message to be sent
     *
     * @param emlMessage
     *            path to eml message to be sent
     */
    public void setEmlMessage(String emlMessage) {
        tfEmlMessage.setText(emlMessage);
    }

    /**
     * Returns if current timestamp is included in the subject (checkbox)
     *
     * @return true if current timestamp is included in subject
     */
    public boolean isIncludeTimestamp() {
        return cbIncludeTimestamp.isSelected();
    }

    /**
     * Set timestamp to be included in the message-subject (checkbox)
     *
     * @param includeTimestamp
     *            Should timestamp be included in subject?
     */
    public void setIncludeTimestamp(boolean includeTimestamp) {
        cbIncludeTimestamp.setSelected(includeTimestamp);
    }

    /**
     * Returns if message size statistics are processed. Output of processing
     * will be included in sample result. (checkbox)
     *
     * @return True if message size will be calculated
     */
    public boolean isMessageSizeStatistics() {
        return cbMessageSizeStats.isSelected();
    }

    /**
     * Set message size to be calculated and included in sample result
     * (checkbox)
     *
     * @param val
     *            Should message size be calculated?
     */
    public void setMessageSizeStatistic(boolean val) {
        cbMessageSizeStats.setSelected(val);
    }

    public String getPassword() {
        return tfAuthPassword.getText();
    }

    public void setPassword(String authPassword) {
        tfAuthPassword.setText(authPassword);
    }

    public String getUsername() {
        return tfAuthUsername.getText();
    }

    public void setUsername(String username) {
        tfAuthUsername.setText(username);
    }

    public CollectionProperty getHeaderFields() {
        CollectionProperty result = new CollectionProperty();
        result.setName(SmtpSampler.HEADER_FIELDS);
        for (JTextField headerName : headerFields.keySet()) {
            String name = headerName.getText();
            String value = headerFields.get(headerName).getText();
            Argument argument = new Argument(name, value);
            result.addItem(argument);
        }
        return result;
    }

    public void setHeaderFields(CollectionProperty fields) {
        clearHeaderFields();
        for (int i = 0; i < fields.size(); i++) {
            Argument argument = (Argument) fields.get(i).getObjectValue();
            String name = argument.getName();
            JButton removeButton = addHeaderActionPerformed(null);
            JTextField nameTF = removeButtons.get(removeButton);
            nameTF.setText(name);
            JTextField valueTF = headerFields.get(nameTF);
            valueTF.setText(argument.getValue());
        }
        validate();
    }

    public String getMailReplyTo() {
        return tfMailReplyTo.getText();
    }

    public void setMailReplyTo(String replyTo) {
        tfMailReplyTo.setText(replyTo);        
    }
    

    /**
     * Main method of class, builds all gui-components for SMTP-sampler.
     */
    private void initComponents() {
        jlAddressReplyTo = new JLabel(JMeterUtils.getResString("smtp_replyto")); // $NON-NLS-1$
        jlAddressFrom = new JLabel(JMeterUtils.getResString("smtp_from")); // $NON-NLS-1$
        jlAddressTo = new JLabel(JMeterUtils.getResString("smtp_to")); // $NON-NLS-1$
        jlAddressToCC = new JLabel(JMeterUtils.getResString("smtp_cc")); // $NON-NLS-1$
        jlAddressToBCC = new JLabel(JMeterUtils.getResString("smtp_bcc")); // $NON-NLS-1$
        jlMailServerPort = new JLabel(JMeterUtils.getResString("smtp_server_port")); // $NON-NLS-1$
        jlMailServer = new JLabel(JMeterUtils.getResString("smtp_server")); // $NON-NLS-1$
        jlMailServerTimeout = new JLabel(JMeterUtils.getResString("smtp_server_timeout")); // $NON-NLS-1$
        jlMailServerConnectionTimeout = new JLabel(JMeterUtils.getResString("smtp_server_connection_timeout")); // $NON-NLS-1$
        jlAttachFile = new JLabel(JMeterUtils.getResString("smtp_attach_file")); // $NON-NLS-1$
        jlDutPortStandard = new JLabel(JMeterUtils.getResString("smtp_default_port")); // $NON-NLS-1$
        jlUsername = new JLabel(JMeterUtils.getResString("smtp_username")); // $NON-NLS-1$
        jlPassword = new JLabel(JMeterUtils.getResString("smtp_password")); // $NON-NLS-1$
        jlSubject = new JLabel(JMeterUtils.getResString("smtp_subject")); // $NON-NLS-1$
        jlMessage = new JLabel(JMeterUtils.getResString("smtp_message")); // $NON-NLS-1$

        tfMailServer = new JTextField(30);
        tfMailServerPort = new JTextField(6);
        tfMailServerTimeout = new JTextField(6);
        tfMailServerConnectionTimeout = new JTextField(6);
        tfMailFrom = new JTextField(25);
        tfMailReplyTo = new JTextField(25);
        tfMailTo = new JTextField(25);
        tfMailToCC = new JTextField(25);
        tfMailToBCC = new JTextField(25);
        tfAuthUsername = new JTextField(20);
        tfAuthPassword = new JPasswordField(20);
        tfSubject = new JTextField(20);
        tfAttachment = new JTextField(30);
        tfEmlMessage = new JTextField(30);

        taMessage = new JTextArea(5, 20);

        cbPlainBody = new JCheckBox(JMeterUtils.getResString("smtp_plainbody")); // $NON-NLS-1$
        
        cbSuppressSubject = new JCheckBox(JMeterUtils.getResString("smtp_suppresssubj")); // $NON-NLS-1$
        cbSuppressSubject.addChangeListener(this::emptySubjectActionPerformed);

        cbUseAuth = new JCheckBox(JMeterUtils.getResString("smtp_useauth")); // $NON-NLS-1$

        cbIncludeTimestamp = new JCheckBox(JMeterUtils.getResString("smtp_timestamp")); // $NON-NLS-1$
        cbMessageSizeStats = new JCheckBox(JMeterUtils.getResString("smtp_messagesize")); // $NON-NLS-1$
        cbEnableDebug = new JCheckBox(JMeterUtils.getResString("smtp_enabledebug")); // $NON-NLS-1$
        cbUseEmlMessage = new JCheckBox(JMeterUtils.getResString("smtp_eml")); // $NON-NLS-1$

        attachmentFileChooser = new JFileChooser();
        emlFileChooser = new JFileChooser();

        browseButton = new JButton(JMeterUtils.getResString("browse")); // $NON-NLS-1$
        emlBrowseButton = new JButton(JMeterUtils.getResString("browse")); // $NON-NLS-1$

        attachmentFileChooser
                .addActionListener(this::attachmentFolderFileChooserActionPerformed);

        emlFileChooser.addActionListener(this::emlFileChooserActionPerformed);

        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraintsMain = new GridBagConstraints();
        gridBagConstraintsMain.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsMain.anchor = GridBagConstraints.WEST;
        gridBagConstraintsMain.weightx = 0.5;

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        /*
         * Server Settings
         */
        JPanel panelServerSettings = new VerticalPanel();
        panelServerSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_server_settings"))); // $NON-NLS-1$
        
        JPanel panelMailServer = new JPanel(new BorderLayout(5, 0));
        panelMailServer.add(jlMailServer, BorderLayout.WEST);
        panelMailServer.add(tfMailServer, BorderLayout.CENTER);
        JPanel panelMailServerPort = new JPanel(new BorderLayout(5, 0));
        panelMailServerPort.add(jlMailServerPort, BorderLayout.WEST);
        panelMailServerPort.add(tfMailServerPort, BorderLayout.CENTER);
        panelMailServerPort.add(jlDutPortStandard, BorderLayout.EAST);
        
        panelServerSettings.add(panelMailServer, BorderLayout.CENTER);
        panelServerSettings.add(panelMailServerPort, BorderLayout.SOUTH);

        JPanel panelServerTimeoutsSettings = new VerticalPanel();
        panelServerTimeoutsSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_server_timeouts_settings"))); // $NON-NLS-1$
        
        JPanel panelMailServerConnectionTimeout = new JPanel(new BorderLayout(5, 0));
        panelMailServerConnectionTimeout.add(jlMailServerConnectionTimeout, BorderLayout.WEST);
        panelMailServerConnectionTimeout.add(tfMailServerConnectionTimeout, BorderLayout.CENTER);
        JPanel panelMailServerTimeout = new JPanel(new BorderLayout(5, 0));
        panelMailServerTimeout.add(jlMailServerTimeout, BorderLayout.WEST);
        panelMailServerTimeout.add(tfMailServerTimeout, BorderLayout.CENTER);
        
        panelServerTimeoutsSettings.add(panelMailServerConnectionTimeout, BorderLayout.CENTER);
        panelServerTimeoutsSettings.add(panelMailServerTimeout, BorderLayout.SOUTH);

        JPanel panelServerConfig = new HorizontalPanel();
        panelServerConfig.add(panelServerSettings, BorderLayout.CENTER);
        panelServerConfig.add(panelServerTimeoutsSettings, BorderLayout.EAST);

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 0;
        add(panelServerConfig, gridBagConstraintsMain);

        /*
         * E-Mail Settings
         */
        JPanel panelMailSettings = new JPanel(new GridBagLayout());
        panelMailSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_mail_settings"))); // $NON-NLS-1$

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelMailSettings.add(jlAddressFrom, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        panelMailSettings.add(tfMailFrom, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panelMailSettings.add(jlAddressTo, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        panelMailSettings.add(tfMailTo, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        panelMailSettings.add(jlAddressToCC, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        panelMailSettings.add(tfMailToCC, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        panelMailSettings.add(jlAddressToBCC, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        panelMailSettings.add(tfMailToBCC, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        panelMailSettings.add(jlAddressReplyTo, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        panelMailSettings.add(tfMailReplyTo, gridBagConstraints);

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 1;
        add(panelMailSettings, gridBagConstraintsMain);

        /*
         * Auth Settings
         */
        JPanel panelAuthSettings = new JPanel(new GridBagLayout());
        panelAuthSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_auth_settings"))); // $NON-NLS-1$

        cbUseAuth.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbUseAuth.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseAuth.addActionListener(this::cbUseAuthActionPerformed);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelAuthSettings.add(cbUseAuth, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0;
        panelAuthSettings.add(jlUsername, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 0.5;
        panelAuthSettings.add(tfAuthUsername, gridBagConstraints);
        tfAuthUsername.setEditable(false);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 0;
        panelAuthSettings.add(jlPassword, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.5;
        panelAuthSettings.add(tfAuthPassword, gridBagConstraints);
        tfAuthPassword.setEditable(false);

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 2;
        add(panelAuthSettings, gridBagConstraintsMain);

        /*
         * Security Settings
         */
        securitySettingsPanel = new SecuritySettingsPanel();

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 3;
        add(securitySettingsPanel, gridBagConstraintsMain);

        /*
         * (non-Javadoc) Message Settings
         */
        JPanel panelMessageSettings = new JPanel(new GridBagLayout());
        panelMessageSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_message_settings"))); // $NON-NLS-1$

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelMessageSettings.add(jlSubject, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panelMessageSettings.add(tfSubject, gridBagConstraints);

        cbSuppressSubject.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSuppressSubject.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(cbSuppressSubject, gridBagConstraints);

        cbIncludeTimestamp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbIncludeTimestamp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(cbIncludeTimestamp, gridBagConstraints);

        /*
         * Add the header panel
         */

        addHeaderFieldButton = new JButton(JMeterUtils.getResString("smtp_header_add")); // $NON-NLS-1$
        addHeaderFieldButton.addActionListener(this::addHeaderActionPerformed);
        headerFieldName = new JLabel(JMeterUtils.getResString("smtp_header_name")); // $NON-NLS-1$
        headerFieldValue = new JLabel(JMeterUtils.getResString("smtp_header_value")); // $NON-NLS-1$
        headerFieldsPanel = new JPanel(new GridBagLayout());

        headerFieldName.setVisible(false);
        headerFieldValue.setVisible(false);

        headerGridY=0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = headerGridY++;
        headerFieldsPanel.add(addHeaderFieldButton, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = headerGridY;
        headerFieldsPanel.add(headerFieldName, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = headerGridY++;
        headerFieldsPanel.add(headerFieldValue, gridBagConstraints);

        gridBagConstraintsMain.gridx = 1;
        gridBagConstraintsMain.gridy = 2;
        panelMessageSettings.add(headerFieldsPanel, gridBagConstraintsMain);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        panelMessageSettings.add(jlMessage, gridBagConstraints);

        taMessage.setBorder(BorderFactory.createBevelBorder(1));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        panelMessageSettings.add(taMessage, gridBagConstraints);
        
        cbPlainBody.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbPlainBody.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(cbPlainBody, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(jlAttachFile, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panelMessageSettings.add(tfAttachment, gridBagConstraints);
        tfAttachment.setToolTipText(JMeterUtils.getResString("smtp_attach_file_tooltip")); // $NON-NLS-1$

        browseButton.addActionListener(this::browseButtonActionPerformed);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(browseButton, gridBagConstraints);

        cbUseEmlMessage.setSelected(false);
        cbUseEmlMessage.addActionListener(this::cbUseEmlMessageActionPerformed);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(cbUseEmlMessage, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        tfEmlMessage.setEnabled(false);
        panelMessageSettings.add(tfEmlMessage, gridBagConstraints);

        emlBrowseButton.addActionListener(this::emlBrowseButtonActionPerformed);
        emlBrowseButton.setEnabled(false);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        panelMessageSettings.add(emlBrowseButton, gridBagConstraints);

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 6;
        add(panelMessageSettings, gridBagConstraintsMain);

        /*
         * Additional Settings
         */
        JPanel panelAdditionalSettings = new JPanel(new GridBagLayout());
        panelAdditionalSettings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_additional_settings"))); // $NON-NLS-1$

        cbMessageSizeStats.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbMessageSizeStats.setMargin(new java.awt.Insets(0, 0, 0, 0));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelAdditionalSettings.add(cbMessageSizeStats, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        panelAdditionalSettings.add(cbEnableDebug, gridBagConstraints);

        gridBagConstraintsMain.gridx = 0;
        gridBagConstraintsMain.gridy = 7;
        add(panelAdditionalSettings, gridBagConstraintsMain);
    }

    /**
     * ActionPerformed-method for checkbox "useAuth"
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void cbUseAuthActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        tfAuthUsername.setEditable(cbUseAuth.isSelected());
        tfAuthPassword.setEditable(cbUseAuth.isSelected());
    }

    /**
     * ActionPerformed-method for filechoser "attachmentFileChoser", creates
     * FileChoser-Object
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void attachmentFolderFileChooserActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        File chosen = attachmentFileChooser.getSelectedFile();
        if (chosen == null) {
            return;
        }
        final String attachments = tfAttachment.getText().trim();
        if (attachments.length() > 0) {
            tfAttachment.setText(attachments
                            + SmtpSampler.FILENAME_SEPARATOR
                            + chosen.getAbsolutePath());
        } else {
            tfAttachment.setText(chosen.getAbsolutePath());
        }
    }

    /**
     * ActionPerformed-method for button "browseButton", opens FileDialog-Object
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void browseButtonActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        attachmentFileChooser.showOpenDialog(this);
    }

    private void cbUseEmlMessageActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        if (cbUseEmlMessage.isSelected()) {
            tfEmlMessage.setEnabled(true);
            emlBrowseButton.setEnabled(true);
            taMessage.setEnabled(false);
            tfAttachment.setEnabled(false);
            browseButton.setEnabled(false);
        } else {
            tfEmlMessage.setEnabled(false);
            emlBrowseButton.setEnabled(false);
            taMessage.setEnabled(true);
            tfAttachment.setEnabled(true);
            browseButton.setEnabled(true);
        }
    }

    /**
     * ActionPerformed-method for filechoser "emlFileChoser", creates
     * FileChoser-Object
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void emlFileChooserActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        tfEmlMessage.setText(emlFileChooser.getSelectedFile().getAbsolutePath());
    }

    /**
     * ActionPerformed-method for button "emlButton", opens FileDialog-Object
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void emlBrowseButtonActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        emlFileChooser.showOpenDialog(this);
    }

    /**
     * Reset all the Gui fields.
     */
    public void clear() {
        cbIncludeTimestamp.setSelected(false);
        cbMessageSizeStats.setSelected(false);
        cbEnableDebug.setSelected(false);
        cbUseEmlMessage.setSelected(false);
        cbUseAuth.setSelected(false);
        taMessage.setText("");
        tfAttachment.setText("");
        tfAuthPassword.setText("");
        tfAuthUsername.setText("");
        tfEmlMessage.setText("");
        tfMailFrom.setText("");
        tfMailReplyTo.setText("");
        tfMailServer.setText("");
        tfMailServerPort.setText("");
        tfMailServerConnectionTimeout.setText("");
        tfMailServerTimeout.setText("");
        tfMailTo.setText("");
        tfMailToBCC.setText("");
        tfMailToCC.setText("");
        tfSubject.setText("");
        cbPlainBody.setSelected(false);
        cbSuppressSubject.setSelected(false);
        securitySettingsPanel.clear();
        clearHeaderFields();
        validate();
    }

    private void clearHeaderFields() {
        headerFieldName.setVisible(false);
        headerFieldValue.setVisible(false);

        for (Iterator<JButton> iterator = removeButtons.keySet().iterator(); iterator.hasNext();) {
            JButton removeButton = iterator.next();
            JTextField headerName = removeButtons.get(removeButton);
            JTextField headerValue = headerFields.get(headerName);

            headerFieldsPanel.remove(headerName);
            if (headerValue != null){ // Can be null (not sure why)
                headerFieldsPanel.remove(headerValue);
            }
            headerFieldsPanel.remove(removeButton);
            headerFields.remove(headerName);
            iterator.remove();
        }
    }

    private JButton addHeaderActionPerformed(ActionEvent evt){
        if(headerFields.isEmpty()){
            headerFieldName.setVisible(true);
            headerFieldValue.setVisible(true);
        }
        JTextField nameTF = new JTextField();
        JTextField valueTF = new JTextField();
        JButton removeButton = new JButton(JMeterUtils.getResString("smtp_header_remove")); // $NON-NLS-1$
        headerFields.put(nameTF, valueTF);
        removeButtons.put(removeButton, nameTF);

        removeButton.addActionListener(this::removeHeaderActionPerformed);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = headerGridY;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        headerFieldsPanel.add(nameTF, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = headerGridY;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        headerFieldsPanel.add(valueTF, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = headerGridY++;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        headerFieldsPanel.add(removeButton, gridBagConstraints);

        validate();
        return removeButton;
    }
    public SecuritySettingsPanel getSecuritySettingsPanel() {
        return securitySettingsPanel;
    }

    public void setSecuritySettingsPanel(SecuritySettingsPanel securitySettingsPanel) {
        this.securitySettingsPanel = securitySettingsPanel;
    }

    private void removeHeaderActionPerformed(ActionEvent evt){ // NOSONAR This method is used through lambda
        final Object source = evt.getSource();
        if(source instanceof JButton){
            if(headerFields.size() == 1){
                headerFieldName.setVisible(false);
                headerFieldValue.setVisible(false);
            }
            JTextField nameTF = removeButtons.get(source);
            JTextField valueTF = headerFields.get(nameTF);
            headerFields.remove(nameTF);

            headerFieldsPanel.remove(nameTF);
            headerFieldsPanel.remove(valueTF);
            headerFieldsPanel.remove((JButton)source);
            validate();
        }
    }
    
    private void emptySubjectActionPerformed(ChangeEvent evt) { // NOSONAR This method is used through lambda
        final Object source = evt.getSource();
        if(source instanceof JCheckBox){
            if(cbSuppressSubject.isSelected()){
                tfSubject.setEnabled(false);
                cbIncludeTimestamp.setEnabled(false);
            }else{
                tfSubject.setEnabled(true);
                cbIncludeTimestamp.setEnabled(true);
            }
        }
    }

}
