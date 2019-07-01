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
package org.apache.jmeter.report.processor.graph;

import org.apache.jmeter.report.processor.AggregatorFactory;

/**
 * The class GroupInfo stores information about the series group of a graph.
 *
 * @since 3.0
 */
public class GroupInfo {
    private final boolean enableAggregatedKeysSeries;
    private final boolean enableOverallSeries;
    private final GraphSeriesSelector seriesSelector;
    private final GraphValueSelector valueSelector;
    private final AggregatorFactory aggregatorFactory;
    private final GroupData groupData;

    /**
     * Enables aggregated keys seriesData.
     *
     * @return the enableAggregatedKeysSeries
     */
    public final boolean enablesAggregatedKeysSeries() {
        return enableAggregatedKeysSeries;
    }

    /**
     * Enables overall seriesData.
     *
     * @return the enableOverallSeries
     */
    public final boolean enablesOverallSeries() {
        return enableOverallSeries;
    }

    /**
     * Gets the seriesData selector.
     *
     * @return the seriesData selector
     */
    public final GraphSeriesSelector getSeriesSelector() {
        return seriesSelector;
    }

    /**
     * Gets the value selector.
     *
     * @return the value selector
     */
    public final GraphValueSelector getValueSelector() {
        return valueSelector;
    }

    /**
     * Gets the aggregator factory.
     *
     * @return the aggregatorFactory
     */
    public final AggregatorFactory getAggregatorFactory() {
        return aggregatorFactory;
    }

    /**
     * Gets the group data.
     *
     * @return the group data
     */
    public final GroupData getGroupData() {
        return groupData;
    }

    /**
     * Instantiates a new group info.
     *
     * @param aggregatorFactory
     *            the aggregator factory
     * @param seriesSelector
     *            the series selector
     * @param valueSelector
     *            the value selector
     * @param enableOverallSeries
     *            the enable overall series
     * @param enableAggregatedKeysSeries
     *            the enable aggregated keys series
     */
    public GroupInfo(AggregatorFactory aggregatorFactory,
            GraphSeriesSelector seriesSelector,
            GraphValueSelector valueSelector, boolean enableOverallSeries,
            boolean enableAggregatedKeysSeries) {
        this.enableOverallSeries = enableOverallSeries;
        this.seriesSelector = seriesSelector;
        this.valueSelector = valueSelector;
        this.aggregatorFactory = aggregatorFactory;
        this.enableAggregatedKeysSeries = enableAggregatedKeysSeries;
        this.groupData = new GroupData(aggregatorFactory, enableOverallSeries,
                enableAggregatedKeysSeries);
    }
}
