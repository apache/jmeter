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

package org.apache.jmeter.protocol.smtp.sampler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.jmeter.protocol.smtp.sampler.protocol.MailBodyProvider;
import org.apache.jmeter.protocol.smtp.sampler.protocol.SendMailCommand;
import org.apache.jmeter.protocol.smtp.sampler.tools.CounterOutputStream;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Sampler-Class for JMeter - builds, starts and interprets the results of the
 * sampler. Has to implement some standard-methods for JMeter in order to be
 * integrated in the framework. All getter/setter methods just deliver/set
 * values from/to the sampler, not from/to the message-object. Therefore, all
 * these methods are also present in class SendMailCommand.
 */
public class SmtpSampler extends AbstractSampler {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //+JMX file attribute names - do not change any values!
    public final static String SERVER               = "SMTPSampler.server"; // $NON-NLS-1$
    public final static String SERVER_PORT          = "SMTPSampler.serverPort"; // $NON-NLS-1$
    public final static String USE_AUTH             = "SMTPSampler.useAuth"; // $NON-NLS-1$
    public final static String USERNAME             = "SMTPSampler.username"; // $NON-NLS-1$
    public final static String PASSWORD             = "SMTPSampler.password"; // $NON-NLS-1$
    public final static String MAIL_FROM            = "SMTPSampler.mailFrom"; // $NON-NLS-1$
    public final static String RECEIVER_TO          = "SMTPSampler.receiverTo"; // $NON-NLS-1$
    public final static String RECEIVER_CC          = "SMTPSampler.receiverCC"; // $NON-NLS-1$
    public final static String RECEIVER_BCC         = "SMTPSampler.receiverBCC"; // $NON-NLS-1$

    public final static String SUBJECT              = "SMTPSampler.subject"; // $NON-NLS-1$
    public final static String MESSAGE              = "SMTPSampler.message"; // $NON-NLS-1$
    public final static String INCLUDE_TIMESTAMP    = "SMTPSampler.include_timestamp"; // $NON-NLS-1$
    public final static String ATTACH_FILE          = "SMTPSampler.attachFile"; // $NON-NLS-1$
    public final static String MESSAGE_SIZE_STATS   = "SMTPSampler.messageSizeStatistics"; // $NON-NLS-1$

    public final static String USE_SSL              = "SMTPSampler.useSSL"; // $NON-NLS-1$
    public final static String USE_STARTTLS         = "SMTPSampler.useStartTLS"; // $NON-NLS-1$
    public final static String SSL_TRUST_ALL_CERTS  = "SMTPSampler.trustAllCerts"; // $NON-NLS-1$
    public final static String ENFORCE_STARTTLS     = "SMTPSampler.enforceStartTLS"; // $NON-NLS-1$
    public final static String USE_LOCAL_TRUSTSTORE = "SMTPSampler.useLocalTrustStore"; // $NON-NLS-1$
    public final static String TRUSTSTORE_TO_USE    = "SMTPSampler.trustStoreToUse"; // $NON-NLS-1$
    public final static String USE_EML              = "SMTPSampler.use_eml"; // $NON-NLS-1$
    public final static String EML_MESSAGE_TO_SEND  = "SMTPSampler.emlMessageToSend"; // $NON-NLS-1$

    // Used to separate attachment file names in JMX fields - do not change!
    public static final String FILENAME_SEPARATOR = ";";
    //-JMX file attribute names


    public SmtpSampler() {
    }

