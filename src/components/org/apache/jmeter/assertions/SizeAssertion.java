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

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Checks if the results of a Sample matches a particular size.
 * 
 * @author <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 */
public class SizeAssertion extends AbstractTestElement implements Serializable, Assertion {

	private String comparatorErrorMessage = "ERROR!";

	// * Static int to signify the type of logical comparitor to assert
	public final static int EQUAL = 1;

	public final static int NOTEQUAL = 2;

	public final static int GREATERTHAN = 3;

	public final static int LESSTHAN = 4;

	public final static int GREATERTHANEQUAL = 5;

	public final static int LESSTHANEQUAL = 6;

	/** Key for storing assertion-informations in the jmx-file. */
	private static final String SIZE_KEY = "SizeAssertion.size";

	private static final String OPERATOR_KEY = "SizeAssertion.operator";

	byte[] resultData;

	/**
	 * Returns the result of the Assertion. Here it checks wether the Sample
	 * took to long to be considered successful. If so an AssertionResult
	 * containing a FailureMessage will be returned. Otherwise the returned
	 * AssertionResult will reflect the success of the Sample.
	 */
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult(getName());
		result.setFailure(false);
		resultData = response.getResponseData();
		long resultSize = resultData.length;
		if (resultSize==0) {
			return result.setResultForNull();
		}
		// is the Sample the correct size?
		if ((!(compareSize(resultSize)) && (getAllowedSize() > 0))) {
			result.setFailure(true);
			Object[] arguments = { new Long(resultSize), comparatorErrorMessage, new Long(getAllowedSize()) };
			String message = MessageFormat.format(JMeterUtils.getResString("size_assertion_failure"), arguments); //$NON-NLS-1$
			result.setFailureMessage(message);
		}
		return result;
	}

	/**
	 * Returns the size in bytes to be asserted. A duration of 0 indicates this
	 * assertion is to be ignored.
	 */
	public long getAllowedSize() {
		return getPropertyAsLong(SIZE_KEY);
	}

	/***************************************************************************
	 * set the Operator
	 **************************************************************************/
	public void setCompOper(int operator) {
		setProperty(new IntegerProperty(OPERATOR_KEY, operator));

	}

	/**
	 * Returns the operator to be asserted. EQUAL = 1, NOTEQUAL = 2 GREATERTHAN =
	 * 3,LESSTHAN = 4,GREATERTHANEQUAL = 5,LESSTHANEQUAL = 6
	 */

	public int getCompOper() {
		return getPropertyAsInt(OPERATOR_KEY);
	}

	/**
	 * Set the size that shall be asserted.
	 * 
	 * @param size -
	 *            a number of bytes. Is not allowed to be negative. Use
	 *            Long.MAX_VALUE to indicate illegal or empty inputs. This will
	 *            result in not checking the assertion.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>size</code> is negative.
	 */
	public void setAllowedSize(long size) throws IllegalArgumentException {
		if (size < 0L) {
			throw new IllegalArgumentException(JMeterUtils.getResString("argument_must_not_be_negative")); //$NON-NLS-1$
		}
		if (size == Long.MAX_VALUE) {
			setProperty(new LongProperty(SIZE_KEY, 0));
		} else {
			setProperty(new LongProperty(SIZE_KEY, size));
		}
	}

	/**
	 * Compares the the size of a return result to the set allowed size using a
	 * logical comparator set in setLogicalComparator().
	 * 
	 * Possible values are: equal, not equal, greater than, less than, greater
	 * than eqaul, less than equal, .
	 * 
	 */
	private boolean compareSize(long resultSize) {
		boolean result = false;
		int comp = getCompOper();
		switch (comp) {
		case EQUAL:
			result = (resultSize == getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_equal"); //$NON-NLS-1$
			break;
		case NOTEQUAL:
			result = (resultSize != getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_notequal"); //$NON-NLS-1$
			break;
		case GREATERTHAN:
			result = (resultSize > getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greater"); //$NON-NLS-1$
			break;
		case LESSTHAN:
			result = (resultSize < getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_less"); //$NON-NLS-1$
			break;
		case GREATERTHANEQUAL:
			result = (resultSize >= getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greaterequal"); //$NON-NLS-1$
			break;
		case LESSTHANEQUAL:
			result = (resultSize <= getAllowedSize());
			comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_lessequal"); //$NON-NLS-1$
			break;
		}
		return result;
	}
}
