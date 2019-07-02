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

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleSelector;

/**
 * The Class AggregateConsumer provides a consumer that can aggregate samples to
 * provide a result
 *
 * @since 3.0
 */
public class AggregateConsumer extends AbstractSampleConsumer {

    private static final String MUST_NOT_BE_NULL = "%s must not be null";

    /** The aggregator. */
    private Aggregator aggregator;

    /** The selector. */
    private SampleSelector<Double> selector;

    /**
     * Gets the aggregator.
     *
     * @return the aggregator
     */
    public final Aggregator getAggregator() {
        return aggregator;
    }

    /**
     * Gets the selector.
     *
     * @return the selector
     */
    public final SampleSelector<Double> getSelector() {
        return selector;
    }

    /**
     * Instantiates a new abstract aggregate consumer.
     *
     * @param aggregator
     *            the aggregator (must not be {@code null})
     * @param selector
     *            the selector (must not be {@code null})
     */
    public AggregateConsumer(Aggregator aggregator,
            SampleSelector<Double> selector) {
        Validate.notNull(aggregator, MUST_NOT_BE_NULL, "aggregator");
        Validate.notNull(selector, MUST_NOT_BE_NULL, "selector");

        this.aggregator = aggregator;
        this.selector = selector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.SampleConsumer#startConsuming()
     */
    @Override
    public void startConsuming() {
        // Broadcast metadata to consumes for each channel
        int channelCount = getConsumedChannelCount();
        for (int i = 0; i < channelCount; i++) {
            super.setProducedMetadata(getConsumedMetadata(i), i);
        }

        super.startProducing();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.SampleConsumer#consume(org.apache.
     * jmeter.report.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {
        aggregator.addValue(selector.select(sample).doubleValue());
        super.produce(sample, channel);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.SampleConsumer#stopConsuming()
     */
    @Override
    public void stopConsuming() {
        setDataToContext(getName(), new ValueResultData(Double.valueOf(aggregator.getResult())));
        super.stopProducing();
    }

}
