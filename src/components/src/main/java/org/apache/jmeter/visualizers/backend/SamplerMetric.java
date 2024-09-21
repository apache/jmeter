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

package org.apache.jmeter.visualizers.backend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.report.processor.DescriptiveStatisticsFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.documentation.VisibleForTesting;

/**
 * Sampler metric
 * @since 2.13
 */
public class SamplerMetric {
    private static final int SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault("backend_metrics_window", 100);
    private static final int LARGE_SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault("backend_metrics_large_window", 5000);

    private static volatile WindowMode globalWindowMode = WindowMode.get();

    /**
     * Response times for OK samples
     */
    private final DescriptiveStatistics okResponsesStats = DescriptiveStatisticsFactory.createDescriptiveStatistics(LARGE_SLIDING_WINDOW_SIZE);
    /**
     * Response times for KO samples
     */
    private final DescriptiveStatistics koResponsesStats = DescriptiveStatisticsFactory.createDescriptiveStatistics(LARGE_SLIDING_WINDOW_SIZE);
    /**
     * Response times for All samples
     */
    private final DescriptiveStatistics allResponsesStats = DescriptiveStatisticsFactory.createDescriptiveStatistics(LARGE_SLIDING_WINDOW_SIZE);
    /**
     *  OK, KO, ALL stats
     */
    private final List<DescriptiveStatistics> windowedStats = initWindowedStats();
    /**
     * Timeboxed percentiles don't makes sense
     */
    private final DescriptiveStatistics pctResponseStats = DescriptiveStatisticsFactory.createDescriptiveStatistics(SLIDING_WINDOW_SIZE);
    private int successes;
    private int failures;
    private int hits;
    private final Map<ErrorMetric, Integer> errors = new HashMap<>();
    private long sentBytes;
    private long receivedBytes;


    /**
     *
     */
    public SamplerMetric() {
        // Limit to sliding window of SLIDING_WINDOW_SIZE values for FIXED mode
        if (globalWindowMode == WindowMode.FIXED) {
            for (DescriptiveStatistics stat : windowedStats) {
                stat.setWindowSize(SLIDING_WINDOW_SIZE);
            }
        }
    }

    /**
     * Set {@link WindowMode} to use for newly created metrics.
     * @param windowMode new visibility mode
     * @deprecated only used for internal testing
     */
    @Deprecated
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public static void setDefaultWindowMode(WindowMode windowMode) {
        globalWindowMode = windowMode;
    }

    /**
     * @return List of {@link DescriptiveStatistics}
     */
    private List<DescriptiveStatistics> initWindowedStats() {
        return Arrays.asList(okResponsesStats, koResponsesStats, allResponsesStats);
    }

    /**
     * Add a {@link SampleResult} to be used in the statistics
     * @param result {@link SampleResult} to be used
     */
    public synchronized void add(SampleResult result) {
        add(result, false);
    }

    /**
     * Add a {@link SampleResult} and its sub-results to be used in the statistics
     * @param result {@link SampleResult} to be used
     */
    public synchronized void addCumulated(SampleResult result) {
        add(result, true);
    }

    /**
     * Add a {@link SampleResult} to be used in the statistics
     * @param result {@link SampleResult} to be used
     * @param isCumulated is the overall Sampler Metric
     */
    private synchronized void add(SampleResult result, boolean isCumulated) {
        if(result.isSuccessful()) {
            successes+=result.getSampleCount()-result.getErrorCount();
        } else {
            failures+=result.getErrorCount();
            ErrorMetric error = new ErrorMetric(result);
            errors.put(error, errors.getOrDefault(error, 0) + result.getErrorCount() );
        }
        long time = result.getTime();
        allResponsesStats.addValue((double) time);
        pctResponseStats.addValue((double) time);
        if(result.isSuccessful()) {
            // Should we also compute KO , all response time ?
            // only take successful requests for time computing
            okResponsesStats.addValue((double) time);
        }else {
            koResponsesStats.addValue((double) time);
        }
        addHits(result, isCumulated);
        addNetworkData(result, isCumulated);
    }

    /**
     * Increment traffic metrics. A Parent sampler cumulates its children metrics.
     * @param result SampleResult
     * @param isCumulated related to the overall sampler metric
     */
    private void addNetworkData(SampleResult result, boolean isCumulated) {
        if (isCumulated && TransactionController.isFromTransactionController(result)
                && result.getSubResults().length == 0) { // Transaction controller without generate parent sampler
            return;
        }
        sentBytes += result.getSentBytes();
        receivedBytes += result.getBytesAsLong();
    }

    /**
     * Compute hits from result
     * @param result {@link SampleResult}
     * @param isCumulated related to the overall sampler metric
     */
    private void addHits(SampleResult result, boolean isCumulated) {
        SampleResult[] subResults = result.getSubResults();
        if (isCumulated && TransactionController.isFromTransactionController(result)
                && subResults.length == 0) { // Transaction controller without generate parent sampler
            return;
        }
        if (!(TransactionController.isFromTransactionController(result) && subResults.length > 0)) {
            hits += result.getSampleCount();
        }
        for (SampleResult subResult : subResults) {
            addHits(subResult, isCumulated);
        }
    }

