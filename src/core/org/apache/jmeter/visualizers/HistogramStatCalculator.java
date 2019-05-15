package org.apache.jmeter.visualizers;

import java.util.HashMap;
import java.util.Map;

import org.HdrHistogram.Histogram;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.math.IStatCalculator;
import org.slf4j.LoggerFactory;

public class HistogramStatCalculator implements IStatCalculator<Long> {

    private final Histogram histogram = new Histogram(JMeterUtils.getPropDefault("histogram.accuracy", 3));
    private long bytes = 0;
    private long sentBytes = 0;
    private long sum = 0;
    private double m2 = 0.0;
    private double mean = 0.0;
    private long count = 0;

    public HistogramStatCalculator() {
        LoggerFactory.getLogger(this.getClass()).debug("HistogramStatCalculator used.");
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
            count += histoCalc.count;
            bytes += histoCalc.bytes;
            sentBytes += histoCalc.sentBytes;
            histogram.add(histoCalc.histogram);
            mean = histogram.getMean();
            m2 = histogram.getStdDeviation() * histogram.getStdDeviation();
        } else {
            throw new IllegalArgumentException("Only instances of HistogramStatCalculator allowed.");
        }
    }

    @Override
    public Long getMedian() {
        return Long.valueOf(histogram.getValueAtPercentile(50));
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
        return Long.valueOf(histogram.getValueAtPercentile(100.0 * percent));
    }

    @Override
    public Map<Number, Number[]> getDistribution() {
        Map<Number, Number[]> result = new HashMap<>();
        histogram.percentiles(5).forEach(p -> {
            Long valueIteratedTo = Long.valueOf(p.getValueIteratedTo());
            result.put(valueIteratedTo,
                    new Number[] { valueIteratedTo, Long.valueOf(p.getCountAddedInThisIterationStep()) });
        });
        return result;
    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public double getStandardDeviation() {
        return Math.sqrt(m2);
    }

    @Override
    public Long getMin() {
        return Long.valueOf(histogram.getMinValue());
    }

    @Override
    public Long getMax() {
        return Long.valueOf(histogram.getMaxValue());
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    public void addValue(Long value, long sampleCount) {
        long lval = value.longValue();
        sum += lval * sampleCount;
        histogram.recordValueWithCount(lval, sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            count++;
            double delta = lval - mean;
            mean += delta / count;
            double delta2 = lval - mean;
            m2 += delta * delta2;
        }
    }

    @Override
    public void addValue(Long value) {
        long lval = value.longValue();
        sum += lval;
        histogram.recordValue(lval);
        count++;
        double delta = lval - mean;
        mean += delta / count;
        double delta2 = lval - mean;
        m2 += delta * delta2;
    }

}
