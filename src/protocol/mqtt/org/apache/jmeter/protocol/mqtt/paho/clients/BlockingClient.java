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

package org.apache.jmeter.protocol.mqtt.paho.clients;

import org.apache.jmeter.protocol.mqtt.data.objects.Message;
import org.apache.jorphan.logging.LoggingManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 * <p/>
 * It can be run from the command line in one of two modes:
 * - as a publisher, sending a single message to a topic on the server
 * - as a subscriber, listening for messages from the server
 * <p/>
 * There are three versions of the sample that implement the same features
 * but do so using using different programming styles:
 * <ol>
 * <li>Sample (this one) which uses the API which blocks until the operation completes</li>
 * <li>SampleAsyncWait shows how to use the asynchronous API with waiters that block until
 * an action completes</li>
 * <li>SampleAsyncCallBack shows how to use the asynchronous API where events are
 * used to notify the application when an action completes<li>
 * </ol>
 * <p/>
 * If the application is run with the -h parameter then info is displayed that
 * describes all of the options / parameters.
 */
public class BlockingClient extends BaseClient {

    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();
    private MqttClient client;
    private String brokerUrl;

    /**
     * Constructs an instance of the sample client wrapper
     *
     * @param brokerUrl    the url of the server to connect to
     * @param clientId     the client id to connect with
     * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
     * @param userName     the username to connect with
     * @param password     the password for the user
     * @throws MqttException
     */
    public BlockingClient(String brokerUrl, String clientId, boolean cleanSession, String userName,
                          String password, int keepAlive) throws MqttException {
        this.brokerUrl = brokerUrl;
        String testPlanFileDir = System.getProperty("java.io.tmpdir") + File.separator + "mqtt" + File.separator +
                                 clientId + File.separator + Thread.currentThread().getId();
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(testPlanFileDir);

        // Construct the connection options object that contains connection parameters
        // such as cleanSession and LWT
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(cleanSession);
        if (password != null && !password.isEmpty()) {
            conOpt.setPassword(password.toCharArray());
        }
        if (userName != null && !userName.isEmpty()) {
            conOpt.setUserName(userName);
        }

        // Setting keep alive time
        conOpt.setKeepAliveInterval(keepAlive);

        // Construct an MQTT blocking mode client
        client = new MqttClient(this.brokerUrl, clientId, dataStore);

        // Set this wrapper as the callback handler
        client.setCallback(this);

        // Connect to the MQTT server
        log.info("Connecting to " + brokerUrl + " with client ID '" + client.getClientId() + "' and cleanSession is " +
                                                                String.valueOf(cleanSession) + " as a blocking client");
        client.connect(conOpt);
        log.info("Connected");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() throws MqttException {
        // Disconnect the client
        client.disconnect();
        log.info("Disconnected");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(String topicName, int qos, byte[] payload, boolean isRetained) throws MqttException {
        // Create and configure a message
        MqttMessage message = new MqttMessage(payload);
        message.setRetained(isRetained);
        message.setQos(qos);

        // Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        client.publish(topicName, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(String topicName, int qos) throws MqttException {
        mqttMessageStorage = new ConcurrentLinkedQueue<Message>();
        receivedMessageCounter = new AtomicLong(0);

        // Subscribe to the requested topic
        // The QoS specified is the maximum level that messages will be sent to the client at.
        // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
        // be downgraded to 1 when delivering to the client but messages published at 1 and 0
        // will be received at the same level they were published at.
        log.info("Subscribing to topic \"" + topicName + "\" qos " + qos);
        client.subscribe(topicName, qos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.info("Connection to " + brokerUrl + " lost!" + cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        Message newMessage = new Message(message);
        mqttMessageStorage.add(newMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException{
        try {
            client.disconnect();
        } catch (MqttException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
