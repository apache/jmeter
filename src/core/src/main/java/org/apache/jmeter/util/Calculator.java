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

package org.apache.jmeter.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Class to calculate various items that don't require all previous results to be saved:
 * <ul>
 *   <li>mean = average</li>
 *   <li>standard deviation</li>
 *   <li>minimum</li>
 *   <li>maximum</li>
 * </ul>
 */
public class Calculator {

    private final DoubleAdder sum = new DoubleAdder();

    private final DoubleAdder sumOfSquares = new DoubleAdder();

    private final LongAdder count = new LongAdder();

    private final LongAdder bytes = new LongAdder();

    private final LongAdder sentBytes = new LongAdder();

    private final AtomicLong maximum = new AtomicLong();

    private final AtomicLong minimum = new AtomicLong();

    private final LongAdder errors = new LongAdder();

    private final String label;

    private final AtomicLong startTime = new AtomicLong(Long.MAX_VALUE);

    private final LongAccumulator elapsedTime = new LongAccumulator(Math::max, Long.MIN_VALUE);

    public Calculator() {
        this("");
    }

    public Calculator(String label) {
        this.label = label;
    }

    public void clear() {
        maximum.set(Long.MIN_VALUE);
        minimum.set(Long.MAX_VALUE);
        sum.reset();
        sumOfSquares.reset();
        count.reset();
        bytes.reset();
        sentBytes.reset();
        errors.reset();
        startTime.set(Long.MAX_VALUE);
        elapsedTime.reset();
    }

    /**
     * Add the value for (possibly multiple) samples.
     * Updates the count, sum, min, max, sumOfSquares, mean and deviation.
     *
     * @param newValue the total value for all the samples.
     * @param sampleCount number of samples included in the value
     */
    private void addValue(long newValue, int sampleCount) {
        count.add(sampleCount);
        sum.add((double) newValue);
        long value;
        double extraSumOfSquares;
        if (sampleCount > 1) {
            value = newValue / sampleCount;
            // For n values in an aggregate sample the average value = (val/n)
            // So need to add n * (val/n) * (val/n) = val * val / n
            extraSumOfSquares = ((double) newValue * (double) newValue) / sampleCount;
        } else { // no point dividing by 1
            value = newValue;
            extraSumOfSquares = (double) newValue * (double) newValue;
        }
        sumOfSquares.add(extraSumOfSquares);

        long currentMinimum = minimum.get();
        if (currentMinimum > value) {
            // We don't expect minimum to update often
            minimum.accumulateAndGet(value, Math::min);
        }
        long currentMaximum = maximum.get();
        if (currentMaximum < value) {
            // We don't expect the maximum to update often
            maximum.accumulateAndGet(value, Math::max);
        }
    }

    /**
     * Add details for a sample result, which may consist of multiple samples.
     * Updates the number of bytes read and sent, error count, startTime and elapsedTime
     * @param res the sample result; might represent multiple values
     */
    public void addSample(SampleResult res) {
        addBytes(res.getBytesAsLong());
        addSentBytes(res.getSentBytes());
        addValue(res.getTime(),res.getSampleCount());
        errors.add(res.getErrorCount()); // account for multiple samples
        long testStarted = startTime.get();
        // accumulateAndGet always performs the mutation, however, the "test start" timestamp is stable,
        // so we expect only a few operations to go through the slow path of accumulateAndGet
        if (res.getStartTime() < testStarted) {
            testStarted = startTime.accumulateAndGet(res.getStartTime(), Math::min);
        }
        elapsedTime.accumulate(res.getEndTime() - testStarted);
    }

    /**
     * add received bytes
     * @param newValue received bytes
     */
    public void addBytes(long newValue) {
        bytes.add(newValue);
    }

    /**
     * add Sent bytes
     * @param value sent bytes
     */
    private void addSentBytes(long value) {
        sentBytes.add(value);
    }

    public long getTotalBytes() {
        return bytes.sum();
    }


    public double getMean() {
        double sum = this.sum.sum();
        double count = this.count.sum();
        if (count == 0) {
            return 0.0;
        }
        return sum / count;
    }

    public Number getMeanAsNumber() {
        // It was long in previous releases, so let's keep it that way for now
        return (long) getMean();
    }

    public double getStandardDeviation() {
        double sum = this.sum.sum();
        double sumOfSquares = this.sumOfSquares.sum();
        double count = this.count.sum();
        // Just in case
        if (count == 0) {
            return 0.0;
        }
        double mean = sum / count;
        return Math.sqrt((sumOfSquares / count) - (mean * mean));
    }

    public long getMin() {
        return minimum.get();
    }

    public long getMax() {
        return maximum.get();
    }

    public int getCount() {
        return (int) getCountLong();
    }

    public long getCountLong() {
        return count.sum();
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0)
     *
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded.
     */
    public double getErrorPercentage() {
        long count = this.count.sum();
        if (count == 0) {
            return 0.0;
        }
        return errors.sum() / (double) count;
    }

    /**
     * Returns the throughput associated to this sampler in requests per second.
     * May be slightly skewed because it takes the timestamps of the first and
     * last samples as the total time passed, and the test may actually have
     * started before that start time and ended after that end time.
     *
     * @return throughput associated to this sampler in requests per second
     */
    public double getRate() {
        return getRatePerSecond(count.sum());
    }

    /**
     * calculates the average page size, which means divide the bytes by number
     * of samples.
     *
     * @return average page size in bytes
     */
    public double getAvgPageBytes() {
        long count = this.count.sum();
        long bytes = this.bytes.sum();
        if (count > 0 && bytes > 0) {
            return ((double) bytes) / count;
        }
        return 0.0;
    }

    /**
     * Throughput in bytes / second
     *
     * @return throughput in bytes/second
     */
    public double getBytesPerSecond() {
        return getRatePerSecond(bytes.sum());
    }

    /**
     * Sent Throughput in kilobytes / second
     *
     * @return Sent Throughput in kilobytes / second
     */
    public double getKBPerSecond() {
        return getBytesPerSecond() / 1024; // 1024=bytes per kb
    }

    /**
     * Sent bytes / second
     *
     * @return throughput in bytes/second
     */
    public double getSentBytesPerSecond() {
        return getRatePerSecond(sentBytes.sum());
    }

    /**
     * Sent bytes throughput in kilobytes / second
     *
     * @return Throughput in kilobytes / second
     */
    public double getSentKBPerSecond() {
        return getSentBytesPerSecond() / 1024; // 1024=bytes per kb
    }

    /**
     *
     * @param value value for which we compute rate
     * @return double rate
     */
    private double getRatePerSecond(long value) {
        long elapsedTime = this.elapsedTime.get();
        if (elapsedTime > 0) {
            return value / ((double) elapsedTime / 1000); // 1000 = millisecs/sec
        }
        return 0.0;
    }

}
