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

public class IterationCounter extends AbstractFunction implements Serializable
{

    private static final List desc = new LinkedList();
    private static final String KEY = "__counter";

    static {
        desc.add(JMeterUtils.getResString("iteration_counter_arg_1"));
        desc.add(JMeterUtils.getResString("function_name_param"));
    }

    transient private Object[] variables;
    transient private int[] counter;
    transient private String key; // Used to keep track of counter

    public IterationCounter()
    {
        counter = new int[1];
    	// TODO use better key if poss. Can't use varName - it may not be present
        key=KEY+System.identityHashCode(this);
    }

    public Object clone()
    {
        IterationCounter newCounter = new IterationCounter();
        newCounter.counter = counter;
        return newCounter;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public synchronized String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException
    {
        counter[0]++;

        JMeterVariables vars = getVariables();

        boolean perThread =
            Boolean.valueOf(((CompoundVariable) variables[0]).execute()).booleanValue();

        String varName =
            ((CompoundVariable) variables[variables.length - 1]).execute();
        String counterString = "";

        if (perThread)
        {
        	counterString = vars.get(key);
        	if (null==counterString){
        		counterString= "1";
        	}
        	else
        	{
        		counterString = Integer.toString(Integer.parseInt(counterString)+1);
        	}
        	vars.put(key,counterString);
        	
        }
        else
        {
            counterString = String.valueOf(counter[0]);
        }

        if (varName.length()>0) vars.put(varName, counterString);
        return counterString;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {

        variables = parameters.toArray();

        if (variables.length < 2)
        {
            throw new InvalidVariableException("Fewer than 2 parameters");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc()
    {
        return desc;
    }
}
