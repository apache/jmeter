/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author pete
 * 
 */
public class Publisher {

	private static final Logger log = LoggingManager.getLoggerForClass();

	private TopicConnection CONN = null;

	private TopicSession SESSION = null;

	private Topic TOPIC = null;

	private TopicPublisher PUBLISHER = null;

	//private byte[] RESULT = null;

	//private Object OBJ_RESULT = null;

	/**
	 * 
	 */
	public Publisher(boolean useProps, String jndi, String url, String connfactory, String topic, String useAuth,
			String user, String pwd) {
		super();
		Context ctx = initJNDI(useProps, jndi, url, useAuth, user, pwd);
		if (ctx != null) {
			initConnection(ctx, connfactory, topic);
		} else {
			log.error("Could not initialize JNDI Initial Context Factory");
		}
	}

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

	public void initConnection(Context ctx, String connfactory, String topic) {
		try {
			ConnectionFactory.getTopicConnectionFactory(ctx,connfactory);
			this.CONN = ConnectionFactory.getTopicConnection();
			this.TOPIC = InitialContextFactory.lookupTopic(ctx, topic);
			this.SESSION = this.CONN.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			this.PUBLISHER = this.SESSION.createPublisher(this.TOPIC);
			log.info("created the topic connection successfully");
		} catch (JMSException e) {
			log.error("Connection error: " + e.getMessage());
		}
	}

	public void publish(String text) {
		try {
			TextMessage msg = this.SESSION.createTextMessage(text);
			this.PUBLISHER.publish(msg);
		} catch (JMSException e) {
			log.error(e.getMessage());
		}
	}

	public void publish(Serializable contents) {
		try {
			ObjectMessage msg = this.SESSION.createObjectMessage(contents);
			this.PUBLISHER.publish(msg);
		} catch (JMSException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Clise will close the session
	 */
	public void close() {
		try {
			log.info("Publisher closed");
			this.PUBLISHER.close();
			this.SESSION.close();
			this.CONN.close();
			this.PUBLISHER = null;
			this.SESSION = null;
			this.CONN = null;
		} catch (JMSException e) {
			log.error(e.getMessage());
		} catch (Throwable e) {
			log.error(e.getMessage());
		}
	}

}
