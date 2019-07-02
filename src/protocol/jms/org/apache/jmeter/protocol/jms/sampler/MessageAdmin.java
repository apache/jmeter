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
import java.util.concurrent.CountDownLatch;

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Administration of messages.
 *
 */
public class MessageAdmin {

    private static final class PlaceHolder {
        private final CountDownLatch latch;
        private final Object request;

        private Object reply;

        PlaceHolder(Object original, CountDownLatch latch) {
            this.request = original;
            this.latch = latch;
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

        /**
         * @return the latch
         */
        public CountDownLatch getLatch() {
            return latch;
        }
    }

    private static final MessageAdmin SINGLETON = new MessageAdmin();

    private final Map<String, PlaceHolder> table = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(MessageAdmin.class);

    private MessageAdmin() {
    }

    /**
     * Get the singleton MessageAdmin object
     *
     * @return singleton instance
     */
    public static MessageAdmin getAdmin() {
        return SINGLETON;
    }

    /**
     * Store a request under the given id, so that an arriving reply can be
     * associated with this request and the waiting party can be signaled by
     * means of a {@link CountDownLatch}
     *
     * @param id
     *            id of the request
     * @param request
     *            request object to store under id
     * @param latch
     *            communication latch to signal when a reply for this request
     *            was received
     */
    public void putRequest(String id, Message request, CountDownLatch latch) {
        log.debug("REQ_ID [{}]", id);
        table.put(id, new PlaceHolder(request, latch));
    }

    /**
     * Try to associate a reply to a previously stored request. If a matching
     * request is found, the owner of the request will be notified with the
     * registered {@link CountDownLatch}
     *
     * @param id
     *            id of the request
     * @param reply
     *            object with the reply
     */
    public void putReply(String id, Message reply) {
        PlaceHolder holder = table.get(id);
        log.debug("RPL_ID [{}] for holder {}", id, holder);
        if (holder != null) {
            holder.setReply(reply);
            CountDownLatch latch = holder.getLatch();
            if(log.isDebugEnabled()) {
                log.debug("{} releasing latch : {}", Thread.currentThread().getName(), latch);
            }
            latch.countDown();
            if(log.isDebugEnabled()) {
                log.debug("{} released latch : {}", Thread.currentThread().getName(), latch);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Failed to match reply: {}", reply);
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
        log.debug("GET_ID [{}] for {}", id, holder);
        if (holder == null || !holder.hasReply()) {
            log.debug("Message with {} not found.", id);
        }
        return holder==null ? null : (Message) holder.getReply();
    }
}
