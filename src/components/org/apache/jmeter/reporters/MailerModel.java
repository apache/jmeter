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

package org.apache.jmeter.reporters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model for a MailerVisualizer.
 *
 */
public class MailerModel extends AbstractTestElement implements Serializable {
    public enum MailAuthType {
        SSL,
        TLS,
        NONE
    }

    private static final long serialVersionUID = 271L;

    private static final Logger log = LoggerFactory.getLogger(MailerModel.class);

    private static final String MAIL_SMTP_HOST = "mail.smtp.host"; //$NON-NLS-1$

    private static final String MAIL_SMTP_PORT = "mail.smtp.port"; //$NON-NLS-1$

    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth"; //$NON-NLS-1$

    private static final String MAIL_SMTP_SOCKETFACTORY_CLASS = "mail.smtp.socketFactory.class"; //$NON-NLS-1$

    private static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable"; //$NON-NLS-1$

    private long failureCount = 0;

    private long successCount = 0;

    private boolean failureMsgSent = false;

    private boolean siteDown = false;

    private boolean successMsgSent = false;

    private static final String FROM_KEY = "MailerModel.fromAddress"; //$NON-NLS-1$

    private static final String TO_KEY = "MailerModel.addressie"; //$NON-NLS-1$

    private static final String HOST_KEY = "MailerModel.smtpHost"; //$NON-NLS-1$

    private static final String PORT_KEY = "MailerModel.smtpPort"; //$NON-NLS-1$

    private static final String SUCCESS_SUBJECT = "MailerModel.successSubject"; //$NON-NLS-1$

    private static final String FAILURE_SUBJECT = "MailerModel.failureSubject"; //$NON-NLS-1$

    private static final String FAILURE_LIMIT_KEY = "MailerModel.failureLimit"; //$NON-NLS-1$

    private static final String SUCCESS_LIMIT_KEY = "MailerModel.successLimit"; //$NON-NLS-1$

    private static final String LOGIN = "MailerModel.login"; //$NON-NLS-1$

    private static final String PASSWORD = "MailerModel.password"; //$NON-NLS-1$ NOSONAR no hardcoded password

    private static final String MAIL_AUTH_TYPE = "MailerModel.authType"; //$NON-NLS-1$

    private static final String DEFAULT_LIMIT = "2"; //$NON-NLS-1$

    private static final String DEFAULT_SMTP_PORT = "25";

    private static final String DEFAULT_PASSWORD_VALUE = ""; //$NON-NLS-1$ NOSONAR no hardcoded password

    private static final String DEFAULT_MAIL_AUTH_TYPE_VALUE = MailAuthType.NONE.toString(); //$NON-NLS-1$

    private static final String DEFAULT_LOGIN_VALUE = ""; //$NON-NLS-1$

    /** The listener for changes. */
    private transient ChangeListener changeListener;

    /**
     * Constructs a MailerModel.
     */
    public MailerModel() {
        super();

        setProperty(SUCCESS_LIMIT_KEY, JMeterUtils.getPropDefault("mailer.successlimit", DEFAULT_LIMIT)); //$NON-NLS-1$
        setProperty(FAILURE_LIMIT_KEY, JMeterUtils.getPropDefault("mailer.failurelimit", DEFAULT_LIMIT)); //$NON-NLS-1$
    }

    public void addChangeListener(ChangeListener list) {
        changeListener = list;
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        MailerModel m = (MailerModel) super.clone();
        m.changeListener = changeListener;
        return m;
    }

