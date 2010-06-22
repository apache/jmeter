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

package org.apache.jmeter.protocol.jms;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log.Logger;

/**
 * Utility methods for JMS protocol.
 * WARNING - the API for this class is likely to change!
 */
public final class Utils {

    public static void close(MessageConsumer closeable, Logger log){
        if (closeable != null){
            try {
                closeable.close();
            } catch (JMSException e) {
                log.error("Error during close: ", e);
            }
        }
    }

    public static void close(Session closeable, Logger log) {
        if (closeable != null){
            try {
                closeable.close();
            } catch (JMSException e) {
                log.error("Error during close: ", e);
            }
        }
    }

    public static void close(Connection closeable, Logger log) {
        if (closeable != null){
            try {
                closeable.close();
            } catch (JMSException e) {
                log.error("Error during close: ", e);
            }
        }
    }

    public static void close(MessageProducer closeable, Logger log) {
        if (closeable != null){
            try {
                closeable.close();
            } catch (JMSException e) {
                log.error("Error during close: ", e);
            }
        }
    }

    public static String messageProperties(Message msg){
        return messageProperties(new StringBuilder(), msg).toString();
    }

    public static StringBuilder messageProperties(StringBuilder sb, Message msg){
        requestHeaders(sb, msg);
        sb.append("Properties:\n");
        Enumeration<?> rme;
        try {
            rme = msg.getPropertyNames();
            while(rme.hasMoreElements()){
                String name=(String) rme.nextElement();
                sb.append(name).append('\t');
                String value=msg.getStringProperty(name);
                sb.append(value).append('\n');
            }
        } catch (JMSException e) {
            sb.append("\nError: "+e.toString());
        }
        return sb;
    }
    
    public static StringBuilder requestHeaders(StringBuilder sb, Message msg){
        try {
            sb.append("JMSCorrelationId ").append(msg.getJMSCorrelationID()).append('\n');
            sb.append("JMSMessageId     ").append(msg.getJMSMessageID()).append('\n');
            sb.append("JMSTimestamp     ").append(msg.getJMSTimestamp()).append('\n');
            sb.append("JMSType          ").append(msg.getJMSType()).append('\n');
            sb.append("JMSExpiration    ").append(msg.getJMSExpiration()).append('\n');
            sb.append("JMSPriority      ").append(msg.getJMSPriority()).append('\n');
            sb.append("JMSDestination   ").append(msg.getJMSDestination()).append('\n');
        } catch (JMSException e) {
            sb.append("\nError: "+e.toString());
        }
        return sb;
    }

    /**
     * Method will lookup a given topic using JNDI.
     *
     * @param context
     * @param name the topic name
     * @return the topic
     * @throws NamingException if the name cannot be found as a Topic
     */
    public static Topic lookupTopic(Context context, String name) throws NamingException {
        Object o = context.lookup(name);
        if (o instanceof Topic){
            return (Topic) o;
        }
        throw new NamingException("Found: "+name+"; expected Topic, but was: "+o.getClass().getName());
    }

    /**
     * Method will lookup a given topic using JNDI.
     *
     * @param context
     * @param name the Queue name
     * @return the Queue
     * @throws NamingException if the name cannot be found as a Queue
     */
    public static Queue lookupQueue(Context context, String name) throws NamingException {
        Object o = context.lookup(name);
        if (o instanceof Queue){
            return (Queue) o;
        }
        throw new NamingException("Found: "+name+"; expected Queue, but was: "+o.getClass().getName());
    }

}
