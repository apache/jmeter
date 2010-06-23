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
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.jmeter.protocol.jms.Utils;
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
public class ReceiveSubscriber implements Runnable, Closeable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Connection CONN;

    private final Session SESSION;

    private final MessageConsumer SUBSCRIBER;

    //@GuardedBy("this")
    private int counter;

    // Only MapMessage and TextMessage are currently supported
    private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<Message>();

    private volatile boolean RUN = true;
    // Needs to be volatile to ensure value is picked up

    //@GuardedBy("this")
    private Thread CLIENTTHREAD;

    public ReceiveSubscriber(boolean useProps, String jndi, String url, String connfactory, String destinationName,
            boolean useAuth, String user, String pwd) throws NamingException, JMSException {
        Context ctx = InitialContextFactory.getContext(useProps, jndi, url, useAuth, user, pwd);
        CONN = Utils.getConnection(ctx, connfactory);
        SESSION = CONN.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = Utils.lookupDestination(ctx, destinationName);
        SUBSCRIBER = SESSION.createConsumer(dest);
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
     * Get the message
     * @return the next message from the queue or null if none
     */
    public synchronized Message getMessage() {
        Message msg = queue.poll();
        if (msg != null) {
            counter--;
        }
        return msg;
    }

    /**
     * close() will stop the connection first. Then it closes the subscriber,
     * session and connection.
     */
    public synchronized void close() { // called from testEnded() thread
        this.RUN = false;
        try {
            this.CONN.stop();
            Utils.close(SUBSCRIBER, log);
            Utils.close(SESSION, log);
            Utils.close(CONN, log);
            this.CLIENTTHREAD.interrupt();
            this.CLIENTTHREAD = null;
            queue.clear();
        } catch (JMSException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
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
        while (RUN) {
            try {
                Message message = this.SUBSCRIBER.receive();
                if (message instanceof TextMessage || message instanceof MapMessage) {
                    queue.add(message);
                    count(1);
                } else if (message != null){
                	log.warn("Discarded non Map|TextMessage " +  message);
                }
            } catch (JMSException e) {
                log.error("Communication error: " + e.getMessage());
            }
        }
    }
}
