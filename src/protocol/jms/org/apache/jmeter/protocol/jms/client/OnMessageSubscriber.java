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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author pete
 * 
 * OnMessageSubscriber is designed to create the connection, session and
 * subscriber. The sampler is responsible for implementing
 * javax.jms.MessageListener interface and onMessage(Message msg) method.
 * 
 * The implementation provides a close() method to clean up the client at the
 * end of a test. This is important to make sure there aren't any zombie threads
 * or odd memory leaks.
 */
public class OnMessageSubscriber {

	static Logger log = LoggingManager.getLoggerForClass();

	private TopicConnection CONN = null;

	private TopicSession SESSION = null;

	private Topic TOPIC = null;

	private TopicSubscriber SUBSCRIBER = null;

	/**
	 * 
	 */
	public OnMessageSubscriber() {
		super();
	}

	/**
	 * Constructor takes the necessary JNDI related parameters to create a
	 * connection and begin receiving messages.
	 * 
	 * @param useProps
	 * @param jndi
	 * @param url
	 * @param connfactory
	 * @param topic
	 * @param useAuth
	 * @param user
	 * @param pwd
	 */
	public OnMessageSubscriber(boolean useProps, String jndi, String url, String connfactory, String topic,
			String useAuth, String user, String pwd) {
		Context ctx = initJNDI(useProps, jndi, url, useAuth, user, pwd);
		if (ctx != null) {
			initConnection(ctx, connfactory, topic);
		} else {
			log.error("Could not initialize JNDI Initial Context Factory");
		}
	}

	/**
	 * initialize the JNDI intial context
	 * 
	 * @param useProps
	 * @param jndi
	 * @param url
	 * @param useAuth
	 * @param user
	 * @param pwd
	 * @return
	 */
	public Context initJNDI(boolean useProps, String jndi, String url, String useAuth, String user, String pwd) {
		if (useProps) {
			try {
				return new InitialContext();
			} catch (NamingException e) {
				log.error(e.getMessage());
				return null;
			}
		} else {
			return InitialContextFactory.lookupContext(jndi, url, useAuth, user, pwd);
		}
	}

	/**
	 * Initialize the connection, session and subscriber
	 * 
	 * @param ctx
	 * @param connfactory
	 * @param topic
	 */
	public void initConnection(Context ctx, String connfactory, String topic) {
		try {
			this.CONN = ConnectionFactory.getTopicConnection();
			this.TOPIC = InitialContextFactory.lookupTopic(ctx, topic);
			this.SESSION = this.CONN.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			this.SUBSCRIBER = this.SESSION.createSubscriber(this.TOPIC);
			log.info("created the topic connection successfully");
		} catch (JMSException e) {
			log.error("Connection error: " + e.getMessage());
		}
	}

	/**
	 * resume will call Connection.start() to begin receiving inbound messages.
	 */
	public void resume() {
		try {
			this.CONN.start();
		} catch (JMSException e) {
			log.error("failed to start recieving");
		}
	}

	/**
	 * close will close all the objects and set them to null.
	 */
	public void close() {
		try {
			log.info("Subscriber closed");
			this.SUBSCRIBER.close();
			this.SESSION.close();
			this.CONN.close();
			this.SUBSCRIBER = null;
			this.SESSION = null;
			this.CONN = null;
		} catch (JMSException e) {
			log.error(e.getMessage());
		} catch (Throwable e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * The sample uses this method to set itself as the listener. That means the
	 * sampler need to implement MessageListener interface.
	 * 
	 * @param listener
	 */
	public void setMessageListener(MessageListener listener) {
		try {
			this.SUBSCRIBER.setMessageListener(listener);
		} catch (JMSException e) {
			log.error(e.getMessage());
		}
	}
}
