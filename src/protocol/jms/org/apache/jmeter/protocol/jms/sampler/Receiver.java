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

import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Receiver of pseudo-synchronous reply messages.
 *
 */
public class Receiver implements Runnable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private volatile boolean active;

    private final Session session;

    private final MessageConsumer consumer;

    private final Connection conn;

    private final boolean useResMsgIdAsCorrelId;

    private Receiver(ConnectionFactory factory, Destination receiveQueue, String principal, String credentials, boolean useResMsgIdAsCorrelId) throws JMSException {
        if (null != principal && null != credentials) {
            log.info("creating receiver WITH authorisation credentials. UseResMsgId="+useResMsgIdAsCorrelId);
            conn = factory.createConnection(principal, credentials);
        }else{
            log.info("creating receiver without authorisation credentials. UseResMsgId="+useResMsgIdAsCorrelId);
            conn = factory.createConnection(); 
        }
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(receiveQueue);
        this.useResMsgIdAsCorrelId = useResMsgIdAsCorrelId;
        log.debug("Receiver - ctor. Starting connection now");
        conn.start();
        log.info("Receiver - ctor. Connection to messaging system established");
    }

    /**
     * Create a receiver to process responses.
     * 
     * @param factory
     * @param receiveQueue
     * @param principal
     * @param credentials
     * @param useResMsgIdAsCorrelId true if should use JMSMessageId, false if should use JMSCorrelationId
     * @return the Receiver which will process the responses
     * @throws JMSException
     */
    public static Receiver createReceiver(ConnectionFactory factory, Destination receiveQueue,
            String principal, String credentials, boolean useResMsgIdAsCorrelId)
            throws JMSException {
        Receiver receiver = new Receiver(factory, receiveQueue, principal, credentials, useResMsgIdAsCorrelId);
        Thread thread = new Thread(receiver);
        thread.start();
        return receiver;
    }

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
