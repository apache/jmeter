package org.apache.jmeter.threads;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.samplers.*;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class JMeterContext {
	
	JMeterVariables variables;
	SampleResult previousResult;
	Sampler currentSampler;
	
	JMeterContext() {
		variables = null;
		previousResult = null;
		currentSampler = null;
	}
	
	
	public JMeterVariables getVariables() {
		return variables;
	}
	
	void setVariables( JMeterVariables vars ) {
		this.variables = vars;
	}
	
	
	public SampleResult getPreviousResult() {
		return previousResult;
	}
	
	void setPreviousResult( SampleResult result ) {
		this.previousResult = result;
	}
	
	
	public Sampler getCurrentSampler() {
		return currentSampler;
	}
	
	void setCurrentSampler( Sampler sampler ) {
		this.currentSampler = sampler;
	}
	

}
