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

import static org.junit.Assert.assertThat;

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
