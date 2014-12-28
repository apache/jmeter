package org.apache.jmeter.reporters;

import java.text.DecimalFormat;

import org.apache.jmeter.samplers.SampleResult;

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

    private final int index;

    public SummariserRunningSample(String label, int index) {
        this.label = label;
        this.index = index;
        init();
    }

    /**
     * Copy constructor
     * @param src the instance to copy
     */
    public SummariserRunningSample(SummariserRunningSample src) {
        label = src.label;
        index = src.index;
        counter = src.counter;
        errorCount = src.errorCount;
        startTime = src.startTime;
        endTime = src.endTime;
        max = src.max;
        min = src.min;
        runningSum = src.runningSum;
    }

    private void init() {
        counter = 0L;
        runningSum = 0L;
        max = Long.MIN_VALUE;
        min = Long.MAX_VALUE;
        errorCount = 0L;
        startTime = System.currentTimeMillis();
        endTime = startTime;
    }

    public void clear() {
        init();
    }

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
     * the RunningSample class.
     *
     * @return the number of samples that have been recorded by this instance of
     *         the RunningSample class.
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

    public void setEndTime() {
        endTime = System.currentTimeMillis();
    }

}
