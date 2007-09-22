/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

// For unit tests, @see TestSampleResult

/**
 * This is a nice packaging for the various information returned from taking a
 * sample of an entry.
 * 
 */
public class SampleResult implements Serializable {

    // Needs to be accessible from Test code
    static final Logger log = LoggingManager.getLoggerForClass();

	// Bug 33196 - encoding ISO-8859-1 is only suitable for Western countries
	// However the suggested System.getProperty("file.encoding") is Cp1252 on
	// Windows
	// So use a new property with the original value as default
    // needs to be accessible from test code
	static final String DEFAULT_ENCODING 
            = JMeterUtils.getPropDefault("sampleresult.default.encoding", // $NON-NLS-1$
			"ISO-8859-1"); // $NON-NLS-1$

	/**
	 * Data type value indicating that the response data is text.
	 * 
	 * @see #getDataType
	 * @see #setDataType(java.lang.String)
	 */
	public final static String TEXT = "text"; // $NON-NLS-1$

	/**
	 * Data type value indicating that the response data is binary.
	 * 
	 * @see #getDataType
	 * @see #setDataType(java.lang.String)
	 */
	public final static String BINARY = "bin"; // $NON-NLS-1$

	/* empty arrays which can be returned instead of null */
	private static final byte[] EMPTY_BA = new byte[0];

	private static final SampleResult[] EMPTY_SR = new SampleResult[0];

	private static final AssertionResult[] EMPTY_AR = new AssertionResult[0];

	private SampleSaveConfiguration saveConfig;

	private SampleResult parent = null;

	/**
	 * @param propertiesToSave
	 *            The propertiesToSave to set.
	 */
	public void setSaveConfig(SampleSaveConfiguration propertiesToSave) {
		this.saveConfig = propertiesToSave;
	}

	public SampleSaveConfiguration getSaveConfig() {
		return saveConfig;
	}

	private byte[] responseData = EMPTY_BA;

	private String responseCode = "";// Never return null

	private String label = "";// Never return null

    private String resultFileName = ""; // Filename used by ResultSaver
    
	private String samplerData;

	private String threadName = ""; // Never return null

	private String responseMessage = "";

	private String responseHeaders = ""; // Never return null

	private String contentType = ""; // e.g. text/html; charset=utf-8

	private String requestHeaders = "";

	private long timeStamp = 0;// the time stamp - can be start or end

	private long startTime = 0;

	private long endTime = 0;

	private long idleTime = 0;// Allow for non-sample time

	private long pauseTime = 0;// Start of pause (if any)

	private List assertionResults;

	private List subResults;

	private String dataType=""; // Don't return null if not set

	private boolean success;

	private Set files; // files that this sample has been saved in

	private String dataEncoding;// (is this really the character set?) e.g.
								// ISO-8895-1, UTF-8
	// If null, then DEFAULT_ENCODING is returned by getDataEncoding()

	private long time = 0;

	private long latency = 0;

	private boolean stopThread = false; // Should thread terminate?

	private boolean stopTest = false; // Should test terminate?

	private boolean isMonitor = false;

	private int sampleCount = 1;

	private int bytes = 0;

	// TODO do contentType and/or dataEncoding belong in HTTPSampleResult instead?

	private final static String TOTAL_TIME = "totalTime"; // $NON-NLS-1$

	private static final boolean startTimeStamp 
        = JMeterUtils.getPropDefault("sampleresult.timestamp.start", false);  // $NON-NLS-1$

	static {
		if (startTimeStamp) {
			log.info("Note: Sample TimeStamps are START times");
		} else {
			log.info("Note: Sample TimeStamps are END times");
		}
		log.info("sampleresult.default.encoding is set to " + DEFAULT_ENCODING);
	}

	public SampleResult() {
		time = 0;
	}

	/**
	 * Construct a 'parent' result for an already-existing result, essentially
	 * cloning it
	 * 
	 * @param res
	 *            existing sample result
	 */
	public SampleResult(SampleResult res) {
		setStartTime(res.getStartTime());
		setEndTime(res.getStartTime()); 
		// was setElapsed(0) which is the same as setStartTime=setEndTime=now

		setSampleLabel(res.getSampleLabel());
		setRequestHeaders(res.getRequestHeaders());
		setResponseData(res.getResponseData());
		setResponseCode(res.getResponseCode());
		setSuccessful(res.isSuccessful());
		setResponseMessage(res.getResponseMessage());
		setDataType(res.getDataType());
		setResponseHeaders(res.getResponseHeaders());
        setContentType(res.getContentType());
        setDataEncoding(res.getDataEncoding());
		setURL(res.getURL());

		addSubResult(res); // this will add res.getTime() to getTime().
	}

