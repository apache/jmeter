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

import java.util.List;

/**
 * The interface SampleSource represents a source of samples for sample consumers.
 *
 * @since 3.0
 */
public interface SampleSource extends Runnable {

    /**
     * Gets the sample context.
     *
     * @return the sampleContext
     */
    SampleContext getSampleContext();

    /**
     * Sets the sample context.
     *
     * @param sampleContext
     *            the sampleContext to set
     */
    void setSampleContext(SampleContext sampleContext);

    /**
     * Sets the specified sample consumers that will consume samples produced by
     * this sample source.
     *
     * @param consumers
     *            consumers to be set
     */
    void setSampleConsumers(List<SampleConsumer> consumers);

    /**
     * Add a sample consumer to this sample source.
     *
     * @param consumer
     *            consumer to be added
     */
    void addSampleConsumer(SampleConsumer consumer);

    /**
     * Remove a sample consumer from this sample source.
     *
     * @param consumer
     *            consumer to be removed
     */
    void removeSampleConsumer(SampleConsumer consumer);
}
