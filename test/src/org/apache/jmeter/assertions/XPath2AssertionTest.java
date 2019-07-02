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

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;

public class XPath2AssertionTest {

    private final String xmlDoc = JMeterUtils.getResourceFileAsText("XPathUtilTestXml.xml");
    private XPath2Assertion assertion;
    private SampleResult response;
    private JMeterVariables vars;
    private JMeterContext jmctx;

    @Before
    public void setUp() throws Exception {
        jmctx = JMeterContextService.getContext();
        assertion = new  XPath2Assertion();
        assertion.setThreadContext(jmctx);// This would be done by the run command
        response = new SampleResult();
        response.setResponseData(xmlDoc, "UTF-8");
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(response);
    }

    @Test
    public void testXPath2AssertionPath1() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "//Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 conforms to xml, the result of assertion should be true ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
    @Test
    public void testXPath2AssertionPath1Negated() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "//Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        assertion.setNegated(true);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertTrue("When xpath2 conforms to xml, the result of assertion should be false ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());

    }
    @Test
    public void testXPath2AssertionPath2() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84#";
        String xPathQuery = "//Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertTrue("When xpath2 doesn't conform to xml, the result of assertion should be false ", res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
    @Test
    public void testXPath2AssertionPath2Negated() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84#";
        String xPathQuery = "//Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        assertion.setNegated(true);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 doesn't conform to xml, the result of assertion should be true ", res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }

    @Test
    public void testXPath2AssertionBool1() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "count(//Employee)=4";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 conforms to xml, the result of assertion should be true ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
    @Test
    public void testXPath2AssertionBool1Negated() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "count(//Employee)=4";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        assertion.setNegated(true);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertTrue("When xpath2 conforms to xml, the result of assertion should be false ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
    @Test
    public void testXPath2AssertionBool2() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "count(//Employee)=3";  //Wrong
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertTrue("When xpath2 doesn't conforms to xml, the result of assertion should be false ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
    @Test
    public void testXPath2AssertionBool2Negated() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "count(//Employee)=3";  //Wrong
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        assertion.setNegated(true);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 doesn't conforms to xml, the result of assertion should be true ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }

    @Test
    public void testScope(){
        assertion.setThreadContext(jmctx);// This would be done by the run command
        SampleResult result = new SampleResult();
        String data = "<html><head><title>testtitle</title></head></html>";
        assertion.setScopeVariable("testScope");
        vars.put("testScope", data);
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/html/head/title");
        AssertionResult res = assertion.getResult(result);
        assertFalse("When xpath conforms to xml, the result of assertion "
                + "should be true ",res.isFailure());
        assertFalse(res.isError());
    }
    @Test
    public void testScopeFailure(){
        assertion.setThreadContext(jmctx);// This would be done by the run command
        SampleResult result = new SampleResult();
        String data = "<html><head><title>testtitle</title></head></html>";
        assertion.setScopeVariable("testScope");
        vars.put("testScope", data);
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/html/head/tit");
        AssertionResult res = assertion.getResult(result);
        assertTrue("When xpath doesn't conforms to xml, the result "
                + "of assertion should be false ",res.isFailure());
        assertFalse(res.isError());
    }
    @Test
    public void testResponseDataIsEmpty(){
        assertion.setThreadContext(jmctx);// This would be done by the run command
        SampleResult result = new SampleResult();
        assertion.setScopeVariable("testScope");
        vars.put("testScope", null);
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        assertion.setXPathString("/html/head/tit");
        AssertionResult res = assertion.getResult(result);
        assertTrue("When xpath doesn't conforms to xml, the result "
                + "of assertion should be false ",res.isFailure());
        assertFalse(res.isError());
        assertEquals("When the response data is empty, the result of assertion should be false",
                "Response was null", res.getFailureMessage());
    }
    @Test
    public void testBadXpathFormat() throws FactoryConfigurationError {
        String namespaces = "age=http://www.w3.org/2003/01/geo/wgs84_pos#";
        String xPathQuery = "///Employees/Employee[1]/age:ag";
        assertion.setNamespaces(namespaces);
        assertion.setXPathString(xPathQuery);
        response.setResponseData(xmlDoc, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertTrue("When format of xpath is wrong, the test should failed",res.isError());
        assertTrue(res.getFailureMessage().contains("Exception occured computing assertion with XPath"));
    }

    @Test
    public void testXPath2AssertionPathWithoutNamespace() throws FactoryConfigurationError {
        String data = "<html><head><title>testtitle</title></head></html>";
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(response);
        String xPathQuery = "/html/head";
        assertion.setXPathString(xPathQuery);
        response.setResponseData(data, "UTF-8");
        AssertionResult res = assertion.getResult(response);
        assertFalse("When xpath2 conforms to xml, the result of assertion should be true ",res.isFailure());
        assertFalse("When the format of xpath2 is right, assertion will run correctly ",res.isError());
    }
}
