package org.apache.jmeter.report.dashboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jodd.props.Props;

public class ApdexPerTransactionTest {
	
	private static final Logger log = LoggerFactory.getLogger(ApdexPerTransactionTest.class);
	
	// prop in the file mixes comma, semicolon and spans several lines.
	// it also includes hardcoded sample names mixed with regexes 
	private static final String apdexString = "sample(\\d+):1000|2000,samples12:3000|4000;scenar01-12:5000|6000";
	
	@Test
	public void testgetApdexPerTransactionProperty() {
		final Properties merged = new Properties();
		final Props props = new Props();
		final String REPORT_GENERATOR_KEY_PREFIX = "jmeter.reportgenerator";
		final char KEY_DELIMITER = '.';
		final String REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION = REPORT_GENERATOR_KEY_PREFIX
	            + KEY_DELIMITER + "apdex_per_transaction";
		
		File rgp = new File("test/resources/", "reportgenerator_test.properties");
		merged.putAll(loadProps(rgp));
		props.load(merged);
		final String apdexPerTransaction = getOptionalProperty(props, 
        		REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION, 
        		String.class);
		assertEquals(apdexString, apdexPerTransaction);
		
	}
	
	@Test
	public void testGetApdexPerTransactionParts() {
		Map<String, Long[]> apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts(apdexString);
		assertNotNull("map should not be null", apdex);
		assertEquals(3, apdex.size());
		Set<String> keys = apdex.keySet();
		assertTrue(keys.contains("samples12"));
		assertTrue(keys.contains("scenar01-12"));
		assertTrue(keys.contains("sample(\\d+)"));
		assertArrayEquals(new Long[] {1000L,  2000L}, apdex.get("sample(\\d+)"));
	}
	
	@Test
	public void testSampleNameMatching() {
		/* matching pairs : 
		 * sample(\d+) sample2
		 * sample(\d+) sample12
		 * scenar01-12 scenar01-12
		 * samples12 samples12
		 * */
		
		String[] sampleNames = {"sample2","sample12", "scenar01-12", "samples12"};
		
		Map<String, Long[]> apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts(apdexString);
		for (String sampleName : sampleNames) {
			boolean hasMatched = false;
			for (Map.Entry<String, Long[]> entry : apdex.entrySet()) {
				org.apache.oro.text.regex.Pattern regex = JMeterUtils.getPatternCache().getPattern(entry.getKey());
    			PatternMatcher matcher = JMeterUtils.getMatcher();
    			if(matcher.matches(sampleName, regex)) {
    				hasMatched= true;
    			}
    		}
			assertTrue(hasMatched);
    	}
		
	}
	
	private static Properties loadProps(File file) {
        final Properties props = new Properties();
        try (FileInputStream inStream = new FileInputStream(file)) {
            props.load(inStream);
        } catch (IOException e) {
            log.error("Problem loading properties. " + e); // NOSONAR
        }
        return props;
    }
	
	private static String getOptionalProperty(Props props,
            String key, Class clazz) {
        String property = getProperty(props, key, null, clazz);
        if (property != null) {
        }
        return property;
    }
	
	private static String getProperty(Props props, String key,
            String defaultValue, Class clazz)
             {
        String value = props.getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
