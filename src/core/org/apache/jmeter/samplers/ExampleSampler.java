/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.samplers;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Example Sampler (non-Bean version)
 * 
 * JMeter creates an instance of a sampler class for every
 * occurrence of the element in every thread.
 * [some additional copies may be created before the test run starts]
 *
 * Thus each sampler is guaranteed to be called by a single thread -
 * there is no need to synchronize access to instance variables.
 * 
 * However, access to class fields must be synchronized.
 *  
 * @author sebb AT apache DOT org
 * @version $revision$ $date$
 */
public class ExampleSampler extends AbstractSampler
{

	protected static Logger log = LoggingManager.getLoggerForClass();

    // The name of the property used to hold our data
	public final static String DATA = "ExampleSampler.data";   //$NON-NLS-1$
	
	private transient static int classCount=0; // keep track of classes created
	// (for instructional purposes only!)
	
	public ExampleSampler()
	{
		classCount++;
		trace("ExampleSampler()");
	}

    /* (non-Javadoc)
     * Performs the sample, and returns the result
     * 
     * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
     */
    public SampleResult sample(Entry e)
    {
    	trace("sample()");
		SampleResult res = new SampleResult();
		boolean isOK = false; // Did sample succeed?
		String data=getData(); // Sampler data
		String response=null;
		
		res.setSampleLabel(getTitle());
		/*
		 * Perform the sampling
		 */
		res.sampleStart(); //Start timing
		try {
			
			// Do something here ...
			
			response=Thread.currentThread().getName();
			
			/*
			 * Set up the sample result details
			 */
			res.setSamplerData(data);
			res.setResponseData(response.getBytes());
			res.setDataType(SampleResult.TEXT);
			
			res.setResponseCode("200");
			res.setResponseMessage("OK");
			isOK = true;
		}
		catch (Exception ex){
			log.debug("",ex);
			res.setResponseCode("500");
			res.setResponseMessage(ex.toString());
		}
		res.sampleEnd(); //End timimg
		
		res.setSuccessful(isOK);

        return res;
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle()
    {
        return this.getName();
    }
    
    /**
     * @return the data for the sample
     */
    public String getData()
    {
    	return getPropertyAsString(DATA);
    }
    
    /*
     * Helper method
     */
	private void trace(String s)
	{
		String tl = getTitle();
		String tn = Thread.currentThread().getName();
		String th = this.toString();
		log.debug(tn+" ("+classCount+") "+tl+" "+s+" "+th);
	}
}