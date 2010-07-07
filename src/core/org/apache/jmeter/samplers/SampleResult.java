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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final long serialVersionUID = 233L;

    // Needs to be accessible from Test code
    static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * The default encoding to be used if not overridden.
     * The value is ISO-8859-1.
     */
    public static final String DEFAULT_HTTP_ENCODING = "ISO-8859-1";  // $NON-NLS-1$

    // Bug 33196 - encoding ISO-8859-1 is only suitable for Western countries
    // However the suggested System.getProperty("file.encoding") is Cp1252 on
    // Windows
    // So use a new property with the original value as default
    // needs to be accessible from test code
    /**
     * The default encoding to be used to decode the responseData byte array.
     * The value is defined by the property "sampleresult.default.encoding"
     * with a default of DEFAULT_HTTP_ENCODING if that is not defined.
     */
    static final String DEFAULT_ENCODING
            = JMeterUtils.getPropDefault("sampleresult.default.encoding", // $NON-NLS-1$
            DEFAULT_HTTP_ENCODING);

    /* The default used by {@link #setResponseData(String, String)} */
    private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

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

    /** Filename used by ResultSaver */
    private String resultFileName = "";

    /** The data used by the sampler */
    private String samplerData;

    private String threadName = ""; // Never return null

    private String responseMessage = "";

    private String responseHeaders = ""; // Never return null

    private String contentType = ""; // e.g. text/html; charset=utf-8

    private String requestHeaders = "";

    // TODO timeStamp == 0 means either not yet initialised or no stamp available (e.g. when loading a results file)
    /** the time stamp - can be start or end */
    private long timeStamp = 0;

    private long startTime = 0;

    private long endTime = 0;

    private long idleTime = 0;// Allow for non-sample time

    /** Start of pause (if any) */
    private long pauseTime = 0;

    private List<AssertionResult> assertionResults;

    private List<SampleResult> subResults;

    private String dataType=""; // Don't return null if not set

    private boolean success;

    //@GuardedBy("this"")
    /** files that this sample has been saved in */
    private final Set<String> files = new HashSet<String>();

    private String dataEncoding;// (is this really the character set?) e.g.
                                // ISO-8895-1, UTF-8

    // a reference time from the nanosecond clock
    private static final long referenceTimeNsClock = sampleNsClockInMs();

    // a reference time from the millisecond clock
    private static final long referenceTimeMsClock = System.currentTimeMillis();

    /** elapsed time */
    private long time = 0;

    /** time to first response */
    private long latency = 0;

    /** Should thread terminate? */
    private boolean stopThread = false;

    /** Should test terminate? */
    private boolean stopTest = false;

    /** Should test terminate abruptly? */
    private boolean stopTestNow = false;

    /** Is the sampler acting as a monitor? */
    private boolean isMonitor = false;

    private int sampleCount = 1;

    private int bytes = 0; // Allows override of sample size in case sampler does not want to store all the data

    /** Currently active threads in this thread group */
    private volatile int groupThreads = 0;

    /** Currently active threads in all thread groups */
    private volatile int allThreads = 0;

    // TODO do contentType and/or dataEncoding belong in HTTPSampleResult instead?

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
     * Copy constructor.
     * 
     * @param res existing sample result
     */
    public SampleResult(SampleResult res) {
        allThreads = res.allThreads;//OK
        assertionResults = res.assertionResults;// TODO ??
        bytes = res.bytes;
        contentType = res.contentType;//OK
        dataEncoding = res.dataEncoding;//OK
        dataType = res.dataType;//OK
        endTime = res.endTime;//OK
        // files is created automatically, and applies per instance
        groupThreads = res.groupThreads;//OK
        idleTime = res.idleTime;
        isMonitor = res.isMonitor;
        label = res.label;//OK
        latency = res.latency;
        location = res.location;//OK
        parent = res.parent; // TODO ??
        pauseTime = res.pauseTime;
        requestHeaders = res.requestHeaders;//OK
        responseCode = res.responseCode;//OK
        responseData = res.responseData;//OK
        responseHeaders = res.responseHeaders;//OK
        responseMessage = res.responseMessage;//OK
        // Don't copy this; it is per instance resultFileName = res.resultFileName;
        sampleCount = res.sampleCount;
        samplerData = res.samplerData;
        saveConfig = res.saveConfig;
        startTime = res.startTime;//OK
        stopTest = res.stopTest;
        stopTestNow = res.stopTestNow;
        stopThread = res.stopThread;
        subResults = res.subResults; // TODO ??
        success = res.success;//OK
        threadName = res.threadName;//OK
        time = res.time;
        timeStamp = res.timeStamp;
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
        long now = currentTimeInMs();
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
        long now = currentTimeInMs();
        return createTestSample(now, now + elapsed);
    }

    /**
     * Allow users to create a sample with specific timestamp and elapsed times
     * for cloning purposes, but don't allow the times to be changed later
     *
     * Currently used by OldSaveService, CSVSaveService and StatisticalSampleResult
     *
     * @param stamp -
     *            this may be a start time or an end time
     * @param elapsed
     */
    public SampleResult(long stamp, long elapsed) {
        stampAndTime(stamp, elapsed);
    }

    private static long sampleNsClockInMs() {
        return System.nanoTime() / 1000000;
    }

    // Helper method to get 1 ms resolution timing.
    public static long currentTimeInMs() {
        long elapsedInMs = sampleNsClockInMs() - referenceTimeNsClock;
        return referenceTimeMsClock + elapsedInMs;
    }

    // Helper method to maintain timestamp relationships
    private void stampAndTime(long stamp, long elapsed) {
        if (startTimeStamp) {
            startTime = stamp;
            endTime = stamp + elapsed;
        } else {
            startTime = stamp - elapsed;
            endTime = stamp;
        }
        timeStamp = stamp;
        time = elapsed;
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
     * Set the "marked" flag to show that the result has been written to the file.
     *
     * @param filename
     * @return true if the result was previously marked
     */
    public synchronized boolean markFile(String filename) {
        return !files.add(filename);
    }

    public String getResponseCode() {
        return responseCode;
    }

    private static final String OK_CODE = Integer.toString(HttpURLConnection.HTTP_OK);
    private static final String OK_MSG = "OK"; // $NON-NLS-1$

    /**
     * Set response code to OK, i.e. "200"
     *
     */
    public void setResponseCodeOK(){
        responseCode=OK_CODE;
    }

    public void setResponseCode(String code) {
        responseCode = code;
    }

    public boolean isResponseCodeOK(){
        return responseCode.equals(OK_CODE);
    }
    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String msg) {
        responseMessage = msg;
    }

    public void setResponseMessageOK() {
        responseMessage = OK_MSG;
    }

    /**
     * Set result statuses OK - shorthand method to set:
     * <ul>
     * <li>ResponseCode</li>
     * <li>ResponseMessage</li>
     * <li>Successful status</li>
     * </ul>
     */
    public void setResponseOK(){
        setResponseCodeOK();
        setResponseMessageOK();
        setSuccessful(true);
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

    /**
     * Get the sample label for use in summary reports etc.
     *
     * @param includeGroup whether to include the thread group name
     * @return the label
     */
    public String getSampleLabel(boolean includeGroup) {
        if (includeGroup) {
            StringBuilder sb = new StringBuilder(threadName.substring(0,threadName.lastIndexOf(" "))); //$NON-NLS-1$
            return sb.append(":").append(label).toString(); //$NON-NLS-1$
        }
        return label;
    }

    public void setSampleLabel(String label) {
        this.label = label;
    }

    public void addAssertionResult(AssertionResult assertResult) {
        if (assertionResults == null) {
            assertionResults = new ArrayList<AssertionResult>();
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
        return assertionResults.toArray(new AssertionResult[0]);
    }

    /**
     * Add a subresult and adjust the parent byte count and end-time.
     * 
     * @param subResult
     */
    public void addSubResult(SampleResult subResult) {
        String tn = getThreadName();
        if (tn.length()==0) {
            tn=Thread.currentThread().getName();//TODO do this more efficiently
            this.setThreadName(tn);
        }
        subResult.setThreadName(tn); // TODO is this really necessary?

        // Extend the time to the end of the added sample
        setEndTime(Math.max(getEndTime(), subResult.getEndTime()));
        // Include the byte count for the added sample
        setBytes(getBytes() + subResult.getBytes());
        addRawSubResult(subResult);
    }
    
    /**
     * Add a subresult to the collection without updating any parent fields.
     * 
     * @param subResult
     */
    public void addRawSubResult(SampleResult subResult){
        if (subResults == null) {
            subResults = new ArrayList<SampleResult>();
        }
        subResults.add(subResult);
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
            subResults = new ArrayList<SampleResult>();
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
        return subResults.toArray(new SampleResult[0]);
    }

    /**
     * Sets the responseData attribute of the SampleResult object.
     *
     * If the parameter is null, then the responseData is set to an empty byte array.
     * This ensures that getResponseData() can never be null.
     *
     * @param response
     *            the new responseData value
     */
    public void setResponseData(byte[] response) {
        responseData = response == null ? EMPTY_BA : response;
    }

    /**
     * Sets the responseData attribute of the SampleResult object.
     * Should only be called after setting the dataEncoding (if necessary)
     *
     * @param response
     *            the new responseData value (String)
     *
     * @deprecated - only intended for use from BeanShell code
     */
    @Deprecated
    public void setResponseData(String response) {
        try {
            responseData = response.getBytes(getDataEncodingWithDefault());
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not convert string, using default encoding. "+e.getLocalizedMessage());
            responseData = response.getBytes();
        }
    }

    /**
     * Sets the encoding and responseData attributes of the SampleResult object.
     *
     * @param response the new responseData value (String)
     * @param encoding the encoding to set and then use (if null, use platform default)
     *
     */
    public void setResponseData(final String response, final String encoding) {
        String encodeUsing = encoding != null? encoding : DEFAULT_CHARSET;
        try {
            responseData = response.getBytes(encodeUsing);
            setDataEncoding(encodeUsing);
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not convert string using '"+encodeUsing+
                    "', using default encoding: "+DEFAULT_CHARSET,e);
            responseData = response.getBytes();
            setDataEncoding(DEFAULT_CHARSET);
        }
    }

    /**
     * Gets the responseData attribute of the SampleResult object.
     * <p>
     * Note that some samplers may not store all the data, in which case
     * getResponseData().length will be incorrect.
     *
     * Instead, always use {@link #getBytes()} to obtain the sample result byte count.
     * </p>
     * @return the responseData value (cannot be null)
     */
    public byte[] getResponseData() {
        return responseData;
    }

    /**
     * Gets the responseData of the SampleResult object as a String
     *
     * @return the responseData value as a String, converted according to the encoding
     */
    public String getResponseDataAsString() {
        try {
            return new String(responseData,getDataEncodingWithDefault());
        } catch (UnsupportedEncodingException e) {
            log.warn("Using platform default as "+getDataEncodingWithDefault()+" caused "+e);
            return new String(responseData);
        }
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
     * Extract and save the DataEncoding and DataType from the parameter provided.
     * Does not save the full content Type.
     * @see #setContentType(String) which should be used to save the full content-type string
     *
     * @param ct - content type (may be null)
     */
    public void setEncodingAndType(String ct){
        if (ct != null) {
            // Extract charset and store as DataEncoding
            // N.B. The meta tag:
            // <META http-equiv="content-type" content="text/html; charset=foobar">
            // is now processed by HTTPSampleResult#getDataEncodingWithDefault
            final String CS_PFX = "charset="; // $NON-NLS-1$
            int cset = ct.toLowerCase(java.util.Locale.ENGLISH).indexOf(CS_PFX);
            if (cset >= 0) {
                String charSet = ct.substring(cset + CS_PFX.length());
                // handle: ContentType: text/plain; charset=ISO-8859-1; format=flowed
                int semiColon = charSet.indexOf(';');
                if (semiColon >= 0) {
                    charSet=charSet.substring(0, semiColon);
                }
                // Check for quoted string
                if (charSet.startsWith("\"")){ // $NON-NLS-1$
                    setDataEncoding(charSet.substring(1, charSet.length()-1)); // remove quotes
                } else {
                    setDataEncoding(charSet);
                }
            }
            if (isBinaryType(ct)) {
                setDataType(BINARY);
            } else {
                setDataType(TEXT);
            }
        }
    }

    // List of types that are known to be binary
    private static final String[] BINARY_TYPES = {
        "image/",       //$NON-NLS-1$
        "audio/",       //$NON-NLS-1$
        "video/",       //$NON-NLS-1$
        };

    /*
     * Determine if content-type is known to be binary, i.e. not displayable as text.
     *
     * @param ct content type
     * @return true if content-type is of type binary.
     */
    private static boolean isBinaryType(String ct){
        for (int i = 0; i < BINARY_TYPES.length; i++){
            if (ct.startsWith(BINARY_TYPES[i])){
                return true;
            }
        }
        return false;
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
    @Override
    public String toString() {
        return getSampleLabel();
    }

    /**
     * Returns the dataEncoding or the default if no dataEncoding was provided.
     * 
     * @return the value of the dataEncoding or DEFAULT_ENCODING
     */
    public String getDataEncodingWithDefault() {
        return getDataEncodingWithDefault(DEFAULT_ENCODING);
    }

    /**
     * Returns the dataEncoding or the default if no dataEncoding was provided.
     * 
     * @param defaultEncoding the default to be applied
     * @return the value of the dataEncoding or the provided default
     */
    protected String getDataEncodingWithDefault(String defaultEncoding) {
        if (dataEncoding != null && dataEncoding.length() > 0) {
            return dataEncoding;
        }
        return defaultEncoding;
    }

    /**
     * Returns the dataEncoding. May be null or the empty String.
     * @return the value of the dataEncoding
     */
    public String getDataEncodingNoDefault() {
        return dataEncoding;
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
     * @return whether to stop the test now
     */
    public boolean isStopTestNow() {
        return stopTestNow;
    }

    /**
     * @return whether to stop this thread
     */
    public boolean isStopThread() {
        return stopThread;
    }

    public void setStopTest(boolean b) {
        stopTest = b;
    }

    public void setStopTestNow(boolean b) {
        stopTestNow = b;
    }

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
        return JOrphanUtils.trim(contentType," ;").toLowerCase(java.util.Locale.ENGLISH);
    }

    /**
     * Stores the content-type string, e.g. "text/xml; charset=utf-8"
     * @see #setEncodingAndType(String) which can be used to extract the charset.
     *
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

    /**
     * Set idle time pause.
     * For use by SampleResultConverter/CSVSaveService.
     * @param idle long
     */
    public void setIdleTime(long idle) {
        idleTime = idle;
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
            setStartTime(currentTimeInMs());
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
            setEndTime(currentTimeInMs());
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
        pauseTime = currentTimeInMs();
    }

    /**
     * Resume a sample
     *
     */
    public void sampleResume() {
        if (pauseTime == 0) {
            log.error("sampleResume without samplePause", new Throwable("Invalid call sequence"));
        }
        idleTime += currentTimeInMs() - pauseTime;
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
     * The statistical sample sender aggregates several samples to save on
     * transmission costs.
     * 
     * @param count number of samples represented by this instance
     */
    public void setSampleCount(int count) {
        sampleCount = count;
    }

    /**
     * return the sample count. by default, the value is 1.
     *
     * @return the sample count
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
     * TODO: error counting needs to be sorted out.
     *
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
     * @return byte count
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
        latency = currentTimeInMs() - startTime - idleTime;
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

    public int getGroupThreads() {
        return groupThreads;
    }

    public void setGroupThreads(int n) {
        this.groupThreads = n;
    }

    public int getAllThreads() {
        return allThreads;
    }

    public void setAllThreads(int n) {
        this.allThreads = n;
    }

    // Bug 47394
    /**
     * Allow custom SampleSenders to drop unwanted assertionResults
     */
    public void removeAssertionResults() {
        this.assertionResults = null;
    }

    /**
     * Allow custom SampleSenders to drop unwanted subResults
     */
    public void removeSubResults() {
        this.subResults = null;
    }
}
