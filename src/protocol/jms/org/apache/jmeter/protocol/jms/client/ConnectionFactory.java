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

import javax.naming.Context;
import javax.naming.NamingException;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;

import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.engine.event.LoopIterationEvent;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * ConnectionFactory is responsible for creating new connections. Eventually,
 * the connection factory should read an external configuration file and create
 * a pool of connections. The current implementation just does the basics. Once
 * the tires get kicked a bit, we can add connection pooling support.
 * 
 * Note: the connection factory will retry to get the connection factory 5 times
 * before giving up. Thanks to Peter Johnson for catching the bug and providing
 * the patch.
 */
public class ConnectionFactory implements TestListener {

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
    protected ConnectionFactory() {
        super();
    }

    /** {@inheritDoc} */
    public void testStarted(String test) {
    }

    /** {@inheritDoc} */
    public void testEnded(String test) {
        testEnded();
    }

    /** {@inheritDoc} */
    public synchronized void testEnded() {
        ConnectionFactory.factory = null;//N.B. static reference
    }

    /**
     * startTest sets up the client and gets it ready for the test. Since async
     * messaging is different than request/ response applications, the
     * connection is created at the beginning of the test and closed at the end
     * of the test.
     */
    public void testStarted() {
    }

    /** {@inheritDoc} */
    public void testIterationStart(LoopIterationEvent event) {
    }

    public static synchronized TopicConnectionFactory getTopicConnectionFactory(Context ctx, String fac) {
        int counter = MAX_RETRY;
        while (factory == null && counter > 0) {
             try {
                 Object objfac = ctx.lookup(fac);
                 if (objfac instanceof TopicConnectionFactory) {
                     factory = (TopicConnectionFactory) objfac;
                 }
             } catch (NamingException e) {
                if (counter == MAX_RETRY) {
                    log.error("Unable to find connection factory " + fac + ", will retry. Error: " + e.toString());
                } else if (counter == 1) {
                    log.error("Unable to find connection factory " + fac + ", giving up. Error: " + e.toString());
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

    public static synchronized QueueConnectionFactory getQueueConnectionFactory(Context ctx, String fac) {
        int counter = MAX_RETRY;
        while (qfactory == null && counter > 0) {
             try {
                 Object objfac = ctx.lookup(fac);
                 if (objfac instanceof QueueConnectionFactory) {
                     qfactory = (QueueConnectionFactory) objfac;
                 }
             } catch (NamingException e) {
                if (counter == MAX_RETRY) {
                    log.error("Unable to find connection factory " + fac + ", will retry. Error: " + e.toString());
                } else if (counter == 1) {
                    log.error("Unable to find connection factory " + fac + ", giving up. Error: " + e.toString());
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
     * Use the factory to create a topic connection.
     * 
     * @return the connection
     * @throws JMSException if the factory is null or the create() method fails
     */
    public static synchronized TopicConnection getTopicConnection() throws JMSException {
        if (factory != null) {
            return factory.createTopicConnection();
        }
        throw new JMSException("Factory has not been initialised");
    }

    public static synchronized QueueConnection getQueueConnection(Context ctx, String queueConn) {
        if (factory != null) {
            try {
                return qfactory.createQueueConnection();
            } catch (JMSException e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }
}
