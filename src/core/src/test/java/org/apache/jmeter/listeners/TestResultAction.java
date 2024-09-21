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

package org.apache.jmeter.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestResultAction extends JMeterTestCase {
    private ResultAction resultAction;
    private SampleResult sampleResult;
    private final String data = "response Data";

    @BeforeEach
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        resultAction = new ResultAction();
        resultAction.setThreadContext(jmctx);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
        sampleResult.setResponseData(data, null);
    }

    @Test
    public void testSuccess() {
        sampleResult.setSuccessful(true);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTEST);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        assertFalse(sampleResult.isStopTest());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testOnFailureStopTest() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTEST);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        assertTrue(sampleResult.isStopTest());
        assertFalse(sampleResult.isStopTestNow());
        assertFalse(sampleResult.isStopThread());
        assertFalse(sampleResult.isStartNextThreadLoop());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testOnFailureStopTestNow() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTEST_NOW);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        assertFalse(sampleResult.isStopTest());
        assertTrue(sampleResult.isStopTestNow());
        assertFalse(sampleResult.isStopThread());
        assertFalse(sampleResult.isStartNextThreadLoop());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testOnFailureStopThread() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTHREAD);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        assertFalse(sampleResult.isStopTest());
        assertFalse(sampleResult.isStopTestNow());
        assertTrue(sampleResult.isStopThread());
        assertFalse(sampleResult.isStartNextThreadLoop());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testOnFailureStartNextThreadLoop() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_START_NEXT_THREAD_LOOP);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        assertFalse(sampleResult.isStopTest());
        assertFalse(sampleResult.isStopTestNow());
        assertFalse(sampleResult.isStopThread());
        assertTrue(sampleResult.isStartNextThreadLoop());
    }
}
