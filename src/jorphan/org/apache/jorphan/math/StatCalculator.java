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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class serves as a way to calculate the median, max, min etc. of a list of values.
 * It is not threadsafe.
 * 
 */
public abstract class StatCalculator<T extends Number & Comparable<? super T>> {
    
    private final List<T> values = new ArrayList<T>();

    private double sum = 0;

    private double sumOfSquares = 0;

    private double mean = 0;

    private double deviation = 0;

    private int count = 0;

    private long bytes = 0;

    private final T ZERO;
    
    private final T MAX_VALUE;
    
    private final T MIN_VALUE;
    
    /**
     * This constructor is used to set up particular values for the generic class instance.
     *
     * @param zero - value to return for Median and PercentPoint if there are no values
     * @param min - value to return for minimum if there are no values
     * @param max - value to return for maximum if there are no values
     */
    public StatCalculator(T zero, T min, T max) {
        super();
        ZERO = zero;
        MAX_VALUE = max;
        MIN_VALUE = min;
    }

    public void clear() {
        values.clear();
        sum = 0;
        sumOfSquares = 0;
        mean = 0;
        deviation = 0;
        count = 0;
    }


    public void addBytes(long newValue) {
        bytes += newValue;
    }

    public void addAll(StatCalculator<T> calc) {
        Iterator<T> iter = calc.values.iterator();
        while (iter.hasNext()) {
            addValue(iter.next());
        }
    }

    public T getMedian() {
        if (count > 0) {
            return values.get((int) (values.size() * .5));
        }
        return ZERO;
    }

    public long getTotalBytes() {
        return bytes;
    }

    /**
     * Get the value which %percent% of the values are less than. This works
     * just like median (where median represents the 50% point). A typical
     * desire is to see the 90% point - the value that 90% of the data points
     * are below, the remaining 10% are above.
     *
     * @param percent
     * @return number of values less than the percentage
     */
    public T getPercentPoint(float percent) {
        if (count > 0) {
            return values.get((int) (values.size() * percent));
        }
        return ZERO;
    }

    /**
     * Get the value which %percent% of the values are less than. This works
     * just like median (where median represents the 50% point). A typical
     * desire is to see the 90% point - the value that 90% of the data points
     * are below, the remaining 10% are above.
     *
     * @param percent
     * @return number of values less than the percentage
     */
    public T getPercentPoint(double percent) {
        if (count > 0) {
            return values.get((int) (values.size() * percent));
        }
        return ZERO;
    }

    /**
     * Returns the distribution of the values in the list.
     * 
     * TODO round values to reduce the number of distinct entries.
     *
     * @return map containing either Integer or Long keys; entries are a Number array containing the key and the [Integer] count.
     * TODO - why is the key value also stored in the entry array?
     */
    public synchronized HashMap<Number, Number[]> getDistribution() {
        HashMap<Number, Number[]> items = new HashMap<Number, Number[]>();
        Iterator<T> itr = this.values.iterator();
        Number[] dis;
        while (itr.hasNext()) {
            Number nx = itr.next();
            if (!(nx instanceof Integer || nx instanceof Long)){
                nx=new Long(nx.longValue()); // convert to Long unless Integer or Long
            }
            if (items.containsKey(nx)) {
                dis = items.get(nx);
                dis[1] = new Integer(dis[1].intValue() + 1);
                items.put(nx, dis);
            } else {
                dis = new Number[2];
                dis[0] = nx;
                dis[1] = new Integer(1);
                items.put(nx, dis);
            }
        }
        return items;
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return deviation;
    }

    public T getMin() {
        if (count > 0) {
            return values.get(0);
        }
        return MIN_VALUE;
    }

    public T getMax() {
        if (count > 0) {
            return values.get(count - 1);
        }
        return MAX_VALUE;
    }

    public int getCount() {
        return count;
    }

    public void addValue(T val) {
        addSortedValue(val);
        count++;
        double currentVal = val.doubleValue();
        sum += currentVal;
        sumOfSquares += currentVal * currentVal;
        mean = sum / count;
        deviation = Math.sqrt((sumOfSquares / count) - (mean * mean));
    }

    private void addSortedValue(T val) {
        int index = Collections.binarySearch(values, val);
        if (index >= 0 && index < values.size()) {
            values.add(index, val);
        } else if (index == values.size() || values.size() == 0) {
            values.add(val);
        } else {
            values.add((index * (-1)) - 1, val);
        }
    }
}
