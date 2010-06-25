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
 
 package org.apache.jmeter.assertions.gui;
 
 import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.assertions.SMIMEAssertionTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
 
 public class SMIMEAssertionGui extends AbstractAssertionGui {
 
	private static final long serialVersionUID = 1L;

	private JCheckBox verifySignature = new JCheckBox();
 
 	private JCheckBox notSigned = new JCheckBox();
 
 	private JRadioButton signerNoCheck = new JRadioButton();
 
 	private JRadioButton signerCheckConstraints = new JRadioButton();
 
 	private JRadioButton signerCheckByFile = new JRadioButton();
 
 	private JTextField signerDnField = new JTextField();
 
 	private JTextField signerSerialNumberField = new JTextField();
 
 	private JTextField signerEmailField = new JTextField();
 
 	private JTextField issuerDnField = new JTextField();
 
 	private JTextField signerCertFile = new JTextField();
 
 	public SMIMEAssertionGui() {
 		init();
 	}
 
 	public String getLabelResource() {
 		return "smime_assertion_title";
 	}
 
 	private void init() {
 		setLayout(new BorderLayout());
 		setBorder(makeBorder());
 
 		Box box = Box.createVerticalBox();
 		box.add(makeTitlePanel());
		box.add(createSignaturePanel());
 		box.add(createSignerPanel());
 		add(box, BorderLayout.NORTH);
 	}
 
	private JPanel createSignaturePanel() {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils
 			.getResString("smime_assertion_signature")));
  
 		verifySignature = new JCheckBox(JMeterUtils
 			.getResString("smime_assertion_verify_signature"));
 
 		notSigned = new JCheckBox(JMeterUtils
 			.getResString("smime_assertion_not_signed"));
 		notSigned.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				verifySignature.setEnabled(!notSigned.isSelected());
 			}
 		});
 
 		panel.add(verifySignature);
 		panel.add(notSigned);
 
 		return panel;
 	}
 
 	private JPanel createSignerPanel() {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils
 			.getResString("smime_assertion_signer")));
 
 		panel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
 
 		signerNoCheck = new JRadioButton(JMeterUtils
 			.getResString("smime_assertion_signer_no_check"));
 		signerCheckConstraints = new JRadioButton(JMeterUtils
 			.getResString("smime_assertion_signer_constraints"));
 		signerCheckByFile = new JRadioButton(JMeterUtils
 			.getResString("smime_assertion_signer_by_file"));
 
 		signerCertFile = new JTextField(25);
 
 		ButtonGroup buttonGroup = new ButtonGroup();
 		buttonGroup.add(signerNoCheck);
 		buttonGroup.add(signerCheckConstraints);
 		buttonGroup.add(signerCheckByFile);
 
 		panel.add(signerNoCheck);
 
 		panel.add(signerCheckConstraints);
 		signerCheckConstraints.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				signerDnField.setEnabled(signerCheckConstraints.isSelected());
 				signerSerialNumberField.setEnabled(signerCheckConstraints
 					.isSelected());
 				signerEmailField
 					.setEnabled(signerCheckConstraints.isSelected());
 				issuerDnField.setEnabled(signerCheckConstraints.isSelected());
 			}
 		});
 		Box box = Box.createHorizontalBox();
 		box.add(new JLabel(JMeterUtils
 			.getResString("smime_assertion_signer_dn")));
 		box.add(Box.createHorizontalStrut(5));
 		signerDnField = new JTextField(25);
 		box.add(signerDnField);
 		panel.add(box);
 
 		box = Box.createHorizontalBox();
 		box.add(new JLabel(JMeterUtils
 			.getResString("smime_assertion_signer_email")));
 		box.add(Box.createHorizontalStrut(5));
 		signerEmailField = new JTextField(25);
 		box.add(signerEmailField);
 		panel.add(box);
 
 		box = Box.createHorizontalBox();
 		box.add(new JLabel(JMeterUtils
 			.getResString("smime_assertion_issuer_dn")));
 		box.add(Box.createHorizontalStrut(5));
 		issuerDnField = new JTextField(25);
 		box.add(issuerDnField);
 		panel.add(box);
 
 		box = Box.createHorizontalBox();
 		box.add(new JLabel(JMeterUtils
 			.getResString("smime_assertion_signer_serial")));
 		box.add(Box.createHorizontalStrut(5));
 		signerSerialNumberField = new JTextField(25);
 		box.add(signerSerialNumberField);
 		panel.add(box);
 
 		// panel.add(signerCheckByFile);
 		signerCheckByFile.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				signerCertFile.setEnabled(signerCheckByFile.isSelected());
 			}
 		});
 		box = Box.createHorizontalBox();
 		box.add(signerCheckByFile);
 		box.add(Box.createHorizontalStrut(5));
 		box.add(signerCertFile);
 		panel.add(box);
 
 		return panel;
 	}
 
 	public void configure(TestElement el) {
 		super.configure(el);
 		SMIMEAssertionTestElement smimeAssertion = (SMIMEAssertionTestElement) el;
 		verifySignature.setSelected(smimeAssertion.isVerifySignature());
 		notSigned.setSelected(smimeAssertion.isNotSigned());
 
 		if (smimeAssertion.isSignerNoCheck())
 			signerNoCheck.setSelected(true);
 		if (smimeAssertion.isSignerCheckConstraints())
 			signerCheckConstraints.setSelected(true);
 		if (smimeAssertion.isSignerCheckByFile())
 			signerCheckByFile.setSelected(true);
 
 		issuerDnField.setText(smimeAssertion.getIssuerDn());
 		signerDnField.setText(smimeAssertion.getSignerDn());
 		signerSerialNumberField.setText(smimeAssertion.getSignerSerial());
 		signerEmailField.setText(smimeAssertion.getSignerEmail());
 
 		signerCertFile.setText(smimeAssertion.getSignerCertFile());
 	}
 
 	public void modifyTestElement(TestElement el) {
 		configureTestElement(el);
 		SMIMEAssertionTestElement smimeAssertion = (SMIMEAssertionTestElement) el;
 		smimeAssertion.setVerifySignature(verifySignature.isSelected());
 		smimeAssertion.setNotSigned(notSigned.isSelected());
 
 		smimeAssertion.setIssuerDn(issuerDnField.getText());
 		smimeAssertion.setSignerDn(signerDnField.getText());
 		smimeAssertion.setSignerSerial(signerSerialNumberField.getText());
 		smimeAssertion.setSignerEmail(signerEmailField.getText());
 
 		smimeAssertion.setSignerCertFile(signerCertFile.getText());
 
 		smimeAssertion.setSignerNoCheck(signerNoCheck.isSelected());
 		smimeAssertion.setSignerCheckConstraints(signerCheckConstraints
 				.isSelected());
 		smimeAssertion.setSignerCheckByFile(signerCheckByFile.isSelected());
 	}
 
 	public TestElement createTestElement() {
 		SMIMEAssertionTestElement smimeAssertion = new SMIMEAssertionTestElement();
 		modifyTestElement(smimeAssertion);
 		return smimeAssertion;
 	}
 
 }
