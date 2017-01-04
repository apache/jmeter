package org.apache.jmeter.visualizers.backend;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

public class SamplerMetricInfluxdbTest {

    private static final int DEFAULT_ELAPSED_TIME = 5_000;

    @Test
    public void checkResetOkAndAllStats() {
        SamplerMetric metric = new SamplerMetric();

        metric.add(createSampleResult(true));
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);

        metric.resetForTimeInterval();
        assertEquals("Before reset  ok.max", Double.NaN, metric.getOkMaxTime(), 0.0);
        assertEquals("Before reset all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
    }

    @Test
    public void checkResetKoAndAllStats() {
        SamplerMetric metric = new SamplerMetric();

        metric.add(createSampleResult(false));
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);

        metric.resetForTimeInterval();
        assertEquals("Before reset  ko.max", Double.NaN, metric.getKoMaxTime(), 0.0);
        assertEquals("Before reset all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
    }

    private SampleResult createSampleResult(boolean success) {
        SampleResult result = new SampleResult();
        result.setSuccessful(success);
        result.setSampleCount(1);
        result.setErrorCount(success ? 0 : 1);
        result.sampleStart();
        result.setEndTime(result.getStartTime() + DEFAULT_ELAPSED_TIME);
        return result;
    }
}
