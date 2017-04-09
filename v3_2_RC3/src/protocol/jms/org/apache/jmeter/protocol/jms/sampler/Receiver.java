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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.jms.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receiver of pseudo-synchronous reply messages.
 *
 */
public final class Receiver implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    private volatile boolean active;

    private final Session session;

    private final MessageConsumer consumer;

    private final Connection conn;

    private final boolean useResMsgIdAsCorrelId;


    /**
     * Constructor
     * @param factory
     * @param receiveQueue Receive Queue
     * @param principal Username
     * @param credentials Password
     * @param useResMsgIdAsCorrelId
     * @param jmsSelector JMS Selector
     * @throws JMSException
     */
    private Receiver(ConnectionFactory factory, Destination receiveQueue, String principal, String credentials, boolean useResMsgIdAsCorrelId, String jmsSelector) throws JMSException {
        if (null != principal && null != credentials) {
            log.info("creating receiver WITH authorisation credentials. UseResMsgId={}", useResMsgIdAsCorrelId);
            conn = factory.createConnection(principal, credentials);
        }else{
            log.info("creating receiver without authorisation credentials. UseResMsgId={}", useResMsgIdAsCorrelId);
            conn = factory.createConnection(); 
        }
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        log.debug("Receiver - ctor. Creating consumer with JMS Selector:{}", jmsSelector);
        if(StringUtils.isEmpty(jmsSelector)) {
            consumer = session.createConsumer(receiveQueue);
        } else {
            consumer = session.createConsumer(receiveQueue, jmsSelector);
        }
        this.useResMsgIdAsCorrelId = useResMsgIdAsCorrelId;
        log.debug("Receiver - ctor. Starting connection now");
        conn.start();
        log.info("Receiver - ctor. Connection to messaging system established");
    }

    /**
     * Create a receiver to process responses.
     *
     * @param factory
     *            connection factory to use
     * @param receiveQueue
     *            name of the receiving queue
     * @param principal
     *            user name to use for connecting to the queue
     * @param credentials
     *            credentials to use for connecting to the queue
     * @param useResMsgIdAsCorrelId
     *            <code>true</code> if should use JMSMessageId,
     *            <code>false</code> if should use JMSCorrelationId
     * @param jmsSelector
     *            JMS selector
     * @return the Receiver which will process the responses
     * @throws JMSException
     *             when creating the receiver fails
     */
    public static Receiver createReceiver(ConnectionFactory factory, Destination receiveQueue,
            String principal, String credentials, boolean useResMsgIdAsCorrelId, String jmsSelector)
            throws JMSException {
        Receiver receiver = new Receiver(factory, receiveQueue, principal, credentials, useResMsgIdAsCorrelId, jmsSelector);
        Thread thread = new Thread(receiver, Thread.currentThread().getName()+"-JMS-Receiver");
        thread.start();
        return receiver;
    }

    @Override
    public void run() {
        active = true;
        Message reply;

        while (active) {
            reply = null;
            try {
                reply = consumer.receive(5000);
                if (reply != null) {
                    String messageKey;
                    final MessageAdmin admin = MessageAdmin.getAdmin();
                    if (useResMsgIdAsCorrelId){
                        messageKey = reply.getJMSMessageID();
                        synchronized (admin) {// synchronize with FixedQueueExecutor
                            admin.putReply(messageKey, reply);                            
                        }
                    } else {
                        messageKey = reply.getJMSCorrelationID();
                        if (messageKey == null) {// JMSMessageID cannot be null
                            log.warn("Received message with correlation id null. Discarding message ...");
                        } else {
                            admin.putReply(messageKey, reply);
                        }
                    }
                }

            } catch (JMSException e1) {
                log.error("Error handling receive",e1);
            }
        }
        Utils.close(consumer, log);
        Utils.close(session, log);
        Utils.close(conn, log);
    }

    public void deactivate() {
        active = false;
    }

}
