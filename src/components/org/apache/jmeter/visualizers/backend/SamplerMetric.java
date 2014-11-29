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

/**
 * Sampler metric
 * @since 2.13
 */
public class SamplerMetric {
    // Limit to sliding window of 100 values 
    private DescriptiveStatistics stats = new DescriptiveStatistics(100);
    private int success;
    private int failure;
    private long maxTime=0L;
    private long minTime=Long.MAX_VALUE;
    private int maxActiveThreads = 0;
    private int minActiveThreads = Integer.MAX_VALUE;
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
            success++;
        } else {
            failure++;
        }
        long time = result.getTime();
        int activeThreads = result.getAllThreads();
        maxTime = Math.max(time, maxTime);
        minTime = Math.min(time, minTime); 
        maxActiveThreads = Math.max(maxActiveThreads, activeThreads);
        minActiveThreads = Math.min(minActiveThreads, activeThreads);
        if(result.isSuccessful()) {
            // Should we also compute KO , all response time ?
            // only take successful requests for time computing
            stats.addValue(time);
        }
    }
    
    /**
     * Reset metric except for percentile related data
     */
    public synchronized void resetForTimeInterval() {
        // We don't clear stats as it will slide as per my understanding of 
        // http://commons.apache.org/proper/commons-math/userguide/stat.html
        success = 0;
        failure = 0;
        maxTime=0L;
        minTime=Long.MAX_VALUE;
        maxActiveThreads = 0;
        minActiveThreads = Integer.MAX_VALUE;
    }

    /**
     * Get the arithmetic mean of the stored values
     * 
     * @return The arithmetic mean of the stored values
     */
    public double getMean() {
        return stats.getMean();
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
        return stats.getPercentile(percentile);
    }

    /**
     * Get the number of total requests for the current time slot
     * 
     * @return number of total requests
     */
    public int getTotal() {
        return success+failure;
    }
    
    /**
     * Get the number of successful requests for the current time slot
     * 
     * @return number of successful requests
     */
    public int getSuccess() {
        return success;
    }

    /**
     * Get the number of failed requests for the current time slot
     * 
     * @return number of failed requests
     */
    public int getFailure() {
        return failure;
    }

    /**
     * Get the maximal elapsed time for all requests
     * 
     * @return the maximal elapsed time, or <code>0</code> if no requests have
     *         been added yet
     */
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * Get the minimal elapsed time for all requests
     * 
     * @return the minTime, or {@link Long#MAX_VALUE} if no requests have been
     *         added yet
     */
    public long getMinTime() {
        return minTime;
    }

    /**
     * @return the max number of active threads for this test run, or
     *         <code>0</code> if no samples have been added yet
     */
    public int getMaxActiveThreads() {
        return maxActiveThreads;
    }

    /**
     * @return the min number of active threads for this test run, or
     *         {@link Integer#MAX_VALUE} if no samples have been added yet
     */
    public int getMinActiveThreads() {
        return minActiveThreads;
    }
}
