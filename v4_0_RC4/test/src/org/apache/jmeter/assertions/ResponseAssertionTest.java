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

package org.apache.jmeter.assertions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.oro.text.MalformedCachePatternException;
import org.junit.Before;
import org.junit.Test;

public class ResponseAssertionTest {

    public ResponseAssertionTest() {
    }

    private ResponseAssertion assertion;
    private SampleResult sample;
    private AssertionResult result;
    
    @Before
    public void setUp() throws MalformedURLException {
        JMeterContext jmctx = JMeterContextService.getContext();
        assertion = new ResponseAssertion();
        assertion.setThreadContext(jmctx);
        sample = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(sample);
        sample.setResponseData("response Data\nline 2\n\nEOF", null);
        sample.setURL(new URL("http://localhost/Sampler/Data/"));
        sample.setResponseCode("401");
        sample.setResponseHeaders("X-Header: abcd");
        sample.setRequestHeaders("X-reqHeader: cdef");
    }

    @Test
    public void testResponseAssertionNoText() throws Exception{
        assertion.clearTestStrings();
        assertion.unsetNotType();
        assertion.setToSubstringType();
        assertion.setTestFieldResponseData();
        //assertion.addTestString("response Data");
        result = assertion.getResult(sample);
        assertPassed();
    }
    
    @Test
    public void testResponseAssertionEquals() throws Exception{
        assertion.unsetNotType();
        assertion.setToEqualsType();
        assertion.setTestFieldURL();
        assertion.addTestString("Sampler Label");
        assertion.addTestString("Sampler labelx");      
        result = assertion.getResult(sample);
        assertFailed();

        assertion.setToNotType();
        assertion.clearTestStrings();
        assertion.addTestString("Sampler LabeL");
        assertion.addTestString("Sampler Labelx");      
        result = assertion.getResult(sample);
        assertPassed();
    }
    
    @Test
    public void testCustomFailureMessage() throws Exception {
        assertion.unsetNotType();
        assertion.setToEqualsType();
        assertion.setTestFieldURL();
        assertion.addTestString("Sampler Label");
        assertion.addTestString("Sampler labelx");
        assertion.setCustomFailureMessage("Custom failure message");
        result = assertion.getResult(sample);
        assertFailed();
        assertEquals("Custom failure message", result.getFailureMessage());
        
        assertion.setToOrType();
        result = assertion.getResult(sample);
        assertFailed();
        assertEquals("Custom failure message", result.getFailureMessage());
    }
    
    @Test
    public void testMalformedCachePatternException() throws Exception{
        assertion.unsetNotType();
        assertion.setToMatchType();
        assertion.setTestFieldResponseHeaders();
        assertion.addTestString("[]");
        result = assertion.getResult(sample);
        assertNotNull(result.getFailureMessage());
        assertFalse("Should not be: Response was null","Response was null".equals(result.getFailureMessage()));
        assertTrue("Not expecting error: "+result.getFailureMessage(),result.isError());
        
        assertion.setCustomFailureMessage("Custom failure message");
        result = assertion.getResult(sample);
        assertTrue("Did not get expected error: "+result.getFailureMessage(),result.isError());
        assertFalse("Failure message must not be custom failure message for error", 
                "Custom failure message".equals(result.getFailureMessage()));

    }
    
    @Test
    public void testResponseAssertionResponseHeaders() throws Exception{
        assertion.unsetNotType();
        assertion.setToEqualsType();
        assertion.setTestFieldResponseHeaders();
        assertion.addTestString("X-Header: abcd");
        assertion.addTestString("X-Header: abcdx");
        result = assertion.getResult(sample);
        assertFailed();

        assertion.clearTestStrings();
        assertion.addTestString("X-Header: abcd");
        result = assertion.getResult(sample);
        assertPassed();
    }
    
    @Test
    public void testResponseAssertionRequestHeaders() throws Exception{
        assertion.unsetNotType();
        assertion.setToEqualsType();
        assertion.setTestFieldRequestHeaders();
        assertion.addTestString("X-reqHeader: cdef");
        assertion.addTestString("X-reqHeader: cdefx");
        result = assertion.getResult(sample);
        assertFailed();

        assertion.clearTestStrings();
        assertion.addTestString("X-reqHeader: cdef");
        result = assertion.getResult(sample);
        assertPassed();
    }
    