    /**
     * Reset metric except for percentile related data
     */
    public synchronized void resetForTimeInterval() {
        switch (globalWindowMode) {
        case FIXED:
            // We don't clear responsesStats nor usersStats as it will slide as per my understanding of
            // http://commons.apache.org/proper/commons-math/userguide/stat.html
            break;
        case TIMED:
            for (DescriptiveStatistics stat : windowedStats) {
                stat.clear();
            }
            break;
        }
        errors.clear();
        successes = 0;
        failures = 0;
        hits = 0;
        sentBytes = 0;
        receivedBytes = 0;
    }

    /**
     * Get the number of total requests for the current time slot
     *
     * @return number of total requests
     */
    public int getTotal() {
        return successes+failures;
    }

    /**
     * Get the number of successful requests for the current time slot
     *
     * @return number of successful requests
     */
    public int getSuccesses() {
        return successes;
    }

    /**
     * Get the number of failed requests for the current time slot
     *
     * @return number of failed requests
     */
    public int getFailures() {
        return failures;
    }

    /**
     * Get the maximal elapsed time for requests within sliding window
     *
     * @return the maximal elapsed time, or <code>0</code> if no requests have
     *         been added yet
     */
    public double getOkMaxTime() {
        return okResponsesStats.getMax();
    }

    /**
     * Get the minimal elapsed time for requests within sliding window
     *
     * @return the minTime, or {@link Long#MAX_VALUE} if no requests have been
     *         added yet
     */
    public double getOkMinTime() {
        return okResponsesStats.getMin();
    }

    /**
     * Get the arithmetic mean of the stored values
     *
     * @return The arithmetic mean of the stored values
     */
    public double getOkMean() {
        return okResponsesStats.getMean();
    }

    /**
     * Returns an estimate for the requested percentile of the stored values.
     *
     * @param percentile
     *            the requested percentile (scaled from 0 - 100)
     * @return Returns an estimate for the requested percentile of the stored
     *         values.
     */
    public double getOkPercentile(double percentile) {
        return okResponsesStats.getPercentile(percentile);
    }

    /**
     * Get the maximal elapsed time for requests within sliding window
     *
     * @return the maximal elapsed time, or <code>0</code> if no requests have
     *         been added yet
     */
    public double getKoMaxTime() {
        return koResponsesStats.getMax();
    }

    /**
     * Get the minimal elapsed time for requests within sliding window
     *
     * @return the minTime, or {@link Long#MAX_VALUE} if no requests have been
     *         added yet
     */
    public double getKoMinTime() {
        return koResponsesStats.getMin();
    }

    /**
     * Get the arithmetic mean of the stored values
     *
     * @return The arithmetic mean of the stored values
     */
    public double getKoMean() {
        return koResponsesStats.getMean();
    }

    /**
     * Returns an estimate for the requested percentile of the stored values.
     *
     * @param percentile
     *            the requested percentile (scaled from 0 - 100)
     * @return Returns an estimate for the requested percentile of the stored
     *         values.
     */
    public double getKoPercentile(double percentile) {
        return koResponsesStats.getPercentile(percentile);
    }

    /**
     * Get the maximal elapsed time for requests within sliding window
     *
     * @return the maximal elapsed time, or <code>0</code> if no requests have
     *         been added yet
     */
    public double getAllMaxTime() {
        return allResponsesStats.getMax();
    }

    /**
     * Get the minimal elapsed time for requests within sliding window
     *
     * @return the minTime, or {@link Long#MAX_VALUE} if no requests have been
     *         added yet
     */
    public double getAllMinTime() {
        return allResponsesStats.getMin();
    }

    /**
     * Get the arithmetic mean of the stored values
     *
     * @return The arithmetic mean of the stored values
     */
    public double getAllMean() {
        return allResponsesStats.getMean();
    }

    /**
     * Returns an estimate for the requested percentile of the stored values.
     *
     * @param percentile
     *            the requested percentile (scaled from 0 - 100)
     * @return Returns an estimate for the requested percentile of the stored
     *         values.
     */
    public double getAllPercentile(double percentile) {
        return pctResponseStats.getPercentile(percentile);
    }

    /**
     * Returns hits to server
     * @return the hits
     */
    public int getHits() {
        return hits;
    }

    /**
     * Returns by type ( response code and message ) the count of errors occurs
     * @return errors
     */
    public Map<ErrorMetric, Integer> getErrors() {
        return errors;
    }

    /**
     * @return the sentBytes
     */
    public long getSentBytes() {
        return sentBytes;
    }

    /**
     * @return the receivedBytes
     */
    public long getReceivedBytes() {
        return receivedBytes;
    }
}
