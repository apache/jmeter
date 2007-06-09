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
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Sampler for JMS Communication. <br>
 * Created on: October 28, 2004
 * 
 */
public class JMSSampler extends AbstractSampler implements ThreadListener {

	public static final String JNDI_INITIAL_CONTEXT_FACTORY = "JMSSampler.initialContextFactory"; // $NON-NLS-1$

	public static final String JNDI_CONTEXT_PROVIDER_URL = "JMSSampler.contextProviderUrl"; // $NON-NLS-1$

	public static final String JNDI_PROPERTIES = "JMSSampler.jndiProperties"; // $NON-NLS-1$

	private static final int DEFAULT_TIMEOUT = 2000;

	public final static String TIMEOUT = "JMSSampler.timeout"; // $NON-NLS-1$

	public static final String IS_ONE_WAY = "JMSSampler.isFireAndForget"; // $NON-NLS-1$

	public static final String JMS_PROPERTIES = "arguments"; // $NON-NLS-1$

	public static final String RECEIVE_QUEUE = "JMSSampler.ReceiveQueue"; // $NON-NLS-1$

	public static final String XML_DATA = "HTTPSamper.xml_data"; // $NON-NLS-1$

	public final static String SEND_QUEUE = "JMSSampler.SendQueue"; // $NON-NLS-1$

	public final static String QUEUE_CONNECTION_FACTORY_JNDI = "JMSSampler.queueconnectionfactory"; // $NON-NLS-1$

	private static final Logger LOGGER = LoggingManager.getLoggerForClass();

	//
	// Member variables
	//
	/** Factory for the connections to the queueing system. */
	// NOTUSED private QueueConnectionFactory factory;
	/** Queue for receiving messages (if applicable). */
	private transient Queue receiveQueue;

	/** The session with the queueing system. */
	private transient QueueSession session;

	/** Connection to the queueing system. */
	private transient QueueConnection connection;

	/** Queue for sending messages. */
	private transient Queue sendQueue;

	/** Is the communication oneway? */
	// NOTUSED private boolean oneway;
	/** The executor for (pseudo) synchronous communication. */
	private transient QueueExecutor executor;

	/** Producer of the messages. */
	private transient QueueSender producer;

