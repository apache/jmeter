//$Header:
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
package org.apache.jmeter.report.gui;

import javax.swing.JCheckBox;

import org.apache.jmeter.testelement.Table;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class TableGui extends AbstractReportGui {

    private JCheckBox meanCheck = new JCheckBox(JMeterUtils.getResString("average"));
    private JCheckBox medianCheck = new JCheckBox(JMeterUtils.getResString("graph_results_median"));
    private JCheckBox maxCheck = new JCheckBox(JMeterUtils.getResString("aggregate_report_max"));
    private JCheckBox minCheck = new JCheckBox(JMeterUtils.getResString("aggregate_report_min"));
    private JCheckBox responseRateCheck = 
    	new JCheckBox(JMeterUtils.getResString("aggregate_report_rate"));
    private JCheckBox transferRateCheck = 
    	new JCheckBox(JMeterUtils.getResString("aggregate_report_bandwidth"));
    private JCheckBox fiftypercentCheck = 
    	new JCheckBox(JMeterUtils.getResString("monitor_label_left_middle"));
    private JCheckBox nintypercentCheck = 
    	new JCheckBox(JMeterUtils.getResString("aggregate_report_90%_line"));
    private JCheckBox errorRateCheck = new JCheckBox(JMeterUtils.getResString("aggregate_report_error"));

    public TableGui() {
		super();
	}

	public TestElement createTestElement() {
		return new Table() ;
	}

	public void modifyTestElement(TestElement element) {
		this.configureTestElement(element);
		Table tb = (Table)element;
		tb.set50Percent(String.valueOf(fiftypercentCheck.isSelected()));
		tb.set90Percent(String.valueOf(nintypercentCheck.isSelected()));
		tb.setErrorRate(String.valueOf(errorRateCheck.isSelected()));
		tb.setMax(String.valueOf(maxCheck.isSelected()));
		tb.setMean(String.valueOf(meanCheck.isSelected()));
		tb.setMedian(String.valueOf(medianCheck.isSelected()));
		tb.setMin(String.valueOf(minCheck.isSelected()));
	}

}
