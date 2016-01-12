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
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client API in
 * non-blocking waiter mode.
 * <p/>
 * It can be run from the command line in one of two modes:
 * - as a publisher, sending a single message to a topic on the server
 * - as a subscriber, listening for messages from the server
 * <p/>
 * There are three versions of the sample that implement the same features
 * but do so using using different programming styles:
 * <ol>
 * <li>Sample which uses the API which blocks until the operation completes</li>
 * <li>SampleAsyncWait (this one) shows how to use the asynchronous API with waiters that block until
 * an action completes</li>
 * <li>SampleAsyncCallBack shows how to use the asynchronous API where events are
 * used to notify the application when an action completes<li>
 * </ol>
 * <p/>
 * If the application is run with the -h parameter then info is displayed that
 * describes all of the options / parameters.
 */

public class AsyncClient extends BaseClient {

    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();
    private MqttAsyncClient client;
    private String brokerUrl;

    /**
     * Constructs an instance of the sample client wrapper
     *
     * @param brokerUrl    the url to connect to
     * @param clientId     the client id to connect with
     * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
     * @param userName     the username to connect with
     * @param password     the password for the user
     * @throws MqttException
     */
    public AsyncClient(String brokerUrl, String clientId, boolean cleanSession,
                       String userName, String password, int keepAlive) throws MqttException {
        this.brokerUrl = brokerUrl;

        String testPlanFileDir = System.getProperty("java.io.tmpdir") + File.separator + "mqtt" + File.separator +
                                                            clientId + File.separator + Thread.currentThread().getId();
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(testPlanFileDir);

        try {
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

            // Construct a non-blocking MQTT client instance
            client = new MqttAsyncClient(this.brokerUrl, clientId, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);

            // Connect to the MQTT server
            // issue a non-blocking connect and then use the token to wait until the
            // connect completes. An exception is thrown if connect fails.
            log.info("Connecting to " + brokerUrl + " with client ID '" + client.getClientId() + "' and cleanSession " +
                     "                                  is " + String.valueOf(cleanSession) + " as an async clientt");
            IMqttToken conToken = client.connect(conOpt, null, null);
            conToken.waitForCompletion();
            log.info("Connected");

        } catch (MqttException e) {
            log.info("Unable to set up client: " + e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(String topicName, int qos, byte[] payload, boolean isRetained) throws MqttException {
        // Construct the message to send
        MqttMessage message = new MqttMessage(payload);
        message.setRetained(isRetained);
        message.setQos(qos);

        // Send the message to the server, control is returned as soon
        // as the MQTT client has accepted to deliver the message.
        // Use the delivery token to wait until the message has been
        // delivered
        IMqttDeliveryToken pubToken = client.publish(topicName, message, null, null);
        pubToken.waitForCompletion();
        log.info("Published");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(String topicName, int qos) throws MqttException {
        mqttMessageStorage = new ConcurrentLinkedQueue<Message>();
        receivedMessageCounter = new AtomicLong(0);

        // Subscribe to the requested topic.
        // Control is returned as soon client has accepted to deliver the subscription.
        // Use a token to wait until the subscription is in place.
        log.info("Subscribing to topic \"" + topicName + "\" qos " + qos);
        IMqttToken subToken = client.subscribe(topicName, qos, null, null);
        subToken.waitForCompletion();
        log.info("Subscribed to topic \"" + topicName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionLost(Throwable cause) {
        // Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        log.info("Connection to " + brokerUrl + " lost!" + cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Called when a message has been delivered to the
        // server. The token passed in here is the same one
        // that was passed to or returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver and
        // uses the token.waitForCompletion() call in the main thread which
        // blocks until the delivery has completed.
        // Additionally the deliveryComplete method will be called if
        // the callback is set on the client
        //
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendinTokens method will provide tokens for any messages
        // that are still to be delivered.
        try {
            log.info("Delivery complete callback: Publish Completed " + token.getMessage());
        } catch (Exception ex) {
            log.info("Exception in delivery complete callback" + ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {
        Message newMessage = new Message(mqttMessage);
        mqttMessageStorage.add(newMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() throws MqttException {
        // Disconnect the client
        // Issue the disconnect and then use the token to wait until
        // the disconnect completes.
        log.info("Disconnecting");
        IMqttToken discToken = client.disconnect(null, null);
        discToken.waitForCompletion();
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
    public void close() throws IOException {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }
}
