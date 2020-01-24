/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.report.processor.graph.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.TimeRateAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.GraphValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class HitsPerSecondGraphConsumer provides a graph to visualize bytes throughput
 * per time period (defined by granularity)
 *
 * @since 3.0
 */
public class BytesThroughputGraphConsumer extends AbstractOverTimeGraphConsumer {

    private static final String RECEIVED_BYTES_SERIES_LABEL = "Bytes received per second";
    private static final String SENT_BYTES_SERIES_LABEL = "Bytes sent per second";

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.csv.processor.impl.AbstractOverTimeGraphConsumer
     * #createTimeStampKeysSelector()
     */
    @Override
    protected TimeStampKeysSelector createTimeStampKeysSelector() {
        TimeStampKeysSelector keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        return keysSelector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        AbstractSeriesSelector seriesSelector = new AbstractSeriesSelector() {
            private final Iterable<String> values = Arrays.asList(
                    RECEIVED_BYTES_SERIES_LABEL,
                    SENT_BYTES_SERIES_LABEL);

            @Override
            public Iterable<String> select(Sample sample) {
                return values;
            }
        };

        GraphValueSelector graphValueSelector = (series, sample) -> {
            // Ignore Transaction Controller results
            if (sample.isController()) {
                return null;
            } else {
                return (double) (RECEIVED_BYTES_SERIES_LABEL.equals(series)
                        ? sample.getReceivedBytes()
                        : sample.getSentBytes());
            }
        };

        return Collections.singletonMap(
                AbstractGraphConsumer.DEFAULT_GROUP,
                new GroupInfo(new TimeRateAggregatorFactory(), seriesSelector, graphValueSelector, false, false));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.csv.processor.impl.AbstractOverTimeGraphConsumer
     * #setGranularity(long)
     */
    @Override
    public void setGranularity(long granularity) {
        super.setGranularity(granularity);
    }

    @Override
    public void initialize() {
        super.initialize();
        // Override the granularity of the aggregators factory
        ((TimeRateAggregatorFactory) getGroupInfos().get(
                AbstractGraphConsumer.DEFAULT_GROUP).getAggregatorFactory())
                .setGranularity(getGranularity());
    }
}
