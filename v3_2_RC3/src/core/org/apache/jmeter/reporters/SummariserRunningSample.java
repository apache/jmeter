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

package org.apache.jmeter.reporters;

import java.text.DecimalFormat;

import org.apache.jmeter.samplers.SampleResult;

/**
 * <p>
 * Running sample data container. Just instantiate a new instance of this
 * class, and then call {@link #addSample(SampleResult)} a few times, and pull
 * the stats out with whatever methods you prefer.
 * </p>
 * <p>
 * Please note that this class is not thread-safe.
 * The calling class is responsible for ensuring thread safety if required.
 * The caller needs to synchronize access in order to ensure that variables are consistent.
 * </p>
 * @since 2.13
 */
class SummariserRunningSample {

    private final DecimalFormat errorFormatter = new DecimalFormat("#0.00%"); // $NON-NLS-1$

    private long counter;

    private long runningSum;

    private long max;

    private long min;

    private long errorCount;

    private long startTime;

    private long endTime;

    private final String label;

    /**
     * @param label the label of this component
     */
    public SummariserRunningSample(String label) {
        this.label = label;
        init();
    }

    /**
     * Copy constructor
     * @param src the instance to copy
     */
    public SummariserRunningSample(SummariserRunningSample src) {
        label = src.label;
        counter = src.counter;
        errorCount = src.errorCount;
        startTime = src.startTime;
        endTime = src.endTime;
        max = src.max;
        min = src.min;
        runningSum = src.runningSum;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        counter = 0L;
        runningSum = 0L;
        max = Long.MIN_VALUE;
        min = Long.MAX_VALUE;
        errorCount = 0L;
        startTime = System.currentTimeMillis();
        endTime = startTime;
    }

    /**
     * Clear at every interval
     */
    public void clear() {
        init();
    }

    /**
     * Used for delta
     * @param rs {@link SummariserRunningSample}
     */
    public void addSample(SummariserRunningSample rs) {
        counter += rs.counter;
        errorCount += rs.errorCount;
        runningSum += rs.runningSum;
        if (max < rs.max) {
            max = rs.max;
        }
        if (min > rs.min) {
            min = rs.min;
        }
        // We want end time to be current time so sample rates reflect real time
        endTime = System.currentTimeMillis();
    }

    /**
     * Used for each SampleResult
     * @param res {@link SampleResult}
     */
    public void addSample(SampleResult res) {
        counter += res.getSampleCount();
        errorCount += res.getErrorCount();
        long aTimeInMillis = res.getTime();
        runningSum += aTimeInMillis;
        if (aTimeInMillis > max) {
            max = aTimeInMillis;
        }
        if (aTimeInMillis < min) {
            min = aTimeInMillis;
        }
        // We want end time to be current time so sample rates reflect real time
        endTime = System.currentTimeMillis();
    }

    /**
     * Returns the number of samples that have been recorded by this instance of
     * the {@link SummariserRunningSample} class.
     *
     * @return the number of samples that have been recorded by this instance of
     *         the {@link SummariserRunningSample} class.
     */
    public long getNumSamples() {
        return counter;
    }

    /**
     * Get the elapsed time for the samples
     *
     * @return how long the samples took
     */
    public long getElapsed() {
        if (counter == 0) {
            return 0;// No samples collected ...
        }
        return endTime - startTime;
    }

    /**
     * Returns the throughput associated to this sampler in requests per second.
     *
     * @return throughput associated to this sampler
     */
    public double getRate() {
        if (counter == 0) {
            return 0.0;// No samples collected ...
        }

        long howLongRunning = endTime - startTime;

        if (howLongRunning == 0) {
            return Double.MAX_VALUE;
        }

        return (double) counter / howLongRunning * 1000.0;
    }

    /**
     * Returns the average time in milliseconds that samples ran in.
     *
     * @return the average time in milliseconds that samples ran in.
     */
    public long getAverage() {
        if (counter == 0) {
            return 0;
        }
        return runningSum / counter;
    }

    /**
     * @return errorCount
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * Returns a String which represents the percentage of sample errors that
     * have occurred. ("0.00%" through "100.00%")
     *
     * @return a String which represents the percentage of sample errors that
     *         have occurred.
     */
    public String getErrorPercentageString() {
        return errorFormatter.format(getErrorPercentage());
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0) If you want a nicer return
     * format, see {@link #getErrorPercentageString()}.
     *
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded. Returns 0.0 if there are no samples
     */
    public double getErrorPercentage() {
        if (counter == 0) {
            return 0.0;
        }
        double rval = (double) errorCount / (double) counter;
        return rval;
    }

    /**
     * Returns the time in milliseconds of the slowest sample.
     *
     * @return the time in milliseconds of the slowest sample.
     */
    public long getMax() {
        return max;
    }

    /**
     * Returns the time in milliseconds of the quickest sample.
     *
     * @return the time in milliseconds of the quickest sample.
     */
    public long getMin() {
        return min;
    }

    /**
     * Set end time
     */
    public void setEndTime() {
        endTime = System.currentTimeMillis();
    }

}
