/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * MessageClient is a basic interface defining the types of parameters
 * a JMS client may need to set to create a connection and start
 * communicating with a JMS server.
 * @author pete
 *
 */
public interface MessageClient {

	boolean isConsumer();
	boolean isProducer();
	
	Connection getConnection();
	MessageConsumer getConsumer();
	String getConnectionFactory();
	String getJNDIContextFactory();
	String getPassword();
	MessageProducer getProducer();
	String getProviderURL();
	Session getSession();
	String getUsername();

	void setConnection(javax.jms.Connection conn);
	void setConsumer(javax.jms.MessageConsumer consumer);
	void setConnectionFactory(String factory);
	void setJNDIContextFactory(String jndi);
	void setPassword(String pwd);
	void setProducer(javax.jms.MessageProducer producer);
	void setProviderURL(String url);
	void setSession(javax.jms.Session session);
	void setUsername(String user);
}
