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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * N.B. to add a new field, remember the following
 * - static _xyz
 * - instance xyz=_xyz
 * - clone s.xyz = xyz (perhaps)
 * - setXyz(boolean)
 * - saveXyz()
 * - add Xyz to SAVE_CONFIG_NAMES list
 * - update SampleSaveConfigurationConverter to add new fields to marshall() and shouldSerialiseMember()
 * - update ctor SampleSaveConfiguration(boolean value) to set the value if it is a boolean property
 * - update SampleResultConverter and/or HTTPSampleConverter
 * - update CSVSaveService: CSV_XXXX, makeResultFromDelimitedString, printableFieldNamesToString, static{}
 * - update messages.properties to add save_xyz entry
 * - update jmeter.properties to add new property
 * - update listeners.xml to add new property, CSV and XML names etc.
 * - take screenshot sample_result_config.png
 * - update listeners.xml and component_reference.xml with new dimensions (might not change)
 */

/**
 * Holds details of which sample attributes to save.
 *
 * The pop-up dialogue for this is created by the class SavePropertyDialog, which assumes:
 * <p>
 * For each field <em>XXX</em>
 * <ul>
 *  <li>methods have the signature "boolean save<em>XXX</em>()"</li>
 *  <li>a corresponding "void set<em>XXX</em>(boolean)" method</li>
 *  <li>messages.properties contains the key save_<em>XXX</em></li>
 * </ul>
 */
public class SampleSaveConfiguration implements Cloneable, Serializable {
    private static final long serialVersionUID = 8L;

    private static final Logger log = LoggerFactory.getLogger(SampleSaveConfiguration.class);

    // ---------------------------------------------------------------------
    // PROPERTY FILE CONSTANTS
    // ---------------------------------------------------------------------

    /** Indicates that the results file should be in XML format. * */
    private static final String XML = "xml"; // $NON_NLS-1$

    /** Indicates that the results file should be in CSV format. * */
    private static final String CSV = "csv"; // $NON_NLS-1$

    /** A properties file indicator for true. * */
    private static final String TRUE = "true"; // $NON_NLS-1$

    /** A properties file indicator for false. * */
    private static final String FALSE = "false"; // $NON_NLS-1$

    /** A properties file indicator for milliseconds. * */
    public static final String MILLISECONDS = "ms"; // $NON_NLS-1$

    /** A properties file indicator for none. * */
    public static final String NONE = "none"; // $NON_NLS-1$

    /** A properties file indicator for the first of a series. * */
    private static final String FIRST = "first"; // $NON_NLS-1$

    /** A properties file indicator for all of a series. * */
    private static final String ALL = "all"; // $NON_NLS-1$

    /***************************************************************************
     * The name of the property indicating which assertion results should be
     * saved.
     **************************************************************************/
    public static final String ASSERTION_RESULTS_FAILURE_MESSAGE_PROP =
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
    public static final String DEFAULT_DELIMITER_PROP = "jmeter.save.saveservice.default_delimiter"; // $NON_NLS-1$

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

    // Save bytes written
    private static final String SAVE_SENT_BYTES_PROP = "jmeter.save.saveservice.sent_bytes"; // $NON_NLS-1$

    // Save URL
    private static final String SAVE_URL_PROP = "jmeter.save.saveservice.url"; // $NON_NLS-1$

    // Save fileName for ResultSaver
    private static final String SAVE_FILENAME_PROP = "jmeter.save.saveservice.filename"; // $NON_NLS-1$

    // Save hostname for ResultSaver
    private static final String SAVE_HOSTNAME_PROP = "jmeter.save.saveservice.hostname"; // $NON_NLS-1$

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
    private static final String CONNECT_TIME_PROP    = "jmeter.save.saveservice.connect_time"; // $NON_NLS-1$
    private static final String SAMPLERDATA_PROP     = "jmeter.save.saveservice.samplerData"; // $NON_NLS-1$
    private static final String RESPONSEHEADERS_PROP = "jmeter.save.saveservice.responseHeaders"; // $NON_NLS-1$
    private static final String REQUESTHEADERS_PROP  = "jmeter.save.saveservice.requestHeaders"; // $NON_NLS-1$
    private static final String ENCODING_PROP        = "jmeter.save.saveservice.encoding"; // $NON_NLS-1$


