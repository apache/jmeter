// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.save;


/**
 *  This interface defines a number of constants used in the properties file
 *  that is used to indicate which portions of the results will be
 *  stored in the results files.  It also contains constants representing XML
 *  tags, elements, etc.
 *  
 *
 *@author     <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell</a>
 *@version    $Revision$ $Date$
*/

public interface SaveServiceConstants
{
    // ---------------------------------------------------------------------
    // PROPERTY FILE CONSTANTS
    // ---------------------------------------------------------------------

    /** Indicates that the results file should be in XML format.  **/
    public static final String XML = "xml";

    /** Indicates that the results file should be in CSV format.  **/
    public static final String CSV = "csv";

    /** Indicates that the results should be stored in a database.  **/
    public static final String DATABASE = "db";

    /** A properties file indicator for true.  **/
    public static final String TRUE = "true";

    /** A properties file indicator for false.  **/
    public static final String FALSE = "false";

    /** A properties file indicator for milliseconds.  **/
    public static final String MILLISECONDS = "ms";

    /** A properties file indicator for none.  **/
    public static final String NONE = "none";

    /** A properties file indicator for the first of a series.  **/
    public static final String FIRST = "first";

    /** A properties file indicator for all of a series.  **/
    public static final String ALL = "all";

    /** The name of the property indicating which assertion results
     should be saved.  **/
    public static final String ASSERTION_RESULTS_FAILURE_MESSAGE_PROP
            = "jmeter.save.saveservice.assertion_results_failure_message";

    /** The name of the property indicating which assertion results
     should be saved.  **/
    public static final String ASSERTION_RESULTS_PROP
            = "jmeter.save.saveservice.assertion_results";

    /** The name of the property indicating which delimiter should be
        used when saving in a delimited values format.  **/
    public static final String DEFAULT_DELIMITER_PROP
            = "jmeter.save.saveservice.default_delimiter";

    /** The name of the property indicating which format should be
        used when saving the results, e.g., xml or csv.  **/
    public static final String OUTPUT_FORMAT_PROP
            = "jmeter.save.saveservice.output_format";

    /** The name of the property indicating whether field names should be
        printed to a delimited file.  **/
    public static final String PRINT_FIELD_NAMES_PROP
            = "jmeter.save.saveservice.print_field_names";

    /** Indicates that results should be saved as XML.  **/
    public static final int SAVE_AS_XML = 0;

    /** Indicates that results should be saved as comma-separated-values.  **/
    public static final int SAVE_AS_CSV = SAVE_AS_XML + 1;


    /** The name of the property indicating whether the data type
     should be saved.  **/
    public static final String SAVE_DATA_TYPE_PROP
            = "jmeter.save.saveservice.data_type";

    /** The name of the property indicating whether the label
     should be saved.  **/
    public static final String SAVE_LABEL_PROP
            = "jmeter.save.saveservice.label";

    /** The name of the property indicating whether the response code
     should be saved.  **/
    public static final String SAVE_RESPONSE_CODE_PROP
            = "jmeter.save.saveservice.response_code";

    /** The name of the property indicating whether the response data
     should be saved.  **/
    public static final String SAVE_RESPONSE_DATA_PROP
            = "jmeter.save.saveservice.response_data";

    /** The name of the property indicating whether the response message
     should be saved.  **/
    public static final String SAVE_RESPONSE_MESSAGE_PROP
            = "jmeter.save.saveservice.response_message";

    /** The name of the property indicating whether the success indicator
     should be saved.  **/
    public static final String SAVE_SUCCESSFUL_PROP
            = "jmeter.save.saveservice.successful";

    /** The name of the property indicating whether the thread name
     should be saved.  **/
    public static final String SAVE_THREAD_NAME_PROP
            = "jmeter.save.saveservice.thread_name";

    /** The name of the property indicating whether the time
     should be saved.  **/
    public static final String SAVE_TIME_PROP
            = "jmeter.save.saveservice.time";

    /** The name of the property indicating whether the time stamp
     should be saved.  **/
    public static final String TIME_STAMP_FORMAT_PROP
            = "jmeter.save.saveservice.timestamp_format";

    // ---------------------------------------------------------------------
    // XML RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    public final static String PRESERVE = "preserve";
    public final static String XML_SPACE = "xml:space";
    public static final String ASSERTION_RESULT_TAG_NAME = "assertionResult";
    public static final String BINARY = "binary";
    public static final String DATA_TYPE = "dataType";
    public static final String ERROR = "error";
    public static final String FAILURE = "failure";
    public static final String FAILURE_MESSAGE = "failureMessage";
    public static final String LABEL = "label";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String RESPONSE_MESSAGE = "responseMessage";
    public static final String SAMPLE_RESULT_TAG_NAME = "sampleResult";
    public static final String SUCCESSFUL = "success";
    public static final String THREAD_NAME = "threadName";
    public static final String TIME = "time";
    public static final String TIME_STAMP = "timeStamp";

}
