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
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.TimeRateAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.SeriesData;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class TotalTPSGraphConsumer provides a graph to visualize transactions
 * rate per second.
 *
 * @since 5.0
 */
public class TotalTPSGraphConsumer extends AbstractOverTimeGraphConsumer {

    private static final String STATUS_SERIES_FORMAT = "%s-%s";
    private static final String SUCCESS_SERIES_SUFFIX = "success";
    private static final String FAILURE_SERIES_SUFFIX = "failure";
    private static final String TRANSACTION_SUCCESS_LABEL = String.format(STATUS_SERIES_FORMAT, "Transaction", SUCCESS_SERIES_SUFFIX);
    private static final String TRANSACTION_FAILURE_LABEL = String.format(STATUS_SERIES_FORMAT, "Transaction", FAILURE_SERIES_SUFFIX);

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
        AbstractSeriesSelector seriesSelector = new AbstractSeriesSelector(true) {
            @Override
            public Iterable<String> select(Sample sample) {
                return Collections.singletonList(sample.getSuccess() ? TRANSACTION_SUCCESS_LABEL : TRANSACTION_FAILURE_LABEL);
            }
        };
        return Collections.singletonMap(
                AbstractGraphConsumer.DEFAULT_GROUP,
                new GroupInfo(
                        new TimeRateAggregatorFactory(), seriesSelector,
                        // We include Transaction Controller results
                        new CountValueSelector(false), false, false));
    }

    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        super.initializeExtraResults(parentResult);
        String[] seriesLabels = new String[]{
                TRANSACTION_SUCCESS_LABEL, TRANSACTION_FAILURE_LABEL
        };
        initializeSeries(parentResult, seriesLabels);
    }

    @Override
    public void initialize() {
        super.initialize();
        // Override the granularity of the aggregators factory
        ((TimeRateAggregatorFactory) getGroupInfos().get(AbstractGraphConsumer.DEFAULT_GROUP).getAggregatorFactory())
                .setGranularity(getGranularity());
    }


    private void initializeSeries(MapResultData parentResult, String[] series) {
        ListResultData listResultData = (ListResultData) parentResult.getResult("series");
        for (String s : series) {
            listResultData.addResult(create(s));
        }
    }

    private MapResultData create(String serie) {
        GroupInfo groupInfo = getGroupInfos().get(AbstractGraphConsumer.DEFAULT_GROUP);
        SeriesData seriesData = new SeriesData(groupInfo.getAggregatorFactory(),
                groupInfo.enablesAggregatedKeysSeries(), false,
                groupInfo.enablesOverallSeries());
        return createSerieResult(serie, seriesData);
    }
}
