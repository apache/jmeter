/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

// @see org.apache.jmeter.functions.PackageTest for unit tests

/**
 * Function to log a message
 * 
 * Parameters: - string - log level (optional; defaults to INFO; or DEBUG if
 * unrecognised) - throwable message (optional)
 * 
 * Returns: - the input string
 * 
 * @version $Revision$ Updated: $Date$
 */
public class SplitFunction extends AbstractFunction implements Serializable {
	private static Logger log = LoggingManager.getLoggerForClass();

	private static final List desc = new LinkedList();

	private static final String KEY = "__split";// $NON-NLS-1$

	// Number of parameters expected - used to reject invalid calls
	private static final int MIN_PARAMETER_COUNT = 2;

	private static final int MAX_PARAMETER_COUNT = 3;
	static {
		desc.add("String to split");
		desc.add("Variable name");
		desc.add("Split character (omit for ',')");
	}

	private Object[] values;

	public SplitFunction() {
	}

	public Object clone() {
		return new SplitFunction();
	}

	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		JMeterVariables vars = getVariables();

		String stringToSplit = ((CompoundVariable) values[0]).execute();
		String varNamePrefix = ((CompoundVariable) values[1]).execute();
		String splitString = ",";

		if (values.length > 2) { // Split string provided
			splitString = ((CompoundVariable) values[2]).execute();
		}
        if (log.isDebugEnabled()){
            log.debug("Split "+stringToSplit+ " using "+ splitString+ " into "+varNamePrefix);
        }
		String parts[] = JOrphanUtils.split(stringToSplit, splitString, "?");// $NON-NLS-1$

		vars.put(varNamePrefix, stringToSplit);
		vars.put(varNamePrefix + "_n", "" + parts.length);// $NON-NLS-1$ // $NON-NLS-2$
		for (int i = 1; i <= parts.length; i++) {
            if (log.isDebugEnabled()){
                log.debug(parts[i-1]);
            }
			vars.put(varNamePrefix + "_" + i, parts[i - 1]);// $NON-NLS-1$
		} 
		vars.remove(varNamePrefix + "_" + (parts.length+1));
		return stringToSplit;

	}

	public void setParameters(Collection parameters) throws InvalidVariableException {

		values = parameters.toArray();

		if ((values.length < MIN_PARAMETER_COUNT) || (values.length > MAX_PARAMETER_COUNT)) {
			throw new InvalidVariableException("Parameter Count not between " + MIN_PARAMETER_COUNT + " & "
					+ MAX_PARAMETER_COUNT);
		}

	}

	public String getReferenceKey() {
		return KEY;
	}

	public List getArgumentDesc() {
		return desc;
	}

}