package org.apache.jmeter.testelement;

import org.apache.jmeter.threads.JMeterVariables;

/**
 * Used to pass messages to test elements that are interested in information
 * unique to a particular test thread, such as when new iterations through the
 * test plan are begun, and access to thread-level variable values.
 */
public interface ThreadListener {
	
	/**
	 * Tells all thread listeners that a new iteration has started
	 * and passes the number of the iteration (starting with 1).
	 */
	public void iterationStarted(int iterationCount);
	
	/**
	 * Hands the JMeterVariables object for that thread to the 
	 * ThreadListener object.  The object should save the 
	 * reference for use throughout the test.
	 * <p>
	 * One thing to be aware of is that if your test element does not
	 * implement PerThreadClonable, then this method will be called 
	 * multiple times, as each thread hands off it's JMeterVariables
	 * object.  In that case, it is your job to store all instances of
	 * JMeterVariables handed to you, and keep them straight by
	 * their thread names.
	 */
	public void setJMeterVariables(JMeterVariables jmVars);
	
	

}
