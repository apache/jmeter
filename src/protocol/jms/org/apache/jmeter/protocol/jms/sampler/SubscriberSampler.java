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
 */

package org.apache.jmeter.protocol.jms.sampler;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;

import org.apache.jmeter.protocol.jms.control.gui.JMSSubscriberGui;
import org.apache.jmeter.protocol.jms.client.ClientPool;
import org.apache.jmeter.protocol.jms.client.OnMessageSubscriber;
import org.apache.jmeter.protocol.jms.client.ReceiveSubscriber;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class implements the JMS Subcriber sampler
 */
public class SubscriberSampler extends BaseJMSSampler implements Interruptible, TestListener, MessageListener {

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // No need to synch/ - only used by sampler and ClientPool (which does its own synch)
    private transient ReceiveSubscriber SUBSCRIBER = null;

    //@GuardedBy("this")
    private final StringBuffer BUFFER = new StringBuffer();

    //@GuardedBy("this")
    private transient int counter = 0;

    private transient volatile boolean interrupted = false;

    // Don't change the string, as it is used in JMX files
    private static final String CLIENT_CHOICE = "jms.client_choice"; // $NON-NLS-1$

    public SubscriberSampler() {
    }

    public void testEnded(String test) {
        testEnded();
    }

    public void testStarted(String test) {
        testStarted();
    }

