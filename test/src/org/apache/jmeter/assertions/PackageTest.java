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

import junit.framework.TestCase;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
public class PackageTest extends TestCase {

	public PackageTest() {
		super();
	}

	public PackageTest(String arg0) {
		super(arg0);
	}
	public void testHex() throws Exception {
		assertEquals("00010203", MD5HexAssertion.baToHex(new byte[] { 0, 1, 2, 3 }));
		assertEquals("03020100", MD5HexAssertion.baToHex(new byte[] { 3, 2, 1, 0 }));
		assertEquals("0f807fff", MD5HexAssertion.baToHex(new byte[] { 0xF, -128, 127, -1 }));
	}

	public void testMD5() throws Exception {
		assertEquals("D41D8CD98F00B204E9800998ECF8427E", MD5HexAssertion.baMD5Hex(new byte[] {}).toUpperCase());
	}

	int threadsRunning;

	int failed;

	public void testThreadSafety() throws Exception {
		Thread[] threads = new Thread[100];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new TestThread();
		}
		failed = 0;
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
			threadsRunning++;
		}
		synchronized (this) {
			while (threadsRunning > 0) {
				wait();
			}
		}
		assertEquals(failed, 0);
	}

	class TestThread extends Thread {
		static final String TEST_STRING = "DAbale arroz a la zorra el abad.";

		// Used to be 'dábale', but caused trouble on Gump. Reasons
		// unknown.
		static final String TEST_PATTERN = ".*A.*\\.";

		public void run() {
			ResponseAssertion assertion = new ResponseAssertion();
			assertion.setTestFieldResponseData();
			assertion.setToContainsType();
			assertion.addTestString(TEST_PATTERN);
			SampleResult response = new SampleResult();
			response.setResponseData(TEST_STRING.getBytes());
			for (int i = 0; i < 100; i++) {
				AssertionResult result;
				result = assertion.getResult(response);
				if (result.isFailure() || result.isError()) {
					failed++;
				}
			}
			synchronized (PackageTest.this) {
				threadsRunning--;
				PackageTest.this.notifyAll();
			}
		}
	}
	
	public static class XPathAssertionTest extends TestCase {
		private static final Logger log = LoggingManager.getLoggerForClass();

		XPathAssertion assertion;

		SampleResult result;

		JMeterVariables vars;

		JMeterContext jmctx;

		public XPathAssertionTest() {
			super();
		}

		public XPathAssertionTest(String name) {
			super(name);
		}

		public void setUp() {
			jmctx = JMeterContextService.getContext();
			assertion = new XPathAssertion();
			assertion.setThreadContext(jmctx);// This would be done by the run
												// command
			// assertion.setRefName("regVal");

			result = new SampleResult();
			String data = "<company-xmlext-query-ret>" + "<row>" + "<value field=\"RetCode\">LIS_OK</value>"
					+ "<value field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value>"
					+ "<value field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value>"
					+ "<value field=\"pinposition1\">1</value>" + "<value field=\"pinpositionvalue1\"></value>"
					+ "<value field=\"pinposition2\">5</value>" + "<value field=\"pinpositionvalue2\"></value>"
					+ "<value field=\"pinposition3\">6</value>" + "<value field=\"pinpositionvalue3\"></value>"
					+ "</row>" + "</company-xmlext-query-ret>";
			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
		}

		public void testAssertion() throws Exception {
			assertion.setXPathString("//row/value[@field = 'alias']");
			AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
			log.debug(" res " + res.isError());
			log.debug(" failure " + res.getFailureMessage());
			assertFalse(res.isError());
			assertFalse(res.isFailure());
		}

		public void testNegateAssertion() throws Exception {
			assertion.setXPathString("//row/value[@field = 'noalias']");
			assertion.setNegated(true);

			AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
			log.debug(" res " + res.isError());
			log.debug(" failure " + res.getFailureMessage());
			assertFalse(res.isError());
			assertFalse(res.isFailure());
		}

		public void testValidationFailure() throws Exception {
			assertion.setXPathString("//row/value[@field = 'alias']");
			assertion.setNegated(false);
			assertion.setValidating(true);
			AssertionResult res = assertion.getResult(jmctx.getPreviousResult());
			log.debug(res.getFailureMessage() + " error: " + res.isError() + " failure: " + res.isFailure());
			assertTrue(res.isError());
			assertFalse(res.isFailure());

		}

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

			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
			assertion.setXPathString("/");
			assertion.setValidating(true);
			AssertionResult res = assertion.getResult(result);
			assertFalse(res.isError());
			assertFalse(res.isFailure());
		}

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

			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
			assertion.setXPathString("/");
			assertion.setValidating(true);
			AssertionResult res = assertion.getResult(result);
			log.debug("failureMessage: " + res.getFailureMessage());
			assertTrue(res.isError());
			assertFalse(res.isFailure());
		}

		public void testTolerance() throws Exception {
			String data = "<html><head><title>testtitle</title></head>" + "<body>"
					+ "<p><i><b>invalid tag nesting</i></b><hr>" + "</body></html>";

			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
			assertion.setXPathString("/html/head/title");
			assertion.setValidating(true);
			assertion.setTolerant(true);
			AssertionResult res = assertion.getResult(result);
			log.debug("failureMessage: " + res.getFailureMessage());
			assertFalse(res.isFailure());
			assertFalse(res.isError());
		}

		public void testNoTolerance() throws Exception {
			String data = "<html><head><title>testtitle</title></head>" + "<body>"
					+ "<p><i><b>invalid tag nesting</i></b><hr>" + "</body></html>";

			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
			assertion.setXPathString("/html/head/title");
			assertion.setValidating(false);
			assertion.setTolerant(false);
			AssertionResult res = assertion.getResult(result);
			log.debug("failureMessage: " + res.getFailureMessage());
			assertTrue(res.isError());
			assertFalse(res.isFailure());
		}

	}

	public static class ResponseAssertionTest extends TestCase{

		private JMeterContext jmctx;
		private ResponseAssertion assertion;
		private SampleResult sample;
		private JMeterVariables vars;
		private AssertionResult result;
		
		public void setUp() {
			jmctx = JMeterContextService.getContext();
			assertion = new ResponseAssertion();
			assertion.setThreadContext(jmctx);
			sample = new SampleResult();
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(sample);
			sample.setResponseData(
					(
					"response Data\n" +
					"line 2\n\nEOF"
					).getBytes());
			sample.setSamplerData("Sampler Label");// This is where RA checks the URL!
			sample.setResponseCode("401");
			sample.setResponseHeaders("X-Header: abcd");
		}

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
		
		public void testResponseAssertionHeaders() throws Exception{
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
			assertion.addTestString("(?s)line \\d+.*EOF");
			result = assertion.getResult(sample);
			assertPassed();

			assertion.setTestFieldResponseCode();
			
			assertion.clearTestStrings();
			assertion.addTestString("401");
			result = assertion.getResult(sample);
			assertPassed();

        }
// TODO - need a lot more tests
		
		private void assertPassed() throws Exception{
			if (null != result.getFailureMessage()){
				//System.out.println(result.getFailureMessage());// debug
			}
			assertNull(result.getFailureMessage(),result.getFailureMessage());
			assertFalse(result.isError());
			assertFalse(result.isFailure());		
		}
		
		private void assertFailed() throws Exception{
			assertNotNull(result.getFailureMessage());
			//System.out.println(result.getFailureMessage());
			assertFalse("Should not be: Response was null","Response was null".equals(result.getFailureMessage()));
			assertFalse(result.isError());
			assertTrue(result.isFailure());		
			
		}
}

	public static class SizeAssertionTest extends JMeterTestCase{

		private JMeterContext jmctx;
		private SizeAssertion assertion;
		private SampleResult sample1,sample0;
		private JMeterVariables vars;
		private AssertionResult result;
		private String data1 = "response Data\n" +	"line 2\n\nEOF";
		private int data1Len=data1.length();
		public void setUp() {
			jmctx = JMeterContextService.getContext();
			assertion = new SizeAssertion();
			assertion.setThreadContext(jmctx);
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			sample0 = new SampleResult();
			sample1 = new SampleResult();
			sample1.setResponseData(data1.getBytes());
		}

		public void testSizeAssertionEquals() throws Exception{
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
		
		public void testSizeAssertionNotEquals() throws Exception{
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

		public void testSizeAssertionGreaterThan() throws Exception{
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
		
		public void testSizeAssertionGreaterThanEqual() throws Exception{
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
		
		public void testSizeAssertionLessThan() throws Exception{
			assertion.setCompOper(SizeAssertion.LESSTHAN);
			assertion.setAllowedSize(0);
			result = assertion.getResult(sample1);
			assertFailed();

			result = assertion.getResult(sample0);
			assertFailed();

			assertion.setAllowedSize(data1Len+1);
			result = assertion.getResult(sample1);
			assertPassed();

			result = assertion.getResult(sample0);
			assertPassed();
        }

		public void testSizeAssertionLessThanEqual() throws Exception{
			assertion.setCompOper(SizeAssertion.LESSTHANEQUAL);
			assertion.setAllowedSize(0);
			result = assertion.getResult(sample1);
			assertFailed();

			result = assertion.getResult(sample0);
			assertPassed();

			assertion.setAllowedSize(data1Len+1);
			result = assertion.getResult(sample1);
			assertPassed();

			result = assertion.getResult(sample0);
			assertPassed();
        }
// TODO - need a lot more tests
		
		private void assertPassed() throws Exception{
			if (null != result.getFailureMessage()){
				//System.out.println(result.getFailureMessage());// debug
			}
			assertNull("Failure message should be null",result.getFailureMessage());
			assertFalse(result.isError());
			assertFalse(result.isFailure());		
		}
		
		private void assertFailed() throws Exception{
			assertNotNull("Failure nessage should not be null",result.getFailureMessage());
			//System.out.println(result.getFailureMessage());
			assertFalse("Should not be: Response was null","Response was null".equals(result.getFailureMessage()));
			assertFalse(result.isError());
			assertTrue(result.isFailure());		
			
		}
}

}
