// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.util.JMeterUtils;
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
 * @version    $Revision$ $Date$
 */
public class SampleResult implements Serializable
{
	// Bug 33196 - encoding ISO-8859-1 is only suitable for Western countries
	// However the suggested System.getProperty("file.encoding") is Cp1252 on Windows
	// So use a new property with the original value as default
    private static final String DEFAULT_ENCODING =
		JMeterUtils.getPropDefault("sampleresult.default.encoding","ISO-8859-1");
	
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

	/* empty arrays which can be returned instead of null */
	private static final byte [] EMPTY_BA = new byte [0];
	private static final SampleResult [] EMPTY_SR = new SampleResult[0];
	private static final AssertionResult [] EMPTY_AR = new AssertionResult[0];

    private byte[] responseData = EMPTY_BA;
    private String responseCode;
    private String label;
    private String samplerData;
    private String threadName;
    private String responseMessage="";
    private String responseHeaders=""; // Never return null
    private String contentType=""; // e.g. text/html; charset=utf-8
    private String requestHeaders="";
    private long timeStamp = 0;// the time stamp - can be start or end
    private long startTime = 0;
    private long endTime = 0;
    private long idleTime = 0;// Allow for non-sample time
    private long pauseTime = 0;// Start of pause (if any)
    private List assertionResults;
    private List subResults;
    private String dataType;
    private boolean success;
    private Set files;
    private String dataEncoding;// (is this really the character set?) e.g. ISO-8895-1, UTF-8
    private long time = 0;
    private boolean stopThread = false; //Should thread terminate?
	private boolean stopTest = false;   //Should test terminate?
	private boolean isMonitor = false;
	//TODO do contentType and/or dataEncoding belong in HTTPSampleResult instead?

    private final static String TOTAL_TIME = "totalTime";

    transient private static Logger log = LoggingManager.getLoggerForClass();

    private static final boolean startTimeStamp = 
        JMeterUtils.getPropDefault("sampleresult.timestamp.start",false);

    static{
    	if (startTimeStamp){
        	log.info("Note: Sample TimeStamps are START times");
    	} else {
        	log.info("Note: Sample TimeStamps are END times");   		
    	}
		log.info("sampleresult.default.encoding is set to "+DEFAULT_ENCODING);
    }
    public SampleResult()
    {
    	time = 0;
    }

	/**
	 * Construct a 'parent' result for an already-existing result, essentially
	 * cloning it
	 * 
	 * @param res existing sample result
	 */
	public SampleResult(SampleResult res)
	{
		setStartTime(res.getStartTime());
		setTime(0);

		setSampleLabel(res.getSampleLabel());
		setRequestHeaders(res.getRequestHeaders());
		setResponseData(res.getResponseData());
		setResponseCode(res.getResponseCode());
		setSuccessful(res.isSuccessful());
		setResponseMessage(res.getResponseMessage());
		setDataType(res.getDataType());
		setResponseHeaders(res.getResponseHeaders());

		addSubResult(res); // this will add res.getTime() to getTime().
	}

    /**
     * Create a sample with a specific elapsed time
     * but don't allow the times to be changed later
     * 
     * (only used by HTTPSampleResult)
     * 
     * @param elapsed time
     * @param atend create the sample finishing now, else starting now
     */
	protected SampleResult(long elapsed, boolean atend)
	{
		long now = System.currentTimeMillis();
		if (atend){
			setTimes(now - elapsed, now);
		} else {
			setTimes(now, now + elapsed);
		}
	}
    
	/**
	 * Create a sample with specific start and end times
	 * for test purposes, but don't allow the times to be changed later
	 * 
	 * (used by StatVisualizerModel.Test)
	 * 
	 * @param start start time
	 * @param end end time 
	 */
	public static SampleResult createTestSample(long start, long end)
	{
		SampleResult res = new SampleResult();
		res.setStartTime(start);
		res.setEndTime(end);
		return res;
	}

	/**
	 * Create a sample with a specific elapsed time
	 * for test purposes, but don't allow the times to be changed later
	 * 
	 * @param elapsed - desired elapsed time
	 */
	public static SampleResult createTestSample(long elapsed)
	{
		long now = System.currentTimeMillis();
		return createTestSample(now,now+elapsed);
	}

