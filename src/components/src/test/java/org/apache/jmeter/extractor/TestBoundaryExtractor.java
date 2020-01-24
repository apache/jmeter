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

package org.apache.jmeter.extractor;


import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestBoundaryExtractor {

    private BoundaryExtractor extractor;

    private SampleResult result;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @BeforeEach
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        extractor = new BoundaryExtractor();
        extractor.setThreadContext(jmctx);
        extractor.setRefName("regVal");
        result = new SampleResult();
        String data = "<company-xmlext-query-ret><row></row></company-xmlext-query-ret>";
        result.setResponseData(data, null);
        result.setResponseHeaders("Header1: Value1\nHeader2: Value2");
        result.setResponseCode("abcd");
        result.setResponseMessage("The quick brown fox");
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void testNoBoundariesOneVariable() {
        vars.put("content", "one");
        extractor.setMatchNumber(1);
        extractor.setRefName("varname");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertThat(vars.get("varname"), CoreMatchers.is("one"));
        assertThat("Indexed variable name should be null", vars.get("varname_1"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat("MatchNumber is incorrect", vars.get("varname_matchNr"), CoreMatchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void testNoBoundaries() {
        vars.put("content", "one");
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertThat("Non indexed variable name should be null", vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat("First match is incorrect", vars.get("varname_1"), CoreMatchers.is("one"));
        assertThat("MatchNumber is incorrect", vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }

    @Test
    public void testOnlyLeftBoundary() {
        vars.put("content", "one");
        extractor.setLeftBoundary("o");
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();

        assertThat("Non indexed variable name should be null", vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat("First match is incorrect", vars.get("varname_1"), CoreMatchers.is("ne"));
        assertThat("MatchNumber is incorrect", vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }

    @Test
    public void testOnlyRightBoundary() {
        vars.put("content", "one");
        extractor.setRightBoundary("e");
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertThat("Non indexed variable name should be null", vars.get("varname"), CoreMatchers.is(CoreMatchers.nullValue()));
        assertThat("First match is incorrect", vars.get("varname_1"), CoreMatchers.is("on"));
        assertThat("MatchNumber is incorrect", vars.get("varname_matchNr"), CoreMatchers.is("1"));
    }
}
