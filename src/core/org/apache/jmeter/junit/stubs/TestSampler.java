/*
 * Created on Apr 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.junit.stubs;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestSampler extends AbstractSampler
{

    /* (non-Javadoc)
     * @see 
org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    public SampleResult sample(Entry e)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TestSampler(String name)
    {
        setName(name);
    }

    public TestSampler()
    {}
    
    public String toString()
    {
        return getName();
    }

}

