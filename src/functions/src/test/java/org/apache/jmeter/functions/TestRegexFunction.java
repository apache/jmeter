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

package org.apache.jmeter.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestRegexFunction extends JMeterTestCase {
        private static final String INPUT_VARIABLE_NAME = "INVAR";

        private RegexFunction variable;

        private SampleResult result;

        private Collection<CompoundVariable> params;

        private JMeterVariables vars;

        private JMeterContext jmctx;

        @Before
        public void setUp() {
            variable = new RegexFunction();
            result = new SampleResult();
            jmctx = JMeterContextService.getContext();
            String data = "<company-xmlext-query-ret><row>" + "<value field=\"RetCode\">" + "LIS_OK</value><value"
                    + " field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value><value"
                    + " field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value><value"
                    + " field=\"pinposition1\">1</value><value" + " field=\"pinpositionvalue1\"></value><value"
                    + " field=\"pinposition2\">5</value><value" + " field=\"pinpositionvalue2\"></value><value"
                    + " field=\"pinposition3\">6</value><value" + " field=\"pinpositionvalue3\"></value>"
                    + "</row></company-xmlext-query-ret>";
            result.setResponseData(data, null);
            vars = new JMeterVariables();
            String data2 = "The quick brown fox jumped over the lazy dog 123 times";
            vars.put(INPUT_VARIABLE_NAME, data2);
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
        }

        @Test
        public void testVariableExtraction() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$"));
            params.add(new CompoundVariable("2"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("5", match);
        }

        // Test with output variable name
        @Test
        public void testVariableExtraction1a() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$")); // template
            params.add(new CompoundVariable("2")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("3", vars.getObject("OUTVAR_matchNr"));
            assertEquals("5", match);
            assertEquals("5", vars.getObject("OUTVAR"));
            assertEquals("<value field=\"pinposition2\">5</value>", vars.getObject("OUTVAR_g0"));
            assertEquals("pinposition2", vars.getObject("OUTVAR_g1"));
            assertEquals("5", vars.getObject("OUTVAR_g2"));
        }

        // Test with empty output variable name
        @Test
        public void testVariableExtraction1b() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$")); // template
            params.add(new CompoundVariable("2")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable(""));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("5", match);
            assertNull(vars.getObject("OUTVAR"));
        }

        @Test
        public void testVariableExtractionFromVariable() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("$2$")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("times", match);
            assertEquals("times", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable2() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("$1$$2$")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("123times", match);
            assertEquals("123times", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable3() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("pre$2$post")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("pretimespost", match);
            assertEquals("pretimespost", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable4() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("pre$2$")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("pretimes", match);
            assertEquals("pretimes", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable5() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("$2$post")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("timespost", match);
            assertEquals("timespost", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable6() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("$2$$2$")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("timestimes", match);
            assertEquals("timestimes", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable7() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("pre$1$mid$2$post")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("pre123midtimespost", match);
            assertEquals("pre123midtimespost", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable8() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("pre$1$mid$2$")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("pre123midtimes", match);
            assertEquals("pre123midtimes", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtractionFromVariable9() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("(\\d+)\\s+(\\w+)"));
            params.add(new CompoundVariable("$1$mid$2$post")); // template
            params.add(new CompoundVariable("1")); // match number
            params.add(new CompoundVariable("-")); // ALL separator
            params.add(new CompoundVariable("default"));
            params.add(new CompoundVariable("OUTVAR"));
            params.add(new CompoundVariable(INPUT_VARIABLE_NAME));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1", vars.getObject("OUTVAR_matchNr"));
            assertEquals("123midtimespost", match);
            assertEquals("123midtimespost", vars.getObject("OUTVAR"));
            assertEquals("123 times", vars.getObject("OUTVAR_g0"));
            assertEquals("123", vars.getObject("OUTVAR_g1"));
            assertEquals("times", vars.getObject("OUTVAR_g2"));
        }

        @Test
        public void testVariableExtraction2() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("3"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition3", match);
        }

        @Test
        public void testVariableExtraction5() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("ALL"));
            params.add(new CompoundVariable("_"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition1_pinposition2_pinposition3", match);
        }

        @Test
        public void testVariableExtraction6() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$"));
            params.add(new CompoundVariable("4"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("default"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("default", match);
        }

        @Test
        public void testComma() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value,? field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("3"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("pinposition3", match);
        }

        @Test
        public void testVariableExtraction3() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("_$1$"));
            params.add(new CompoundVariable("2"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("_pinposition2", match);
        }

        @Test
        public void testExtractionIndexTooHigh() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("_$1$"));
            params.add(new CompoundVariable("10"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("No Value Found"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("No Value Found", match);
        }

        @Test
        public void testRandomExtraction() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<company-xmlext-query-ret>(.+?)</company-xmlext-query-ret>"));
            params.add(new CompoundVariable("$1$"));
            params.add(new CompoundVariable("RAND"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("No Value Found"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("<row>" + "<value field=\"RetCode\">" + "LIS_OK</value><value"
                    + " field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value><value"
                    + " field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value><value"
                    + " field=\"pinposition1\">1</value><value" + " field=\"pinpositionvalue1\"></value><value"
                    + " field=\"pinposition2\">5</value><value" + " field=\"pinpositionvalue2\"></value><value"
                    + " field=\"pinposition3\">6</value><value" + " field=\"pinpositionvalue3\"></value>"
                    + "</row>", match);
        }


        @Test(expected=NumberFormatException.class)
        public void testExtractionIndexNotNumeric() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("_$1$"));
            params.add(new CompoundVariable("0.333a"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("No Value Found"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("No Value Found", match);
        }

        @Test
        public void testVariableExtraction4() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$, "));
            params.add(new CompoundVariable(".333"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("1, ", match);
        }

        @Test
        public void testDefaultValue() throws Exception {
            params = new LinkedList<>();
            params.add(new CompoundVariable("<value,, field=\"(pinposition\\d+)\">(\\d+)</value>"));
            params.add(new CompoundVariable("$2$, "));
            params.add(new CompoundVariable(".333"));
            params.add(new CompoundVariable(""));
            params.add(new CompoundVariable("No Value Found"));
            variable.setParameters(params);
            String match = variable.execute(result, null);
            assertEquals("No Value Found", match);
        }
}
