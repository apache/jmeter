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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

/**
 * @author Thad Smith
 */
public class MailReaderSamplerGui extends AbstractSamplerGui {
	// Gui Components
	private JComboBox serverTypeBox;

	private JTextField serverBox;

	private JTextField usernameBox;

	private JTextField passwordBox;

	private JTextField folderBox;

	private JLabel folderLabel;

	private JRadioButton allMessagesButton;

	private JRadioButton someMessagesButton;

	private JTextField someMessagesField;

	private JCheckBox deleteBox;

	// Labels
	private final static String POP3Label = JMeterUtils.getResString("mail_reader_pop3");

	private final static String IMAPLabel = JMeterUtils.getResString("mail_reader_imap");

	private final static String ServerTypeLabel = JMeterUtils.getResString("mail_reader_server_type");

	private final static String ServerLabel = JMeterUtils.getResString("mail_reader_server");

	private final static String AccountLabel = JMeterUtils.getResString("mail_reader_account");

	private final static String PasswordLabel = JMeterUtils.getResString("mail_reader_password");

	private final static String NumMessagesLabel = JMeterUtils.getResString("mail_reader_num_messages");

	private final static String AllMessagesLabel = JMeterUtils.getResString("mail_reader_all_messages");

	private final static String DeleteLabel = JMeterUtils.getResString("mail_reader_delete");

	private final static String FolderLabel = JMeterUtils.getResString("mail_reader_folder");

	// NOTREAD private String type;
	private boolean delete;

	private int num_messages;

	public MailReaderSamplerGui() {
		// NOTREAD type = MailReaderSampler.TYPE_POP3;
		delete = false;
		init();
	}

	public String getLabelResource() {
		return "mail_reader_title";
	}

	/*
	 * (non-Javadoc) Copy the data from the test element to the GUI
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
	 */
	public void configure(TestElement element) {
		MailReaderSampler mrs = (MailReaderSampler) element;
		if (mrs.getServerType().equals(MailReaderSampler.TYPE_POP3)) {
			serverTypeBox.setSelectedItem(POP3Label);
			folderBox.setText("INBOX");
		} else {
			serverTypeBox.setSelectedItem(IMAPLabel);
			folderBox.setText(mrs.getFolder());
		}
		serverBox.setText(mrs.getServer());
		usernameBox.setText(mrs.getUserName());
		passwordBox.setText(mrs.getPassword());
		if (mrs.getNumMessages() == -1) {
			allMessagesButton.setSelected(true);
			someMessagesField.setText(Integer.toString(0));
		} else {
			someMessagesButton.setSelected(true);
			someMessagesField.setText(Integer.toString(mrs.getNumMessages()));
		}
		deleteBox.setSelected(mrs.getDeleteMessages());
		super.configure(element);
	}

	/*
	 * (non-Javadoc) Create the corresponding Test Element and set up its data
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		MailReaderSampler sampler = new MailReaderSampler();
		modifyTestElement(sampler);
		return sampler;
	}

	/*
	 * (non-Javadoc) Modifies a given TestElement to mirror the data in the gui
	 * components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement te) {
		te.clear();
		configureTestElement(te);

		MailReaderSampler mrs = (MailReaderSampler) te;

		if (((String) serverTypeBox.getSelectedItem()).equals(POP3Label))
			mrs.setServerType(MailReaderSampler.TYPE_POP3);
		else
			mrs.setServerType(MailReaderSampler.TYPE_IMAP);

		mrs.setFolder(folderBox.getText());
		mrs.setServer(serverBox.getText());
		mrs.setUserName(usernameBox.getText());
		mrs.setPassword(passwordBox.getText());
		if (allMessagesButton.isSelected())
			mrs.setNumMessages(-1);
		else
			mrs.setNumMessages(num_messages);
		mrs.setDeleteMessages(delete);
	}

	/*
	 * Helper method to set up the GUI screen
	 */
	private void init() {
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
		setBorder(makeBorder());
		add(makeTitlePanel());

		JPanel serverTypePanel = new JPanel();
		serverTypePanel.add(new JLabel(ServerTypeLabel));
		DefaultComboBoxModel serverTypeModel = new DefaultComboBoxModel();
		serverTypeModel.addElement(POP3Label);
		serverTypeModel.addElement(IMAPLabel);
		serverTypeBox = new JComboBox(serverTypeModel);
		serverTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (((String) serverTypeBox.getSelectedItem()).equals(POP3Label)) {
					// NOTREAD type = MailReaderSampler.TYPE_POP3;
					folderLabel.setEnabled(false);
					folderBox.setText("INBOX");
					folderBox.setEnabled(false);
				} else {
					// NOTREAD type = MailReaderSampler.TYPE_IMAP;
					folderLabel.setEnabled(true);
					folderBox.setEnabled(true);
				}
			}
		});
		serverTypePanel.add(serverTypeBox);
		add(serverTypePanel);

		JPanel serverPanel = new JPanel();
		serverPanel.add(new JLabel(ServerLabel));
		serverBox = new JTextField(20);
		serverPanel.add(serverBox);
		add(serverPanel);

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
		folderBox = new JTextField("INBOX", 10);
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
		someMessagesField.setText(Integer.toString(0));
		someMessagesField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				try {
					num_messages = Integer.parseInt(someMessagesField.getText());
				} catch (NumberFormatException nfe) {
					num_messages = 0;
				}
			}
		});
		allMessagesButton.setSelected(true);
		numMessagesPanel.add(allMessagesButton);
		numMessagesPanel.add(someMessagesButton);
		numMessagesPanel.add(someMessagesField);
		add(numMessagesPanel);

		deleteBox = new JCheckBox(DeleteLabel, delete);
		deleteBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					delete = true;
				} else {
					delete = false;
				}
			}
		});
		add(deleteBox);
	}
}
