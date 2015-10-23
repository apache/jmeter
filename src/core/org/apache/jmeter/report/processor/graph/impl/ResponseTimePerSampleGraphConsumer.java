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

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import org.apache.jmeter.report.config.GraphConfiguration;
import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.JsonUtil;
import org.apache.jmeter.report.processor.MeanAggregatorFactory;
import org.apache.jmeter.report.processor.MedianAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.ElapsedTimeValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.IndexedNameSelector;
import org.apache.jmeter.report.processor.graph.StaticSeriesSelector;

/**
 * The class ResponseTimePerSampleGraphConsumer provides a graph to visualize
 * ...
 *
 * @since 2.14
 */
public class ResponseTimePerSampleGraphConsumer extends AbstractGraphConsumer {

    private static final String RESPONSE_TIME_PER_SAMPLE_SERIES_AVG = "Average Time";
    private static final String RESPONSE_TIME_PER_SAMPLE_SERIES_MED = "Median Time";

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

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
	HashMap<String, GroupInfo> groupInfos = new HashMap<String, GroupInfo>(
	        2);

	StaticSeriesSelector averageSeriesSelector = new StaticSeriesSelector();
	averageSeriesSelector
	        .setSeriesName(RESPONSE_TIME_PER_SAMPLE_SERIES_AVG);
	groupInfos.put(RESPONSE_TIME_PER_SAMPLE_SERIES_AVG, new GroupInfo(
	        new MeanAggregatorFactory(), averageSeriesSelector,
	        new ElapsedTimeValueSelector(), false, false));

	StaticSeriesSelector medianSeriesSelector = new StaticSeriesSelector();
	medianSeriesSelector.setSeriesName(RESPONSE_TIME_PER_SAMPLE_SERIES_MED);
	groupInfos.put(RESPONSE_TIME_PER_SAMPLE_SERIES_MED, new GroupInfo(
	        new MedianAggregatorFactory(), medianSeriesSelector,
	        new ElapsedTimeValueSelector(), false, false));

	return groupInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#exportData(org.apache
     * .jmeter.report.config.GraphConfiguration)
     */
    @Override
    public DataContext exportData(GraphConfiguration configuration) {
	DataContext result = super.exportData(configuration);
	JsonArrayBuilder builder = Json.createArrayBuilder();
	IndexedNameSelector indexedNameSelector = (IndexedNameSelector) getKeysSelector();
	int size = indexedNameSelector.getNames().size();
	for (int i = 0; i < size; i++) {
	    builder.add(Json.createArrayBuilder().add(i)
		    .add(indexedNameSelector.getNames().get(i)));
	}
	result.put("sampleNames", JsonUtil.convertJsonToString(builder.build()));
	return result;
    }
}
