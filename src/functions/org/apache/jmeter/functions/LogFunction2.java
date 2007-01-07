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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Function to log a message
 * 
 * Parameters: - string - log level (optional; defaults to INFO; or DEBUG if
 * unrecognised) - throwable message (optional)
 * 
 * Returns: - Empty String (so can be used where return value would be a
 * nuisance)
 * 
 * @version $Revision$ Updated: $Date$
 */
public class LogFunction2 extends AbstractFunction implements Serializable {
	private static Logger log = LoggingManager.getLoggerForClass();

	private static final List desc = new LinkedList();

	private static final String KEY = "__logn";

	// Number of parameters expected - used to reject invalid calls
	private static final int MIN_PARAMETER_COUNT = 1;

	private static final int MAX_PARAMETER_COUNT = 3;
	static {
		desc.add("String to be logged");
		desc.add("Log level (default INFO)");
		desc.add("Throwable text (optional)");
	}

	private static final String DEFAULT_PRIORITY = "INFO"; //$NON-NLS-1$

	private Object[] values;

	public LogFunction2() {
	}

	public Object clone() {
		return new LogFunction2();
	}

	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		String stringToLog = ((CompoundVariable) values[0]).execute();

		String priorityString;
		if (values.length > 1) { // We have a default
			priorityString = ((CompoundVariable) values[1]).execute();
			if (priorityString.length() == 0)
				priorityString = DEFAULT_PRIORITY;
		} else {
			priorityString = DEFAULT_PRIORITY;
		}

		Throwable t = null;
		if (values.length > 2) { // Throwable wanted
			t = new Throwable(((CompoundVariable) values[2]).execute());
		}

		LogFunction.logDetails(log, stringToLog, priorityString, t);

		return "";

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