package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.PerThreadClonable;
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
	private JMeterVariables vars;
	private static int counter;
	private static final List desc = new LinkedList();
	private boolean perThread = true;
	
	static
	{
		desc.add(JMeterUtils.getResString("iteration_counter_arg_1"));
	}
	
	private static final String KEY = "__counter";
	
	public IterationCounter()
	{
		counter = 1;
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
		if(perThread)
		{
			return Integer.toString(vars.getIteration());
		}
		else
		{
			return Integer.toString(counter++);
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

	/**
	 * @see org.apache.jmeter.functions.Function#setJMeterVariables(JMeterVariables)
	 */
	public void setJMeterVariables(JMeterVariables jmv) {
		if(vars == null)
		{
			vars = jmv;
		}
	}

}
