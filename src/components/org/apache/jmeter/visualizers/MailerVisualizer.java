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


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.util.Vector;

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
 * TODO :
 * - Create a subpanel for other visualizers
 * - connect to the properties.
 * - Get the specific URL that is failing.
 * - add a seperate interface to collect the thrown failure messages.
 * -
 * - suggestions ;-)
 */

/**
 * This class implements a visualizer that mails a message when an error
 * occurs.
 *
 * @author <a href="mailto:stuart@personalmd.com">Stuart Schmukler</a>
 * @author <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 * @version    $Revision$ $Date$
 */
public class MailerVisualizer extends AbstractVisualizer
        implements ActionListener, Clearable, ChangeListener
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    private JButton testerButton;
    private JTextField addressField;
    private JTextField fromField;
    private JTextField smtpHostField;
    private JTextField failureSubjectField;
    private JTextField successSubjectField;
    private JTextField failureField;
    private JTextField failureLimitField;
    private JTextField successLimitField;

    //private JPanel mainPanel;
    //private JLabel panelTitleLabel;


    /**
     * Constructs the MailerVisualizer and initializes its GUI.
     */
    public MailerVisualizer()
    {
        super();

        // initialize GUI.
        initGui();
    }

    public JPanel getControlPanel()
    {
        return this;
    }

    /**
     * Clears any stored sampling-informations.
     */
    public synchronized void clear()
    {
        if(getModel() != null)
        {
            ((MailerResultCollector)getModel()).getMailerModel().clear();
        }
    }
    
    public void add(SampleResult res)
    {
    }

    public String toString()
    {
        return "E-Mail Notification";
    }

    /**
     * Initializes the GUI. Lays out components and adds them to the
     * container.
     */
    private void initGui()
    {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new VerticalPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        this.setBorder(margin);

        // NAME
        mainPanel.add(makeTitlePanel());

        // mailer panel
        JPanel mailerPanel = new JPanel();

        mailerPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                getAttributesTitle()));
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

        failureLimitField = new JTextField("2",25);
        failureLimitField.setEditable(true);
        c.gridwidth = GridBagConstraints.REMAINDER;
        g.setConstraints(failureLimitField, c);
        mailerPanel.add(failureLimitField);

        c.gridwidth = 1;
        mailerPanel.add(new JLabel("Success Limit:"));

        successLimitField = new JTextField("2",25);
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

        this.add(mainPanel,BorderLayout.WEST);
    }

    /**
     * Returns a String for the title of the component
     * as set up in the properties-file using the lookup-constant
     * "mailer_visualizer_title".
     *
     * @return  The title of the component.
     */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("mailer_visualizer_title");
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
        return JMeterUtils.getResString("mailer_attributes_panel");
    }

    // ////////////////////////////////////////////////////////////
    //
    // Implementation of the ActionListener-Interface.
    //
    // ////////////////////////////////////////////////////////////

    /**
     * Reacts on an ActionEvent (like pressing a button).
     *
     * @param e The ActionEvent with information about the event and its source.
     */
    public void actionPerformed(ActionEvent e)
    {
		if (e.getSource() == testerButton)
		{
			try
			{
				MailerModel model=((MailerResultCollector)getModel()).getMailerModel();

				String to= addressField.getText();
				String from= fromField.getText();
				String via= smtpHostField.getText();
				String fail= failureSubjectField.getText();
				String success= successSubjectField.getText();
				
				String testString = "JMeter-Testmail" + "\n" 
						+ "To:  " + to + "\n"
                        + "Via:  " + via + "\n"
						+ "Fail Subject:  " + fail + "\n"
                        + "Success Subject:  " + success;

                log.debug(testString);
                Vector destination= new Vector();
                destination.add(to);
                model.sendMail(from, destination, "Testing mail-addresses", testString, via);
                log.info("Mail sent successfully!!");
			}
			catch (UnknownHostException e1)
			{
				log.error("Invalid Mail Server ", e1);
				displayMessage(JMeterUtils.getResString("invalid_mail_server"), true);
			}
			catch (Exception ex)
			{
				log.error("Couldn't send mail...", ex);
				displayMessage(JMeterUtils.getResString("invalid_mail_server"), true);
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
    public void configure(TestElement el)
    {
        super.configure(el);
        updateVisualizer(((MailerResultCollector)el).getMailerModel());
    }

    /**
     * Makes MailerVisualizer storable.
     */
    public TestElement createTestElement()
    {
        if (getModel() == null)
        {
            setModel( new MailerResultCollector());
        }
        modifyTestElement(getModel());
        return getModel();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement c)
    {
        super.modifyTestElement(c);
        MailerModel mailerModel = ((MailerResultCollector)c).getMailerModel();
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
    public void updateVisualizer(MailerModel model)
    {
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
    public void displayMessage(String message, boolean isError)
    {
        int type = 0;

        if (isError)
        {
            type = JOptionPane.ERROR_MESSAGE;
        }
        else
        {
            type = JOptionPane.INFORMATION_MESSAGE;
        }
        JOptionPane.showMessageDialog(null, message, "Error", type);
    }

    /* (non-Javadoc)
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if(e.getSource() instanceof MailerModel)
        {
            MailerModel testModel = (MailerModel)e.getSource();
            updateVisualizer(testModel);
        }
        else
        {
            super.stateChanged(e);
        }
    }

}

