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
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestXPathExtractor {
        private XPathExtractor extractor;

        private SampleResult result;

        private String data;
        
        private JMeterVariables vars;


        private JMeterContext jmctx;

        private static final String VAL_NAME = "value";
        private static final String VAL_NAME_NR = "value_matchNr";
        
        @Before
        public void setUp() throws UnsupportedEncodingException {
            jmctx = JMeterContextService.getContext();
            extractor = new XPathExtractor();
            extractor.setThreadContext(jmctx);// This would be done by the run command
            extractor.setRefName(VAL_NAME);
            extractor.setDefaultValue("Default");
            result = new SampleResult();
            data = "<book><preface title='Intro'>zero</preface><page>one</page><page>two</page><empty></empty><a><b></b></a></book>";
            result.setResponseData(data.getBytes("UTF-8"));
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
        }

        @Test
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
        
        @Test
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
            
            // Test match 1
            extractor.setXPathQuery("/book/page");
            extractor.setMatchNumber(1);
            extractor.process();
            assertEquals("one", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("one", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));
            assertNull(vars.get(VAL_NAME+"_3"));
            
            // Test match Random
            extractor.setXPathQuery("/book/page");
            extractor.setMatchNumber(0);
            extractor.process();
            assertEquals("1", vars.get(VAL_NAME_NR));
            Assert.assertTrue(StringUtils.isNoneEmpty(vars.get(VAL_NAME)));
            Assert.assertTrue(StringUtils.isNoneEmpty(vars.get(VAL_NAME+"_1")));
            assertNull(vars.get(VAL_NAME+"_2"));
            assertNull(vars.get(VAL_NAME+"_3"));
            
            // Put back default value
            extractor.setMatchNumber(-1);
            
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

            // No text
            extractor.setXPathQuery("//a");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));

            // No text all matches
            extractor.setXPathQuery("//a");
            extractor.process();
            extractor.setMatchNumber(-1);
            assertEquals("Default", vars.get(VAL_NAME));

            // No text match second
            extractor.setXPathQuery("//a");
            extractor.process();
            extractor.setMatchNumber(2);
            assertEquals("Default", vars.get(VAL_NAME));

            // No text match random
            extractor.setXPathQuery("//a");
            extractor.process();
            extractor.setMatchNumber(0);
            assertEquals("Default", vars.get(VAL_NAME));

            extractor.setMatchNumber(-1);
            // Test fragment
            extractor.setXPathQuery("/book/page[2]");
            extractor.setFragment(true);
            extractor.process();
            assertEquals("<page>two</page>", vars.get(VAL_NAME));
            // Now get its text
            extractor.setXPathQuery("/book/page[2]/text()");
            extractor.process();
            assertEquals("two", vars.get(VAL_NAME));

            // No text, but using fragment mode
            extractor.setXPathQuery("//a");
            extractor.process();
            assertEquals("<a><b/></a>", vars.get(VAL_NAME));
        }

        @Test
        public void testScope(){
            extractor.setXPathQuery("/book/preface");
            extractor.process();
            assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));            

            extractor.setScopeChildren(); // There aren't any
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
            assertNull(vars.get(VAL_NAME+"_1"));

            extractor.setScopeAll(); // same as Parent
            extractor.process();
            assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));            

            // Try to get data from subresult
            result.sampleStart(); // Needed for addSubResult()
            result.sampleEnd();
            SampleResult subResult = new SampleResult();
            subResult.sampleStart();
            subResult.setResponseData(result.getResponseData());
            subResult.sampleEnd();
            result.addSubResult(subResult);
            
            
            // Get data from both
            extractor.setScopeAll();
            extractor.process();
            assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("2", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertEquals("zero", vars.get(VAL_NAME+"_2"));
            assertNull(vars.get(VAL_NAME+"_3"));

            // get data from child
            extractor.setScopeChildren();
            extractor.process();
            assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));
            
            
            // get data from child
            extractor.setScopeVariable("result");
            result = new SampleResult();
            vars.put("result", data);
            extractor.process();
            assertEquals("zero", vars.get(VAL_NAME));
            assertEquals("1", vars.get(VAL_NAME_NR));
            assertEquals("zero", vars.get(VAL_NAME+"_1"));
            assertNull(vars.get(VAL_NAME+"_2"));
            
            // get data from child
            extractor.setScopeVariable("result");
            result = new SampleResult();
            vars.remove("result");
            extractor.process();
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));            
        }

        @Test
        public void testInvalidXpath() throws Exception {
            extractor.setXPathQuery("<");
            extractor.process();
            assertEquals(1, result.getAssertionResults().length);
            assertEquals(extractor.getName(), result.getAssertionResults()[0].getName());
            org.junit.Assert.assertTrue(result.getAssertionResults()[0].
                    getFailureMessage().contains("A location path was expected, but the following token was encountered"));
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
        }

        @Test
        public void testNonXmlDocument() throws Exception {
            result.setResponseData("Error:exception occurred", null);
            extractor.setXPathQuery("//test");
            extractor.process();
            assertEquals(1, result.getAssertionResults().length);
            assertEquals(extractor.getName(), result.getAssertionResults()[0].getName());
            org.junit.Assert.assertTrue(result.getAssertionResults()[0].
                    getFailureMessage().contains("Content is not allowed in prolog"));
            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
        }
        @Test
        public void testInvalidDocument() throws Exception {
            result.setResponseData("<z>", null);
            extractor.setXPathQuery("//test");
            extractor.process();
            
            assertEquals(1, result.getAssertionResults().length);
            assertEquals(extractor.getName(), result.getAssertionResults()[0].getName());
            org.junit.Assert.assertThat(result.getAssertionResults()[0].
                    getFailureMessage(), CoreMatchers.containsString("XML document structures must start and end within the same entity"));

            assertEquals("Default", vars.get(VAL_NAME));
            assertEquals("0", vars.get(VAL_NAME_NR));
        }
}
