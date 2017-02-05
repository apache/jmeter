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

import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.PercentileAggregatorFactory;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.ElapsedTimeValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.IndexedNameSelector;
import org.apache.jmeter.report.processor.graph.StaticSeriesSelector;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class ResponseTimePerSampleGraphConsumer provides a graph to visualize
 * percentiles of response time for each sample name.
 * NOT USED FOR NOW as of 3.0
 * @since 3.0
 * 
 */
public class ResponseTimePerSampleGraphConsumer extends AbstractGraphConsumer {

    private static final String RESPONSE_TIME_PER_SAMPLE_SERIES_FORMAT = "%dth percentile";

    /**
     * Instantiates a new response time per sample graph consumer.
     */
    public ResponseTimePerSampleGraphConsumer() {
        setRevertKeysAndValues(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createKeysSelector()
     */
    @Override
    protected final GraphKeysSelector createKeysSelector() {
        return new IndexedNameSelector();
    }

    /**
     * Creates the group info for elapsed time percentile depending on jmeter
     * properties.
     *
     * @param propertyKey
     *            the property key
     * @param defaultValue
     *            the default value
     * @return the group info
     */
    private GroupInfo createGroupInfo(String propertyKey, int defaultValue) {
        int property = JMeterUtils.getPropDefault(propertyKey, defaultValue);
        PercentileAggregatorFactory factory = new PercentileAggregatorFactory();
        factory.setPercentileIndex(property);
        StaticSeriesSelector seriesSelector = new StaticSeriesSelector();
        seriesSelector.setSeriesName(String.format(
                RESPONSE_TIME_PER_SAMPLE_SERIES_FORMAT, Integer.valueOf(property)));

        return new GroupInfo(factory, seriesSelector,
                // We include Transaction Controller results
                new ElapsedTimeValueSelector(false), false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        HashMap<String, GroupInfo> groupInfos = new HashMap<>(2);

        groupInfos.put("aggregate_rpt_pct1",
                createGroupInfo("aggregate_rpt_pct1", 90));

        groupInfos.put("aggregate_rpt_pct2",
                createGroupInfo("aggregate_rpt_pct2", 95));

        groupInfos.put("aggregate_rpt_pct3",
                createGroupInfo("aggregate_rpt_pct3", 99));

        return groupInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * initializeExtraResults(org.apache.jmeter.report.processor.MapResultData)
     */
    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        ListResultData samples = new ListResultData();
        IndexedNameSelector indexedNameSelector = (IndexedNameSelector) getKeysSelector();
        int size = indexedNameSelector.getNames().size();
        for (int i = 0; i < size; i++) {
            samples.addResult(new ValueResultData(indexedNameSelector
                    .getNames().get(i)));
        }
        parentResult.setResult("sampleNames", samples);
    }
}
