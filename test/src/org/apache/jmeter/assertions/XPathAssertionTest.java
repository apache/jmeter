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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class XPathAssertionTest extends JMeterTestCase {

	private XPathAssertion assertion;

	private SampleResult result;

	private JMeterVariables vars;

	private JMeterContext jmctx;

	public XPathAssertionTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		jmctx = JMeterContextService.getContext();
		assertion = new XPathAssertion();
		assertion.setThreadContext(jmctx);// This would be done by the run
											// command
		result = new SampleResult();
		result.setResponseData(readFile("testfiles/XPathAssertionTest.xml"));
		vars = new JMeterVariables();
		jmctx.setVariables(vars);
		//jmctx.setPreviousResult(result);
		//testLog.setPriority(org.apache.log.Priority.DEBUG);
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

	public void testAssertionOK() throws Exception {
		assertion.setXPathString("/");
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertFalse("Should not be a failure", res.isFailure());
	}

	public void testAssertionFail() throws Exception {
		assertion.setXPathString("//x");
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionPath1() throws Exception {
		assertion.setXPathString("//*[code=1]");
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertFalse("Should not be a failure",res.isFailure());
	}

	public void testAssertionPath2() throws Exception {
		assertion.setXPathString("//*[code=2]"); // Not present
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionBool1() throws Exception {
		assertion.setXPathString("count(//error)=2");
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertFalse("Should not be a failure",res.isFailure());
	}

	public void testAssertionBool2() throws Exception {
		assertion.setXPathString("count(//*[code=1])=1");
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertFalse("Should not be a failure",res.isFailure());
	}

	public void testAssertionBool3() throws Exception {
		assertion.setXPathString("count(//error)=1"); // wrong
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionBool4() throws Exception {
		assertion.setXPathString("count(//*[code=2])=1"); //Wrong
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionNumber() throws Exception {
		assertion.setXPathString("count(//error)");// not yet handled
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionNoResult() throws Exception {
		// result.setResponseData - not set
		result = new SampleResult();
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionEmptyResult() throws Exception {
		result.setResponseData("".getBytes());
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertEquals(AssertionResult.RESPONSE_WAS_NULL, res.getFailureMessage());
		assertFalse("Should not be an error", res.isError());
		assertTrue("Should be a failure",res.isFailure());
	}

	public void testAssertionBlankResult() throws Exception {
		result.setResponseData(" ".getBytes());
		AssertionResult res = assertion.getResult(result);
		testLog.debug("isError() " + res.isError() + " isFailure() " + res.isFailure());
		testLog.debug("failure message: " + res.getFailureMessage());
		assertTrue(res.getFailureMessage().indexOf("Premature end of file") > 0);
		assertTrue("Should be an error",res.isError());
		assertFalse("Should not be a failure", res.isFailure());
	}

}
