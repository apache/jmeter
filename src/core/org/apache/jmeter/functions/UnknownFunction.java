package org.apache.jmeter.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class UnknownFunction implements Function {
	
	private static final String KEY = "__unknownFunction";
	private Map varMap = new HashMap();
	private String name;
	
	public UnknownFunction(String name)
	{
		this.name = name;
	}
	
	public UnknownFunction()
	{
		this.name = "";
	}

	/**
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public String execute(SampleResult previousResult, Sampler currentSampler)
		throws InvalidVariableException {
		String ret = getVariables().get(name);
		if(ret == null)
		{
			return "${"+name+"}";
		}
		return ret;
	}
	
	private JMeterVariables getVariables()
	{
		return (JMeterVariables)varMap.get(Thread.currentThread().getName());
	}

	/**
	 * @see org.apache.jmeter.functions.Function#setParameters(String)
	 */
	public void setParameters(String parameters)
		throws InvalidVariableException {
			
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
		return null;
	}

	/**
	 * @see org.apache.jmeter.functions.Function#setJMeterVariables(JMeterVariables)
	 */
	public void setJMeterVariables(JMeterVariables jmv) {
		varMap.put(Thread.currentThread().getName(),jmv);
	}

}
