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
package org.apache.jmeter.report.processor.graph.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.TimeRateAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class TransactionsPerSecondGraphConsumer provides a graph to visualize
 * transactions rate per second.
 *
 * @since 3.0
 */
public class TransactionsPerSecondGraphConsumer extends
        AbstractOverTimeGraphConsumer {

    private static final String SUCCESS_SERIES_SUFFIX = "success";
    private static final String FAILURE_SERIES_SUFFIX = "failure";

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
        GroupInfo value = new GroupInfo(
                new TimeRateAggregatorFactory(),
                new AbstractSeriesSelector(true) {

                    @Override
                    public Iterable<String> select(Sample sample) {
                        String success = sample.getSuccess() ? SUCCESS_SERIES_SUFFIX : FAILURE_SERIES_SUFFIX;
                        String label = sample.getName() + "-" + success;
                        return Arrays.asList(label);
                    }
                },
                // We include Transaction Controller results
                new CountValueSelector(false), false, false);
        return Collections.singletonMap(AbstractGraphConsumer.DEFAULT_GROUP, value);
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
