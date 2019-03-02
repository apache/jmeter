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

package org.apache.jmeter.visualizers;

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
 * Versions prior to 2.3.2 appeared to be thread-safe but weren't as label and index were not final.
 * Also the caller needs to synchronize access in order to ensure that variables are consistent.
 * </p>
 *
 */
public class RunningSample {

    private final DecimalFormat rateFormatter = new DecimalFormat("#.0"); // $NON-NLS-1$

    private final DecimalFormat errorFormatter = new DecimalFormat("#0.00%"); // $NON-NLS-1$

    private long counter;

    private long runningSum;

    private long max;

    private long min;

    private long errorCount;

    private long firstTime;

    private long lastTime;

    private final String label;

    private final int index;

    /**
     * Use this constructor to create the initial instance
     *
     * @param label the label for this component
     * @param index the index of this component
     */
    public RunningSample(String label, int index) {
        this.label = label;
        this.index = index;
        init();
    }

    /**
     * Copy constructor to create a duplicate of existing instance (without the
     * disadvantages of clone()
     *
     * @param src existing RunningSample to be copied
     */
    public RunningSample(RunningSample src) {
        this.counter = src.counter;
        this.errorCount = src.errorCount;
        this.firstTime = src.firstTime;
        this.index = src.index;
        this.label = src.label;
        this.lastTime = src.lastTime;
        this.max = src.max;
        this.min = src.min;
        this.runningSum = src.runningSum;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        counter = 0L;
        runningSum = 0L;
        max = Long.MIN_VALUE;
        min = Long.MAX_VALUE;
        errorCount = 0L;
        firstTime = Long.MAX_VALUE;
        lastTime = 0L;
    }

    /**
     * Clear the counters (useful for differential stats)
     *
     */
    public void clear() {
        init();
    }

    /**
     * Get the elapsed time for the samples
     *
     * @return how long the samples took
     */
    public long getElapsed() {
        if (lastTime == 0) {
            return 0;// No samples collected ...
        }
        return lastTime - firstTime;
    }

    /**
     * Returns the throughput associated to this sampler in requests per second.
     * May be slightly skewed because it takes the timestamps of the first and
     * last samples as the total time passed, and the test may actually have
     * started before that start time and ended after that end time.
     *
     * @return throughput associated with this sampler per second
     */
    public double getRate() {
        if (counter == 0) {
            return 0.0; // Better behaviour when howLong=0 or lastTime=0
        }

        long howLongRunning = lastTime - firstTime;

        if (howLongRunning == 0) {
            return Double.MAX_VALUE;
        }

        return (double) counter / howLongRunning * 1000.0;
    }

    /**
     * Returns the throughput associated to this sampler in requests per min.
     * May be slightly skewed because it takes the timestamps of the first and
     * last samples as the total time passed, and the test may actually have
     * started before that start time and ended after that end time.
     *
     * @return throughput associated with this sampler per minute
     */
    public double getRatePerMin() {
        if (counter == 0) {
            return 0.0; // Better behaviour when howLong=0 or lastTime=0
        }

        long howLongRunning = lastTime - firstTime;

        if (howLongRunning == 0) {
            return Double.MAX_VALUE;
        }
        return (double) counter / howLongRunning * 60000.0;
    }

    /**
     * Returns a String that represents the throughput associated for this
     * sampler, in units appropriate to its dimension:
     * <p>
     * The number is represented in requests/second or requests/minute or
     * requests/hour.
     * <p>
     * Examples: "34.2/sec" "0.1/sec" "43.0/hour" "15.9/min"
     *
     * @return a String representation of the rate the samples are being taken
     *         at.
     */
    public String getRateString() {
        double rate = getRate();

        if (Double.compare(rate, Double.MAX_VALUE) == 0) {
            return "N/A";
        }

        String unit = "sec";

        if (rate < 1.0) {
            rate *= 60.0;
            unit = "min";
        }
        if (rate < 1.0) {
            rate *= 60.0;
            unit = "hour";
        }

        return rateFormatter.format(rate) + "/" + unit;
    }

    /**
     * @return the label for this component
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the index of this component
     */
    public int getIndex() {
        return index;
    }

    /**
     * Records a sample.
     *
     * @param res sample to record
     */
    public void addSample(SampleResult res) {
        long aTimeInMillis = res.getTime();

        counter+=res.getSampleCount();
        errorCount += res.getErrorCount();

        long startTime = res.getStartTime();
        long endTime = res.getEndTime();

        if (firstTime > startTime) {
            // this is our first sample, set the start time to current timestamp
            firstTime = startTime;
        }

        // Always update the end time
        if (lastTime < endTime) {
            lastTime = endTime;
        }
        runningSum += aTimeInMillis;

        if (aTimeInMillis > max) {
            max = aTimeInMillis;
        }

        if (aTimeInMillis < min) {
            min = aTimeInMillis;
        }

    }

    /**
     * Adds another RunningSample to this one.
     * Does not check if it has the same label and index.
     *
     * @param rs sample to add
     */
    public void addSample(RunningSample rs) {
        this.counter += rs.counter;
        this.errorCount += rs.errorCount;
        this.runningSum += rs.runningSum;
        if (this.firstTime > rs.firstTime) {
            this.firstTime = rs.firstTime;
        }
        if (this.lastTime < rs.lastTime) {
            this.lastTime = rs.lastTime;
        }
        if (this.max < rs.max) {
            this.max = rs.max;
        }
        if (this.min > rs.min) {
            this.min = rs.min;
        }
    }

    /**
     * Returns the time in milliseconds of the quickest sample.
     *
     * @return the time in milliseconds of the quickest sample.
     */
    public long getMin() {
        long rval = 0;

        if (min != Long.MAX_VALUE) {
            rval = min;
        }
        return rval;
    }

    /**
     * Returns the time in milliseconds of the slowest sample.
     *
     * @return the time in milliseconds of the slowest sample.
     */
    public long getMax() {
        long rval = 0;

        if (max != Long.MIN_VALUE) {
            rval = max;
        }
        return rval;
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
     * Returns the number of samples that have been recorded by this instance of
     * the RunningSample class.
     *
     * @return the number of samples that have been recorded by this instance of
     *         the RunningSample class.
     */
    public long getNumSamples() {
        return counter;
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0) If you want a nicer return
     * format, see {@link #getErrorPercentageString()}.
     *
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded.
     */
    public double getErrorPercentage() {
        double rval = 0.0;

        if (counter == 0) {
            return rval;
        }
        rval = (double) errorCount / (double) counter;
        return rval;
    }

    /**
     * Returns a String which represents the percentage of sample errors that
     * have occurred. ("0.00%" through "100.00%")
     *
     * @return a String which represents the percentage of sample errors that
     *         have occurred.
     */
    public String getErrorPercentageString() {
        double myErrorPercentage = this.getErrorPercentage();

        return errorFormatter.format(myErrorPercentage);
    }

    /**
     * For debugging purposes, mainly.
     */
    @Override
    public String toString() {
        StringBuilder mySB = new StringBuilder();

        mySB.append("Samples: " + this.getNumSamples() + "  ");
        mySB.append("Avg: " + this.getAverage() + "  ");
        mySB.append("Min: " + this.getMin() + "  ");
        mySB.append("Max: " + this.getMax() + "  ");
        mySB.append("Error Rate: " + this.getErrorPercentageString() + "  ");
        mySB.append("Sample Rate: " + this.getRateString());
        return mySB.toString();
    }

    /**
     * @return errorCount
     */
    public long getErrorCount() {
        return errorCount;
    }

}
