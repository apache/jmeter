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
package org.apache.jmeter.protocol.mail.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.smtp.sampler.gui.SecuritySettingsPanel;
import org.apache.jmeter.protocol.smtp.sampler.protocol.LocalTrustStoreSSLSocketFactory;
import org.apache.jmeter.protocol.smtp.sampler.protocol.TrustAllSSLSocketFactory;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sampler that can read from POP3 and IMAP mail servers
 */
public class MailReaderSampler extends AbstractSampler implements Interruptible {
    private static final Logger log = LoggerFactory.getLogger(MailReaderSampler.class);

    private static final long serialVersionUID = 240L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    //+ JMX attributes - do not change the values
    private static final String SERVER_TYPE = "host_type"; // $NON-NLS-1$
    private static final String SERVER = "host"; // $NON-NLS-1$
    private static final String PORT = "port"; // $NON-NLS-1$
    private static final String USERNAME = "username"; // $NON-NLS-1$
    private static final String PASSWORD = "password"; // $NON-NLS-1$ NOSONAR not a hardcoded password
    private static final String FOLDER = "folder"; // $NON-NLS-1$
    private static final String DELETE = "delete"; // $NON-NLS-1$
    private static final String NUM_MESSAGES = "num_messages"; // $NON-NLS-1$
    private static final String NEW_LINE = "\n"; // $NON-NLS-1$
    private static final String STORE_MIME_MESSAGE = "storeMimeMessage"; // $NON-NLS-1$
    private static final String HEADER_ONLY = "headerOnly"; // $NON-NLS-1$
    private static final boolean HEADER_ONLY_DEFAULT = false;
    //-

    private static final String RFC_822_DEFAULT_ENCODING = "iso-8859-1"; // RFC 822 uses ascii per default

    public static final String DEFAULT_PROTOCOL = "pop3";  // $NON-NLS-1$

    // Use the actual class so the name must be correct.
    private static final String TRUST_ALL_SOCKET_FACTORY = TrustAllSSLSocketFactory.class.getName();

    private static final String FALSE = "false";  // $NON-NLS-1$

    private static final String TRUE = "true";  // $NON-NLS-1$

    public boolean isUseLocalTrustStore() {
        return getPropertyAsBoolean(SecuritySettingsPanel.USE_LOCAL_TRUSTSTORE);
    }

    public String getTrustStoreToUse() {
        return getPropertyAsString(SecuritySettingsPanel.TRUSTSTORE_TO_USE);
    }


    public boolean isUseSSL() {
        return getPropertyAsBoolean(SecuritySettingsPanel.USE_SSL);
    }


    public boolean isUseStartTLS() {
        return getPropertyAsBoolean(SecuritySettingsPanel.USE_STARTTLS);
    }


    public boolean isTrustAllCerts() {
        return getPropertyAsBoolean(SecuritySettingsPanel.SSL_TRUST_ALL_CERTS);
    }


    public boolean isEnforceStartTLS() {
        return getPropertyAsBoolean(SecuritySettingsPanel.ENFORCE_STARTTLS);

    }

    public static final int ALL_MESSAGES = -1; // special value

    private volatile boolean busy;