    /**
     * Allow users to create a sample with specific timestamp and elapsed times
     * for cloning purposes, but don't allow the times to be changed later
     * 
     * Currently used by SaveService only
     * 
     * @param stamp - this may be a start time or an end time
     * @param elapsed
     */
	public SampleResult(long stamp, long elapsed)
	{
		// Maintain the timestamp relationships
		if (startTimeStamp) {
			setTimes(stamp, stamp + elapsed);
		} else {
			setTimes(stamp - elapsed, stamp);
		}
	}

    /**
     * Method to set the elapsed time for a sample.
     * Retained for backward compatibility with 3rd party add-ons
     * It is assumed that the method is called at the end of a sample
     * 
     * Must not be used in conjunction with sampleStart()/End()
     * 
     * @deprecated use sampleStart() and sampleEnd() instead
     * @param elapsed time in milliseconds
     */
    public void setTime(long elapsed){
		long now = System.currentTimeMillis();
    	setTimes(now-elapsed,now);
    }

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
     *         Returns empty array if there are no assertion results.
     */
    public AssertionResult[] getAssertionResults()
    {
        if (assertionResults == null)
        {
            return EMPTY_AR;
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
        setTime(getTime()+subResult.getTime());
    }

    /**
     * Gets the subresults associated with this sample.
     *
     * @return an array containing the subresults for this sample. Returns
     *         an empty array if there are no subresults.
     */
    public SampleResult[] getSubResults()
    {
        if (subResults == null)
        {
            return EMPTY_SR;
        }
        return (SampleResult[]) subResults.toArray(new SampleResult[0]);
    }

    public void configure(Configuration info)
    {
        time = info.getAttributeAsLong(TOTAL_TIME, 0L);
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
     * @return elapsed time in milliseonds
     * 
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
            return DEFAULT_ENCODING;
        }
    }

    /**
     * Sets the dataEncoding.
     * @param dataEncoding the dataEncoding to set, e.g. ISO-8895-1, UTF-8
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
     * @return the content type - e.g. text/html [;charset=utf-8 ]
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
	 * @return the end time
	 */
	public long getEndTime()
	{
		return endTime;
	}

	/**
	 * @return the start time
	 */
	public long getStartTime()
	{
		return startTime;
	}

    /*
     * Helper methods
     * N.B. setStartTime must be called before setEndTime
     * 
     * setStartTime is used by HTTPSampleResult to clone
     * the parent sampler and allow the original start time to be kept
     */
    protected final void setStartTime(long start)
    {
		startTime = start;
		if (startTimeStamp){
			timeStamp = startTime;
		}
    }

	private void setEndTime(long end)
	{
		endTime = end;
		if (!startTimeStamp){
			timeStamp = endTime;
		}
		if (startTime == 0){
			log.error("setEndTime must be called after setStartTime"
			         , new Throwable("Invalid call sequence"));
			//TODO should this throw an error?
		} else {
			time = endTime - startTime - idleTime;
		}
	}
	
	private void setTimes(long start, long end)
	{
		setStartTime(start);
		setEndTime(end);
	}

    /**
     * Record the start time of a sample
     *
     */
    public void sampleStart()
    {
    	if (startTime == 0){
			setStartTime(System.currentTimeMillis());
    	} else {
			log.error("sampleStart called twice"
			         , new Throwable("Invalid call sequence"));
    	}
    }
    
	/**
	 * Record the end time of a sample and calculate the elapsed time
	 *
	 */
    public void sampleEnd()
    {
    	if (endTime == 0){
			setEndTime(System.currentTimeMillis());
    	} else {
    		log.error("sampleEnd called twice"
    		         , new Throwable("Invalid call sequence"));
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
	
	/**
	 * When a Sampler is working as a monitor
	 * @param monitor
	 */
	public void setMonitor(boolean monitor){
		isMonitor = monitor;
	}

	/**
	 * If the sampler is a monitor, method will
	 * return true.
	 * @return true if the sampler is a monitor
	 */
	public boolean isMonitor(){
		return isMonitor;
	}
	
////////////////////////////// Start of Test Code ///////////////////////////


//TODO need more tests - particularly for the new functions

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
			assertTrue(res.getTime() >= 100);
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
			long sampleTime = res.getTime();
			if ((sampleTime < 200) || (sampleTime > 290)) {
				fail("Accumulated time ("+sampleTime+") was not between 200 and 290 ms");
			}
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

	private URL location;

	public void setURL(URL location) {
	    this.location= location;
	}

	public URL getURL() {
	    return location;
	}
}
