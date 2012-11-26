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
 * This is a basic implementation of PageSummary interface.
 */
public class DefaultPageSummary implements PageSummary {

    private long START = 0;
    private long END = 0;
    private String title;
    private String fileName;
    private boolean success;

    /**
     *
     */
    public DefaultPageSummary() {
        super();
    }

    /**
     * Returns the elapsed time in milliseconds
     */
    @Override
    public long getElapsedTime() {
        return END - START;
    }

    @Override
    public long getEndTimeStamp() {
        return END;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#getPageTitle()
     */
    @Override
    public String getPageTitle() {
        return title;
    }

    @Override
    public long getStartTimeStamp() {
        return START;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#isSuccessful()
     */
    @Override
    public boolean isSuccessful() {
        return success;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#pageStarted()
     */
    @Override
    public void pageStarted() {
        START = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#pageEnded()
     */
    @Override
    public void pageEnded() {
        END = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#setFileName(java.lang.String)
     */
    @Override
    public void setFileName(String file) {
        this.fileName = file;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#setPageTitle(java.lang.String)
     */
    @Override
    public void setPageTitle(String title) {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.writers.PageSummary#setSuccessful(boolean)
     */
    @Override
    public void setSuccessful(boolean success) {
        this.success = success;
    }

}
