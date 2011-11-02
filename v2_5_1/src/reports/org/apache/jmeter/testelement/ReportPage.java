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

import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;

/**
 * ReportPage
 *
 */
public class ReportPage extends AbstractTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

//    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String REPORT_PAGE_TITLE = "ReportPage.title";
    public static final String REPORT_PAGE_INDEX = "ReportPage.index";
    public static final String REPORT_PAGE_CSS = "ReportPage.css";
    public static final String REPORT_PAGE_HEADER = "ReportPage.header";
    public static final String REPORT_PAGE_FOOTER = "ReportPage.footer";
    public static final String REPORT_PAGE_INTRO = "ReportPage.intro";

    /**
     * No-arg constructor.
     */
    public ReportPage() {
    }

    public static ReportPage createReportPage(String name) {
        ReportPage page = new ReportPage();
        return page;
    }

    public String getTitle() {
        return getPropertyAsString(REPORT_PAGE_TITLE);
    }

    public void setTitle(String title) {
        setProperty(REPORT_PAGE_TITLE,title);
    }

    public boolean getIndex() {
        return getPropertyAsBoolean(REPORT_PAGE_INDEX);
    }

    public void setIndex(String makeIndex) {
        setProperty(REPORT_PAGE_INDEX,makeIndex);
    }

    public String getCSS() {
        return getPropertyAsString(REPORT_PAGE_CSS);
    }

    public void setCSS(String css) {
        setProperty(REPORT_PAGE_CSS,css);
    }

    public String getHeaderURL() {
        return getPropertyAsString(REPORT_PAGE_HEADER);
    }

    public void setHeaderURL(String url) {
        setProperty(REPORT_PAGE_HEADER,url);
    }

    public String getFooterURL() {
        return getPropertyAsString(REPORT_PAGE_FOOTER);
    }

    public void setFooterURL(String url) {
        setProperty(REPORT_PAGE_FOOTER,url);
    }

    public String getIntroduction() {
        return getPropertyAsString(REPORT_PAGE_INTRO);
    }

    public void setIntroduction(String intro) {
        setProperty(REPORT_PAGE_INTRO,intro);
    }

}
