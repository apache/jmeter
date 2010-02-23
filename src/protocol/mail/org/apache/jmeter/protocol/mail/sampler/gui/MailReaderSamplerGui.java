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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.mail.sampler.MailReaderSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

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
    }

    // TODO - fix GUI layout problems

    /*
     * Helper method to set up the GUI screen
     */
    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        JPanel serverTypePanel = new JPanel();
        serverTypePanel.add(new JLabel(ServerTypeLabel));
        serverTypeBox = new JTextField(20);
        serverTypeBox.addActionListener(this);
        serverTypeBox.addFocusListener(this);
        serverTypePanel.add(serverTypeBox);
        add(serverTypePanel);

        JPanel serverPanel = new JPanel();
        serverPanel.add(new JLabel(ServerLabel));
        serverBox = new JTextField(20);
        serverPanel.add(serverBox);
        add(serverPanel);

        JPanel portPanel = new JPanel();
        portPanel.add(new JLabel(PortLabel));
        portBox = new JTextField(20);
        portPanel.add(portBox);
        add(portPanel);

        JPanel accountNamePanel = new JPanel();
        accountNamePanel.add(new JLabel(AccountLabel));
        usernameBox = new JTextField(20);
        accountNamePanel.add(usernameBox);
        add(accountNamePanel);

        JPanel accountPassPanel = new JPanel();
        accountPassPanel.add(new JLabel(PasswordLabel));
        passwordBox = new JTextField(20);
        accountPassPanel.add(passwordBox);
        add(accountPassPanel);

        JPanel folderPanel = new JPanel();
        folderLabel = new JLabel(FolderLabel);
        folderBox = new JTextField(INBOX, 20);
        folderPanel.add(folderLabel);
        folderPanel.add(folderBox);
        add(folderPanel);

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
        add(numMessagesPanel);

        deleteBox = new JCheckBox(DeleteLabel);
        add(deleteBox);

        storeMimeMessageBox = new JCheckBox(STOREMIME);
        add(storeMimeMessageBox);
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
