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
 * Defines a sample producer
 * <p>
 * A sample producer is able to produce sample on different channels.
 * </p>
 * <p>
 * Typically, a {@link SampleProducer} can be connected to
 * {@link SampleConsumer} where it will produced sample that will be consumed by
 * the sample consumer.
 * </p>
 * <p>
 * A sample producer can produce samples of different metadata for reach
 * channel/
 * </p>
 * <p>
 * The following production sequence must be observed:
 * </p><ul>
 * <li>Call <code>setProducedMetadata()</code> for each produced channel</li>
 * <li>Call <code>startProducing()</code></li>
 * <li>Call <code>produce()</code> for each sample to produce for every channel</li>
 * <li>Call <code>stopProducing()</code></li>
 * </ul>
 *
 * @since 3.0
 */
public interface SampleProducer extends SampleProcessor {

    /**
     * Set the metadata associated with the specified channel
     *
     * @param metadata
     *            The metadata to be associated to the specified channel
     * @param channel
     *            The channel whoses metadata are being associated with
     */
    void setProducedMetadata(SampleMetadata metadata, int channel);

    /**
     * Start producing samples, must be invoked before any call to
     * <code>produce()</code>
     */
    void startProducing();

    /**
     * Produce a single sample on the specified channel
     *
     * @param s
     *            The sample produced
     * @param channel
     *            The channel on which is produced the sample
     */
    void produce(Sample s, int channel);

    /**
     * Stop producing samples, no <code>produce()</code> call should occur after
     * this service has been called.
     */
    void stopProducing();

}
