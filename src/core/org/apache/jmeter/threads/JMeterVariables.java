package org.apache.jmeter.threads;

import java.util.HashMap;
import java.util.Map;

/**
 * @author default
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class JMeterVariables {
	private static Map iterationData = new HashMap();
	
	public String getThreadName()
	{
		return Thread.currentThread().getName();
	}
	
	public int getIteration()
	{
		int[] a = (int[])iterationData.get(getThreadName());
		return a[0];
	}
	
	public void incIteration()
	{
		int[] a = (int[])iterationData.get(getThreadName());
		if(a == null)
		{
			a = new int[1];
			a[0] = 0;
			iterationData.put(getThreadName(),a);
		}
		a[0]++;
	}
	
	public static void initialize()
	{
		iterationData.clear();
	}

}