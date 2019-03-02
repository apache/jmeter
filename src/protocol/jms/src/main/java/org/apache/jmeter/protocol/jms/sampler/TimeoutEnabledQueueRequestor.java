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

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TimeoutEnabledQueueRequestor</code> helper class simplifies making
 * service requests using the request/reply pattern.
 *
 * <P>
 * The <code>TimeoutEnabledQueueRequestor</code> constructor is given a
 * non-transacted <code>QueueSession</code> and a destination
 * <code>Queue</code>. It creates a <code>TemporaryQueue</code> for the
 * responses and provides a <code>request</code> method that sends the request
 * message and waits for its reply.
 *
 * @since 4.0
 */
public class TimeoutEnabledQueueRequestor {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutEnabledQueueRequestor.class);
    private TemporaryQueue tempQueue;
    private MessageProducer sender;
    private MessageConsumer receiver;

    /**
     * Constructor for the <code>TimeoutEnabledQueueRequestor</code> class.
     *
     * <P>
     * This implementation assumes the session parameter to be non-transacted,
     * with a delivery mode of either <code>AUTO_ACKNOWLEDGE</code> or
     * <code>DUPS_OK_ACKNOWLEDGE</code>.
     *
     * @param session the <code>QueueSession</code> the queue belongs to, session will not be closed by {@link TimeoutEnabledQueueRequestor}
     * @param queue the queue to performthe request/reply call on
     *
     * @exception JMSException
     *                if the JMS provider fails to create the
     *                <code>TimeoutEnabledQueueRequestor</code> due to some
     *                internal error.
     * @exception InvalidDestinationException
     *                if an invalid queue is specified.
     */
    public TimeoutEnabledQueueRequestor(Session session, Queue queue) throws JMSException {
        tempQueue = session.createTemporaryQueue();
        sender = session.createProducer(queue);
        receiver = session.createConsumer(tempQueue);
    }

    /**
     * Sends a request and waits for a reply. The temporary queue is used for
     * the <code>JMSReplyTo</code> destination, and only one reply per request
     * is expected. The method blocks indefinitely until a message arrives!
     *
     * @param message the message to send
     *
     * @return the reply message
     *
     * @exception JMSException
     *                if the JMS provider fails to complete the request due to
     *                some internal error.
     */
    public Message request(Message message) throws JMSException {
        return request(message, 0);
    }

    /**
     * Sends a request and waits for a reply. The temporary queue is used for
     * the <code>JMSReplyTo</code> destination, and only one reply per request
     * is expected. The client waits/blocks for the reply until the timeout is
     * reached.
     *
     * @param message
     *            the message to send
     * @param timeout
     *            time to wait for a reply on temporary queue. If you specify no
     *            arguments or an argument of 0, the method blocks indefinitely
     *            until a message arrives
     *
     * @return the reply message
     *
     * @exception JMSException
     *                if the JMS provider fails to complete the request due to
     *                some internal error.
     */
    public Message request(Message message, long timeout) throws JMSException {
        message.setJMSReplyTo(tempQueue);
        sender.send(message);
        return receiver.receive(timeout);
    }

    /**
     * Closes the <code>TimeoutEnabledQueueRequestor</code> and its session.
     *
     * <P>
     * Since a provider may allocate some resources on behalf of a
     * <code>TimeoutEnabledQueueRequestor</code> outside the Java virtual
     * machine, clients should close them when they are not needed. Relying on
     * garbage collection to eventually reclaim these resources may not be
     * timely enough.
     *
     * <P>
     * This method closes the <code>Session</code> object passed to the
     * <code>TimeoutEnabledQueueRequestor</code> constructor.
     *
     * @exception JMSException
     *                if the JMS provider fails to close the
     *                <code>TimeoutEnabledQueueRequestor</code> due to some
     *                internal error.
     */
    public void close() throws JMSException {
        String queueName = tempQueue.getQueueName();
        try {
            sender.close();
        } catch (Exception ex) {
            logger.error("Error closing sender", ex);
        }
        try {
            receiver.close();
        } catch (Exception ex) {
            logger.error("Error closing receiver", ex);
        }
        try {
            tempQueue.delete();
        } catch (Exception ex) {
            logger.error("Error deleting tempQueue {}", queueName, ex);
        }
    }
}
