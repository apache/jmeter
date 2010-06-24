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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * OnMessageSubscriber is designed to create the connection, session and
 * subscriber (MessageConsumer). The sampler is responsible for implementing
 * javax.jms.MessageListener interface and the onMessage(Message msg) method.
 *
 * The implementation provides a close() method to clean up the client at the
 * end of a test. This is important to make sure there aren't any zombie threads
 * or odd memory leaks.
 */
public class OnMessageSubscriber implements Closeable {

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
    public OnMessageSubscriber(boolean useProps, 
            String initialContextFactory, String providerUrl, String connfactory, String destinationName,
            boolean useAuth, 
            String securityPrincipal, String securityCredentials) throws JMSException, NamingException {
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
     * close will close all the objects
     */
    public void close() {
        log.debug("close()");
        Utils.close(SUBSCRIBER, log);
        Utils.close(SESSION, log);
        Utils.close(CONN, log);
    }

    /**
     * Set the MessageListener.
     *
     * @param listener
     * @throws JMSException 
     */
    public void setMessageListener(MessageListener listener) throws JMSException {
       SUBSCRIBER.setMessageListener(listener);
    }
}
