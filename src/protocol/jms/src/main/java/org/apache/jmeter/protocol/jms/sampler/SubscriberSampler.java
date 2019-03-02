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

import java.util.Enumeration;
import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.jms.Utils;
import org.apache.jmeter.protocol.jms.client.InitialContextFactory;
import org.apache.jmeter.protocol.jms.client.ReceiveSubscriber;
import org.apache.jmeter.protocol.jms.control.gui.JMSSubscriberGui;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the JMS Subscriber sampler.
 * It supports both receive and onMessage strategies via the ReceiveSubscriber class.
 *
 */
// TODO: do we need to implement any kind of connection pooling?
// If so, which connections should be shared?
// Should threads share connections to the same destination?
// What about cross-thread sharing?

// Note: originally the code did use the ClientPool to "share" subscribers, however since the
// key was "this" and each sampler is unique - nothing was actually shared.

public class SubscriberSampler extends BaseJMSSampler implements Interruptible, ThreadListener, TestStateListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(SubscriberSampler.class);

    // Default wait (ms) for a message if timeouts are not enabled
    // This is the maximum time the sampler can be blocked.
    private static final long DEFAULT_WAIT = 500L;

    // No need to synch/ - only used by sampler
    // Note: not currently added to the ClientPool
    private transient ReceiveSubscriber SUBSCRIBER = null;

    private transient volatile boolean interrupted = false;

    private transient long timeout;

    private transient boolean useReceive;

    // This will be null if initialization succeeds.
    private transient Exception exceptionDuringInit;

    // If true, start/stop subscriber for each sample
    private transient boolean stopBetweenSamples;

    // Don't change the string, as it is used in JMX files
    private static final String CLIENT_CHOICE = "jms.client_choice"; // $NON-NLS-1$
    private static final String TIMEOUT = "jms.timeout"; // $NON-NLS-1$
    private static final String TIMEOUT_DEFAULT = ""; // $NON-NLS-1$
    private static final String DURABLE_SUBSCRIPTION_ID = "jms.durableSubscriptionId"; // $NON-NLS-1$
    private static final String CLIENT_ID = "jms.clientId"; // $NON-NLS-1$
    private static final String JMS_SELECTOR = "jms.selector"; // $NON-NLS-1$
    private static final String DURABLE_SUBSCRIPTION_ID_DEFAULT = "";
    private static final String CLIENT_ID_DEFAULT = ""; // $NON-NLS-1$
    private static final String JMS_SELECTOR_DEFAULT = ""; // $NON-NLS-1$
    private static final String STOP_BETWEEN = "jms.stop_between_samples"; // $NON-NLS-1$
    private static final String SEPARATOR = "jms.separator"; // $NON-NLS-1$
    private static final String SEPARATOR_DEFAULT = ""; // $NON-NLS-1$
    private static final String ERROR_PAUSE_BETWEEN = "jms_error_pause_between"; // $NON-NLS-1$
    private static final String ERROR_PAUSE_BETWEEN_DEFAULT = ""; // $NON-NLS-1$


    private transient boolean START_ON_SAMPLE = false;

    private transient String separator;

    public SubscriberSampler() {
        super();
    }

    /**
     * Create the OnMessageSubscriber client and set the sampler as the message
     * listener.
     * @throws JMSException
     * @throws NamingException
     *
     */
    private void initListenerClient() throws JMSException, NamingException {
        SUBSCRIBER = new ReceiveSubscriber(0, getUseJNDIPropertiesAsBoolean(), getJNDIInitialContextFactory(),
                    getProviderUrl(), getConnectionFactory(), getDestination(), getDurableSubscriptionId(),
                    getClientId(), getJmsSelector(), isUseAuth(), getUsername(), getPassword());
        log.debug("SubscriberSampler.initListenerClient called");
    }

    /**
     * Create the ReceiveSubscriber client for the sampler.
     * @throws NamingException
     * @throws JMSException
     */
    private void initReceiveClient() throws NamingException, JMSException {
        SUBSCRIBER = new ReceiveSubscriber(getUseJNDIPropertiesAsBoolean(),
                getJNDIInitialContextFactory(), getProviderUrl(), getConnectionFactory(), getDestination(),
                getDurableSubscriptionId(), getClientId(), getJmsSelector(), isUseAuth(), getUsername(), getPassword());
        log.debug("SubscriberSampler.initReceiveClient called");
    }

    /**
     * sample method will check which client it should use and call the
     * appropriate client specific sample method.
     *
     * @return the appropriate sample result
     */
    // TODO - should we call start() and stop()?
    @Override
    public SampleResult sample() {
        // run threadStarted only if Destination setup on each sample
        if (!isDestinationStatic()) {
            threadStarted(true);
        }
        SampleResult result = new SampleResult();
        result.setDataType(SampleResult.TEXT);
        result.setSampleLabel(getName());
        result.sampleStart();
        if (exceptionDuringInit != null) {
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseCode("000");
            result.setResponseMessage(exceptionDuringInit.toString());
            handleErrorAndAddTemporize(true);
            return result;
        }
        if (stopBetweenSamples){ // If so, we need to start collection here
            try {
                SUBSCRIBER.start();
            } catch (JMSException e) {
                log.warn("Problem starting subscriber", e);
            }
        }
        StringBuilder buffer = new StringBuilder();
        StringBuilder propBuffer = new StringBuilder();

        int loop = getIterationCount();
        int read = 0;

        long until = 0L;
        long now = System.currentTimeMillis();
        if (timeout > 0) {
            until = timeout + now;
        }
        while (!interrupted
                && (until == 0 || now < until)
                && read < loop) {
            Message msg;
            try {
                msg = SUBSCRIBER.getMessage(calculateWait(until, now));
                if (msg != null){
                    read++;
                    extractContent(buffer, propBuffer, msg, read == loop);
                }
            } catch (JMSException e) {
                String errorCode = Optional.ofNullable(e.getErrorCode()).orElse("");
                log.warn("Error [{}] {}", errorCode, e.toString(), e);

                handleErrorAndAddTemporize(getIsReconnectErrorCode().test(errorCode));
            }
            now = System.currentTimeMillis();
        }
        result.sampleEnd();
        if (getReadResponseAsBoolean()) {
            result.setResponseData(buffer.toString().getBytes()); // TODO - charset?
        } else {
            result.setBytes((long)buffer.toString().length());
        }
        result.setResponseHeaders(propBuffer.toString());
        if (read == 0) {
            result.setResponseCode("404"); // Not found
            result.setSuccessful(false);
        } else if (read < loop) { // Not enough messages found
            result.setResponseCode("500"); // Server error
            result.setSuccessful(false);
        } else {
            result.setResponseCodeOK();
            result.setSuccessful(true);
        }
        result.setResponseMessage(read + " message(s) received successfully of " + loop + " expected");
        result.setSamplerData(loop + " messages expected");
        result.setSampleCount(read);

        if (stopBetweenSamples){
            try {
                SUBSCRIBER.stop();
            } catch (JMSException e) {
                log.warn("Problem stopping subscriber", e);
            }
        }
        // run threadFinished only if Destination setup on each sample (stop Listen queue)
        if (!isDestinationStatic()) {
            threadFinished(true);
        }
        return result;
    }

    /**
     * Try to reconnect if configured to or temporize if not or an exception occurred
     * @param reconnect
     */
    private void handleErrorAndAddTemporize(boolean reconnect) {
        if (reconnect) {
            cleanup();
            initClient();
        }

        if (!reconnect || exceptionDuringInit != null) {
            try {
                long pause = getPauseBetweenErrorsAsLong();
                if(pause > 0) {
                    Thread.sleep(pause);
                }
            } catch (InterruptedException ie) {
                log.warn("Interrupted {}", ie.toString(), ie);
                Thread.currentThread().interrupt();
                interrupted = true;
            }
        }
    }

    /**
     *
     */
    private void cleanup() {
        IOUtils.closeQuietly(SUBSCRIBER);
    }

    /**
     * Calculate the wait time, will never be more than DEFAULT_WAIT.
     *
     * @param until target end time or 0 if timeouts not active
     * @param now current time
     * @return wait time
     */
    private long calculateWait(long until, long now) {
        if (until == 0) {
            return DEFAULT_WAIT; // Timeouts not active
        }
        long wait = until - now; // How much left
        return wait > DEFAULT_WAIT ? DEFAULT_WAIT : wait;
    }

    private void extractContent(StringBuilder buffer, StringBuilder propBuffer,
            Message msg, boolean isLast) {
        if (msg != null) {
            try {
                if (msg instanceof TextMessage){
                    buffer.append(((TextMessage) msg).getText());
                } else if (msg instanceof ObjectMessage){
                    ObjectMessage objectMessage = (ObjectMessage) msg;
                    if(objectMessage.getObject() != null) {
                        buffer.append(objectMessage.getObject().getClass());
                    } else {
                        buffer.append("object is null");
                    }
                } else if (msg instanceof BytesMessage){
                    BytesMessage bytesMessage = (BytesMessage) msg;
                    buffer.append(bytesMessage.getBodyLength() + " bytes received in BytesMessage");
                } else if (msg instanceof MapMessage){
                    MapMessage mapm = (MapMessage) msg;
                    @SuppressWarnings("unchecked") // MapNames are Strings
                    Enumeration<String> enumb = mapm.getMapNames();
                    while(enumb.hasMoreElements()){
                        String name = enumb.nextElement();
                        Object obj = mapm.getObject(name);
                        buffer.append(name);
                        buffer.append(",");
                        buffer.append(obj.getClass().getCanonicalName());
                        buffer.append(",");
                        buffer.append(obj);
                        buffer.append("\n");
                    }
                }
                Utils.messageProperties(propBuffer, msg);
                if(!isLast && !StringUtils.isEmpty(separator)) {
                    propBuffer.append(separator);
                    buffer.append(separator);
                }
            } catch (JMSException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * Initialise the thread-local variables.
     * <br>
     * {@inheritDoc}
     */
    @Override
    public void threadStarted() {
        configureIsReconnectErrorCode();

        // Disabled thread start if listen on sample choice
        if (isDestinationStatic() || START_ON_SAMPLE) {
            timeout = getTimeoutAsLong();
            interrupted = false;
            exceptionDuringInit = null;
            useReceive = getClientChoice().equals(JMSSubscriberGui.RECEIVE_RSC);
            stopBetweenSamples = isStopBetweenSamples();
            setupSeparator();
            initClient();
        }
    }

    private void initClient() {
        exceptionDuringInit = null;
        try {
            if(useReceive) {
                initReceiveClient();
            } else {
                initListenerClient();
            }
            if (!stopBetweenSamples) { // Don't start yet if stop between
                                       // samples
                SUBSCRIBER.start();
            }
        } catch (NamingException | JMSException e) {
            exceptionDuringInit = e;
        }

        if (exceptionDuringInit != null) {
            log.error("Could not initialise client", exceptionDuringInit);
        }
    }

    public void threadStarted(boolean wts) {
        if (wts) {
            START_ON_SAMPLE = true; // listen on sample
        }
        threadStarted();
    }

    /**
     * Close subscriber.
     * <br>
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        if (SUBSCRIBER != null){ // Can be null if init fails
            cleanup();
        }
    }

    public void threadFinished(boolean wts) {
        if (wts) {
            START_ON_SAMPLE = false; // listen on sample
        }
        threadFinished();
    }

    /**
     * Handle an interrupt of the test.
     */
    @Override
    public boolean interrupt() {
        boolean oldvalue = interrupted;
        interrupted = true;   // so we break the loops in SampleWithListener and SampleWithReceive
        return !oldvalue;
    }

    // ----------- get/set methods ------------------- //
    /**
     * Set the client choice. There are two options: ReceiveSusbscriber and
     * OnMessageSubscriber.
     *
     * @param choice
     *            the client to use. One of {@link JMSSubscriberGui#RECEIVE_RSC
     *            RECEIVE_RSC} or {@link JMSSubscriberGui#ON_MESSAGE_RSC
     *            ON_MESSAGE_RSC}
     */
    public void setClientChoice(String choice) {
        setProperty(CLIENT_CHOICE, choice);
    }

    /**
     * Return the client choice.
     *
     * @return the client choice, either {@link JMSSubscriberGui#RECEIVE_RSC
     *         RECEIVE_RSC} or {@link JMSSubscriberGui#ON_MESSAGE_RSC
     *         ON_MESSAGE_RSC}
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

    public String getTimeout(){
        return getPropertyAsString(TIMEOUT, TIMEOUT_DEFAULT);
    }

    public long getTimeoutAsLong(){
        return getPropertyAsLong(TIMEOUT, 0L);
    }

    public void setTimeout(String timeout){
        setProperty(TIMEOUT, timeout, TIMEOUT_DEFAULT);
    }

    public String getDurableSubscriptionId(){
        return getPropertyAsString(DURABLE_SUBSCRIPTION_ID);
    }

    /**
     * @return JMS Client ID
     */
    public String getClientId() {
        return getPropertyAsString(CLIENT_ID, CLIENT_ID_DEFAULT);
    }

    /**
     * @return JMS selector
     */
    public String getJmsSelector() {
        return getPropertyAsString(JMS_SELECTOR, JMS_SELECTOR_DEFAULT);
    }

    public void setDurableSubscriptionId(String durableSubscriptionId){
        setProperty(DURABLE_SUBSCRIPTION_ID, durableSubscriptionId, DURABLE_SUBSCRIPTION_ID_DEFAULT);
    }

    /**
     * @param clientId JMS CLient id
     */
    public void setClientID(String clientId) {
        setProperty(CLIENT_ID, clientId, CLIENT_ID_DEFAULT);
    }

    /**
     * @param jmsSelector JMS Selector
     */
    public void setJmsSelector(String jmsSelector) {
        setProperty(JMS_SELECTOR, jmsSelector, JMS_SELECTOR_DEFAULT);
    }

    /**
     * @return Separator for sampler results
     */
    public String getSeparator() {
        return getPropertyAsString(SEPARATOR, SEPARATOR_DEFAULT);
    }

    /**
     * Separator for sampler results
     *
     * @param text
     *            separator to use for sampler results
     */
    public void setSeparator(String text) {
        setProperty(SEPARATOR, text, SEPARATOR_DEFAULT);
    }

    // This was the old value that was checked for
    private static final String RECEIVE_STR = JMeterUtils.getResString(JMSSubscriberGui.RECEIVE_RSC); // $NON-NLS-1$

    public boolean isStopBetweenSamples() {
        return getPropertyAsBoolean(STOP_BETWEEN, false);
    }

    public void setStopBetweenSamples(boolean selected) {
        setProperty(STOP_BETWEEN, selected, false);
    }

    public void setPauseBetweenErrors(String pause) {
        setProperty(ERROR_PAUSE_BETWEEN, pause, ERROR_PAUSE_BETWEEN_DEFAULT);
    }

    public String getPauseBetweenErrors() {
        return getPropertyAsString(ERROR_PAUSE_BETWEEN, ERROR_PAUSE_BETWEEN_DEFAULT);
    }

    public long getPauseBetweenErrorsAsLong() {
        return getPropertyAsLong(ERROR_PAUSE_BETWEEN, DEFAULT_WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        InitialContextFactory.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        testStarted("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        // NOOP
    }

    /**
     *
     */
    private void setupSeparator() {
        separator = getSeparator();
        separator = separator.replace("\\t", "\t");
        separator = separator.replace("\\n", "\n");
        separator = separator.replace("\\r", "\r");
    }

    private Object readResolve(){
        setupSeparator();
        exceptionDuringInit=null;
        return this;
    }
}
