//$Header$
/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.jmeter.testelement;

import javax.swing.JComponent;

import org.apache.jmeter.report.ReportTable;

public class LineGraph extends AbstractChart {

    public static final String REPORT_CHART_URLS = "ReportChart.chart.urls";

	public LineGraph() {
		super();
	}

    public String getURLs() {
        return getPropertyAsString(REPORT_CHART_URLS);
    }
    
    public void setURLs(String urls) {
        setProperty(REPORT_CHART_URLS,urls);
    }
    
	public JComponent renderChart(ReportTable element) {
		return null;
	}

}
