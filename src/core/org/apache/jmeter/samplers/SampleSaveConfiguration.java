/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

/*
 * Created on Sep 7, 2004
 */
package org.apache.jmeter.samplers;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;

/*
 * N.B. to add a new field, remember the following
 * - static _xyz
 * - instance xyz=_xyz
 * - clone s.xyz = xyz
 * - setXyz(boolean)
 * - saveXyz()
 * - update SampleSaveConfigurationConverter to add new fields to marshall() and shouldSerialiseMember()
 * - update SampleResultConverter and/or HTTPSampleConverter
 * - update CSV routines in OldSaveService
 * - update messages.properties to add save_xyz entry
 * 
 */
/**
 * Holds details of which sample attributes to save.
 * 
 * The pop-up dialogue for this is created by the class SavePropertyDialog, which assumes:
 * For each field XXX
 * - methods have the signature "boolean saveXXX()" 
 * - a corresponding "void setXXX(boolean)" method
 * - messages.properties contains the key save_XXX
 * 
 * @author mstover
 *
 */
public class SampleSaveConfiguration implements Cloneable, Serializable {
	private static final long serialVersionUID = 6L;

	// ---------------------------------------------------------------------
	// PROPERTY FILE CONSTANTS
	// ---------------------------------------------------------------------

	/** Indicates that the results file should be in XML format. * */
	private static final String XML = "xml"; // $NON_NLS-1$

	/** Indicates that the results file should be in CSV format. * */
	//NOTUSED private static final String CSV = "csv"; // $NON_NLS-1$

	/** Indicates that the results should be stored in a database. * */
	//NOTUSED private static final String DATABASE = "db"; // $NON_NLS-1$

	/** A properties file indicator for true. * */
	private static final String TRUE = "true"; // $NON_NLS-1$

	/** A properties file indicator for false. * */
	private static final String FALSE = "false"; // $NON_NLS-1$

	/** A properties file indicator for milliseconds. * */
	private static final String MILLISECONDS = "ms"; // $NON_NLS-1$

	/** A properties file indicator for none. * */
	private static final String NONE = "none"; // $NON_NLS-1$

	/** A properties file indicator for the first of a series. * */
	private static final String FIRST = "first"; // $NON_NLS-1$

	/** A properties file indicator for all of a series. * */
	private static final String ALL = "all"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating which assertion results should be
	 * saved.
	 **************************************************************************/
	private static final String ASSERTION_RESULTS_FAILURE_MESSAGE_PROP = 
        "jmeter.save.saveservice.assertion_results_failure_message";  // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating which assertion results should be
	 * saved.
	 **************************************************************************/
	private static final String ASSERTION_RESULTS_PROP = "jmeter.save.saveservice.assertion_results"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating which delimiter should be used when
	 * saving in a delimited values format.
	 **************************************************************************/
	private static final String DEFAULT_DELIMITER_PROP = "jmeter.save.saveservice.default_delimiter"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating which format should be used when
	 * saving the results, e.g., xml or csv.
	 **************************************************************************/
	private static final String OUTPUT_FORMAT_PROP = "jmeter.save.saveservice.output_format"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether field names should be printed
	 * to a delimited file.
	 **************************************************************************/
	private static final String PRINT_FIELD_NAMES_PROP = "jmeter.save.saveservice.print_field_names"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the data type should be
	 * saved.
	 **************************************************************************/
	private static final String SAVE_DATA_TYPE_PROP = "jmeter.save.saveservice.data_type"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the label should be saved.
	 **************************************************************************/
	private static final String SAVE_LABEL_PROP = "jmeter.save.saveservice.label"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the response code should be
	 * saved.
	 **************************************************************************/
	private static final String SAVE_RESPONSE_CODE_PROP = "jmeter.save.saveservice.response_code"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the response data should be
	 * saved.
	 **************************************************************************/
	private static final String SAVE_RESPONSE_DATA_PROP = "jmeter.save.saveservice.response_data"; // $NON_NLS-1$

