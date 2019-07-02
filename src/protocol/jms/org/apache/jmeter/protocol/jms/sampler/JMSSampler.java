/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.protocol.jms.sampler;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the JMS Point-to-Point sampler
 *
 */
public class JMSSampler extends AbstractSampler implements ThreadListener {

    private static final long serialVersionUID = 233L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSSampler.class);

    private static final String PREV_SAMPLER = "__PREV_SAMPLER__";

    private static final int DEFAULT_TIMEOUT = 2000;

    private static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

    // These are JMX names, and must not be changed
    private static final String JNDI_INITIAL_CONTEXT_FACTORY = "JMSSampler.initialContextFactory"; // $NON-NLS-1$

    private static final String JNDI_CONTEXT_PROVIDER_URL = "JMSSampler.contextProviderUrl"; // $NON-NLS-1$

    private static final String JNDI_PROPERTIES = "JMSSampler.jndiProperties"; // $NON-NLS-1$

    private static final String TIMEOUT = "JMSSampler.timeout"; // $NON-NLS-1$

    private static final String JMS_PRIORITY = "JMSSampler.priority"; // $NON-NLS-1$

    private static final String JMS_EXPIRATION = "JMSSampler.expiration"; // $NON-NLS-1$

    private static final String JMS_SELECTOR = "JMSSampler.jmsSelector"; // $NON-NLS-1$

    private static final String JMS_SELECTOR_DEFAULT = ""; // $NON-NLS-1$

    /**
     * @deprecated since 4.0 replaced by JMS_COMMUNICATION_STYLE
     */
    @Deprecated
    public static final String IS_ONE_WAY = "JMSSampler.isFireAndForget"; // $NON-NLS-1$ // NOSONAR

    private static final String JMS_NUMBEROFSAMPLES = "JMSSampler.jmsNumberOfSamplesToAggregate"; // $NON-NLS-1$

    private static final String JMS_NUMBEROFSAMPLES_DEFAULT = "1"; // $NON-NLS-1$

    public static final String JMS_COMMUNICATION_STYLE = "JMSSampler.communicationStyle"; // $NON-NLS-1$

    private static final String JMS_PROPERTIES = "arguments"; // $NON-NLS-1$

    private static final String RECEIVE_QUEUE = "JMSSampler.ReceiveQueue"; // $NON-NLS-1$

    private static final String XML_DATA = "HTTPSamper.xml_data"; // $NON-NLS-1$

    private static final String SEND_QUEUE = "JMSSampler.SendQueue"; // $NON-NLS-1$

    private static final String QUEUE_CONNECTION_FACTORY_JNDI = "JMSSampler.queueconnectionfactory"; // $NON-NLS-1$

    private static final String IS_NON_PERSISTENT = "JMSSampler.isNonPersistent"; // $NON-NLS-1$

    private static final String USE_REQ_MSGID_AS_CORRELID = "JMSSampler.useReqMsgIdAsCorrelId"; // $NON-NLS-1$

    private static final String USE_RES_MSGID_AS_CORRELID = "JMSSampler.useResMsgIdAsCorrelId"; // $NON-NLS-1$

    private static final boolean USE_RES_MSGID_AS_CORRELID_DEFAULT = false; // Default
                                                                            // to
                                                                            // be
                                                                            // applied

    public enum COMMUNICATION_STYLE {
        ONE_WAY(0),
        REQUEST_REPLY(1),
        READ(2),
        BROWSE(3),
        CLEAR(4);

        private final int value;
        COMMUNICATION_STYLE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // --

    // Should we use java.naming.security.[principal|credentials] to create the
    // QueueConnection?
    private static final boolean USE_SECURITY_PROPERTIES = JMeterUtils
            .getPropDefault("JMSSampler.useSecurity.properties", true); // $NON-NLS-1$

    //
    // Member variables
    //
    /** Queue for receiving messages (if applicable). */
    private transient Queue receiveQueue;

    /** Queue for sending messages. */
    private transient Queue sendQueue;

    /** The session with the queueing system. */
    private transient QueueSession session;

    /** Connection to the queueing system. */
    private transient QueueConnection connection;

    /** The executor for (pseudo) synchronous communication. */
    private transient QueueExecutor executor;

    /** Producer of the messages. */
    private transient QueueSender producer;

    private transient Receiver receiverThread = null;

    private transient Throwable thrown = null;

    private transient Context context = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry entry) {
        JMeterContext jmeterContext = JMeterContextService.getContext();

        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSamplerData(getContent());
        res.setSuccessful(false); // Assume failure
        res.setDataType(SampleResult.TEXT);
        res.sampleStart();

        try {
            LOGGER.debug("Point-to-point mode: {}", getCommunicationstyle());
            if (isBrowse()) {
                handleBrowse(res);
            } else if (isClearQueue()) {
                handleClearQueue(res);
            } else if (isOneway()) {
                handleOneWay(res);
            } else if (isRead()) {
                handleRead(jmeterContext, res);
            } else {
                handleRequestResponse(res);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            if (thrown != null) {
                res.setResponseMessage(thrown.toString());
            } else {
                res.setResponseMessage(e.getLocalizedMessage());
            }
        }
        res.sampleEnd();
        return res;
    }

    private void handleBrowse(SampleResult res) throws JMSException {
        LOGGER.debug("isBrowseOnly");
        StringBuilder sb = new StringBuilder(75);
        res.setSuccessful(true);
        sb.append("Browse message on Send Queue ").append(sendQueue.getQueueName())
            .append(": ")
            .append(browseQueueDetails(sendQueue, res));
        res.setResponseData(sb.toString().getBytes());
        res.setResponseCodeOK();
    }

    private void handleClearQueue(SampleResult res) throws JMSException {
        LOGGER.debug("isClearQueue");
        StringBuilder sb = new StringBuilder(75);
        res.setSuccessful(true);
        sb.append("Clear messages on Send Queue ").append(sendQueue.getQueueName())
                .append(": ")
                .append(clearQueue(sendQueue, res));
        res.setResponseData(sb.toString().getBytes());
        res.setResponseCodeOK();
    }

    private void handleOneWay(SampleResult res) throws JMSException {
        LOGGER.debug("isOneWay");
        TextMessage msg = createMessage();
        int deliveryMode = isNonPersistent() ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
        producer.send(msg, deliveryMode, Integer.parseInt(getPriority()), Long.parseLong(getExpiration()));
        res.setRequestHeaders(Utils.messageProperties(msg));
        res.setResponseOK();
        res.setResponseData("Oneway request has no response data", null);
    }

    private void handleRead(JMeterContext context, SampleResult res) {
        LOGGER.debug("isRead");
        StringBuilder sb = new StringBuilder(75);
        res.setSuccessful(true);
        Sampler sampler = context.getPreviousSampler();
        SampleResult sr = context.getPreviousResult();
        String jmsSelector = getJMSSelector();
        if (JMSSampler.PREV_SAMPLER.equals(jmsSelector)
                && (sampler instanceof JMSSampler)) {
            jmsSelector = sr.getResponseMessage();
        }
        int sampleCounter = 0;
        int sampleTries = 0;
        String result;

        StringBuilder buffer = new StringBuilder();
        StringBuilder propBuffer = new StringBuilder();

        do {
            result = browseQueueForConsumption(sendQueue, jmsSelector, res, buffer, propBuffer);
            if (result != null) {
                sb.append(result);
                sb.append('\n');
                sampleCounter++;
            }
            sampleTries++;
        } while ((result != null) && (sampleTries < getNumberOfSamplesToAggregateAsInt()));

        res.setResponseData(buffer.toString(), StandardCharsets.UTF_8.name());
        res.setResponseHeaders(propBuffer.toString());
        if (sampleCounter == 0) {
            res.setSuccessful(false);
            res.setResponseCode("404");
            res.setResponseMessage(sampleCounter + " samples messages received, last try had following response message:"+
                    res.getResponseMessage());
        } else {
            res.setSuccessful(true);
            res.setResponseCodeOK();
            res.setResponseMessage(sampleCounter + " message(s) received successfully");
        }
        res.setSamplerData(getNumberOfSamplesToAggregateAsInt() + " messages expected");
        res.setSampleCount(sampleCounter);
    }

    private void handleRequestResponse(SampleResult res) throws JMSException {
        TextMessage msg = createMessage();
        if (!useTemporyQueue()) {
            LOGGER.debug("NO TEMP QUEUE");
            msg.setJMSReplyTo(receiveQueue);
        }
        LOGGER.debug("Create temp message");
        Message replyMsg = executor.sendAndReceive(msg,
                isNonPersistent() ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT,
                Integer.parseInt(getPriority()), Long.parseLong(getExpiration()));
        res.setRequestHeaders(Utils.messageProperties(msg));
        if (replyMsg == null) {
            res.setResponseMessage("No reply message received");
        } else {
            if (replyMsg instanceof TextMessage) {
                res.setResponseData(((TextMessage) replyMsg).getText(), null);
            } else {
                res.setResponseData(replyMsg.toString(), null);
            }
            res.setResponseHeaders(Utils.messageProperties(replyMsg));
            res.setResponseOK();
        }
    }

    private String browseQueueForConsumption(Queue queue,
            String jmsSelector,
            SampleResult res,
            StringBuilder buffer,
            StringBuilder propBuffer) {
        String retVal = null;
        QueueReceiver consumer = null;
        Message reply = null;
        String queueName = null;
        try {
            queueName = queue.getQueueName();
            consumer = session.createReceiver(queue, jmsSelector);
            reply = consumer.receive(getTimeoutAsInt());
            LOGGER.debug("Message: {}", reply);
            if (reply != null) {
                res.setResponseMessage("1 message received successfully");
                res.setResponseHeaders(reply.toString());
                TextMessage msg = (TextMessage) reply;
                retVal = msg.getText();
                extractContent(buffer, propBuffer, msg);
            } else {
                res.setResponseMessage("No message received");
            }
        } catch (Exception ex) {
            res.setResponseMessage("Error browsing queue '"+queueName+"' with selector '"
                    + jmsSelector+ "', timeout '"+getTimeout()+"', message:"+ex.getMessage());
            LOGGER.error("Error browsing queue {} with selector {} and configured timeout {}", queueName, jmsSelector,
                    getTimeout(), ex);
        } finally {
            Utils.close(consumer, LOGGER);
        }
        return retVal;
    }

    private void extractContent(StringBuilder buffer, StringBuilder propBuffer, Message msg) {
        if (msg != null) {
            try {
                if (msg instanceof TextMessage) {
                    buffer.append(((TextMessage) msg).getText());
                } else if (msg instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) msg;
                    if (objectMessage.getObject() != null) {
                        buffer.append(objectMessage.getObject().getClass());
                    } else {
                        buffer.append("object is null");
                    }
                } else if (msg instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) msg;
                    buffer.append(bytesMessage.getBodyLength() + " bytes received in BytesMessage");
                } else if (msg instanceof MapMessage) {
                    MapMessage mapm = (MapMessage) msg;
                    @SuppressWarnings("unchecked") // MapNames are Strings
                    Enumeration<String> enumb = mapm.getMapNames();
                    while (enumb.hasMoreElements()) {
                        String name = enumb.nextElement();
                        Object obj = mapm.getObject(name);
                        buffer.append(name);
                        buffer.append(",");
                        buffer.append(obj.getClass().getCanonicalName());
                        buffer.append(",");
                        buffer.append(obj);
                        buffer.append("\n");
                    }
                }
                Utils.messageProperties(propBuffer, msg);
            } catch (JMSException e) {
                buffer.append("Error extracting content from message:"+e.getMessage());
                LOGGER.error("Error extracting content from message", e);
            }
        }
    }

    private String browseQueueDetails(Queue queue, SampleResult res) {
        String queueName = null;
        try {
            queueName = queue.getQueueName();
            StringBuilder messageBodies = new StringBuilder(150);
            messageBodies.append("==== Browsing Messages ===\n");
            // get some queue details
            QueueBrowser qBrowser = session.createBrowser(queue);
            // browse the messages
            Enumeration<?> e = qBrowser.getEnumeration();
            int numMsgs = 0;
            // count number of messages
            String corrID;
            while (e.hasMoreElements()) {
                TextMessage message = (TextMessage) e.nextElement();
                corrID = message.getJMSCorrelationID();
                if (corrID == null) {
                    corrID = message.getJMSMessageID();
                    messageBodies.append(numMsgs)
                        .append(" - MessageID: ")
                        .append(corrID)
                        .append(": ").append(message.getText())
                        .append("\n");
                } else {
                    messageBodies.append(numMsgs)
                    .append(" - CorrelationID: ")
                    .append(corrID)
                    .append(": ")
                    .append(message.getText())
                    .append("\n");
                }
                numMsgs++;
            }
            res.setResponseMessage(numMsgs + " messages available on the queue");
            res.setResponseHeaders(qBrowser.toString());
            return messageBodies + queue.getQueueName() + " has " + numMsgs + " messages";
        } catch (Exception e) {
            res.setResponseMessage("Error counting message on the queue");
            LOGGER.error("Error browsing messages on the queue {}", queueName, e);
            return "Error browsing messages on the queue, message "+ e.getMessage();
        }
    }

    private String clearQueue(Queue queue, SampleResult res) {
        String retVal = null;
        QueueReceiver consumer = null;
        String queueName = null;
        try {
            queueName = queue.getQueueName();
            consumer = session.createReceiver(queue);
            Message deletedMsg = null;
            long deletedMsgCount = 0;
            do {
                deletedMsg = consumer.receive(getTimeoutAsInt());
                if (deletedMsg != null) {
                    deletedMsgCount++;
                    deletedMsg.acknowledge();
                }
            } while (deletedMsg != null);
            retVal = deletedMsgCount + " message(s) removed using receive timeout:"+
                    getTimeoutAsInt()+"ms";
            res.setResponseMessage(retVal);
        } catch (Exception ex) {
            res.setResponseMessage("Error clearing queue:"+queueName);
            LOGGER.error("Error clearing queue {}", queueName, ex);
            return "Error clearing queue "+queueName+", message:" +ex.getMessage();
        } finally {
            Utils.close(consumer, LOGGER);
        }
        return retVal;
    }

    private TextMessage createMessage() throws JMSException {
        if (session == null) {
            throw new IllegalStateException("Session may not be null while creating message");
        }
        TextMessage msg = session.createTextMessage();
        msg.setText(getContent());
        addJMSProperties(msg);
        return msg;
    }

    private void addJMSProperties(TextMessage msg) throws JMSException {
        Utils.addJMSProperties(msg, getJMSProperties().getJmsPropertysAsMap());
    }

    /**
     * @return {@link JMSProperties} JMS Properties
     */
    public JMSProperties getJMSProperties() {
        Object o = getProperty(JMS_PROPERTIES).getObjectValue();
        JMSProperties jmsProperties = null;
        // Backward compatibility with versions <= 2.10
        if (o instanceof Arguments) {
            jmsProperties = Utils.convertArgumentsToJmsProperties((Arguments) o);
        } else {
            jmsProperties = (JMSProperties) o;
        }
        if (jmsProperties == null) {
            jmsProperties = new JMSProperties();
            setJMSProperties(jmsProperties);
        }
        return jmsProperties;
    }

    /**
     * @param jmsProperties
     *            JMS Properties
     */
    public void setJMSProperties(JMSProperties jmsProperties) {
        setProperty(new TestElementProperty(JMS_PROPERTIES, jmsProperties));
    }

    public Arguments getJNDIProperties() {
        return getArguments(JMSSampler.JNDI_PROPERTIES);
    }

    public void setJNDIProperties(Arguments args) {
        setProperty(new TestElementProperty(JMSSampler.JNDI_PROPERTIES, args));
    }

    public String getQueueConnectionFactory() {
        return getPropertyAsString(QUEUE_CONNECTION_FACTORY_JNDI);
    }

    public void setQueueConnectionFactory(String qcf) {
        setProperty(QUEUE_CONNECTION_FACTORY_JNDI, qcf);
    }

    public String getSendQueue() {
        return getPropertyAsString(SEND_QUEUE);
    }

    public void setSendQueue(String name) {
        setProperty(SEND_QUEUE, name);
    }

    public String getReceiveQueue() {
        return getPropertyAsString(RECEIVE_QUEUE);
    }

    public void setReceiveQueue(String name) {
        setProperty(RECEIVE_QUEUE, name);
    }

    public String getContent() {
        return getPropertyAsString(XML_DATA);
    }

    public void setContent(String content) {
        setProperty(XML_DATA, content);
    }

    public boolean isOneway() {
        return COMMUNICATION_STYLE.ONE_WAY.value == getPropertyAsInt(JMS_COMMUNICATION_STYLE);
    }

    public boolean isRead() {
        return COMMUNICATION_STYLE.READ.value == getPropertyAsInt(JMS_COMMUNICATION_STYLE);
    }

    public boolean isBrowse() {
        return COMMUNICATION_STYLE.BROWSE.value == getPropertyAsInt(JMS_COMMUNICATION_STYLE);
    }

    public boolean isClearQueue() {
        return COMMUNICATION_STYLE.CLEAR.value == getPropertyAsInt(JMS_COMMUNICATION_STYLE);
    }

    public boolean isNonPersistent() {
        return getPropertyAsBoolean(IS_NON_PERSISTENT);
    }

    /**
     * Which request field to use for correlation?
     *
     * @return true if correlation should use the request JMSMessageID rather
     *         than JMSCorrelationID
     */
    public boolean isUseReqMsgIdAsCorrelId() {
        return getPropertyAsBoolean(USE_REQ_MSGID_AS_CORRELID);
    }

    /**
     * Which response field to use for correlation?
     *
     * @return true if correlation should use the response JMSMessageID rather
     *         than JMSCorrelationID
     */
    public boolean isUseResMsgIdAsCorrelId() {
        return getPropertyAsBoolean(USE_RES_MSGID_AS_CORRELID, USE_RES_MSGID_AS_CORRELID_DEFAULT);
    }

    public String getInitialContextFactory() {
        return getPropertyAsString(JMSSampler.JNDI_INITIAL_CONTEXT_FACTORY);
    }

    public String getContextProvider() {
        return getPropertyAsString(JMSSampler.JNDI_CONTEXT_PROVIDER_URL);
    }

    public int getCommunicationstyle() {
        JMeterProperty prop = getProperty(JMS_COMMUNICATION_STYLE);
        return Integer.parseInt(prop.getStringValue());
    }

    public String getCommunicationstyleString() {
        return getPropertyAsString(JMS_COMMUNICATION_STYLE);
    }

    public void setCommunicationstyle(int communicationStyle) {
        setProperty(new IntegerProperty(JMS_COMMUNICATION_STYLE, communicationStyle));
    }

    public void setIsOneway(boolean isOneway) {
        setProperty(new IntegerProperty(JMS_COMMUNICATION_STYLE,
                isOneway ?
                        COMMUNICATION_STYLE.ONE_WAY.value :
                            COMMUNICATION_STYLE.REQUEST_REPLY.value));
    }

    public void setNonPersistent(boolean value) {
        setProperty(new BooleanProperty(IS_NON_PERSISTENT, value));
    }

    public void setUseReqMsgIdAsCorrelId(boolean value) {
        setProperty(new BooleanProperty(USE_REQ_MSGID_AS_CORRELID, value));
    }

    public void setUseResMsgIdAsCorrelId(boolean value) {
        setProperty(USE_RES_MSGID_AS_CORRELID, value, USE_RES_MSGID_AS_CORRELID_DEFAULT);
    }

    @Override
    public String toString() {
        return getQueueConnectionFactory() + ", queue: " + getSendQueue();
    }

    @Override
    public void threadStarted() {
        logThreadStart();

        thrown = null;
        try {
            context = getInitialContext();
            Object obj = context.lookup(getQueueConnectionFactory());
            if (!(obj instanceof QueueConnectionFactory)) {
                String msg = "QueueConnectionFactory expected, but got "
                        + (obj != null ? obj.getClass().getName() : "null");
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
            QueueConnectionFactory factory = (QueueConnectionFactory) obj;
            sendQueue = (Queue) context.lookup(getSendQueue());

            if (!useTemporyQueue()) {
                receiveQueue = (Queue) context.lookup(getReceiveQueue());
                receiverThread = Receiver.createReceiver(factory, receiveQueue,
                        Utils.getFromEnvironment(context, Context.SECURITY_PRINCIPAL),
                        Utils.getFromEnvironment(context, Context.SECURITY_CREDENTIALS), isUseResMsgIdAsCorrelId(),
                        getJMSSelector());
            }

            String principal = null;
            String credentials = null;
            if (USE_SECURITY_PROPERTIES) {
                principal = Utils.getFromEnvironment(context, Context.SECURITY_PRINCIPAL);
                credentials = Utils.getFromEnvironment(context, Context.SECURITY_CREDENTIALS);
            }
            if (principal != null && credentials != null) {
                connection = factory.createQueueConnection(principal, credentials);
            } else {
                connection = factory.createQueueConnection();
            }

            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            LOGGER.debug("Session created");

            if (!(isBrowse() || isRead() || isClearQueue())) {
                if (isOneway()) {
                    producer = session.createSender(sendQueue);
                    if (isNonPersistent()) {
                        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    }
                    producer.setPriority(Integer.parseInt(getPriority()));
                    producer.setTimeToLive(Long.parseLong(getExpiration()));
                } else {
                    if (useTemporyQueue()) {
                        executor = new TemporaryQueueExecutor(session, sendQueue, getTimeoutAsInt());
                    } else {
                        producer = session.createSender(sendQueue);
                        executor = new FixedQueueExecutor(producer, getTimeoutAsInt(), isUseReqMsgIdAsCorrelId());
                    }
                }
            }
            LOGGER.debug("Starting connection");

            connection.start();

            LOGGER.debug("Connection started");
        } catch (Exception | NoClassDefFoundError e) {
            thrown = e;
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    private Context getInitialContext() throws NamingException {
        Hashtable<String, String> table = new Hashtable<>();

        if (getInitialContextFactory() != null && getInitialContextFactory().trim().length() > 0) {
            LOGGER.debug("Using InitialContext [{}]", getInitialContextFactory());
            table.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        }
        if (getContextProvider() != null && getContextProvider().trim().length() > 0) {
            LOGGER.debug("Using Provider [{}]", getContextProvider());
            table.put(Context.PROVIDER_URL, getContextProvider());
        }
        Map<String, String> map = getArguments(JMSSampler.JNDI_PROPERTIES).getArgumentsAsMap();
        if (LOGGER.isDebugEnabled()) {
            if (map.isEmpty()) {
                LOGGER.debug("Empty JNDI properties");
            } else {
                LOGGER.debug("Number of JNDI properties: {}", map.size());
            }
        }
        for (Map.Entry<String, String> me : map.entrySet()) {
            table.put(me.getKey(), me.getValue());
        }

        Context initialContext = new InitialContext(table);
        if (LOGGER.isDebugEnabled()) {
            printEnvironment(initialContext);
        }
        return initialContext;
    }

    private void printEnvironment(Context context) throws NamingException {
        try {
            Hashtable<?, ?> env = context.getEnvironment();
            if (env != null) {
                LOGGER.debug("Initial Context Properties");
                for (Map.Entry<?, ?> entry : env.entrySet()) {
                    LOGGER.debug("{}={}", entry.getKey(), entry.getValue());
                }
            } else {
                LOGGER.warn(
                        "context.getEnvironment() returned null (should not happen according to javadoc but non compliant implementation can return this)");
            }
        } catch (javax.naming.OperationNotSupportedException ex) {
            // Some JNDI implementation can return this
            LOGGER.warn("context.getEnvironment() not supported by implementation ");
        }
    }

    private void logThreadStart() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Thread started {}", new Date());
            LOGGER.debug("JMSSampler: [{}], hashCode=[{}]", Thread.currentThread().getName(), hashCode());
            LOGGER.debug("QCF: [{}], sendQueue=[{}]", getQueueConnectionFactory(), getSendQueue());
            LOGGER.debug("Timeout = [{}]", getTimeout());
            LOGGER.debug("Use temporary queue =[{}]", useTemporyQueue());
            LOGGER.debug("Reply queue         = [{}]", getReceiveQueue());
        }
    }

    private int getTimeoutAsInt() {
        String propAsString = getPropertyAsString(TIMEOUT);
        if(StringUtils.isEmpty(propAsString)){
            return DEFAULT_TIMEOUT;
        } else {
            return Integer.parseInt(propAsString);
        }
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT, DEFAULT_TIMEOUT_STRING);
    }

    public String getExpiration() {
        String expiration = getPropertyAsString(JMS_EXPIRATION);
        if (expiration.length() == 0) {
            return Utils.DEFAULT_NO_EXPIRY;
        } else {
            return expiration;
        }
    }

    public String getPriority() {
        String priority = getPropertyAsString(JMS_PRIORITY);
        if (priority.length() == 0) {
            return Utils.DEFAULT_PRIORITY_4;
        } else {
            return priority;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        LOGGER.debug("Thread ended {}", new Date());

        if (context != null) {
            try {
                context.close();
            } catch (NamingException ignored) {
                // ignore
            }
        }
        if (executor != null) {
            try {
                executor.close();
            } catch (JMSException e) {
                LOGGER.error("Error closing executor {}", executor.getClass(), e);
            }
        }
        Utils.close(session, LOGGER);
        Utils.close(connection, LOGGER);
        if (receiverThread != null) {
            receiverThread.deactivate();
        }
    }

    private boolean useTemporyQueue() {
        String recvQueue = getReceiveQueue();
        return recvQueue == null || recvQueue.trim().length() == 0;
    }

    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(JMSSampler.JMS_PROPERTIES, args));
    }

    public Arguments getArguments(String name) {
        return (Arguments) getProperty(name).getObjectValue();
    }

    public void setTimeout(String s) {
        setProperty(JMSSampler.TIMEOUT, s);
    }

    public void setPriority(String s) {
        setProperty(JMSSampler.JMS_PRIORITY, s, Utils.DEFAULT_PRIORITY_4);
    }

    public void setExpiration(String s) {
        setProperty(JMSSampler.JMS_EXPIRATION, s, Utils.DEFAULT_NO_EXPIRY);
    }

    /**
     * @return String JMS Selector
     */
    public String getJMSSelector() {
        return getPropertyAsString(JMSSampler.JMS_SELECTOR, JMS_SELECTOR_DEFAULT);
    }

    /**
     * @param selector
     *            String selector
     */
    public void setJMSSelector(String selector) {
        setProperty(JMSSampler.JMS_SELECTOR, selector, JMS_SELECTOR_DEFAULT);
    }

    public String getNumberOfSamplesToAggregate() {
        return getPropertyAsString(JMSSampler.JMS_NUMBEROFSAMPLES, JMS_NUMBEROFSAMPLES_DEFAULT);
    }

    public void setNumberOfSamplesToAggregate(String selector) {
        setProperty(JMSSampler.JMS_NUMBEROFSAMPLES, selector, JMS_NUMBEROFSAMPLES_DEFAULT);
    }

    private int getNumberOfSamplesToAggregateAsInt() {
        int val = 1;
        try {
            val = getPropertyAsInt(JMS_NUMBEROFSAMPLES);
        } catch (Exception e) { // NOSONAR
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed parsing number of samples to aggregate");
            }
            val = 1;
        }
        if (val < 1) {
            val = 1;
        }
        return val;
    }

    /**
     * @param string
     *            name of the initial context factory to use
     */
    public void setInitialContextFactory(String string) {
        setProperty(JNDI_INITIAL_CONTEXT_FACTORY, string);
    }

    /**
     * @param string
     *            url of the provider
     */
    public void setContextProvider(String string) {
        setProperty(JNDI_CONTEXT_PROVIDER_URL, string);
    }
}
