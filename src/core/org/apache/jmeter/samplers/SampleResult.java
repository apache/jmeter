/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache JMeter" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache JMeter", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.format.Formatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.io.WriterTarget;

/**
 * This is a nice packaging for the various information returned from taking a
 * sample of an entry.
 *
 * @author     mstover?
 * @version    $Revision$ $Date$
 */
public class SampleResult implements Serializable
{
    /**
     * Data type value indicating that the response data is text.
     *
     * @see #getDataType
     * @see #setDataType(java.lang.String)
     */
    public final static String TEXT = "text";

    /**
     * Data type value indicating that the response data is binary.
     *
     * @see #getDataType
     * @see #setDataType(java.lang.String)
     */
    public final static String BINARY = "bin";

	/* empty array which can be returned instead of null */
	private static final byte [] EMPTY_BA = new byte [0];

    private byte[] responseData;
    private String responseCode;
    private String label;
    private String samplerData;
    private String threadName;
    private String responseMessage="";
    private String responseHeaders=""; // Never return null
    private String contentType;
    private String requestHeaders="";
    private long timeStamp = 0;// currently the end time stamp
    private long startTime = 0;
    private long endTime = 0;
    private long idleTime = 0;// Allow for non-sample time
    private long pauseTime = 0;// Start of pause (if any)
    private List assertionResults;
    private List subResults;
    private String dataType;
    private boolean success;
    private Set files;
    private String dataEncoding;
    private long time;
    private boolean stopThread = false; //Should thread terminate?
	private boolean stopTest = false;   //Should test terminate?

    private final static String TOTAL_TIME = "totalTime";

    transient private static Logger log = LoggingManager.getLoggerForClass();

    public void setMarked(String filename)
    {
        if (files == null)
        {
            files = new HashSet();
        }
        files.add(filename);
    }

    public boolean isMarked(String filename)
    {
        return files != null && files.contains(filename);
    }

    public String getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(String code)
    {
        responseCode = code;
    }

    public String getResponseMessage()
    {
        return responseMessage;
    }

