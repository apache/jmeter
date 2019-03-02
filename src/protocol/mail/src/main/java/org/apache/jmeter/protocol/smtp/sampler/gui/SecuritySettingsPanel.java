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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class SecuritySettingsPanel extends JPanel{

    private static final long serialVersionUID = 1L;

    //++JMX attribute names - do not change the values!
    // These were moved from SMTPSampler, which is why the prefix is still SMTSampler
    public static final String USE_SSL              = "SMTPSampler.useSSL"; // $NON-NLS-1$
    public static final String USE_STARTTLS         = "SMTPSampler.useStartTLS"; // $NON-NLS-1$
    public static final String SSL_TRUST_ALL_CERTS  = "SMTPSampler.trustAllCerts"; // $NON-NLS-1$
    public static final String ENFORCE_STARTTLS     = "SMTPSampler.enforceStartTLS"; // $NON-NLS-1$
    public static final String USE_LOCAL_TRUSTSTORE = "SMTPSampler.useLocalTrustStore"; // $NON-NLS-1$
    public static final String TRUSTSTORE_TO_USE    = "SMTPSampler.trustStoreToUse"; // $NON-NLS-1$
    public static final String TLS_PROTOCOLS        = "SMTPSampler.tlsProtocols"; // $NON-NLS-1$
    //--JMX attribute names

    private ButtonGroup bgSecuritySettings;

    private JRadioButton rbUseNone;

    private JRadioButton rbUseSSL;

    private JRadioButton rbUseStartTLS;

    private JCheckBox cbTrustAllCerts;

    private JCheckBox cbEnforceStartTLS;

    private JCheckBox cbUseLocalTrustStore;

    private JLabel jlTrustStoreToUse;

    private JTextField tfTrustStoreToUse;

    private JTextField tfTlsProtocolsToUse;

    private JLabel jlTlsProtocolsToUse;


    public SecuritySettingsPanel() {
        super();
        init();
    }

    private void init(){ // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("smtp_security_settings"))); // $NON-NLS-1$

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 3, 3);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        rbUseNone = new JRadioButton(JMeterUtils.getResString("smtp_usenone")); // $NON-NLS-1$
        rbUseSSL = new JRadioButton(JMeterUtils.getResString("smtp_usessl")); // $NON-NLS-1$
        rbUseStartTLS = new JRadioButton(JMeterUtils.getResString("smtp_usestarttls")); // $NON-NLS-1$

        cbTrustAllCerts = new JCheckBox(JMeterUtils.getResString("smtp_trustall")); // $NON-NLS-1$
        cbEnforceStartTLS = new JCheckBox(JMeterUtils.getResString("smtp_enforcestarttls")); // $NON-NLS-1$
        cbUseLocalTrustStore = new JCheckBox(JMeterUtils.getResString("smtp_usetruststore")); // $NON-NLS-1$

        jlTrustStoreToUse = new JLabel(JMeterUtils.getResString("smtp_truststore")); // $NON-NLS-1$
        jlTlsProtocolsToUse = new JLabel(JMeterUtils.getResString("smtp_tlsprotocols")); // $NON-NLS-1$

        tfTrustStoreToUse = new JTextField(20);
        tfTlsProtocolsToUse = new JTextField(20);

        rbUseNone.setSelected(true);
        bgSecuritySettings = new ButtonGroup();
        bgSecuritySettings.add(rbUseNone);
        bgSecuritySettings.add(rbUseSSL);
        bgSecuritySettings.add(rbUseStartTLS);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        this.add(rbUseNone, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        this.add(rbUseSSL, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        this.add(rbUseStartTLS, gridBagConstraints);

        rbUseNone.addItemListener(this::rbSecuritySettingsItemStateChanged);
        rbUseSSL.addItemListener(this::rbSecuritySettingsItemStateChanged);
        rbUseStartTLS.addItemListener(this::rbSecuritySettingsItemStateChanged);

        cbTrustAllCerts.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbTrustAllCerts.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbTrustAllCerts.setEnabled(false);
        cbTrustAllCerts.setToolTipText(JMeterUtils.getResString("smtp_trustall_tooltip")); // $NON-NLS-1$
        cbTrustAllCerts.addActionListener(this::cbTrustAllCertsActionPerformed);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        this.add(cbTrustAllCerts, gridBagConstraints);

        cbEnforceStartTLS.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbEnforceStartTLS.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbEnforceStartTLS.setEnabled(false);
        cbEnforceStartTLS.addActionListener(this::cbEnforceStartTLSActionPerformed);
        cbEnforceStartTLS.setToolTipText(JMeterUtils.getResString("smtp_enforcestarttls_tooltip")); // $NON-NLS-1$

        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        this.add(cbEnforceStartTLS, gridBagConstraints);

        cbUseLocalTrustStore.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbUseLocalTrustStore.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseLocalTrustStore.setEnabled(false);
        cbUseLocalTrustStore.addActionListener(this::cbUseLocalTrustStoreActionPerformed);

        cbUseLocalTrustStore.setToolTipText(JMeterUtils.getResString("smtp_usetruststore_tooltip")); // $NON-NLS-1$

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        this.add(cbUseLocalTrustStore, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        jlTrustStoreToUse.setToolTipText(JMeterUtils.getResString("smtp_truststore_tooltip")); // $NON-NLS-1$
        this.add(jlTrustStoreToUse, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        tfTrustStoreToUse.setToolTipText(JMeterUtils.getResString("smtp_truststore_tooltip")); // $NON-NLS-1$
        this.add(tfTrustStoreToUse, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        jlTlsProtocolsToUse.setToolTipText(JMeterUtils.getResString("smtp_tlsprotocols_tooltip")); // $NON-NLS-1$
        this.add(jlTlsProtocolsToUse, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        tfTrustStoreToUse.setToolTipText(JMeterUtils.getResString("smtp_tlsprotocols_tooltip")); // $NON-NLS-1$
        this.add(tfTlsProtocolsToUse, gridBagConstraints);
    }

    /**
     * ActionPerformed-method for checkbox "useLocalTrustStore"
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void cbUseLocalTrustStoreActionPerformed( // NOSONAR This method is used through lambda
            ActionEvent evt) {
        final boolean selected = cbUseLocalTrustStore.isSelected();
        tfTrustStoreToUse.setEditable(selected); // must follow the checkbox setting
        if (selected) {
            cbTrustAllCerts.setSelected(false); // not compatible
        }
    }
    /**
     * ActionPerformed-method for checkbox "cbTrustAllCerts"
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void cbTrustAllCertsActionPerformed( // NOSONAR This method is used through lambda
            ActionEvent evt) {
        final boolean selected = cbTrustAllCerts.isSelected();
        if (selected) {
            cbUseLocalTrustStore.setSelected(false); // not compatible
            tfTrustStoreToUse.setEditable(false); // must follow the checkbox setting
        }
    }

    /**
     * ActionPerformed-method for checkbox "enforceStartTLS", empty method
     * header
     *
     * @param evt
     *            ActionEvent to be handled
     */
    private void cbEnforceStartTLSActionPerformed(ActionEvent evt) { // NOSONAR This method is used through lambda
        // NOOP
    }

    /**
     * ItemStateChanged-method for radiobutton "securitySettings"
     *
     * @param evt
     *            ItemEvent to be handled
     */
    private void rbSecuritySettingsItemStateChanged(ItemEvent evt) { // NOSONAR This method is used through lambda
        final Object source = evt.getSource();
        if (source == rbUseNone) {
            cbTrustAllCerts.setEnabled(false);
            cbTrustAllCerts.setSelected(false);
            cbEnforceStartTLS.setEnabled(false);
            cbEnforceStartTLS.setSelected(false);
            cbUseLocalTrustStore.setSelected(false);
            cbUseLocalTrustStore.setEnabled(false);
            tfTrustStoreToUse.setEditable(false);
            tfTlsProtocolsToUse.setEditable(false);
        } else if (source == rbUseSSL) {
            cbTrustAllCerts.setEnabled(true);
            cbEnforceStartTLS.setEnabled(false);
            cbEnforceStartTLS.setSelected(false);
            cbUseLocalTrustStore.setEnabled(true);
            tfTrustStoreToUse.setEditable(false);
            tfTlsProtocolsToUse.setEditable(true);
        } else if (source == rbUseStartTLS) {
            cbTrustAllCerts.setEnabled(true);
            cbTrustAllCerts.setSelected(false);
            cbEnforceStartTLS.setEnabled(true);
            cbUseLocalTrustStore.setEnabled(true);
            cbUseLocalTrustStore.setSelected(false);
            tfTrustStoreToUse.setEditable(false);
            tfTlsProtocolsToUse.setEditable(true);
        }
    }
    /**
     * Returns if SSL is used to secure the SMTP-connection (checkbox)
     *
     * @return true if SSL is used to secure the SMTP-connection
     */
    public boolean isUseSSL() {
        return rbUseSSL.isSelected();
    }

    /**
     * Sets SSL to be used to secure the SMTP-connection (checkbox)
     *
     * @param useSSL
     *            Use SSL to secure the connection
     */
    public void setUseSSL(boolean useSSL) {
        rbUseSSL.setSelected(useSSL);
    }

    /**
     * Returns if StartTLS is used to secure the connection (checkbox)
     *
     * @return true if StartTLS is used to secure the connection
     */
    public boolean isUseStartTLS() {
        return rbUseStartTLS.isSelected();
    }

    /**
     * Sets StartTLS to be used to secure the SMTP-connection (checkbox)
     *
     * @param useStartTLS
     *            Use StartTLS to secure the connection
     */
    public void setUseStartTLS(boolean useStartTLS) {
        rbUseStartTLS.setSelected(useStartTLS);
    }

    /**
     * Returns if StartTLS is enforced (normally, SMTP uses plain
     * SMTP-connection as fallback if "250-STARTTLS" isn't sent from the
     * mailserver) (checkbox)
     *
     * @return true if StartTLS is enforced
     */
    public boolean isEnforceStartTLS() {
        return cbEnforceStartTLS.isSelected();
    }

    /**
     * Enforces StartTLS to secure the SMTP-connection (checkbox)
     *
     * @param enforceStartTLS
     *            Enforce the use of StartTLS to secure the connection
     * @see #isEnforceStartTLS()
     */
    public void setEnforceStartTLS(boolean enforceStartTLS) {
        cbEnforceStartTLS.setSelected(enforceStartTLS);
    }
    /**
     * Returns if local (pre-installed) truststore is used to avoid
     * SSL-connection-exceptions (checkbox)
     *
     * @return true if a local truststore is used
     */
    public boolean isUseLocalTrustStore() {
        return cbUseLocalTrustStore.isSelected();
    }

    /**
     * Set the use of a local (pre-installed) truststore to avoid
     * SSL-connection-exceptions (checkbox)
     *
     * @param useLocalTrustStore
     *            Use local keystore
     */
    public void setUseLocalTrustStore(boolean useLocalTrustStore) {
        cbUseLocalTrustStore.setSelected(useLocalTrustStore);
        tfTrustStoreToUse.setEditable(useLocalTrustStore); // ensure correctly set on initial display
    }

    /**
     * Returns the path to the local (pre-installed) truststore to be used to
     * avoid SSL-connection-exceptions
     *
     * @return Path to local truststore
     */
    public String getTrustStoreToUse() {
        return tfTrustStoreToUse.getText();
    }

    /**
     * Set the path to local (pre-installed) truststore to be used to avoid
     * SSL-connection-exceptions
     *
     * @param trustStoreToUse
     *            Path to local truststore
     */
    public void setTrustStoreToUse(String trustStoreToUse) {
        tfTrustStoreToUse.setText(trustStoreToUse);
    }

    /**
     * Returns the TLS protocols to use for handshake
     *
     * @return Space separated list of protocols
     */
    public String getTlsProtocolsToUse() {
        return tfTlsProtocolsToUse.getText();
    }

    /**
     * Set the TLS protocols to use for handshake
     *
     * @param tlsProtocols
     *              Space separated list of protocols to use
     */
    public void setTlsProtocolsToUse(String tlsProtocols) {
        tfTlsProtocolsToUse.setText(tlsProtocols);
    }

    public void setUseNoSecurity(boolean selected) {
        rbUseNone.setSelected(selected);
    }
    /**
     * Returns if all certificates are blindly trusted (using according
     * SocketFactory) (checkbox)
     *
     * @return true if all certificates are blindly trusted
     */
    public boolean isTrustAllCerts() {
        return cbTrustAllCerts.isSelected();
    }

    /**
     * Enforces JMeter to trust all certificates, no matter what CA is issuer
     * (checkbox)
     *
     * @param trustAllCerts
     *            Trust all certificates
     * @see #isTrustAllCerts()
     */
    public void setTrustAllCerts(boolean trustAllCerts) {
        cbTrustAllCerts.setSelected(trustAllCerts);
    }

    public void clear() {
        tfTrustStoreToUse.setText("");
        tfTlsProtocolsToUse.setText("");
        rbUseNone.setSelected(true);
    }

    public void configure(TestElement element) {
        setUseSSL(element.getPropertyAsBoolean(USE_SSL));
        setUseStartTLS(element.getPropertyAsBoolean(USE_STARTTLS));
        if(!element.getPropertyAsBoolean(USE_STARTTLS) && !element.getPropertyAsBoolean(USE_SSL)){
            setUseNoSecurity(true);
        }
        setTrustAllCerts(element.getPropertyAsBoolean(SSL_TRUST_ALL_CERTS));
        setEnforceStartTLS(element.getPropertyAsBoolean(ENFORCE_STARTTLS));
        setUseLocalTrustStore(element.getPropertyAsBoolean(USE_LOCAL_TRUSTSTORE));
        setTrustStoreToUse(element.getPropertyAsString(TRUSTSTORE_TO_USE));
        setTlsProtocolsToUse(element.getPropertyAsString(TLS_PROTOCOLS));
    }

    public void modifyTestElement(TestElement te) {
        te.setProperty(USE_SSL, Boolean.toString(isUseSSL()));
        te.setProperty(USE_STARTTLS, Boolean.toString(isUseStartTLS()));
        te.setProperty(SSL_TRUST_ALL_CERTS, Boolean.toString(isTrustAllCerts()));
        te.setProperty(ENFORCE_STARTTLS, Boolean.toString(isEnforceStartTLS()));
        te.setProperty(USE_LOCAL_TRUSTSTORE, Boolean.toString(isUseLocalTrustStore()));
        te.setProperty(TRUSTSTORE_TO_USE, getTrustStoreToUse());
        te.setProperty(TLS_PROTOCOLS, getTlsProtocolsToUse());
    }
}
