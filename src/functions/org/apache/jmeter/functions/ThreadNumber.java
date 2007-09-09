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

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

/**
 * TODO: should this extend AbstractFunction?
 */
public class ThreadNumber implements Function, Serializable {

	private static final String KEY = "__threadNum"; //$NON-NLS-1$

	private static final List desc = new LinkedList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
		return Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public void setParameters(Collection parameters) throws InvalidVariableException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}
}