    public MailReaderSampler() {
        setServerType(DEFAULT_PROTOCOL);
        setFolder("INBOX");  // $NON-NLS-1$
        setNumMessages(ALL_MESSAGES);
        setDeleteMessages(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        SampleResult parent = new SampleResult();
        boolean isOK = false; // Did sample succeed?
        final boolean deleteMessages = getDeleteMessages();
        final String serverProtocol = getServerType();

        parent.setSampleLabel(getName());

        String samplerString = toString();
        parent.setSamplerData(samplerString);

        /*
         * Perform the sampling
         */
        parent.sampleStart(); // Start timing
        try {
            // Create empty properties
            Properties props = new Properties();

            if (isUseStartTLS()) {
                props.setProperty(mailProp(serverProtocol, "starttls.enable"), TRUE);  // $NON-NLS-1$
                if (isEnforceStartTLS()){
                    // Requires JavaMail 1.4.2+
                    props.setProperty(mailProp(serverProtocol, "starttls.require"), TRUE);  // $NON-NLS-1$
                }
            }

            if (isTrustAllCerts()) {
                if (isUseSSL()) {
                    props.setProperty(mailProp(serverProtocol, "ssl.socketFactory.class"), TRUST_ALL_SOCKET_FACTORY);  // $NON-NLS-1$
                    props.setProperty(mailProp(serverProtocol, "ssl.socketFactory.fallback"), FALSE);  // $NON-NLS-1$
                } else if (isUseStartTLS()) {
                    props.setProperty(mailProp(serverProtocol, "ssl.socketFactory.class"), TRUST_ALL_SOCKET_FACTORY);  // $NON-NLS-1$
                    props.setProperty(mailProp(serverProtocol, "ssl.socketFactory.fallback"), FALSE);  // $NON-NLS-1$
                }
            } else if (isUseLocalTrustStore()){
                File truststore = new File(getTrustStoreToUse());
                log.info("load local truststore - try to load truststore from: "+truststore.getAbsolutePath());
                if(!truststore.exists()){
                    log.info("load local truststore -Failed to load truststore from: "+truststore.getAbsolutePath());
                    truststore = new File(FileServer.getFileServer().getBaseDir(), getTrustStoreToUse());
                    log.info("load local truststore -Attempting to read truststore from:  "+truststore.getAbsolutePath());
                    if(!truststore.exists()){
                        log.info("load local truststore -Failed to load truststore from: "+truststore.getAbsolutePath() + ". Local truststore not available, aborting execution.");
                        throw new IOException("Local truststore file not found. Also not available under : " + truststore.getAbsolutePath());
                    }
                }
                if (isUseSSL()) {
                    // Requires JavaMail 1.4.2+
                    props.put(mailProp(serverProtocol, "ssl.socketFactory"),   // $NON-NLS-1$ 
                            new LocalTrustStoreSSLSocketFactory(truststore));
                    props.put(mailProp(serverProtocol, "ssl.socketFactory.fallback"), FALSE);  // $NON-NLS-1$
                } else if (isUseStartTLS()) {
                    // Requires JavaMail 1.4.2+
                    props.put(mailProp(serverProtocol, "ssl.socketFactory"),  // $NON-NLS-1$
                            new LocalTrustStoreSSLSocketFactory(truststore));
                    props.put(mailProp(serverProtocol, "ssl.socketFactory.fallback"), FALSE);  // $NON-NLS-1$
                }
            }

            // Get session
            Session session = Session.getInstance(props, null);

            // Get the store
            Store store = session.getStore(serverProtocol);
            store.connect(getServer(), getPortAsInt(), getUserName(), getPassword());

            // Get folder
            Folder folder = store.getFolder(getFolder());
            if (deleteMessages) {
                folder.open(Folder.READ_WRITE);
            } else {
                folder.open(Folder.READ_ONLY);
            }

            final int messageTotal = folder.getMessageCount();
            int n = getNumMessages();
            if (n == ALL_MESSAGES || n > messageTotal) {
                n = messageTotal;
            }

            // Get directory
            Message[] messages = folder.getMessages(1,n);
            StringBuilder pdata = new StringBuilder();
            pdata.append(messages.length);
            pdata.append(" messages found\n");
            parent.setResponseData(pdata.toString(),null);
            parent.setDataType(SampleResult.TEXT);
            parent.setContentType("text/plain"); // $NON-NLS-1$

            final boolean headerOnly = getHeaderOnly();
            busy = true;
            for (Message message : messages) {
                StringBuilder cdata = new StringBuilder();
                SampleResult child = new SampleResult();
                child.sampleStart();

                cdata.append("Message "); // $NON-NLS-1$
                cdata.append(message.getMessageNumber());
                child.setSampleLabel(cdata.toString());
                child.setSamplerData(cdata.toString());
                cdata.setLength(0);

                final String contentType = message.getContentType();
                child.setContentType(contentType);// Store the content-type
                child.setDataEncoding(RFC_822_DEFAULT_ENCODING); // RFC 822 uses ascii per default
                child.setEncodingAndType(contentType);// Parse the content-type

                if (isStoreMimeMessage()) {
                    // Don't save headers - they are already in the raw message
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    message.writeTo(bout);
                    child.setResponseData(bout.toByteArray()); // Save raw message
                    child.setDataType(SampleResult.TEXT);
                } else {
                    @SuppressWarnings("unchecked") // Javadoc for the API says this is OK
                    Enumeration<Header> hdrs = message.getAllHeaders();
                    while(hdrs.hasMoreElements()){
                        Header hdr = hdrs.nextElement();
                        String value = hdr.getValue();
                        try {
                            value = MimeUtility.decodeText(value);
                        } catch (UnsupportedEncodingException uce) {
                            // ignored
                        }
                        cdata.append(hdr.getName()).append(": ").append(value).append("\n");
                    }
                    child.setResponseHeaders(cdata.toString());
                    cdata.setLength(0);
                    if (!headerOnly) {
                        appendMessageData(child, message);
                    }
                }

                if (deleteMessages) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
                child.setResponseOK();
                if (child.getEndTime()==0){// Avoid double-call if addSubResult was called.
                    child.sampleEnd();
                }
                parent.addSubResult(child);
            }

            // Close connection
            folder.close(true);
            store.close();

            parent.setResponseCodeOK();
            parent.setResponseMessageOK();
            isOK = true;
        } catch (NoClassDefFoundError | IOException ex) {
            log.debug("",ex);// No need to log normally, as we set the status
            parent.setResponseCode("500"); // $NON-NLS-1$
            parent.setResponseMessage(ex.toString());
        } catch (MessagingException ex) {
            log.debug("", ex);// No need to log normally, as we set the status
            parent.setResponseCode("500"); // $NON-NLS-1$
            parent.setResponseMessage(ex.toString() + "\n" + samplerString); // $NON-NLS-1$
        } finally {
            busy = false;
        }

        if (parent.getEndTime()==0){// not been set by any child samples
            parent.sampleEnd();
        }
        parent.setSuccessful(isOK);
        return parent;
    }

    private void appendMessageData(SampleResult child, Message message)
            throws MessagingException, IOException {
        StringBuilder cdata = new StringBuilder();
        cdata.append("Date: "); // $NON-NLS-1$
        cdata.append(message.getSentDate());// TODO - use a different format here?
        cdata.append(NEW_LINE);

        cdata.append("To: "); // $NON-NLS-1$
        Address[] recips = message.getAllRecipients(); // may be null
        for (int j = 0; recips != null && j < recips.length; j++) {
            cdata.append(recips[j].toString());
            if (j < recips.length - 1) {
                cdata.append("; "); // $NON-NLS-1$
            }
        }
        cdata.append(NEW_LINE);

        cdata.append("From: "); // $NON-NLS-1$
        Address[] from = message.getFrom(); // may be null
        for (int j = 0; from != null && j < from.length; j++) {
            cdata.append(from[j].toString());
            if (j < from.length - 1) {
                cdata.append("; "); // $NON-NLS-1$
            }
        }
        cdata.append(NEW_LINE);

        cdata.append("Subject: "); // $NON-NLS-1$
        cdata.append(message.getSubject());
        cdata.append(NEW_LINE);

        cdata.append(NEW_LINE);
        Object content = message.getContent();
        if (content instanceof MimeMultipart) {
            appendMultiPart(child, cdata, (MimeMultipart) content);
        } else if (content instanceof InputStream){
            child.setResponseData(IOUtils.toByteArray((InputStream) content));
        } else {
            cdata.append(content);
            child.setResponseData(cdata.toString(),child.getDataEncodingNoDefault());
        }
    }

    private void appendMultiPart(SampleResult child, StringBuilder cdata,
            MimeMultipart mmp) throws MessagingException, IOException {
        String preamble = mmp.getPreamble();
        if (preamble != null ){
            cdata.append(preamble);
        }
        child.setResponseData(cdata.toString(),child.getDataEncodingNoDefault());
        int count = mmp.getCount();
        for (int j=0; j<count;j++){
            BodyPart bodyPart = mmp.getBodyPart(j);
            final Object bodyPartContent = bodyPart.getContent();
            final String contentType = bodyPart.getContentType();
            SampleResult sr = new SampleResult();
            sr.setSampleLabel("Part: "+j);
            sr.setContentType(contentType);
            sr.setDataEncoding(RFC_822_DEFAULT_ENCODING);
            sr.setEncodingAndType(contentType);
            sr.sampleStart();
            if (bodyPartContent instanceof InputStream){
                sr.setResponseData(IOUtils.toByteArray((InputStream) bodyPartContent));
            } else if (bodyPartContent instanceof MimeMultipart){
                appendMultiPart(sr, cdata, (MimeMultipart) bodyPartContent);
            } else {
                sr.setResponseData(bodyPartContent.toString(),sr.getDataEncodingNoDefault());
            }
            sr.setResponseOK();
            if (sr.getEndTime()==0){// not been set by any child samples
                sr.sampleEnd();
            }
            child.addSubResult(sr);
        }
    }

    /**
     * Sets the type of protocol to use when talking with the remote mail
     * server. Either MailReaderSampler.TYPE_IMAP[S] or
     * MailReaderSampler.TYPE_POP3[S]. Default is MailReaderSampler.TYPE_POP3.
     *
     * @param serverType protocol to use
     */
    public void setServerType(String serverType) {
        setProperty(SERVER_TYPE, serverType);
    }

    /**
     * Returns the type of the protocol set to use when talking with the remote
     * server. Either MailReaderSampler.TYPE_IMAP[S] or
     * MailReaderSampler.TYPE_POP3[S].
     *
     * @return Server Type
     */
    public String getServerType() {
        return getPropertyAsString(SERVER_TYPE);
    }

    /**
     * @param server -
     *            The name or address of the remote server.
     */
    public void setServer(String server) {
        setProperty(SERVER, server);
    }

    /**
     * @return The name or address of the remote server.
     */
    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    private int getPortAsInt() {
        return getPropertyAsInt(PORT, -1);
    }

    public void setPort(String port) {
        setProperty(PORT, port, "");
    }

    /**
     * @param username -
     *            The username of the mail account.
     */
    public void setUserName(String username) {
        setProperty(USERNAME, username);
    }

    /**
     * @return The username of the mail account.
     */
    public String getUserName() {
        return getPropertyAsString(USERNAME);
    }

    /**
     * @param password the password to use
     */
    public void setPassword(String password) {
        setProperty(PASSWORD, password);
    }

    /**
     * @return password
     */
    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    /**
     * @param folder -
     *            Name of the folder to read emails from. "INBOX" is the only
     *            acceptable value if the server type is POP3.
     */
    public void setFolder(String folder) {
        setProperty(FOLDER, folder);
    }

    /**
     * @return folder
     */
    public String getFolder() {
        return getPropertyAsString(FOLDER);
    }

    /**
     * @param numMessages -
     *            The number of messages to retrieve from the mail server. Set
     *            this value to -1 to retrieve all messages.
     */
    public void setNumMessages(int numMessages) {
        setProperty(new IntegerProperty(NUM_MESSAGES, numMessages));
    }

    /**
     * @param numMessages -
     *            The number of messages to retrieve from the mail server. Set
     *            this value to -1 to retrieve all messages.
     */
    public void setNumMessages(String numMessages) {
        setProperty(new StringProperty(NUM_MESSAGES, numMessages));
    }

    /**
     * @return The number of messages to retrieve from the mail server.
     *         -1 denotes get all messages.
     */
    public int getNumMessages() {
        return getPropertyAsInt(NUM_MESSAGES);
    }

    /**
     * @return The number of messages to retrieve from the mail server.
     *         -1 denotes get all messages.
     */
    public String getNumMessagesString() {
        return getPropertyAsString(NUM_MESSAGES);
    }

    /**
     * @param delete -
     *            Whether or not to delete the read messages from the folder.
     */
    public void setDeleteMessages(boolean delete) {
        setProperty(new BooleanProperty(DELETE, delete));
    }

    /**
     * @return Whether or not to delete the read messages from the folder.
     */
    public boolean getDeleteMessages() {
        return getPropertyAsBoolean(DELETE);
    }

    /**
     * @return Whether or not to store the retrieved message as MIME message in
     *         the sample result
     */
    public boolean isStoreMimeMessage() {
        return getPropertyAsBoolean(STORE_MIME_MESSAGE, false);
    }

    /**
     * @param storeMimeMessage
     *            Whether or not to store the retrieved message as MIME message in the
     *            sample result
     */
    public void setStoreMimeMessage(boolean storeMimeMessage) {
        setProperty(STORE_MIME_MESSAGE, storeMimeMessage, false);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getServerType());
        sb.append("://");
        String name = getUserName();
        if (name.length() > 0){
            sb.append(name);
            sb.append("@");
        }
        sb.append(getServer());
        int port=getPortAsInt();
        if (port != -1){
            sb.append(":").append(port);
        }
        sb.append("/");
        sb.append(getFolder());
        sb.append("[");
        sb.append(getNumMessages());
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean interrupt() {
        boolean wasbusy = busy;
        busy = false;
        return wasbusy;
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    public boolean getHeaderOnly() {
        return getPropertyAsBoolean(HEADER_ONLY, HEADER_ONLY_DEFAULT);
    }

    public void setHeaderOnly(boolean selected) {
        setProperty(HEADER_ONLY, selected, HEADER_ONLY_DEFAULT);
    }

    /**
     * Build a property name of the form "mail.pop3s.starttls.require"
     *
     * @param protocol the protocol, i.e. "pop3s" in the example
     * @param propname the property name suffix, i.e. "starttls.require" in the example
     * @return the constructed name
     */
    private String mailProp(String protocol, String propname) {
        StringBuilder sb = new StringBuilder();
        sb.append("mail.").append(protocol).append(".");
        sb.append(propname);
        return sb.toString();
    }
}
