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

package org.apache.jmeter.protocol.smtp.sampler.protocol;

import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a listener for SMTP events and a monitor for all
 * threads sending mail. The main purpose is to synchronize the send action with
 * the end of communication with remote smtp server, so that sending time can be
 * measured.
 */
public class SynchronousTransportListener extends TransportAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SynchronousTransportListener.class);

    private boolean finished = false;

    private final Object LOCK = new Object();
    
    /**
     * Creates a new instance of SynchronousTransportListener
     */
    public SynchronousTransportListener() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageDelivered(TransportEvent e) {
        logger.debug("Message delivered");
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageNotDelivered(TransportEvent e) {
        logger.debug("Message not delivered");
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messagePartiallyDelivered(TransportEvent e) {
        logger.debug("Message partially delivered");
        finish();
    }

    /**
     * Synchronized-method
     * <p>
     * Waits until {@link #finish()} was called and thus the end of the mail
     * sending was signalled.
     *
     * @throws InterruptedException
     *             when interrupted while waiting with the lock
     */
    public void attend() throws InterruptedException {
        synchronized(LOCK) {
            while (!finished) {
                LOCK.wait();            
            }
        }
    }

    /**
     * Synchronized-method
     */
    public void finish() {
        finished = true;
        synchronized(LOCK) {
            LOCK.notify();
        }
    }

}
