package org.apache.jmeter.modifiers;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.VariablesCollection;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class UserParameters
	extends ConfigTestElement
	implements Serializable, ThreadListener {
		
		private static final String NAMES = "UserParameters.names";
		private static final String THREAD_VALUES = "UserParameters.thread_values";

	VariablesCollection vars = new VariablesCollection();
	int counter = 0;
	Iterator threadValues;
	/**
	 * @see org.apache.jmeter.config.Modifier#modifyEntry(Sampler)
	 */
	public boolean modifyEntry(Sampler Sampler) {
		return false;
	}
	
	public void iterationStarted(int iter)
	{
		if(iter == 1)
		{
			setVariables();
		}
	}
	
	public void setJMeterVariables(JMeterVariables vars)
	{
		this.vars.addJMeterVariables(vars);
	}
	
	public List getNames()
	{
		return (List)getProperty(NAMES);
	}
	
	public List getThreadLists()
	{
		return (List)getProperty(THREAD_VALUES);
	}	
	
	/**
	 * The list of names of the variables to hold values.  This list must come in
	 * the same order as the sub lists that are given to setThreadLists(List).
	 */
	public void setNames(List list)
	{
		setProperty(NAMES,list);
	}
	
	/**
	 * The thread list is a list of lists.  Each list within the parent list is a
	 * collection of values for a simulated user.  As many different sets of 
	 * values can be supplied in this fashion to cause JMeter to set different 
	 * values to variables for different test threads.
	 */
	public void setThreadLists(List threadLists)
	{
		setProperty(THREAD_VALUES,threadLists);
	}
	
	private synchronized List getValues()
	{
		if(threadValues == null || !threadValues.hasNext())
		{
			threadValues = ((List)getProperty(THREAD_VALUES)).iterator();
		}
		if(threadValues.hasNext())
		{
			return (List)threadValues.next();
		}
		else
		{
			return new LinkedList();
		}
	}	
		
	
	private void setVariables()
	{
		Iterator namesIter = getNames().iterator();
		Iterator valueIter = getValues().iterator();
		JMeterVariables jmvars = vars.getVariables();
		while(namesIter.hasNext() && valueIter.hasNext())
		{
			String name = (String)namesIter.next();
			String value = (String)valueIter.next();
			jmvars.put(name,value);
		}
	}

}
