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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.reporters.MailerModel;
import org.apache.jmeter.reporters.MailerResultCollector;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * TODO : - Create a subpanel for other visualizers - connect to the properties. -
 * Get the specific URL that is failing. - add a separate interface to collect
 * the thrown failure messages. - - suggestions ;-)
 */

/**
 * This class implements a visualizer that mails a message when an error occurs.
 *
 */
public class MailerVisualizer extends AbstractVisualizer implements ActionListener, Clearable, ChangeListener {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(MailerVisualizer.class);

    private JButton testerButton;

    private JTextField addressField;

    private JTextField fromField;

    private JTextField smtpHostField;

    private JTextField smtpPortField;

    private JTextField failureSubjectField;

    private JTextField successSubjectField;

    private JTextField failureField;

    private JTextField failureLimitField;

    private JTextField successLimitField;

    private JTextField smtpLoginField;

    private JTextField smtpPasswordField;

    private JComboBox<String> authTypeCombo;

    /**
     * Constructs the MailerVisualizer and initializes its GUI.
     */
    public MailerVisualizer() {
        super();
        setModel(new MailerResultCollector());
        // initialize GUI.
        initGui();
    }

    public JPanel getControlPanel() {
        return this;
    }

    /**
     * Clears any stored sampling-informations.
     */
    @Override
    public synchronized void clearData() {
        if (getModel() != null) {
            MailerModel model = ((MailerResultCollector) getModel()).getMailerModel();
            model.clear();
            updateVisualizer(model);
        }
    }

    @Override
    public void add(final SampleResult res) {
        if (getModel() != null) {
            JMeterUtils.runSafe(false, new Runnable() {
                @Override
                public void run() {
                    MailerModel model = ((MailerResultCollector) getModel()).getMailerModel();
                    // method called by add is synchronized
                    model.add(res);//this is a different model from the one used by the result collector
                    updateVisualizer(model);
                }
            });
        }
    }

    @Override
    public String toString() {
        return JMeterUtils.getResString("mailer_string"); // $NON-NLS-1$
    }

    /**
     * Initializes the GUI. Lays out components and adds them to the container.
     */
    private void initGui() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new VerticalPanel();
        Border margin = new EmptyBorder(5, 10, 5, 10);
        this.setBorder(margin);

        mainPanel.add(makeTitlePanel());

