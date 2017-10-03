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
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

/**
 * User metric
 * @since 2.13
 */
public class UserMetric {
    private static final int SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault("backend_metrics_window", 100); //$NON-NLS-1$
    
    // Limit to sliding window of SLIDING_WINDOW_SIZE values 
    private DescriptiveStatistics usersStats = new DescriptiveStatistics(SLIDING_WINDOW_SIZE);
    /**
     * 
     */
    public UserMetric() {
    }

    /**
     * Add a {@link SampleResult} to be used in the statistics
     * @param result {@link SampleResult} to be used
     */
    public synchronized void add(SampleResult result) {
        usersStats.addValue(JMeterContextService.getThreadCounts().activeThreads);
    }
    
    /**
     * Reset metric except for percentile related data
     */
    public synchronized void resetForTimeInterval() {
        // NOOP
    }

    /**
     * @return the max number of active threads for this test run 
     *          using a sliding window of SLIDING_WINDOW_SIZE
     */
    public int getMaxActiveThreads() {
        return (int) usersStats.getMax();
    }

    /**
     * @return the mean number of active threads for this test run
     *          using a sliding window of SLIDING_WINDOW_SIZE
     */
    public int getMeanActiveThreads() {
        return (int) usersStats.getMean();
    }
    
    /**
     * @return the min number of active threads for this test run
     *          using a sliding window of SLIDING_WINDOW_SIZE
     */
    public int getMinActiveThreads() {
        return (int) usersStats.getMin();
    }

    /**
     * @return finished threads
     */
    public int getFinishedThreads() {
        return JMeterContextService.getThreadCounts().finishedThreads;
    }

    /**
     * @return started threads
     */
    public int getStartedThreads() {
        return JMeterContextService.getThreadCounts().startedThreads;
    }
    
    /**
     * Clear stats
     */
    public void clear() {
        this.usersStats.clear();
    }
}
