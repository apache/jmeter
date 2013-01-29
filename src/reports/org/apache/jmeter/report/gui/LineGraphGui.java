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
package org.apache.jmeter.report.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.AbstractTable;
import org.apache.jmeter.testelement.AbstractChart;
import org.apache.jmeter.testelement.LineChart;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

public class LineGraphGui extends AbstractReportGui {

    private static final long serialVersionUID = 240L;

    private JLabeledChoice xAxisLabel = new JLabeledChoice();

    private JLabeledTextField yAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("report_chart_y_axis_label")); // $NON-NLS-1$

    private JLabeledTextField caption =
        new JLabeledTextField(JMeterUtils.getResString("report_chart_caption"), // $NON-NLS-1$
                Color.white);

    private JLabeledTextField urls =
        new JLabeledTextField(JMeterUtils.getResString("report_line_graph_urls"), // $NON-NLS-1$
                Color.white);

    private JLabeledChoice yItems = new JLabeledChoice();
    private JLabeledChoice xItems = new JLabeledChoice();

    public LineGraphGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "report_line_graph";
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(10,10));
        pane.setBackground(Color.white);
        pane.add(this.getNamePanel(),BorderLayout.NORTH);

        VerticalPanel options = new VerticalPanel(Color.white);
        yAxisLabel.setBackground(Color.white);

        JLabel xLabel = new JLabel(JMeterUtils.getResString("report_chart_x_axis")); // $NON-NLS-1$
        HorizontalPanel xpanel = new HorizontalPanel(Color.white);
        xLabel.setBorder(new EmptyBorder(5,2,5,2));
        xItems.setBackground(Color.white);
        xItems.setValues(AbstractTable.xitems);
        xpanel.add(xLabel);
        xpanel.add(xItems);
        options.add(xpanel);

        JLabel xALabel = new JLabel(JMeterUtils.getResString("report_chart_x_axis_label")); // $NON-NLS-1$
        HorizontalPanel xApanel = new HorizontalPanel(Color.white);
        xALabel.setBorder(new EmptyBorder(5,2,5,2));
        xAxisLabel.setBackground(Color.white);
        xAxisLabel.setValues(AbstractChart.X_LABELS);
        xApanel.add(xALabel);
        xApanel.add(xAxisLabel);
        options.add(xApanel);

        JLabel yLabel = new JLabel(JMeterUtils.getResString("report_chart_y_axis")); // $NON-NLS-1$
        HorizontalPanel ypanel = new HorizontalPanel(Color.white);
        yLabel.setBorder(new EmptyBorder(5,2,5,2));
        yItems.setBackground(Color.white);
        yItems.setValues(AbstractTable.items);
        ypanel.add(yLabel);
        ypanel.add(yItems);
        options.add(ypanel);
        options.add(yAxisLabel);
        options.add(caption);
        options.add(urls);

        add(pane,BorderLayout.NORTH);
        add(options,BorderLayout.CENTER);
    }

    @Override
    public TestElement createTestElement() {
        LineChart element = new LineChart();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        this.configureTestElement(element);
        LineChart bc = (LineChart)element;
        bc.setXAxis(xItems.getText());
        bc.setYAxis(yItems.getText());
        bc.setXLabel(xAxisLabel.getText());
        bc.setYLabel(yAxisLabel.getText());
        bc.setCaption(caption.getText());
        bc.setURLs(urls.getText());
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        LineChart bc = (LineChart)element;
        xItems.setText(bc.getXAxis());
        yItems.setText(bc.getYAxis());
        xAxisLabel.setText(bc.getXLabel());
        yAxisLabel.setText(bc.getYLabel());
        caption.setText(bc.getCaption());
        urls.setText(bc.getURLs());
    }
}
