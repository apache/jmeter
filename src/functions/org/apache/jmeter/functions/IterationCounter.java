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

/**
 * @author default
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IterationCounter extends AbstractFunction implements Serializable
{
	
	private static final List desc = new LinkedList();
	private static final String KEY = "__counter";
	
	static
	{
		desc.add(JMeterUtils.getResString("iteration_counter_arg_1"));
		desc.add(JMeterUtils.getResString("function_name_param"));
	}
	
	private Object[] variables;
	private static int counter;


	public IterationCounter()
	{
		counter = 0;
	}
	
	public Object clone()
	{
		IterationCounter newCounter = new IterationCounter();
		return newCounter;
	}

	/**
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
		throws InvalidVariableException 
	{
		counter++;

		JMeterVariables vars = getVariables();
		
		boolean perThread = new Boolean(((CompoundVariable)variables[0]).execute()).booleanValue();

		String varName = ((CompoundVariable)variables[variables.length - 1]).execute();
		String counterString = "";

		if(perThread)
		{
			counterString = Integer.toString(vars.getIteration());			
		}
		else
		{
			counterString = Integer.toString(counter);
		}
		
		vars.put( varName, counterString );
		return counterString;
	}


	/**
	 * @see org.apache.jmeter.functions.Function#setParameters(String)
	 */
	public void setParameters(Collection parameters)
		throws InvalidVariableException {

			variables = parameters.toArray();
			
			if ( variables.length < 2 ) {
				throw new InvalidVariableException();
			}
	}

	/**
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}

	/**
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}


}
