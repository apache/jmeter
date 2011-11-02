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

package org.apache.jmeter.protocol.http.util.accesslog;

import org.apache.jmeter.testelement.TestElement;

/**
 * Description:<br>
 * <br>
 * Filter interface is designed to make it easier to use Access Logs for JMeter
 * test plans. Normally, a person would have to clean a log file manually and
 * create the JMeter requests. The access log parse utility uses the filter to
 * include/exclude files by either file name or regular expression pattern.
 * <p>
 * It will also be used by HttpSamplers that use access logs. Using access logs
 * is intended as a way to simulate production traffic. For functional testing,
 * it is better to use the standard functional testing tools in JMeter. Using
 * access logs can also reduce the amount of memory needed to run large test
 * plans. <br>
 *
 * @version $Revision$
 */

public interface Filter {

    /**
     * @param oldextension
     * @param newextension
     */
    public void setReplaceExtension(String oldextension, String newextension);

    /**
     * Include all files in the array.
     *
     * @param filenames
     */
    public void includeFiles(String[] filenames);

    /**
     * Exclude all files in the array
     *
     * @param filenames
     */
    public void excludeFiles(String[] filenames);

    /**
     * Include any log entry that contains the following regular expression
     * pattern.
     *
     * @param regexp
     */
    public void includePattern(String[] regexp);

    /**
     * Exclude any log entry that contains the following regular expression
     * pattern.
     *
     * @param regexp
     */
    public void excludePattern(String[] regexp);

    /**
     * Log parser will call this method to see if a particular entry should be
     * filtered or not.
     *
     * @param path
     * @return boolean
     */
    public boolean isFiltered(String path,TestElement sampler);

    /**
     * In case the user wants to replace the file extension, log parsers should
     * call this method. This is useful for regression test plans. If a website
     * is migrating from one platform to another and the file extension changes,
     * the filter provides an easy way to do it without spending a lot of time.
     *
     * @param text
     * @return String
     */
    public String filter(String text);

    /**
     * Tell the filter when the parsing has reached the end of the log file and
     * is about to begin again. Gives the filter a chance to adjust it's values,
     * if needed.
     *
     */
    public void reset();

}
