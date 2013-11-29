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
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.jms.sampler.JMSProperties;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Utility methods for JMS protocol.
 * WARNING - the API for this class is likely to change!
 */
public final class Utils {
    private static final Logger log = LoggingManager.getLoggerForClass();

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
     * Method will lookup a given destination (topic/queue) using JNDI.
     *
     * @param context
     * @param name the destination name
     * @return the destination, never null
     * @throws NamingException if the name cannot be found as a Destination
     */
    public static Destination lookupDestination(Context context, String name) throws NamingException {
        Object o = context.lookup(name);
        if (o instanceof Destination){
            return (Destination) o;
        }
        throw new NamingException("Found: "+name+"; expected Destination, but was: "+(o!=null ? o.getClass().getName() : "null"));
    }

    /**
     * Obtain the queue connection from the context and factory name.
     * 
     * @param ctx
     * @param factoryName
     * @return the queue connection
     * @throws JMSException
     * @throws NamingException
     */
    public static Connection getConnection(Context ctx, String factoryName) throws JMSException, NamingException {
        Object objfac = null;
        try {
            objfac = ctx.lookup(factoryName);
        } catch (NoClassDefFoundError e) {
            throw new NamingException("Lookup failed: "+e.toString());
        }
        if (objfac instanceof javax.jms.ConnectionFactory) {
            @SuppressWarnings("unchecked") // The environment is supposed to use String keys only
            Map<String, Object> env = (Map<String, Object>)ctx.getEnvironment();
            if(env.containsKey(Context.SECURITY_PRINCIPAL)) {
                String username = (String)env.get(Context.SECURITY_PRINCIPAL);
                String password = (String)env.get(Context.SECURITY_CREDENTIALS);
                return ((javax.jms.ConnectionFactory) objfac).createConnection(username, password);                
            }
            else {
                return ((javax.jms.ConnectionFactory) objfac).createConnection();
            }
        }
        throw new NamingException("Expected javax.jms.ConnectionFactory, found "+(objfac != null ? objfac.getClass().getName(): "null"));
    }
    
    /**
     * Set JMS Properties to msg
     * @param msg Message
     * @param map Map<String, String>
     * @throws JMSException
     */
    public static void addJMSProperties(Message msg, Map<String, Object> map) throws JMSException {
        if(map == null) {
            return;
        }
        for (Map.Entry<String, Object> me : map.entrySet()) {
            String name = me.getKey();
            Object value = me.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Adding property [" + name + "=" + value + "]");
            }

            // WebsphereMQ does not allow corr. id. to be set using setStringProperty()
            if("JMSCorrelationID".equalsIgnoreCase(name)) { // $NON-NLS-1$
                msg.setJMSCorrelationID((String)value);
            } else {
                msg.setObjectProperty(name, value);
            }
        }
    }


    /**
     * Converts {@link Arguments} to {@link JMSProperties} defaulting to String type
     * Used to convert version <= 2.10 test plans
     * @param args {@link Arguments}
     * @return jmsProperties {@link JMSProperties}
     */
    public static final JMSProperties convertArgumentsToJmsProperties(Arguments args) {
        JMSProperties jmsProperties = new JMSProperties();
        Map<String,String>  map = args.getArgumentsAsMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            jmsProperties.addJmsProperty(entry.getKey(), entry.getValue());
        }
        return jmsProperties;
    }
}
