package org.apache.jmeter.engine.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.*;

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
