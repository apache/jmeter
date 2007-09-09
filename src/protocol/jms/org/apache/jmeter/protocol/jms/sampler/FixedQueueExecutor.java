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

package org.apache.jmeter.protocol.jms.sampler;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSender;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Request/reply executor with a fixed reply queue. <br>
 * Created on: October 28, 2004
 * 
 * @author Martijn Blankestijn
 * @version $Id: FixedQueueExecutor.java,v 1.3 2005/05/19 15:36:53 mblankestijn
 *          Exp $
 */
public class FixedQueueExecutor implements QueueExecutor {
	/** Sender. */
	private QueueSender producer;

	/** Timeout used for waiting on message. */
	private int timeout;

	static Logger log = LoggingManager.getLoggerForClass();

	/**
	 * Constructor.
	 * 
	 * @param producer
	 *            the queue to send the message on
	 * @param timeout
	 *            timeout to use for the return message
	 */
	public FixedQueueExecutor(QueueSender producer, int timeout) {
		this.producer = producer;
		this.timeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.jms.sampler.QueueExecutor#sendAndReceive(javax.jms.Message)
	 */
	public Message sendAndReceive(Message request) throws JMSException {
		producer.send(request);
		String id = request.getJMSMessageID();
		MessageAdmin.getAdmin().putRequest(id, request);
		try {
			if (log.isDebugEnabled()) {
				log.debug("wait for reply " + id + " started on " + System.currentTimeMillis());
			}
			synchronized (request) {
				request.wait(timeout);
			}
			if (log.isDebugEnabled()) {
				log.debug("done waiting for " + id + " ended on " + System.currentTimeMillis());
			}

		} catch (InterruptedException e) {
			log.warn("Interrupt exception caught", e);
		}
		return MessageAdmin.getAdmin().get(request.getJMSMessageID());
	}
}
