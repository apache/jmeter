/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.save;

/************************************************************
 *  This interface defines a number of constants used in the properties file
 *  that is used to indicate which portions of the results will be
 *  stored in the results files.  It also contains constants representing XML
 *  tags, elements, etc.
 *  
 *
 *@author     <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell</a>
 *@created    $Date$
 *@version    $Revision$ $Date$
 ***********************************************************/

public interface SaveServiceConstants
{
        //---------------------------------------------------------------------
        // PROPERTY FILE CONSTANTS
        //---------------------------------------------------------------------

        /** The file containing the properties governing the information to
            be saved.  **/
        public static final String PROPS_FILE =
        "jmeter.properties";

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
        public static final String ASSERTION_RESULTS_PROP
        = "jmeter.save.saveservice.assertion_results";

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

        //---------------------------------------------------------------------
        // XML RESULT FILE CONSTANTS
        //---------------------------------------------------------------------

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
