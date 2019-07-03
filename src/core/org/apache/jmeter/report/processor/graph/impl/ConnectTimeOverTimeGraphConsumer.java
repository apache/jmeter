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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.processor.PercentileAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.ConnectTimeValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.NameSeriesSelector;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;
import org.apache.jmeter.util.JMeterUtils;
/**
 * The class ConnectTimeOverTimeGraphConsumer provides a graph to visualize Connection time
 * per time period (defined by granularity)
 *
 * @since 3.1
 */
public class ConnectTimeOverTimeGraphConsumer extends AbstractOverTimeGraphConsumer {
    private static final boolean CONNECT_TIME_SAVED =
            JMeterUtils.getPropDefault("jmeter.save.saveservice.connect_time", true); //$NON-NLS-1$
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

     * @param seriesName Series name
     * @return the group info
     */
    private GroupInfo createPercentileGroupInfo(String propertyKey, int defaultValue, String seriesName) {
        int property = JMeterUtils.getPropDefault(propertyKey, defaultValue);
        PercentileAggregatorFactory factory = new PercentileAggregatorFactory();
        factory.setPercentileIndex(property);

        return new GroupInfo(factory, new NameSeriesSelector(),
                new ConnectTimeValueSelector(false), false, false);
    }

    /*
   * (non-Javadoc)
   *
   * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
   * createGroupInfos()
   */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        if(!CONNECT_TIME_SAVED) {
            return Collections.emptyMap();
        }

        HashMap<String, GroupInfo> groupInfos = new HashMap<>();
        groupInfos.put("aggregate_rpt_pct2", //$NON-NLS-1$
                createPercentileGroupInfo("aggregate_rpt_pct2", 95, //$NON-NLS-1$
                        String.format(
                                PERCENTILE_FORMAT, Integer.valueOf(95))));
        return groupInfos;
    }
}
