package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class IterationCounter extends AbstractFunction
{
	private static int counter;
	private static final List desc = new LinkedList();
	private boolean perThread = true;
	
	static
	{
		desc.add(JMeterUtils.getResString("iteration_counter_arg_1"));
		desc.add(JMeterUtils.getResString("function_name_param"));
	}
	
	private static final String KEY = "__counter";
	private String trueCount;
	private String falseCount;
	
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
	public String execute(SampleResult previousResult, Sampler currentSampler)
		throws InvalidVariableException {
		counter++;
		JMeterVariables vars = getVariables();
		String falseCounterString = Integer.toString(counter);
		String trueCounterString = Integer.toString(vars.getIteration());
		vars.put(trueCount,trueCounterString);
		vars.put(falseCount,falseCounterString);
		
		if(perThread)
		{
			return trueCounterString;
		}
		else
		{
			return falseCounterString;
		}
	}

	/**
	 * @see org.apache.jmeter.functions.Function#setParameters(String)
	 */
	public void setParameters(String parameters)
		throws InvalidVariableException {
			Collection params = this.parseArguments(parameters);
			String[] values = (String[])params.toArray(new String[0]);
			perThread = new Boolean(values[0]).booleanValue();
			if(values.length > 1)
			{
				trueCount = values[1]+"_true";
				falseCount = values[1]+"_false";
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
