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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Sampler that can read from POP3 and IMAP mail servers
 */
public class MailReaderSampler extends AbstractSampler {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    //+ JMX attributes - do not change the values
    private final static String SERVER_TYPE = "host_type"; // $NON-NLS-1$
    private final static String SERVER = "host"; // $NON-NLS-1$
    private final static String PORT = "port"; // $NON-NLS-1$
    private final static String USERNAME = "username"; // $NON-NLS-1$
    private final static String PASSWORD = "password"; // $NON-NLS-1$
    private final static String FOLDER = "folder"; // $NON-NLS-1$
    private final static String DELETE = "delete"; // $NON-NLS-1$
    private final static String NUM_MESSAGES = "num_messages"; // $NON-NLS-1$
    private static final String NEW_LINE = "\n"; // $NON-NLS-1$
    private final static String STORE_MIME_MESSAGE = "storeMimeMessage";

    // Needed by GUI
    public final static String TYPE_POP3 = "pop3"; // $NON-NLS-1$
    public final static String TYPE_POP3S = "pop3s"; // $NON-NLS-1$
    public final static String TYPE_IMAP = "imap"; // $NON-NLS-1$
    public final static String TYPE_IMAPS = "imaps"; // $NON-NLS-1$
    //-
    
    public static final int ALL_MESSAGES = -1; // special value

    public MailReaderSampler() {
        setServerType(TYPE_POP3);
        setFolder("INBOX");
        setNumMessages(ALL_MESSAGES);
        setDeleteMessages(false);
    }

    /*
     * (non-Javadoc) Performs the sample, and returns the result
     *
     * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    public SampleResult sample(Entry e) {
        SampleResult parent = new SampleResult();
        boolean isOK = false; // Did sample succeed?
        boolean deleteMessages = getDeleteMessages();

        parent.setSampleLabel(getName());
        int port=getPortAsInt();
        if (port > 0){
            parent.setSamplerData(getServerType() + "://" + getUserName() + "@" + getServer()+ ":" + port);
        } else {
            parent.setSamplerData(getServerType() + "://" + getUserName() + "@" + getServer());
        }
        /*
         * Perform the sampling
         */
        parent.sampleStart(); // Start timing
        try {
            // Create empty properties
            Properties props = new Properties();

            // Get session
            Session session = Session.getDefaultInstance(props, null);

            // Get the store
            Store store = session.getStore(getServerType());
            if (port > 0){
                store.connect(getServer(), port, getUserName(), getPassword());
            } else {
                store.connect(getServer(), getUserName(), getPassword());
            }

            // Get folder
            Folder folder = store.getFolder(getFolder());
            if (deleteMessages) {
                folder.open(Folder.READ_WRITE);
            } else {
                folder.open(Folder.READ_ONLY);
            }

            // Get directory
            Message messages[] = folder.getMessages();
            Message message;
            StringBuilder pdata = new StringBuilder();
            pdata.append(messages.length);
            pdata.append(" messages found\n");

            int n = getNumMessages();
            if (n == ALL_MESSAGES || n > messages.length) {
                n = messages.length;
            }

            for (int i = 0; i < n; i++) {
                StringBuilder cdata = new StringBuilder();
                SampleResult child = new SampleResult();
                child.sampleStart();
                message = messages[i];

                //if (i == 0)
                { // Assumes all the messaged have the same type ...
                    child.setContentType(message.getContentType());
                }

                cdata.append("Message "); // $NON-NLS-1$
                cdata.append(message.getMessageNumber());
                child.setSampleLabel(cdata.toString());
                child.setSamplerData(cdata.toString());
                cdata.setLength(0);

                if (isStoreMimeMessage()) {
                    appendMessageAsMime(cdata, message);
                } else {
                    appendMessageData(cdata, message);
                }

                if (deleteMessages) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
                child.setResponseData(cdata.toString().getBytes());
                child.setDataType(SampleResult.TEXT);
                child.setResponseCodeOK();
                child.setResponseMessage("OK"); // $NON-NLS-1$
                child.setSuccessful(true);
                child.sampleEnd();
                parent.addSubResult(child);
            }

            // Close connection
            folder.close(true);
            store.close();

            /*
             * Set up the sample result details
             */
            parent.setResponseData(pdata.toString().getBytes());
            parent.setDataType(SampleResult.TEXT);
            parent.setContentType("text/plain"); // $NON-NLS-1$

            parent.setResponseCodeOK();
            parent.setResponseMessage("OK"); // $NON-NLS-1$
            isOK = true;
        } catch (NoClassDefFoundError ex) {
            log.debug("",ex);// No need to log normally, as we set the status
            parent.setResponseCode("500"); // $NON-NLS-1$
            parent.setResponseMessage(ex.toString());
        } catch (MessagingException ex) {
            log.debug("", ex);// No need to log normally, as we set the status
            parent.setResponseCode("500"); // $NON-NLS-1$
            parent.setResponseMessage(ex.toString());
        } catch (IOException ex) {
            log.debug("", ex);// No need to log normally, as we set the status
            parent.setResponseCode("500"); // $NON-NLS-1$
            parent.setResponseMessage(ex.toString());
        }

