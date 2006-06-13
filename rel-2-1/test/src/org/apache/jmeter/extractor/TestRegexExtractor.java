/*
 * Copyright 2003-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


import java.net.URL;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @version $Revision$
 */
public class TestRegexExtractor extends TestCase {
		RegexExtractor extractor;

		SampleResult result;

		JMeterVariables vars;

		public TestRegexExtractor(String name) {
			super(name);
		}

		private JMeterContext jmctx = null;

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
			result.setResponseData(data.getBytes());
			result.setResponseHeaders("Header1: Value1\nHeader2: Value2");
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
		}

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

		static void templateSetup(RegexExtractor rex, String tmp) {
			rex.setRegex("<company-(\\w+?)-(\\w+?)-(\\w+?)>");
			rex.setMatchNumber(1);
			rex.setTemplate(tmp);
			rex.process();
		}

		public void testTemplate1() throws Exception {
			templateSetup(extractor, "");
			assertEquals("<company-xmlext-query-ret>", vars.get("regVal_g0"));
			assertEquals("xmlext", vars.get("regVal_g1"));
			assertEquals("query", vars.get("regVal_g2"));
			assertEquals("ret", vars.get("regVal_g3"));
			assertEquals("", vars.get("regVal"));
            assertEquals("3",vars.get("regVal_g"));
		}

		public void testTemplate2() throws Exception {
			templateSetup(extractor, "ABC");
			assertEquals("ABC", vars.get("regVal"));
		}

		public void testTemplate3() throws Exception {
			templateSetup(extractor, "$2$");
			assertEquals("query", vars.get("regVal"));
		}

		public void testTemplate4() throws Exception {
			templateSetup(extractor, "PRE$2$");
			assertEquals("PREquery", vars.get("regVal"));
		}

		public void testTemplate5() throws Exception {
			templateSetup(extractor, "$2$POST");
			assertEquals("queryPOST", vars.get("regVal"));
		}

		public void testTemplate6() throws Exception {
			templateSetup(extractor, "$2$$1$");
			assertEquals("queryxmlext", vars.get("regVal"));
		}

		public void testTemplate7() throws Exception {
			templateSetup(extractor, "$2$MID$1$");
			assertEquals("queryMIDxmlext", vars.get("regVal"));
		}

		public void testVariableExtraction2() throws Exception {
			extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
			extractor.setTemplate("$1$");
			extractor.setMatchNumber(3);
			extractor.process();
			assertEquals("pinposition3", vars.get("regVal"));
		}

		public void testVariableExtraction6() throws Exception {
			extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
			extractor.setTemplate("$2$");
			extractor.setMatchNumber(4);
			extractor.setDefaultValue("default");
			extractor.process();
			assertEquals("default", vars.get("regVal"));
		}

		public void testVariableExtraction3() throws Exception {
			extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
			extractor.setTemplate("_$1$");
			extractor.setMatchNumber(2);
			extractor.process();
			assertEquals("_pinposition2", vars.get("regVal"));
		}

		public void testVariableExtraction5() throws Exception {
			extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
			extractor.setTemplate("$1$");
			extractor.setMatchNumber(-1);
			extractor.process();
			assertEquals("3", vars.get("regVal_matchNr"));
			assertEquals("pinposition1", vars.get("regVal_1"));
			assertEquals("pinposition2", vars.get("regVal_2"));
			assertEquals("pinposition3", vars.get("regVal_3"));
			assertEquals("pinposition1", vars.get("regVal_1_g1"));
			assertEquals("1", vars.get("regVal_1_g2"));
			assertEquals("<value field=\"pinposition1\">1</value>", vars.get("regVal_1_g0"));
			assertNull(vars.get("regVal_4"));

			// Check old values don't hang around:
			extractor.setRegex("(\\w+)count"); // fewer matches
			extractor.process();
			assertEquals("2", vars.get("regVal_matchNr"));
			assertEquals("position", vars.get("regVal_1"));
			assertEquals("invalidpin", vars.get("regVal_2"));
			assertNull("Unused variables should be null", vars.get("regVal_3"));
			assertNull("Unused variables should be null", vars.get("regVal_3_g0"));
			assertNull("Unused variables should be null", vars.get("regVal_3_g1"));
		}

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

        public void testNoDefault() throws Exception {
            extractor.setRegex("<value field=\"(pinposition\\d+)\">(\\d+)</value>");
            extractor.setTemplate("$2$");
            extractor.setMatchNumber(4);
            //extractor.setDefaultValue("default");
            vars.put("regVal", "initial");
            assertEquals("initial", vars.get("regVal"));
            extractor.process();
            assertEquals("initial", vars.get("regVal"));
        }

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

}
