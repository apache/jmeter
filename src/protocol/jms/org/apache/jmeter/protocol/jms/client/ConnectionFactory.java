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
 * @author pete
 * 
 * ConnectionFactory is responsible for creating new connections. Eventually,
 * the connection factory should read an external configuration file and create
 * a pool of connections. The current implementation just does the basics. Once
 * the tires get kicked a bit, we can add connection pooling support.
 */
public class ConnectionFactory implements TestListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

	private static TopicConnectionFactory factory = null;

	private static QueueConnectionFactory qfactory = null;

	/**
	 * 
	 */
	protected ConnectionFactory() {
		super();
	}

	public void testStarted(String test) {
	}

	public void testEnded(String test) {
		testEnded();
	}

	/**
	 * endTest cleans up the client
	 * 
	 * @see junit.framework.TestListener#endTest(junit.framework.Test)
	 */
	public void testEnded() {
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

	public void testIterationStart(LoopIterationEvent event) {
	}

	/**
	 * 
	 * @param ctx
	 * @param fac
	 * @return
	 */
	public static synchronized TopicConnectionFactory getTopicConnectionFactory(Context ctx, String fac) {
		while (factory == null) {
			try {
				Object objfac = ctx.lookup(fac);
				if (objfac instanceof TopicConnectionFactory) {
					factory = (TopicConnectionFactory) objfac;
				}
			} catch (NamingException e) {
				log.error(e.toString());
			}
		}
		return factory;
	}

	/**
	 * 
	 * @param ctx
	 * @param fac
	 * @return
	 */
	public static synchronized QueueConnectionFactory getQueueConnectionFactory(Context ctx, String fac) {
		while (qfactory == null) {
			try {
				Object objfac = ctx.lookup(fac);
				if (objfac instanceof QueueConnectionFactory) {
					qfactory = (QueueConnectionFactory) objfac;
				}
			} catch (NamingException e) {
				log.error(e.getMessage());
			}
		}
		return qfactory;
	}

	/**
	 * 
	 * @return
	 */
	public static synchronized TopicConnection getTopicConnection() {
		if (factory != null) {
			try {
				return factory.createTopicConnection();
			} catch (JMSException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * 
	 * @param ctx
	 * @param queueConn
	 * @return
	 */
	public static QueueConnection getQueueConnection(Context ctx, String queueConn) {
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
