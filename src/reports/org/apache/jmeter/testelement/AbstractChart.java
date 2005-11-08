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

import org.apache.jmeter.report.ReportChart;
import org.apache.jmeter.report.ReportTable;

/**
 * The general idea of the chart graphs information for a table.
 * A chart can only be generated from a specific table, though more
 * than one chart can be generated from a single table.
 * @author Peter Lin
 *
 */
public abstract class AbstractChart extends AbstractTestElement implements ReportChart {

    public static final String REPORT_CHART_X_AXIS = "ReportChart.chart.x.axis";
    public static final String REPORT_CHART_Y_AXIS = "ReportChart.chart.y.axis";
    public static final String REPORT_CHART_X_LABEL = "ReportChart.chart.x.label";
    public static final String REPORT_CHART_Y_LABEL = "ReportChart.chart.y.label";
    public static final String REPORT_CHART_TITLE = "ReportChart.chart.title";
    public static final String REPORT_CHART_CAPTION = "ReportChart.chart.caption";

    public AbstractChart() {
		super();
	}
    
    public String getXAxis() {
    	return getPropertyAsString(REPORT_CHART_X_AXIS);
    }
    
    public void setXAxis(String field) {
    	setProperty(REPORT_CHART_X_AXIS,field);
    }
    
    public String getYAxis() {
    	return getPropertyAsString(REPORT_CHART_Y_AXIS);
    }
    
    public void setYAxis(String scale) {
    	setProperty(REPORT_CHART_Y_AXIS,scale);
    }

    public String getXLabel() {
    	return getPropertyAsString(REPORT_CHART_X_LABEL);
    }
    
    public void setXLabel(String label) {
    	setProperty(REPORT_CHART_X_LABEL,label);
    }
    
    public String getYLabel() {
    	return getPropertyAsString(REPORT_CHART_Y_LABEL);
    }
    
    public void setYLabel(String label) {
    	setProperty(REPORT_CHART_Y_LABEL,label);
    }
    
    /**
     * The title is a the name for the chart. A page link will
     * be generated using the title. The title will also be
     * used for a page index.
     * @return
     */
    public String getTitle() {
    	return getPropertyAsString(REPORT_CHART_TITLE);
    }
    
    /**
     * The title is a the name for the chart. A page link will
     * be generated using the title. The title will also be
     * used for a page index.
     * @param title
     */
    public void setTitle(String title) {
    	setProperty(REPORT_CHART_TITLE,title);
    }

    /**
     * The caption is a description for the chart explaining
     * what the chart means.
     * @return
     */
    public String getCaption() {
        return getPropertyAsString(REPORT_CHART_CAPTION);
    }
    
    /**
     * The caption is a description for the chart explaining
     * what the chart means.
     * @param caption
     */
    public void setCaption(String caption) {
        setProperty(REPORT_CHART_CAPTION,caption);
    }
    
    /**
     * Subclasses will need to implement the method by doing the following:
     * 1. get the x and y axis
     * 2. filter the table data
     * 3. pass the data to the chart library
     * 4. return the generated chart
     */
	public abstract JComponent renderChart(ReportTable element);
}
