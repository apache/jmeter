package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class VariablesCollection implements Serializable {
	private Map varMap = new HashMap();
	
	public void addJMeterVariables(JMeterVariables jmVars)
	{
		varMap.put(Thread.currentThread().getName(),jmVars);
	}
	
	public JMeterVariables getVariables()
	{
		return (JMeterVariables)varMap.get(Thread.currentThread().getName());
	}

}
