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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class SizeAssertionTest extends JMeterTestCase {

    private SizeAssertion assertion;
    private SampleResult sample1, sample0;
    private AssertionResult result;
    private final String data1 = "response Data\n" + "line 2\n\nEOF";
    private final int data1Len = data1.length();

    @Before
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        assertion = new SizeAssertion();
        assertion.setThreadContext(jmctx);
        assertion.setTestFieldResponseBody();
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sample0 = new SampleResult();
        sample1 = new SampleResult();
        sample1.setResponseData(data1, null);
    }

    @Test
    public void testSizeAssertionEquals() throws Exception {
        assertion.setCompOper(SizeAssertion.EQUAL);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertFailed();

        result = assertion.getResult(sample0);
        assertPassed();

        assertion.setAllowedSize(data1Len);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertFailed();
    }

    @Test
    public void testSizeAssertionNotEquals() throws Exception {
        assertion.setCompOper(SizeAssertion.NOTEQUAL);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertFailed();

        assertion.setAllowedSize(data1Len);
        result = assertion.getResult(sample1);
        assertFailed();

        result = assertion.getResult(sample0);
        assertPassed();
    }

    @Test
    public void testSizeAssertionGreaterThan() throws Exception {
        assertion.setCompOper(SizeAssertion.GREATERTHAN);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertFailed();

        assertion.setAllowedSize(data1Len);
        result = assertion.getResult(sample1);
        assertFailed();

        result = assertion.getResult(sample0);
        assertFailed();
    }

    @Test
    public void testSizeAssertionGreaterThanEqual() throws Exception {
        assertion.setCompOper(SizeAssertion.GREATERTHANEQUAL);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertPassed();

        assertion.setAllowedSize(data1Len);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertFailed();
    }

    @Test
    public void testSizeAssertionLessThan() throws Exception {
        assertion.setCompOper(SizeAssertion.LESSTHAN);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertFailed();

        result = assertion.getResult(sample0);
        assertFailed();

        assertion.setAllowedSize(data1Len + 1);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertPassed();
    }

    @Test
    public void testSizeAssertionLessThanEqual() throws Exception {
        assertion.setCompOper(SizeAssertion.LESSTHANEQUAL);
        assertion.setAllowedSize(0);
        result = assertion.getResult(sample1);
        assertFailed();

        result = assertion.getResult(sample0);
        assertPassed();

        assertion.setAllowedSize(data1Len + 1);
        result = assertion.getResult(sample1);
        assertPassed();

        result = assertion.getResult(sample0);
        assertPassed();
    }
    // TODO - need a lot more tests

    private void assertPassed() throws Exception {
        assertNull("Failure message should be null", result.getFailureMessage());
        assertFalse(result.isError());
        assertFalse(result.isFailure());
    }

    private void assertFailed() throws Exception {
        assertNotNull("Failure message should not be null", result.getFailureMessage());
        assertFalse("Should not be: Response was null", "Response was null".equals(result.getFailureMessage()));
        assertFalse(result.isError());
        assertTrue(result.isFailure());
    }
}
