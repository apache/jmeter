/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.reporters;


import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;


/**
 * The model for a MailerVisualizer.
 *
 * @author     <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 * @version    $Revision$ $Date$
 */
public class MailerModel extends AbstractTestElement implements Serializable
{
    private long failureCount = 0;
    private long successCount = 0;
    private boolean failureMsgSent = false;
    private boolean siteDown = false;
    private boolean successMsgSent = false;

    private static final String FROM_KEY = "MailerModel.fromAddress";
    private static final String TO_KEY = "MailerModel.addressie";
    private static final String HOST_KEY = "MailerModel.smtpHost";
    private static final String SUCCESS_SUBJECT = "MailerModel.successSubject";
    private static final String FAILURE_SUBJECT = "MailerModel.failureSubject";
    private static final String FAILURE_LIMIT_KEY = "MailerModel.failureLimit";
    private static final String SUCCESS_LIMIT_KEY = "MailerModel.successLimit";

    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor(JMeterUtils.ELEMENTS);

    /** The listener for changes. */
    ChangeListener changeListener;

    /**
     * Constructs a MailerModel.
     */
    public MailerModel()
    {
        super();

        setProperty(
            SUCCESS_LIMIT_KEY,
            JMeterUtils.getPropDefault("mailer.successlimit", "2"));
        setProperty(
            FAILURE_LIMIT_KEY,
            JMeterUtils.getPropDefault("mailer.failurelimit", "2"));
    }
    
    public void addChangeListener(ChangeListener list)
    {
        changeListener = list;
    }
    
    public Object clone()
    {
        MailerModel m = (MailerModel)super.clone();
        m.changeListener = changeListener;
        return m;
    }

    /**
     * Returns wether there had been more failures than acceptable.
     *
     * @return a boolean value indicating whether the limit of acceptable
     *         failures has been reached.
     */
    public synchronized boolean isFailing()
    {
        return (failureCount > getFailureLimit());
    }
    
