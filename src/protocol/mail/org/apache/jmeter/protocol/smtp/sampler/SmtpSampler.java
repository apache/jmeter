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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.smtp.sampler.gui.SecuritySettingsPanel;
import org.apache.jmeter.protocol.smtp.sampler.protocol.SendMailCommand;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sampler-Class for JMeter - builds, starts and interprets the results of the
 * sampler. Has to implement some standard-methods for JMeter in order to be
 * integrated in the framework. All getter/setter methods just deliver/set
 * values from/to the sampler, not from/to the message-object. Therefore, all
 * these methods are also present in class SendMailCommand.
 */
public class SmtpSampler extends AbstractSampler {

    private static final long serialVersionUID = 1L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    private static final Logger log = LoggerFactory.getLogger(SmtpSampler.class);

    //+JMX file attribute names - do not change any values!
    public static final String SERVER               = "SMTPSampler.server"; // $NON-NLS-1$
    public static final String SERVER_PORT          = "SMTPSampler.serverPort"; // $NON-NLS-1$
    public static final String SERVER_TIMEOUT       = "SMTPSampler.serverTimeout"; // $NON-NLS-1$
    public static final String SERVER_CONNECTION_TIMEOUT = "SMTPSampler.serverConnectionTimeout"; // $NON-NLS-1$
    public static final String USE_AUTH             = "SMTPSampler.useAuth"; // $NON-NLS-1$
    public static final String USERNAME             = "SMTPSampler.username"; // $NON-NLS-1$
    public static final String PASSWORD             = "SMTPSampler.password"; // $NON-NLS-1$ NOSONAR not a hardcoded password
    public static final String MAIL_FROM            = "SMTPSampler.mailFrom"; // $NON-NLS-1$
    public static final String MAIL_REPLYTO         = "SMTPSampler.replyTo"; // $NON-NLS-1$
    public static final String RECEIVER_TO          = "SMTPSampler.receiverTo"; // $NON-NLS-1$
    public static final String RECEIVER_CC          = "SMTPSampler.receiverCC"; // $NON-NLS-1$
    public static final String RECEIVER_BCC         = "SMTPSampler.receiverBCC"; // $NON-NLS-1$

    public static final String SUBJECT              = "SMTPSampler.subject"; // $NON-NLS-1$
    public static final String SUPPRESS_SUBJECT     = "SMTPSampler.suppressSubject"; // $NON-NLS-1$
    public static final String MESSAGE              = "SMTPSampler.message"; // $NON-NLS-1$
    public static final String PLAIN_BODY           = "SMTPSampler.plainBody"; // $NON-NLS-1$
    public static final String INCLUDE_TIMESTAMP    = "SMTPSampler.include_timestamp"; // $NON-NLS-1$
    public static final String ATTACH_FILE          = "SMTPSampler.attachFile"; // $NON-NLS-1$
    public static final String MESSAGE_SIZE_STATS   = "SMTPSampler.messageSizeStatistics"; // $NON-NLS-1$
    public static final String HEADER_FIELDS        = "SMTPSampler.headerFields"; // $NON-NLS-1$

    public static final String USE_EML              = "SMTPSampler.use_eml"; // $NON-NLS-1$
    public static final String EML_MESSAGE_TO_SEND  = "SMTPSampler.emlMessageToSend"; // $NON-NLS-1$
    public static final String ENABLE_DEBUG         = "SMTPSampler.enableDebug"; // $NON-NLS-1$

    // Used to separate attachment file names in JMX fields - do not change!
    public static final String FILENAME_SEPARATOR = ";";
    //-JMX file attribute names


    public SmtpSampler() {
        super();
    }

