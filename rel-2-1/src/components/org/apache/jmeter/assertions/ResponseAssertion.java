/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * 
 * @author Michael Stover
 * @author <a href="mailto:jacarlco@katun.com">Jonathan Carlson</a>
 */
public class ResponseAssertion extends AbstractTestElement implements Serializable, Assertion {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public final static String TEST_FIELD = "Assertion.test_field";  // $NON-NLS-1$

	// Values for TEST_FIELD
	public final static String SAMPLE_LABEL = "Assertion.sample_label"; // $NON-NLS-1$

	public final static String RESPONSE_DATA = "Assertion.response_data"; // $NON-NLS-1$

	public final static String RESPONSE_CODE = "Assertion.response_code"; // $NON-NLS-1$

	public final static String RESPONSE_MESSAGE = "Assertion.response_message"; // $NON-NLS-1$

	public final static String ASSUME_SUCCESS = "Assertion.assume_success"; // $NON-NLS-1$

	public final static String TEST_STRINGS = "Asserion.test_strings"; // $NON-NLS-1$

	public final static String TEST_TYPE = "Assertion.test_type"; // $NON-NLS-1$

	/*
	 * Mask values for TEST_TYPE TODO: remove either MATCH or CONTAINS - they
	 * are mutually exckusive
	 */
	private final static int MATCH = 1 << 0;

	final static int CONTAINS = 1 << 1;

	private final static int NOT = 1 << 2;

	private static ThreadLocal matcher = new ThreadLocal() {
		protected Object initialValue() {
			return new Perl5Matcher();
		}
	};

	private static final PatternCacheLRU patternCache = new PatternCacheLRU(1000, new Perl5Compiler());

