// $Header:
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * The general idea of the chart graphs information for a table.
 * A chart can only be generated from a specific table, though more
 * than one chart can be generated from a single table.
 * @author Peter Lin
 *
 */
public abstract class AbstractChart extends AbstractTestElement implements Chart {

    public static final String REPORT_CHART_X_AXIS = "ReportTable.chart.x.axis";
    public static final String REPORT_CHART_Y_AXIS = "ReportTable.chart.y.axis";
    public static final String REPORT_CHART_X_LABEL = "ReportTable.chart.x.label";
    public static final String REPORT_CHART_Y_LABEL = "ReportTable.chart.y.label";
    public static final String REPORT_CHART_TITLE = "ReportTable.chart.title";

    protected AbstractTable parent = null;
    
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
    
    public String getTitle() {
    	return getPropertyAsString(REPORT_CHART_TITLE);
    }
    
    public void setTitle(String title) {
    	setProperty(REPORT_CHART_TITLE,title);
    }
    
    public void setParentTable(AbstractTable table) {
    	this.parent = table;
    }

    /**
     * Method returns the items that are checked
     * @return
     */
    public List getCheckedItems() {
    	ArrayList checked = new ArrayList();
    	for (int idx=0; idx < AbstractTable.items.length; idx++) {
    		if (this.parent.getPropertyAsString(
    				AbstractTable.items[idx]).equals(String.valueOf(true))) {
    			checked.add(AbstractTable.items[idx]);
    		}
    	}
    	return checked;
    }
    
	public abstract JComponent renderChart(TestElement element);
}
