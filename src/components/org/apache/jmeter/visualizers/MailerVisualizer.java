/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 package org.apache.jmeter.visualizers;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import java.net.*;
import javax.mail.*;
import javax.mail.internet.*;
//import javax.activation.* ;
import java.util.Properties;
import org.apache.jmeter.util.*;
import org.apache.jmeter.gui.util.*;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

/*
 * TODO :
 * - Create a subpanel for other visualizers
 * - connect to the properties.
 * - Get the specific URL that is failing.
 * - add a seperate interface to collect the thrown failure messages.
 * -
 * - suggestions ;-)
 */

/************************************************************
 *  This class implements a visualizer that mails a message when an error
 *  occurs.
 *
 *@author     <a href="mailto:stuart@personalmd.com">Stuart Schmukler</a> and <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 *@created    $Date$
 *@version    $Revision$ $Date$
 ***********************************************************/
//public class MailerVisualizer extends JPanel implements Visualizer, ActionListener
public class MailerVisualizer extends AbstractVisualizer 
		implements ActionListener, FocusListener, Clearable {

	private String addressie;
	private String from;
	private String smtpHost;
	private String failsubject;
	private String successsubject;
	private long failureCount = 0;
	private long successCount = 0;
	private long failureLimit = 2;
	private long successLimit = 2;
	private boolean failureMsgSent = false;
	private boolean siteDown = false;
	private boolean successMsgSent = false;

	private Properties appProperties;

	private JButton testerButton;
	private JTextField addressField;
	private JTextField fromField;
	private JTextField smtpHostField;
	private JTextField failsubjectField;
	private JTextField successsubjectField;
	private JTextField failureField;
	private JTextField failureLimitField;
	private JTextField successLimitField;

	private JPanel mainPanel;
	private JLabel panelTitleLabel;


	//-----------

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public MailerVisualizer() {
		super();

		// Properties connection.
		this.appProperties = JMeterUtils.getJMeterProperties();

		// retrieve successLimit from properties
		try {
			successLimit =	Long.parseLong(appProperties.getProperty("mailer.successlimit"));
		}
		catch (Exception ex) {
			// Ignore any garbage
		}

		// retrieve failureLimit from properties
		try {
			failureLimit =	Long.parseLong(appProperties.getProperty("mailer.failurelimit"));
		}
		catch (Exception ex) {
			// Ignore any garbage
		}

		// initialize GUI.
		initGui();
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public synchronized boolean isFailing()
	{
		return (failureCount > failureLimit);
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public JPanel getControlPanel()
	{
		return this;
	}


	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  theAddressie  !ToDo (Parameter description)
	 *@return               !ToDo (Return description)
	 ***********************************************************/
	public synchronized Vector newAddressVector(String theAddressie)
	{
		Vector addressVector = new Vector();
		if (theAddressie != null) {
			String addressSep = ", ";
	
			StringTokenizer next = new StringTokenizer(theAddressie, addressSep);
	
			while (next.hasMoreTokens())
			{
				String theToken = next.nextToken();
	
				if (theToken.indexOf("@") > 0)
				{
					addressVector.addElement(theToken);
				}
			}
		}
		else {
			return new Vector(0);
		}

		return addressVector;
	}


	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  sample  !ToDo (Parameter description)
	 ***********************************************************/
	public synchronized void add(SampleResult sample)
	{

		// -1 is the code for a failed sample.
		//
		if (!sample.isSuccessful())
		{
			failureCount++;
		}
		else
		{
			successCount++;
		}

		if (this.isFailing() && !siteDown && !failureMsgSent)
		{
			// Display ...
			failureField.setText(Long.toString(failureCount));
			repaint();

			// Send the mail ...
			Vector addressVector = newAddressVector(addressie);
			if (addressVector.size() != 0) {
				sendMail(from, addressVector, failsubject, "URL Failed: " +
						sample.getSampleLabel(), smtpHost);
				siteDown = true;
				failureMsgSent = true;
				successCount = 0;
			}
		}

		if (siteDown && (sample.getTime() != -1) & !successMsgSent)
		{
			// Display ...
			failureField.setText(Long.toString(failureCount));
			repaint();

			// Send the mail ...
			if (successCount > successLimit)
			{
				Vector addressVector = newAddressVector(addressie);
				sendMail(from, addressVector, successsubject, "URL Restarted: " +
						sample.getSampleLabel(), smtpHost);
				siteDown = false;
				successMsgSent = true;
			}
		}

		if (successMsgSent && failureMsgSent)
		{
			clear();
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 ***********************************************************/
	public synchronized void clear()
	{
		failureCount = 0;
		successCount = 0;
		siteDown = false;
		successMsgSent = false;
		failureMsgSent = false;

		failureField.setText(Long.toString(failureCount));
		repaint();
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public String toString()
	{
		return "E-Mail Notification";
	}

	//-----------
	//function to send a mail to list mailaddresses

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  from      !ToDo (Parameter description)
	 *@param  vEmails   !ToDo (Parameter description)
	 *@param  subject   !ToDo (Parameter description)
	 *@param  attText   !ToDo (Parameter description)
	 *@param  SMTPHost  !ToDo (Parameter description)
	 ***********************************************************/
	public static synchronized void sendMail(String from,
			Vector vEmails,
			String subject,
			String attText,
			String SMTPHost)
	{
		try
		{
			String host = SMTPHost;
			boolean debug = Boolean.valueOf(host).booleanValue();
			InetAddress remote = InetAddress.getByName(host);

			InternetAddress[] address = new InternetAddress[vEmails.size()];
			for (int k = 0; k < vEmails.size(); k++)
			{
				address[k] = new InternetAddress(vEmails.elementAt(k).toString());
			}

			// create some properties and get the default Session
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(debug);

			// create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(subject);
			msg.setText(attText);
			Transport.send(msg);
			System.out.println("Mail sent successfully!!");
		}
		catch (UnknownHostException e1)
		{
			System.out.println("NxError:Invalid Mail Server " + e1);
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Initializes the GUI. Lays out components and adds them to the
	 * container.
	 */
	private void initGui() {
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(getStaticLabel());
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());

		// mailer panel
		JPanel mailerPanel = new JPanel();
		mailerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getAttributesTitle()));
		GridBagLayout g = new GridBagLayout();
		mailerPanel.setLayout(g);
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		mailerPanel.add(new JLabel("From:"));

		from = appProperties.getProperty("mailer.from");
		fromField = new JTextField(from, 25);
		fromField.setEditable(true);
		fromField.addActionListener(this);
		fromField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(fromField, c);
		mailerPanel.add(fromField);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Addressie(s):"));

		addressie = appProperties.getProperty("mailer.addressies");
		addressField = new JTextField(addressie, 25);
		addressField.setEditable(true);
		addressField.addActionListener(this);
		addressField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(addressField, c);
		mailerPanel.add(addressField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("SMTP Host:"));

		smtpHost = appProperties.getProperty("mailer.smtphost");
		smtpHostField = new JTextField(smtpHost, 25);
		smtpHostField.setEditable(true);
		smtpHostField.addActionListener(this);
		smtpHostField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(smtpHostField, c);
		mailerPanel.add(smtpHostField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Failure Subject:"));

		failsubject = appProperties.getProperty("mailer.failsubject");
		failsubjectField = new JTextField(failsubject, 25);
		failsubjectField.setEditable(true);
		failsubjectField.addActionListener(this);
		failsubjectField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failsubjectField, c);
		mailerPanel.add(failsubjectField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Success Subject:"));

		successsubject = appProperties.getProperty("mailer.successsubject");
		successsubjectField = new JTextField(successsubject, 25);
		successsubjectField.setEditable(true);
		successsubjectField.addActionListener(this);
		successsubjectField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(successsubjectField, c);
		mailerPanel.add(successsubjectField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Failure Limit:"));

		failureLimitField = new JTextField(Long.toString(failureLimit), 6);
		failureLimitField.setEditable(true);
		failureLimitField.addActionListener(this);
		failureLimitField.addFocusListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failureLimitField, c);
		mailerPanel.add(failureLimitField);

		c.gridwidth = 1;
		mailerPanel.add(new JLabel("Success Limit:"));

		successLimitField = new JTextField(Long.toString(successLimit), 6);
		successLimitField.setEditable(true);
		successLimitField.addActionListener(this);
		successLimitField.addFocusListener(this);
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
		failureField.addActionListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		g.setConstraints(failureField, c);
		mailerPanel.add(failureField);

		mainPanel.add(mailerPanel, BorderLayout.CENTER);

		this.add(mainPanel);
	}

	/**
	 * Returns a String for the title of the component
	 * as set up in the properties-file using the lookup-constant
	 * "mailer_visualizer_title".
	 *
	 *@return  The title of the component.
	 */
	public String getStaticLabel()
	{
		// should be something like this:
		//	return JMeterUtils.getResString("mailer_visualizer_title");
		return "Mailer Visualizer";
	}

	/**
	 * Returns a String for the title of the attributes-panel
	 * as set up in the properties-file using the lookup-constant
	 * "mailer_attributes_panel".
	 *
	 *@return  The title of the component.
	 */
	public String getAttributesTitle()
	{
		// should be something like this:
		//	return JMeterUtils.getResString("mailer_attributes_panel")
		return "Mailing attributes";
	}
	/**
	* Method used to log a String.
	*
	* @param str String to be logged.
	*/
	private static void log(String str) {
		// this is just a tewmporary solution
		// will be replaced by a call to the logging-API
		// of the commons-project.
		System.out.println("MailerVisualizer - " + str);
	}


	/**
	 * Does the actual EventHandling. Gets called by EventHandlers
	 * for either ActionEvents or FocusEvents.
	 *
	 * @param source The object that caused the event.
	 */
	private void doEventHandling(Object source) {
		if (source == addressField) {
			this.addressie = this.addressField.getText();
			log("AddressField=" + addressField.getText());
		}
		else if (source == fromField) {
			this.from = this.fromField.getText();
			log("FromField=" + fromField.getText());
		}
		else if (source == smtpHostField) {
			this.smtpHost = this.smtpHostField.getText();
			log("smtpHostField=" + smtpHostField.getText());
		}
		else if (source == failsubjectField) {
			this.failsubject = this.failsubjectField.getText();
			log("failsubjectField=" + failsubjectField.getText());
		}
		else if (source == successsubjectField) {
			this.successsubject = this.successsubjectField.getText();
			log("successsubjectField=" + successsubjectField.getText());
		}
		else if (source == failureLimitField) {
			this.failureLimit = Long.parseLong(this.failureLimitField.getText());
			log("failureLimitField=" + failureLimitField.getText());
		}
		else if (source == successLimitField) {
			this.successLimit = Long.parseLong(this.successLimitField.getText());
			log("successLimitField=" + successLimitField.getText());
		}
	}


	//////////////////////////////////////////////////////////////
	//
	// Implementation of the ActionListener-Interface.
	//
	//////////////////////////////////////////////////////////////

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  e  !ToDo (Parameter description)
	 ***********************************************************/
	public void actionPerformed(ActionEvent e) {
		try {
			JComponent c = (JComponent)e.getSource();
			if (c == testerButton) {
				log("### Test To:  " + this.addressie + ", " +
						"Via:  " + this.smtpHost + ", " +
						"Fail Subject:  " + this.failsubject + ", " +
						"Success Subject:  " + this.successsubject);

				String testMessage = ("### Test To:  " + this.addressie + ", " +
						"Via:  " + this.smtpHost + ", " +
						"Fail Subject:  " + this.failsubject + ", " +
						"Success Subject:  " + this.successsubject);

				Vector addressVector = newAddressVector(addressie);
				sendMail(from, addressVector, "Testing addressies", testMessage, smtpHost);
			}
			else {
				doEventHandling(c);
			}
		}
		catch (Exception ex)	{
			JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	//////////////////////////////////////////////////////////////
	//
	// Implementation of the FocusListener-Interface.
	//
	//////////////////////////////////////////////////////////////

	/**
	 * Empty implementation of the FocusListener-method.
	 */
	public void focusGained(FocusEvent e) {
	}

	/**
	 * Called every time a element looses its focus. Here used to determine
	 * wether the text inside a JTextField has changed and wether
	 * the field has changed.
	 *
	 * @param e The FocusEvent-object encapsulating all relevant informations
	 * about the FocusEvent.
	 */
	public void focusLost(FocusEvent e) {
		Object source = e.getSource();
		doEventHandling(source);
	}

}

// new ressource_strings:
// mailer_visualizer_title - used as the title of this Visualizer
// mailer_attributes_panel - used as the title of the JPanel (EtchedBorder - title) where the attributes are entered

