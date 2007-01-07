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

import java.util.Collection;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * Provides common methods for all functions
 */
public abstract class AbstractFunction implements Function {

	/**
	 * @see Function#execute(SampleResult, Sampler)
	 */
	abstract public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException;

	public String execute() throws InvalidVariableException {
		JMeterContext context = JMeterContextService.getContext();
		SampleResult previousResult = context.getPreviousResult();
		Sampler currentSampler = context.getCurrentSampler();
		return execute(previousResult, currentSampler);
	}

	/**
	 * @see Function#setParameters(Collection)
     * Note: This may not be called (e.g. if no parameters are provided)
     * 
	 */
	abstract public void setParameters(Collection parameters) throws InvalidVariableException;

	/**
	 * @see Function#getReferenceKey()
	 */
	abstract public String getReferenceKey();

	protected JMeterVariables getVariables() {
		return JMeterContextService.getContext().getVariables();
	}
    
    /*
     * Utility method to check parameter counts 
     */
    protected void checkParameterCount(Collection parameters, int min, int max) 
        throws InvalidVariableException
    {
        int num = parameters.size();
        if ((num > max) || (num < min)) {
            throw new InvalidVariableException(
                    "Wrong number of parameters. Actual: "+num+
                    ". Expected: >= "+min
                    +" and <= "+max
                    );
        }
    }
}
