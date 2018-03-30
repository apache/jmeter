package org.apache.jorphan.math;

import java.util.Map;

public interface IStatCalculator<T extends Number & Comparable<? super T>> {

    void clear();

    /**
     * Add to received bytes
     * @param newValue number of newly received bytes
     */
    void addBytes(long newValue);

    /**
     * Add to sent bytes
     * @param newValue number of newly sent bytes
     */
    void addSentBytes(long newValue);

    void addAll(IStatCalculator<T> calc);

    T getMedian();

    long getTotalBytes();

    long getTotalSentBytes();

    /**
     * Get the value which %percent% of the values are less than. This works
     * just like median (where median represents the 50% point). A typical
     * desire is to see the 90% point - the value that 90% of the data points
     * are below, the remaining 10% are above.
     *
     * @param percent
     *            number representing the wished percent (between <code>0</code>
     *            and <code>1.0</code>)
     * @return number of values less than the percentage
     */
    T getPercentPoint(float percent);

    /**
     * Get the value which %percent% of the values are less than. This works
     * just like median (where median represents the 50% point). A typical
     * desire is to see the 90% point - the value that 90% of the data points
     * are below, the remaining 10% are above.
     *
     * @param percent
     *            number representing the wished percent (between <code>0</code>
     *            and <code>1.0</code>)
     * @return the value which %percent% of the values are less than
     */
    T getPercentPoint(double percent);

    /**
     * Returns the distribution of the values in the list.
     *
     * @return map containing either Integer or Long keys; entries are a Number array containing the key and the [Integer] count.
     * TODO - why is the key value also stored in the entry array? See Bug 53825
     */
    Map<Number, Number[]> getDistribution();

    double getMean();

    double getStandardDeviation();

    T getMin();

    T getMax();

    long getCount();

    double getSum();

    /**
     * Update the calculator with the value for an aggregated sample.
     * 
     * @param val the aggregate value, normally the elapsed time
     * @param sampleCount the number of samples contributing to the aggregate value
     */
    void addValue(T val, long sampleCount);

    /**
     * Add a single value (normally elapsed time)
     * 
     * @param val the value to add, which should correspond with a single sample
     * @see #addValue(Number, long)
     */
    void addValue(T val);

}