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

package org.apache.jmeter.protocol.jms.client;

import java.io.Closeable;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Uses MessageConsumer.receive(timeout) to fetch messages.
 * Does not cache any messages.
 */
public class ReceiveSubscriber implements Closeable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Connection CONN;

    private final Session SESSION;

    private final MessageConsumer SUBSCRIBER;


    /**
     * Constructor takes the necessary JNDI related parameters to create a
     * connection and prepare to begin receiving messages.
     * <br/>
     * The caller must then invoke {@link #start()} to enable message reception.
     * 
     * @param useProps if true, use jndi.properties instead of 
     * initialContextFactory, providerUrl, securityPrincipal, securityCredentials
     * @param initialContextFactory
     * @param providerUrl
     * @param connfactory
     * @param destinationName
     * @param useAuth
     * @param securityPrincipal
     * @param securityCredentials
     * @throws JMSException if could not create context or other problem occurred.
     * @throws NamingException 
     */
    public ReceiveSubscriber(boolean useProps, 
            String initialContextFactory, String providerUrl, String connfactory, String destinationName,
            boolean useAuth, 
            String securityPrincipal, String securityCredentials) throws NamingException, JMSException {
        Context ctx = InitialContextFactory.getContext(useProps, 
                initialContextFactory, providerUrl, useAuth, securityPrincipal, securityCredentials);
        CONN = Utils.getConnection(ctx, connfactory);
        SESSION = CONN.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = Utils.lookupDestination(ctx, destinationName);
        SUBSCRIBER = SESSION.createConsumer(dest);
        log.debug("<init> complete");
    }

    /**
     * Calls Connection.start() to begin receiving inbound messages.
     * @throws JMSException 
     */
    public void start() throws JMSException {
        log.debug("start()");
        CONN.start();
    }

    /**
     * Calls Connection.stop() to stop receiving inbound messages.
     * @throws JMSException 
     */
    public void stop() throws JMSException {
        log.debug("stop()");
        CONN.stop();
    }

    /**
     * Get the next message or null.
     * Never blocks for longer than the specified timeout.
     * 
     * @param timeout in milliseconds
     * @return the next message or null
     * 
     * @throws JMSException
     */
    public Message getMessage(long timeout) throws JMSException {
        Message message = null;
        if (timeout < 10) { // Allow for short/negative times
            message = SUBSCRIBER.receiveNoWait();                
        } else {
            message = SUBSCRIBER.receive(timeout);
        }
        return message;
    }
    /**
     * close() will stop the connection first. Then it closes the subscriber,
     * session and connection.
     */
    public synchronized void close() { // called from testEnded() thread
        log.debug("close()");
        try {
            CONN.stop();
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
        Utils.close(SUBSCRIBER, log);
        Utils.close(SESSION, log);
        Utils.close(CONN, log);
    }
}
