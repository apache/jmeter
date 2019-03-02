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
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathAssertionTest extends JMeterTestCase {
    private static final Logger log = LoggerFactory.getLogger(XPathAssertionTest.class);

    private XPathAssertion assertion;

    private SampleResult result;

    private JMeterVariables vars;

    private JMeterContext jmctx;


    @Before
    public void setUp() throws Exception {
        jmctx = JMeterContextService.getContext();
        assertion = new XPathAssertion();
        assertion.setThreadContext(jmctx);// This would be done by the run command
        result = new SampleResult();
        result.setResponseData(readFile("testfiles/XPathAssertionTest.xml"));
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    private void setAlternateResponseData(){
        String data = "<company-xmlext-query-ret>" + "<row>" + "<value field=\"RetCode\">LIS_OK</value>"
              + "<value field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value>"
              + "<value field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value>"
              + "<value field=\"pinposition1\">1</value>" + "<value field=\"pinpositionvalue1\"></value>"
              + "<value field=\"pinposition2\">5</value>" + "<value field=\"pinpositionvalue2\"></value>"
              + "<value field=\"pinposition3\">6</value>" + "<value field=\"pinpositionvalue3\"></value>"
              + "</row>" + "</company-xmlext-query-ret>";
        result.setResponseData(data, null);
    }

    private ByteArrayOutputStream readBA(String name) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(findTestFile(name)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        int len = 0;
        byte[] data = new byte[512];
        while ((len = bis.read(data)) >= 0) {
            baos.write(data, 0, len);
        }
        bis.close();
        return baos;
    }

    private byte[] readFile(String name) throws IOException {
        return readBA(name).toByteArray();
    }

    @Test
    public void testAssertionOK() throws Exception {
        assertion.setXPathString("/");
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure", res.isFailure());
    }

    @Test
    public void testAssertionFail() throws Exception {
        assertion.setXPathString("//x");
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionPath1() throws Exception {
        assertion.setXPathString("//*[code=1]");
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure",res.isFailure());
    }

    @Test
    public void testAssertionPath2() throws Exception {
        assertion.setXPathString("//*[code=2]"); // Not present
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionBool1() throws Exception {
        assertion.setXPathString("count(//error)=2");
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure",res.isFailure());
    }

    @Test
    public void testAssertionBool2() throws Exception {
        assertion.setXPathString("count(//*[code=1])=1");
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertFalse("Should not be a failure",res.isFailure());
    }

    @Test
    public void testAssertionBool3() throws Exception {
        assertion.setXPathString("count(//error)=1"); // wrong
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure", res.isFailure());
    }

    @Test
    public void testAssertionBool4() throws Exception {
        assertion.setXPathString("count(//*[code=2])=1"); //Wrong
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionNumber() throws Exception {
        assertion.setXPathString("count(//error)");// not yet handled
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionNoResult() throws Exception {
        // result.setResponseData - not set
        result = new SampleResult();
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionEmptyResult() throws Exception {
        result.setResponseData("", null);
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
        assertFalse("Should not be an error", res.isError());
        assertTrue("Should be a failure",res.isFailure());
    }

    @Test
    public void testAssertionBlankResult() throws Exception {
        result.setResponseData(" ", null);
        AssertionResult res = assertion.getResult(result);
        testLog.debug("isError() {} isFailure() {}", res.isError(), res.isFailure());
        testLog.debug("failure message: {}", res.getFailureMessage());
        assertTrue(res.getFailureMessage().indexOf("Premature end of file") > 0);
        assertTrue("Should be an error",res.isError());
        assertFalse("Should not be a failure", res.isFailure());
    }

    @Test
    public void testNoTolerance() throws Exception {
        String data = "<html><head><title>testtitle</title></head>" + "<body>"
                + "<p><i><b>invalid tag nesting</i></b><hr>" + "</body></html>";

        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/html/head/title");
        assertion.setValidating(false);
        assertion.setTolerant(false);
        AssertionResult res = assertion.getResult(result);
        log.debug("failureMessage: {}", res.getFailureMessage());
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testAssertion() throws Exception {
        setAlternateResponseData();
        assertion.setXPathString("//row/value[@field = 'alias']");
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        log.debug(" res {}", res.isError());
        log.debug(" failure {}", res.getFailureMessage());
        assertFalse(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testNegateAssertion() throws Exception {
        setAlternateResponseData();
        assertion.setXPathString("//row/value[@field = 'noalias']");
        assertion.setNegated(true);

        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        log.debug(" res {}", res.isError());
        log.debug(" failure {}", res.getFailureMessage());
        assertFalse(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testValidationFailure() throws Exception {
        setAlternateResponseData();
        assertion.setXPathString("//row/value[@field = 'alias']");
        assertion.setNegated(false);
        assertion.setValidating(true);
        AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
        log.debug("{} error: {} failure: {}", res.getFailureMessage(), res.isError(), res.isFailure());
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testValidationSuccess() throws Exception {
        String data = "<?xml version=\"1.0\"?>" + "<!DOCTYPE BOOK [" + "<!ELEMENT p (#PCDATA)>"
                + "<!ELEMENT BOOK         (OPENER,SUBTITLE?,INTRODUCTION?,(SECTION | PART)+)>"
                + "<!ELEMENT OPENER       (TITLE_TEXT)*>" + "<!ELEMENT TITLE_TEXT   (#PCDATA)>"
                + "<!ELEMENT SUBTITLE     (#PCDATA)>" + "<!ELEMENT INTRODUCTION (HEADER, p+)+>"
                + "<!ELEMENT PART         (HEADER, CHAPTER+)>" + "<!ELEMENT SECTION      (HEADER, p+)>"
                + "<!ELEMENT HEADER       (#PCDATA)>" + "<!ELEMENT CHAPTER      (CHAPTER_NUMBER, CHAPTER_TEXT)>"
                + "<!ELEMENT CHAPTER_NUMBER (#PCDATA)>" + "<!ELEMENT CHAPTER_TEXT (p)+>" + "]>" + "<BOOK>"
                + "<OPENER>" + "<TITLE_TEXT>All About Me</TITLE_TEXT>" + "</OPENER>" + "<PART>"
                + "<HEADER>Welcome To My Book</HEADER>" + "<CHAPTER>"
                + "<CHAPTER_NUMBER>CHAPTER 1</CHAPTER_NUMBER>" + "<CHAPTER_TEXT>"
                + "<p>Glad you want to hear about me.</p>" + "<p>There's so much to say!</p>"
                + "<p>Where should we start?</p>" + "<p>How about more about me?</p>" + "</CHAPTER_TEXT>"
                + "</CHAPTER>" + "</PART>" + "</BOOK>";

        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/");
        assertion.setValidating(true);
        AssertionResult res = assertion.getResult(result);
        assertFalse(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testValidationFailureWithDTD() throws Exception {
        String data = "<?xml version=\"1.0\"?>" + "<!DOCTYPE BOOK [" + "<!ELEMENT p (#PCDATA)>"
                + "<!ELEMENT BOOK         (OPENER,SUBTITLE?,INTRODUCTION?,(SECTION | PART)+)>"
                + "<!ELEMENT OPENER       (TITLE_TEXT)*>" + "<!ELEMENT TITLE_TEXT   (#PCDATA)>"
                + "<!ELEMENT SUBTITLE     (#PCDATA)>" + "<!ELEMENT INTRODUCTION (HEADER, p+)+>"
                + "<!ELEMENT PART         (HEADER, CHAPTER+)>" + "<!ELEMENT SECTION      (HEADER, p+)>"
                + "<!ELEMENT HEADER       (#PCDATA)>" + "<!ELEMENT CHAPTER      (CHAPTER_NUMBER, CHAPTER_TEXT)>"
                + "<!ELEMENT CHAPTER_NUMBER (#PCDATA)>" + "<!ELEMENT CHAPTER_TEXT (p)+>" + "]>" + "<BOOK>"
                + "<OPENER>" + "<TITLE_TEXT>All About Me</TITLE_TEXT>" + "</OPENER>" + "<PART>"
                + "<HEADER>Welcome To My Book</HEADER>" + "<CHAPTER>"
                + "<CHAPTER_NUMBER>CHAPTER 1</CHAPTER_NUMBER>" + "<CHAPTER_TEXT>"
                + "<p>Glad you want to hear about me.</p>" + "<p>There's so much to say!</p>"
                + "<p>Where should we start?</p>" + "<p>How about more about me?</p>" + "</CHAPTER_TEXT>"
                + "</CHAPTER>" + "<illegal>not defined in dtd</illegal>" + "</PART>" + "</BOOK>";

        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/");
        assertion.setValidating(true);
        AssertionResult res = assertion.getResult(result);
        log.debug("failureMessage: {}", res.getFailureMessage());
        assertTrue(res.isError());
        assertFalse(res.isFailure());
    }

    @Test
    public void testTolerance() throws Exception {
        String data = "<html><head><title>testtitle</title></head>" + "<body>"
                + "<p><i><b>invalid tag nesting</i></b><hr>" + "</body></html>";

        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/html/head/title");
        assertion.setValidating(true);
        assertion.setTolerant(true);
        AssertionResult res = assertion.getResult(result);
        log.debug("failureMessage: {}", res.getFailureMessage());
        assertFalse(res.isFailure());
        assertFalse(res.isError());
    }

}
