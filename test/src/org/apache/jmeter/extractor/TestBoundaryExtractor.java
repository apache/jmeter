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
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class TestBoundaryExtractor {

    private static final String VAR_NAME = "varName";
    private BoundaryExtractor extractor;
    
    private SampleResult result;

    private JMeterVariables vars;

    private JMeterContext context;

    @Before
    public void setUp() {
        context = JMeterContextService.getContext();
        extractor = new BoundaryExtractor();
        extractor.setThreadContext(context);// This would be done by the run
                                            // command
        extractor.setRefName("regVal");
        result = new SampleResult();
        String data = "zazzd azd azd azd <t>value</t>azdazd azd azd";
        result.setResponseData(data, null);
        result.setResponseHeaders("Header1: Value1\nHeader2: Value2");
        result.setResponseCode("abcd");
        result.setResponseMessage("The quick brown fox");
        vars = new JMeterVariables();
        context.setVariables(vars);
        context.setPreviousResult(result);
    }
    
    @Test
    public void testMatchOnLeftOnly() throws Exception {
        result.setResponseData("zazzd azd azd azd <t>value</t>azdazd <t>value2</t>azd azd", null);
        extractor.setLeftBoundary("<t>");
        extractor.setRightBoundary("</t1>");
        extractor.setMatchNumber(0);
        extractor.setDefaultValue("default");
        extractor.process();
        assertEquals("default", vars.get("regVal"));
    }

    
    @Test
    public void testStaleVariables() throws Exception {
        result.setResponseData("zazzd azd azd azd <t>value</t>azdazd <t>value2</t>azd azd", null);
        extractor.setLeftBoundary("<t>");
        extractor.setRightBoundary("</t>");
        extractor.setMatchNumber(-1);
        extractor.setDefaultValue("default");
        extractor.process();
        assertEquals("value", vars.get("regVal_1"));
        assertEquals("value2", vars.get("regVal_2"));
        assertEquals("2", vars.get("regVal_matchNr"));
        // Now rerun with match fail
        extractor.setMatchNumber(10);
        extractor.process();
        assertEquals("default", vars.get("regVal"));
        assertNull(vars.get("regVal_1"));
        assertNull(vars.get("regVal_2"));
        assertNull(vars.get("regVal_matchNr"));
    }

    @Test
    public void testScope1() throws Exception {
        result.setResponseData("<title>ONE</title>", "ISO-8859-1");
        extractor.setScopeParent();
        extractor.setMatchNumber(1);
        extractor.setLeftBoundary("<title>");
        extractor.setRightBoundary("</title>");
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
        extractor.setMatchNumber(1);
        extractor.setLeftBoundary("<title>");
        extractor.setRightBoundary("</title>");
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
       

        // Match all
        extractor.setLeftBoundary("<title>");
        extractor.setRightBoundary("</title>");
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
        extractor.setLeftBoundary("<title>");
        extractor.setRightBoundary("</title>");
        extractor.setScopeChildren();
        extractor.process();
        final String found = vars.get("regVal");
        assertTrue(found.equals("ONE") || found.equals("TWO")
                || found.equals("THREE"));
    }
    
    @Test
    public void testEmptyDefaultVariable() throws Exception {
        extractor.setLeftBoundary("<t1>");
        extractor.setRightBoundary("</t1>");
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(true);
        extractor.process();
        assertEquals("", vars.get("regVal"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalArgumentException() throws Exception {
        extractor.setLeftBoundary(null);
        extractor.setRightBoundary(null);
        extractor.setRefName(null);
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(true);
        extractor.process();
        assertEquals("", vars.get("regVal"));
    }
    
    @Test
    public void testNoProcessing() throws Exception {
        extractor.setLeftBoundary("<t>");
        extractor.setRightBoundary("</t>");
        extractor.setMatchNumber(1);
        context.setPreviousResult(null);
        extractor.setDefaultEmptyValue(true);
        extractor.process();
        assertNull(vars.get("regVal"));
    }

    @Test
    public void testEmptyVariable() throws Exception {
        extractor.setLeftBoundary("<t>");
        extractor.setRightBoundary("</t>");
        extractor.setMatchNumber(1);
        extractor.setScopeVariable("contentvar");
        extractor.process();
        assertNull(vars.get("regVal"));
    }
    
    @Test
    public void testNotEmptyDefaultVariable() throws Exception {
        extractor.setLeftBoundary("<t1>");
        extractor.setRightBoundary("</t1>");
        extractor.setMatchNumber(1);
        extractor.setDefaultEmptyValue(false);
        extractor.process();
        assertNull(vars.get("regVal"));
    }
    
    @Test
    public void testProcessAllElementsOneMatch() {
        BoundaryExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValue("NONE");
        processor.setLeftBoundary("<t>");
        processor.setRightBoundary("</t>");
        processor.setRefName("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "zazzd azd azd azd <t>one</t>azdazd azd azd");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is("NONE"));
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }

    @Test
    public void testProcessAllElementsMultipleMatches() {
        BoundaryExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValue("NONE");
        processor.setLeftBoundary("<t>");
        processor.setRightBoundary("</t>");
        processor.setRefName("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "zazzd azd azd azd \r\n<t>one</t>azdazd \r\nazd <t>two</t>azd");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
    }

    @Test
    public void testProcessRandomElementMultipleMatches() {
        BoundaryExtractor processor = setupProcessor(context, "0");
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValue("NONE");
        processor.setLeftBoundary("<t>");
        processor.setRightBoundary("</t>");
        processor.setRefName("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "zazzd azd azd azd \r\n<t>one</t>azdazd \r\nazd <t>two</t>azd");
        processor.process();
        assertThat(vars.get("varname"), 
                CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("one"), CoreMatchers.is("two"))));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testCaseEmptyResponse() {
        BoundaryExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValue("NONE");
        processor.setLeftBoundary("<t>");
        processor.setRightBoundary("</t>");
        processor.setRefName("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "zazzd azd azd azd \r\n<t>one</t>azdazd \r\nazd <t>two</t>azd");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
        vars.put("contentvar", "");
        processor.process();
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("0"));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testCaseMatchOneWithZero() {
        BoundaryExtractor processor = setupProcessor(context, "-1");
        JMeterVariables vars = new JMeterVariables();
        processor.setDefaultValue("NONE");
        processor.setLeftBoundary("<t>");
        processor.setRightBoundary("</t>");
        processor.setRefName("varname");
        processor.setScopeVariable("contentvar");
        context.setVariables(vars);
        vars.put("contentvar", "zazzd azd azd azd \r\n<t>one</t>azdazd \r\nazd <t>two</t>azd");
        processor.process();
        assertThat(vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat(vars.get("varname_2"), CoreMatchers.is("two"));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is("2"));
        vars.put("contentvar", "zaddazddad bvefvdv azd azvddfvfvd \r\n<t>A</t>azdazd \r\nfvdfv <t>B</t>azd");
        processor.setMatchNumber("0");
        processor.process();
        assertThat(vars.get("varname"), CoreMatchers.is(CoreMatchers.anyOf(CoreMatchers.is("A"), CoreMatchers.is("B"))));
        assertThat(vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat(vars.get("varname_2"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    private BoundaryExtractor setupProcessor(JMeterContext context,
            String matchNumber) {
        BoundaryExtractor processor = new BoundaryExtractor();
        processor.setThreadContext(context);
        processor.setRefName(VAR_NAME);
        processor.setMatchNumber(matchNumber);
        return processor;
    }

}
