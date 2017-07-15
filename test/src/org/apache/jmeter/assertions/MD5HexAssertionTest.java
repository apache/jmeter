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

import java.nio.charset.StandardCharsets;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MD5HexAssertionTest {
    
    private MD5HexAssertion assertion;

    @Before
    public void setUp() {
        assertion = new MD5HexAssertion();
    }

    @Test
    public void testEmptyResponse() throws Exception {
        AssertionResult result = assertion.getResult(sampleResult(""));
        Assert.assertTrue(result.isFailure());
        Assert.assertNotEquals(result.getFailureMessage(), "");
    }

    @Test
    public void testEmptyMD5() throws Exception {
        assertion.setAllowedMD5Hex("");
        AssertionResult result = assertion.getResult(sampleResult("anything"));
        Assert.assertTrue(result.isFailure());
        Assert.assertNotEquals(result.getFailureMessage(), "");
    }

    @Test
    public void testWrongMD5() throws Exception {
        assertion.setAllowedMD5Hex("a");
        AssertionResult result = assertion.getResult(sampleResult("anything"));
        Assert.assertTrue(result.isFailure());
        Assert.assertNotEquals(result.getFailureMessage(), "");
    }

    @Test
    public void testCorrectMD5LowerCase() throws Exception {
        assertion.setAllowedMD5Hex("f0e166dc34d14d6c228ffac576c9a43c");
        AssertionResult result = assertion.getResult(sampleResult("anything"));
        Assert.assertFalse(result.isFailure());
        Assert.assertFalse(result.isError());
        Assert.assertNull(result.getFailureMessage());
    }

    @Test
    public void testCorrectMD5MixedCase() throws Exception {
        assertion.setAllowedMD5Hex("F0e166Dc34D14d6c228ffac576c9a43c");
        AssertionResult result = assertion.getResult(sampleResult("anything"));
        Assert.assertFalse(result.isFailure());
        Assert.assertFalse(result.isError());
        Assert.assertNull(result.getFailureMessage());
    }

    @Test
    public void testMD5() throws Exception {
        Assert.assertEquals("D41D8CD98F00B204E9800998ECF8427E", MD5HexAssertion.baMD5Hex(new byte[] {}).toUpperCase(java.util.Locale.ENGLISH));
    }

    private SampleResult sampleResult(String data) {
        SampleResult response = new SampleResult();
        response.setResponseData(data.getBytes(StandardCharsets.UTF_8));
        return response;
    }

}