    // optional processing instruction for line 2; e.g.
    // <?xml-stylesheet type="text/xsl" href="../extras/jmeter-results-detail-report_21.xsl"?>
    private static final String XML_PI               = "jmeter.save.saveservice.xml_pi"; // $NON_NLS-1$

    private static final String SAVE_THREAD_COUNTS   = "jmeter.save.saveservice.thread_counts"; // $NON_NLS-1$

    private static final String SAVE_SAMPLE_COUNT    = "jmeter.save.saveservice.sample_count"; // $NON_NLS-1$

    private static final String SAVE_IDLE_TIME       = "jmeter.save.saveservice.idle_time"; // $NON_NLS-1$

    // Defaults from properties:
    private static final boolean TIME;
    private static final boolean TIMESTAMP;
    private static final boolean SUCCESS;
    private static final boolean LABEL;
    private static final boolean CODE;
    private static final boolean MESSAGE;
    private static final boolean THREAD_NAME;
    private static final boolean IS_XML;
    private static final boolean RESPONSE_DATA;
    private static final boolean DATATYPE;
    private static final boolean ENCODING;
    private static final boolean ASSERTIONS;
    private static final boolean LATENCY;
    private static final boolean CONNECT_TIME;
    private static final boolean SUB_RESULTS;
    private static final boolean SAMPLER_DATA;
    private static final boolean FIELD_NAMES;
    private static final boolean RESPONSE_HEADERS;
    private static final boolean REQUEST_HEADERS;

    private static final boolean RESPONSE_DATA_ON_ERROR;

    private static final boolean SAVE_ASSERTION_RESULTS_FAILURE_MESSAGE;

    private static final int ASSERTIONS_RESULT_TO_SAVE;

    // TODO turn into method?
    public static final int SAVE_NO_ASSERTIONS = 0;

    public static final int SAVE_FIRST_ASSERTION = SAVE_NO_ASSERTIONS + 1;

    public static final int SAVE_ALL_ASSERTIONS = SAVE_FIRST_ASSERTION + 1;

    private static final boolean PRINT_MILLISECONDS;

    private static final boolean BYTES;

    private static final boolean SENT_BYTES;

    private static final boolean URL;

    private static final boolean FILE_NAME;

    private static final boolean HOST_NAME;

    private static final boolean THREAD_COUNTS;

    private static final boolean SAMPLE_COUNT;

    private static final String DATE_FORMAT;

    /**
     * The string used to separate fields when stored to disk, for example, the
     * comma for CSV files.
     */
    private static final String DELIMITER;

    private static final boolean IDLE_TIME;

    public static final String DEFAULT_DELIMITER = ","; // $NON_NLS-1$

