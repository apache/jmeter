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

package org.apache.jmeter.protocol.mqtt.sampler;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.mqtt.client.ClientPool;
import org.apache.jmeter.protocol.mqtt.paho.clients.AsyncClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BaseClient;
import org.apache.jmeter.protocol.mqtt.paho.clients.BlockingClient;
import org.apache.jmeter.protocol.mqtt.utilities.Constants;
import org.apache.jmeter.protocol.mqtt.utilities.Utils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is MQTT Publisher sample class. The implementation includes publishing of MQTT messages with the sample
 * processing.
 */
public class PublisherSampler extends AbstractSampler implements TestStateListener {


    private transient BaseClient client;
    private int qos = 0;
    private String topicName = StringUtils.EMPTY;
    private String publishMessage = StringUtils.EMPTY;
    private boolean retained;
    private AtomicInteger publishedMessageCount = new AtomicInteger(0);
    private static final String nameLabel = "MQTT Publisher";
    private static final String lineSeparator = System.getProperty("line.separator");

    private static final long serialVersionUID = 233L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String BROKER_URL = "mqtt.broker.url";
    private static final String CLIENT_ID = "mqtt.client.id";
    private static final String TOPIC_NAME = "mqtt.topic.name";
    private static final String RETAINED = "mqtt.message.retained";
    private static final String CLEAN_SESSION = "mqtt.clean.session";
    private static final String KEEP_ALIVE = "mqtt.keep.alive";
    private static final String USERNAME = "mqtt.auth.username";
    private static final String PASSWORD = "mqtt.auth.password";
    private static final String QOS = "mqtt.qos";
    private static final String CLIENT_TYPE = "mqtt.client.type";
    private static final String MESSAGE_INPUT_TYPE = "mqtt.message.input.type";
    private static final String MESSAGE_VALUE = "mqtt.message.input.value";

    // Getters
    public String getBrokerUrl() {
        return getPropertyAsString(BROKER_URL);
    }

    public String getClientId() {
        return getPropertyAsString(CLIENT_ID);
    }

    public String getTopicName() {
        return getPropertyAsString(TOPIC_NAME);
    }

    public boolean isMessageRetained() {
        return getPropertyAsBoolean(RETAINED);
    }

    public boolean isCleanSession() {
        return getPropertyAsBoolean(CLEAN_SESSION);
    }

    public int getKeepAlive() {
        return getPropertyAsInt(KEEP_ALIVE);
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public String getQOS() {
        return getPropertyAsString(QOS);
    }

    public String getClientType() {
        return getPropertyAsString(CLIENT_TYPE);
    }

    public String getMessageInputType() {
        return getPropertyAsString(MESSAGE_INPUT_TYPE);
    }

    public String getMessageValue() {
        return getPropertyAsString(MESSAGE_VALUE);
    }

    public String getNameLabel() {
        return nameLabel;
    }

    // Setters
    public void setBrokerUrl(String brokerURL) {
        setProperty(BROKER_URL, brokerURL.trim());
    }

    public void setClientId(String clientID) {
        setProperty(CLIENT_ID, clientID.trim());
    }

    public void setTopicName(String topicName) {
        setProperty(TOPIC_NAME, topicName.trim());
    }

    public void setMessageRetained(boolean isCleanSession) {
        setProperty(RETAINED, isCleanSession);
    }

    public void setCleanSession(boolean isCleanSession) {
        setProperty(CLEAN_SESSION, isCleanSession);
    }

    public void setKeepAlive(String keepAlive) {
        setProperty(KEEP_ALIVE, keepAlive);
    }

    public void setUsername(String username) {
        setProperty(USERNAME, username.trim());
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password.trim());
    }

    public void setQOS(String qos) {
        setProperty(QOS, qos.trim());
    }

    public void setClientType(String clientType) {
        setProperty(CLIENT_TYPE, clientType.trim());
    }

    public void setMessageInputType(String messageInputType) {
        setProperty(MESSAGE_INPUT_TYPE, messageInputType.trim());
    }

    public void setMessageValue(String messageValue) {
        setProperty(MESSAGE_VALUE, messageValue.trim());
    }

    public PublisherSampler() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        log.debug("Thread ended " + new Date());
        try {
            ClientPool.clearClient();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String arg0) {
        testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        if (log.isDebugEnabled()) {
            log.debug("Thread started " + new Date());
            log.debug("MQTT PublishSampler: ["
                      + Thread.currentThread().getName() + "], hashCode=["
                      + hashCode() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String arg0) {
        testStarted();
    }

    /**
     * Initializes the MQTT client for publishing.
     *
     * @throws MqttException
     */
    private void initClient() throws MqttException {
        String brokerURL = getBrokerUrl();
        String clientId = getClientId();
        topicName = getTopicName();
        retained = isMessageRetained();
        boolean isCleanSession = isCleanSession();
        int keepAlive = getKeepAlive();
        String userName = getUsername();
        String password = getPassword();
        String clientType = getClientType();
        String messageInputType = getMessageInputType();

        // Generating client ID if empty
        if (StringUtils.isEmpty(clientId)) {
            clientId = Utils.generateClientID();
        }

        // Quality
        if (Constants.MQTT_AT_MOST_ONCE.equals(getQOS())) {
            qos = 0;
        } else if (Constants.MQTT_AT_LEAST_ONCE.equals(getQOS())) {
            qos = 1;
        } else if (Constants.MQTT_EXACTLY_ONCE.equals(getQOS())) {
            qos = 2;
        }

        if (Constants.MQTT_MESSAGE_INPUT_TYPE_TEXT.equals(messageInputType)) {
            publishMessage = getMessageValue();
        } else if (Constants.MQTT_MESSAGE_INPUT_TYPE_FILE.equals(messageInputType)) {
            publishMessage = Utils.getFileContent(getMessageValue());
        }

        try {
            if (Constants.MQTT_BLOCKING_CLIENT.equals(clientType)) {
                client = new BlockingClient(brokerURL, clientId, isCleanSession, userName, password, keepAlive);
            } else if (Constants.MQTT_ASYNC_CLIENT.equals(clientType)) {
                client = new AsyncClient(brokerURL, clientId, isCleanSession, userName, password, keepAlive);
            }

            if (null != client) {
                ClientPool.addClient(client);
            }
        } catch (MqttException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getNameLabel());
        result.sampleStart();
        if (client == null || !client.isConnected()) {
            try {
                initClient();
            } catch (MqttException e) {
                result.sampleEnd(); // stop stopwatch
                result.setSuccessful(false);
                // get stack trace as a String to return as document data
                java.io.StringWriter stringWriter = new java.io.StringWriter();
                e.printStackTrace(new java.io.PrintWriter(stringWriter));
                result.setResponseData(stringWriter.toString(), null);
                result.setResponseMessage("Unable publish messages." + lineSeparator + "Exception: " + e.toString());
                result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
                result.setResponseCode("FAILED");
                return result;
            }
        }
        try {
            client.publish(topicName, qos, publishMessage.getBytes(), retained);
            result.setSuccessful(true);
            result.sampleEnd(); // stop stopwatch
            result.setResponseMessage("Sent " + publishedMessageCount.incrementAndGet() + " messages total");
            result.setResponseCode("OK");
            return result;
        } catch (MqttException e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString(), null);
            result.setResponseMessage("Unable publish messages." + lineSeparator + "Exception: " + e.toString());
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("FAILED");
            return result;
        }
    }
}
