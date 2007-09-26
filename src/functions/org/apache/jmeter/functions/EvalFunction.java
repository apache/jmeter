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

// @see PackageTest for unit tests

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Function to evaluate a string which may contain variable or function references.
 * 
 * Parameter: string to be evaluated
 * 
 * Returns: the evaluated value 
 * 
 */
public class EvalFunction extends AbstractFunction implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final List desc = new LinkedList();

	private static final String KEY = "__eval"; //$NON-NLS-1$

	// Number of parameters expected - used to reject invalid calls
	private static final int MIN_PARAMETER_COUNT = 1;
	private static final int MAX_PARAMETER_COUNT = 1;
	
	static {
		desc.add(JMeterUtils.getResString("eval_name_param")); //$NON-NLS-1$
	}

	private Object[] values;

	public EvalFunction() {
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		String parameter = ((CompoundVariable) values[0]).execute();
		CompoundVariable cv = new CompoundVariable(parameter);
		return cv.execute();
	}

	public synchronized void setParameters(Collection parameters) throws InvalidVariableException {

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