        if (parent.getEndTime()==0){// not been set by any child samples
            parent.sampleEnd();
        }
        parent.setSuccessful(isOK);
        return parent;
    }

    private void appendMessageData(StringBuilder cdata, Message message)
            throws MessagingException, IOException {
        cdata.append("Date: "); // $NON-NLS-1$
        cdata.append(message.getSentDate());// TODO - use a different format here?
        cdata.append(NEW_LINE);

        cdata.append("To: "); // $NON-NLS-1$
        Address[] recips = message.getAllRecipients();
        for (int j = 0; j < recips.length; j++) {
            cdata.append(recips[j].toString());
            if (j < recips.length - 1) {
                cdata.append("; "); // $NON-NLS-1$
            }
        }
        cdata.append(NEW_LINE);

        cdata.append("From: "); // $NON-NLS-1$
        Address[] from = message.getFrom();
        for (int j = 0; j < from.length; j++) {
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
            MimeMultipart mmp = (MimeMultipart) content;
            int count = mmp.getCount();
            cdata.append("Multipart. Count: ");
            cdata.append(count);
            cdata.append(NEW_LINE);
            for (int j=0; j<count;j++){
                BodyPart bodyPart = mmp.getBodyPart(j);
                cdata.append("Type: ");
                cdata.append(bodyPart.getContentType());
                cdata.append(NEW_LINE);
                try {
                    cdata.append(bodyPart.getContent());
                } catch (UnsupportedEncodingException ex){
                    cdata.append(ex.getLocalizedMessage());
                }
            }
        } else {
            cdata.append(content);
        }
    }

    private void appendMessageAsMime(StringBuilder cdata, Message message)
            throws MessagingException, IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        message.writeTo(bout);
        cdata.append(bout);
    }

    /**
     * Sets the type of protocol to use when talking with the remote mail
     * server. Either MailReaderSampler.TYPE_IMAP[S] or
     * MailReaderSampler.TYPE_POP3[S]. Default is MailReaderSampler.TYPE_POP3.
     *
     * @param serverType
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
        return getPropertyAsInt(PORT);
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
     * @param password
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
     * @param num_messages -
     *            The number of messages to retrieve from the mail server. Set
     *            this value to -1 to retrieve all messages.
     */
    public void setNumMessages(int num_messages) {
        setProperty(new IntegerProperty(NUM_MESSAGES, num_messages));
    }

    /**
     * @param num_messages -
     *            The number of messages to retrieve from the mail server. Set
     *            this value to -1 to retrieve all messages.
     */
    public void setNumMessages(String num_messages) {
        setProperty(new StringProperty(NUM_MESSAGES, num_messages));
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
}
