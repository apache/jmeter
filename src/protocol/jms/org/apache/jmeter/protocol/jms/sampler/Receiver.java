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

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.jms.*;

/**
 * Receiver of pseudo-synchronous reply messages.
 *
 */
public class Receiver implements Runnable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private volatile boolean active;

    private final QueueSession session;

    private final QueueReceiver consumer;

    private final QueueConnection conn;

    // private static Receiver receiver;

    private Receiver(QueueConnectionFactory factory, Queue receiveQueue, String principal, String credentials) throws JMSException {
        if (null != principal && null != credentials) {
            log.info("creating receiver WITH authorisation credentials");
            conn = factory.createQueueConnection(principal, credentials);
        }else{
            log.info("creating receiver without authorisation credentials");
            conn = factory.createQueueConnection(); 
        }
        session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createReceiver(receiveQueue);
        log.debug("Receiver - ctor. Starting connection now");
        conn.start();
        log.info("Receiver - ctor. Connection to messaging system established");
    }

    public static Receiver createReceiver(QueueConnectionFactory factory, Queue receiveQueue,
            String principal, String credentials)
            throws JMSException {
        Receiver receiver = new Receiver(factory, receiveQueue, principal, credentials);
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
                log.error("Error handling receive",e1);
            }
        }
        // not active anymore
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                log.error("Error closing connection",e);
            }
        }

        // session and conn cannot be null (or ctor would have caused NPE)
        try {
            session.close();
        } catch (JMSException e) {
            log.error("Error closing session",e);
        }
        try {
            conn.close();
        } catch (JMSException e) {
            log.error("Error closing connection",e);
        }
    }

    public void deactivate() {
        active = false;
    }

}
