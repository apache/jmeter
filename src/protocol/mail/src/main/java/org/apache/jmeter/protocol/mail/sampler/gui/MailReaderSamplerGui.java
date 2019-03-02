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
package org.apache.jmeter.protocol.mail.sampler.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mail.sampler.MailReaderSampler;
import org.apache.jmeter.protocol.smtp.sampler.gui.SecuritySettingsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class MailReaderSamplerGui extends AbstractSamplerGui implements ActionListener, FocusListener {

    private static final long serialVersionUID = 240L;

    // Gui Components
    private JTextField serverTypeBox;

    private JTextField serverBox;

    private JTextField portBox;

    private JTextField usernameBox;

    private JTextField passwordBox;

    private JTextField folderBox;

    private JLabel folderLabel;

    private JRadioButton allMessagesButton;

    private JRadioButton someMessagesButton;

    private JTextField someMessagesField;

    private JCheckBox deleteBox;

    private JCheckBox storeMimeMessageBox;

    private JCheckBox headerOnlyBox;

    // Labels - don't make these static, else language change will not work

    private final String serverTypeLabel = JMeterUtils.getResString("mail_reader_server_type");// $NON-NLS-1$

    private final String serverLabel = JMeterUtils.getResString("mail_reader_server");// $NON-NLS-1$

    private final String portLabel = JMeterUtils.getResString("mail_reader_port");// $NON-NLS-1$

    private final String accountLabel = JMeterUtils.getResString("mail_reader_account");// $NON-NLS-1$

    private final String passwordLabel = JMeterUtils.getResString("mail_reader_password");// $NON-NLS-1$

    private final String numMessagesLabel = JMeterUtils.getResString("mail_reader_num_messages");// $NON-NLS-1$

    private final String allMessagesLabel = JMeterUtils.getResString("mail_reader_all_messages");// $NON-NLS-1$

    private final String deleteLabel = JMeterUtils.getResString("mail_reader_delete");// $NON-NLS-1$

    private final String folderLabelStr = JMeterUtils.getResString("mail_reader_folder");// $NON-NLS-1$

    private final String storeMime = JMeterUtils.getResString("mail_reader_storemime");// $NON-NLS-1$

    private final String headerOnlyLabel = JMeterUtils.getResString("mail_reader_header_only");// $NON-NLS-1$

    private static final String INBOX = "INBOX"; // $NON-NLS-1$
    
    private SecuritySettingsPanel securitySettingsPanel;

    public MailReaderSamplerGui() {
        init();
        initGui();
    }

    @Override
    public String getLabelResource() {
        return "mail_reader_title"; // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        MailReaderSampler mrs = (MailReaderSampler) element;
        serverTypeBox.setText(mrs.getServerType());
        folderBox.setText(mrs.getFolder());
        serverBox.setText(mrs.getServer());
        portBox.setText(mrs.getPort());
        usernameBox.setText(mrs.getUserName());
        passwordBox.setText(mrs.getPassword());
        if (mrs.getNumMessages() == MailReaderSampler.ALL_MESSAGES) {
            allMessagesButton.setSelected(true);
            someMessagesField.setText("0"); // $NON-NLS-1$
        } else {
            someMessagesButton.setSelected(true);
            someMessagesField.setText(mrs.getNumMessagesString());
        }
        headerOnlyBox.setSelected(mrs.getHeaderOnly());
        deleteBox.setSelected(mrs.getDeleteMessages());
        storeMimeMessageBox.setSelected(mrs.isStoreMimeMessage());
        securitySettingsPanel.configure(element);
        super.configure(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        MailReaderSampler sampler = new MailReaderSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement te) {
        te.clear();
        configureTestElement(te);

        MailReaderSampler mrs = (MailReaderSampler) te;

        mrs.setServerType(serverTypeBox.getText());
        mrs.setFolder(folderBox.getText());
        mrs.setServer(serverBox.getText());
        mrs.setPort(portBox.getText());
        mrs.setUserName(usernameBox.getText());
        mrs.setPassword(passwordBox.getText());
        if (allMessagesButton.isSelected()) {
            mrs.setNumMessages(MailReaderSampler.ALL_MESSAGES);
        } else {
            mrs.setNumMessages(someMessagesField.getText());
        }
        mrs.setHeaderOnly(headerOnlyBox.isSelected());
        mrs.setDeleteMessages(deleteBox.isSelected());
        mrs.setStoreMimeMessage(storeMimeMessageBox.isSelected());
        
        securitySettingsPanel.modifyTestElement(te);
    }

    /*
     * Helper method to set up the GUI screen
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = getConstraints();
        
        serverTypeBox = new JTextField(20);
        serverTypeBox.addActionListener(this);
        serverTypeBox.addFocusListener(this);
        addField(settingsPanel, serverTypeLabel, serverTypeBox, gbc);
        
        serverBox = new JTextField(20);
        addField(settingsPanel, serverLabel, serverBox, gbc);

        portBox = new JTextField(20);
        addField(settingsPanel, portLabel, portBox, gbc);

        usernameBox = new JTextField(20);
        addField(settingsPanel, accountLabel, usernameBox, gbc);

        passwordBox = new JPasswordField(20);
        addField(settingsPanel, passwordLabel, passwordBox, gbc);

        folderLabel = new JLabel(folderLabelStr);
        folderBox = new JTextField(INBOX, 20);
        addField(settingsPanel, folderLabel, folderBox, gbc);

        HorizontalPanel numMessagesPanel = new HorizontalPanel();
        numMessagesPanel.add(new JLabel(numMessagesLabel));
        ButtonGroup nmbg = new ButtonGroup();
        allMessagesButton = new JRadioButton(allMessagesLabel);
        allMessagesButton.addChangeListener(e -> {
            if (allMessagesButton.isSelected()) {
                someMessagesField.setEnabled(false);
            }
        });
        someMessagesButton = new JRadioButton();
        someMessagesButton.addChangeListener(e -> {
            if (someMessagesButton.isSelected()) {
                someMessagesField.setEnabled(true);
            }
        });
        nmbg.add(allMessagesButton);
        nmbg.add(someMessagesButton);
        someMessagesField = new JTextField(5);
        allMessagesButton.setSelected(true);
        numMessagesPanel.add(allMessagesButton);
        numMessagesPanel.add(someMessagesButton);
        numMessagesPanel.add(someMessagesField);

        headerOnlyBox = new JCheckBox(headerOnlyLabel);

        deleteBox = new JCheckBox(deleteLabel);

        storeMimeMessageBox = new JCheckBox(storeMime);
        
        securitySettingsPanel = new SecuritySettingsPanel();
        
        JPanel settings = new VerticalPanel();
        settings.add(Box.createVerticalStrut(5));
        settings.add(settingsPanel);
        settings.add(numMessagesPanel);
        settings.add(headerOnlyBox);
        settings.add(deleteBox);
        settings.add(storeMimeMessageBox);
        settings.add(securitySettingsPanel);

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(settings, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, JLabel label, JComponent field, GridBagConstraints gbc) {
        gbc.fill=GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(label, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(field, gbc);
        nextLine(gbc);
    }

    private void addField(JPanel panel, String text, JComponent field, GridBagConstraints gbc) {
        addField(panel, new JLabel(text), field, gbc);
    }

    private void nextLine(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
    }

    private GridBagConstraints getConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        return gbc;
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initGui();
    }

    private void initGui() {
        allMessagesButton.setSelected(true);
        headerOnlyBox.setSelected(false);
        deleteBox.setSelected(false);
        storeMimeMessageBox.setSelected(false);
        folderBox.setText(INBOX);
        serverTypeBox.setText(MailReaderSampler.DEFAULT_PROTOCOL);
        passwordBox.setText("");// $NON-NLS-1$
        serverBox.setText("");// $NON-NLS-1$
        portBox.setText("");// $NON-NLS-1$
        usernameBox.setText("");// $NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String item = serverTypeBox.getText();
        if (item.equals("pop3")||item.equals("pop3s")) {
            folderLabel.setEnabled(false);
            folderBox.setText(INBOX);
            folderBox.setEnabled(false);
        } else {
            folderLabel.setEnabled(true);
            folderBox.setEnabled(true);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        actionPerformed(null);
    }
}
