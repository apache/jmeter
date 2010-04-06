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

import java.io.Serializable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class Publisher {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private TopicConnection connection = null;

    private TopicSession session = null;

    private Topic topic = null;

    private TopicPublisher publisher = null;

    public final boolean isValid;
    
    /**
     * Create a publisher using either the jndi.properties file or the provided parameters
     * @param useProps true if a jndi.properties file is to be used
     * @param initialContextFactory the (ignored if useProps is true)
     * @param providerUrl (ignored if useProps is true)
     * @param connfactory
     * @param topic
     * @param useAuth (ignored if useProps is true)
     * @param securityPrincipal (ignored if useProps is true)
     * @param securityCredentials (ignored if useProps is true)
     */
    // TODO - does it make sense to return a Publisher that has not been created successfully?
    // Might be simpler just to return JMSException
    public Publisher(boolean useProps, String initialContextFactory, String providerUrl, 
            String connfactory, String topic, boolean useAuth,
            String securityPrincipal, String securityCredentials) {
        super();
        Context ctx = initJNDI(useProps, initialContextFactory, 
                providerUrl, useAuth, securityPrincipal, securityCredentials);
        if (ctx != null) {
            initConnection(ctx, connfactory, topic);
        } else {
            log.error("Could not initialize JNDI Initial Context Factory");
        }
        isValid = publisher != null; // This is the last item set up by initConnection
    }

    private Context initJNDI(boolean useProps, String initialContextFactory, 
            String providerUrl, boolean useAuth, String securityPrincipal, String securityCredentials) {
        if (useProps) {
            try {
                return new InitialContext();
            } catch (NamingException e) {
                log.error(e.getMessage());
                return null;
            }
        } else {
            return InitialContextFactory.lookupContext(initialContextFactory, 
                    providerUrl, useAuth, securityPrincipal, securityCredentials);
        }
    }

    private void initConnection(Context ctx, String connfactory, String topicName) {
        try {
            ConnectionFactory.getTopicConnectionFactory(ctx,connfactory);
            connection = ConnectionFactory.getTopicConnection();
            topic = InitialContextFactory.lookupTopic(ctx, topicName);
            session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            publisher = session.createPublisher(topic);
            log.info("created the topic connection successfully");
        } catch (JMSException e) {
            log.error("Connection error: " + e.getMessage());
        }
    }

    public void publish(String text) {
        try {
            TextMessage msg = session.createTextMessage(text);
            publisher.publish(msg);
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }

    public void publish(Serializable contents) {
        try {
            ObjectMessage msg = session.createObjectMessage(contents);
            publisher.publish(msg);
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Close will close the session
     */
    public void close() {
        try {
            log.info("Publisher close()");
            if (publisher != null){
                publisher.close();
            }
            if (session != null){
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            publisher = null;
            session = null;
            connection = null;
        } catch (JMSException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage());
            if (e instanceof Error){
                throw (Error) e;
            }
            if (e instanceof RuntimeException){
                throw (RuntimeException) e;
            }
        }
    }

}
