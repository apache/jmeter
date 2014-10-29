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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Request/reply executor with a fixed reply queue. <br>
 *
 * Used by JMS Sampler (Point to Point)
 *
 */
public class FixedQueueExecutor implements QueueExecutor {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /** Sender. */
    private final MessageProducer producer;

    /** Timeout used for waiting on message. */
    private final int timeout;

    private final boolean useReqMsgIdAsCorrelId;

    /**
     * Constructor.
     *
     * @param producer
     *            the queue to send the message on
     * @param timeout
     *            timeout to use for the return message
     * @param useReqMsgIdAsCorrelId
     *            whether to use the request message id as the correlation id
     */
    public FixedQueueExecutor(MessageProducer producer, int timeout, boolean useReqMsgIdAsCorrelId) {
        this.producer = producer;
        this.timeout = timeout;
        this.useReqMsgIdAsCorrelId = useReqMsgIdAsCorrelId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message sendAndReceive(Message request, 
            int deliveryMode, 
            int priority, 
            long expiration) throws JMSException {
        String id = request.getJMSCorrelationID();
        if(id == null && !useReqMsgIdAsCorrelId){
            throw new IllegalArgumentException("Correlation id is null. Set the JMSCorrelationID header.");
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final MessageAdmin admin = MessageAdmin.getAdmin();
        if(useReqMsgIdAsCorrelId) {// msgId not available until after send() is called
            // Note: there is only one admin object which is shared between all threads
            synchronized (admin) {// interlock with Receiver
                producer.send(request, deliveryMode, priority, expiration);
                id=request.getJMSMessageID();
                admin.putRequest(id, request, countDownLatch);
            }
        } else {
            admin.putRequest(id, request, countDownLatch);            
            producer.send(request, deliveryMode, priority, expiration);
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(Thread.currentThread().getName()+" will wait for reply " + id + " started on " + System.currentTimeMillis());
            }
            // This used to be request.wait(timeout_ms), where 0 means forever
            // However 0 means return immediately for the latch
            if (timeout == 0){
                countDownLatch.await(); //
            } else {
                if(!countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                    log.debug("Timeout reached before getting a reply message");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(Thread.currentThread().getName()+" done waiting for " + id + " on "+request+" ended on " + System.currentTimeMillis());
            }

        } catch (InterruptedException e) {
            log.warn("Interrupt exception caught", e);
        }
        return admin.get(id);
    }
}
