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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.util.ReportMenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.Table;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class TableGui extends AbstractReportGui implements ChangeListener {

    private static final long serialVersionUID = 240L;

    private JCheckBox meanCheck = new JCheckBox(JMeterUtils.getResString("average")); // $NON-NLS-1$
    private JCheckBox medianCheck = new JCheckBox(JMeterUtils.getResString("graph_results_median")); // $NON-NLS-1$
    private JCheckBox maxCheck = new JCheckBox(JMeterUtils.getResString("aggregate_report_max")); // $NON-NLS-1$
    private JCheckBox minCheck = new JCheckBox(JMeterUtils.getResString("aggregate_report_min")); // $NON-NLS-1$
    private JCheckBox responseRateCheck =
        new JCheckBox(JMeterUtils.getResString("aggregate_report_rate")); // $NON-NLS-1$
    private JCheckBox transferRateCheck =
        new JCheckBox(JMeterUtils.getResString("aggregate_report_bandwidth")); // $NON-NLS-1$
    private JCheckBox fiftypercentCheck =
        new JCheckBox(JMeterUtils.getResString("monitor_label_left_middle")); // $NON-NLS-1$
    private JCheckBox nintypercentCheck =
        new JCheckBox(JMeterUtils.getResString("aggregate_report_90")); // $NON-NLS-1$
    private JCheckBox errorRateCheck =
        new JCheckBox(JMeterUtils.getResString("aggregate_report_error")); // $NON-NLS-1$

    public TableGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "report_table";
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());
        setBackground(Color.white);

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(10,10));
        pane.setBackground(Color.white);
        pane.add(this.getNamePanel(),BorderLayout.NORTH);

        meanCheck.addChangeListener(this);
        VerticalPanel options = new VerticalPanel(Color.white);
        meanCheck.setBackground(Color.white);
        medianCheck.setBackground(Color.white);
        maxCheck.setBackground(Color.white);
        minCheck.setBackground(Color.white);
        responseRateCheck.setBackground(Color.white);
        transferRateCheck.setBackground(Color.white);
        fiftypercentCheck.setBackground(Color.white);
        nintypercentCheck.setBackground(Color.white);
        errorRateCheck.setBackground(Color.white);
        options.add(meanCheck);
        options.add(medianCheck);
        options.add(maxCheck);
        options.add(minCheck);
        options.add(responseRateCheck);
        options.add(transferRateCheck);
        options.add(fiftypercentCheck);
        options.add(nintypercentCheck);
        options.add(errorRateCheck);

        add(pane,BorderLayout.NORTH);
        add(options,BorderLayout.CENTER);
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        ReportMenuFactory.addFileMenu(pop);
        ReportMenuFactory.addEditMenu(pop,true);
        return pop;
    }

    @Override
    public TestElement createTestElement() {
        Table element = new Table();
        modifyTestElement(element);
        return element;
    }

    @Override
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
        tb.setResponseRate(String.valueOf(responseRateCheck.isSelected()));
        tb.setTransferRate(String.valueOf(transferRateCheck.isSelected()));
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        Table tb = (Table)element;
        meanCheck.setSelected(tb.getMean());
        medianCheck.setSelected(tb.getMedian());
        maxCheck.setSelected(tb.getMax());
        minCheck.setSelected(tb.getMin());
        fiftypercentCheck.setSelected(tb.get50Percent());
        nintypercentCheck.setSelected(tb.get90Percent());
        errorRateCheck.setSelected(tb.getErrorRate());
        responseRateCheck.setSelected(tb.getResponseRate());
        transferRateCheck.setSelected(tb.getTransferRate());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        modifyTestElement(ReportGuiPackage.getInstance().getCurrentElement());
    }
}
