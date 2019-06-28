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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class PercentileAggregator is used to get percentile from samples.
 *
 * @since 3.0
 */
public class PercentileAggregator implements Aggregator {
    private static final int SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault(
            ReportGeneratorConfiguration.REPORT_GENERATOR_KEY_PREFIX
                    + ReportGeneratorConfiguration.KEY_DELIMITER
                    + "statistic_window", 20000);

    private final DescriptiveStatistics statistics;
    private final double percentileIndex;

    /**
     * Instantiates a new percentile aggregator.
     *
     * @param index
     *            the index of the percentile
     */
    public PercentileAggregator(double index) {
        statistics = new DescriptiveStatistics(SLIDING_WINDOW_SIZE);
        percentileIndex = index;
    }

    /**
     * @param lastAggregator {@link PercentileAggregator}
     */
    public PercentileAggregator(PercentileAggregator lastAggregator) {
        statistics = new DescriptiveStatistics(SLIDING_WINDOW_SIZE);
        this.percentileIndex = lastAggregator.percentileIndex;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.core.GraphAggregator#getCount()
     */
    @Override
    public long getCount() {
        return statistics.getN();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.core.GraphAggregator#getResult()
     */
    @Override
    public double getResult() {
        return statistics.getPercentile(percentileIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.core.GraphAggregator#addValue(double)
     */
    @Override
    public void addValue(double value) {
        statistics.addValue(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.core.GraphAggregator#reset()
     */
    @Override
    public void reset() {
        statistics.clear();
    }

}
