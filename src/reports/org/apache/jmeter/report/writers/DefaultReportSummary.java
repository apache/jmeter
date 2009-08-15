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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The default implementation of ReportSummary just contains the stats
 * and basic information. It doesn't contain the actual report. In the
 * future we may want to implement a version with all the details to
 * display in a Swing GUI.
 */
public class DefaultReportSummary implements ReportSummary {

    private final ArrayList<PageSummary> pages = new ArrayList<PageSummary>();

    /**
     *
     */
    public DefaultReportSummary() {
        super();
    }

    /**
     * Add a PageSummary to the report
     */
    public void addPageSummary(PageSummary summary) {
        this.pages.add(summary);
    }

    /**
     * current implementation simply iterates over the Page summaries
     * and adds the times.
     */
    public long getElapsedTime() {
        long elpasedTime = 0;
        Iterator<PageSummary> itr = this.pages.iterator();
        while (itr.hasNext()) {
            elpasedTime += itr.next().getElapsedTime();
        }
        return elpasedTime;
    }

    /**
     * The current implementation calls ArrayList.toArray(Object[])
     */
    public PageSummary[] getPagesSummaries() {
        PageSummary[] ps = new PageSummary[this.pages.size()];
        return this.pages.toArray(ps);
    }

    /**
     * remove a PageSummary
     */
    public void removePageSummary(PageSummary summary) {
        this.pages.remove(summary);
    }

}
