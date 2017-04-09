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

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.processor.MaxAggregatorFactory;
import org.apache.jmeter.report.processor.MinAggregatorFactory;
import org.apache.jmeter.report.processor.PercentileAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.StaticSeriesSelector;
import org.apache.jmeter.report.processor.graph.SuccessfulElapsedTimeValueSelector;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class ResponseTimePercentilesOverTimeGraphConsumer provides a graph to visualize percentiles
 * over time period.
 * Only successful responses are taken into account for computations
 *
 * @since 3.1
 */
public class ResponseTimePercentilesOverTimeGraphConsumer extends
        AbstractOverTimeGraphConsumer {
    private static final String PERCENTILE_FORMAT = "%dth percentile";
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

    /**
     * Creates the group info for elapsed time percentile depending on jmeter
     * properties.
     *
     * @param propertyKey
     *            the property key
     * @param defaultValue
     *            the default value
     * @param serieName Serie name
     * @return the group info
     */
    private GroupInfo createPercentileGroupInfo(String propertyKey, int defaultValue, String serieName) {
        int property = JMeterUtils.getPropDefault(propertyKey, defaultValue);
        PercentileAggregatorFactory factory = new PercentileAggregatorFactory();
        factory.setPercentileIndex(property);
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName(serieName);

        return new GroupInfo(factory, seriesSelector,
                new SuccessfulElapsedTimeValueSelector(), false, false);
    }

    /**
     * Creates the group info for min elapsed time
     * @return the group info
     */
    private GroupInfo createMinGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Min");
        return new GroupInfo(new MinAggregatorFactory(), seriesSelector,
                new SuccessfulElapsedTimeValueSelector(), false, false);
    }

    /**
     * Creates the group info for max elapsed time
     * @return the group info
     */
    private GroupInfo createMaxGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Max");
        return new GroupInfo(new MaxAggregatorFactory(), seriesSelector,
                new SuccessfulElapsedTimeValueSelector(), false, false);
    }

    /**
     *
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        HashMap<String, GroupInfo> groupInfos = new HashMap<>(2);

        groupInfos.put("aggregate_report_min", //$NON-NLS-1$
                createMinGroupInfo());

        groupInfos.put("aggregate_report_max", //$NON-NLS-1$
                createMaxGroupInfo());

        groupInfos.put("aggregate_rpt_pct1", //$NON-NLS-1$
                createPercentileGroupInfo("aggregate_rpt_pct1", 90, //$NON-NLS-1$
                        String.format(
                                PERCENTILE_FORMAT, Integer.valueOf(90))));

        groupInfos.put("aggregate_rpt_pct2", //$NON-NLS-1$
                createPercentileGroupInfo("aggregate_rpt_pct2", 95, //$NON-NLS-1$
                        String.format(
                                PERCENTILE_FORMAT, Integer.valueOf(95))));

        groupInfos.put("aggregate_rpt_pct3", //$NON-NLS-1$
                createPercentileGroupInfo("aggregate_rpt_pct3", 99,//$NON-NLS-1$
                        String.format(
                                PERCENTILE_FORMAT, Integer.valueOf(99))));

        return groupInfos;
    }
}
