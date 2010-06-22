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

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * ConnectionFactory is responsible for creating new connections. Eventually,
 * the connection factory should read an external configuration file and create
 * a pool of connections. The current implementation just does the basics. Once
 * the tires get kicked a bit, we can add connection pooling support.
 * 
 * TODO - is there any point in caching the factories and in the retries?
 * 
 * Note: the connection factory will retry to get the connection factory 5 times
 * before giving up. Thanks to Peter Johnson for catching the bug and providing
 * the patch.
 */
public class ConnectionFactory {

    private static final Logger log = LoggingManager.getLoggerForClass();

    //@GuardedBy("this")
    private static TopicConnectionFactory factory = null;

    //@GuardedBy("this")
    private static QueueConnectionFactory qfactory = null;

    /**
     * Maximum number of times we will attempt to obtain a connection factory.
     */
    private static final int MAX_RETRY = 5;

    /**
     * Amount of time to pause between connection factory lookup attempts.
     */
    private static final int PAUSE_MILLIS = 100;

    /**
     *
     */
    private ConnectionFactory() {
        super();
    }

    /**
     * Get the cached TopicConnectionFactory.
     * 
     * @param ctx the context to use
     * @param factoryName the name of the factory
     * @return the factory, or null if it could not be found
     */
    public static synchronized TopicConnectionFactory getTopicConnectionFactory(Context ctx, String factoryName) {
        int counter = MAX_RETRY;
        while (factory == null && counter > 0) {
             try {
                 Object objfac = ctx.lookup(factoryName);
                 if (objfac instanceof TopicConnectionFactory) {
                     factory = (TopicConnectionFactory) objfac;
                 } else {
                     log.error("Expected TopicConnectionFactory, found "+objfac.getClass().getName());
                     break;
                 }
             } catch (NamingException e) {
                if (counter == MAX_RETRY) {
                    log.error("Unable to find topic connection factory " + factoryName + ", will retry. Error: " + e.toString());
                } else if (counter == 1) {
                    log.error("Unable to find topic connection factory " + factoryName + ", giving up. Error: " + e.toString());
                }
                counter--;
                try {
                    Thread.sleep(PAUSE_MILLIS);
                } catch (InterruptedException ie) {
                    // do nothing, getting interrupted is acceptable
                }
             }
         }
         return factory;
    }

    /**
     * Get the cached QueueConnectionFactory.
     * 
     * @param ctx the context to use
     * @param factoryName the queue factory name
     * @return the factory, or null if the factory could not be found
     */
    public static synchronized QueueConnectionFactory getQueueConnectionFactory(Context ctx, String factoryName) {
        int counter = MAX_RETRY;
        while (qfactory == null && counter > 0) {
             try {
                 Object objfac = ctx.lookup(factoryName);
                 if (objfac instanceof QueueConnectionFactory) {
                     qfactory = (QueueConnectionFactory) objfac;
                 } else {
                     log.error("Expected QueueConnectionFactory, found "+objfac.getClass().getName());
                     break;
                 }
             } catch (NamingException e) {
                if (counter == MAX_RETRY) {
                    log.error("Unable to find queue connection factory " + factoryName + ", will retry. Error: " + e.toString());
                } else if (counter == 1) {
                    log.error("Unable to find queue connection factory " + factoryName + ", giving up. Error: " + e.toString());
                }
                counter--;
                try {
                    Thread.sleep(PAUSE_MILLIS);
                } catch (InterruptedException ie) {
                  // do nothing, getting interrupted is acceptable
                }
             }
         }
         return qfactory;
    }

    /**
     * Use the static factory to create a topic connection.
     * 
     * @return the connection
     * @throws JMSException if the factory is null or the create() method fails
     */
    public static synchronized TopicConnection getTopicConnection() throws JMSException {
        if (factory != null) {
            return factory.createTopicConnection();
        }
        throw new JMSException("Topic Factory has not been initialised");
    }

    /**
     * Use the static factory to create a queue connection.
     * 
     * @return the connection
     * @throws JMSException if the factory is null or the create() method fails
     */
    public static synchronized QueueConnection getQueueConnection() throws JMSException {
        if (qfactory != null) {
            return qfactory.createQueueConnection();
        }
        throw new JMSException("Queue Factory has not been initialised");
    }

    /**
     * Obtain the queue connection from the context and factory name.
     * Does not cache the factory.
     * @param ctx
     * @param factoryName
     * @return the queue connection
     * @throws JMSException
     * @throws NamingException
     */
    public static QueueConnection getQueueConnection(Context ctx, String factoryName) throws JMSException, NamingException {
        Object objfac = ctx.lookup(factoryName);
        if (objfac instanceof QueueConnectionFactory) {
            return ((QueueConnectionFactory) objfac).createQueueConnection();
        }
        throw new NamingException("Expected QueueConnectionFactory, found "+objfac.getClass().getName());
    }

    /**
     * Obtain the topic connection from the context and factory name.
     * Does not cache the factory.
     * @param ctx
     * @param factoryName
     * @return the topic connection
     * @throws JMSException
     * @throws NamingException
     */
    public static TopicConnection getTopicConnection(Context ctx, String factoryName) throws JMSException, NamingException {
        Object objfac = ctx.lookup(factoryName);
        if (objfac instanceof TopicConnectionFactory) {
            return ((TopicConnectionFactory) objfac).createTopicConnection();
        }
        throw new NamingException("Expected TopicConnectionFactory, found "+objfac.getClass().getName());
    }
}
