/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.jms.sampler;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.jms.*;

/**
 * Receiver of pseudo-synchronous reply messages.
 * 
 * @author Martijn Blankestijn
 * @version $Id$.
 */
public class Receiver implements Runnable {
	private boolean active;

	private QueueSession session;

	private QueueReceiver consumer;

	private QueueConnection conn;

	// private static Receiver receiver;
	static Logger log = LoggingManager.getLoggerForClass();

	private Receiver(QueueConnectionFactory factory, Queue receiveQueue) throws JMSException {
		conn = factory.createQueueConnection();
		session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createReceiver(receiveQueue);
		if (log.isDebugEnabled()) {
			log.debug("Receiver - ctor. Starting connection now");
		}
		conn.start();
		if (log.isInfoEnabled()) {
			log.info("Receiver - ctor. Connection to messaging system established");
		}
	}

	public static synchronized Receiver createReceiver(QueueConnectionFactory factory, Queue receiveQueue)
			throws JMSException {
		// if (receiver == null) {
		Receiver receiver = new Receiver(factory, receiveQueue);
		Thread thread = new Thread(receiver);
		thread.start();
		// }
		return receiver;
	}

	public void run() {
		activate();
		Message reply;

		while (isActive()) {
			reply = null;
			try {
				reply = consumer.receive(5000);
				if (reply != null) {

					if (log.isDebugEnabled()) {
						log.debug("Received message, correlation id:" + reply.getJMSCorrelationID());
					}

					if (reply.getJMSCorrelationID() == null) {
						log.warn("Received message with correlation id null. Discarding message ...");
					} else {
						MessageAdmin.getAdmin().putReply(reply.getJMSCorrelationID(), reply);
					}
				}

			} catch (JMSException e1) {
				e1.printStackTrace();
			}
		}
		// not active anymore
		if (session != null) {
			try {
				session.close();
				if (conn != null)
					conn.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void activate() {
		active = true;
	}

	public synchronized void deactivate() {
		active = false;
	}

	private synchronized boolean isActive() {
		return active;
	}

}
