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
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;

/**
 * Request/reply executor with a temporary reply queue. <br>
 * 
 * Used by JMS Sampler (Point to Point)
 */
public class TemporaryQueueExecutor implements QueueExecutor {
    /** The sender and receiver. */
    private final QueueRequestor requestor;

    /**
     * Constructor.
     *
     * @param session
     *            the session to use to send the message
     * @param destination
     *            the queue to send the message on
     * @throws JMSException
     *             when internally used {@link QueueRequestor} can not be
     *             constructed with <code>session</code> and
     *             <code>destination</code>
     */
    public TemporaryQueueExecutor(QueueSession session, Queue destination) throws JMSException {
        requestor = new QueueRequestor(session, destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message sendAndReceive(Message request, 
            int deliveryMode, 
            int priority, 
            long expiration) throws JMSException {
        return requestor.request(request);
    }
}
