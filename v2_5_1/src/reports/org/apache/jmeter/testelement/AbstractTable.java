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
package org.apache.jmeter.testelement;

import java.util.List;

import org.apache.jmeter.report.ReportTable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * AbstractTable is the base Element for different kinds of report tables.
 *
 */
public abstract class AbstractTable extends AbstractTestElement
    implements ReportTable
{

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String REPORT_TABLE_MEAN = "ReportTable.Mean";
    public static final String REPORT_TABLE_MEDIAN = "ReportTable.Median";
    public static final String REPORT_TABLE_MAX = "ReportTable.Max";
    public static final String REPORT_TABLE_MIN = "ReportTable.Min";
    public static final String REPORT_TABLE_RESPONSE_RATE = "ReportTable.Response_rate";
    public static final String REPORT_TABLE_TRANSFER_RATE = "ReportTable.Transfer_rate";
    public static final String REPORT_TABLE_50_PERCENT = "ReportTable.50_percent";
    public static final String REPORT_TABLE_90_PERCENT = "ReportTable.90_percent";
    public static final String REPORT_TABLE_ERROR_RATE = "ReportTable.Error.rate";
    public static final String[] items = {
        REPORT_TABLE_MEAN, REPORT_TABLE_MEDIAN, REPORT_TABLE_MAX, REPORT_TABLE_MIN,
        REPORT_TABLE_RESPONSE_RATE, REPORT_TABLE_TRANSFER_RATE, REPORT_TABLE_50_PERCENT,
        REPORT_TABLE_90_PERCENT, REPORT_TABLE_ERROR_RATE };

    public static final String REPORT_TABLE_TOTAL = "ReportTable.total";
    public static final String REPORT_TABLE_URL = "ReportTable.url";

    public static final String[] xitems = { REPORT_TABLE_TOTAL,
        REPORT_TABLE_URL };

    public AbstractTable() {
        super();
    }

    public boolean getMean() {
        return getPropertyAsBoolean(REPORT_TABLE_MEAN);
    }

    public void setMean(String set) {
        setProperty(REPORT_TABLE_MEAN,set);
    }

    public boolean getMedian() {
        return getPropertyAsBoolean(REPORT_TABLE_MEDIAN);
    }

    public void setMedian(String set) {
        setProperty(REPORT_TABLE_MEDIAN,set);
    }

    public boolean getMax() {
        return getPropertyAsBoolean(REPORT_TABLE_MAX);
    }

    public void setMax(String set) {
        setProperty(REPORT_TABLE_MAX,set);
    }

    public boolean getMin() {
        return getPropertyAsBoolean(REPORT_TABLE_MIN);
    }

    public void setMin(String set) {
        setProperty(REPORT_TABLE_MIN,set);
    }

    public boolean getResponseRate() {
        return getPropertyAsBoolean(REPORT_TABLE_RESPONSE_RATE);
    }

    public void setResponseRate(String set) {
        setProperty(REPORT_TABLE_RESPONSE_RATE,set);
    }

    public boolean getTransferRate() {
        return getPropertyAsBoolean(REPORT_TABLE_TRANSFER_RATE);
    }

    public void setTransferRate(String set) {
        setProperty(REPORT_TABLE_TRANSFER_RATE,set);
    }

    public boolean get50Percent() {
        return getPropertyAsBoolean(REPORT_TABLE_50_PERCENT);
    }

    public void set50Percent(String set) {
        setProperty(REPORT_TABLE_50_PERCENT,set);
    }

    public boolean get90Percent() {
        return getPropertyAsBoolean(REPORT_TABLE_90_PERCENT);
    }

    public void set90Percent(String set) {
        setProperty(REPORT_TABLE_90_PERCENT,set);
    }

    public boolean getErrorRate() {
        return getPropertyAsBoolean(REPORT_TABLE_ERROR_RATE);
    }

    public void setErrorRate(String set) {
        setProperty(REPORT_TABLE_ERROR_RATE,set);
    }

    @Override
    public void addTestElement(TestElement el) {
        if (el != null) {
            super.addTestElement(el);
            log.info("TestElement: " + el.getClass().getName());
        }
    }

    /**
     * method isn't implemented and is left abstract. Subclasses
     * need to filter the data in the list and return statistics.
     * The statistics should be like the aggregate listener.
     */
    @SuppressWarnings("rawtypes") // TODO fix this when there is a real implementation
    public abstract String[][] getTableData(List data);

}