    public void notifyChangeListeners() {
        if (changeListener != null) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Gets a List of String-objects. Each String is one mail-address of the
     * addresses-String set by <code>setToAddress(str)</code>. The addresses
     * must be separated by commas. Only String-objects containing a "@" are
     * added to the returned List.
     *
     * @return a List of String-objects wherein each String represents a
     *         mail-address.
     */
    public List<String> getAddressList() {
        String addressees = getToAddress();
        List<String> addressList = new ArrayList<>();

        if (addressees != null) {

            StringTokenizer next = new StringTokenizer(addressees, ","); //$NON-NLS-1$

            while (next.hasMoreTokens()) {
                String theToken = next.nextToken().trim();

                if (theToken.indexOf('@') > 0) { //NOSONAR $NON-NLS-1$ 
                    addressList.add(theToken);
                } else {
                    log.warn("Ignored unexpected e-mail address: {}", theToken);
                }
            }
        }

        return addressList;
    }

    /**
     * Adds a SampleResult for display in the Visualizer.
     *
     * @param sample
     *            the SampleResult encapsulating information about the last sample.
     */
    public void add(SampleResult sample) {
        add(sample, false);
    }

    /**
     * Adds a SampleResult. If SampleResult represents a change concerning the
     * failure/success of the sampling a message might be sent to the addressies
     * according to the settings of <code>successCount</code> and
     * <code>failureCount</code>.
     *
     * @param sample
     *            the SampleResult encapsulating information about the last
     *            sample.
     * @param sendMails whether or not to send e-mails
     */
    public synchronized void add(SampleResult sample, boolean sendMails) {

        // -1 is the code for a failed sample.
        //
        if (!sample.isSuccessful()) {
            failureCount++;
            successCount = 0;
        } else {
            successCount++;
        }

        if (sendMails && (failureCount > getFailureLimit()) && !siteDown && !failureMsgSent) {
            // Send the mail ...
            List<String> addressList = getAddressList();

            if (!addressList.isEmpty()) {
                try {
                    sendMail(getFromAddress(), addressList, getFailureSubject(), "URL Failed: "
                            + sample.getSampleLabel(), getSmtpHost(),
                            getSmtpPort(), getLogin(), getPassword(),
                            getMailAuthType(), false);
                } catch (Exception e) {
                    log.error("Problem sending mail: "+e);
                }
                siteDown = true;
                failureMsgSent = true;
                successCount = 0;
                successMsgSent = false;
            }
        }

        if (sendMails && siteDown && (sample.getTime() != -1) && !successMsgSent && successCount > getSuccessLimit()) {
            List<String> addressList = getAddressList();
            try {
                sendMail(getFromAddress(), addressList, getSuccessSubject(), "URL Restarted: "
                        + sample.getSampleLabel(), getSmtpHost(),
                        getSmtpPort(), getLogin(), getPassword(),
                        getMailAuthType(), false);
            } catch (Exception e) {
                log.error("Problem sending mail", e);
            }
            siteDown = false;
            successMsgSent = true;
            failureCount = 0;
            failureMsgSent = false;
        }

        if (successMsgSent && failureMsgSent) {
            clear();
        }
        notifyChangeListeners();
    }

    /**
     * Resets the state of this object to its default. But: This method does not
     * reset any mail-specific attributes (like sender, mail-subject...) since
     * they are independent of the sampling.
     */
    @Override
    public synchronized void clear() {
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
     * some kind of String-representation of the mail-parameters (like sender,
     * addressies, smtpHost...).
     *
     * @return A String-representation of this object.
     */
    @Override
    public String toString() {
        return "E-Mail Notification";
    }

    /**
     * Sends a mail with the given parameters using SMTP.
     *
     * @param from
     *            the sender of the mail as shown in the mail-client.
     * @param vEmails
     *            all receivers of the mail. The receivers are seperated by
     *            commas.
     * @param subject
     *            the subject of the mail.
     * @param attText
     *            the message-body.
     * @param smtpHost
     *            the smtp-server used to send the mail.
     * @throws MessagingException
     *             if the building of the message fails
     * @throws AddressException
     *             if any of the addresses is wrong
     */
    public void sendMail(String from, List<String> vEmails, String subject, String attText, String smtpHost)
            throws MessagingException {
        sendMail(from, vEmails, subject, attText, smtpHost, DEFAULT_SMTP_PORT, null, null, null, false);
    }

    /**
     * Sends a mail with the given parameters using SMTP.
     *
     * @param from
     *            the sender of the mail as shown in the mail-client.
     * @param vEmails
     *            all receivers of the mail. The receivers are seperated by
     *            commas.
     * @param subject
     *            the subject of the mail.
     * @param attText
     *            the message-body.
     * @param smtpHost
     *            the smtp-server used to send the mail.
     * @param smtpPort the smtp-server port used to send the mail.
     * @param user the login used to authenticate
     * @param password the password used to authenticate
     * @param mailAuthType {@link MailAuthType} Security policy
     * @param debug Flag whether debug messages for the mail session should be generated
     * @throws AddressException If mail address is wrong
     * @throws MessagingException If building MimeMessage fails
     */
    public void sendMail(String from, List<String> vEmails, String subject,
            String attText, String smtpHost,
            String smtpPort,
            final String user,
            final String password,
            MailAuthType mailAuthType,
            boolean debug)
            throws MessagingException{

        InternetAddress[] address = new InternetAddress[vEmails.size()];

        for (int k = 0; k < vEmails.size(); k++) {
            address[k] = new InternetAddress(vEmails.get(k));
        }

        // create some properties and get the default Session
        Properties props = new Properties();

        props.put(MAIL_SMTP_HOST, smtpHost);
        props.put(MAIL_SMTP_PORT, smtpPort); // property values are strings
        Authenticator authenticator = null;
        if(mailAuthType != MailAuthType.NONE) {
            props.put(MAIL_SMTP_AUTH, "true");
            switch (mailAuthType) {
                case SSL:
                    props.put(MAIL_SMTP_SOCKETFACTORY_CLASS,
                            "javax.net.ssl.SSLSocketFactory");
                    break;
                case TLS:
                    props.put(MAIL_SMTP_STARTTLS,
                            "true");
                    break;

                default:
                    break;
                }
        }

        if(!StringUtils.isEmpty(user)) {
            authenticator =
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user,password);
                        }
                    };
        }
        Session session = Session.getInstance(props, authenticator);
        session.setDebug(debug);

