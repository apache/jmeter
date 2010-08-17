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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

    // Labels - don't make these static, else language change will not work

    private final String ServerTypeLabel = JMeterUtils.getResString("mail_reader_server_type");// $NON-NLS-1$

    private final String ServerLabel = JMeterUtils.getResString("mail_reader_server");// $NON-NLS-1$

    private final String PortLabel = JMeterUtils.getResString("mail_reader_port");// $NON-NLS-1$

    private final String AccountLabel = JMeterUtils.getResString("mail_reader_account");// $NON-NLS-1$

    private final String PasswordLabel = JMeterUtils.getResString("mail_reader_password");// $NON-NLS-1$

    private final String NumMessagesLabel = JMeterUtils.getResString("mail_reader_num_messages");// $NON-NLS-1$

    private final String AllMessagesLabel = JMeterUtils.getResString("mail_reader_all_messages");// $NON-NLS-1$

    private final String DeleteLabel = JMeterUtils.getResString("mail_reader_delete");// $NON-NLS-1$

    private final String FolderLabel = JMeterUtils.getResString("mail_reader_folder");// $NON-NLS-1$

    private final String STOREMIME = JMeterUtils.getResString("mail_reader_storemime");// $NON-NLS-1$

    private static final String INBOX = "INBOX"; // $NON-NLS-1$
    
    private SecuritySettingsPanel securitySettingsPanel;

    public MailReaderSamplerGui() {
        init();
        initGui();
    }

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
        deleteBox.setSelected(mrs.getDeleteMessages());
        storeMimeMessageBox.setSelected(mrs.isStoreMimeMessage());
        securitySettingsPanel.configure(element);
        super.configure(element);
    }

    /**
     * {@inheritDoc}
     */
    public TestElement createTestElement() {
        MailReaderSampler sampler = new MailReaderSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * {@inheritDoc}
     */
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
        mrs.setDeleteMessages(deleteBox.isSelected());
        mrs.setStoreMimeMessage(storeMimeMessageBox.isSelected());
        
        securitySettingsPanel.modifyTestElement(te);
    }

    /*
     * Helper method to set up the GUI screen
     */
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = getConstraints();
        
        serverTypeBox = new JTextField(20);
        serverTypeBox.addActionListener(this);
        serverTypeBox.addFocusListener(this);
        addField(settingsPanel, ServerTypeLabel, serverTypeBox, gbc);
        
        serverBox = new JTextField(20);
        addField(settingsPanel, ServerLabel, serverBox, gbc);

        portBox = new JTextField(20);
        addField(settingsPanel, PortLabel, portBox, gbc);

        usernameBox = new JTextField(20);
        addField(settingsPanel, AccountLabel, usernameBox, gbc);

        passwordBox = new JTextField(20);
        addField(settingsPanel, PasswordLabel, passwordBox, gbc);

        folderLabel = new JLabel(FolderLabel);
        folderBox = new JTextField(INBOX, 20);
        addField(settingsPanel, folderLabel, folderBox, gbc);

        HorizontalPanel numMessagesPanel = new HorizontalPanel();
        numMessagesPanel.add(new JLabel(NumMessagesLabel));
        ButtonGroup nmbg = new ButtonGroup();
        allMessagesButton = new JRadioButton(AllMessagesLabel);
        allMessagesButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (allMessagesButton.isSelected()) {
                    someMessagesField.setEnabled(false);
                }
            }
        });
        someMessagesButton = new JRadioButton();
        someMessagesButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (someMessagesButton.isSelected()) {
                    someMessagesField.setEnabled(true);
                }
            }
        });
        nmbg.add(allMessagesButton);
        nmbg.add(someMessagesButton);
        someMessagesField = new JTextField(5);
        allMessagesButton.setSelected(true);
        numMessagesPanel.add(allMessagesButton);
        numMessagesPanel.add(someMessagesButton);
        numMessagesPanel.add(someMessagesField);

        deleteBox = new JCheckBox(DeleteLabel);

        storeMimeMessageBox = new JCheckBox(STOREMIME);
        
        securitySettingsPanel = new SecuritySettingsPanel();
        
        JPanel settings = new VerticalPanel();
        settings.add(Box.createVerticalStrut(5));
        settings.add(settingsPanel);
        settings.add(numMessagesPanel);
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
        deleteBox.setSelected(false);
        storeMimeMessageBox.setSelected(false);
        folderBox.setText(INBOX);
        serverTypeBox.setText(MailReaderSampler.DEFAULT_PROTOCOL);
        passwordBox.setText("");// $NON-NLS-1$
        serverBox.setText("");// $NON-NLS-1$
        portBox.setText("");// $NON-NLS-1$
        usernameBox.setText("");// $NON-NLS-1$
    }

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

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        actionPerformed(null);
    }
}
