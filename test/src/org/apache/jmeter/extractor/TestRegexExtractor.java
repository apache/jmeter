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

package org.apache.jmeter.extractor;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class TestRegexExtractor {
    
        private RegexExtractor extractor;

        private SampleResult result;

        private JMeterVariables vars;

        private JMeterContext jmctx;

        @Before
        public void setUp() {
            jmctx = JMeterContextService.getContext();
            extractor = new RegexExtractor();
            extractor.setThreadContext(jmctx);// This would be done by the run
                                                // command
            extractor.setRefName("regVal");
            result = new SampleResult();
            String data = "<company-xmlext-query-ret>" + "<row>" + "<value field=\"RetCode\">LIS_OK</value>"
                    + "<value field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value>"
                    + "<value field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value>"
                    + "<value field=\"pinposition1\">1</value>" + "<value field=\"pinpositionvalue1\"></value>"
                    + "<value field=\"pinposition2\">5</value>" + "<value field=\"pinpositionvalue2\"></value>"
                    + "<value field=\"pinposition3\">6</value>" + "<value field=\"pinpositionvalue3\"></value>"
                    + "</row>" + "</company-xmlext-query-ret>";
            result.setResponseData(data, null);
            result.setResponseHeaders("Header1: Value1\nHeader2: Value2");
            result.setResponseCode("abcd");
            result.setResponseMessage("The quick brown fox");
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
        }

        @Test
        public void testProcessAllElementsSingleMatch() {
            vars.put("content", "one");
            extractor.setMatchNumber(-1);
            extractor.setRefName("varname");
            extractor.setRegex("(\\w+)");
            extractor.setScopeVariable("content");
            extractor.setThreadContext(jmctx);
            extractor.setTemplate("$1$");
            extractor.process();
            assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
            assertThat(vars.get("varname_matchNr"), CoreMatchers.is("1"));
        }

        @Test
        public void testProcessAllElementsMultipleMatches() {
            vars.put("content", "one, two");
            extractor.setMatchNumber(-1);
            extractor.setRefName("varname");
            extractor.setRegex("(\\w+)");
            extractor.setScopeVariable("content");
            extractor.setThreadContext(jmctx);
            extractor.setTemplate("$1$");
            extractor.process();
            assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
            assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
            assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
            assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
        }

        @Test
        public void testEmptyDefaultVariable() throws Exception {
            extractor.setRegex("<value name=\"positioncount\">(.+?)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            extractor.setDefaultEmptyValue(true);
            extractor.process();
            assertEquals("", vars.get("regVal"));
        }
        
        @Test
        public void testNotEmptyDefaultVariable() throws Exception {
            extractor.setRegex("<value name=\"positioncount\">(.+?)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            extractor.setDefaultEmptyValue(false);
            extractor.process();
            assertNull(vars.get("regVal"));
        }
        
        @Test
        public void testVariableExtraction0() throws Exception {
            extractor.setRegex("<(value) field=\"");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(0);
            extractor.process();
            assertEquals("value", vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("5", vars.get("regVal"));
            assertEquals("pinposition2", vars.get("regVal_g1"));
            assertEquals("5", vars.get("regVal_g2"));
            assertEquals("<value field=\"pinposition2\">5</value>", vars.get("regVal_g0"));
            assertNull(vars.get("regVal_g3"));
            assertEquals("2",vars.get("regVal_g"));
        }

        private static void templateSetup(RegexExtractor rex, String tmp) {
            rex.setRegex("<company-(\\w+?)-(\\w+?)-(\\w+?)>");
            rex.setMatchNumber(1);
            rex.setTemplate(tmp);
            rex.process();
        }

        @Test
        public void testTemplate1() throws Exception {
            templateSetup(extractor, "");
            assertEquals("<company-xmlext-query-ret>", vars.get("regVal_g0"));
            assertEquals("xmlext", vars.get("regVal_g1"));
            assertEquals("query", vars.get("regVal_g2"));
            assertEquals("ret", vars.get("regVal_g3"));
            assertEquals("", vars.get("regVal"));
            assertEquals("3",vars.get("regVal_g"));
        }

        @Test
        public void testTemplate2() throws Exception {
            templateSetup(extractor, "ABC");
            assertEquals("ABC", vars.get("regVal"));
        }

        @Test
        public void testTemplate3() throws Exception {
            templateSetup(extractor, "$2$");
            assertEquals("query", vars.get("regVal"));
        }

        @Test
        public void testTemplate4() throws Exception {
            templateSetup(extractor, "PRE$2$");
            assertEquals("PREquery", vars.get("regVal"));
        }

        @Test
        public void testTemplate5() throws Exception {
            templateSetup(extractor, "$2$POST");
            assertEquals("queryPOST", vars.get("regVal"));
        }

        @Test
        public void testTemplate6() throws Exception {
            templateSetup(extractor, "$2$$1$");
            assertEquals("queryxmlext", vars.get("regVal"));
        }

        @Test
        public void testTemplate7() throws Exception {
            templateSetup(extractor, "$2$MID$1$");
            assertEquals("queryMIDxmlext", vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction2() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(3);
            extractor.process();
            assertEquals("pinposition3", vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction6() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(4);
            extractor.setDefaultValue("default");
            extractor.process();
            assertEquals("default", vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction3() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("_$1$");
            extractor.setMatchNumber(2);
            extractor.process();
            assertEquals("_pinposition2", vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction5() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);// Set up the non-wild variables
            extractor.process();
            assertNotNull(vars.get("regVal"));
            assertEquals("2",vars.get("regVal_g"));
            assertNotNull(vars.get("regVal_g0"));
            assertNotNull(vars.get("regVal_g1"));
            assertNotNull(vars.get("regVal_g2"));

            extractor.setMatchNumber(-1);
            extractor.process();
            assertNotNull(vars.get("regVal"));// Should not clear this?
            assertNull(vars.get("regVal_g"));
            assertNull(vars.get("regVal_g1"));
            assertNull(vars.get("regVal_g2"));
            assertEquals("3", vars.get("regVal_matchNr"));
            assertEquals("pinposition1", vars.get("regVal_1"));
            assertEquals("pinposition2", vars.get("regVal_2"));
            assertEquals("pinposition3", vars.get("regVal_3"));
            assertEquals("2", vars.get("regVal_1_g"));
            assertEquals("pinposition1", vars.get("regVal_1_g1"));
            assertEquals("1", vars.get("regVal_1_g2"));
            assertEquals("6", vars.get("regVal_3_g2"));
            assertEquals("<value field=\"pinposition1\">1</value>", vars.get("regVal_1_g0"));
            assertNull(vars.get("regVal_4"));

            // Check old values don't hang around:
            extractor.setRegex("(\\w+)count"); // fewer matches
            extractor.process();
            assertEquals("2", vars.get("regVal_matchNr"));
            assertEquals("position", vars.get("regVal_1"));
            assertEquals("1", vars.get("regVal_1_g"));
            assertEquals("position", vars.get("regVal_1_g1"));
            assertNull("Unused variables should be null", vars.get("regVal_1_g2"));
            assertEquals("invalidpin", vars.get("regVal_2"));
            assertEquals("1", vars.get("regVal_2_g"));
            assertEquals("invalidpin", vars.get("regVal_2_g1"));
            assertNull("Unused variables should be null", vars.get("regVal_2_g2"));
            assertEquals("1", vars.get("regVal_1_g"));
            assertNull("Unused variables should be null", vars.get("regVal_3"));
            assertNull("Unused variables should be null", vars.get("regVal_3_g"));
            assertNull("Unused variables should be null", vars.get("regVal_3_g0"));
            assertNull("Unused variables should be null", vars.get("regVal_3_g1"));
            assertNull("Unused variables should be null", vars.get("regVal_3_g2"));

            // Check when match fails
            extractor.setRegex("xxxx(.)(.)");
            extractor.process();
            assertEquals("0", vars.get("regVal_matchNr"));
            assertNull("Unused variables should be null", vars.get("regVal_1"));
            assertNull("Unused variables should be null", vars.get("regVal_1_g0"));
            assertNull("Unused variables should be null", vars.get("regVal_1_g1"));
            assertNull("Unused variables should be null", vars.get("regVal_1_g2"));
        }

        @Test
        public void testVariableExtraction7() throws Exception {
            extractor.setRegex("Header1: (\\S+)");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            assertTrue("useBody should be true", extractor.useBody());
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useURL should be false", extractor.useUrl());
            extractor.setUseField(RegexExtractor.USE_BODY);
            assertTrue("useBody should be true", extractor.useBody());
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useURL should be false", extractor.useUrl());
            extractor.setUseField(RegexExtractor.USE_HDRS);
            assertTrue("useHdrs should be true", extractor.useHeaders());
            assertFalse("useBody should be false", extractor.useBody());
            assertFalse("useURL should be false", extractor.useUrl());
            extractor.process();
            assertEquals("Value1", vars.get("regVal"));
            extractor.setUseField(RegexExtractor.USE_URL);
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useBody should be false", extractor.useBody());
            assertTrue("useURL should be true", extractor.useUrl());
        }

        @Test
        public void testVariableExtraction8() throws Exception {
            extractor.setRegex("http://jakarta\\.apache\\.org/(\\w+)");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            extractor.setUseField(RegexExtractor.USE_URL);
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useBody should be false", extractor.useBody());
            assertTrue("useURL should be true", extractor.useUrl());
            extractor.process();
            assertNull(vars.get("regVal"));
            result.setURL(new URL("http://jakarta.apache.org/index.html?abcd"));
            extractor.process();
            assertEquals("index",vars.get("regVal"));
        }

        @Test
        public void testVariableExtraction9() throws Exception {
            extractor.setRegex("(\\w+)");
            extractor.setTemplate("$1$");
            extractor.setMatchNumber(1);
            extractor.setUseField(RegexExtractor.USE_CODE);
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useBody should be false", extractor.useBody());
            assertFalse("useURL should be false", extractor.useUrl());
            assertFalse("useMessage should be false", extractor.useMessage());
            assertTrue("useCode should be true", extractor.useCode());
            extractor.process();
            assertEquals("abcd",vars.get("regVal"));
            extractor.setUseField(RegexExtractor.USE_MESSAGE);
            assertFalse("useHdrs should be false", extractor.useHeaders());
            assertFalse("useBody should be false", extractor.useBody());
            assertFalse("useURL should be false", extractor.useUrl());
            assertTrue("useMessage should be true", extractor.useMessage());
            assertFalse("useCode should be false", extractor.useCode());
            extractor.setMatchNumber(3);
            extractor.process();
            assertEquals("brown",vars.get("regVal"));
        }

        @Test
        public void testNoDefault() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(4);
            vars.put("regVal", "initial");
            assertEquals("initial", vars.get("regVal"));
            extractor.process();
            assertEquals("initial", vars.get("regVal"));
        }

        @Test
        public void testDefault() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(999);
            extractor.setDefaultValue("default");
            vars.put("regVal", "initial");
            assertEquals("initial", vars.get("regVal"));
            extractor.process();
            assertEquals("default", vars.get("regVal"));
            assertNull(vars.get("regVal_g0"));
            assertNull(vars.get("regVal_g1"));
        }

        @Test
        public void testStaleVariables() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(1);
            extractor.setDefaultValue("default");
            extractor.process();
            assertEquals("1", vars.get("regVal"));
            assertEquals("1", vars.get("regVal_g2"));
            assertEquals("2", vars.get("regVal_g"));
            assertNotNull(vars.get("regVal_g0"));
            assertNotNull(vars.get("regVal_g1"));
            // Now rerun with match fail
            extractor.setMatchNumber(10);
            extractor.process();
            assertEquals("default", vars.get("regVal"));
            assertNull(vars.get("regVal_g0"));
            assertNull(vars.get("regVal_g1"));
            assertNull(vars.get("regVal_g"));
        }

    @Test
    public void testScope1() throws Exception {
        result.setResponseData("<title>ONE</title>", "ISO-8859-1");
        extractor.setScopeParent();
        extractor.setTemplate("$1$");
        extractor.setMatchNumber(1);
        extractor.setRegex("<title>([^<]+)<");
        extractor.setDefaultValue("NOTFOUND");
        extractor.process();
        assertEquals("ONE", vars.get("regVal"));
        extractor.setScopeAll();
        extractor.process();
        assertEquals("ONE", vars.get("regVal"));
        extractor.setScopeChildren();
        extractor.process();
        assertEquals("NOTFOUND", vars.get("regVal"));
    }

    @Test
    public void testScope2() throws Exception {
        result.sampleStart();
        result.setResponseData("<title>PARENT</title>", "ISO-8859-1");
        result.sampleEnd();
        SampleResult child1 = new SampleResult();
        child1.sampleStart();
        child1.setResponseData("<title>ONE</title>", "ISO-8859-1");
        child1.sampleEnd();
        result.addSubResult(child1);
        SampleResult child2 = new SampleResult();
        child2.sampleStart();
        child2.setResponseData("<title>TWO</title>", "ISO-8859-1");
        child2.sampleEnd();
        result.addSubResult(child2);
        SampleResult child3 = new SampleResult();
        child3.sampleStart();
        child3.setResponseData("<title>THREE</title>", "ISO-8859-1");
        child3.sampleEnd();
        result.addSubResult(child3);
        extractor.setScopeParent();
        extractor.setTemplate("$1$");
        extractor.setMatchNumber(1);
        extractor.setRegex("<title>([^<]+)<");
        extractor.setDefaultValue("NOTFOUND");
        extractor.process();
        assertEquals("PARENT", vars.get("regVal"));
        extractor.setScopeAll();
        extractor.setMatchNumber(3);
        extractor.process();
        assertEquals("TWO", vars.get("regVal"));
        extractor.setScopeChildren();
        extractor.process();
        assertEquals("THREE", vars.get("regVal"));
        extractor.setRegex(">(...)<");
        extractor.setScopeAll();
        extractor.setMatchNumber(2);
        extractor.process();
        assertEquals("TWO", vars.get("regVal"));

        // Match all
        extractor.setRegex("<title>([^<]+)<");
        extractor.setMatchNumber(-1);

        extractor.setScopeParent();
        extractor.process();
        assertEquals("1", vars.get("regVal_matchNr"));
        extractor.setScopeAll();
        extractor.process();
        assertEquals("4", vars.get("regVal_matchNr"));
        extractor.setScopeChildren();
        extractor.process();
        assertEquals("3", vars.get("regVal_matchNr"));

        // Check random number
        extractor.setMatchNumber(0);
        extractor.setScopeParent();
        extractor.process();
        assertEquals("PARENT", vars.get("regVal"));
        extractor.setRegex("(<title>)");
        extractor.setScopeAll();
        extractor.process();
        assertEquals("<title>", vars.get("regVal"));
        extractor.setScopeChildren();
        extractor.process();
        assertEquals("<title>", vars.get("regVal"));
        extractor.setRegex("<title>(...)<");
        extractor.setScopeAll();
        extractor.process();
        final String found = vars.get("regVal");
        assertTrue(found.equals("ONE") || found.equals("TWO"));
    }

}