	public boolean isStampedAtStart() {
		return startTimeStamp;
	}

	/**
	 * Create a sample with a specific elapsed time but don't allow the times to
	 * be changed later
	 * 
	 * (only used by HTTPSampleResult)
	 * 
	 * @param elapsed
	 *            time
	 * @param atend
	 *            create the sample finishing now, else starting now
	 */
	protected SampleResult(long elapsed, boolean atend) {
		long now = System.currentTimeMillis();
		if (atend) {
			setTimes(now - elapsed, now);
		} else {
			setTimes(now, now + elapsed);
		}
	}

	/**
	 * Create a sample with specific start and end times for test purposes, but
	 * don't allow the times to be changed later
	 * 
	 * (used by StatVisualizerModel.Test)
	 * 
	 * @param start
	 *            start time
	 * @param end
	 *            end time
	 */
	public static SampleResult createTestSample(long start, long end) {
		SampleResult res = new SampleResult();
		res.setStartTime(start);
		res.setEndTime(end);
		return res;
	}

	/**
	 * Create a sample with a specific elapsed time for test purposes, but don't
	 * allow the times to be changed later
	 * 
	 * @param elapsed -
	 *            desired elapsed time
	 */
	public static SampleResult createTestSample(long elapsed) {
		long now = System.currentTimeMillis();
		return createTestSample(now, now + elapsed);
	}

	/**
	 * Allow users to create a sample with specific timestamp and elapsed times
	 * for cloning purposes, but don't allow the times to be changed later
	 * 
	 * Currently used by OldSaveService only
	 * 
	 * @param stamp -
	 *            this may be a start time or an end time
	 * @param elapsed
	 */
	public SampleResult(long stamp, long elapsed) {
		stampAndTime(stamp, elapsed);
	}

	// Helper method to maintain timestamp relationships
	private void stampAndTime(long stamp, long elapsed) {
		if (startTimeStamp) {
			setTimes(stamp, stamp + elapsed);
		} else {
			setTimes(stamp - elapsed, stamp);
		}
	}

	/*
	 * For use by SaveService only.
	 *  
	 * @param stamp -
	 *            this may be a start time or an end time
	 * @param elapsed
	 */
	public void setStampAndTime(long stamp, long elapsed) {
		if (startTime != 0 || endTime != 0){
			throw new RuntimeException("Calling setStampAndTime() after start/end times have been set");
		}
		stampAndTime(stamp, elapsed);
	}

	/**
	 * Method to set the elapsed time for a sample. Retained for backward
	 * compatibility with 3rd party add-ons.
     * It is assumed that the method is only called at the end of a sample
     * and that timeStamps are end-times
	 * 
     * Also used by SampleResultConverter when creating results from files.
     * 
	 * Must not be used in conjunction with sampleStart()/End()
	 * 
	 * @deprecated use sampleStart() and sampleEnd() instead
	 * @param elapsed
	 *            time in milliseconds
	 */
	public void setTime(long elapsed) {
		if (startTime != 0 || endTime != 0){
			throw new RuntimeException("Calling setTime() after start/end times have been set");
		}
		long now = System.currentTimeMillis();
	    setTimes(now - elapsed, now);
	}

	public void setMarked(String filename) {
		if (files == null) {
			files = new HashSet();
		}
		files.add(filename);
	}

	public boolean isMarked(String filename) {
		return files != null && files.contains(filename);
	}

	public String getResponseCode() {
		return responseCode;
	}

    private static final String OK = Integer.toString(HttpURLConnection.HTTP_OK);
    
    /**
     * Set response code to OK, i.e. "200"
     *
     */
    public void setResponseCodeOK(){
        responseCode=OK;
    }
    
	public void setResponseCode(String code) {
		responseCode = code;
	}

