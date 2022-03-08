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

package org.apache.jmeter.report.dashboard;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jodd.props.Props;

public class ApdexPerTransactionTest extends JMeterTestCase {

    // prop in the file mixes comma, semicolon and spans several lines.
    // it also includes hardcoded sample names mixed with regexes
    private static final String apdexString = "sample(\\d+):1000|2000;samples12:3000|4000;scenar01-12:5000|6000";

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    @Test
    public void testgetApdexPerTransactionProperty() throws Exception {
        final Props props = new Props();
        final String REPORT_GENERATOR_KEY_PREFIX = "jmeter.reportgenerator";
        final char KEY_DELIMITER = '.';
        final String REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION = REPORT_GENERATOR_KEY_PREFIX
                + KEY_DELIMITER + "apdex_per_transaction";

        props.load(this.getClass().getResourceAsStream("reportgenerator_test.properties"));
        final String apdexPerTransaction = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION);
        Assertions.assertEquals(apdexString, apdexPerTransaction);
    }

    @Test
    public void testgetApdexPerTransactionPropertySimple() throws Exception {
        final Props props = new Props();
        props.load(this.getClass().getResourceAsStream("reportgenerator_test.properties"));
        final String title = getOptionalProperty(props,
                "jmeter.reportgenerator.graph.responseTimePercentiles.title");
        Assertions.assertNotNull(title, "title should not be null");
    }

    @Test
    public void testGetApdexPerTransactionParts() {
        Map<String, Long[]> apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts(apdexString);
        Assertions.assertNotNull(apdex, "map should not be null");
        Assertions.assertEquals(3, apdex.size());
        Set<String> keys = apdex.keySet();
        Assertions.assertTrue(keys.contains("samples12"));
        Assertions.assertTrue(keys.contains("scenar01-12"));
        Assertions.assertTrue(keys.contains("sample(\\d+)"));
        Assertions.assertArrayEquals(new Long[] {1000L,  2000L}, apdex.get("sample(\\d+)"));
    }

   @Test
    public void testGetApdexPerTransactionPartsOneCustomization() {
        Map<String, Long[]> apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts("sample(\\d+):1000|2000");
        Assertions.assertNotNull(apdex, "map should not be null");
        Assertions.assertEquals(1, apdex.size());
        Set<String> keys = apdex.keySet();
        Assertions.assertTrue(keys.contains("sample(\\d+)"));
        Assertions.assertArrayEquals(new Long[] {1000L,  2000L}, apdex.get("sample(\\d+)"));
    }

   @Test
   public void testGetApdexPerTransactionNoValue() {
       Map<String, Long[]> apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts("");
       Assertions.assertNotNull(apdex, "map should not be null");
       Assertions.assertEquals(0, apdex.size());

       apdex = ReportGeneratorConfiguration.getApdexPerTransactionParts(" ");
       Assertions.assertNotNull(apdex, "map should not be null");
       Assertions.assertEquals(0, apdex.size());
   }

   @Test
   public void testGetApdexPerTransactionWrongFormat() {
       Map<String, Long[]> apdex =
               ReportGeneratorConfiguration.getApdexPerTransactionParts("sample1|123:434");
       Assertions.assertNotNull(apdex, "map should not be null");
       Assertions.assertEquals(0, apdex.size());
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
                if (USE_JAVA_REGEX) {
                    Pattern pattern = JMeterUtils.compilePattern(entry.getKey());
                    Matcher matcher = pattern.matcher(sampleName);
                    if (matcher.matches()) {
                        hasMatched = true;
                    }
                } else {
                    org.apache.oro.text.regex.Pattern regex = JMeterUtils.getPatternCache().getPattern(entry.getKey());
                    PatternMatcher matcher = JMeterUtils.getMatcher();
                    if (matcher.matches(sampleName, regex)) {
                        hasMatched = true;
                    }
                }
            }
            Assertions.assertTrue(hasMatched);
        }

    }

    private static String getOptionalProperty(Props props, String key) {
        return getProperty(props, key, null);
    }

    private static String getProperty(Props props, String key, String defaultValue) {
        String value = props.getValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