    /**
     * Performs the sample, and returns the result
     *
     * @param e Standard-method-header from JMeter
     * @return Result of the sample
     * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    @Override
    public SampleResult sample(Entry e) {
        SendMailCommand sendMailCmd;
        Message message;
        SampleResult result = createSampleResult();

        try {
            sendMailCmd = createSendMailCommandFromProperties();
            message = sendMailCmd.prepareMessage();
            result.setBytes(calculateMessageSize(message));
        } catch (Exception ex) {
            log.warn("Error while preparing message", ex);
            result.setResponseCode("500");
            result.setResponseMessage(ex.toString());
            return result;
        }

        // Set up the sample result details
        result.setDataType(SampleResult.TEXT);
        try {
            result.setRequestHeaders(getRequestHeaders(message));
            result.setSamplerData(getSamplerData(message));
        } catch (MessagingException | IOException ex) {
            result.setSamplerData("Error occurred trying to save request info: " + ex);
            log.warn("Error occurred trying to save request info", ex);
        }

        // Perform the sampling
        result.sampleStart();
        boolean isSuccessful = executeMessage(result, sendMailCmd, message);
        result.sampleEnd();

        try {
            result.setResponseData(processSampler(message));
        } catch (IOException | MessagingException ex) {
            log.warn("Failed to set result response data", ex);
        }

        result.setSuccessful(isSuccessful);

        return result;
    }

    private SampleResult createSampleResult() {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        return result;
    }

    private boolean executeMessage(SampleResult result, SendMailCommand sendMailCmd, Message message) {
        boolean didSampleSucceed = false;
        try {
            sendMailCmd.execute(message);
            result.setResponseCodeOK();
            result.setResponseMessage(
                    "Message successfully sent!\n");
            didSampleSucceed = true;
        } catch (AuthenticationFailedException afex) {
            log.warn("", afex);
            result.setResponseCode("500");
            result.setResponseMessage(
                    "AuthenticationFailedException: authentication failed - wrong username / password!\n"
                            + afex);
        } catch (Exception ex) {
            log.warn("", ex);
            result.setResponseCode("500");
            result.setResponseMessage(ex.getMessage());
        }
        return didSampleSucceed;
    }

    private long calculateMessageSize(Message message) throws IOException, MessagingException {
        if (getPropertyAsBoolean(MESSAGE_SIZE_STATS)) {
            // calculate message size
            CountingOutputStream cs = new CountingOutputStream(new NullOutputStream());
            message.writeTo(cs);
            return cs.getByteCount();
        } else {
            return -1L;
        }
    }

    private byte[] processSampler(Message message) throws IOException, MessagingException {
        // process the sampler result
        try (InputStream is = message.getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    private List<File> getAttachmentFiles() {
        final String[] attachments = getPropertyAsString(ATTACH_FILE).split(FILENAME_SEPARATOR);
        return Arrays.stream(attachments) // NOSONAR No need to close
                .filter(s -> !s.isEmpty())
                .map(this::attachmentToFile)
                .collect(Collectors.toList());
    }

    private File attachmentToFile(String attachment) { // NOSONAR False positive saying not used
        File file = new File(attachment);
        if (!file.isAbsolute() && !file.exists()) {
            if(log.isDebugEnabled()) {
                log.debug("loading file with relative path: " + attachment);
            }
            file = new File(FileServer.getFileServer().getBaseDir(), attachment);
            if(log.isDebugEnabled()) {
                log.debug("file path set to: " + attachment);
            }
        }
        return file;
    }

    private String calculateSubject() {
        if (getPropertyAsBoolean(SUPPRESS_SUBJECT)) {
            return null;
        } else {
            String subject = getPropertyAsString(SUBJECT);
            if (getPropertyAsBoolean(INCLUDE_TIMESTAMP)) {
                subject = subject
                        + " <<< current timestamp: "
                        + System.currentTimeMillis()
                        + " >>>";
            }
            return subject;
        }
    }

    private SendMailCommand createSendMailCommandFromProperties() throws AddressException {
        SendMailCommand sendMailCmd = new SendMailCommand();
        sendMailCmd.setSmtpServer(getPropertyAsString(SmtpSampler.SERVER));
        sendMailCmd.setSmtpPort(getPropertyAsString(SmtpSampler.SERVER_PORT));
        sendMailCmd.setConnectionTimeOut(getPropertyAsString(SmtpSampler.SERVER_CONNECTION_TIMEOUT));
        sendMailCmd.setTimeOut(getPropertyAsString(SmtpSampler.SERVER_TIMEOUT));

        sendMailCmd.setUseSSL(getPropertyAsBoolean(SecuritySettingsPanel.USE_SSL));
        sendMailCmd.setUseStartTLS(getPropertyAsBoolean(SecuritySettingsPanel.USE_STARTTLS));
        sendMailCmd.setTrustAllCerts(getPropertyAsBoolean(SecuritySettingsPanel.SSL_TRUST_ALL_CERTS));
        sendMailCmd.setEnforceStartTLS(getPropertyAsBoolean(SecuritySettingsPanel.ENFORCE_STARTTLS));
        sendMailCmd.setTlsProtocolsToUse(getPropertyAsString(SecuritySettingsPanel.TLS_PROTOCOLS));

        sendMailCmd.setUseAuthentication(getPropertyAsBoolean(USE_AUTH));
        sendMailCmd.setUsername(getPropertyAsString(USERNAME));
        sendMailCmd.setPassword(getPropertyAsString(PASSWORD));

        sendMailCmd.setUseLocalTrustStore(getPropertyAsBoolean(SecuritySettingsPanel.USE_LOCAL_TRUSTSTORE));
        sendMailCmd.setTrustStoreToUse(getPropertyAsString(SecuritySettingsPanel.TRUSTSTORE_TO_USE));

        sendMailCmd.setEmlMessage(getPropertyAsString(EML_MESSAGE_TO_SEND));
        sendMailCmd.setUseEmlMessage(getPropertyAsBoolean(USE_EML));
        if (!getPropertyAsBoolean(USE_EML)) {
            // if we are not sending an .eml file
            sendMailCmd.setMailBody(getPropertyAsString(MESSAGE));
            sendMailCmd.setPlainBody(getPropertyAsBoolean(PLAIN_BODY));
            getAttachmentFiles().forEach(sendMailCmd::addAttachment);
        }

        sendMailCmd.setEnableDebug(getPropertyAsBoolean(ENABLE_DEBUG));

        if (getPropertyAsString(MAIL_FROM).matches(".*@.*")) {
            sendMailCmd.setSender(getPropertyAsString(MAIL_FROM));
        }

        // Process address lists
        sendMailCmd.setReceiverTo(getPropAsAddresses(SmtpSampler.RECEIVER_TO));
        sendMailCmd.setReceiverCC(getPropAsAddresses(SmtpSampler.RECEIVER_CC));
        sendMailCmd.setReceiverBCC(getPropAsAddresses(SmtpSampler.RECEIVER_BCC));
        sendMailCmd.setReplyTo(getPropAsAddresses(SmtpSampler.MAIL_REPLYTO));
        sendMailCmd.setSubject(calculateSubject());

        // needed for measuring sending time
        sendMailCmd.setSynchronousMode(true);

        sendMailCmd.setHeaderFields((CollectionProperty) getProperty(SmtpSampler.HEADER_FIELDS));

        return sendMailCmd;
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
            String boundary = ct.getParameter("boundary");
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
        } else if (content instanceof BodyPart) {
            BodyPart bodyPart = (BodyPart) content;
            writeBodyPart(sb, bodyPart); // throws IOE, ME
        } else if (content instanceof String) {
            sb.append(content);
        } else {
            sb.append("Content has class: ");
            sb.append(content.getClass().getCanonicalName());
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
        if (Part.ATTACHMENT.equals(disposition)) {
            sb.append("<attachment content not shown>");
        } else {
            sb.append(bodyPart.getContent()); // throws IOE, ME
        }
        sb.append("\n");
    }

    /**
     * Get the list of addresses or null.
     * Null is treated differently from an empty list.
     *
     * @param propKey key of the property containing addresses separated by ";"
     * @return the list or null if the input was the empty string
     * @throws AddressException thrown if any address is an illegal format
     */
    private List<InternetAddress> getPropAsAddresses(String propKey) throws AddressException {
        final String propValue = getPropertyAsString(propKey).trim();
        if (!propValue.isEmpty()) { // we have at least one potential address
            List<InternetAddress> addresses = new ArrayList<>();
            for (String address : propValue.split(";")) {
                addresses.add(new InternetAddress(address.trim()));
            }
            return addresses;
        } else {
            return null;
        }
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