    public boolean isResponseCodeOK(){
        return responseCode.equals(OK);
    }
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String msg) {
		responseMessage = msg;
	}

    public void setResponseMessageOK() {
        responseMessage = "OK"; // $NON-NLS-1$       
    }

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

    /**
     * Get the sample timestamp, which may be either the start time or the end time.
     * 
     * @see #getStartTime()
     * @see #getEndTime()
     * 
     * @return timeStamp in milliseconds
     */
	public long getTimeStamp() {
		return timeStamp;
	}

	public String getSampleLabel() {
		return label;
	}

	public void setSampleLabel(String label) {
		this.label = label;
	}

	public void addAssertionResult(AssertionResult assertResult) {
		if (assertionResults == null) {
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
	public AssertionResult[] getAssertionResults() {
		if (assertionResults == null) {
			return EMPTY_AR;
		}
		return (AssertionResult[]) assertionResults.toArray(new AssertionResult[0]);
	}

	public void addSubResult(SampleResult subResult) {
		String tn = getThreadName();
        if (tn.length()==0) {
        	tn=Thread.currentThread().getName();//TODO do this more efficiently
            this.setThreadName(tn);
        }
        subResult.setThreadName(tn);
		if (subResults == null) {
			subResults = new ArrayList();
		}
		subResults.add(subResult);
		// Extend the time to the end of the added sample
		setEndTime(subResult.getEndTime());
		// Include the byte count for the added sample
		setBytes(getBytes() + subResult.getBytes());
		subResult.setParent(this);
	}

    /**
     * Add a subresult read from a results file.
     * 
     * As for addSubResult(), except that the fields don't need to be accumulated
     * 
     * @param subResult
     */
    public void storeSubResult(SampleResult subResult) {
        if (subResults == null) {
            subResults = new ArrayList();
        }
        subResults.add(subResult);
        subResult.setParent(this);
    }

	/**
	 * Gets the subresults associated with this sample.
	 * 
	 * @return an array containing the subresults for this sample. Returns an
	 *         empty array if there are no subresults.
	 */
	public SampleResult[] getSubResults() {
		if (subResults == null) {
			return EMPTY_SR;
		}
		return (SampleResult[]) subResults.toArray(new SampleResult[0]);
	}

	public void configure(Configuration info) {
		time = info.getAttributeAsLong(TOTAL_TIME, 0L);
	}

	/**
	 * Sets the responseData attribute of the SampleResult object.
	 * 
	 * @param response
	 *            the new responseData value
	 */
	public void setResponseData(byte[] response) {
		responseData = response;
	}

    /**
     * Sets the responseData attribute of the SampleResult object.
     * 
     * @param response
     *            the new responseData value (String)
     * 
     * @deprecated - only intended for use from BeanShell code
     */
    public void setResponseData(String response) {
        responseData = response.getBytes();
    }

	/**
	 * Gets the responseData attribute of the SampleResult object.
	 * 
	 * @return the responseData value
	 */
	public byte[] getResponseData() {
		return responseData;
	}

    /**
     * Gets the responseData attribute of the SampleResult object.
     * 
     * @return the responseData value as a String, converted according to the encoding
     */
    public String getResponseDataAsString() {
        try {
            return new String(responseData,getDataEncoding());
        } catch (UnsupportedEncodingException e) {
            log.warn("Using "+dataEncoding+" caused "+e);
            return new String(responseData);
        }
    }

	/**
	 * Convenience method to get responseData as a non-null byte array
	 * 
	 * @return the responseData. If responseData is null then an empty byte
	 *         array is returned rather than null.
	 *
	 * @deprecated - no longer needed, as getResponseData() does not return null
	 */
	public byte[] getResponseDataAsBA() {
		return responseData == null ? EMPTY_BA : responseData;
	}

	public void setSamplerData(String s) {
		samplerData = s;
	}

	public String getSamplerData() {
		return samplerData;
	}

	/**
	 * Get the time it took this sample to occur.
	 * 
	 * @return elapsed time in milliseonds
	 * 
	 */
	public long getTime() {
		return time;
	}

	public boolean isSuccessful() {
		return success;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataType() {
		return dataType;
	}
    /**
     * Set Encoding and DataType from ContentType
     * @param ct - content type (may be null)
     */
    public void setEncodingAndType(String ct){
        if (ct != null) {
            // Extract charset and store as DataEncoding
            // TODO do we need process http-equiv META tags, e.g.:
            // <META http-equiv="content-type" content="text/html;
            // charset=foobar">
            // or can we leave that to the renderer ?
            final String CS_PFX = "charset="; // $NON-NLS-1$
            int cset = ct.toLowerCase().indexOf(CS_PFX);
            if (cset >= 0) {
            	// TODO - assumes charset is not followed by anything else
                String charSet = ct.substring(cset + CS_PFX.length());
                // Check for quoted string
                if (charSet.startsWith("\"")){ // $NON-NLS-1$
                	setDataEncoding(charSet.substring(1, charSet.length()-1)); // remove quotes
                } else {
				    setDataEncoding(charSet);
                }
            }
            if (ct.startsWith("image/")) {// $NON-NLS-1$
                setDataType(BINARY);
            } else {
                setDataType(TEXT);
            }
        }
    }

	/**
	 * Sets the successful attribute of the SampleResult object.
	 * 
	 * @param success
	 *            the new successful value
	 */
	public void setSuccessful(boolean success) {
		this.success = success;
	}

	/**
	 * Returns the display name.
	 * 
	 * @return display name of this sample result
	 */
	public String toString() {
		return getSampleLabel();
	}

	/**
	 * Returns the dataEncoding.
	 */
	public String getDataEncoding() {
		if (dataEncoding != null) {
			return dataEncoding;
		}
		return DEFAULT_ENCODING;
	}

	/**
	 * Sets the dataEncoding.
	 * 
	 * @param dataEncoding
	 *            the dataEncoding to set, e.g. ISO-8895-1, UTF-8
	 */
	public void setDataEncoding(String dataEncoding) {
		this.dataEncoding = dataEncoding;
	}

	/**
	 * @return whether to stop the test
	 */
	public boolean isStopTest() {
		return stopTest;
	}

	/**
	 * @return whether to stop this thread
	 */
	public boolean isStopThread() {
		return stopThread;
	}

	/**
	 * @param b
	 */
	public void setStopTest(boolean b) {
		stopTest = b;
	}

	/**
	 * @param b
	 */
	public void setStopThread(boolean b) {
		stopThread = b;
	}

	/**
	 * @return the request headers
	 */
	public String getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * @return the response headers
	 */
	public String getResponseHeaders() {
		return responseHeaders;
	}

	/**
	 * @param string -
	 *            request headers
	 */
	public void setRequestHeaders(String string) {
		requestHeaders = string;
	}

	/**
	 * @param string -
	 *            response headers
	 */
	public void setResponseHeaders(String string) {
		responseHeaders = string;
	}

	/**
	 * @return the full content type - e.g. text/html [;charset=utf-8 ]
	 */
	public String getContentType() {
		return contentType;
	}

    /**
     * Get the media type from the Content Type
     * @return the media type - e.g. text/html (without charset, if any)
     */
    public String getMediaType() {
        return JOrphanUtils.trim(contentType," ;").toLowerCase();
    }

	/**
	 * @param string
	 */
	public void setContentType(String string) {
		contentType = string;
	}

    /**
     * @return idleTime
     */
    public long getIdleTime() {
        return idleTime;
    }
    
	/**
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/*
	 * Helper methods N.B. setStartTime must be called before setEndTime
	 * 
	 * setStartTime is used by HTTPSampleResult to clone the parent sampler and
	 * allow the original start time to be kept
	 */
	protected final void setStartTime(long start) {
		startTime = start;
		if (startTimeStamp) {
			timeStamp = startTime;
		}
	}

	protected void setEndTime(long end) {
		endTime = end;
		if (!startTimeStamp) {
			timeStamp = endTime;
		}
		if (startTime == 0) {
			log.error("setEndTime must be called after setStartTime", new Throwable("Invalid call sequence"));
			// TODO should this throw an error?
		} else {
			time = endTime - startTime - idleTime;
		}
	}

	private void setTimes(long start, long end) {
		setStartTime(start);
		setEndTime(end);
	}

	/**
	 * Record the start time of a sample
	 * 
	 */
	public void sampleStart() {
		if (startTime == 0) {
			setStartTime(System.currentTimeMillis());
		} else {
			log.error("sampleStart called twice", new Throwable("Invalid call sequence"));
		}
	}

	/**
	 * Record the end time of a sample and calculate the elapsed time
	 * 
	 */
	public void sampleEnd() {
		if (endTime == 0) {
			setEndTime(System.currentTimeMillis());
		} else {
			log.error("sampleEnd called twice", new Throwable("Invalid call sequence"));
		}
	}

	/**
	 * Pause a sample
	 * 
	 */
	public void samplePause() {
		if (pauseTime != 0) {
			log.error("samplePause called twice", new Throwable("Invalid call sequence"));
		}
		pauseTime = System.currentTimeMillis();
	}

	/**
	 * Resume a sample
	 * 
	 */
	public void sampleResume() {
		if (pauseTime == 0) {
			log.error("sampleResume without samplePause", new Throwable("Invalid call sequence"));
		}
		idleTime += System.currentTimeMillis() - pauseTime;
		pauseTime = 0;
	}

	/**
	 * When a Sampler is working as a monitor
	 * 
	 * @param monitor
	 */
	public void setMonitor(boolean monitor) {
		isMonitor = monitor;
	}

	/**
	 * If the sampler is a monitor, method will return true.
	 * 
	 * @return true if the sampler is a monitor
	 */
	public boolean isMonitor() {
		return isMonitor;
	}

	/**
	 * For the JMS sampler, it can perform multiple samples for greater degree
	 * of accuracy.
	 * 
	 * @param count
	 */
	public void setSampleCount(int count) {
		sampleCount = count;
	}

	/**
	 * return the sample count. by default, the value is 1.
	 * 
	 * @return the count of samples
	 */
	public int getSampleCount() {
		return sampleCount;
	}

	/**
	 * Returns the count of errors.
	 * 
	 * @return 0 - or 1 if the sample failed
	 */
	public int getErrorCount(){
		return success ? 0 : 1;
	}
	
	public void setErrorCount(int i){// for reading from CSV files
		// ignored currently
	}
	/*
	 * TODO: error counting needs to be sorted out after 2.3 final.
	 * At present the Statistical Sampler tracks errors separately
	 * It would make sense to move the error count here, but this would
	 * mean lots of changes.
	 * It's also tricky maintaining the count - it can't just be incremented/decremented
	 * when the success flag is set as this may be done multiple times.
	 * The work-round for now is to do the work in the StatisticalSampleResult,
	 * which overrides this method.
	 * Note that some JMS samplers also create samples with > 1 sample count
	 * Also the Transaction Controller probably needs to be changed to do
	 * proper sample and error accounting.
	 * The purpose of this work-round is to allow at least minimal support for
	 * errors in remote statistical batch mode.
	 * 
	 */
	/**
	 * In the event the sampler does want to pass back the actual contents, we
	 * still want to calculate the throughput. The bytes is the bytes of the
	 * response data.
	 * 
	 * @param length
	 */
	public void setBytes(int length) {
		bytes = length;
	}

	/**
	 * return the bytes returned by the response.
	 * 
	 * @return number of bytes in response
	 */
	public int getBytes() {
		return bytes == 0 ? responseData.length : bytes;
	}

	/**
	 * @return Returns the latency.
	 */
	public long getLatency() {
		return latency;
	}

    /**
     * Set the time to the first response
     *
     */
	public void latencyEnd() {
		latency = System.currentTimeMillis() - startTime - idleTime;
	}

	/**
     * This is only intended for use by SampleResultConverter!
     * 
	 * @param latency
	 *            The latency to set.
	 */
	public void setLatency(long latency) {
		this.latency = latency;
	}

	/**
     * This is only intended for use by SampleResultConverter!
     *
	 * @param timeStamp
	 *            The timeStamp to set.
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	private URL location;

	public void setURL(URL location) {
		this.location = location;
	}

	public URL getURL() {
		return location;
	}
	
	/**
	 * Get a String representation of the URL (if defined).
	 * 
	 * @return ExternalForm of URL, or empty string if url is null
	 */
	public String getUrlAsString() {
		return location == null ? "" : location.toExternalForm();
	}

	/**
	 * @return Returns the parent.
	 */
	public SampleResult getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(SampleResult parent) {
		this.parent = parent;
	}

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }
}
