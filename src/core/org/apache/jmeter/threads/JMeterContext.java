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
    Sampler previousSampler;
	
	JMeterContext() {
		variables = null;
		previousResult = null;
		currentSampler = null;
	}
	
	
	public JMeterVariables getVariables() {
		return variables;
	}
	
	public void setVariables( JMeterVariables vars ) {
		this.variables = vars;
	}
	
	
	public SampleResult getPreviousResult() {
		return previousResult;
	}
	
	public void setPreviousResult( SampleResult result ) {
		this.previousResult = result;
	}
	
	
	public Sampler getCurrentSampler() {
		return currentSampler;
	}
	
	public void setCurrentSampler( Sampler sampler ) {
        setPreviousSampler(currentSampler);
		this.currentSampler = sampler;
	}
	

    /**
     * Returns the previousSampler.
     * @return Sampler
     */
    public Sampler getPreviousSampler()
    {
        return previousSampler;
    }

    /**
     * Sets the previousSampler.
     * @param previousSampler The previousSampler to set
     */
    public void setPreviousSampler(Sampler previousSampler)
    {
        this.previousSampler = previousSampler;
    }

}