    /**
     * testEnded is called by Jmeter's engine.
     * Clears the client pool.
     */
    public void testEnded() {
        log.debug("SubscriberSampler.testEnded called");
        ClientPool.clearClient();
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted() {
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /**
     * Create the OnMessageSubscriber client and set the sampler as the message
     * listener.
     *
     */
    private OnMessageSubscriber initListenerClient() {
        interrupted = false;
        OnMessageSubscriber sub = (OnMessageSubscriber) ClientPool.get(this);
        if (sub == null) {
            sub = new OnMessageSubscriber(this.getUseJNDIPropertiesAsBoolean(), this.getJNDIInitialContextFactory(),
                    this.getProviderUrl(), this.getConnectionFactory(), this.getTopic(), this.isUseAuth(), this
                            .getUsername(), this.getPassword());
            sub.setMessageListener(this);
            sub.resume();
            ClientPool.addClient(sub);
            ClientPool.put(this, sub);
            log.debug("SubscriberSampler.initListenerClient called");
            log.debug("loop count " + this.getIterations());
        }
        return sub;
    }

    /**
     * Create the ReceiveSubscriber client for the sampler.
     */
    private void initReceiveClient() {
        interrupted = false;
        this.SUBSCRIBER = new ReceiveSubscriber(this.getUseJNDIPropertiesAsBoolean(), this
                .getJNDIInitialContextFactory(), this.getProviderUrl(), this.getConnectionFactory(), this.getTopic(),
                this.isUseAuth(), this.getUsername(), this.getPassword());
        this.SUBSCRIBER.resume();
        ClientPool.addClient(this.SUBSCRIBER);
        log.debug("SubscriberSampler.initReceiveClient called");
    }

    /**
     * sample method will check which client it should use and call the
     * appropriate client specific sample method.
     *
     * @return the appropriate sample result
     */
    @Override
    public SampleResult sample() {
        if (this.getClientChoice().equals(JMSSubscriberGui.RECEIVE_RSC)) {
            return sampleWithReceive();
        } else {
            return sampleWithListener();
        }
    }

    /**
     * sample will block until messages are received
     *
     * @return the sample result
     */
    private SampleResult sampleWithListener() {
        SampleResult result = new SampleResult();
        result.setDataType(SampleResult.TEXT);
        result.setSampleLabel(getName());
        initListenerClient();

        int loop = this.getIterationCount();

        result.sampleStart();
        int read;
        while ((read=this.count(0)) < loop && interrupted == false) {
            try {
                Thread.sleep(0, 50);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }
        result.sampleEnd();
        synchronized (this) {// Need to synch because buffer is shared with onMessageHandler
            if (this.getReadResponseAsBoolean()) {
                result.setResponseData(this.BUFFER.toString(), null);
            } else {
                result.setBytes(this.BUFFER.toString().length());
            }
            read=this.count(0);
        }
        result.setSuccessful(true);
        result.setResponseCodeOK();
        result.setResponseMessage(read + " messages received");
        result.setSamplerData(loop + " messages expected");
        result.setSampleCount(read);

        this.resetCount();
        return result;
    }

    /**
     * Sample method uses the ReceiveSubscriber client instead of onMessage
     * approach.
     *
     * @return the sample result
     */
    private SampleResult sampleWithReceive() {
        SampleResult result = new SampleResult();
        result.setDataType(SampleResult.TEXT);
        result.setSampleLabel(getName());
        if (this.SUBSCRIBER == null) {
            this.initReceiveClient();
            this.SUBSCRIBER.start();
        }
        int loop = this.getIterationCount();
        this.SUBSCRIBER.setLoop(loop);

        result.sampleStart();
        while (this.SUBSCRIBER.count(0) < loop && interrupted == false) {
            try {
                Thread.sleep(0, 50);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }
        result.sampleEnd();
        int read = this.SUBSCRIBER.count(0);
        if (this.getReadResponseAsBoolean()) {
            result.setResponseData(this.SUBSCRIBER.getMessage(), null);
        } else {
            result.setBytes(this.SUBSCRIBER.getMessage().length());
        }
        result.setSuccessful(true);
        result.setResponseCodeOK();
        result.setResponseMessage(read + " message(s) received successfully");
        result.setSamplerData(loop + " messages expected");
        result.setSampleCount(read);

        this.SUBSCRIBER.reset();
        return result;
    }

    /**
     * The sampler implements MessageListener directly and sets itself as the
     * listener with the TopicSubscriber.
     */
    public synchronized void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                String content = msg.getText();
                if (content != null) {
                    this.BUFFER.append(getMessageHeaders(message));
                    this.BUFFER.append("JMS Message Text:\n\n");
                    this.BUFFER.append(content);
                    count(1);
                }
            }
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * increment the count and return the new value.
     *
     * @param increment
     * @return the new value
     */
    private synchronized int count(int increment) {
        this.counter += increment;
        return this.counter;
    }

    /**
     * resetCount will set the counter to zero and set the length of the
     * StringBuffer to zero.
     */
    private synchronized void resetCount() {
        this.counter = 0;
        this.BUFFER.setLength(0);
    }

    // ----------- get/set methods ------------------- //
    /**
     * Set the client choice. There are two options: ReceiveSusbscriber and
     * OnMessageSubscriber.
     */
    public void setClientChoice(String choice) {
        setProperty(CLIENT_CHOICE, choice);
    }

    /**
     * Return the client choice.
     *
     * @return the client choice, either RECEIVE_RSC or ON_MESSAGE_RSC
     */
    public String getClientChoice() {
        String choice = getPropertyAsString(CLIENT_CHOICE);
        // Convert the old test plan entry (which is the language dependent string) to the resource name
        if (choice.equals(RECEIVE_STR)){
            choice = JMSSubscriberGui.RECEIVE_RSC;
        } else if (!choice.equals(JMSSubscriberGui.RECEIVE_RSC)){
            choice = JMSSubscriberGui.ON_MESSAGE_RSC;
        }
        return choice;
    }

    /**
     * Handle an interrupt of the test.
     */
    public boolean interrupt() {
        boolean oldvalue = interrupted;
        interrupted = true;   // so we break the loops in SampleWithListener and SampleWithReceive
        return !oldvalue;
    }

    // This was the old value that was checked for
    private final static String RECEIVE_STR = JMeterUtils.getResString(JMSSubscriberGui.RECEIVE_RSC); // $NON-NLS-1$
}
