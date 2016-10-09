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
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.Closeable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is the template class for both Async and Sync MQTT clients.
 */
public abstract class BaseClient implements MqttCallback, Closeable {
    protected ConcurrentLinkedQueue<Message> mqttMessageStorage = null;
    protected AtomicLong receivedMessageCounter = null;

    public abstract void publish(String topicName, int qos, byte[] payload, boolean isRetained) throws MqttException;
    public abstract void subscribe(String topicName, int qos) throws MqttException;
    public abstract void disconnect() throws MqttException;
    public abstract  boolean isConnected();

    public ConcurrentLinkedQueue<Message> getReceivedMessages(){
        return mqttMessageStorage;
    }
    public AtomicLong getReceivedMessageCounter(){
        return receivedMessageCounter;
    }
}
