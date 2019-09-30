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
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.LatencyValueSelector;
import org.apache.jmeter.report.processor.graph.StaticSeriesSelector;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class LatencyOverTimeGraphConsumer provides a graph to visualize latency
 * per time period (defined by granularity)
 *
 * @since 3.0
 */
public class LatencyOverTimeGraphConsumer extends AbstractOverTimeGraphConsumer {
    private static final String PERCENTILE_FORMAT = "%dth percentile";
    private static final String PERCENTILE_PROPERTY = "aggregate_rpt_pct2";
    /*
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
     * Creates the group info for max elapsed time
     * @return the group info
     */
    private GroupInfo createMaxGroupInfo() {
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName("Max");
        return new GroupInfo(new MaxAggregatorFactory(), seriesSelector,
                new LatencyValueSelector(false), false, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        HashMap<String, GroupInfo> groupInfos = new HashMap<>();
        groupInfos.put("aggregate_report_max", //$NON-NLS-1$
            createMaxGroupInfo());

        LatencyValueSelector valueSelector = new LatencyValueSelector(false);
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        String seriesName = String.format(PERCENTILE_FORMAT, Integer.valueOf(95));
        seriesSelector.setSeriesName(seriesName);

        groupInfos.put(PERCENTILE_PROPERTY,
                createPercentileGroupInfo(PERCENTILE_PROPERTY, 95,
                        seriesName,valueSelector,seriesSelector));
        return groupInfos;
    }
}