        JPanel attributePane = new VerticalPanel();
        attributePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("mailer_title_settings"))); // $NON-NLS-1$

        // Settings panes
        attributePane.add(createMailingSettings());
        attributePane.add(createSmtpSettings());

        // Test mail button
        JPanel testerPanel = new JPanel(new BorderLayout());
        testerButton = new JButton(JMeterUtils.getResString("mailer_test_mail")); // $NON-NLS-1$
        testerButton.addActionListener(this);
        testerButton.setEnabled(true);
        testerPanel.add(testerButton, BorderLayout.EAST);
        attributePane.add(testerPanel);
        mainPanel.add(attributePane);
        mainPanel.add(Box.createRigidArea(new Dimension(0,5)));

        // Failures count
        JPanel mailerPanel = new JPanel(new BorderLayout());
        mailerPanel.add(new JLabel(JMeterUtils.getResString("mailer_failures")), BorderLayout.WEST); // $NON-NLS-1$
        failureField = new JTextField(6);
        failureField.setEditable(false);
        mailerPanel.add(failureField, BorderLayout.CENTER);
        mainPanel.add(mailerPanel);

        this.add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMailingSettings() {
        JPanel settingsPane = new JPanel(new BorderLayout());
        settingsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("mailer_title_message"))); // $NON-NLS-1$

        JPanel headerPane = new JPanel(new BorderLayout());
        headerPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel fromPane = new JPanel(new BorderLayout());
        fromPane.add(new JLabel(JMeterUtils.getResString("mailer_from")), BorderLayout.WEST); // $NON-NLS-1$
        fromField = new JTextField(25);
        fromField.setEditable(true);
        fromPane.add(fromField, BorderLayout.CENTER);
        fromPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        headerPane.add(fromPane, BorderLayout.WEST);
        JPanel addressPane = new JPanel(new BorderLayout());
        addressPane.add(new JLabel(JMeterUtils.getResString("mailer_addressees")), BorderLayout.WEST); // $NON-NLS-1$
        addressField = new JTextField(10);
        addressField.setEditable(true);
        addressPane.add(addressField, BorderLayout.CENTER);
        headerPane.add(addressPane, BorderLayout.CENTER);

        JPanel successPane = new JPanel(new BorderLayout());
        successPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel succesSubjectPane = new JPanel(new BorderLayout());
        succesSubjectPane.add(new JLabel(JMeterUtils.getResString("mailer_success_subject")), BorderLayout.WEST); // $NON-NLS-1$
        successSubjectField = new JTextField(10);
        successSubjectField.setEditable(true);
        succesSubjectPane.add(successSubjectField, BorderLayout.CENTER);
        succesSubjectPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        successPane.add(succesSubjectPane, BorderLayout.CENTER);
        JPanel successLimitPane = new JPanel(new BorderLayout());
        successLimitPane.add(new JLabel(JMeterUtils.getResString("mailer_success_limit")), BorderLayout.WEST); // $NON-NLS-1$
        successLimitField = new JTextField("2", 5); // $NON-NLS-1$
        successLimitField.setEditable(true);
        successLimitPane.add(successLimitField, BorderLayout.CENTER);
        successPane.add(successLimitPane, BorderLayout.EAST);

        JPanel failurePane = new JPanel(new BorderLayout());
        failurePane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel failureSubjectPane = new JPanel(new BorderLayout());
        failureSubjectPane.add(new JLabel(JMeterUtils.getResString("mailer_failure_subject")), BorderLayout.WEST); // $NON-NLS-1$
        failureSubjectField = new JTextField(10);
        failureSubjectField.setEditable(true);
        failureSubjectPane.add(failureSubjectField, BorderLayout.CENTER);
        failureSubjectPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        failurePane.add(failureSubjectPane, BorderLayout.CENTER);
        JPanel failureLimitPane = new JPanel(new BorderLayout());
        failureLimitPane.add(new JLabel(JMeterUtils.getResString("mailer_failure_limit")), BorderLayout.WEST); // $NON-NLS-1$
        failureLimitField = new JTextField("2", 5); // $NON-NLS-1$
        failureLimitField.setEditable(true);
        failureLimitPane.add(failureLimitField, BorderLayout.CENTER);
        failurePane.add(failureLimitPane, BorderLayout.EAST);

        settingsPane.add(headerPane, BorderLayout.NORTH);
        settingsPane.add(successPane, BorderLayout.CENTER);
        settingsPane.add(failurePane, BorderLayout.SOUTH);

        return settingsPane;
    }

    private JPanel createSmtpSettings() {
        JPanel settingsPane = new JPanel(new BorderLayout());
        settingsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("mailer_title_smtpserver"))); // $NON-NLS-1$

        JPanel hostPane = new JPanel(new BorderLayout());
        hostPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel smtpHostPane = new JPanel(new BorderLayout());
        smtpHostPane.add(new JLabel(JMeterUtils.getResString("mailer_host")), BorderLayout.WEST); // $NON-NLS-1$
        smtpHostField = new JTextField(10);
        smtpHostField.setEditable(true);
        smtpHostPane.add(smtpHostField, BorderLayout.CENTER);
        smtpHostPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        hostPane.add(smtpHostPane, BorderLayout.CENTER);
        JPanel smtpPortPane = new JPanel(new BorderLayout());
        smtpPortPane.add(new JLabel(JMeterUtils.getResString("mailer_port")), BorderLayout.WEST); // $NON-NLS-1$
        smtpPortField = new JTextField(10);
        smtpPortField.setEditable(true);
        smtpPortPane.add(smtpPortField, BorderLayout.CENTER);
        hostPane.add(smtpPortPane, BorderLayout.EAST);

        JPanel authPane = new JPanel(new BorderLayout());
        hostPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel smtpLoginPane = new JPanel(new BorderLayout());
        smtpLoginPane.add(new JLabel(JMeterUtils.getResString("mailer_login")), BorderLayout.WEST); // $NON-NLS-1$
        smtpLoginField = new JTextField(10);
        smtpLoginField.setEditable(true);
        smtpLoginPane.add(smtpLoginField, BorderLayout.CENTER);
        smtpLoginPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        authPane.add(smtpLoginPane, BorderLayout.CENTER);
        JPanel smtpPasswordPane = new JPanel(new BorderLayout());
        smtpPasswordPane.add(new JLabel(JMeterUtils.getResString("mailer_password")), BorderLayout.WEST); // $NON-NLS-1$
        smtpPasswordField = new JPasswordField(10);
        smtpPasswordField.setEditable(true);
        smtpPasswordPane.add(smtpPasswordField, BorderLayout.CENTER);
        smtpPasswordPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.EAST);
        authPane.add(smtpPasswordPane, BorderLayout.EAST);

        JPanel authTypePane = new JPanel(new BorderLayout());
        authTypePane.add(new JLabel(JMeterUtils.getResString("mailer_connection_security")), BorderLayout.WEST); // $NON-NLS-1$
        authTypeCombo = new JComboBox<>(new String[] {
                MailerModel.MailAuthType.NONE.toString(),
                MailerModel.MailAuthType.SSL.toString(),
                MailerModel.MailAuthType.TLS.toString()});
        authTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 10)); // $NON-NLS-1$
        authTypePane.add(authTypeCombo, BorderLayout.CENTER);

        JPanel credPane = new JPanel(new BorderLayout());
        credPane.add(authPane, BorderLayout.CENTER);
        credPane.add(authTypePane, BorderLayout.EAST);

        settingsPane.add(hostPane, BorderLayout.NORTH);
        settingsPane.add(credPane, BorderLayout.CENTER);

        return settingsPane;
    }

    @Override
    public String getLabelResource() {
        return "mailer_visualizer_title"; //$NON-NLS-1$
    }

    /**
     * Returns a String for the title of the attributes-panel as set up in the
     * properties-file using the lookup-constant "mailer_attributes_panel".
     *
     * @return The title of the component.
     */
    public String getAttributesTitle() {
        return JMeterUtils.getResString("mailer_attributes_panel"); //$NON-NLS-1$
    }

    // ////////////////////////////////////////////////////////////
    //
    // Implementation of the ActionListener-Interface.
    //
    // ////////////////////////////////////////////////////////////

    /**
     * Reacts on an ActionEvent (like pressing a button).
     *
     * @param e
     *            The ActionEvent with information about the event and its
     *            source.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == testerButton) {
            ResultCollector testElement = getModel();
            modifyTestElement(testElement);
            try {
                MailerModel model = ((MailerResultCollector) testElement).getMailerModel();
                model.sendTestMail();
                displayMessage(JMeterUtils.getResString("mail_sent"), false); //$NON-NLS-1$
            } catch (AddressException ex) {
                log.error("Invalid mail address ", ex);
                displayMessage(JMeterUtils.getResString("invalid_mail_address") //$NON-NLS-1$
                        + "\n" + ex.getMessage(), true); //$NON-NLS-1$
            } catch (MessagingException ex) {
                log.error("Couldn't send mail...", ex);
                displayMessage(JMeterUtils.getResString("invalid_mail") //$NON-NLS-1$
                        + "\n" + ex.getMessage(), true); //$NON-NLS-1$
            }
        }
    }

    // ////////////////////////////////////////////////////////////
    //
    // Methods used to store and retrieve the MailerVisualizer.
    //
    // ////////////////////////////////////////////////////////////

    /**
     * Restores MailerVisualizer.
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        updateVisualizer(((MailerResultCollector) el).getMailerModel());
    }

    /**
     * Makes MailerVisualizer storable.
     */
    @Override
    public TestElement createTestElement() {
        ResultCollector model = getModel();
        if (model == null) {
            model = new MailerResultCollector();
            setModel(model);
        }
        modifyTestElement(model);
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        MailerModel mailerModel = ((MailerResultCollector) c).getMailerModel();
        mailerModel.setFailureLimit(failureLimitField.getText());
        mailerModel.setFailureSubject(failureSubjectField.getText());
        mailerModel.setFromAddress(fromField.getText());
        mailerModel.setSmtpHost(smtpHostField.getText());
        mailerModel.setSmtpPort(smtpPortField.getText());
        mailerModel.setLogin(smtpLoginField.getText());
        mailerModel.setPassword(smtpPasswordField.getText());
        mailerModel.setMailAuthType(
                authTypeCombo.getSelectedItem().toString());
        mailerModel.setSuccessLimit(successLimitField.getText());
        mailerModel.setSuccessSubject(successSubjectField.getText());
        mailerModel.setToAddress(addressField.getText());
    }

    /**
     * Notifies this Visualizer about model-changes. Causes the Visualizer to
     * query the model about its new state.
     */
    private void updateVisualizer(MailerModel model) {
        addressField.setText(model.getToAddress());
        fromField.setText(model.getFromAddress());
        smtpHostField.setText(model.getSmtpHost());
        smtpPortField.setText(model.getSmtpPort());
        smtpLoginField.setText(model.getLogin());
        smtpPasswordField.setText(model.getPassword());
        authTypeCombo.setSelectedItem(model.getMailAuthType().toString());
        successSubjectField.setText(model.getSuccessSubject());
        failureSubjectField.setText(model.getFailureSubject());
        failureLimitField.setText(String.valueOf(model.getFailureLimit()));
        failureField.setText(String.valueOf(model.getFailureCount()));
        successLimitField.setText(String.valueOf(model.getSuccessLimit()));
        repaint();
    }

    /**
     * Shows a message using a DialogBox.
     */
    private void displayMessage(String message, boolean isError) {
        int type = isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(null, message, isError ?
                JMeterUtils.getResString("mailer_msg_title_error") :  // $NON-NLS-1$
                    JMeterUtils.getResString("mailer_msg_title_information"), type); // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof MailerModel) {
            MailerModel testModel = (MailerModel) e.getSource();
            updateVisualizer(testModel);
        } else {
            super.stateChanged(e);
        }
    }

}
