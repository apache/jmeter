/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.reporters.MailerModel;
import org.apache.jmeter.reporters.MailerResultCollector;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*
 * TODO : - Create a subpanel for other visualizers - connect to the properties. -
 * Get the specific URL that is failing. - add a seperate interface to collect
 * the thrown failure messages. - - suggestions ;-)
 */

/**
 * This class implements a visualizer that mails a message when an error occurs.
 * 
 * @version $Revision$ $Date$
 */
public class MailerVisualizer extends AbstractVisualizer implements ActionListener, Clearable, ChangeListener {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private JButton testerButton;

	private JTextField addressField;

	private JTextField fromField;

	private JTextField smtpHostField;

	private JTextField failureSubjectField;

	private JTextField successSubjectField;

	private JTextField failureField;

	private JTextField failureLimitField;

	private JTextField successLimitField;

	// private JPanel mainPanel;
	// private JLabel panelTitleLabel;

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
	public synchronized void clear() {
		if (getModel() != null) {
			((MailerResultCollector) getModel()).getMailerModel().clear();
		}
	}

	public void add(SampleResult res) {
	}

	public String toString() {
		return "E-Mail Notification";
	}

	/**
	 * Initializes the GUI. Lays out components and adds them to the container.
	 */
	private void initGui() {
		this.setLayout(new BorderLayout());

		// MAIN PANEL
		JPanel mainPanel = new VerticalPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);

		this.setBorder(margin);

		// NAME
		mainPanel.add(makeTitlePanel());

		// mailer panel
		JPanel mailerPanel = new JPanel();

		mailerPanel.setBorder(BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(), getAttributesTitle()));
		GridBagLayout g = new GridBagLayout();

		mailerPanel.setLayout(g);
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		mailerPanel.add(new JLabel("From:"));

		fromField = new JTextField(25);
		fromField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(fromField, c);
		mailerPanel.add(fromField);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Addressee(s):"));

		addressField = new JTextField(25);
		addressField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(addressField, c);
		mailerPanel.add(addressField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("SMTP Host:"));

		smtpHostField = new JTextField(25);
		smtpHostField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(smtpHostField, c);
		mailerPanel.add(smtpHostField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Failure Subject:"));

		failureSubjectField = new JTextField(25);
		failureSubjectField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failureSubjectField, c);
		mailerPanel.add(failureSubjectField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Success Subject:"));

		successSubjectField = new JTextField(25);
		successSubjectField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(successSubjectField, c);
		mailerPanel.add(successSubjectField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Failure Limit:"));

		failureLimitField = new JTextField("2", 25);
		failureLimitField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failureLimitField, c);
		mailerPanel.add(failureLimitField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Success Limit:"));

		successLimitField = new JTextField("2", 25);
		successLimitField.setEditable(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(successLimitField, c);
		mailerPanel.add(successLimitField);

		testerButton = new JButton("Test Mail");
		testerButton.addActionListener(this);
		testerButton.setEnabled(true);
		c.gridwidth = 1;
		g.setConstraints(testerButton, c);
		mailerPanel.add(testerButton);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Failures:"));
		failureField = new JTextField(6);
		failureField.setEditable(false);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failureField, c);
		mailerPanel.add(failureField);

		mainPanel.add(mailerPanel);

		this.add(mainPanel, BorderLayout.WEST);
	}

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
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == testerButton) {
			try {
				MailerModel model = ((MailerResultCollector) getModel()).getMailerModel();
				model.sendTestMail();
				displayMessage(JMeterUtils.getResString("mail_sent"), false); //$NON-NLS-1$
			} catch (UnknownHostException e1) {
				log.error("Invalid Mail Server ", e1);
				displayMessage(JMeterUtils.getResString("invalid_mail_server"), true); //$NON-NLS-1$
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
	public void configure(TestElement el) {
		super.configure(el);
		updateVisualizer(((MailerResultCollector) el).getMailerModel());
	}

	/**
	 * Makes MailerVisualizer storable.
	 */
	public TestElement createTestElement() {
		if (getModel() == null) {
			setModel(new MailerResultCollector());
		}
		modifyTestElement(getModel());
		return getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement c) {
		super.modifyTestElement(c);
		MailerModel mailerModel = ((MailerResultCollector) c).getMailerModel();
		mailerModel.setFailureLimit(failureLimitField.getText());
		mailerModel.setFailureSubject(failureSubjectField.getText());
		mailerModel.setFromAddress(fromField.getText());
		mailerModel.setSmtpHost(smtpHostField.getText());
		mailerModel.setSuccessLimit(successLimitField.getText());
		mailerModel.setSuccessSubject(successSubjectField.getText());
		mailerModel.setToAddress(addressField.getText());
	}

	// ////////////////////////////////////////////////////////////
	//
	// Methods to implement the ModelListener.
	//
	// ////////////////////////////////////////////////////////////

	/**
	 * Notifies this Visualizer about model-changes. Causes the Visualizer to
	 * query the model about its new state.
	 */
	public void updateVisualizer(MailerModel model) {
		addressField.setText(model.getToAddress());
		fromField.setText(model.getFromAddress());
		smtpHostField.setText(model.getSmtpHost());
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
		int type = 0;

		if (isError) {
			type = JOptionPane.ERROR_MESSAGE;
		} else {
			type = JOptionPane.INFORMATION_MESSAGE;
		}
		JOptionPane.showMessageDialog(null, message, isError ? "Error" : "Information", type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof MailerModel) {
			MailerModel testModel = (MailerModel) e.getSource();
			updateVisualizer(testModel);
		} else {
			super.stateChanged(e);
		}
	}

}
