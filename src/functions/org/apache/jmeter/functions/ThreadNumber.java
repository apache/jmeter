package org.apache.jmeter.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

public class ThreadNumber implements Function,Serializable {
	
	transient private JMeterVariables vars;
	private static final String KEY = "__threadNum";

    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public String execute(SampleResult previousResult, Sampler currentSampler)
		throws InvalidVariableException {
		return Thread.currentThread().getName().substring(
				Thread.currentThread().getName().indexOf("-")+1);
	}

    /* This method no longer appears to be in use.
     * jeremy_a@bigfoot.com  03 May 2003
     * 
	 * @see org.apache.jmeter.functions.Function#setParameters(String)
	public void setParameters(String parameters)
		throws InvalidVariableException {
	}
     */
	
	
    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public void setParameters(Collection parameters)
		throws InvalidVariableException {
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return new LinkedList();
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.functions.Function#setJMeterVariables(JMeterVariables)
	 */
	public void setJMeterVariables(JMeterVariables jmv) {
		vars = jmv;
	}

}