	/***************************************************************************
	 * !ToDo (Constructor description)
	 **************************************************************************/
	public ResponseAssertion() {
		setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList()));
	}

	/***************************************************************************
	 * !ToDo (Constructor description)
	 * 
	 * @param field
	 *            !ToDo (Parameter description)
	 * @param type
	 *            !ToDo (Parameter description)
	 * @param string
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public ResponseAssertion(String field, int type, String string) {
		this();
		setTestField(field);
		setTestType(type);
		getTestStrings().addProperty(new StringProperty(string, string));
	}

	public void clear() {
		super.clear();
		setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList()));
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param testField
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void setTestField(String testField) {
		setProperty(TEST_FIELD, testField);
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param testType
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void setTestType(int testType) {
		setProperty(new IntegerProperty(TEST_TYPE, testType));
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param testString
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void addTestString(String testString) {
		getTestStrings().addProperty(new StringProperty(String.valueOf(testString.hashCode()), testString));
	}

	public void setTestString(String testString, int index)// NOTUSED?
	{
		getTestStrings().set(index, testString);
	}

	public void removeTestString(String testString)// NOTUSED?
	{
		getTestStrings().remove(testString);
	}

	public void removeTestString(int index)// NOTUSED?
	{
		getTestStrings().remove(index);
	}

	public void clearTestStrings() {
		getTestStrings().clear();
	}

	/***************************************************************************
	 * !ToDoo (Method description)
	 * 
	 * @param response
	 *            !ToDo (Parameter description)
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result;

		// None of the other Assertions check the response status, so remove
		// this check
		// for the time being, at least...
		// if (!response.isSuccessful())
		// {
		// result = new AssertionResult();
		// result.setError(true);
		// byte [] ba = response.getResponseData();
		// result.setFailureMessage(
		// ba == null ? "Unknown Error (responseData is empty)" : new String(ba)
		// );
		// return result;
		// }

		result = evaluateResponse(response);
		return result;
	}

	/***************************************************************************
	 * !ToDoo (Method description)
	 * 
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public String getTestField() {
		return getPropertyAsString(TEST_FIELD);
	}

	/***************************************************************************
	 * !ToDoo (Method description)
	 * 
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public int getTestType() {
		JMeterProperty type = getProperty(TEST_TYPE);
		if (type instanceof NullProperty) {
			return CONTAINS;
		}
		return type.getIntValue();
	}

	/***************************************************************************
	 * !ToDoo (Method description)
	 * 
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public CollectionProperty getTestStrings() {
		return (CollectionProperty) getProperty(TEST_STRINGS);
	}

	public boolean isContainsType() {
		return (getTestType() & CONTAINS) > 0;
	}

	public boolean isMatchType() {
		return (getTestType() & MATCH) > 0;
	}

	public boolean isNotType() {
		return (getTestType() & NOT) > 0;
	}

	public void setToContainsType() {
		setTestType((getTestType() | CONTAINS) & (~MATCH));
	}

	public void setToMatchType() {
		setTestType((getTestType() | MATCH) & (~CONTAINS));
	}

	public void setToNotType() {
		setTestType((getTestType() | NOT));
	}

	public void unsetNotType() {
		setTestType(getTestType() & ~NOT);
	}

	public boolean getAssumeSuccess() {
		return getPropertyAsBoolean(ASSUME_SUCCESS, false);
	}

	public void setAssumeSuccess(boolean b) {
		setProperty(ASSUME_SUCCESS, JOrphanUtils.booleanToString(b));
	}

	/**
	 * Make sure the response satisfies the specified assertion requirements.
	 * 
	 * @param response
	 *            an instance of SampleResult
	 * @return an instance of AssertionResult
	 */
	AssertionResult evaluateResponse(SampleResult response) {
		boolean pass = true;
		boolean not = (NOT & getTestType()) > 0;
		AssertionResult result = new AssertionResult();
		String toCheck = ""; // The string to check (Url or data)

		if (getAssumeSuccess()) {
			response.setSuccessful(true);// Allow testing of failure codes
		}

		// What are we testing against?
		if (ResponseAssertion.RESPONSE_DATA.equals(getTestField())) {
			// TODO treat header separately from response? (would not apply to
			// all samplers)
			String data = response.getResponseDataAsString(); // (bug25052)
			toCheck = new StringBuffer(response.getResponseHeaders()).append(data).toString();
		} else if (ResponseAssertion.RESPONSE_CODE.equals(getTestField())) {
			toCheck = response.getResponseCode();
		} else if (ResponseAssertion.RESPONSE_MESSAGE.equals(getTestField())) {
			toCheck = response.getResponseMessage();
		} else { // Assume it is the URL
			toCheck = response.getSamplerData();
			if (toCheck == null)
				toCheck = "";
		}

		if (toCheck.length() == 0) {
			return result.setResultForNull();
		}

		result.setFailure(false);
		result.setError(false);

		boolean contains = isContainsType(); // do it once outside loop
		boolean debugEnabled = log.isDebugEnabled();
		if (debugEnabled){
			log.debug("Type:" + (contains?"Contains":"Match") + (not? "(not)": ""));
		}
		
		try {
			// Get the Matcher for this thread
			Perl5Matcher localMatcher = (Perl5Matcher) matcher.get();
			PropertyIterator iter = getTestStrings().iterator();
			while (iter.hasNext()) {
				String stringPattern = iter.next().getStringValue();
				Pattern pattern = patternCache.getPattern(stringPattern, Perl5Compiler.READ_ONLY_MASK);
				boolean found;
				if (contains) {
					found = localMatcher.contains(toCheck, pattern);
				} else {
					found = localMatcher.matches(toCheck, pattern);
				}
				pass = not ? !found : found;
				if (!pass) {
					if (debugEnabled){log.debug("Failed: "+pattern);}
					result.setFailure(true);
					result.setFailureMessage(getFailText(stringPattern));
					break;
				}
				if (debugEnabled){log.debug("Passed: "+pattern);}
			}
		} catch (MalformedCachePatternException e) {
			result.setError(true);
			result.setFailure(false);
			result.setFailureMessage("Bad test configuration " + e);
		}
		return result;
	}

	/**
	 * Generate the failure reason from the TestType
	 * 
	 * @param stringPattern
	 * @return the message for the assertion report 
	 */
	// TODO strings should be resources
	private String getFailText(String stringPattern) {
		String text;
		String what;
		if (ResponseAssertion.RESPONSE_DATA.equals(getTestField())) {
			what = "text";
		} else if (ResponseAssertion.RESPONSE_CODE.equals(getTestField())) {
			what = "code";
		} else if (ResponseAssertion.RESPONSE_MESSAGE.equals(getTestField())) {
			what = "message";
		} else // Assume it is the URL
		{
			what = "URL";
		}
		switch (getTestType()) {
		case CONTAINS:
			text = " expected to contain ";
			break;
		case NOT | CONTAINS:
			text = " expected not to contain ";
			break;
		case MATCH:
			text = " expected to match ";
			break;
		case NOT | MATCH:
			text = " expected not to match ";
			break;
		default:// should never happen...
			text = " expected something using ";
		}

		return "Test failed, " + what + text + "/" + stringPattern + "/";
	}
}
