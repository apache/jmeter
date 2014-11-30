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

package org.apache.jmeter.visualizers.backend;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Sampler metric
 * @since 2.13
 */
public class SamplerMetric {
    private static final int SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault("backend_metrics_window", 100); //$NON-NLS-1$
    
    // Limit to sliding window of SLIDING_WINDOW_SIZE values 
    private DescriptiveStatistics responsesStats = new DescriptiveStatistics(SLIDING_WINDOW_SIZE);
    private int successes;
    private int failures;
    /**
     * 
     */
    public SamplerMetric() {
    }

    /**
     * Add a {@link SampleResult} to be used in the statistics
     * @param result {@link SampleResult} to be used
     */
    public synchronized void add(SampleResult result) {
        if(result.isSuccessful()) {
            successes++;
        } else {
            failures++;
        }
        long time = result.getTime();
        if(result.isSuccessful()) {
            // Should we also compute KO , all response time ?
            // only take successful requests for time computing
            responsesStats.addValue(time);
        }
    }
    
    /**
     * Reset metric except for percentile related data
     */
    public synchronized void resetForTimeInterval() {
        // We don't clear responsesStats nor usersStats as it will slide as per my understanding of 
        // http://commons.apache.org/proper/commons-math/userguide/stat.html
        successes = 0;
        failures = 0;
    }

    /**
     * Get the arithmetic mean of the stored values
     * 
     * @return The arithmetic mean of the stored values
     */
    public double getMean() {
        return responsesStats.getMean();
    }
    
    /**
     * Returns an estimate for the requested percentile of the stored values.
     * 
     * @param percentile
     *            the requested percentile (scaled from 0 - 100)
     * @return Returns an estimate for the requested percentile of the stored
     *         values.
     */
    public double getPercentile(double percentile) {
        return responsesStats.getPercentile(percentile);
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
    public double getMaxTime() {
        return responsesStats.getMax();
    }

    /**
     * Get the minimal elapsed time for requests within sliding window
     * 
     * @return the minTime, or {@link Long#MAX_VALUE} if no requests have been
     *         added yet
     */
    public double getMinTime() {
        return responsesStats.getMin();
    }
}
