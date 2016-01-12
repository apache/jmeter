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

package org.apache.jmeter.protocol.mqtt.data.objects;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Message object to hold MQTT message content.
 */
public class Message {
    private byte[] payload;
    private int qos = 0;
    private boolean retained = false;
    private boolean dup = false;
    private long currentTimestamp;

    public Message(byte[] payload, int qos, boolean retained, boolean dup, long currentTimestamp) {
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.dup = dup;
        this.currentTimestamp = currentTimestamp;
    }

    public Message(MqttMessage mqttMessage) {
        this.payload = mqttMessage.getPayload();
        this.qos = mqttMessage.getQos();
        this.retained = mqttMessage.isRetained();
        this.dup = mqttMessage.isDuplicate();
        this.currentTimestamp = System.currentTimeMillis();
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetained() {
        return retained;
    }

    public boolean isDup() {
        return dup;
    }

    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
}
