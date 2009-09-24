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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Receives messages in a separate thread until told to stop.
 * Run loop permanently receives messages; the sampler calls reset()
 * when it has taken enough messages.
 *
 */
/*
 * TODO Needs rework - there is a window between receiving a message and calling reset()
 * which means that a message can be lost. It's not clear why a separate thread is needed,
 * given that the sampler loops until enough samples have been received.
 * Also, messages are received in wait mode, so the RUN flag won't be checked until
 * at least one more message has been received.
*/
public class ReceiveSubscriber implements Runnable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final TopicConnection CONN;

    private final TopicSession SESSION;

    private final TopicSubscriber SUBSCRIBER;

    //@GuardedBy("this")
    private int counter;

    private int loop = 1; // TODO never read

    //@GuardedBy("this")
    private StringBuffer buffer = new StringBuffer();

    //@GuardedBy("this")
    private volatile boolean RUN = true;
    // Needs to be volatile to ensure value is picked up

    //@GuardedBy("this")
    private Thread CLIENTTHREAD;

    public ReceiveSubscriber(boolean useProps, String jndi, String url, String connfactory, String topic,
            boolean useAuth, String user, String pwd) {
        Context ctx = initJNDI(useProps, jndi, url, useAuth, user, pwd);
        TopicConnection _conn = null;
        Topic _topic = null;
        TopicSession _session = null;
        TopicSubscriber _subscriber = null;
        if (ctx != null) {
            try {
                ConnectionFactory.getTopicConnectionFactory(ctx,connfactory);
                _conn = ConnectionFactory.getTopicConnection();
                _topic = InitialContextFactory.lookupTopic(ctx, topic);
                _session = _conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
                _subscriber = _session.createSubscriber(_topic);
                log.info("created the topic connection successfully");
            } catch (JMSException e) {
                log.error("Connection error: " + e.getMessage());
            }
        } else {
            log.error("Could not initialize JNDI Initial Context Factory");
        }
        this.CONN = _conn;
        this.SESSION = _session;
        this.SUBSCRIBER = _subscriber;
    }

    /**
     * Initialize the JNDI initial context
     *
     * @param useProps
     * @param jndi
     * @param url
     * @param useAuth
     * @param user
     * @param pwd
     * @return  the JNDI initial context or null
     */
    // Called by ctor
    private Context initJNDI(boolean useProps, String jndi, String url, boolean useAuth, String user, String pwd) {
        if (useProps) {
            try {
                return new InitialContext();
            } catch (NamingException e) {
                log.error(e.getMessage());
                return null;
            }
        } else {
            return InitialContextFactory.lookupContext(jndi, url, useAuth, user, pwd);
        }
    }

    /**
     * Set the number of iterations for each call to sample()
     *
     * @param loop
     */
    public void setLoop(int loop) {
        this.loop = loop;
    }

    /**
     * Resume will call Connection.start() and begin receiving messages from the
     * JMS provider.
     */
    public void resume() {
        if (this.CONN == null) {
            log.error("Connection not set up");
            return;
        }
        try {
            this.CONN.start();
        } catch (JMSException e) {
            log.error("failed to start recieving");
        }
    }

    /**
     * Get the message as a string
     *
     */
    public synchronized String getMessage() {
        return this.buffer.toString();
    }

    /**
     * Get the message(s) as an array of byte[]
     * 
     */
    public synchronized byte[] getByteResult() {
        return this.buffer.toString().getBytes();
    }

    /**
     * close() will stop the connection first. Then it closes the subscriber,
     * session and connection.
     */
    public synchronized void close() { // called from testEnded() thread
        try {
            this.RUN = false;
            this.CONN.stop();
            this.SUBSCRIBER.close();
            this.SESSION.close();
            this.CONN.close();
            this.CLIENTTHREAD.interrupt();
            this.CLIENTTHREAD = null;
            this.buffer.setLength(0);
        } catch (JMSException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Reset the receiver ready for receiving any further messages
     */
    public synchronized void reset() {
        counter = 0;
        this.buffer.setLength(0);
    }

    /**
     * Increment the count and return the new value
     *
     * @param increment
     */
    public synchronized int count(int increment) {
        counter += increment;
        return counter;
    }

    /**
     * start will create a new thread and pass this class. once the thread is
     * created, it calls Thread.start().
     */
    public void start() {
        // No point starting thread unless subscriber exists
        if (SUBSCRIBER == null) {
            log.error("Subscriber has not been set up");
            return;
        }
        this.CLIENTTHREAD = new Thread(this, "Subscriber2");
        this.CLIENTTHREAD.start();
    }

    /**
     * run calls listen to begin listening for inbound messages from the
     * provider.
     * 
     * Updates the count field so the caller can check how many messages have been receieved.
     * 
     */
    public void run() {
        if (SUBSCRIBER == null) { // just in case
            log.error("Subscriber has not been set up");
            return;
        }
        while (RUN) {
            try {
                Message message = this.SUBSCRIBER.receive();
                if (message != null && message instanceof TextMessage) {
                    TextMessage msg = (TextMessage) message;
                    String text = msg.getText();
                    if (text.trim().length() > 0) {
                        synchronized (this) {
                            this.buffer.append(text);
                            count(1);
                        }
                    }
                }
            } catch (JMSException e) {
                log.error("Communication error: " + e.getMessage());
            }
        }
    }
}
