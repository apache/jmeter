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
