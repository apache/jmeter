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

package org.apache.jorphan.math;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableLong;

/**
 * This class serves as a way to calculate the median, max, min etc. of a list of values.
 * It is not threadsafe.
 *
 * @param <T> type parameter for the calculator
 *
 */
public abstract class StatCalculator<T extends Number & Comparable<? super T>> implements IStatCalculator<T> {

    // key is the type to collect (usually long), value = count of entries
    private final Map<T, MutableLong> valuesMap = new TreeMap<>();
    // We use a TreeMap because we need the entries to be sorted

    // Running values, updated for each sample
    private double sum = 0;

    private double sumOfSquares = 0;

    private double mean = 0;

    private double deviation = 0;

    private long count = 0;

    private T min;

    private T max;

    private long bytes = 0;
    
    private long sentBytes = 0;

    private final T ZERO;

    private final T MAX_VALUE; // e.g. Long.MAX_VALUE

    private final T MIN_VALUE; // e.g. Long.MIN_VALUE

    /**
     * This constructor is used to set up particular values for the generic class instance.
     *
     * @param zero - value to return for Median and PercentPoint if there are no values
     * @param min - value to return for minimum if there are no values
     * @param max - value to return for maximum if there are no values
     */
    public StatCalculator(final T zero, final T min, final T max) {
        super();
        ZERO = zero;
        MAX_VALUE = max;
        MIN_VALUE = min;
        this.min = MAX_VALUE;
        this.max = MIN_VALUE;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#clear()
     */
    @Override
    public void clear() {
        valuesMap.clear();
        sum = 0;
        sumOfSquares = 0;
        mean = 0;
        deviation = 0;
        count = 0;
        bytes = 0;
        sentBytes = 0;
        max = MIN_VALUE;
        min = MAX_VALUE;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#addBytes(long)
     */
    @Override
    public void addBytes(long newValue) {
        bytes += newValue;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#addSentBytes(long)
     */
    @Override
    public void addSentBytes(long newValue) {
        sentBytes += newValue;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#addAll(org.apache.jorphan.math.StatCalculator)
     */
    public void addAll(IStatCalculator<T> calc) {
        if (this.getClass().isAssignableFrom(calc.getClass())) {
            for(Entry<T, MutableLong> ent : ((StatCalculator<T>) calc).valuesMap.entrySet()) {
                addEachValue(ent.getKey(), ent.getValue().longValue());
            }
        } else {
            throw new RuntimeException("Incompatible StatCalculator was given.");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getMedian()
     */
    @Override
    public T getMedian() {
        return getPercentPoint(0.5);
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getTotalBytes()
     */
    @Override
    public long getTotalBytes() {
        return bytes;
    }
    
    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getTotalSentBytes()
     */
    @Override
    public long getTotalSentBytes() {
        return sentBytes;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getPercentPoint(float)
     */
    @Override
    public T getPercentPoint(float percent) {
        return getPercentPoint((double) percent);
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getPercentPoint(double)
     */
    @Override
    public T getPercentPoint(double percent) {
        if (count <= 0) {
                return ZERO;
        }
        if (percent >= 1.0) {
            return getMax();
        }

        // use Math.round () instead of simple (long) to provide correct value rounding
        long target = Math.round(count * percent);
        try {
            for (Entry<T, MutableLong> val : valuesMap.entrySet()) {
                target -= val.getValue().longValue();
                if (target <= 0){
                    return val.getKey();
                }
            }
        } catch (ConcurrentModificationException ignored) {
            // ignored. May happen occasionally, but no harm done if so.
        }
        return ZERO; // TODO should this be getMin()?
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getDistribution()
     */
    @Override
    public Map<Number, Number[]> getDistribution() {
        Map<Number, Number[]> items = new HashMap<>();

        for (Entry<T, MutableLong> entry : valuesMap.entrySet()) {
            Number[] dis = new Number[2];
            dis[0] = entry.getKey();
            dis[1] = entry.getValue();
            items.put(entry.getKey(), dis);
        }
        return items;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getMean()
     */
    @Override
    public double getMean() {
        return mean;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getStandardDeviation()
     */
    @Override
    public double getStandardDeviation() {
        return deviation;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getMin()
     */
    @Override
    public T getMin() {
        return min;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getMax()
     */
    @Override
    public T getMax() {
        return max;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getCount()
     */
    @Override
    public long getCount() {
        return count;
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#getSum()
     */
    @Override
    public double getSum() {
        return sum;
    }

    protected abstract T divide(T val, int n);

    protected abstract T divide(T val, long n);

    /**
     * Update the calculator with the values for a set of samples.
     * 
     * @param val the common value, normally the elapsed time
     * @param sampleCount the number of samples with the same value
     */
    void addEachValue(T val, long sampleCount) {
        count += sampleCount;
        double currentVal = val.doubleValue();
        sum += currentVal * sampleCount;
        // For n same values in sum of square is equal to n*val^2
        sumOfSquares += currentVal * currentVal * sampleCount;
        updateValueCount(val, sampleCount);
        calculateDerivedValues(val);
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#addValue(T, long)
     */
    @Override
    public void addValue(T val, long sampleCount) {
        count += sampleCount;
        double currentVal = val.doubleValue();
        sum += currentVal;
        T actualValue = val;
        if (sampleCount > 1){
            // For n values in an aggregate sample the average value = (val/n)
            // So need to add n * (val/n) * (val/n) = val * val / n
            sumOfSquares += currentVal * currentVal / sampleCount;
            actualValue = divide(val, sampleCount);
        } else { // no need to divide by 1
            sumOfSquares += currentVal * currentVal;
        }
        updateValueCount(actualValue, sampleCount);
        calculateDerivedValues(actualValue);
    }

    private void calculateDerivedValues(T actualValue) {
        mean = sum / count;
        deviation = Math.sqrt((sumOfSquares / count) - (mean * mean));
        if (actualValue.compareTo(max) > 0){
            max=actualValue;
        }
        if (actualValue.compareTo(min) < 0){
            min=actualValue;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jorphan.math.IStatCalculator#addValue(T)
     */
    @Override
    public void addValue(T val) {
        addValue(val, 1L);
    }

    private void updateValueCount(T actualValue, long sampleCount) {
        MutableLong count = valuesMap.get(actualValue);
        if (count != null) {
            count.add(sampleCount);
        } else {
            // insert new value
            valuesMap.put(actualValue, new MutableLong(sampleCount));
        }
    }
}
