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

import java.util.HashMap;

import org.apache.jmeter.report.processor.AggregatorFactory;

/**
 * The class GroupData helps to store group data for a graph.
 */
public class GroupData {
    private final HashMap<String, SeriesData> seriesData = new HashMap<>();
    private final SeriesData overallSeries;

    /**
     * Gets the series data map.
     *
     * @return the series data map
     */
    public final HashMap<String, SeriesData> getSeriesInfo() {
        return seriesData;
    }

    /**
     * Gets the overall series data
     *
     * @return the overall series data
     */
    public final SeriesData getOverallSeries() {
        return overallSeries;
    }

    /**
     * Instantiates a new group groupData.
     *
     * @param factory
     *            the factory
     * @param hasOverall
     *            the status defining if the group has an overall seriesData
     * @param hasAggregatedKey
     *            the status defining if the group aggregates keys
     */
    public GroupData(AggregatorFactory factory, boolean hasOverall,
            boolean hasAggregatedKey) {
        overallSeries = hasOverall ? new SeriesData(factory, hasAggregatedKey,
                false, true) : null;
    }

    public void clear() {
        seriesData.clear();
        if (overallSeries != null) {
            overallSeries.clear();
        }
    }
}
