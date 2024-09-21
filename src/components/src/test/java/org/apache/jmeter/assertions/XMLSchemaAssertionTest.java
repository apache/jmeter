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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XMLSchemaAssertionTest extends JMeterTestCase {

    private XMLSchemaAssertion assertion;
    private SampleResult result;
    private JMeterContext jmctx;

    @BeforeEach
    public void setUp() throws Exception {
        jmctx = JMeterContextService.getContext();
        assertion = new XMLSchemaAssertion();
        assertion.setThreadContext(jmctx);// This would be done by the run command
        result = new SampleResult();
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    private ByteArrayOutputStream readBA(String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(findTestFile(name)))) {
            int len = 0;
            byte[] data = new byte[512];
            while ((len = bis.read(data)) >= 0) {
                baos.write(data, 0, len);
            }
        }
        return baos;
    }

    private byte[] readFile(String name) throws IOException {
        try (ByteArrayOutputStream baos = readBA(name)) {
            return baos.toByteArray();
        }
    }

    @Test
    public void testAssertionOK() throws Exception {
        result.setResponseData(readFile("testfiles/XMLSchematest.xml"));
        assertion.setXsdFileName(findTestPath("testfiles/XMLSchema-pass.xsd"));
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertFalse(res.isError(), "Should not be an error");
        assertFalse(res.isFailure(), "Should not be a failure");
    }

    @Test
    public void testAssertionFail() throws Exception {
        result.setResponseData(readFile("testfiles/XMLSchematest.xml"));
        assertion.setXsdFileName("testfiles/XMLSchema-fail.xsd");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testAssertionBadXSDFile() throws Exception {
        result.setResponseData(readFile("testfiles/XMLSchematest.xml"));
        assertion.setXsdFileName("xtestfiles/XMLSchema-fail.xsd");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertTrue(res.getFailureMessage().indexOf("Failed to read schema document") > 0);
        assertTrue(res.isError());// TODO - should this be a failure?
        assertFalse(res.isFailure());
    }

    @Test
    public void testAssertionNoFile() throws Exception {
        result.setResponseData(readFile("testfiles/XMLSchematest.xml"));
        assertion.setXsdFileName("");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertEquals(XMLSchemaAssertion.FILE_NAME_IS_REQUIRED, res.getFailureMessage());
        assertFalse(res.isError());
        assertTrue(res.isFailure());
    }

    @Test
    public void testAssertionNoResult() throws Exception {
        // result.setResponseData - not set
        assertion.setXsdFileName("testfiles/XMLSchema-fail.xsd");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
        assertFalse(res.isError());
        assertTrue(res.isFailure());
    }

    @Test
    public void testAssertionEmptyResult() throws Exception {
        result.setResponseData("", null);
        assertion.setXsdFileName("testfiles/XMLSchema-fail.xsd");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
        assertFalse(res.isError());
        assertTrue(res.isFailure());
    }

    @Test
    public void testAssertionBlankResult() throws Exception {
        result.setResponseData(" ", null);
        assertion.setXsdFileName("testfiles/XMLSchema-fail.xsd");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertTrue(res.getFailureMessage().indexOf("Premature end of file") > 0);
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testXMLTrailingContent() throws Exception {
        try (ByteArrayOutputStream baos = readBA("testfiles/XMLSchematest.xml")) {
            baos.write("extra".getBytes(StandardCharsets.UTF_8));
            result.setResponseData(baos.toByteArray());
        }
        assertion.setXsdFileName(findTestPath("testfiles/XMLSchema-pass.xsd"));
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertTrue(res.getFailureMessage().indexOf("Content is not allowed in trailing section") > 0);
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testXMLTrailingWhitespace() throws Exception {
        try (ByteArrayOutputStream baos = readBA("testfiles/XMLSchematest.xml")) {
            baos.write(" \t\n".getBytes(StandardCharsets.UTF_8));
            result.setResponseData(baos.toByteArray());
        }
        assertion.setXsdFileName(findTestPath("testfiles/XMLSchema-pass.xsd"));
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        testLog.debug("xisError() " + res.isError() + " isFailure() " + res.isFailure());
        testLog.debug("failure " + res.getFailureMessage());
        assertFalse(res.isError());
        assertFalse(res.isFailure());
    }
}