	private static final String SAVE_RESPONSE_DATA_ON_ERROR_PROP = "jmeter.save.saveservice.response_data.on_error"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the response message should
	 * be saved.
	 **************************************************************************/
	private static final String SAVE_RESPONSE_MESSAGE_PROP = "jmeter.save.saveservice.response_message"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the success indicator should
	 * be saved.
	 **************************************************************************/
	private static final String SAVE_SUCCESSFUL_PROP = "jmeter.save.saveservice.successful"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the thread name should be
	 * saved.
	 **************************************************************************/
	private static final String SAVE_THREAD_NAME_PROP = "jmeter.save.saveservice.thread_name"; // $NON_NLS-1$

	// Save bytes read
	private static final String SAVE_BYTES_PROP = "jmeter.save.saveservice.bytes"; // $NON_NLS-1$

	// Save URL
	private static final String SAVE_URL_PROP = "jmeter.save.saveservice.url"; // $NON_NLS-1$

	// Save fileName for ResultSaver
	private static final String SAVE_FILENAME_PROP = "jmeter.save.saveservice.filename"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property indicating whether the time should be saved.
	 **************************************************************************/
	private static final String SAVE_TIME_PROP = "jmeter.save.saveservice.time"; // $NON_NLS-1$

	/***************************************************************************
	 * The name of the property giving the format of the time stamp
	 **************************************************************************/
	private static final String TIME_STAMP_FORMAT_PROP = "jmeter.save.saveservice.timestamp_format"; // $NON_NLS-1$

    private static final String SUBRESULTS_PROP      = "jmeter.save.saveservice.subresults"; // $NON_NLS-1$
    private static final String ASSERTIONS_PROP      = "jmeter.save.saveservice.assertions"; // $NON_NLS-1$
    private static final String LATENCY_PROP         = "jmeter.save.saveservice.latency"; // $NON_NLS-1$
    private static final String SAMPLERDATA_PROP     = "jmeter.save.saveservice.samplerData"; // $NON_NLS-1$
    private static final String RESPONSEHEADERS_PROP = "jmeter.save.saveservice.responseHeaders"; // $NON_NLS-1$
    private static final String REQUESTHEADERS_PROP  = "jmeter.save.saveservice.requestHeaders"; // $NON_NLS-1$
    private static final String ENCODING_PROP        = "jmeter.save.saveservice.encoding"; // $NON_NLS-1$
    

    // optional processing instruction for line 2; e.g.
    // <?xml-stylesheet type="text/xsl" href="../extras/jmeter-results-detail-report_21.xsl"?>
    private static final String XML_PI               = "jmeter.save.saveservice.xml_pi"; // $NON_NLS-1$

    private static final String SAVE_THREAD_COUNTS = "jmeter.save.saveservice.thread_counts"; // $NON_NLS-1$

    private static final String SAVE_SAMPLE_COUNT = "jmeter.save.saveservice.sample_count"; // $NON_NLS-1$

    // N.B. Remember to update the equals and hashCode methods when adding new variables.
    
	// Initialise values from properties
	private boolean time = _time, latency = _latency, timestamp = _timestamp, success = _success, label = _label,
			code = _code, message = _message, threadName = _threadName, dataType = _dataType, encoding = _encoding,
			assertions = _assertions, subresults = _subresults, responseData = _responseData,
			samplerData = _samplerData, xml = _xml, fieldNames = _fieldNames, responseHeaders = _responseHeaders,
			requestHeaders = _requestHeaders, responseDataOnError = _responseDataOnError;

	private boolean saveAssertionResultsFailureMessage = _saveAssertionResultsFailureMessage;

	private boolean url = _url, bytes = _bytes , fileName = _fileName;
	
    private boolean threadCounts = _threadCounts;

    private boolean sampleCount = _sampleCount;
    