    // Read in the properties having to do with saving from a properties file.
    static {
        Properties props = JMeterUtils.getJMeterProperties();

        SUB_RESULTS      = TRUE.equalsIgnoreCase(props.getProperty(SUBRESULTS_PROP, TRUE));
        ASSERTIONS      = TRUE.equalsIgnoreCase(props.getProperty(ASSERTIONS_PROP, TRUE));
        LATENCY         = TRUE.equalsIgnoreCase(props.getProperty(LATENCY_PROP, TRUE));
        CONNECT_TIME     = TRUE.equalsIgnoreCase(props.getProperty(CONNECT_TIME_PROP, TRUE));
        SAMPLER_DATA     = TRUE.equalsIgnoreCase(props.getProperty(SAMPLERDATA_PROP, FALSE));
        RESPONSE_HEADERS = TRUE.equalsIgnoreCase(props.getProperty(RESPONSEHEADERS_PROP, FALSE));
        REQUEST_HEADERS  = TRUE.equalsIgnoreCase(props.getProperty(REQUESTHEADERS_PROP, FALSE));
        ENCODING        = TRUE.equalsIgnoreCase(props.getProperty(ENCODING_PROP, FALSE));

        String dlm = JMeterUtils.getDelimiter(props.getProperty(DEFAULT_DELIMITER_PROP, DEFAULT_DELIMITER));
        char ch = dlm.charAt(0);

        if (CharUtils.isAsciiAlphanumeric(ch) || ch == CSVSaveService.QUOTING_CHAR){
            throw new JMeterError("Delimiter '"+ch+"' must not be alphanumeric or "+CSVSaveService.QUOTING_CHAR+".");
        }

        if (ch != '\t' && !CharUtils.isAsciiPrintable(ch)){
            throw new JMeterError("Delimiter (code "+(int)ch+") must be printable.");
        }

        DELIMITER = dlm;

        FIELD_NAMES = TRUE.equalsIgnoreCase(props.getProperty(PRINT_FIELD_NAMES_PROP, TRUE));

        DATATYPE = TRUE.equalsIgnoreCase(props.getProperty(SAVE_DATA_TYPE_PROP, TRUE));

        LABEL = TRUE.equalsIgnoreCase(props.getProperty(SAVE_LABEL_PROP, TRUE));

        CODE = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_CODE_PROP, TRUE));

        RESPONSE_DATA = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_PROP, FALSE));

        RESPONSE_DATA_ON_ERROR = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_ON_ERROR_PROP, FALSE));

        MESSAGE = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_MESSAGE_PROP, TRUE));

        SUCCESS = TRUE.equalsIgnoreCase(props.getProperty(SAVE_SUCCESSFUL_PROP, TRUE));

        THREAD_NAME = TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_NAME_PROP, TRUE));

        BYTES = TRUE.equalsIgnoreCase(props.getProperty(SAVE_BYTES_PROP, TRUE));

        SENT_BYTES = TRUE.equalsIgnoreCase(props.getProperty(SAVE_SENT_BYTES_PROP, TRUE));

        URL = TRUE.equalsIgnoreCase(props.getProperty(SAVE_URL_PROP, TRUE));

        FILE_NAME = TRUE.equalsIgnoreCase(props.getProperty(SAVE_FILENAME_PROP, FALSE));

        HOST_NAME = TRUE.equalsIgnoreCase(props.getProperty(SAVE_HOSTNAME_PROP, FALSE));

        TIME = TRUE.equalsIgnoreCase(props.getProperty(SAVE_TIME_PROP, TRUE));

        String temporaryTimestampFormat = props.getProperty(TIME_STAMP_FORMAT_PROP, MILLISECONDS);

        PRINT_MILLISECONDS = MILLISECONDS.equalsIgnoreCase(temporaryTimestampFormat);

        if (!PRINT_MILLISECONDS && !NONE.equalsIgnoreCase(temporaryTimestampFormat)) {
            DATE_FORMAT = validateFormat(temporaryTimestampFormat);
        } else {
            DATE_FORMAT = null;
        }

        TIMESTAMP = !NONE.equalsIgnoreCase(temporaryTimestampFormat);// reversed compare allows for null

        SAVE_ASSERTION_RESULTS_FAILURE_MESSAGE = TRUE.equalsIgnoreCase(props.getProperty(
                ASSERTION_RESULTS_FAILURE_MESSAGE_PROP, TRUE));

        String whichAssertionResults = props.getProperty(ASSERTION_RESULTS_PROP, NONE);
        if (NONE.equals(whichAssertionResults)) {
            ASSERTIONS_RESULT_TO_SAVE = SAVE_NO_ASSERTIONS;
        } else if (FIRST.equals(whichAssertionResults)) {
            ASSERTIONS_RESULT_TO_SAVE = SAVE_FIRST_ASSERTION;
        } else if (ALL.equals(whichAssertionResults)) {
            ASSERTIONS_RESULT_TO_SAVE = SAVE_ALL_ASSERTIONS;
        } else {
            ASSERTIONS_RESULT_TO_SAVE = 0;
        }

        String howToSave = props.getProperty(OUTPUT_FORMAT_PROP, CSV);

        if (XML.equals(howToSave)) {
            IS_XML = true;
        } else {
            if (!CSV.equals(howToSave)) {
                log.warn("{} has unexepected value: '{}' - assuming 'csv' format", OUTPUT_FORMAT_PROP, howToSave);
            }
            IS_XML = false;
        }

        THREAD_COUNTS=TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_COUNTS, TRUE));

        SAMPLE_COUNT=TRUE.equalsIgnoreCase(props.getProperty(SAVE_SAMPLE_COUNT, FALSE));

        IDLE_TIME=TRUE.equalsIgnoreCase(props.getProperty(SAVE_IDLE_TIME, TRUE));
    }

    private static final SampleSaveConfiguration STATIC_SAVE_CONFIGURATION = new SampleSaveConfiguration();

    // for test code only
    static final String CONFIG_GETTER_PREFIX = "save";  // $NON-NLS-1$

    // for test code only
    static final String CONFIG_SETTER_PREFIX = "set";  // $NON-NLS-1$

    /**
     * List of saveXXX/setXXX(boolean) methods which is used to build the Sample Result Save Configuration dialog.
     * New method names should be added at the end so that existing layouts are not affected.
     */
    // The current order is derived from http://jmeter.apache.org/usermanual/listeners.html#csvlogformat
    // TODO this may not be the ideal order; fix further and update the screenshot(s)
    public static final List<String> SAVE_CONFIG_NAMES = Collections.unmodifiableList(Arrays.asList(new String[]{
        "AsXml",
        "FieldNames", // CSV
        "Timestamp",
        "Time", // elapsed
        "Label",
        "Code", // Response Code
        "Message", // Response Message
        "ThreadName",
        "DataType",
        "Success",
        "AssertionResultsFailureMessage",
        "Bytes",
        "SentBytes",
        "ThreadCounts", // grpThreads and allThreads
        "Url",
        "FileName",
        "Latency",
        "ConnectTime",
        "Encoding",
        "SampleCount", // Sample and Error Count
        "Hostname",
        "IdleTime",
        "RequestHeaders", // XML
        "SamplerData", // XML
        "ResponseHeaders", // XML
        "ResponseData", // XML
        "Subresults", // XML
        "Assertions", // XML
    }));
    // N.B. Remember to update the equals and hashCode methods when adding new variables.

    // Initialise values from properties
    private boolean time = TIME;
    private boolean latency = LATENCY;
    private boolean connectTime=CONNECT_TIME;
    private boolean timestamp = TIMESTAMP;
    private boolean success = SUCCESS;
    private boolean label = LABEL;
    private boolean code = CODE;
    private boolean message = MESSAGE;
    private boolean threadName = THREAD_NAME;
    private boolean dataType = DATATYPE;
    private boolean encoding = ENCODING;
    private boolean assertions = ASSERTIONS;
    private boolean subresults = SUB_RESULTS;
    private boolean responseData = RESPONSE_DATA;
    private boolean samplerData = SAMPLER_DATA;
    private boolean xml = IS_XML;
    private boolean fieldNames = FIELD_NAMES;
    private boolean responseHeaders = RESPONSE_HEADERS;
    private boolean requestHeaders = REQUEST_HEADERS;
    private boolean responseDataOnError = RESPONSE_DATA_ON_ERROR;

    private boolean saveAssertionResultsFailureMessage = SAVE_ASSERTION_RESULTS_FAILURE_MESSAGE;

    private boolean url = URL;
    private boolean bytes = BYTES;
    private boolean sentBytes = SENT_BYTES;
    private boolean fileName = FILE_NAME;

    private boolean hostname = HOST_NAME;

    private boolean threadCounts = THREAD_COUNTS;

    private boolean sampleCount = SAMPLE_COUNT;

    private boolean idleTime = IDLE_TIME;

    // Does not appear to be used (yet)
    private int assertionsResultsToSave = ASSERTIONS_RESULT_TO_SAVE;

    // Don't save this, as it is derived from the time format
    private boolean printMilliseconds = PRINT_MILLISECONDS;

    private transient String dateFormat = DATE_FORMAT;

    /** A formatter for the time stamp.
     * Make transient as we don't want to save the FastDateFormat class
     * Also, there's currently no way to change the value via the GUI, so changing it
     * later means editing the JMX, or recreating the Listener.
     */
    private transient FastDateFormat timestampFormatter =
        dateFormat != null ? FastDateFormat.getInstance(dateFormat) : null;

    // Don't save this, as not settable via GUI
    private String delimiter = DELIMITER;

    // Don't save this - only needed for processing CSV headers currently
    private transient int varCount = 0;

    public SampleSaveConfiguration() {
    }

    /**
     * Alternate constructor for use by CsvSaveService
     *
     * @param value initial setting for boolean fields used in Config dialogue
     */
    public SampleSaveConfiguration(boolean value) {
        assertions = value;
        bytes = value;
        code = value;
        connectTime = value;
        dataType = value;
        encoding = value;
        fieldNames = value;
        fileName = value;
        hostname = value;
        idleTime = value;
        label = value;
        latency = value;
        message = value;
        printMilliseconds = PRINT_MILLISECONDS;//is derived from properties only
        requestHeaders = value;
        responseData = value;
        responseDataOnError = value;
        responseHeaders = value;
        sampleCount = value;
        samplerData = value;
        saveAssertionResultsFailureMessage = value;
        sentBytes = value;
        subresults = value;
        success = value;
        threadCounts = value;
        threadName = value;
        time = value;
        timestamp = value;
        url = value;
        xml = value;
    }

    public int getVarCount() { // Only for use by CSVSaveService
        return varCount;
    }

    public void setVarCount(int varCount) { // Only for use by CSVSaveService
        this.varCount = varCount;
    }

    // Give access to initial configuration
    public static SampleSaveConfiguration staticConfig() {
        return STATIC_SAVE_CONFIGURATION;
    }

    /**
     * Convert a config name to the method name of the getter.
     * The getter method returns a boolean.
     * @param configName the config name
     * @return the getter method name
     */
    public static final String getterName(String configName) {
        return CONFIG_GETTER_PREFIX + configName;
    }

    /**
     * Convert a config name to the method name of the setter
     * The setter method requires a boolean parameter.
     * @param configName the config name
     * @return the setter method name
     */
    public static final String setterName(String configName) {
        return CONFIG_SETTER_PREFIX + configName;
    }

    /**
     * Validate pattern
     * @param temporaryTimestampFormat DateFormat pattern
     * @return format if ok or null
     */
    private static String validateFormat(String temporaryTimestampFormat) {
        try {
            new SimpleDateFormat(temporaryTimestampFormat);
            if(log.isDebugEnabled()) {
                log.debug("Successfully validated pattern value {} for property {}",
                        temporaryTimestampFormat, TIME_STAMP_FORMAT_PROP);
            }
            return temporaryTimestampFormat;
        } catch(IllegalArgumentException ex) {
            log.error("Invalid pattern value {} for property {}", temporaryTimestampFormat, TIME_STAMP_FORMAT_PROP,
                    ex);
            return null;
        }
    }


    private Object readResolve(){
        setupDateFormat(DATE_FORMAT);
        return this;
    }

    /**
     * Initialize threadSafeLenientFormatter
     * @param pDateFormat String date format
     */
    private void setupDateFormat(String pDateFormat) {
        this.dateFormat = pDateFormat;
        if(dateFormat != null) {
            this.timestampFormatter = FastDateFormat.getInstance(dateFormat);
        } else {
            this.timestampFormatter = null;
        }
    }

    @Override
    public Object clone() {
        try {
            SampleSaveConfiguration clone = (SampleSaveConfiguration)super.clone();
            if(this.dateFormat != null) {
                clone.timestampFormatter = (FastDateFormat)this.threadSafeLenientFormatter().clone();
            }
            return clone;
        }
        catch(CloneNotSupportedException e) {
            throw new RuntimeException("Should not happen",e);
        }
    }

    @Override
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
            s.connectTime == connectTime &&
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
            s.sentBytes == sentBytes &&
            s.fileName == fileName &&
            s.hostname == hostname &&
            s.sampleCount == sampleCount &&
            s.idleTime == idleTime &&
            s.threadCounts == threadCounts;

        boolean stringValues = false;
        if(primitiveValues) {
            stringValues = Objects.equals(delimiter, s.delimiter);
        }
        boolean complexValues = false;
        if(primitiveValues && stringValues) {
            complexValues = Objects.equals(dateFormat, s.dateFormat);
        }

        return primitiveValues && stringValues && complexValues;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (time ? 1 : 0);
        hash = 31 * hash + (latency ? 1 : 0);
        hash = 31 * hash + (connectTime ? 1 : 0);
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
        hash = 31 * hash + (sentBytes ? 1 : 0);
        hash = 31 * hash + (fileName ? 1 : 0);
        hash = 31 * hash + (hostname ? 1 : 0);
        hash = 31 * hash + (threadCounts ? 1 : 0);
        hash = 31 * hash + (delimiter != null  ? delimiter.hashCode() : 0);
        hash = 31 * hash + (dateFormat != null  ? dateFormat.hashCode() : 0);
        hash = 31 * hash + (sampleCount ? 1 : 0);
        hash = 31 * hash + (idleTime ? 1 : 0);

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

    public boolean saveConnectTime() {
        return connectTime;
    }

    public void setConnectTime(boolean connectTime) {
        this.connectTime = connectTime;
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

    public boolean saveSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(boolean save) {
        this.sentBytes = save;
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
     * Intended for use by CsvSaveService (and test cases)
     * @param fmt
     *            format of the date to be saved. If <code>null</code>
     *            milliseconds since epoch will be printed
     */
    public void setDateFormat(String fmt){
        printMilliseconds = fmt == null; // maintain relationship
        setupDateFormat(fmt);
    }

    public boolean printMilliseconds() {
        return printMilliseconds;
    }

    /**
     * @return {@link DateFormat} non lenient
     */
    public DateFormat strictDateFormatter() {
        if(dateFormat != null) {
            return new SimpleDateFormat(dateFormat);
        } else {
            return null;
        }
    }

    /**
     * @return {@link FastDateFormat} Thread safe lenient formatter
     */
    public FastDateFormat threadSafeLenientFormatter() {
        // When restored by XStream threadSafeLenientFormatter may not have
        // been initialized
        if(timestampFormatter == null) {
            timestampFormatter =
                    dateFormat != null ? FastDateFormat.getInstance(dateFormat) : null;
        }
        return timestampFormatter;
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

    // Used by old Save service
    public void setDelimiter(String delim) {
        delimiter=delim;
    }

    // Used by SampleSaveConfigurationConverter.unmarshall()
    public void setDefaultDelimiter() {
        delimiter=DELIMITER;
    }

    // Used by SampleSaveConfigurationConverter.unmarshall()
    public void setDefaultTimeStampFormat() {
        printMilliseconds=PRINT_MILLISECONDS;
        setupDateFormat(DATE_FORMAT);
    }

    public boolean saveHostname(){
        return hostname;
    }

    public void setHostname(boolean save){
        hostname = save;
    }

    public boolean saveIdleTime() {
        return idleTime;
    }

    public void setIdleTime(boolean save) {
        idleTime = save;
    }
}
