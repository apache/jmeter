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

import java.io.Closeable;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class Publisher implements Closeable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Connection connection;

    private final Session session;

    private final  MessageProducer producer;
    
    private final Context ctx;
    
    private final boolean staticDest;


    /**
     * Create a publisher using either the jndi.properties file or the provided parameters.
     * Uses a static destination and persistent messages(for backward compatibility)
     * 
     * @param useProps true if a jndi.properties file is to be used
     * @param initialContextFactory the (ignored if useProps is true)
     * @param providerUrl (ignored if useProps is true)
     * @param connfactory
     * @param destinationName
     * @param useAuth (ignored if useProps is true)
     * @param securityPrincipal (ignored if useProps is true)
     * @param securityCredentials (ignored if useProps is true) 
     * @throws JMSException if the context could not be initialised, or there was some other error
     * @throws NamingException 
     */
    public Publisher(boolean useProps, String initialContextFactory, String providerUrl, 
            String connfactory, String destinationName, boolean useAuth,
            String securityPrincipal, String securityCredentials) throws JMSException, NamingException {
        this(useProps, initialContextFactory, providerUrl, connfactory,
                destinationName, useAuth, securityPrincipal,
                securityCredentials, true, false);
    }
    
    /**
     * Create a publisher using either the jndi.properties file or the provided parameters.
     * Uses a static destination (for backward compatibility)
     * 
     * @param useProps true if a jndi.properties file is to be used
     * @param initialContextFactory the (ignored if useProps is true)
     * @param providerUrl (ignored if useProps is true)
     * @param connfactory
     * @param destinationName
     * @param useAuth (ignored if useProps is true)
     * @param securityPrincipal (ignored if useProps is true)
     * @param securityCredentials (ignored if useProps is true)
     * @param useNonPersistentMessages Flag Delivery Mode as Non persistent if true
     * @throws JMSException if the context could not be initialised, or there was some other error
     * @throws NamingException 
     */
    public Publisher(boolean useProps, String initialContextFactory, String providerUrl, 
            String connfactory, String destinationName, boolean useAuth,
            String securityPrincipal, String securityCredentials, boolean useNonPersistentMessages) throws JMSException, NamingException {
        this(useProps, initialContextFactory, providerUrl, connfactory,
                destinationName, useAuth, securityPrincipal,
                securityCredentials, true, useNonPersistentMessages);
    }
    
    /**
     * Create a publisher using either the jndi.properties file or the provided parameters
     * @param useProps true if a jndi.properties file is to be used
     * @param initialContextFactory the (ignored if useProps is true)
     * @param providerUrl (ignored if useProps is true)
     * @param connfactory
     * @param destinationName
     * @param useAuth (ignored if useProps is true)
     * @param securityPrincipal (ignored if useProps is true)
     * @param securityCredentials (ignored if useProps is true)
     * @param staticDestination true is the destination is not to change between loops
     * @param useNonPersistentMessages Flag Delivery Mode as Non persistent if true
     * @throws JMSException if the context could not be initialised, or there was some other error
     * @throws NamingException 
     */
    public Publisher(boolean useProps, String initialContextFactory, String providerUrl, 
            String connfactory, String destinationName, boolean useAuth,
            String securityPrincipal, String securityCredentials,
            boolean staticDestination,  boolean useNonPersistentMessages) throws JMSException, NamingException {
        super();
        boolean initSuccess = false;
        try{
            ctx = InitialContextFactory.getContext(useProps, initialContextFactory, 
                    providerUrl, useAuth, securityPrincipal, securityCredentials);
            connection = Utils.getConnection(ctx, connfactory);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            staticDest = staticDestination;
            if (staticDest) {
                Destination dest = Utils.lookupDestination(ctx, destinationName);
                producer = session.createProducer(dest);
            } else {
                producer = session.createProducer(null);
            }
            if(useNonPersistentMessages) {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            initSuccess = true;
        } finally {
            if(!initSuccess) {
                close();
            }
        }
    }

    public Message publish(String text) throws JMSException,
            NamingException {
        return publish(text, null, null);
    }
    
    public Message publish(String text, String destinationName)
            throws JMSException, NamingException {
        return publish(text, destinationName, null);
    }
    
    public Message publish(String text, String destinationName, Map<String, Object> properties)
            throws JMSException, NamingException {
        TextMessage msg = session.createTextMessage(text);
        return setPropertiesAndSend(destinationName, properties, msg);
    }
    
    public Message publish(Serializable contents) throws JMSException,
            NamingException {
        return publish(contents, null);
    }

    public Message publish(Serializable contents, String destinationName) 
            throws JMSException, NamingException {
        return publish(contents, destinationName, null);
    }
    
    public Message publish(Serializable contents, String destinationName, Map<String, Object> properties)
            throws JMSException, NamingException {
        ObjectMessage msg = session.createObjectMessage(contents);
        return setPropertiesAndSend(destinationName, properties, msg);
    }
    
    public Message publish(byte[] bytes, String destinationName, Map<String, Object> properties)
            throws JMSException, NamingException {
        BytesMessage msg = session.createBytesMessage();
        msg.writeBytes(bytes);
        return setPropertiesAndSend(destinationName, properties, msg);
    }

    public Message publish(Map<String, Object> map) throws JMSException,
            NamingException {
        return publish(map, null, null);
    }
    
    public Message publish(Map<String, Object> map, String destinationName)
            throws JMSException, NamingException {
        return publish(map, destinationName, null);
    }
    
    public MapMessage publish(Map<String, Object> map, String destinationName, Map<String, Object> properties)
            throws JMSException, NamingException {
        MapMessage msg = session.createMapMessage();
        for (Entry<String, Object> me : map.entrySet()) {
            msg.setObject(me.getKey(), me.getValue());
        }
        return (MapMessage)setPropertiesAndSend(destinationName, properties, msg);
    }

    /**
     * @param destinationName 
     * @param properties Map<String, String>
     * @param msg Message
     * @return Message
     * @throws JMSException
     * @throws NamingException
     */
    private Message setPropertiesAndSend(String destinationName,
            Map<String, Object> properties, Message msg)
            throws JMSException, NamingException {
        Utils.addJMSProperties(msg, properties);
        if (staticDest || destinationName == null) {
            producer.send(msg);
        } else {
            Destination dest = Utils.lookupDestination(ctx, destinationName);
            producer.send(dest, msg);
        }
        return msg;
    }

    /**
     * Close will close the session
     */
    @Override
    public void close() {
        Utils.close(producer, log);
        Utils.close(session, log);
        Utils.close(connection, log);
    }
}