    // Does not appear to be used (yet)
	private int assertionsResultsToSave = _assertionsResultsToSave;


	// Don't save this, as it is derived from the time format
	private boolean printMilliseconds = _printMilliseconds;

	/** A formatter for the time stamp. */
	private transient DateFormat formatter = _formatter;
    /* Make transient as we don't want to save the SimpleDataFormat class
     * Also, there's currently no way to change the value via the GUI, so changing it
     * later means editting the JMX, or recreating the Listener.
     */

	// Defaults from properties:
	private static final boolean _time, _timestamp, _success, _label, _code, _message, _threadName, _xml,
			_responseData, _dataType, _encoding, _assertions, _latency, _subresults, _samplerData, _fieldNames,
			_responseHeaders, _requestHeaders;

	private static final boolean _responseDataOnError;

	private static final boolean _saveAssertionResultsFailureMessage;

	private static final String _timeStampFormat;

	private static int _assertionsResultsToSave;

	// TODO turn into method?
	public static final int SAVE_NO_ASSERTIONS = 0;

	public static final int SAVE_FIRST_ASSERTION = SAVE_NO_ASSERTIONS + 1;

	public static final int SAVE_ALL_ASSERTIONS = SAVE_FIRST_ASSERTION + 1;

	private static final boolean _printMilliseconds;

	private static final boolean _bytes;

	private static final boolean _url;
	
	private static final boolean _fileName;

    private static final boolean _threadCounts;
    
    private static final boolean _sampleCount;
    
	private static final DateFormat _formatter;

	/**
	 * The string used to separate fields when stored to disk, for example, the
	 * comma for CSV files.
	 */
	private static final String _delimiter;

	private static final String DEFAULT_DELIMITER = ","; // $NON_NLS-1$

