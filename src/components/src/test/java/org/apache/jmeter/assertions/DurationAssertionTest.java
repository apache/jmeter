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

package org.apache.jmeter.assertions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DurationAssertionTest extends JMeterTestCase {

    private DurationAssertion assertion;
    private SampleResult sampleResult;
    private AssertionResult result;

    @BeforeEach
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        assertion = new DurationAssertion();
        assertion.setThreadContext(jmctx);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
        sampleResult.setResponseData("response Data", null);
    }

    @Test
    public void testDurationLower() throws Exception {
        sampleResult.setStampAndTime(0, 1000);
        assertion.setAllowedDuration(1100L);
        result = assertion.getResult(sampleResult);
        assertFalse(result.isError());
        assertFalse(result.isFailure());
        assertNull(result.getFailureMessage());
    }

    @Test
    public void testDurationEquals() throws Exception {
        sampleResult.setStampAndTime(0, 1000);
        assertion.setAllowedDuration(1000L);
        result = assertion.getResult(sampleResult);
        assertFalse(result.isError());
        assertFalse(result.isFailure());
        assertNull(result.getFailureMessage());
    }

    @Test
    public void testDurationHigher() throws Exception {
        sampleResult.setStampAndTime(0, 1200);
        assertion.setAllowedDuration(1100);
        result = assertion.getResult(sampleResult);
        assertFalse(result.isError());
        assertTrue(result.isFailure());
        assertNotNull(result.getFailureMessage());
    }

    @Test
    public void testDurationZero() throws Exception {
        sampleResult.setStampAndTime(0, 0);
        assertion.setAllowedDuration(1100);
        result = assertion.getResult(sampleResult);
        assertFalse(result.isFailure());
        assertFalse(result.isError());
        assertNull(result.getFailureMessage());
    }
}
