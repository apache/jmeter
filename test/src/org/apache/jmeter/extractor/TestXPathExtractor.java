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


import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class TestXPathExtractor extends TestCase {
		XPathExtractor extractor;

		SampleResult result;

		JMeterVariables vars;

		public TestXPathExtractor(String name) {
			super(name);
		}

		private JMeterContext jmctx = null;

        private final static String VAL_NAME = "value";
        private final static String VAL_NAME_NR = "value_matchNr";
		public void setUp() {
			jmctx = JMeterContextService.getContext();
			extractor = new XPathExtractor();
			extractor.setThreadContext(jmctx);// This would be done by the run command
			extractor.setRefName(VAL_NAME);
            extractor.setDefaultValue("Default");
			result = new SampleResult();
			String data = "<book><preface title='Intro'>zero</preface><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
		}

		public void testAttributeExtraction() throws Exception {
			extractor.setXPathQuery("/book/preface/@title");
			extractor.process();
			assertEquals("Intro", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("Intro", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));

            extractor.setXPathQuery("/book/preface[@title]");
			extractor.process();
			assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));

            extractor.setXPathQuery("/book/preface[@title='Intro']");
			extractor.process();
			assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));

            extractor.setXPathQuery("/book/preface[@title='xyz']");
			extractor.process();
			assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
            assertNull(vars.get(VAL_NAME+"_1"));
		}
		
        public void testVariableExtraction() throws Exception {
			extractor.setXPathQuery("/book/preface");
			extractor.process();
			assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));
            
            extractor.setXPathQuery("/book/page");
            extractor.process();
            assertEquals("one", vars.get(VAL_NAME));
            assertEquals("2", vars.get(VAL_NAME_NR));
            assertEquals("one", vars.get(VAL_NAME+"_1"));
            assertEquals("two", vars.get(VAL_NAME+"_2"));
            assertNull(vars.get(VAL_NAME+"_3"));
            
            extractor.setXPathQuery("/book/page[2]");
            extractor.process();
            assertEquals("two", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("two", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));
            assertNull(vars.get(VAL_NAME+"_3"));

            extractor.setXPathQuery("/book/index");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
            assertNull(vars.get(VAL_NAME+"_1"));

            // Has child, but child is empty
            extractor.setXPathQuery("/book/a");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertNull(vars.get(VAL_NAME+"_1"));

            // Has no child
            extractor.setXPathQuery("/book/empty");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertNull(vars.get(VAL_NAME+"_1"));
        }

        public void testInvalidXpath() throws Exception {
            extractor.setXPathQuery("<");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
		}

        public void testInvalidDocument() throws Exception {
            result.setResponseData("<z>".getBytes());
            extractor.setXPathQuery("<");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
        }
}
