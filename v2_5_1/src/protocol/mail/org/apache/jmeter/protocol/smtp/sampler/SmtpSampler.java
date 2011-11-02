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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;

import org.apache.jmeter.protocol.smtp.sampler.gui.SecuritySettingsPanel;
import org.apache.jmeter.protocol.smtp.sampler.protocol.SendMailCommand;
import org.apache.jmeter.protocol.smtp.sampler.tools.CounterOutputStream;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.CollectionProperty;
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
    public static final String MAIL_REPLYTO         = "SMTPSampler.replyTo"; // $NON-NLS-1$
    public final static String RECEIVER_TO          = "SMTPSampler.receiverTo"; // $NON-NLS-1$
    public final static String RECEIVER_CC          = "SMTPSampler.receiverCC"; // $NON-NLS-1$
    public final static String RECEIVER_BCC         = "SMTPSampler.receiverBCC"; // $NON-NLS-1$

    public final static String SUBJECT              = "SMTPSampler.subject"; // $NON-NLS-1$
    public final static String SUPPRESS_SUBJECT     = "SMTPSampler.suppressSubject"; // $NON-NLS-1$
    public final static String MESSAGE              = "SMTPSampler.message"; // $NON-NLS-1$
    public final static String PLAIN_BODY           = "SMTPSampler.plainBody"; // $NON-NLS-1$
    public final static String INCLUDE_TIMESTAMP    = "SMTPSampler.include_timestamp"; // $NON-NLS-1$
    public final static String ATTACH_FILE          = "SMTPSampler.attachFile"; // $NON-NLS-1$
    public final static String MESSAGE_SIZE_STATS   = "SMTPSampler.messageSizeStatistics"; // $NON-NLS-1$
    public static final String HEADER_FIELDS        = "SMTPSampler.headerFields"; // $NON-NLS-1$

    public final static String USE_EML              = "SMTPSampler.use_eml"; // $NON-NLS-1$
    public final static String EML_MESSAGE_TO_SEND  = "SMTPSampler.emlMessageToSend"; // $NON-NLS-1$
    public static final String ENABLE_DEBUG         = "SMTPSampler.enableDebug"; // $NON-NLS-1$

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

        instance.setUseSSL(getPropertyAsBoolean(SecuritySettingsPanel.USE_SSL));
        instance.setUseStartTLS(getPropertyAsBoolean(SecuritySettingsPanel.USE_STARTTLS));
        instance.setTrustAllCerts(getPropertyAsBoolean(SecuritySettingsPanel.SSL_TRUST_ALL_CERTS));
        instance.setEnforceStartTLS(getPropertyAsBoolean(SecuritySettingsPanel.ENFORCE_STARTTLS));

        instance.setUseAuthentication(getPropertyAsBoolean(USE_AUTH));
        instance.setUsername(getPropertyAsString(USERNAME));
        instance.setPassword(getPropertyAsString(PASSWORD));

        instance.setUseLocalTrustStore(getPropertyAsBoolean(SecuritySettingsPanel.USE_LOCAL_TRUSTSTORE));
        instance.setTrustStoreToUse(getPropertyAsString(SecuritySettingsPanel.TRUSTSTORE_TO_USE));
        instance.setEmlMessage(getPropertyAsString(EML_MESSAGE_TO_SEND));
        instance.setUseEmlMessage(getPropertyAsBoolean(USE_EML));

        instance.setEnableDebug(getPropertyAsBoolean(ENABLE_DEBUG));

        if (getPropertyAsString(MAIL_FROM).matches(".*@.*")) {
            instance.setSender(getPropertyAsString(MAIL_FROM));
        }

        final String receiverTo = getPropertyAsString(SmtpSampler.RECEIVER_TO).trim();
        final String receiverCC = getPropertyAsString(SmtpSampler.RECEIVER_CC).trim();
        final String receiverBcc = getPropertyAsString(SmtpSampler.RECEIVER_BCC).trim();
        final String replyTo = getPropertyAsString(SmtpSampler.MAIL_REPLYTO).trim();

        try {
            // Process address lists
            instance.setReceiverTo(getPropNameAsAddresses(receiverTo));
            instance.setReceiverCC(getPropNameAsAddresses(receiverCC));
            instance.setReceiverBCC(getPropNameAsAddresses(receiverBcc));
            instance.setReplyTo(getPropNameAsAddresses(replyTo));

            if(getPropertyAsBoolean(SUPPRESS_SUBJECT)){
                instance.setSubject(null);
            }else{
                String subject = getPropertyAsString(SUBJECT);
                if (getPropertyAsBoolean(INCLUDE_TIMESTAMP)){
                    StringBuilder sb = new StringBuilder(subject);
                    sb.append(" <<< current timestamp: ");
                    sb.append(new Date().getTime());
                    sb.append(" >>>");
                    subject = sb.toString();
                }
                instance.setSubject(subject);
            }

            if (!getPropertyAsBoolean(USE_EML)) { // part is only needed if we
                // don't send an .eml-file
                instance.setMailBody(getPropertyAsString(MESSAGE));
                instance.setPlainBody(getPropertyAsBoolean(PLAIN_BODY));
                final String filesToAttach = getPropertyAsString(ATTACH_FILE);
                if (!filesToAttach.equals("")) {
                    String[] attachments = filesToAttach.split(FILENAME_SEPARATOR);
                    for (String attachment : attachments) {
                        File file = new File(attachment);
                        if(!file.isAbsolute() && !file.exists()){
                            log.debug("loading file with relative path: " +attachment);
                            file = new File(FileServer.getFileServer().getBaseDir(), attachment);
                            log.debug("file path set to: "+attachment);
                        }
                        instance.addAttachment(file);
                    }
                }

            }

            // needed for measuring sending time
            instance.setSynchronousMode(true);

            instance.setHeaderFields((CollectionProperty)getProperty(SmtpSampler.HEADER_FIELDS));
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
            res.setResponseCode("500");
            res.setResponseMessage(ex.toString());
            return res;
        } catch (IOException ex) {
            log.warn("Error while preparing message", ex);
            res.setResponseCode("500");
            res.setResponseMessage(ex.toString());
            return res;
        } catch (MessagingException ex) {
            log.warn("Error while preparing message", ex);
            res.setResponseCode("500");
            res.setResponseMessage(ex.toString());
            return res;
        }

        // Set up the sample result details
        res.setDataType(SampleResult.TEXT);
        try {
            res.setRequestHeaders(getRequestHeaders(message));
            res.setSamplerData(getSamplerData(message));
        } catch (MessagingException e1) {
            res.setSamplerData("Error occurred trying to save request info: "+e1);
            log.warn("Error occurred trying to save request info",e1);
        } catch (IOException e1) {
            res.setSamplerData("Error occurred trying to save request info: "+e1);
            log.warn("Error occurred trying to save request info",e1);
        }

        // Perform the sampling
        res.sampleStart();

        try {
            instance.execute(message);

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
        // SSL not supported, startTLS not supported, other messagingException
        } catch (MessagingException mex) {
            log.warn("",mex);
            res.setResponseCode("500");
            if (mex.getMessage().matches(".*Could not connect to SMTP host.*465.*")
                    && mex.getCause().getMessage().matches(".*Connection timed out.*")) {
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
                res.setResponseMessage("Other MessagingException: " + mex.toString());
            }
        }  catch (Exception ex) {   // general exception
            log.warn("",ex);
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
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[1024];
            int read = is.read(buf);
            while (read > 0) {
                sb.append(new String(buf, 0, read));  // TODO - charset?
                read = is.read(buf);
            }
            // TODO - charset?
            res.setResponseData(sb.toString().getBytes()); // TODO this should really be request data, but there is none
        } catch (IOException ex) {
            log.warn("",ex);
        } catch (MessagingException ex) {
            log.warn("",ex);
        }

        res.setSuccessful(isOK);

        return res;
    }

    private String getRequestHeaders(Message message) throws MessagingException {
        StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked") // getAllHeaders() is not yet genericised
        Enumeration<Header> headers = message.getAllHeaders(); // throws ME
        writeHeaders(headers, sb);
        return sb.toString();
    }

    private String getSamplerData(Message message) throws MessagingException, IOException {
        StringBuilder sb = new StringBuilder();
        Object content = message.getContent(); // throws ME
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            String contentType = multipart.getContentType();
            ContentType ct = new ContentType(contentType);
            String boundary=ct.getParameter("boundary");
            for (int i = 0; i < multipart.getCount(); i++) { // throws ME
                sb.append("--");
                sb.append(boundary);
                sb.append("\n");
                BodyPart bodyPart = multipart.getBodyPart(i); // throws ME
                writeBodyPart(sb, bodyPart); // throws IOE, ME
            }
            sb.append("--");
            sb.append(boundary);
            sb.append("--");
            sb.append("\n");
        } else if(content instanceof BodyPart){
            BodyPart bodyPart = (BodyPart) content;
            writeBodyPart(sb, bodyPart); // throws IOE, ME
        } else if (content instanceof String){
            sb.append(content);
        } else {
            sb.append("Content has class: "+content.getClass().getCanonicalName());
        }
        return sb.toString();
    }

    private void writeHeaders(Enumeration<Header> headers, StringBuilder sb) {
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            sb.append(header.getName());
            sb.append(": ");
            sb.append(header.getValue());
            sb.append("\n");
        }
    }

    private void writeBodyPart(StringBuilder sb, BodyPart bodyPart)
            throws MessagingException, IOException {
        @SuppressWarnings("unchecked") // API not yet generic
        Enumeration<Header> allHeaders = bodyPart.getAllHeaders(); // throws ME
        writeHeaders(allHeaders, sb);
        String disposition = bodyPart.getDisposition(); // throws ME
        sb.append("\n");
        if (BodyPart.ATTACHMENT.equals(disposition)) {
            sb.append("<attachment content not shown>");
        } else {
            sb.append(bodyPart.getContent()); // throws IOE, ME
        }
        sb.append("\n");
    }

    /**
     * Get the list of addresses or null.
     * Null is treated differently from an empty list.
     * @param propValue addresses separated by ";"
     * @return the list or null if the input was the empty string
     * @throws AddressException
     */
    private List<InternetAddress> getPropNameAsAddresses(String propValue) throws AddressException{
        if (propValue.length() > 0){ // we have at least one potential address
            List<InternetAddress> addresses = new ArrayList<InternetAddress>();
            for (String address : propValue.split(";")){
                addresses.add(new InternetAddress(address.trim()));
            }
            return addresses;
        } else {
            return null;
        }
    }
}