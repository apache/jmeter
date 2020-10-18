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

package org.apache.jmeter.visualizers.backend;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SamplerMetricTimedModeTest {

    private static final int DEFAULT_ELAPSED_TIME = 1_000;
    private static final double ALLOWED_DELTA = 25.0;

    @BeforeEach
    @SuppressWarnings("deprecation")
    public void initMode() throws Exception {
        SamplerMetric.setDefaultWindowMode(WindowMode.TIMED);
    }

    @Test
    public void checkResetOkAndAllStats() throws Exception {

        SamplerMetric metric = new SamplerMetric();

        metric.add(createSampleResult(true));
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset hits", 1, metric.getHits(), 0.0);
        assertEquals("Before reset sent bytes", 1000, metric.getSentBytes(), 0.0);
        assertEquals("Before reset received bytes", 2000, metric.getReceivedBytes(), 0.0);

        metric.resetForTimeInterval();

        assertEquals("After reset in TIMED mode ok.max", Double.NaN, metric.getOkMaxTime(), 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset hits", 0, metric.getHits(), 0.0);
        assertEquals("After reset sent bytes", 0, metric.getSentBytes(), 0.0);
        assertEquals("After reset received bytes", 0, metric.getReceivedBytes(), 0.0);
    }

    @Test
    public void checkResetKoAndAllStats() throws Exception {

        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(false));
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getFailures(), 0.0);
        assertEquals("Before reset sent bytes", 1000, metric.getSentBytes(), 0.0);
        assertEquals("Before reset received bytes", 2000, metric.getReceivedBytes(), 0.0);

        metric.resetForTimeInterval();

        assertEquals("After reset in TIMED mode  ko.max", Double.NaN, metric.getKoMaxTime(), 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getFailures(), 0.001);
        assertEquals("After reset sent bytes", 0, metric.getSentBytes(), 0.0);
        assertEquals("After reset received bytes", 0, metric.getReceivedBytes(), 0.0);
    }

    private SampleResult createSampleResult(boolean success) {
        SampleResult result = new SampleResult();
        result.setSuccessful(success);
        result.setSampleCount(1);
        result.setErrorCount(success ? 0 : 1);
        result.sampleStart();
        result.setSentBytes(1000);
        result.setBytes(2000L);
        result.setEndTime(result.getStartTime() + DEFAULT_ELAPSED_TIME);
        return result;
    }

    private SampleResult createSampleResultWithSubresults(boolean success) {
        SampleResult result = new SampleResult();
        result.sampleStart();
        result.setSampleCount(1);
        result.setErrorCount(success ? 0 : 1);
        result.setSuccessful(success);
        result.addSubResult(createSampleResult(success));
        result.addSubResult(createSampleResult(success));
        result.setEndTime(Arrays.stream(result.getSubResults()).mapToLong(SampleResult::getEndTime).max().orElse(0));
        result.setBytes(Arrays.stream(result.getSubResults()).mapToLong(SampleResult::getBytesAsLong).sum());
        result.setSentBytes(Arrays.stream(result.getSubResults()).mapToLong(SampleResult::getSentBytes).sum());
        result.setResponseMessage("Number of samples in transaction : "); // This is a constant in TransactionController
        return result;
    }

    @Test
    public void checkAddCumulatedOk() throws Exception {
        SamplerMetric metric = new SamplerMetric();
        SampleResult sample = createSampleResultWithSubresults(true);
        assertEquals("We are recognized as a TransactionController made sample", Boolean.TRUE,
                Boolean.valueOf(TransactionController.isFromTransactionController(sample)));
        metric.addCumulated(sample);
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime(), ALLOWED_DELTA);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), ALLOWED_DELTA);
        assertEquals("Before reset hits", 2, metric.getHits(), 0.0);
        assertEquals("Before reset sent bytes", 2000, metric.getSentBytes(), 0.0);
        assertEquals("Before reset received bytes", 4000, metric.getReceivedBytes(), 0.0);

        metric.resetForTimeInterval();

        assertEquals("After reset in TIMED mode ok.max", Double.NaN, metric.getOkMaxTime(), 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset hits", 0, metric.getHits(), 0.0);
        assertEquals("After reset sent bytes", 0, metric.getSentBytes(), 0.0);
        assertEquals("After reset received bytes", 0, metric.getReceivedBytes(), 0.0);
    }

    @Test
    public void checkAddCumulatedKo() throws Exception {
        SamplerMetric metric = new SamplerMetric();
        SampleResult sample = createSampleResultWithSubresults(false);
        assertEquals("We are recognized as a TransactionController made sample", Boolean.TRUE,
                Boolean.valueOf(TransactionController.isFromTransactionController(sample)));
        metric.addCumulated(sample);
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime(), ALLOWED_DELTA);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), ALLOWED_DELTA);
        assertEquals("Before reset failures", 1, metric.getFailures(), 0.0);
        assertEquals("Before reset sent bytes", 2000, metric.getSentBytes(), 0.0);
        assertEquals("Before reset received bytes", 4000, metric.getReceivedBytes(), 0.0);

        metric.resetForTimeInterval();

        assertEquals("After reset in TIMED mode ko.max", Double.NaN, metric.getKoMaxTime(), 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failures", 0, metric.getFailures(), 0.0);
        assertEquals("After reset sent bytes", 0, metric.getSentBytes(), 0.0);
        assertEquals("After reset received bytes", 0, metric.getReceivedBytes(), 0.0);
    }

}
