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
package org.apache.jmeter.report.processor;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;

/**
 * Defines a sample consumer<br>
 * <p>
 * A sample consumer is meant to consume samples in order to process them.
 * </p>
 * <p>
 * A sample consumer can consume samples on different channels and each channel
 * is assigned a single metadata structure.
 * </p>
 * <p>
 * A sample consumer is passive, meaning that its <code>consume()</code> service
 * must be called by a third party object.
 * </p>
 * <p>
 * Sample metadata must be provided to the consumer before
 * <code>startConsuming()</code> is called.
 * </p>
 * <p>
 * The following sequence must be observed when consuming samples :
 * </p>
 * <ul>
 * <li>Call <code>setConsumedMetadata()</code> for each channel that will consume
 * samples</li>
 * <li>Call <code>startConsuming()</code> before any call to
 * <code>consume()</code></li>
 * <li>Call <code>consume()</code> for each sample to be consumed by the
 * consumer, specify the channel on which to consume</li>
 * <li>Call <code>stopConsuming()</code> after every sample has been consumed</li>
 * </ul>
 *
 * @since 3.0
 */
public interface SampleConsumer extends SampleProcessor {

    /**
     * Set the metadata of samples that will be consumed on the specified
     * channel.
     *
     * @param sampleMetadata
     *            The sample metadata that are beeing consumed for the
     *            associated channel
     * @param channel
     *            The channel whose sample metadata are beeing defined
     */
    void setConsumedMetadata(SampleMetadata sampleMetadata, int channel);

    /**
     * Start the sample consuming. This step is used by consumer to initialize
     * their process.
     */
    void startConsuming();

    /**
     * Consumes the specified sample ton the specified channel.
     *
     * @param s
     *            The sample to be consumed
     * @param channel
     *            The channel on which the sample is consumed
     */
    void consume(Sample s, int channel);

    /**
     * Stops the consuming process. No sample will be processed after this
     * service has been called.
     */
    void stopConsuming();

}
