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

import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.NamingException;

import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.QueueConnection;

import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.engine.event.LoopIterationEvent;

/**
 * @author pete
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ConnectionFactory implements TestListener {

	private static TopicConnectionFactory factory = null;
	
    /**
     * 
     */
    protected ConnectionFactory() {
        super();
    }

	public void testStarted(String test){
	}
	
	public void testEnded(String test){
		testEnded();
	}

	/**
	 * endTest cleans up the client
	 * @see junit.framework.TestListener#endTest(junit.framework.Test)
	 */
	public void testEnded() {
		factory = null;
	}

	/**
	 * startTest sets up the client and gets it ready for the
	 * test. Since async messaging is different than request/
	 * response applications, the connection is created at the
	 * beginning of the test and closed at the end of the test.
	 */
	public void testStarted() {
	}

	public void testIterationStart(LoopIterationEvent event){
	}

	public static synchronized TopicConnectionFactory getTopicConnectionFactory(
	Context ctx, String fac){
		while (factory == null){
			try {
				factory = (TopicConnectionFactory)ctx.lookup(fac);
			} catch (NamingException e){
				e.printStackTrace();
			}
		}
		return factory;
	}
	
	public static synchronized TopicConnection getTopicConnection(){
		if (factory != null){
			try {
				return factory.createTopicConnection();
			} catch (JMSException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static QueueConnection getQueueConnection(Context ctx, String queueConn){
		return null;
	}
}
