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

package org.apache.jmeter.listeners;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test for {@link ResultAction}
 */
public class TestResultAction extends JMeterTestCase {
    private ResultAction resultAction;
    private SampleResult sampleResult;
    private final String data = "response Data";

    @Before
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
        Assert.assertFalse(sampleResult.isStopTest());
    }
    
    @Test
    public void testOnFailureStopTest() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTEST);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        Assert.assertTrue(sampleResult.isStopTest());
        Assert.assertFalse(sampleResult.isStopTestNow());
        Assert.assertFalse(sampleResult.isStopThread());
        Assert.assertFalse(sampleResult.isStartNextThreadLoop());
    }
    
    @Test
    public void testOnFailureStopTestNow() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTEST_NOW);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        Assert.assertFalse(sampleResult.isStopTest());
        Assert.assertTrue(sampleResult.isStopTestNow());
        Assert.assertFalse(sampleResult.isStopThread());
        Assert.assertFalse(sampleResult.isStartNextThreadLoop());
    }
    
    @Test
    public void testOnFailureStopThread() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_STOPTHREAD);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        Assert.assertFalse(sampleResult.isStopTest());
        Assert.assertFalse(sampleResult.isStopTestNow());
        Assert.assertTrue(sampleResult.isStopThread());
        Assert.assertFalse(sampleResult.isStartNextThreadLoop());
    }

    @Test
    public void testOnFailureStartNextThreadLoop() {
        sampleResult.setSuccessful(false);
        resultAction.setErrorAction(ResultAction.ON_ERROR_START_NEXT_THREAD_LOOP);
        resultAction.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        Assert.assertFalse(sampleResult.isStopTest());
        Assert.assertFalse(sampleResult.isStopTestNow());
        Assert.assertFalse(sampleResult.isStopThread());
        Assert.assertTrue(sampleResult.isStartNextThreadLoop());
    }
}
