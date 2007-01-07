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

package org.apache.jmeter.protocol.jms.sampler;

import java.util.Hashtable;
import java.util.Map;

import javax.jms.Message;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Administration of messages.
 * 
 * @author Martijn Blankestijn
 * @version $Id$.
 */
public class MessageAdmin {
	private static final MessageAdmin SINGLETON = new MessageAdmin();

	private Map table = new Hashtable();

	static Logger log = LoggingManager.getLoggerForClass();

	private MessageAdmin() {
	}

	public static synchronized MessageAdmin getAdmin() {
		return SINGLETON;
	}

	/**
	 * @param request
	 */
	public void putRequest(String id, Message request) {
		if (log.isDebugEnabled()) {
			log.debug("REQ_ID [" + id + "]");
		}
		table.put(id, new PlaceHolder(request));
	}

	public void putReply(String id, Message reply) {
		PlaceHolder holder = (PlaceHolder) table.get(id);
		if (log.isDebugEnabled()) {
			log.debug("RPL_ID [" + id + "] for holder " + holder);
		}
		if (holder != null) {
			holder.setReply(reply);
			Object obj = holder.getRequest();
			synchronized (obj) {
				obj.notify();
			}

		}
	}

	/**
	 * Get the reply message.
	 * 
	 * @param id
	 *            the id of the message
	 * @return the received message or <code>null</code>
	 */
	public Message get(String id) {
		PlaceHolder holder = (PlaceHolder) table.remove(id);
		if (log.isDebugEnabled()) {
			log.debug("GET_ID [" + id + "] for " + holder);
		}
		if (!holder.hasReply()) {
			log.info("Message with " + id + " not found.");
		}
		return (Message) holder.getReply();
	}
}

class PlaceHolder {
	private Object request;

	private Object reply;

	PlaceHolder(Object original) {
		this.request = original;
	}

	void setReply(Object reply) {
		this.reply = reply;
	}

	public Object getReply() {
		return reply;
	}

	public Object getRequest() {
		return request;
	}

	boolean hasReply() {
		return reply != null;
	}

	public String toString() {
		return "request=" + request + ", reply=" + reply;
	}
}