    public void setResponseMessage(String msg)
    {
        responseMessage = msg;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getSampleLabel()
    {
        return label;
    }

    public void setSampleLabel(String label)
    {
        this.label = label;
    }

    public void addAssertionResult(AssertionResult assertResult)
    {
        if (assertionResults == null)
        {
            assertionResults = new ArrayList();
        }
        assertionResults.add(assertResult);
    }

    /**
     * Gets the assertion results associated with this sample.
     *
     * @return an array containing the assertion results for this sample.
     *         Returns null if there are no assertion results.
     */
    public AssertionResult[] getAssertionResults()
    {
        if (assertionResults == null)
        {
            return null;
        }
        return (AssertionResult[]) assertionResults.toArray(
            new AssertionResult[0]);
    }

    public void addSubResult(SampleResult subResult)
    {
        if (subResults == null)
        {
            subResults = new ArrayList();
        }
        subResults.add(subResult);
    }

    /**
     * Gets the subresults associated with this sample.
     *
     * @return an array containing the subresults for this sample. Returns
     *         null if there are no subresults.
     */
    public SampleResult[] getSubResults()
    {
        if (subResults == null)
        {
            return null;
        }
        return (SampleResult[]) subResults.toArray(new SampleResult[0]);
    }

    public void configure(Configuration info)
    {
        setTime(info.getAttributeAsLong(TOTAL_TIME, 0L));
    }

    /**
     * Set the time this sample took to occur.
     */
    public void setTime(long t)
    {
        time = t;
    }

    /**
     * Sets the responseData attribute of the SampleResult object.
     *
     * @param  response  the new responseData value
     */
    public void setResponseData(byte[] response)
    {
        responseData = response;
    }

    /**
     * Gets the responseData attribute of the SampleResult object.
     *
     * @return    the responseData value
     */
    public byte[] getResponseData()
    {
        return responseData;
    }

	/**
	 * Convenience method to get responseData as a non-null byte array
	 * 
	 * @return the responseData. If responseData is null
	 * then an empty byte array is returned rather than null.
	 * 
	 */
	public byte [] responseDataAsBA()
	{
		return responseData == null ? EMPTY_BA : responseData;
	}

    public void setSamplerData(String s)
    {
        samplerData = s;
    }

    public String getSamplerData()
    {
        return samplerData;
    }

    /**
     * Get the time it took this sample to occur.
     */
    public long getTime()
    {
        return time;
    }

    public boolean isSuccessful()
    {
        return success;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getDataType()
    {
        return dataType;
    }

    /**
     * Sets the successful attribute of the SampleResult object.
     *
     * @param  success  the new successful value
     */
    public void setSuccessful(boolean success)
    {
        this.success = success;
    }

    /**
     * Returns the display name.
     *
     * @return    display name of this sample result
     */
    public String toString()
    {
        return getSampleLabel();
    }
    
    /**
     * Returns the dataEncoding.
     */
    public String getDataEncoding()
    {
        if (dataEncoding != null)
        {
            return dataEncoding;
        }
        else
        {
            return "8859-1";
        }
    }

    /**
     * Sets the dataEncoding.
     * @param dataEncoding the dataEncoding to set
     */
    public void setDataEncoding(String dataEncoding)
    {
        this.dataEncoding = dataEncoding;
    }
    /**
     * @return whether to stop the test
     */
    public boolean isStopTest()
    {
        return stopTest;
    }

    /**
     * @return whether to stop this thread
     */
    public boolean isStopThread()
    {
        return stopThread;
    }

    /**
     * @param b
     */
    public void setStopTest(boolean b)
    {
        stopTest = b;
    }

    /**
     * @param b
     */
    public void setStopThread(boolean b)
    {
        stopThread = b;
    }

    /**
     * @return the request headers
     */
    public String getRequestHeaders()
    {
        return requestHeaders;
    }

    /**
     * @return the response headers
     */
    public String getResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * @param string - request headers
     */
    public void setRequestHeaders(String string)
    {
        requestHeaders = string;
    }

    /**
     * @param string - response headers
     */
    public void setResponseHeaders(String string)
    {
        responseHeaders = string;
    }

    /**
     * @return the content type - text or bin
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @param string
     */
    public void setContentType(String string)
    {
        contentType = string;
    }

    /**
     * Record the start time of a sample
     *
     */
    public void sampleStart()
    {
    	if (startTime == 0){
			startTime = System.currentTimeMillis();
    	} else {
			log.error("sampleStart called twice", new Throwable("Invalid call sequence"));
    	}
    }
    
	/**
	 * Record the end time of a sample and calculate the elapsed time
	 *
	 */
    public void sampleEnd()
    {
    	if (endTime == 0){
			endTime = System.currentTimeMillis();
			time = endTime - startTime - idleTime;
    	} else {
    		log.error("sampleEnd called twice", new Throwable("Invalid call sequence"));
    	}
    }

    /**
     * Pause a sample
     *
     */
	public void samplePause()
	{
		if (pauseTime != 0) {
			log.error("samplePause called twice",new Throwable("Invalid call sequence"));
		} 
		pauseTime = System.currentTimeMillis();
	}
	
	/**
	 * Resume a sample
	 *
	 */
	public void sampleResume()
	{
		if (pauseTime == 0) {
			log.error("sampleResume without samplePause",new Throwable("Invalid call sequence"));
		}
		idleTime += System.currentTimeMillis() - pauseTime;
		pauseTime=0;
	}
	
////////////////////////////// Start of Test Code ///////////////////////////

    public static class Test extends TestCase
    {
    	public Test(String name)
    	{
    		super(name);
    	}
    	
    	public void testElapsed() throws Exception
    	{
    		SampleResult res = new SampleResult();

    		// Check sample increments OK
    		res.sampleStart();
    		Thread.sleep(100);
			res.sampleEnd();
    	}

		public void testPause() throws Exception
		{
			SampleResult res = new SampleResult();
			// Check sample increments OK
			res.sampleStart();
			Thread.sleep(100);
			res.samplePause();

			Thread.sleep(200);
			
			// Re-increment
			res.sampleResume();
			Thread.sleep(100);
			res.sampleEnd();
			assertTrue(res.time  >= 200);
			assertFalse(res.time >= 210); // we hope!
		}

		private static Formatter fmt=new RawFormatter();
        private StringWriter wr = null;
        
        public void divertLog()
        {
			wr=new StringWriter(1000);
			LogTarget [] lt = {new WriterTarget(wr,fmt)};
			log.setLogTargets(lt);
        }
        
		public void testPause2() throws Exception
		{
			divertLog();
			SampleResult res = new SampleResult();
			res.sampleStart();
			res.samplePause();
			assertTrue(wr.toString().length()==0);
			res.samplePause();
			assertFalse(wr.toString().length()==0);
		}
        // TODO some more invalid sequence tests needed
    }
}
