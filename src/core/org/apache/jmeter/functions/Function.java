package org.apache.jmeter.functions;

import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface Function {
	
	/**
	 * Given the previous SampleResult and the current Sampler, return
	 * a string to use as a replacement value for the function call.
	 * Assume "setParameter" was previously called.
	 * 
	 * This method must be threadsafe - multiple threads will be using
	 * the same object.
	 */
	public String execute(SampleResult previousResult,Sampler currentSampler)
			throws InvalidVariableException;
	
	/**
	 * A string representing a comma-delimited list of URLEncoded arguments.
	 * You will have to split on the comma, and decode each argument.
	 * Extend AbstractFunction to gain access to a convenience method
	 * that does that for you.  These parameters are used to "configure"
	 * your function.
	 */
	public void setParameters(String parameters) throws InvalidVariableException;
	
	/**
	 * Return the name of your function.  Convention is to prepend "__"
	 * to the name (ie "__regexFunction")
	 */
	public String getReferenceKey();
	
	/**
	 * Return a list of strings briefly describing each parameter
	 * your function takes.  Please use JMeterUtils.getResString(resource_name)
	 * to grab a resource string.  Otherwise, your help text will be
	 * difficult to internationalize.  Add your strings to all
	 * org.apache.jmeter.resources.*.properties files.  Do not worry
	 * about translating - that's someone else's responsibility.
	 * 
	 * This list is not optional.  If you don't wish to write help, you
	 * must at least return a List containing the correct number of
	 * blank strings, one for each argument.
	 */
	public List getArgumentDesc();
	
	/**
	 * A means of giving the function access to built-in JMeter data
	 * such as iteration number, current thread name, etc
	 */
	public void setJMeterVariables(JMeterVariables jmv);
}