    @Test
    public void testResponseAssertionContains() throws Exception{
        assertion.unsetNotType();
        assertion.setToContainsType();
        assertion.setTestFieldURL();
        assertion.addTestString("Sampler");
        assertion.addTestString("Label");
        assertion.addTestString(" x");
        
        result = assertion.getResult(sample);
        assertFailed();
        
        assertion.setToNotType();
        
        result = assertion.getResult(sample);
        assertFailed();

        assertion.clearTestStrings();
        assertion.addTestString("r l");
        result = assertion.getResult(sample);
        assertPassed();

        assertion.unsetNotType();
        assertion.setTestFieldResponseData();
        
        assertion.clearTestStrings();
        assertion.addTestString("line 2");
        result = assertion.getResult(sample);
        assertPassed();
        
        assertion.clearTestStrings();
        assertion.addTestString("line 2");
        assertion.addTestString("NOTINSAMPLEDATA");
        result = assertion.getResult(sample);
        assertFailed();
        
        assertion.clearTestStrings();
        assertion.setToOrType();
        assertion.addTestString("line 2");
        assertion.addTestString("NOTINSAMPLEDATA");
        result = assertion.getResult(sample);
        assertPassed();
        assertion.unsetOrType();
        
        assertion.clearTestStrings();
        assertion.setToOrType();
        assertion.addTestString("NOTINSAMPLEDATA");
        assertion.addTestString("line 2");
        result = assertion.getResult(sample);
        assertPassed();
        assertion.unsetOrType();
        
        assertion.clearTestStrings();
        assertion.setToOrType();
        assertion.addTestString("NOTINSAMPLEDATA");
        assertion.addTestString("NOTINSAMPLEDATA2");
        result = assertion.getResult(sample);
        assertFailed();
        assertion.unsetOrType();
        
        assertion.clearTestStrings();
        assertion.setToOrType();
        assertion.setToNotType();
        assertion.addTestString("line 2");
        assertion.addTestString("NOTINSAMPLEDATA2");
        result = assertion.getResult(sample);
        assertPassed();
        assertion.unsetOrType();
        assertion.unsetNotType();

        
        assertion.clearTestStrings();
        assertion.setToNotType();
        assertion.addTestString("NOTINSAMPLEDATA");
        result = assertion.getResult(sample);
        assertPassed();
        assertion.unsetNotType();
        
        
        assertion.clearTestStrings();
        assertion.addTestString("(?s)line \\d+.*EOF");
        result = assertion.getResult(sample);
        assertPassed();

        assertion.setTestFieldResponseCode();
        
        assertion.clearTestStrings();
        assertion.addTestString("401");
        result = assertion.getResult(sample);
        assertPassed();

    }

    // Bug 46831 - check can match dollars
    @Test
    public void testResponseAssertionContainsDollar() throws Exception {
        sample.setResponseData("value=\"${ID}\" Group$ctl00$drpEmails", null);
        assertion.unsetNotType();
        assertion.setToContainsType();
        assertion.setTestFieldResponseData();
        assertion.addTestString("value=\"\\${ID}\" Group\\$ctl00\\$drpEmails");
        
        result = assertion.getResult(sample);
        assertPassed();        
    }
    
    @Test
    public void testResponseAssertionSubstring() throws Exception{
        assertion.unsetNotType();
        assertion.setToSubstringType();
        assertion.setTestFieldURL();
        assertion.addTestString("Sampler");
        assertion.addTestString("Label");
        assertion.addTestString("+(");
        
        result = assertion.getResult(sample);
        assertFailed();
        
        assertion.setToNotType();
        
        result = assertion.getResult(sample);
        assertFailed();

        assertion.clearTestStrings();
        assertion.addTestString("r l");
        result = assertion.getResult(sample);
        assertPassed();

        assertion.unsetNotType();
        assertion.setTestFieldResponseData();
        
        assertion.clearTestStrings();
        assertion.addTestString("line 2");
        result = assertion.getResult(sample);
        assertPassed();

        assertion.clearTestStrings();
        assertion.addTestString("line 2\n\nEOF");
        result = assertion.getResult(sample);
        assertPassed();

        assertion.setTestFieldResponseCode();
        
        assertion.clearTestStrings();
        assertion.addTestString("401");
        result = assertion.getResult(sample);
        assertPassed();

    }

//TODO - need a lot more tests
    
    private void assertPassed() throws Exception{
        assertNull(result.getFailureMessage(),result.getFailureMessage());
        assertFalse("Not expecting error: "+result.getFailureMessage(),result.isError());
        assertFalse("Not expecting error",result.isError());
        assertFalse("Not expecting failure",result.isFailure());        
    }
    
    private void assertFailed() throws Exception{
        assertNotNull(result.getFailureMessage());
        assertFalse("Should not be: Response was null","Response was null".equals(result.getFailureMessage()));
        assertFalse("Not expecting error: "+result.getFailureMessage(),result.isError());
        assertTrue("Expecting failure",result.isFailure());     
        
    }
    private AtomicInteger failed;

    @Test
    public void testThreadSafety() throws Exception {
        Thread[] threads = new Thread[100];
        CountDownLatch latch = new CountDownLatch(threads.length);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TestThread(latch);
        }
        failed = new AtomicInteger(0);
        for (Thread thread : threads) {
            thread.start();
        }
        latch.await();
        assertEquals(failed.get(), 0);
    }

    class TestThread extends Thread {
        static final String TEST_STRING = "DAbale arroz a la zorra el abad.";

        // Used to be 'dábale', but caused trouble on Gump. Reasons
        // unknown.
        static final String TEST_PATTERN = ".*A.*\\.";

        private CountDownLatch latch;

        public TestThread(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                ResponseAssertion assertion = new ResponseAssertion();
                assertion.setTestFieldResponseData();
                assertion.setToContainsType();
                assertion.addTestString(TEST_PATTERN);
                SampleResult response = new SampleResult();
                response.setResponseData(TEST_STRING, null);
                for (int i = 0; i < 100; i++) {
                    AssertionResult result;
                    result = assertion.getResult(response);
                    if (result.isFailure() || result.isError()) {
                        failed.incrementAndGet();
                    }
                }
            } finally {
                latch.countDown();
            }
        }
    }
}