        // create a message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);
        msg.setText(attText);
        Transport.send(msg);
    }

    /**
     * Send a Test Mail to check configuration
     * @throws AddressException If mail address is wrong
     * @throws MessagingException If building MimeMessage fails
     */
    public synchronized void sendTestMail() throws MessagingException {
        String to = getToAddress();
        String from = getFromAddress();
        String subject = "Testing mail-addresses";
        String smtpHost = getSmtpHost();
        String attText = "JMeter-Testmail" + "\n" + "To:  " + to + "\n" + "From: " + from + "\n" + "Via:  " + smtpHost
                + "\n" + "Fail Subject:  " + getFailureSubject() + "\n" + "Success Subject:  " + getSuccessSubject();

        log.info(attText);

        sendMail(from, getAddressList(), subject, attText, smtpHost,
                getSmtpPort(),
                getLogin(),
                getPassword(),
                getMailAuthType(),
                true);
        log.info("Test mail sent successfully!!");
    }

    // ////////////////////////////////////////////////////////////
    //
    // setter/getter - JavaDoc-Comments not needed...
    //
    // ////////////////////////////////////////////////////////////

    public void setToAddress(String str) {
        setProperty(TO_KEY, str);
    }

    public void setFromAddress(String str) {
        setProperty(FROM_KEY, str);
    }

    public void setSmtpHost(String str) {
        setProperty(HOST_KEY, str);
    }

    public void setSmtpPort(String value) {
        if(StringUtils.isEmpty(value)) {
            value = DEFAULT_SMTP_PORT;
        }
        setProperty(PORT_KEY, value, DEFAULT_SMTP_PORT);
    }

    public void setLogin(String login) {
        setProperty(LOGIN, login, DEFAULT_LOGIN_VALUE);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password, DEFAULT_PASSWORD_VALUE);
    }

    public void setMailAuthType(String value) {
        setProperty(MAIL_AUTH_TYPE, value, DEFAULT_MAIL_AUTH_TYPE_VALUE);
    }

    public void setFailureSubject(String str) {
        setProperty(FAILURE_SUBJECT, str);
    }

    public void setSuccessSubject(String str) {
        setProperty(SUCCESS_SUBJECT, str);
    }

    public void setSuccessLimit(String limit) {
        setProperty(SUCCESS_LIMIT_KEY, limit);
    }

    public void setFailureLimit(String limit) {
        setProperty(FAILURE_LIMIT_KEY, limit);
    }

    public String getToAddress() {
        return getPropertyAsString(TO_KEY);
    }

    public String getFromAddress() {
        return getPropertyAsString(FROM_KEY);
    }

    public String getSmtpHost() {
        return getPropertyAsString(HOST_KEY);
    }

    public String getSmtpPort() {
        return getPropertyAsString(PORT_KEY, DEFAULT_SMTP_PORT);
    }

    public String getFailureSubject() {
        return getPropertyAsString(FAILURE_SUBJECT);
    }

    public String getSuccessSubject() {
        return getPropertyAsString(SUCCESS_SUBJECT);
    }

    public long getSuccessLimit() {
        return getPropertyAsLong(SUCCESS_LIMIT_KEY);
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureLimit() {
        return getPropertyAsLong(FAILURE_LIMIT_KEY);
    }

    public long getFailureCount() {
        return this.failureCount;
    }

    public String getLogin() {
        return getPropertyAsString(LOGIN, DEFAULT_LOGIN_VALUE);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD, DEFAULT_PASSWORD_VALUE);
    }

    public MailAuthType getMailAuthType() {
        String authType = getPropertyAsString(MAIL_AUTH_TYPE, DEFAULT_MAIL_AUTH_TYPE_VALUE);
        return MailAuthType.valueOf(authType);
    }
}
