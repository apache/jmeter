/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.text.MessageFormat;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Checks if an Sample is sampled within a specified time-frame. If the
 * duration is larger than the timeframe the Assertion is considered
 * a failure.
 *
 Copyright: Copyright (c) 2001
 * Company: Apache
 *
 * @author <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 *
 * @version $Revision$, $Date$
 */
public class SizeAssertion extends AbstractTestElement implements Serializable, Assertion {

	int comparator = 1;
	String comparatorErrorMessage = "ERROR!";
	//* Static int to signify the type of logical comparitor to assert
	public final static int EQUAL = 1;
	public final static int NOTEQUAL = 2;
	public final static int GREATERTHAN = 3;
	public final static int LESSTHAN = 4;
	public final static int GREATERTHANEQUAL = 5;
	public final static int LESSTHANEQUAL = 6;
	/** Key for storing assertion-informations in the jmx-file. */
	private static final String SIZE_KEY = "size_assertion_size";
	byte[] resultData;
	
	/**
	 * Returns the result of the Assertion. Here it checks wether the
	 * Sample took to long to be considered successful. If so an AssertionResult
	 * containing a FailureMessage will be returned. Otherwise the returned
	 * AssertionResult will reflect the success of the Sample.
	 */
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult();
		result.setFailure(false);

		// is the Sample the correct size?
		resultData = getResultBody(response.getResponseData());
		long resultSize = resultData.length;
		if ((!(compareSize(resultSize)) && (getAllowedSize() > 0))) {
			result.setFailure(true);
			Object[] arguments = { new Long(resultSize), new String(comparatorErrorMessage), new Long(getAllowedSize())};
			String message = MessageFormat.format(JMeterUtils.getResString("size_assertion_failure"), arguments);
			result.setFailureMessage(message);
		}
		return result;
	}

	/**
	 * Returns the size in bytes to be asserted. A duration of 0 indicates this assertion is to 
	 * be ignored.
	 */
	public long getAllowedSize() {
		return getPropertyAsLong(SIZE_KEY);
	}

	/**
	 * Set the size that shall be asserted.
	 *
	 * @param duration A number of bytes. Is not allowed to be negative. Use Double.MAX_VALUE to indicate illegal or empty inputs. This will result to not checking the assertion.
	 *
	 * @throws IllegalArgumentException If <code>duration</code> is negative.
	 */
	public void setAllowedSize(long size) throws IllegalArgumentException {
		if (size < 0L) {
			throw new IllegalArgumentException(JMeterUtils.getResString("argument_must_not_be_negative"));
		}
		if (size == Long.MAX_VALUE) {
			setProperty(SIZE_KEY, new Long(0));
		}
		else {
			setProperty(SIZE_KEY, new Long(size));
		}
	}

	/**
	 * Return the body of the http return.
	 *
	 * 
	 *
	 * 
	 */
	private byte[] getResultBody(byte[] resultData) {
		for (int i = 0; i < (resultData.length - 1) ; i++) {
	 		if (resultData[i] == '\n' && resultData[i+1] == '\n') {
	 			return getByteArraySlice(resultData,(i+3),resultData.length);
	 		}
		}
		return resultData;
	}
	 
	/**
	 * Return a slice of a byte array
	 *
	 * 
	 *
	 * 
	 */
	private byte[] getByteArraySlice(byte[] array, int begin, int end) {
		byte[] slice = new byte [(end - begin  + 1)];
		int count = 0;
		for (int i = begin; i < end ; i++) {
			slice[count] = array[i];
			count++;
		}
		return slice;
	}	 
	
	/**
	 * Set the type of logical comparator to assert.
	 *
	 * Possible values are:
	 * equal, not equal, 
	 * greater than, less than, 
	 * greater than eqaul, less than equal, .
	 * 
	 *@param comparator is an int value indicating logical comparator type
	 *
	 */
	public void setLogicalComparator(int comparator) {
		this.comparator = comparator;
	}
	
	/**
	 * Compares the the size of a return result to the set allowed size
	 *using a logical comparator set in setLogicalComparator().
	 *
	 * Possible values are:
	 * equal, not equal, 
	 * greater than, less than, 
	 * greater than eqaul, less than equal, .
	 * 
	 */
	
	private boolean compareSize(long resultSize) {
		boolean result = false;
		switch (comparator) 
		{
			case EQUAL: 
				result = (resultSize == getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_equal");
				break;
			case NOTEQUAL: 
				result = (resultSize != getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_notequal");
				break;
			case GREATERTHAN: 
				result = (resultSize > getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greater");
				break;
			case LESSTHAN: 
				result = (resultSize < getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_less");
				break;				
			case GREATERTHANEQUAL: 
				result = (resultSize >= getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greaterequal");
				break;
			case LESSTHANEQUAL: 
				result = (resultSize <= getAllowedSize());
				comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_lessequal");
				break;
		}
		return result;
	}
}
