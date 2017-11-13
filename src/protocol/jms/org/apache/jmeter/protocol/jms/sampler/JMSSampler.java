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

	private static final Logger LOGGER = LoggerFactory.getLogger(JMSSampler.class);

	private static final long serialVersionUID = 233L;

	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

	// ++ These are JMX names, and must not be changed
	private static final String JNDI_INITIAL_CONTEXT_FACTORY = "JMSSampler.initialContextFactory"; // $NON-NLS-1$

	private static final String JNDI_CONTEXT_PROVIDER_URL = "JMSSampler.contextProviderUrl"; // $NON-NLS-1$

	private static final String JNDI_PROPERTIES = "JMSSampler.jndiProperties"; // $NON-NLS-1$

	private static final String TIMEOUT = "JMSSampler.timeout"; // $NON-NLS-1$

	private static final String JMS_PRIORITY = "JMSSampler.priority"; // $NON-NLS-1$

	private static final String JMS_EXPIRATION = "JMSSampler.expiration"; // $NON-NLS-1$

	private static final String JMS_SELECTOR = "JMSSampler.jmsSelector"; // $NON-NLS-1$

	private static final String JMS_SELECTOR_DEFAULT = ""; // $NON-NLS-1$

	private static final String JMS_NUMBEROFSAMPLES = "JMSSampler.jmsNumberOfSamplesToAggregate"; // $NON-NLS-1$

	private static final String JMS_NUMBEROFSAMPLES_DEFAULT = "1"; // $NON-NLS-1$

	public static final String COMMUNICATIONSTYLE = "JMSSampler.communicationstyle"; // $NON-NLS-1$

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

	private static final int ONE_WAY = 0;

	private static final int READ = 2;

	private static final int BROWSE = 3;

	private static final int CLEAR = 4;

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
	private Queue sendQueue;

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
		SampleResult sampleResult;
		try {
			LOGGER.debug("Point-to-point mode: " + getCommunicationstyle());
			if (isBrowse()) {
				sampleResult = handleBrowse();
			} else if (isClearQueue()) {
				sampleResult = handleClearQueue();
			} else if (isOneway()) {
				sampleResult = handleOneWay();
			} else if (isRead()) {
				sampleResult = handleRead();
			} else {
				sampleResult = handleRequestResponse();
			}
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
			sampleResult = constructSampleResult(false);
			if (thrown != null) {
				sampleResult.setResponseMessage(thrown.toString());
			} else {
				sampleResult.setResponseMessage(e.getLocalizedMessage());
			}
		}
		sampleResult.sampleEnd();
		return sampleResult;
	}

	private SampleResult constructSampleResult(boolean successfull) {
		SampleResult sampleResult = new SampleResult();
		sampleResult.setSampleLabel(getName());
		sampleResult.setSamplerData(getContent());
		sampleResult.setSuccessful(successfull);
		sampleResult.setDataType(SampleResult.TEXT);
		sampleResult.sampleStart();
		return sampleResult;
	}
	
	private SampleResult handleBrowse() throws JMSException {
		LOGGER.debug("handle browse");
		StringBuilder sb = new StringBuilder();
		SampleResult sampleResult = constructSampleResult(true);
		sb.append("\n \n  Browse message on Send Queue " + sendQueue.getQueueName());
		sb.append(browseQueueDetails(sendQueue, sampleResult));
		sampleResult.setResponseData(sb.toString().getBytes());
		return sampleResult;
	}

	private SampleResult handleClearQueue() throws JMSException {
		LOGGER.debug("handle clear queue");
		StringBuilder sb = new StringBuilder();
		SampleResult sampleResult = constructSampleResult(true);
		sb.append("\n \n  Clear messages on Send Queue " + sendQueue.getQueueName());
		sb.append(clearQueue(sendQueue, sampleResult));
		sampleResult.setResponseData(sb.toString().getBytes());
		return sampleResult;
	}

	private SampleResult handleOneWay() throws JMSException {
		LOGGER.debug("handle one way");
		TextMessage msg = createMessage();
		int deliveryMode = isNonPersistent() ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT;
		producer.send(msg, deliveryMode, Integer.parseInt(getPriority()), Long.parseLong(getExpiration()));
		SampleResult sampleResult = constructSampleResult(false);
		sampleResult.setRequestHeaders(Utils.messageProperties(msg));
		sampleResult.setResponseOK();
		sampleResult.setResponseData("Oneway request has no response data", null);
		return sampleResult;
	}

	private SampleResult handleRead() {
		LOGGER.debug("handle read");
		JMeterContext jmeterContext = JMeterContextService.getContext();
		Sampler previousSampler = jmeterContext.getPreviousSampler();
		SampleResult previousSampleResult = jmeterContext.getPreviousResult();
		String jmsSelector = getJMSSelector();
		if ("_PREV_SAMPLER_".equals(jmsSelector) && previousSampler instanceof JMSSampler) {
			jmsSelector = previousSampleResult.getResponseMessage();
		}
		return processNumberOfSamplesFromQueue(jmsSelector);
	}

	private SampleResult processNumberOfSamplesFromQueue(String jmsSelector) {
		int sampleCounter = 0;
		int sampleTries = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder messageBuilder = new StringBuilder();
		StringBuilder propertiesBuilder = new StringBuilder();
		SampleResult sampleResult = constructSampleResult(true);

		String result;
		do {
			result = browseQueueForConsumption(jmsSelector, sampleResult, messageBuilder, propertiesBuilder);
			if (result != null) {
				sb.append(result);
				sb.append('\n');
				sampleCounter++;
			}
			sampleTries++;
		} while ((result != null) && (sampleTries < getNumberOfSamplesToAggregateAsInt()));

		sampleResult.setResponseMessage(sampleCounter + " samples messages received");
		sampleResult.setResponseData(messageBuilder.toString().getBytes());
		sampleResult.setResponseHeaders(propertiesBuilder.toString());
		if (sampleCounter == 0) {
			sampleResult.setResponseCode("404");
			sampleResult.setSuccessful(false);
		} else {
			sampleResult.setResponseCodeOK();
			sampleResult.setSuccessful(true);
		}
		sampleResult.setResponseMessage(sampleCounter + " message(s) received successfully");
		sampleResult.setSamplerData(getNumberOfSamplesToAggregateAsInt() + " messages expected");
		sampleResult.setSampleCount(sampleCounter);
		return sampleResult;
	}

	private String browseQueueForConsumption(String jmsSelector, SampleResult sampleResult, StringBuilder messageBuilder,
			StringBuilder propertiesBuilder) {
		String returnValue = null;
		try {
			QueueReceiver consumer = session.createReceiver(sendQueue, jmsSelector);
			Message reply = consumer.receive(Long.valueOf(getTimeout()));
			LOGGER.debug("Message: " + reply);
			consumer.close();
			if (reply != null) {
				sampleResult.setResponseMessage("1 message(s) received successfully");
				sampleResult.setResponseHeaders(reply.toString());
				TextMessage message = (TextMessage) reply;
				returnValue = message.getText();
				extractContent(messageBuilder, propertiesBuilder, message);
			} else {
				sampleResult.setResponseMessage("No message received");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.error(ex.getMessage());
		}
		return returnValue;
	}
	
	private SampleResult handleRequestResponse() throws JMSException {
		TextMessage msg = createMessage();
		if (!useTemporyQueue()) {
			LOGGER.debug("NO TEMP QUEUE");
			msg.setJMSReplyTo(receiveQueue);
		}
		LOGGER.debug("Create temp message");
		Message replyMsg = executor.sendAndReceive(msg,
				isNonPersistent() ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT,
				Integer.parseInt(getPriority()), Long.parseLong(getExpiration()));
		SampleResult sampleResult = constructSampleResult(false);
		sampleResult.setRequestHeaders(Utils.messageProperties(msg));
		if (replyMsg == null) {
			sampleResult.setResponseMessage("No reply message received");
		} else {
			if (replyMsg instanceof TextMessage) {
				sampleResult.setResponseData(((TextMessage) replyMsg).getText(), null);
			} else {
				sampleResult.setResponseData(replyMsg.toString(), null);
			}
			sampleResult.setResponseHeaders(Utils.messageProperties(replyMsg));
			sampleResult.setResponseOK();
		}
		return sampleResult;
	}

	private void extractContent(StringBuilder messageBuilder, StringBuilder propertiesBuilder, Message message) {
		if (message != null) {
			try {
				if (message instanceof TextMessage) {
					messageBuilder.append(((TextMessage) message).getText());
				} else if (message instanceof ObjectMessage) {
					ObjectMessage objectMessage = (ObjectMessage) message;
					if (objectMessage.getObject() != null) {
						messageBuilder.append(objectMessage.getObject().getClass());
					} else {
						messageBuilder.append("object is null");
					}
				} else if (message instanceof BytesMessage) {
					BytesMessage bytesMessage = (BytesMessage) message;
					messageBuilder.append(bytesMessage.getBodyLength() + " bytes received in BytesMessage");
				} else if (message instanceof MapMessage) {
					MapMessage mapm = (MapMessage) message;
					@SuppressWarnings("unchecked") // MapNames are Strings
					Enumeration<String> enumb = mapm.getMapNames();
					while (enumb.hasMoreElements()) {
						String name = enumb.nextElement();
						Object obj = mapm.getObject(name);
						messageBuilder.append(name);
						messageBuilder.append(",");
						messageBuilder.append(obj.getClass().getCanonicalName());
						messageBuilder.append(",");
						messageBuilder.append(obj);
						messageBuilder.append("\n");
					}
				}
				Utils.messageProperties(propertiesBuilder, message);
			} catch (JMSException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	private String browseQueueDetails(Queue queue, SampleResult res) {
		try {
			String messageBodies = new String("\n==== Browsing Messages === \n");
			// get some queue details
			QueueBrowser qBrowser = session.createBrowser(queue);
			// browse the messages
			Enumeration<?> e = qBrowser.getEnumeration();
			int numMsgs = 0;
			// count number of messages
			String corrID = "";
			while (e.hasMoreElements()) {
				TextMessage message = (TextMessage) e.nextElement();
				corrID = message.getJMSCorrelationID();
				if (corrID == null) {
					corrID = message.getJMSMessageID();
					messageBodies = messageBodies + numMsgs + " - MessageID: " + corrID + ": " + message.getText()
							+ "\n";
				} else {
					messageBodies = messageBodies + numMsgs + " - CorrelationID: " + corrID + ": " + message.getText()
							+ "\n";
				}
				numMsgs++;
			}
			res.setResponseMessage(numMsgs + " messages available on the queue");
			res.setResponseHeaders(qBrowser.toString());
			return (messageBodies + queue.getQueueName() + " has " + numMsgs + " messages");
		} catch (Exception e) {
			res.setResponseMessage("Error counting message on the queue");
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			return "";
		}
	}

	private String clearQueue(Queue queue, SampleResult res) {
		String retVal = null;
		try {
			QueueReceiver consumer = session.createReceiver(queue);
			Message deletedMsg = null;
			long deletedMsgCount = 0;
			do {
				deletedMsg = consumer.receiveNoWait();
				if (deletedMsg != null) {
					deletedMsgCount++;
					deletedMsg.acknowledge();
				}
			} while (deletedMsg != null);
			retVal = deletedMsgCount + " message(s) removed";
			res.setResponseMessage(retVal);
		} catch (Exception ex) {
			res.setResponseMessage("Error clearing queue");
			ex.printStackTrace();
			LOGGER.error(ex.getMessage());
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
		return ONE_WAY == getPropertyAsInt(COMMUNICATIONSTYLE);
	}

	public boolean isRead() {
		return READ == getPropertyAsInt(COMMUNICATIONSTYLE);
	}

	public boolean isBrowse() {
		return BROWSE == getPropertyAsInt(COMMUNICATIONSTYLE);
	}

	public boolean isClearQueue() {
		return CLEAR == getPropertyAsInt(COMMUNICATIONSTYLE);
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
		JMeterProperty prop = getProperty(COMMUNICATIONSTYLE);
		return Integer.parseInt(prop.getStringValue());
	}

	public String getCommunicationstyleString() {
		return getPropertyAsString(COMMUNICATIONSTYLE);
	}

	public void setCommunicationstyle(int communicationStyle) {
		setProperty(new IntegerProperty(COMMUNICATIONSTYLE, communicationStyle));
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

			if (isBrowse() || isRead() || isClearQueue()) {
				// Do nothing!
			} else if (isOneway()) {
				producer = session.createSender(sendQueue);
				if (isNonPersistent()) {
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				}
				producer.setPriority(Integer.parseInt(getPriority()));
				producer.setTimeToLive(Long.parseLong(getExpiration()));
			} else {
				if (useTemporyQueue()) {
					executor = new TemporaryQueueExecutor(session, sendQueue);
				} else {
					producer = session.createSender(sendQueue);
					executor = new FixedQueueExecutor(producer, getTimeoutAsInt(), isUseReqMsgIdAsCorrelId());
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

		Context context = new InitialContext(table);
		if (LOGGER.isDebugEnabled()) {
			printEnvironment(context);
		}
		return context;
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
			LOGGER.debug("Thread started " + new Date());
			LOGGER.debug("JMSSampler: [" + Thread.currentThread().getName() + "], hashCode=[" + hashCode() + "]");
			LOGGER.debug("QCF: [" + getQueueConnectionFactory() + "], sendQueue=[" + getSendQueue() + "]");
			LOGGER.debug("Timeout             = " + getTimeout() + "]");
			LOGGER.debug("Use temporary queue =" + useTemporyQueue() + "]");
			LOGGER.debug("Reply queue         =" + getReceiveQueue() + "]");
		}
	}

	private int getTimeoutAsInt() {
		if (getPropertyAsInt(TIMEOUT) < 1) {
			return DEFAULT_TIMEOUT;
		}
		return getPropertyAsInt(TIMEOUT);
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
		} catch (Exception e) {
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
