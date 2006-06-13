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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;

/**
 * Request/reply executor with a temporary reply queue. <br>
 * Created on: October 28, 2004
 * 
 * @author Martijn Blankestijn
 * @version $Id: TemporaryQueueExecutor.java,v 1.3 2005/06/01 18:10:32
 *          mblankestijn Exp $
 */
public class TemporaryQueueExecutor implements QueueExecutor {
	/** The sender and receiver. */
	private QueueRequestor requestor;

	/**
	 * Constructor.
	 * 
	 * @param session
	 *            the session to use to send the message
	 * @param destination
	 *            the queue to send the message on
	 * @throws JMSException
	 */
	public TemporaryQueueExecutor(QueueSession session, Queue destination) throws JMSException {
		requestor = new QueueRequestor(session, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.jms.sampler.QueueExecutor#sendAndReceive(javax.jms.Message)
	 */
	public Message sendAndReceive(Message request) throws JMSException {
		return requestor.request(request);
	}
}
