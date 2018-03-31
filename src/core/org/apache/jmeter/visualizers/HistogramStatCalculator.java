package org.apache.jmeter.visualizers;

import java.util.HashMap;
import java.util.Map;

import org.HdrHistogram.Histogram;
import org.apache.jorphan.math.IStatCalculator;
import org.slf4j.LoggerFactory;

public class HistogramStatCalculator implements IStatCalculator<Long> {
    
    private final Histogram histogram = new Histogram(3);
    private long bytes = 0;
    private long sentBytes = 0;
    private long sum = 0;
    private double m2 = 0.0;
    private double mean = 0.0;
    private long count = 0;

    public HistogramStatCalculator() {
        LoggerFactory.getLogger(this.getClass()).info("HistogramStatCalculator used.");
    }

    @Override
    public void clear() {
        histogram.reset();
        bytes = 0;
        sentBytes = 0;
        sum = 0;
        m2 = 0.0;
        mean = 0.0;
        count = 0;
    }

    @Override
    public void addBytes(long newValue) {
        bytes += newValue;

    }

    @Override
    public void addSentBytes(long newValue) {
        sentBytes += newValue;
    }

    @Override
    public void addAll(IStatCalculator<Long> calc) {
        if (calc instanceof HistogramStatCalculator) {
            HistogramStatCalculator histoCalc = (HistogramStatCalculator) calc;
            sum += histoCalc.sum;
            bytes += histoCalc.bytes;
            sentBytes += histoCalc.sentBytes;
            histogram.add(histoCalc.histogram);
        } else {
            throw new IllegalArgumentException("Only instances of HistogramStatCalculator allowed.");
        }

    }

    @Override
    public Long getMedian() {
        return histogram.getValueAtPercentile(50);
    }

    @Override
    public long getTotalBytes() {
        return bytes;
    }

    @Override
    public long getTotalSentBytes() {
        return sentBytes;
    }

    @Override
    public Long getPercentPoint(float percent) {
        return getPercentPoint((double) percent);
    }

    @Override
    public Long getPercentPoint(double percent) {
        return histogram.getValueAtPercentile(100.0 * percent);
    }

    @Override
    public Map<Number, Number[]> getDistribution() {
        Map<Number, Number[]> result = new HashMap<>();
        histogram.percentiles(5).forEach(p -> {
            result.put(p.getValueIteratedTo(), new Number[] { p.getValueIteratedTo(), p.getCountAddedInThisIterationStep() } );
        });
        return result;
    }

    @Override
    public double getMean() {
        return mean;
//        return histogram.getMean();
    }

    @Override
    public double getStandardDeviation() {
        return Math.sqrt(m2);
//        return histogram.getStdDeviation();
    }

    @Override
    public Long getMin() {
        return histogram.getMinValue();
    }

    @Override
    public Long getMax() {
        return histogram.getMaxValue();
    }

    @Override
    public long getCount() {
        return count;
//        return histogram.getTotalCount();
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    public void addValue(Long val, long sampleCount) {
        sum += val * sampleCount;
        histogram.recordValueWithCount(val, sampleCount);
        count += sampleCount;
        double delta = val - mean;
        mean = mean + delta / count;
        double delta2 = val - mean;
        m2 = m2 + delta * delta2;
    }

    @Override
    public void addValue(Long val) {
        sum += val;
        histogram.recordValue(val);
        count++;
        double delta = val - mean;
        mean = mean + delta / count;
        double delta2 = val - mean;
        m2 = m2 + delta * delta2;
    }

}
