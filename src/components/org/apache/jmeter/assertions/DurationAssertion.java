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
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Checks if an Sample is sampled within a specified time-frame. If the duration
 * is larger than the timeframe the Assertion is considered a failure.
 * 
 */
public class DurationAssertion extends AbstractScopedAssertion implements Serializable, Assertion {
    private static final long serialVersionUID = 241L;

    /** Key for storing assertion-information in the jmx-file. */
    public static final String DURATION_KEY = "DurationAssertion.duration"; // $NON-NLS-1$

    /**
     * Returns the result of the Assertion. Here it checks whether the Sample
     * took to long to be considered successful. If so an AssertionResult
     * containing a FailureMessage will be returned. Otherwise the returned
     * AssertionResult will reflect the success of the Sample.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        long duration=getAllowedDuration();
        if (duration > 0) {
            long responseTime=response.getTime();
            // has the Sample lasted too long?
            if ( responseTime > duration ) {
                result.setFailure(true);
                Object[] arguments = { Long.valueOf(responseTime), Long.valueOf(duration) };
                String message = MessageFormat.format(
                        JMeterUtils.getResString("duration_assertion_failure") // $NON-NLS-1$
                        , arguments);
                result.setFailureMessage(message);
            }
        }
        return result;
    }

    /**
     * Returns the duration to be asserted. A duration of 0 indicates this
     * assertion is to be ignored.
     */
    private long getAllowedDuration() {
        return getPropertyAsLong(DURATION_KEY);
    }
    

    /**
     * Set duration
     * @param duration Duration in millis
     */
    public void setAllowedDuration(long duration) {
        setProperty(DURATION_KEY, duration);
    }
}
