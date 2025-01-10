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

import java.util.Collections;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.Aggregator;
import org.apache.jmeter.report.processor.MeanAggregator;
import org.apache.jmeter.report.processor.MeanAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.GraphValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class ActiveThreadsGraphConsumer provides a graph to visualize active threads
 * per time period (defined by granularity)
 *
 * @since 3.0
 */
public class ActiveThreadsGraphConsumer extends AbstractOverTimeGraphConsumer {

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

            @Override
            public Iterable<String> select(Sample sample) {
                if (sample.isEmptyController()) {
                    return Collections.emptyList();
                }
                String threadName = sample.getThreadName();
                int index = threadName.lastIndexOf(' ');
                if (index >= 0) {
                    threadName = threadName.substring(0, index);
                }
                return Collections.singletonList(threadName);
            }
        };

        GraphValueSelector graphValueSelector = (series, sample) -> {
            if (!sample.isEmptyController()) {
                return (double) sample.getGroupThreads();
            } else {
                return null;
            }
        };

        return Collections.singletonMap(
                AbstractGraphConsumer.DEFAULT_GROUP,
                new GroupInfo(new MeanAggregatorFactory() {
                    @Override
                    protected Aggregator createAggregator() {
                        return new MeanAggregator() {
                            @Override
                            public double getResult() {
                                return mean.getResult();
                            }
                        };
                    }
                }, seriesSelector, graphValueSelector, false, false));
    }

}
