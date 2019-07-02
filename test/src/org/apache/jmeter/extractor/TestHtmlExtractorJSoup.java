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
import static org.junit.Assert.assertThat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class TestHtmlExtractorJSoup {

    protected HtmlExtractor extractor;

    protected SampleResult result;

    protected JMeterVariables vars;

    protected JMeterContext jmctx;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        extractor = new HtmlExtractor();
        extractor.setThreadContext(jmctx);// This would be done by the run
                                          // command
        extractor.setRefName("regVal");
        result = new SampleResult();
        String data = "<p>An <a href='http://example.com/'><b>example1</b></a> link.</p>"+
                "<p>A second <a class='myclass' href='http://example2.com/'><b>example2</b></a> link.</p>"+
                "<p class='single'>Single</p>";
        result.setResponseData(data, null);
        result.setResponseCode("200");
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void testProcessAllElementsSingleMatch() {
        vars.put("content", "<p><a href='http://jmeter.apache.org/'>Link1</a>"
                + "<a class='mylink' href='http://jmeter.apache.org/'>Link2</a></p>"
                );
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setExpression("a.mylink");
        extractor.setAttribute("href");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is("http://jmeter.apache.org/"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }

    @Test
    public void testProcessAllElementsMultipleMatches() {
        vars.put("content", "<p><a href='http://www.apache.org/'>Link1</a>"
                + "<a class='mylink' href='http://jmeter.apache.org/'>Link2</a></p>"
                );
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setExpression("a");
        extractor.setAttribute("href");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is("http://www.apache.org/"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("http://jmeter.apache.org/"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
    }

    @Test
    public void testEmptyDefaultVariable() throws Exception {
        extractor.setExpression("p.missing");
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(true);
        extractor.process();
        assertEquals("", vars.get("regVal"));
    }

    @Test
    public void testNotEmptyDefaultVariable() throws Exception {
        extractor.setExpression("p.missing");
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(false);
        extractor.process();
        assertNull(vars.get("regVal"));
    }

    @Test
    public void testNotEmptyDefaultValue() throws Exception {
        extractor.setExpression("p.missing");
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(false);
        extractor.setDefaultValue("nv_value");
        extractor.process();
        assertEquals("nv_value", vars.get("regVal"));
    }

    @Test
    public void testVariableExtraction0() throws Exception {
        extractor.setExpression("p.single");
        extractor.setMatchNumber(0);
        extractor.process();
        assertEquals("Single", vars.get("regVal"));
    }

    @Test
    public void testVariableExtraction2() throws Exception {
        extractor.setExpression("a");
        extractor.setMatchNumber(2);
        extractor.process();
        assertEquals("example2", vars.get("regVal"));
    }

    @Test
    public void testVariableExtractionWithAttribute2() throws Exception {
        extractor.setExpression("a");
        extractor.setAttribute("href");
        extractor.setMatchNumber(2);
        extractor.process();
        assertEquals("http://example2.com/", vars.get("regVal"));
    }

    @Test
    public void testMultipleVariableExtraction() throws Exception {
        extractor.setExpression("a");
        extractor.setAttribute("href");
        extractor.setMatchNumber(-1);
        extractor.process();
        assertThat(vars.get("regVal_matchNr"), CoreMatchers.is("2"));
        assertEquals("http://example.com/", vars.get("regVal_1"));
        assertEquals("http://example2.com/", vars.get("regVal_2"));
    }

    @Test
    public void testMultipleVariableExtractionWithAttribute() throws Exception {
        extractor.setExpression("b");
        extractor.setMatchNumber(-1);
        extractor.process();
        assertThat(vars.get("regVal_matchNr"), CoreMatchers.is("2"));
        assertEquals("example1", vars.get("regVal_1"));
        assertEquals("example2", vars.get("regVal_2"));
    }

    @Test
    public void testMultipleVariableExtractionNoMatch() throws Exception {
        extractor.setExpression("c");
        extractor.setMatchNumber(-1);
        extractor.process();
        assertThat(vars.get("regVal_matchNr"), CoreMatchers.is("0"));
        assertNull(vars.get("regVal"));
        assertNull(vars.get("regVal_1"));
    }

    @Test
    public void testPreviousVarsAreCleanedUp() throws Exception {
        testMultipleVariableExtractionWithAttribute();
        testMultipleVariableExtractionNoMatch();
        assertNull(vars.get("regVal_2"));
    }

    @Test
    public void testUnknownExtractor() throws Exception {
        extractor.setExtractor("UNKNOWN");
        extractor.setExpression("c");
        extractor.setMatchNumber(-1);
        extractor.process();
        assertNull(vars.get("regVal_matchNr"));
    }

    @Test
    public void testNoPrevious() throws Exception {
        jmctx.setPreviousResult(null);
        extractor.setExpression("b");
        extractor.setMatchNumber(-1);
        extractor.process();
        assertNull(vars.get("regVal_matchNr"));
    }
}
