/*
 * Copyright 2001-2004, 2006 The Apache Software Foundation.
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
import org.apache.jmeter.util.JMeterUtils;

public class IterationCounter extends AbstractFunction implements Serializable {

	private static final List desc = new LinkedList();

	private static final String KEY = "__counter";

    private transient ThreadLocal perThreadInt = new ThreadLocal(){
        protected synchronized Object initialValue() {
            return new Integer(0);
        }
    };
    
	static {
		desc.add(JMeterUtils.getResString("iteration_counter_arg_1")); //$NON-NLS-1$
		desc.add(JMeterUtils.getResString("function_name_param")); //$NON-NLS-1$
	}

	transient private Object[] variables;

	transient private int globalCounter;//MAXINT = 2,147,483,647

	public IterationCounter() {
        globalCounter=0;
    }

	public Object clone() {
		IterationCounter newCounter = new IterationCounter();
		newCounter.globalCounter = globalCounter;
		return newCounter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

        new Integer(1);
        globalCounter++;

		JMeterVariables vars = getVariables();

		boolean perThread = Boolean.valueOf(((CompoundVariable) variables[0]).execute()).booleanValue();

		String varName = ""; //$NON-NLS-1$
        if (variables.length >=2) {// Ensure variable has been provided
            varName = ((CompoundVariable) variables[1]).execute();
        }
		
        String counterString = ""; //$NON-NLS-1$

		if (perThread) {
			int threadCounter;
            threadCounter = ((Integer) perThreadInt.get()).intValue() + 1;                
            perThreadInt.set(new Integer(threadCounter));
			counterString = String.valueOf(threadCounter);
		} else {
			counterString = String.valueOf(globalCounter);
		}

		if (varName.length() > 0)
			vars.put(varName, counterString);
		return counterString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public void setParameters(Collection parameters) throws InvalidVariableException {

		variables = parameters.toArray();

		if (variables.length < 1) {
			throw new InvalidVariableException("Need at least 1 parameter");
		}
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
