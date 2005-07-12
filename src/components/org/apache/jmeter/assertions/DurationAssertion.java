// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import java.text.MessageFormat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Checks if an Sample is sampled within a specified time-frame. If the duration
 * is larger than the timeframe the Assertion is considered a failure.
 * 
 * @author <a href="mailto:wolfram.rittmeyer@web.de">Wolfram Rittmeyer</a>
 * @version $Revision$, $Date$
 */
public class DurationAssertion extends AbstractTestElement implements Serializable, Assertion {
	/** Key for storing assertion-informations in the jmx-file. */
	private static final String DURATION_KEY = "DurationAssertion.duration";

	/**
	 * Returns the result of the Assertion. Here it checks wether the Sample
	 * took to long to be considered successful. If so an AssertionResult
	 * containing a FailureMessage will be returned. Otherwise the returned
	 * AssertionResult will reflect the success of the Sample.
	 */
	public AssertionResult getResult(SampleResult response) {
		AssertionResult result = new AssertionResult();
		result.setFailure(false);
		// has the Sample lasted to long?
		if (((response.getTime() > getAllowedDuration()) && (getAllowedDuration() > 0))) {
			result.setFailure(true);
			Object[] arguments = { new Long(response.getTime()), new Long(getAllowedDuration()) };
			String message = MessageFormat.format(JMeterUtils.getResString("duration_assertion_failure"), arguments);
			result.setFailureMessage(message);
		}
		return result;
	}

	/**
	 * Returns the duration to be asserted. A duration of 0 indicates this
	 * assertion is to be ignored.
	 */
	public long getAllowedDuration() {
		return getPropertyAsLong(DURATION_KEY);
	}

	/**
	 * Set the duration that shall be asserted.
	 * 
	 * @param duration
	 *            a period of time in milliseconds. Is not allowed to be
	 *            negative. Use Double.MAX_VALUE to indicate illegal or empty
	 *            inputs. This will result to not checking the assertion.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>duration</code> is negative.
	 */
	public void setAllowedDuration(long duration) throws IllegalArgumentException {
		if (duration < 0L) {
			throw new IllegalArgumentException(JMeterUtils.getResString("argument_must_not_be_negative"));
		}
		if (duration == Long.MAX_VALUE) {
			setProperty(new LongProperty(DURATION_KEY, 0));
		} else {
			setProperty(new LongProperty(DURATION_KEY, duration));
		}
	}
}