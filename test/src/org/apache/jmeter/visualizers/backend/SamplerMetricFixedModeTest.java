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

package org.apache.jmeter.visualizers.backend;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

public class SamplerMetricFixedModeTest {

    private static final int DEFAULT_ELAPSED_TIME = 1_000;

    /**
     * Method to change a static final field
     */
    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    @Before
    public void initMode() throws Exception {
        setFinalStatic(SamplerMetric.class.getDeclaredField("WINDOW_MODE"), WindowMode.FIXED);
    }

    @Test
    public void checkResetOkAndAllStats() throws Exception {

        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(true));
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getHits(), 0.0);

        // In fixed mode DescriptiveStatistics are not reset, just sliding on a
        // window
        metric.resetForTimeInterval();

        assertEquals("After reset in FIXED mode ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime(), 0.001);
        assertEquals("After reset in FIXED mode all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getHits(), 0.0);
    }

    @Test
    public void checkResetKoAndAllStats() throws Exception {

        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(false));
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime(), 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getFailures(), 0.0);

        // In fixed mode DescriptiveStatistics are not reset, just sliding on a
        // window
        metric.resetForTimeInterval();

        assertEquals("After reset in FIXED mode  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime(), 0.0);
        assertEquals("After reset in FIXED mode all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getFailures(), 0.001);
    }

    @Test
    public void checkErrorsDetailStat() {

        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult("400", "bad request"));
        metric.add(createSampleResult("400", "Bad Request "));
        metric.add(createSampleResult("500", "Internal Server Error"));
        ErrorMetric error = new ErrorMetric(createSampleResult("400", "Bad request"));
        assertEquals("Count for '400 - bad request' error ", 2, metric.getErrors().get(error), 0.0);
        error = new ErrorMetric(createSampleResult("500", "Internal Server Error"));
        assertEquals("Count for '500 - Internal Server Error' error ", 1, metric.getErrors().get(error), 0.0);
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

    private SampleResult createSampleResult(String errorCode, String errorMessage) {
        SampleResult result = createSampleResult(false);
        result.setResponseCode(errorCode);
        result.setResponseMessage(errorMessage);
        return result;
    }
}
