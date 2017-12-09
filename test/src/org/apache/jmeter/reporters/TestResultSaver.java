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

package org.apache.jmeter.reporters;

import java.io.File;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ResultSaver}
 */
public class TestResultSaver extends JMeterTestCase implements JMeterSerialTest {
    private ResultSaver resultSaver;
    private SampleResult sampleResult;
    private final String data = "response Data";
    private final JMeterVariables vars = new JMeterVariables();

    @Before
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        resultSaver = new ResultSaver();
        resultSaver.setThreadContext(jmctx);
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
        sampleResult.setResponseData(data, null);
    }
    
    @Test
    public void testSuccess() {
        sampleResult.setSuccessful(true);
        resultSaver.setProperty(ResultSaver.NUMBER_PAD_LENGTH, "5");
        resultSaver.testStarted();
        resultSaver.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        String fileName = sampleResult.getResultFileName();
        Assert.assertNotNull(fileName);
        Assert.assertEquals("00001.unknown", fileName);
        File file = new File(FileServer.getDefaultBase(), fileName);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
    }
    
    @Test
    public void testSuccessWithVariable() {
        sampleResult.setSuccessful(true);
        resultSaver.setProperty(ResultSaver.NUMBER_PAD_LENGTH, "5");
        resultSaver.setProperty(ResultSaver.VARIABLE_NAME,"myVar");
        resultSaver.testStarted();
        resultSaver.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        String fileName = sampleResult.getResultFileName();
        Assert.assertNotNull(fileName);
        Assert.assertEquals("00001.unknown", fileName);
        File file = new File(FileServer.getDefaultBase(), fileName);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
        Assert.assertEquals("00001.unknown", vars.get("myVar"));
    }
    
    @Test
    public void testSuccessSaveErrorsOnly() {
        sampleResult.setSuccessful(true);
        resultSaver.setProperty(ResultSaver.NUMBER_PAD_LENGTH, "5");
        resultSaver.setProperty(ResultSaver.VARIABLE_NAME,"myVar");
        resultSaver.setProperty(ResultSaver.ERRORS_ONLY, "true");
        resultSaver.testStarted();
        resultSaver.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        String fileName = sampleResult.getResultFileName();
        Assert.assertEquals("", fileName);
        Assert.assertNull(vars.get("myVar"));
    }
    
    @Test
    public void testFailureSaveErrorsOnly() {
        sampleResult.setSuccessful(true);
        resultSaver.setProperty(ResultSaver.NUMBER_PAD_LENGTH, "5");
        resultSaver.setProperty(ResultSaver.VARIABLE_NAME,"myVar");
        resultSaver.setProperty(ResultSaver.ERRORS_ONLY, "true");
        resultSaver.testStarted();
        sampleResult.setSuccessful(false);
        resultSaver.sampleOccurred(new SampleEvent(sampleResult, "JUnit-TG"));
        String fileName = sampleResult.getResultFileName();
        Assert.assertNotNull(fileName);
        Assert.assertEquals("00001.unknown", fileName);
        File file = new File(FileServer.getDefaultBase(), fileName);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
        Assert.assertEquals("00001.unknown", vars.get("myVar"));
    }
    
    @Test
    public void testMakeFileName() {
        resultSaver.setProperty(ResultSaver.FILENAME, "test");
        resultSaver.testStarted();
        Assert.assertEquals("test", resultSaver.makeFileName(null, true, true));
        resultSaver.testStarted();
        Assert.assertEquals("test", resultSaver.makeFileName("text/plain", true, true));
        resultSaver.testStarted();
        Assert.assertEquals("test", resultSaver.makeFileName("text/plain;charset=utf8", true, true));
        
        Assert.assertEquals("test1.plain", resultSaver.makeFileName("text/plain", false, false));
        resultSaver.testStarted();
        Assert.assertEquals("test.plain", resultSaver.makeFileName("text/plain", true, false));
        resultSaver.testStarted();
        Assert.assertEquals("test1", resultSaver.makeFileName("text/plain", false, true));
        Assert.assertEquals("test2", resultSaver.makeFileName("text/plain", false, true));
        
        resultSaver.testStarted();
        Assert.assertEquals("test.plain", resultSaver.makeFileName("text/plain;charset=UTF-8", true, false));
        
        resultSaver.testStarted();
        Assert.assertEquals("test.unknown", resultSaver.makeFileName(null, true, false));

    }
}
