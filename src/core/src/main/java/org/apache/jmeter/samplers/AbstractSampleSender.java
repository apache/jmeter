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

package org.apache.jmeter.samplers;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Base class for SampleSender implementations
 */
public abstract class AbstractSampleSender implements SampleSender {

    // Note: this is an instance field (and is not transient), so is created by the JMeter client
    // and propagated to the server instance by RMI.
    // [a static field would be recreated on the server, and would pick up the server properties]
    private final boolean isClientConfigured = JMeterUtils.getPropDefault("sample_sender_client_configured", true); // $NON-NLS-1$

    /**
     * @return boolean indicates how SampleSender configuration is done, @{code true}
     *         means use client properties and send to servers, {@code false} means use
     *         server configurations
     */
    public boolean isClientConfigured() {
        return isClientConfigured;
    }

    /**
     *
     */
    public AbstractSampleSender() {
        super();
    }

    @Override
    public void testEnded() {
        // Not used
    }

}
