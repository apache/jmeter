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
package org.apache.jmeter.report.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.LineGraph;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

public class LineGraphGui extends AbstractReportGui {

    private JLabeledTextField xAxis = 
        new JLabeledTextField(JMeterUtils.getResString("report_chart_x_axis"));
    
    private JLabeledTextField yAxis = 
        new JLabeledTextField(JMeterUtils.getResString("report_chart_y_axis"));
    
    private JLabeledTextField xAxisLabel = 
        new JLabeledTextField(JMeterUtils.getResString("report_chart_x_axis_label"));
    
    private JLabeledTextField yAxisLabel = 
        new JLabeledTextField(JMeterUtils.getResString("report_chart_y_axis_label"));

    public LineGraphGui() {
		super();
		init();
	}
	
	public String getLabelResource() {
		return "report_line_graph";
	}
	
	public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
	}

	protected void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(10,10));
        pane.setBackground(Color.white);
        pane.add(this.getNamePanel(),BorderLayout.NORTH);
        
        VerticalPanel options = new VerticalPanel(Color.white);
        xAxis.setBackground(Color.white);
        yAxis.setBackground(Color.white);
        xAxisLabel.setBackground(Color.white);
        yAxisLabel.setBackground(Color.white);
        options.add(xAxis);
        options.add(yAxis);
        options.add(xAxisLabel);
        options.add(yAxisLabel);
        
        add(pane,BorderLayout.NORTH);
        add(options,BorderLayout.CENTER);
	}
	
	public TestElement createTestElement() {
		LineGraph element = new LineGraph();
		modifyTestElement(element);
		return element;
	}

	public void modifyTestElement(TestElement element) {
		this.configureTestElement(element);
		LineGraph bc = (LineGraph)element;
		bc.setXAxis(xAxis.getText());
		bc.setYAxis(yAxis.getText());
		bc.setXLabel(xAxisLabel.getText());
		bc.setYLabel(yAxisLabel.getText());
	}
	
    public void configure(TestElement element) {
        super.configure(element);
        LineGraph bc = (LineGraph)element;
        xAxis.setText(bc.getXAxis());
        yAxis.setText(bc.getYAxis());
        xAxisLabel.setText(bc.getXLabel());
        yAxisLabel.setText(bc.getYLabel());
    }
}
