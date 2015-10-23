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
package org.apache.jmeter.report.processor;

import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;

/**
 * The class PercentileAggregator is used to get percentile from samples.
 * 
 * @since 2.14
 */
public class PercentileAggregator implements Aggregator {

    private PSquarePercentile percentile;

    /**
     * Instantiates a new percentile aggregator.
     *
     * @param index
     *            the index of the percentile
     */
    public PercentileAggregator(double index) {
	percentile = new PSquarePercentile(index);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.core.GraphAggregator#getCount()
     */
    @Override
    public long getCount() {
	return percentile.getN();
    }
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.report.core.GraphAggregator#getResult()
     */
    @Override
    public double getResult() {
	return percentile.getResult();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.core.GraphAggregator#addValue(double)
     */
    @Override
    public void addValue(double value) {
	percentile.increment(value);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.core.GraphAggregator#reset()
     */
    @Override
    public void reset() {
	percentile.clear();
    }

}
