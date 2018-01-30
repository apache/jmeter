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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.SMIMEAssertionTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

 public class SMIMEAssertionGui extends AbstractAssertionGui {

    private static final long serialVersionUID = 1L;

    private final JCheckBox verifySignature =
        new JCheckBox(JMeterUtils.getResString("smime_assertion_verify_signature")); // $NON-NLS-1$

    private final JCheckBox notSigned =
        new JCheckBox(JMeterUtils.getResString("smime_assertion_not_signed")); // $NON-NLS-1$

    private final JRadioButton signerNoCheck =
        new JRadioButton(JMeterUtils.getResString("smime_assertion_signer_no_check")); // $NON-NLS-1$

    private final JRadioButton signerCheckConstraints =
        new JRadioButton(JMeterUtils.getResString("smime_assertion_signer_constraints")); // $NON-NLS-1$

    private final JRadioButton signerCheckByFile =
        new JRadioButton(JMeterUtils.getResString("smime_assertion_signer_by_file")); // $NON-NLS-1$

    private final JTextField signerDnField = new JTextField(50);

    private final JTextField signerSerialNumberField = new JTextField(25);

    private final JTextField signerEmailField = new JTextField(25);

    private final JTextField issuerDnField = new JTextField(50);

    private final JTextField signerCertFile = new JTextField(25);

    private final JTextField messagePositionTf = new JTextField(25);

    public SMIMEAssertionGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "smime_assertion_title";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        issuerDnField.setText("");
        messagePositionTf.setText("");
        notSigned.setSelected(false);
        signerCertFile.setText("");
        signerCheckByFile.setSelected(false);
        signerCheckConstraints.setSelected(false);
        signerDnField.setText("");
        signerEmailField.setText("");
        signerNoCheck.setSelected(false);
        signerSerialNumberField.setText("");
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createSignaturePanel());
        box.add(createSignerPanel());
        box.add(createMessagePositionPanel());
        add(box, BorderLayout.NORTH);
    }

    private JPanel createSignaturePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils
            .getResString("smime_assertion_signature"))); // $NON-NLS-1$
        notSigned.addChangeListener(
                evt -> verifySignature.setEnabled(!notSigned.isSelected()));

        panel.add(verifySignature);
        panel.add(notSigned);

        return panel;
    }

    private JPanel createSignerPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils
            .getResString("smime_assertion_signer"))); // $NON-NLS-1$

        panel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(signerNoCheck);
        buttonGroup.add(signerCheckConstraints);
        buttonGroup.add(signerCheckByFile);

        panel.add(signerNoCheck);

        panel.add(signerCheckConstraints);
        signerCheckConstraints.addChangeListener(evt -> {
                boolean signerCC = signerCheckConstraints.isSelected();
                signerDnField.setEnabled(signerCC);
                signerSerialNumberField.setEnabled(signerCC);
                signerEmailField.setEnabled(signerCC);
                issuerDnField.setEnabled(signerCC);
                });
        Box box = Box.createHorizontalBox();
        box.add(new JLabel(JMeterUtils.getResString("smime_assertion_signer_dn"))); // $NON-NLS-1$
        box.add(Box.createHorizontalStrut(5));
        box.add(signerDnField);
        panel.add(box);

        box = Box.createHorizontalBox();
        box.add(new JLabel(JMeterUtils.getResString("smime_assertion_signer_email"))); // $NON-NLS-1$
        box.add(Box.createHorizontalStrut(5));
        box.add(signerEmailField);
        panel.add(box);

        box = Box.createHorizontalBox();
        box.add(new JLabel(JMeterUtils.getResString("smime_assertion_issuer_dn"))); // $NON-NLS-1$
        box.add(Box.createHorizontalStrut(5));
        box.add(issuerDnField);
        panel.add(box);

        box = Box.createHorizontalBox();
        box.add(new JLabel(JMeterUtils.getResString("smime_assertion_signer_serial"))); // $NON-NLS-1$
        box.add(Box.createHorizontalStrut(5));
        box.add(signerSerialNumberField);
        panel.add(box);

        signerCheckByFile.addChangeListener(
                evt -> signerCertFile.setEnabled(signerCheckByFile.isSelected()));
        box = Box.createHorizontalBox();
        box.add(signerCheckByFile);
        box.add(Box.createHorizontalStrut(5));
        box.add(signerCertFile);
        panel.add(box);

        return panel;
    }

    private JPanel createMessagePositionPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils
            .getResString("smime_assertion_message_position"))); // $NON-NLS-1$
        panel.add(messagePositionTf);
        return panel;
    }
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SMIMEAssertionTestElement smimeAssertion = (SMIMEAssertionTestElement) el;
        verifySignature.setSelected(smimeAssertion.isVerifySignature());
        notSigned.setSelected(smimeAssertion.isNotSigned());

        if (smimeAssertion.isSignerNoCheck()) {
            signerNoCheck.setSelected(true);
        }
        if (smimeAssertion.isSignerCheckConstraints()) {
            signerCheckConstraints.setSelected(true);
        }
        if (smimeAssertion.isSignerCheckByFile()) {
            signerCheckByFile.setSelected(true);
        }

        issuerDnField.setText(smimeAssertion.getIssuerDn());
        signerDnField.setText(smimeAssertion.getSignerDn());
        signerSerialNumberField.setText(smimeAssertion.getSignerSerial());
        signerEmailField.setText(smimeAssertion.getSignerEmail());

        signerCertFile.setText(smimeAssertion.getSignerCertFile());
        messagePositionTf.setText(smimeAssertion.getSpecificMessagePosition());
    }

    @Override
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
        smimeAssertion.setSignerCheckConstraints(signerCheckConstraints.isSelected());
        smimeAssertion.setSignerCheckByFile(signerCheckByFile.isSelected());
        smimeAssertion.setSpecificMessagePosition(messagePositionTf.getText());
    }

    @Override
    public TestElement createTestElement() {
        SMIMEAssertionTestElement smimeAssertion = new SMIMEAssertionTestElement();
        modifyTestElement(smimeAssertion);
        return smimeAssertion;
    }

}
