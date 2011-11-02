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
package org.apache.jmeter.report.writers;

/**
 *
 * The purpose of ReportSummary is to provide a detailed description of the
 * reports generated, how long it took and where the generated files are
 * located.
 */
public interface ReportSummary extends Cloneable {
    /**
     * Add a page summary to the report summary
     * @param summary
     */
    void addPageSummary(PageSummary summary);
    /**
     * This should be the elapsed time to run all the reports. Classes
     * implementing it should simply add up the elapsed time for each
     * report page.
     * @return elapsed time
     */
    long getElapsedTime();
    /**
     * The method should return a list of the pages generated for the
     * report and whether it succeeded or not
     * @return page summary array
     */
    PageSummary[] getPagesSummaries();
    /**
     * Remove a page summary from the report summary.
     * @param summary
     */
    void removePageSummary(PageSummary summary);
}
