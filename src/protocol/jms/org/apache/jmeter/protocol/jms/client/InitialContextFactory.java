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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.Topic;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.jmeter.protocol.jms.sampler.BaseJMSSampler;

/**
 * @author pete
 * 
 * InitialContextFactory is responsible for getting and instance of the initial
 * context. It is also responsible for looking up JMS topics and queues.
 */
public class InitialContextFactory {

	private static java.util.HashMap MAP = new HashMap();

	static Logger log = LoggingManager.getLoggerForClass();

	public static synchronized Context lookupContext(String jndi, String url, String useAuth, String user, String pwd) {
		Context ctx = (Context) MAP.get(jndi + url);
		if (ctx == null) {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY, jndi);
			props.setProperty(Context.PROVIDER_URL, url);
			if (useAuth != null && useAuth.equals(BaseJMSSampler.required) && user != null && pwd != null
					&& user.length() > 0 && pwd.length() > 0) {
				props.setProperty(Context.SECURITY_PRINCIPAL, user);
				props.setProperty(Context.SECURITY_CREDENTIALS, pwd);
				log.info("authentication properties set");
			}
			try {
				ctx = new InitialContext(props);
				log.info("created the JNDI initial context factory");
			} catch (NamingException e) {
				log.error("lookupContext:: " + e.getMessage());
			}
			if (ctx != null) {
				MAP.put(jndi + url, ctx);
			}
		}
		return ctx;
	}

	/**
	 * Method will lookup a given topic using JNDI.
	 * 
	 * @param ctx
	 * @param name
	 * @return
	 */
	public static synchronized Topic lookupTopic(Context ctx, String name) {
		Topic t = null;
		if (name != null && ctx != null) {
			try {
				t = (Topic) ctx.lookup(name);
			} catch (NamingException e) {
				log.error("JNDI error: " + e.getMessage());
			}
		} else if (name == null) {
			log.error("lookupTopic: name was null");
		} else {
			log.error("lookupTopic: Context was null");
		}
		return t;
	}

	/**
	 * clear all the InitialContext objects.
	 */
	public static void close() {
		Iterator itr = MAP.keySet().iterator();
		while (itr.hasNext()) {
			Context ctx = (Context) MAP.get(itr.next());
			try {
				ctx.close();
			} catch (NamingException e) {
				log.error(e.getMessage());
			}
		}
		log.info("InitialContextFactory.close() called and Context instances cleaned up");
	}
}
