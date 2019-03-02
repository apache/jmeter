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

import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.math.StatCalculatorLong;

/**
 * Aggregate sample data container. Just instantiate a new instance of this
 * class, and then call {@link #addSample(SampleResult)} a few times, and pull
 * the stats out with whatever methods you prefer.
 *
 */
public class SamplingStatCalculator {
    private final StatCalculatorLong calculator = new StatCalculatorLong();

    private double maxThroughput;

    private long firstTime;

    private String label;

    private volatile Sample currentSample;

    public SamplingStatCalculator(){ // Only for use by test code
        this("");
    }

    public SamplingStatCalculator(String label) {
        this.label = label;
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        firstTime = Long.MAX_VALUE;
        calculator.clear();
        maxThroughput = Double.MIN_VALUE;
        currentSample = new Sample();
    }

    /**
     * Clear the counters (useful for differential stats)
     *
     */
    public synchronized void clear() {
        init();
    }

    public Sample getCurrentSample() {
        return currentSample;
    }

    /**
     * Get the elapsed time for the samples
     *
     * @return how long the samples took
     */
    public long getElapsed() {
        if (getCurrentSample().getEndTime() == 0) {
            return 0;// No samples collected ...
        }
        return getCurrentSample().getEndTime() - firstTime;
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
        if (calculator.getCount() == 0) {
            return 0.0; // Better behaviour when howLong=0 or lastTime=0
        }

        return getCurrentSample().getThroughput();
    }

    /**
     * Throughput in bytes / second
     *
     * @return throughput in bytes/second
     */
    public double getBytesPerSecond() {
        return getRatePerSecond(calculator.getTotalBytes());
    }

    /**
     * Throughput in kilobytes / second
     *
     * @return Throughput in kilobytes / second
     */
    public double getKBPerSecond() {
        return getBytesPerSecond() / 1024; // 1024=bytes per kb
    }

    /**
     * Sent Throughput in bytes / second
     *
     * @return sent throughput in bytes/second
     */
    public double getSentBytesPerSecond() {
        return getRatePerSecond(calculator.getTotalSentBytes());
    }

    /**
     * @param value long
     * @return rate per second
     */
    private double getRatePerSecond(long value) {
        double rate = 0;
        if (this.getElapsed() > 0 && value > 0) {
            rate = value / ((double) this.getElapsed() / 1000);
        }
        if (rate < 0) {
            rate = 0;
        }
        return rate;
    }

    /**
     * Sent Throughput in kilobytes / second
     *
     * @return Sent Throughput in kilobytes / second
     */
    public double getSentKBPerSecond() {
        return getSentBytesPerSecond() / 1024; // 1024=bytes per kb
    }

    /**
     * calculates the average page size, which means divide the bytes by number
     * of samples.
     *
     * @return average page size in bytes (0 if sample count is zero)
     */
    public double getAvgPageBytes() {
        long count = calculator.getCount();
        if (count == 0) {
            return 0;
        }
        return calculator.getTotalBytes() / (double) count;
    }

    /**
     * @return the label of this component
     */
    public String getLabel() {
        return label;
    }

    /**
     * Records a sample.
     *
     * @param res
     *            the sample to record
     * @return newly created sample with current statistics
     *
     */
    public Sample addSample(SampleResult res) {
        long rtime;
        long cmean;
        long cstdv;
        long cmedian;
        long cpercent;
        long eCount;
        long endTime;
        double throughput;
        boolean rbool;
        synchronized (calculator) {
            calculator.addValue(res.getTime(), res.getSampleCount());
            calculator.addBytes(res.getBytesAsLong());
            calculator.addSentBytes(res.getSentBytes());
            setStartTime(res);
            eCount = getCurrentSample().getErrorCount();
            eCount += res.getErrorCount();
            endTime = getEndTime(res);
            long howLongRunning = endTime - firstTime;
            throughput = ((double) calculator.getCount() / (double) howLongRunning) * 1000.0;
            if (throughput > maxThroughput) {
                maxThroughput = throughput;
            }

            rtime = res.getTime();
            cmean = (long)calculator.getMean();
            cstdv = (long)calculator.getStandardDeviation();
            cmedian = calculator.getMedian().longValue();
            cpercent = calculator.getPercentPoint( 0.500 ).longValue();
// TODO cpercent is the same as cmedian here - why? and why pass it to "distributionLine"?
            rbool = res.isSuccessful();
        }

        long count = calculator.getCount();
        Sample s =
            new Sample( null, rtime, cmean, cstdv, cmedian, cpercent, throughput, eCount, rbool, count, endTime );
        currentSample = s;
        return s;
    }

    private long getEndTime(SampleResult res) {
        long endTime = res.getEndTime();
        long lastTime = getCurrentSample().getEndTime();
        if (lastTime < endTime) {
            lastTime = endTime;
        }
        return lastTime;
    }

    /**
     * @param res
     */
    private void setStartTime(SampleResult res) {
        long startTime = res.getStartTime();
        if (firstTime > startTime) {
            // this is our first sample, set the start time to current timestamp
            firstTime = startTime;
        }
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0)
     *
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded.
     */
    public double getErrorPercentage() {
        double rval = 0.0;

        if (calculator.getCount() == 0) {
            return rval;
        }
        rval = (double) getCurrentSample().getErrorCount() / (double) calculator.getCount();
        return rval;
    }

    /**
     * For debugging purposes, only.
     */
    @Override
    public String toString() {
        StringBuilder mySB = new StringBuilder();

        mySB.append("Samples: " + this.getCount() + "  ");
        mySB.append("Avg: " + this.getMean() + "  ");
        mySB.append("Min: " + this.getMin() + "  ");
        mySB.append("Max: " + this.getMax() + "  ");
        mySB.append("Error Rate: " + this.getErrorPercentage() + "  ");
        mySB.append("Sample Rate: " + this.getRate());
        return mySB.toString();
    }

    /**
     * @return errorCount
     */
    public long getErrorCount() {
        return getCurrentSample().getErrorCount();
    }

    /**
     * @return Returns the maxThroughput.
     */
    public double getMaxThroughput() {
        return maxThroughput;
    }

    public Map<Number, Number[]> getDistribution() {
        return calculator.getDistribution();
    }

    public Number getPercentPoint(double percent) {
        return calculator.getPercentPoint(percent);
    }

    public long getCount() {
        return calculator.getCount();
    }

    public Number getMax() {
        return calculator.getMax();
    }

    public double getMean() {
        return calculator.getMean();
    }

    public Number getMeanAsNumber() {
        return Long.valueOf((long) calculator.getMean());
    }

    public Number getMedian() {
        return calculator.getMedian();
    }

    public Number getMin() {
        if (calculator.getMin().longValue() < 0) {
            return Long.valueOf(0);
        }
        return calculator.getMin();
    }

    public Number getPercentPoint(float percent) {
        return calculator.getPercentPoint(percent);
    }

    public double getStandardDeviation() {
        return calculator.getStandardDeviation();
    }
}
