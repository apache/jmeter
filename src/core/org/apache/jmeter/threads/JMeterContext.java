package org.apache.jmeter.threads;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

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
    boolean samplingStarted;
    private int threadNum;
	
	JMeterContext() {
		variables = null;
		previousResult = null;
		currentSampler = null;
        samplingStarted = false;
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

    /**
     * Returns the threadNum.
     * @return int
     */
    public int getThreadNum()
    {
        return threadNum;
    }

    /**
     * Sets the threadNum.
     * @param threadNum The threadNum to set
     */
    public void setThreadNum(int threadNum)
    {
        this.threadNum = threadNum;
    }

    /**
     * @return
     */
    public boolean isSamplingStarted()
    {
        return samplingStarted;
    }

    /**
     * @param b
     */
    public void setSamplingStarted(boolean b)
    {
        samplingStarted = b;
    }

}
