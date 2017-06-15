package org.apache.jmeter.visualizers.backend;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

public class SamplerMetricInfluxdbTest {

    private static final int DEFAULT_ELAPSED_TIME = 1_000;

    /**
     * Method to change a static final field
     * @param field
     * @param newValue
     * @throws Exception
     */
    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
     }

    @Test
    public void checkResetOkAndAllStats() throws NoSuchFieldException, SecurityException, Exception {
        setFinalStatic(SamplerMetric.class.getDeclaredField("WINDOW_MODE"), WindowMode.FIXED);
        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(true));
        assertEquals("Before reset  ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime() , 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getHits(), 0.0);
        
        // In fixed mode DescriptiveStatistics are not reset, just sliding on a window
        metric.resetForTimeInterval();
        
        assertEquals("After reset in FIXED mode ok.max", DEFAULT_ELAPSED_TIME, metric.getOkMaxTime() , 0.0);
        assertEquals("After reset in FIXED mode all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getHits(), 0.0);
        
        // Change mode to TIMED, now all metric are reset on each interval
        setFinalStatic(SamplerMetric.class.getDeclaredField("WINDOW_MODE"), WindowMode.TIMED);
        metric.resetForTimeInterval();
        
        assertEquals("After reset in TIMED mode ok.max", Double.NaN, metric.getOkMaxTime() , 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
        
    }

    @Test
    public void checkResetKoAndAllStats() throws NoSuchFieldException, SecurityException, Exception {
       
        setFinalStatic(SamplerMetric.class.getDeclaredField("WINDOW_MODE"), WindowMode.FIXED);
        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult(false));
        assertEquals("Before reset  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime() , 0.001);
        assertEquals("Before reset all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.001);
        assertEquals("Before reset failure", 1, metric.getFailures(), 0.0);
        
        // In fixed mode DescriptiveStatistics are not reset, just sliding on a window
        metric.resetForTimeInterval();
        
        assertEquals("After reset in FIXED mode  ko.max", DEFAULT_ELAPSED_TIME, metric.getKoMaxTime() , 0.0);
        assertEquals("After reset in FIXED mode all.max", DEFAULT_ELAPSED_TIME, metric.getAllMaxTime(), 0.0);
        assertEquals("After reset failure", 0, metric.getFailures(), 0.001);
        
        // Change mode to TIMED, now all metric are reset on each interval
        setFinalStatic(SamplerMetric.class.getDeclaredField("WINDOW_MODE"), WindowMode.TIMED);
        metric.resetForTimeInterval();
        
        assertEquals("After reset in TIMED mode  ko.max", Double.NaN, metric.getKoMaxTime() , 0.0);
        assertEquals("After reset in TIMED mode all.max", Double.NaN, metric.getAllMaxTime(), 0.0);
    }
    

    @Test
    public void checkErrorsDetailStat() {
            
        SamplerMetric metric = new SamplerMetric();
        metric.add(createSampleResult("400", "bad request"));
        metric.add(createSampleResult("400", "Bad Request "));
        metric.add(createSampleResult("500", "Internal Server Error"));
        ErrorMetric error = new ErrorMetric( createSampleResult("400", "Bad request") );
        assertEquals("Count for '400 - bad request' error ", 2,  metric.getErrors().get(error) , 0.0);
        error = new ErrorMetric( createSampleResult("500", "Internal Server Error") );
        assertEquals("Count for '500 - Internal Server Error' error ", 1,  metric.getErrors().get(error) , 0.0);
        
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