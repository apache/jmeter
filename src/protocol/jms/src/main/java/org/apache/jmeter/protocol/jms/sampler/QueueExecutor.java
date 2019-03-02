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

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Executor for (pseudo) synchronous communication. <br>
 *
 */
public interface QueueExecutor {
    /**
     * Sends and receives a message.
     *
     * @param request the message to send
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param expiration messages lifetime in ms
     * @return the received message or <code>null</code>
     * @throws JMSException
     *             in case of an exception from the messaging system
     */
    Message sendAndReceive(Message request,
            int deliveryMode,
            int priority,
            long expiration) throws JMSException;

    /**
     * Close the resources
     * @throws JMSException
     *             in case of an exception from the messaging system
     */
    void close() throws JMSException;
}