	/**
	 * Read in the properties having to do with saving from a properties file.
	 */
	static {
		Properties props = JMeterUtils.getJMeterProperties();

        _subresults      = TRUE.equalsIgnoreCase(props.getProperty(SUBRESULTS_PROP, TRUE));
        _assertions      = TRUE.equalsIgnoreCase(props.getProperty(ASSERTIONS_PROP, TRUE));
        _latency         = TRUE.equalsIgnoreCase(props.getProperty(LATENCY_PROP, TRUE));
        _samplerData     = TRUE.equalsIgnoreCase(props.getProperty(SAMPLERDATA_PROP, FALSE));
        _responseHeaders = TRUE.equalsIgnoreCase(props.getProperty(RESPONSEHEADERS_PROP, FALSE));
        _requestHeaders  = TRUE.equalsIgnoreCase(props.getProperty(REQUESTHEADERS_PROP, FALSE));
        _encoding        = TRUE.equalsIgnoreCase(props.getProperty(ENCODING_PROP, FALSE));

		_delimiter = props.getProperty(DEFAULT_DELIMITER_PROP, DEFAULT_DELIMITER);

		_fieldNames = TRUE.equalsIgnoreCase(props.getProperty(PRINT_FIELD_NAMES_PROP, FALSE));

		_dataType = TRUE.equalsIgnoreCase(props.getProperty(SAVE_DATA_TYPE_PROP, TRUE));

		_label = TRUE.equalsIgnoreCase(props.getProperty(SAVE_LABEL_PROP, TRUE));

		_code = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_CODE_PROP, TRUE));

		_responseData = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_PROP, FALSE));

		_responseDataOnError = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_ON_ERROR_PROP, FALSE));

		_message = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_MESSAGE_PROP, TRUE));

		_success = TRUE.equalsIgnoreCase(props.getProperty(SAVE_SUCCESSFUL_PROP, TRUE));

		_threadName = TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_NAME_PROP, TRUE));

		_bytes = TRUE.equalsIgnoreCase(props.getProperty(SAVE_BYTES_PROP, TRUE));
		
		_url = TRUE.equalsIgnoreCase(props.getProperty(SAVE_URL_PROP, FALSE));

		_fileName = TRUE.equalsIgnoreCase(props.getProperty(SAVE_FILENAME_PROP, FALSE));

		_time = TRUE.equalsIgnoreCase(props.getProperty(SAVE_TIME_PROP, TRUE));

		_timeStampFormat = props.getProperty(TIME_STAMP_FORMAT_PROP, MILLISECONDS);

		_printMilliseconds = MILLISECONDS.equalsIgnoreCase(_timeStampFormat);

		// Prepare for a pretty date
		if (!_printMilliseconds && !NONE.equalsIgnoreCase(_timeStampFormat) && (_timeStampFormat != null)) {
			_formatter = new SimpleDateFormat(_timeStampFormat);
		} else {
			_formatter = null;
		}

		_timestamp = !NONE.equalsIgnoreCase(_timeStampFormat);// reversed compare allows for null

		_saveAssertionResultsFailureMessage = TRUE.equalsIgnoreCase(props.getProperty(
				ASSERTION_RESULTS_FAILURE_MESSAGE_PROP, FALSE));

		String whichAssertionResults = props.getProperty(ASSERTION_RESULTS_PROP, NONE);
		if (NONE.equals(whichAssertionResults)) {
			_assertionsResultsToSave = SAVE_NO_ASSERTIONS;
		} else if (FIRST.equals(whichAssertionResults)) {
			_assertionsResultsToSave = SAVE_FIRST_ASSERTION;
		} else if (ALL.equals(whichAssertionResults)) {
			_assertionsResultsToSave = SAVE_ALL_ASSERTIONS;
		}

		String howToSave = props.getProperty(OUTPUT_FORMAT_PROP, XML);

		if (XML.equals(howToSave)) {
			_xml = true;
		} else {
			_xml = false;
		}

        _threadCounts=TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_COUNTS, FALSE));

        _sampleCount=TRUE.equalsIgnoreCase(props.getProperty(SAVE_SAMPLE_COUNT, FALSE));
	}

	// Don't save this, as not settable via GUI
	private String delimiter = _delimiter;
	private static final SampleSaveConfiguration _static = new SampleSaveConfiguration();

	// Give access to initial configuration
	public static SampleSaveConfiguration staticConfig() {
		return _static;
	}

	public SampleSaveConfiguration() {
	}

	/**
	 * Alternate constructor for use by OldSaveService
	 * 
	 * @param value initial setting for boolean fields used in Config dialogue
	 */
	public SampleSaveConfiguration(boolean value) {
		assertions = value;
		bytes = value;
		code = value;
		dataType = value;
		encoding = value;
		fieldNames = value;
		fileName = value;
		label = value;
		latency = value;
		message = value;
		printMilliseconds = _printMilliseconds;//is derived from properties only
		requestHeaders = value;
		responseData = value;
		responseDataOnError = value;
		responseHeaders = value;
		samplerData = value;
		saveAssertionResultsFailureMessage = value;
		subresults = value;
		success = value;
		threadCounts = value;
		sampleCount = value;
		threadName = value;
		time = value;
		timestamp = value;
		url = value;
		xml = value;
	}

    private Object readResolve() throws ObjectStreamException{
	   formatter = _formatter;
       return this;
    }

    public Object clone() {
        try {
            SampleSaveConfiguration clone = (SampleSaveConfiguration)super.clone();
            if(this.formatter != null) {
                clone.formatter = (SimpleDateFormat)this.formatter.clone();
            }
            return clone;
        }
        catch(CloneNotSupportedException e) {
            // this should not happen
            return null;
        }
	}
    
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        // We know we are comparing to another SampleSaveConfiguration
        SampleSaveConfiguration s = (SampleSaveConfiguration)obj;
        boolean primitiveValues = s.time == time &&
            s.latency == latency && 
            s.timestamp == timestamp &&
            s.success == success &&
            s.label == label &&
            s.code == code &&
            s.message == message &&
            s.threadName == threadName &&
            s.dataType == dataType &&
            s.encoding == encoding &&
            s.assertions == assertions &&
            s.subresults == subresults &&
            s.responseData == responseData &&
            s.samplerData == samplerData &&
            s.xml == xml &&
            s.fieldNames == fieldNames &&
            s.responseHeaders == responseHeaders &&
            s.requestHeaders == requestHeaders &&
            s.assertionsResultsToSave == assertionsResultsToSave &&
            s.saveAssertionResultsFailureMessage == saveAssertionResultsFailureMessage &&
            s.printMilliseconds == printMilliseconds &&
            s.responseDataOnError == responseDataOnError &&
            s.url == url &&
            s.bytes == bytes &&
            s.fileName == fileName &&
            s.sampleCount == sampleCount &&
            s.threadCounts == threadCounts;
        
        boolean stringValues = false;
        if(primitiveValues) {
            stringValues = s.delimiter == delimiter || (delimiter != null && delimiter.equals(s.delimiter));
        }
        boolean complexValues = false;
        if(primitiveValues && stringValues) {
            complexValues = s.formatter == formatter || (formatter != null && formatter.equals(s.formatter));
        }
        
        return primitiveValues && stringValues && complexValues;
    }
    
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (time ? 1 : 0);
        hash = 31 * hash + (latency ? 1 : 0);
        hash = 31 * hash + (timestamp ? 1 : 0);
        hash = 31 * hash + (success ? 1 : 0);
        hash = 31 * hash + (label ? 1 : 0);
        hash = 31 * hash + (code ? 1 : 0);
        hash = 31 * hash + (message ? 1 : 0);
        hash = 31 * hash + (threadName ? 1 : 0);
        hash = 31 * hash + (dataType ? 1 : 0);
        hash = 31 * hash + (encoding ? 1 : 0);
        hash = 31 * hash + (assertions ? 1 : 0);
        hash = 31 * hash + (subresults ? 1 : 0);
        hash = 31 * hash + (responseData ? 1 : 0);
        hash = 31 * hash + (samplerData ? 1 : 0);
        hash = 31 * hash + (xml ? 1 : 0);
        hash = 31 * hash + (fieldNames ? 1 : 0);
        hash = 31 * hash + (responseHeaders ? 1 : 0);
        hash = 31 * hash + (requestHeaders ? 1 : 0);
        hash = 31 * hash + assertionsResultsToSave;
        hash = 31 * hash + (saveAssertionResultsFailureMessage ? 1 : 0);
        hash = 31 * hash + (printMilliseconds ? 1 : 0);
        hash = 31 * hash + (responseDataOnError ? 1 : 0);
        hash = 31 * hash + (url ? 1 : 0);
        hash = 31 * hash + (bytes ? 1 : 0);
        hash = 31 * hash + (fileName ? 1 : 0);
        hash = 31 * hash + (threadCounts ? 1 : 0);
        hash = 31 * hash + (delimiter != null  ? delimiter.hashCode() : 0);
        hash = 31 * hash + (formatter != null  ? formatter.hashCode() : 0);
        hash = 31 * hash + (sampleCount ? 1 : 0);
        
        return hash;
    }

    ///////////////////// Start of standard save/set access methods /////////////////////
    
	public boolean saveResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(boolean r) {
		responseHeaders = r;
	}

	public boolean saveRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(boolean r) {
		requestHeaders = r;
	}

	public boolean saveAssertions() {
		return assertions;
	}

	public void setAssertions(boolean assertions) {
		this.assertions = assertions;
	}

	public boolean saveCode() {
		return code;
	}

	public void setCode(boolean code) {
		this.code = code;
	}

	public boolean saveDataType() {
		return dataType;
	}

	public void setDataType(boolean dataType) {
		this.dataType = dataType;
	}

	public boolean saveEncoding() {
		return encoding;
	}

	public void setEncoding(boolean encoding) {
		this.encoding = encoding;
	}

	public boolean saveLabel() {
		return label;
	}

	public void setLabel(boolean label) {
		this.label = label;
	}

	public boolean saveLatency() {
		return latency;
	}

	public void setLatency(boolean latency) {
		this.latency = latency;
	}

	public boolean saveMessage() {
		return message;
	}

	public void setMessage(boolean message) {
		this.message = message;
	}

	public boolean saveResponseData(SampleResult res) {
		return responseData || TestPlan.getFunctionalMode() || (responseDataOnError && !res.isSuccessful());
	}
    
    public boolean saveResponseData()
    {
        return responseData;
    }

	public void setResponseData(boolean responseData) {
		this.responseData = responseData;
	}

	public boolean saveSamplerData(SampleResult res) {
		return samplerData || TestPlan.getFunctionalMode() // as per 2.0 branch
				|| (responseDataOnError && !res.isSuccessful());
	}
    
    public boolean saveSamplerData()
    {
        return samplerData;
    }

	public void setSamplerData(boolean samplerData) {
		this.samplerData = samplerData;
	}

	public boolean saveSubresults() {
		return subresults;
	}

	public void setSubresults(boolean subresults) {
		this.subresults = subresults;
	}

	public boolean saveSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean saveThreadName() {
		return threadName;
	}

	public void setThreadName(boolean threadName) {
		this.threadName = threadName;
	}

	public boolean saveTime() {
		return time;
	}

	public void setTime(boolean time) {
		this.time = time;
	}

	public boolean saveTimestamp() {
		return timestamp;
	}

	public void setTimestamp(boolean timestamp) {
		this.timestamp = timestamp;
	}

	public boolean saveAsXml() {
		return xml;
	}

	public void setAsXml(boolean xml) {
		this.xml = xml;
	}

	public boolean saveFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(boolean printFieldNames) {
		this.fieldNames = printFieldNames;
	}

	public boolean saveUrl() {
		return url;
	}

	public void setUrl(boolean save) {
		this.url = save;
	}

	public boolean saveBytes() {
		return bytes;
	}

	public void setBytes(boolean save) {
		this.bytes = save;
	}

	public boolean saveFileName() {
		return fileName;
	}

	public void setFileName(boolean save) {
		this.fileName = save;
	}

	public boolean saveAssertionResultsFailureMessage() {
		return saveAssertionResultsFailureMessage;
	}

	public void setAssertionResultsFailureMessage(boolean b) {
		saveAssertionResultsFailureMessage = b;
	}

    public boolean saveThreadCounts() {
        return threadCounts;
    }

    public void setThreadCounts(boolean save) {
        this.threadCounts = save;
    }

    public boolean saveSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(boolean save) {
        this.sampleCount = save;
    }

	///////////////// End of standard field accessors /////////////////////
	
    /**
     * Only intended for use by OldSaveService (and test cases)
     */
    public void setFormatter(DateFormat fmt){
    	printMilliseconds = "ms".equals(fmt);// $NON-NLS-1$ maintain relationship
    	formatter = fmt;
    }
    
	public boolean printMilliseconds() {
		return printMilliseconds;
	}

	public DateFormat formatter() {
		return formatter;
	}

	public int assertionsResultsToSave() {
		return assertionsResultsToSave;
	}

	public String getDelimiter() {
		return delimiter;
	}

    public String getXmlPi() {
        return JMeterUtils.getJMeterProperties().getProperty(XML_PI, ""); // Defaults to empty;
    }

    // Used by SampleSaveConfigurationConverter.unmarshall()
	public void setDefaultDelimiter() {
		delimiter=_delimiter;
	}

    // Used by SampleSaveConfigurationConverter.unmarshall()
	public void setDefaultTimeStampFormat() {
		printMilliseconds=_printMilliseconds;
		formatter=_formatter;
	}
}