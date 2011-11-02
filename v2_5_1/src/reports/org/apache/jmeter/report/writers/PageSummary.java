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
 * PageSummary defines the summary of a report page and the runtime
 * information for debugging and logging purposes. It's a good idea
 * to return summary so that automated process can start the report
 * and a summary of how the reports ran.
 */
public interface PageSummary extends Cloneable {
    long getElapsedTime();
    long getEndTimeStamp();
    String getFileName();
    String getPageTitle();
    long getStartTimeStamp();
    boolean isSuccessful();
    void pageStarted();
    void pageEnded();
    void setFileName(String file);
    void setPageTitle(String title);
    void setSuccessful(boolean success);
}
