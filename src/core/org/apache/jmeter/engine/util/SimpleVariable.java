package org.apache.jmeter.engine.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class SimpleVariable {

	private static final String KEY = "__unknownFunction";
	private Map varMap = new HashMap();
	private String name;
	
	public SimpleVariable(String name)
	{
		this.name = name;
	}
	
	public SimpleVariable()
	{
		this.name = "";
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public String toString()
	{
		String ret = null;
		JMeterVariables vars = getVariables();
		
		if ( vars != null )
			ret = vars.get(name);
		if( ret == null  || ret.length() == 0 )
			return "${"+name+"}";

		return ret;
	}
	
	private JMeterVariables getVariables()
	{
		JMeterContext context = JMeterContextService.getContext();
		return context.getVariables();
	}

}