    /**
     * Performs the sample, and returns the result
     *
     * @param e
     *            Standard-method-header from JMeter
     * @return sampleresult Result of the sample
     * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    public SampleResult sample(Entry e) {
        Message message = null;
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        boolean isOK = false; // Did sample succeed?
        SendMailCommand instance = new SendMailCommand();
        instance.setSmtpServer(getPropertyAsString(SmtpSampler.SERVER));
        instance.setSmtpPort(getPropertyAsString(SmtpSampler.SERVER_PORT));

        instance.setUseSSL(getPropertyAsBoolean(USE_SSL));
        instance.setUseStartTLS(getPropertyAsBoolean(USE_STARTTLS));
        instance.setTrustAllCerts(getPropertyAsBoolean(SSL_TRUST_ALL_CERTS));
        instance.setEnforceStartTLS(getPropertyAsBoolean(ENFORCE_STARTTLS));

        instance.setUseAuthentication(getPropertyAsBoolean(USE_AUTH));
        instance.setUsername(getPropertyAsString(USERNAME));
        instance.setPassword(getPropertyAsString(PASSWORD));

        instance.setUseLocalTrustStore(getPropertyAsBoolean(USE_LOCAL_TRUSTSTORE));
        instance.setTrustStoreToUse(getPropertyAsString(TRUSTSTORE_TO_USE));
        instance.setEmlMessage(getPropertyAsString(EML_MESSAGE_TO_SEND));
        instance.setUseEmlMessage(getPropertyAsBoolean(USE_EML));

        if (getMailFrom().matches(".*@.*")) {
            instance.setSender(getMailFrom());
        }

        try {
            if (!getPropertyAsBoolean(USE_EML)) { // part is only needed if we
                // don't send an .eml-file

                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                List<InternetAddress> receiversTo = new Vector<InternetAddress>();
                if (getPropertyAsString(SmtpSampler.RECEIVER_TO).matches(".*@.*")) {
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_TO))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversTo.add(new InternetAddress(strReceivers[i].trim()));
                    }
                    instance.setReceiverTo(receiversTo);
                }


                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                if (getPropertyAsString(SmtpSampler.RECEIVER_CC).matches(".*@.*")) {
                    List<InternetAddress> receiversCC = new Vector<InternetAddress>();
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_CC))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversCC.add(new InternetAddress(strReceivers[i].trim()));
                    }
                    instance.setReceiverCC(receiversCC);
                }

                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                if (getPropertyAsString(SmtpSampler.RECEIVER_BCC).matches(".*@.*")) {
                    List<InternetAddress> receiversBCC = new Vector<InternetAddress>();
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_BCC))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversBCC.add(new InternetAddress(strReceivers[i].trim()));
                    }
                    instance.setReceiverBCC(receiversBCC);
                }

                MailBodyProvider mb = new MailBodyProvider();
                if (getPropertyAsString(MESSAGE) != null
                        && !getPropertyAsString(MESSAGE).equals(""))
                    mb.setBody(getPropertyAsString(MESSAGE));
                instance.setMbProvider(mb);

                if (!getAttachments().equals("")) {
                    String[] attachments = getAttachments().split(FILENAME_SEPARATOR);
                    for (String attachment : attachments) {
                        instance.addAttachment(new File(attachment));
                    }
                }

                instance.setSubject(getPropertyAsString(SUBJECT)
                                + (getPropertyAsBoolean(INCLUDE_TIMESTAMP) ? " <<< current timestamp: "
                                        + new Date().getTime() + " >>>"
                                        : ""));
            } else {

                // send an .eml-file

                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                if (getPropertyAsString(SmtpSampler.RECEIVER_TO).matches(".*@.*")) {
                    List<InternetAddress> receiversTo = new Vector<InternetAddress>();
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_TO))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversTo.add(new InternetAddress(strReceivers[i].trim()));
                    }
                    instance.setReceiverTo(receiversTo);
                }

                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                if (getPropertyAsString(SmtpSampler.RECEIVER_CC).matches(".*@.*")) {
                    List<InternetAddress> receiversCC = new Vector<InternetAddress>();
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_CC))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversCC.add(new InternetAddress(strReceivers[i].trim()));
                    }
                    instance.setReceiverCC(receiversCC);
                }

                // check if there are really mail-addresses in the fields and if
                // there are multiple ones
                if (getPropertyAsString(SmtpSampler.RECEIVER_BCC).matches(
                        ".*@.*")) {
                    List<InternetAddress> receiversBCC = new Vector<InternetAddress>();
                    String[] strReceivers = (getPropertyAsString(SmtpSampler.RECEIVER_BCC))
                            .split(";");
                    for (int i = 0; i < strReceivers.length; i++) {
                        receiversBCC.add(new InternetAddress(strReceivers[i]
                                .trim()));
                    }
                    instance.setReceiverBCC(receiversBCC);
                }

                String subj = getPropertyAsString(SUBJECT);
                if (subj.trim().length() > 0) {
                    instance.setSubject(subj
                            + (getPropertyAsBoolean(INCLUDE_TIMESTAMP) ? " <<< current timestamp: "
                                    + new Date().getTime() + " >>>"
                                    : ""));
                }
            }
            // needed for measuring sending time
            instance.setSynchronousMode(true);

            message = instance.prepareMessage();

            if (getPropertyAsBoolean(MESSAGE_SIZE_STATS)) {
                // calculate message size
                CounterOutputStream cs = new CounterOutputStream();
                message.writeTo(cs);
                res.setBytes(cs.getCount());
            } else {
                res.setBytes(-1);
            }

        } catch (AddressException ex) {
            log.warn("Error while preparing message", ex);
            return res;
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (MessagingException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        // Perform the sampling
        res.sampleStart();

        try {
            instance.execute(message);

            // Set up the sample result details
            res.setSamplerData("To: "
                    + getPropertyAsString(SmtpSampler.RECEIVER_TO) + "\nCC: "
                    + getPropertyAsString(SmtpSampler.RECEIVER_CC) + "\nBCC: "
                    + getPropertyAsString(SmtpSampler.RECEIVER_BCC));
            res.setDataType(SampleResult.TEXT);
            res.setResponseCodeOK();
            /*
             * TODO if(instance.getSMTPStatusCode == 250)
             * res.setResponseMessage("Message successfully sent!"); else
             * res.setResponseMessage(instance.getSMTPStatusCodeIncludingMessage);
             */
            res.setResponseMessage("Message successfully sent!\n"
                    + instance.getServerResponse());
            isOK = true;
        }
        // username / password incorrect
        catch (AuthenticationFailedException afex) {
            log.warn("", afex);
            res.setResponseCode("500");
            res.setResponseMessage("AuthenticationFailedException: authentication failed - wrong username / password!\n"
                            + afex);
        }
        // SSL not supported, startTLS not supported, other messagingException
        catch (MessagingException mex) {
            log.warn("",mex);
            res.setResponseCode("500");
            if (mex.getMessage().matches(
                    ".*Could not connect to SMTP host.*465.*")
                    && mex.getCause().getMessage().matches(
                            ".*Connection timed out.*")) {
                res.setResponseMessage("MessagingException: Probably, SSL is not supported by the SMTP-Server!\n"
                                + mex);
            } else if (mex.getMessage().matches(".*StartTLS failed.*")) {
                res.setResponseMessage("MessagingException: StartTLS not supported by server or initializing failed!\n"
                                + mex);
            } else if (mex.getMessage().matches(".*send command to.*")
                    && mex.getCause().getMessage().matches(
                                    ".*unable to find valid certification path to requested target.*")) {
                res.setResponseMessage("MessagingException: Server certificate not trusted - perhaps you have to restart JMeter!\n"
                                + mex);
            } else {
                res.setResponseMessage("Other MessagingException: "
                        + mex.toString());
            }
        }
        // general exception
        catch (Exception ex) {
            res.setResponseCode("500");
            if (null != ex.getMessage()
                    && ex.getMessage().matches("Failed to build truststore")) {
                res.setResponseMessage("Failed to build truststore - did not try to send mail!");
            } else {
                res.setResponseMessage("Other Exception: " + ex.toString());
            }
        }

        res.sampleEnd();

        try {
            // process the sampler result
            InputStream is = message.getInputStream();
            StringBuffer sb = new StringBuffer();
            byte[] buf = new byte[1024];
            int read = is.read(buf);
            while (read > 0) {
                sb.append(new String(buf, 0, read));
                read = is.read(buf);
            }
            res.setResponseData(sb.toString().getBytes()); // TODO this should really be request data, but there is none
        } catch (IOException ex) {
            log.warn("",ex);
        } catch (MessagingException ex) {
            log.warn("",ex);
        }

        res.setSuccessful(isOK);

        return res;
    }

    /**
     * @return FQDN or IP of mailserver
     */
    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    /**
     * @return Mailserver-Port
     */
    public int getPort() {
        return getPropertyAsInt(SERVER_PORT);
    }

    /**
     * @return Sender's mail address
     */
    public String getMailFrom() {
        return getPropertyAsString(MAIL_FROM);
    }

    /**
     * @return Receiver in field "to"
     */
    public String getReceiverTo() {
        return getPropertyAsString(RECEIVER_TO);
    }

    /**
     * @return Receiver in field "cc"
     */
    public String getReceiverCC() {
        return getPropertyAsString(RECEIVER_CC);
    }

    /**
     * @return Receiver in field "bcc"
     */
    public String getReceiverBCC() {
        return getPropertyAsString(RECEIVER_BCC);
    }

    /**
     * @return Username for mailserver-login
     */
    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    /**
     * @return Password for mailserver-login
     */
    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    /**
     * @return Mail-subject
     */
    public String getSubject() {
        return this.getPropertyAsString(SUBJECT);
    }

    /**
     * @return true if timestamp is included in subject
     */
    public boolean getIncludeTimestamp() {
        return this.getPropertyAsBoolean(INCLUDE_TIMESTAMP);
    }

    /**
     * @return Path to file(s) to attach
     */
    public String getAttachments() {
        return this.getPropertyAsString(ATTACH_FILE);
    }

    /**
     * @return true if authentication is used to access mailserver
     */
    public boolean getUseAuthentication() {
        return this.getPropertyAsBoolean(USE_AUTH);
    }
}