	private transient Receiver receiverThread = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
	 */
	public SampleResult sample(Entry entry) {
		SampleResult res = new SampleResult();
		res.setSampleLabel(getName());
		res.setSamplerData(getContent());
		res.setDataType(SampleResult.TEXT);
		res.sampleStart();

		try {
			TextMessage msg = createMessage();

			if (isOneway()) {
				producer.send(msg);
				res.setSuccessful(true);
				res.setResponseData("Oneway request has no response data".getBytes());
			} else {
				if (!useTemporyQueue()) {
					msg.setJMSReplyTo(receiveQueue);
				}

				Message replyMsg = executor.sendAndReceive(msg);
				if (replyMsg == null) {
					res.setSuccessful(false);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("No reply message received");
					}
				} else {
					if (replyMsg instanceof TextMessage) {
						res.setResponseData(((TextMessage) replyMsg).getText().getBytes());
					} else {
						res.setResponseData(replyMsg.toString().getBytes());
					}
					res.setSuccessful(true);
				}
			}
		} catch (Exception e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
			res.setResponseData(new byte[0]);
			res.setSuccessful(false);
		}
		res.sampleEnd();
		return res;
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
		Map map = getArguments(JMSSampler.JMS_PROPERTIES).getArgumentsAsMap();
		Iterator argIt = map.entrySet().iterator();
		while (argIt.hasNext()) {
            Map.Entry me = (Map.Entry) argIt.next();
			String name = (String) me.getKey();
			String value = (String) me.getValue();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding property [" + name + "=" + value + "]");
			}
			msg.setStringProperty(name, value);
		}
	}

	public Arguments getJMSProperties() {
		return getArguments(JMSSampler.JMS_PROPERTIES);
	}

	public Arguments getJNDIProperties() {
		return getArguments(JMSSampler.JNDI_PROPERTIES);
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
		return getPropertyAsBoolean(IS_ONE_WAY);
	}

	public String getInitialContextFactory() {
		return getPropertyAsString(JMSSampler.JNDI_INITIAL_CONTEXT_FACTORY);
	}

	public String getContextProvider() {
		return getPropertyAsString(JMSSampler.JNDI_CONTEXT_PROVIDER_URL);
	}

	public void setIsOneway(boolean isOneway) {
		setProperty(new BooleanProperty(IS_ONE_WAY, isOneway));
	}

	public String toString() {
		return getQueueConnectionFactory() + ", queue: " + getSendQueue();
	}

	public synchronized void testStarted() {
		LOGGER.debug("testStarted, thread: " + Thread.currentThread().getName());

	}

	public synchronized void testEnded() {
		LOGGER.debug("testEndded(), thread: " + Thread.currentThread().getName());
	}

	public void testIterationStart(LoopIterationEvent event) {
		// LOGGER.debug("testIterationStart");
	}

	public void threadStarted() {
		logThreadStart();

		Context context = null;
		try {
			context = getInitialContext();
			Object obj = context.lookup(getQueueConnectionFactory());
			if (!(obj instanceof QueueConnectionFactory)) {
				String msg = "QueueConnectionFactory expected, but got " + obj.getClass().getName();
				LOGGER.fatalError(msg);
				throw new IllegalStateException(msg);
			}
			QueueConnectionFactory factory = (QueueConnectionFactory) obj;
			Queue queue = (Queue) context.lookup(getSendQueue());

			sendQueue = queue;
			if (!useTemporyQueue()) {
				receiveQueue = (Queue) context.lookup(getReceiveQueue());
				receiverThread = Receiver.createReceiver(factory, receiveQueue);
			}

			connection = factory.createQueueConnection();

			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Session created");
			}

			if (getPropertyAsBoolean(IS_ONE_WAY)) {
				producer = session.createSender(sendQueue);
			} else {

				if (useTemporyQueue()) {
					executor = new TemporaryQueueExecutor(session, sendQueue);
				} else {
					producer = session.createSender(sendQueue);
					executor = new FixedQueueExecutor(producer, getTimeout());
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Starting connection");
			}

			connection.start();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Connection started");
			}
		} catch (JMSException e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
		} catch (NamingException e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
		} finally {
			if (context != null) {
				try {
					context.close();
				} catch (NamingException e1) {
					// ignore
				}
			}
		}
	}

	private Context getInitialContext() throws NamingException {
		Hashtable table = new Hashtable();

		if (getInitialContextFactory() != null && getInitialContextFactory().trim().length() > 0) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using InitialContext [" + getInitialContextFactory() + "]");
			table.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
		}
		if (getContextProvider() != null && getContextProvider().trim().length() > 0) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Using Provider [" + getContextProvider() + "]");
			table.put(Context.PROVIDER_URL, getContextProvider());
		}
		Map map = getArguments(JMSSampler.JNDI_PROPERTIES).getArgumentsAsMap();
		if (LOGGER.isDebugEnabled()) {
			if (map.isEmpty()) {
				LOGGER.debug("Empty JNDI properties");
			} else {
				LOGGER.debug("Number of JNDI properties: " + map.size());
			}
		}
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			table.put(key, map.get(key));
		}

		Context context = new InitialContext(table);
		if (LOGGER.isDebugEnabled()) {
			printEnvironment(context);
		}
		return context;
	}

	private void printEnvironment(Context context) throws NamingException {
		Hashtable env = context.getEnvironment();
		LOGGER.debug("Initial Context Properties");
		Enumeration keys = env.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			LOGGER.debug(key + "=" + env.get(key));
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

	/**
	 * @return
	 */
	public int getTimeout() {
		if (getPropertyAsInt(TIMEOUT) < 1) {
			return DEFAULT_TIMEOUT;
		}
		return getPropertyAsInt(TIMEOUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestElement#threadFinished()
	 */
	public void threadFinished() {
		LOGGER.debug("Thread ended " + new Date());

		if (session != null)
			try {
				session.close();
			} catch (JMSException e) {
				LOGGER.info(e.getLocalizedMessage());

			}
		if (connection != null)
			try {
				connection.close();
			} catch (JMSException e) {
				LOGGER.info(e.getLocalizedMessage());
			}
		if (receiverThread != null)
			receiverThread.deactivate();
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

	/**
	 * @param i
	 */
	public void setTimeout(int i) {
		setProperty(JMSSampler.TIMEOUT, String.valueOf(i));

	}

	/**
	 * @param string
	 */
	public void setInitialContextFactory(String string) {
		setProperty(JNDI_INITIAL_CONTEXT_FACTORY, string);

	}

	/**
	 * @param string
	 */
	public void setContextProvider(String string) {
		setProperty(JNDI_CONTEXT_PROVIDER_URL, string);

	}

}
