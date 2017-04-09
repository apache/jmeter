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
import org.apache.jmeter.report.core.SamplePredicate;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;

/**
 * <p>
 * The class FilterConsumer provides a way to discard samples in a consumer
 * chain. This class uses a predicate for the filtering decision.
 * </p>
 * 
 * <ul>
 * <li>
 * When reverseFilter is false, samples are discarded if none predicate is
 * defined or samples don't match the predicate.</li>
 * <li>
 * When reverseFilter is true, samples are discarded if a predicate is defined
 * and samples match the predicate.</li>
 * </ul>
 * 
 * @since 3.0
 */
public class FilterConsumer extends AbstractSampleConsumer {
    private SamplePredicate samplePredicate;

    private boolean reverseFilter = false;

    /**
     * Checks if the filtering is reversed.
     *
     * @return true if the filtering is reversed; otherwise false.
     */
    public final boolean isReverseFilter() {
        return reverseFilter;
    }

    /**
     * Reverses the filtering decision.
     *
     * @param reverseFilter
     *            the filter mode to set
     */
    public final void setReverseFilter(boolean reverseFilter) {
        this.reverseFilter = reverseFilter;
    }

    /**
     * Gets the sample predicate used to filter the samples.
     *
     * @return the sample predicate used to filter the samples.
     */
    public final SamplePredicate getSamplePredicate() {
        return samplePredicate;
    }

    /**
     * Sets the sample predicate used to filter the samples.
     *
     * @param samplePredicate
     *            the new sample predicate.
     */
    public final void setSamplePredicate(SamplePredicate samplePredicate) {
        this.samplePredicate = samplePredicate;
    }

    @Override
    public void startConsuming() {
        // Broadcast metadata to consumers for each channel
        int channelCount = getConsumedChannelCount();
        for (int i = 0; i < channelCount; i++) {
            super.setProducedMetadata(getConsumedMetadata(i), i);
        }
        super.startProducing();
    }

    @Override
    public void consume(Sample sample, int channel) {
        // The sample is reproduced if :
        // A predicate is defined and the sample matches it when reverseFilter
        // is false.
        // OR
        // None predicate is defined or the sample does not match when
        // reverseFilter is true.
        if ((!reverseFilter && samplePredicate != null && samplePredicate
                .matches(sample))
                || (reverseFilter && (samplePredicate == null || !samplePredicate
                        .matches(sample)))) {
            super.produce(sample, channel);
        }
    }

    @Override
    public void stopConsuming() {
        super.stopProducing();
    }
}
