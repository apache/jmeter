/*
 * Created on 24/02/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.jmeter.examples.testbeans.example2;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;

/**
 * This TestBean is just an example about how to write testbeans. The intent is to demonstrate
 * usage of the TestBean features to podential TestBean developers. Note that only the class's
 * introspector view matters: the methods do nothing -- nothing useful, in any case.
 */
public class Example2 extends TestBean implements Sampler {
	public SampleResult sample(Entry e) {
		return new SampleResult();
	}
	
	// A TestBean is a Java Bean. Just define some properties and they will
	// autmagically show up in the GUI.
	// A String property:
	public void setMyStringProperty(String s)
	{};
	public String getMyStringProperty()
	{return "";}
}
