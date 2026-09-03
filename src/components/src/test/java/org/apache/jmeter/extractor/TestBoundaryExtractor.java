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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
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
        assertEquals("one", vars.get("varname"));
        assertNull(vars.get("varname_1"), "Indexed variable name should be null");
        assertNull(vars.get("varname_matchNr"), "MatchNumber is incorrect");
    }

    @Test
    public void testNoBoundaries() {
        vars.put("content", "one");
        extractor.setMatchNumber(-1);
        extractor.setRefName("varname");
        extractor.setScopeVariable("content");
        extractor.setThreadContext(jmctx);
        extractor.process();
        assertNull(vars.get("varname"), "Non indexed variable name should be null");
        assertEquals("one", vars.get("varname_1"), "First match is incorrect");
        assertEquals("1", vars.get("varname_matchNr"), "MatchNumber is incorrect");
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

        assertNull(vars.get("varname"), "Non indexed variable name should be null");
        assertEquals("ne", vars.get("varname_1"), "First match is incorrect");
        assertEquals("1", vars.get("varname_matchNr"), "MatchNumber is incorrect");
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
        assertNull(vars.get("varname"), "Non indexed variable name should be null");
        assertEquals("on", vars.get("varname_1"), "First match is incorrect");
        assertEquals("1", vars.get("varname_matchNr"), "MatchNumber is incorrect");
    }
}
