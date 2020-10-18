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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.processor.AggregatorFactory;
import org.apache.jmeter.report.processor.MaxAggregatorFactory;
import org.apache.jmeter.report.processor.MedianAggregatorFactory;
import org.apache.jmeter.report.processor.MinAggregatorFactory;
import org.apache.jmeter.report.processor.PercentileAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.StaticSeriesSelector;
import org.apache.jmeter.report.processor.graph.SuccessfulElapsedTimeValueSelector;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class ResponseTimePercentilesOverTimeGraphConsumer provides a graph to
 * visualize percentiles over time period.
 * Only successful responses are taken into account for computations.
 *
 * @since 3.1
 */
public class ResponseTimePercentilesOverTimeGraphConsumer
        extends AbstractOverTimeGraphConsumer {
    private static final String PCT1_LABEL = JMeterUtils.getPropDefault(
            "aggregate_rpt_pct1", "90");
    private static final String PCT2_LABEL = JMeterUtils.getPropDefault(
            "aggregate_rpt_pct2", "95");
    private static final String PCT3_LABEL = JMeterUtils.getPropDefault(
            "aggregate_rpt_pct3", "99");

    @Override
    protected TimeStampKeysSelector createTimeStampKeysSelector() {
        TimeStampKeysSelector keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        return keysSelector;
    }

    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        HashMap<String, GroupInfo> groupInfos = new HashMap<>(8);

        groupInfos.put("aggregate_report_min", createMinGroupInfo());
        groupInfos.put("aggregate_report_max", createMaxGroupInfo());
        groupInfos.put("aggregate_report_median", createMedianGroupInfo());
        groupInfos.put("aggregate_rpt_pct1",
                createPercentileGroupInfo("aggregate_rpt_pct1", PCT1_LABEL));
        groupInfos.put("aggregate_rpt_pct2",
                createPercentileGroupInfo("aggregate_rpt_pct2", PCT2_LABEL));
        groupInfos.put("aggregate_rpt_pct3",
                createPercentileGroupInfo("aggregate_rpt_pct3", PCT3_LABEL));

        return groupInfos;
    }

    private String formatPercentile(String percentileLabel) {
        return String.format("%sth percentile", percentileLabel);
    }

    private GroupInfo createMinGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Min");
        return createGroupInfo(new MinAggregatorFactory(), seriesSelector);
    }

    private GroupInfo createMaxGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Max");
        return createGroupInfo(new MaxAggregatorFactory(), seriesSelector);
    }

    private GroupInfo createMedianGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Median");
        return createGroupInfo(new MedianAggregatorFactory(), seriesSelector);
    }

    private GroupInfo createPercentileGroupInfo(String propKey, String label) {
        String seriesName = formatPercentile(label);
        double defaultValue = new BigDecimal(label).setScale(2, RoundingMode.CEILING).doubleValue();
        double property = JMeterUtils.getPropDefault(propKey, defaultValue);
        PercentileAggregatorFactory factory = new PercentileAggregatorFactory();
        factory.setPercentileIndex(property);
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName(seriesName);

        return createGroupInfo(factory, seriesSelector);
    }

    private GroupInfo createGroupInfo(AggregatorFactory aggregationFactory, StaticSeriesSelector seriesSelector) {
        return new GroupInfo(
                aggregationFactory,
                seriesSelector,
                new SuccessfulElapsedTimeValueSelector(),
                false,
                false);
    }
}
