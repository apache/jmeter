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
import java.util.Map;

import org.apache.jmeter.report.processor.Aggregator;
import org.apache.jmeter.report.processor.AggregatorFactory;

/**
 * The class SeriesData helps to store series data in a graph.
 * 
 * @since 2.14
 */
public class SeriesData {

    /** The regular groupData. */
    private final HashMap<Double, Aggregator> aggregators = new HashMap<Double, Aggregator>();

    /** The keys aggregator for aggregated keys seriesData. */
    private final Aggregator keysAggregator;

    /** The values aggregator for aggregated keys seriesData. */
    private final Aggregator valuesAggregator;

    /** The count of samples of this series. */
    private long count = 0L;

    /**
     * Gets the groupData.
     *
     * @return the groupData
     */
    public Map<Double, Aggregator> getAggregatorInfo() {
	return aggregators;
    }

    /**
     * Gets the keys aggregator of aggregated keys seriesData.
     *
     * @return the keys aggregator
     */
    public Aggregator getKeysAggregator() {
	return keysAggregator;
    }

    /**
     * Gets the values aggregator of aggregated keys seriesData.
     *
     * @return the values aggregator
     */
    public Aggregator getValuesAggregator() {
	return valuesAggregator;
    }

    /**
     * Gets the count of samples.
     *
     * @return the count of samples
     */
    public long getCount() {
	return count;
    }

    /**
     * Instantiates a new data seriesData.
     *
     * @param factory
     *            the factory
     * @param hasAggregatedKey
     *            the has aggregated key
     */
    public SeriesData(AggregatorFactory factory, boolean hasAggregatedKey) {
	if (hasAggregatedKey) {
	    keysAggregator = factory.createKeyAggregator();
	    valuesAggregator = factory.createAggregatedKeyValueAggregator();
	} else {
	    keysAggregator = null;
	    valuesAggregator = null;
	}
    }

    /**
     * Increment the count of samples.
     */
    public void incrementCount() {
	count++;
    }

    public void clear() {
	aggregators.clear();
	count = 0L;
	if (keysAggregator != null)
	    keysAggregator.reset();
	if (valuesAggregator != null)
	    valuesAggregator.reset();
    }
}