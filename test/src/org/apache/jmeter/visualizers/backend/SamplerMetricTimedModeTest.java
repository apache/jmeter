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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;

public class SamplerMetricTimedModeTest {

    private static final int DEFAULT_ELAPSED_TIME = 1_000;
    public static void createJmeterEnv() throws IOException {
        File propsFile;
        try {
            propsFile = File.createTempFile("jmeter", ".properties");
            propsFile.deleteOnExit();
            JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        JMeterUtils.setLocale(new Locale("ignoreResources"));
    }
    
    @Before
    public void initMode() throws IOException  {
        createJmeterEnv();
        JMeterUtils.setProperty("backend_metrics_window_mode", "TIMED");
     }

    @Test
    public void checkResetOkAndAllStats() throws NoSuchFieldException, SecurityException, Exception {

        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(true));
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime() , 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getHits(), 0.0);
       
        metric.resetForTimeInterval();
        
        assertEquals("After reset in TIMED mode ok.max", Double.NaN, metric.getOkMaxTime() , 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getHits(), 0.0);
        
    }

    @Test
    public void checkResetKoAndAllStats() throws NoSuchFieldException, SecurityException, Exception {
       
    
        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(false));
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime() , 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getFailures(), 0.0);
   
        metric.resetForTimeInterval();
        
        assertEquals("After reset in TIMED mode  ko.max", Double.NaN, metric.getKoMaxTime() , 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getFailures(), 0.001);
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