    public void notifyChangeListeners()
    {
        if(changeListener != null)
        {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Gets a Vector of String-objects. Each String is one mail-address
     * of the addresses-String set by <code>setToAddress(str)</code>.
     * The addresses must be seperated by commas. Only String-objects
     * containing a "@" are added to the returned Vector.
     *
     * @return a Vector of String-objects wherein each String represents a
     *         mail-address.
     */
    public synchronized Vector getAddressVector()
    {
        String theAddressie = getToAddress();
        Vector addressVector = new Vector();

        if (theAddressie != null)
        {
            String addressSep = ",";

            StringTokenizer next =
                new StringTokenizer(theAddressie, addressSep);

            while (next.hasMoreTokens())
            {
                String theToken = next.nextToken().trim();

                if (theToken.indexOf("@") > 0)
                {
                    addressVector.addElement(theToken);
                }
            }
        }
        else
        {
            return new Vector(0);
        }

        return addressVector;
    }

    /**
     * Adds a SampleResult. If SampleResult represents a change concerning
     * the failure/success of the sampling a message might be send to the
     * addressies according to the settings of <code>successCount</code>
     * and <code>failureCount</code>.
     *
     * @param sample the SampleResult encapsulating informations about the last
     *               sample.
     */
    public synchronized void add(SampleResult sample)
    {

        // -1 is the code for a failed sample.
        //
        if (!sample.isSuccessful())
        {
            failureCount++;
            successCount = 0;
        }
        else
        {
            successCount++;
        }

        if (this.isFailing() && !siteDown && !failureMsgSent)
        {
            // Send the mail ...
            Vector addressVector = getAddressVector();

            if (addressVector.size() != 0)
            {
                try
                {
                    sendMail(
                        getFromAddress(),
                        addressVector,
                        getFailureSubject(),
                        "URL Failed: " + sample.getSampleLabel(),
                        getSmtpHost());
                }
                catch (Exception e)
                {
                    log.error("Problem sending mail", e);
                }
                siteDown = true;
                failureMsgSent = true;
                successCount = 0;
                successMsgSent = false;
            }
        }

        if (siteDown && (sample.getTime() != -1) & !successMsgSent)
        {
            // Send the mail ...
            if (successCount > getSuccessLimit())
            {
                Vector addressVector = getAddressVector();

                try
                {
                    sendMail(
                        getFromAddress(),
                        addressVector,
                        getSuccessSubject(),
                        "URL Restarted: " + sample.getSampleLabel(),
                        getSmtpHost());
                }
                catch (Exception e)
                {
                    log.error("Problem sending mail", e);
                }
                siteDown = false;
                successMsgSent = true;
                failureCount = 0;
                failureMsgSent = false;
            }
        }

        if (successMsgSent && failureMsgSent)
        {
            clear();
        }
        notifyChangeListeners();
    }

    /**
     * Resets the state of this object to its default. But: This method does not
     * reset any mail-specific attributes (like sender, mail-subject...)
     * since they are independent of the sampling.
     */
    public synchronized void clear()
    {
        failureCount = 0;
        successCount = 0;
        siteDown = false;
        successMsgSent = false;
        failureMsgSent = false;
        notifyChangeListeners();
    }

    /**
     * Returns a String-representation of this object. Returns always
     * "E-Mail-Notification". Might be enhanced in future versions to return
     * some kind of String-representation of the mail-parameters (like
     * sender, addressies, smtpHost...).
     *
     * @return A String-representation of this object.
     */
    public String toString()
    {
        return "E-Mail Notification";
    }

    /**
     * Sends a mail with the given parameters using SMTP.
     *
     * @param  from     the sender of the mail as shown in the mail-client.
     * @param  vEmails  all receivers of the mail. The receivers are seperated
     *                  by commas.
     * @param  subject  the subject of the mail.
     * @param  attText  the message-body.
     * @param  smtpHost the smtp-server used to send the mail.
     */
    public synchronized void sendMail(
        String from,
        Vector vEmails,
        String subject,
        String attText,
        String smtpHost)
        throws UnknownHostException, AddressException, MessagingException
    {
        String host = smtpHost;
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
    }

    // ////////////////////////////////////////////////////////////
    //
    // setter/getter - JavaDoc-Comments not needed...
    //
    // ////////////////////////////////////////////////////////////

    public void setToAddress(String str)
    {
       setProperty(TO_KEY,str);
    }

    public void setFromAddress(String str)
    {
        setProperty(FROM_KEY,str);
    }

    public void setSmtpHost(String str)
    {
        setProperty(HOST_KEY,str);
    }

    public void setFailureSubject(String str)
    {
        setProperty(FAILURE_SUBJECT,str);
    }

    public void setSuccessSubject(String str)
    {
        setProperty(SUCCESS_SUBJECT,str);
    }

    public void setSuccessLimit(String limit)
    {
        setProperty(SUCCESS_LIMIT_KEY,limit);
    }

    private  void setSuccessCount(long count)
    {
        this.successCount = count;
    }

    public void setFailureLimit(String limit)
    {
        setProperty(FAILURE_LIMIT_KEY,limit);
    }

    private void setFailureCount(long count)
    {
        this.failureCount = count;
    }

    public String getToAddress()
    {
        return getPropertyAsString(TO_KEY);
    }

    public String getFromAddress()
    {
        return getPropertyAsString(FROM_KEY);
    }

    public String getSmtpHost()
    {
        return getPropertyAsString(HOST_KEY);
    }

    public String getFailureSubject()
    {
        return getPropertyAsString(FAILURE_SUBJECT);
    }

    public String getSuccessSubject()
    {
        return getPropertyAsString(SUCCESS_SUBJECT);
    }

    public long getSuccessLimit()
    {
        return getPropertyAsLong(SUCCESS_LIMIT_KEY);
    }

    public long getSuccessCount()
    {
        return successCount;
    }

    public long getFailureLimit()
    {
        return getPropertyAsLong(FAILURE_LIMIT_KEY);
    }

    public long getFailureCount()
    {
        return this.failureCount;
    }
}

