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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Administration of messages.
 *
 */
public class MessageAdmin {
    private static final MessageAdmin SINGLETON = new MessageAdmin();

    private final Map<String, PlaceHolder> table = new ConcurrentHashMap<String, PlaceHolder>();

    private static final Logger log = LoggingManager.getLoggerForClass();

    private MessageAdmin() {
    }

    public static MessageAdmin getAdmin() {
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
        PlaceHolder holder = table.get(id);
        if (log.isDebugEnabled()) {
            log.debug("RPL_ID [" + id + "] for holder " + holder);
        }
        if (holder != null) {
            holder.setReply(reply);
            Object obj = holder.getRequest();
            // Findbugs : False positive
            synchronized (obj) {
                obj.notify();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Failed to match reply: " + reply);
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
        PlaceHolder holder = table.remove(id);
        if (log.isDebugEnabled()) {
            log.debug("GET_ID [" + id + "] for " + holder);
        }
        if (holder == null || !holder.hasReply()) {
            log.debug("Message with " + id + " not found.");
        }
        return holder==null ? null : (Message) holder.getReply();
    }
}

class PlaceHolder {
    private final Object request;

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

    @Override
    public String toString() {
        return "request=" + request + ", reply=" + reply;
    }
}