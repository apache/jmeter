/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;

/**
 * Simpler (lower memory) version of Aggregate Report (StatVisualizer).
 * Excludes the Median and 90% columns, which are expensive in memory terms
 */
public class SummaryReport extends AbstractVisualizer implements Clearable {
	private final String[] COLUMNS = { 
            JMeterUtils.getResString("sampler_label"),               //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_count"),      //$NON-NLS-1$
            JMeterUtils.getResString("average"),                     //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_min"),        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_max"),        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_stddev"),     //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_error%"),     //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_rate"),       //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_bandwidth"),  //$NON-NLS-1$
            JMeterUtils.getResString("average_bytes"),               //$NON-NLS-1$
            };

	private final String TOTAL_ROW_LABEL 
        = JMeterUtils.getResString("aggregate_report_total_label");  //$NON-NLS-1$

	protected JTable myJTable;

	protected JScrollPane myScrollPane;

	transient private ObjectTableModel model;

	Map tableRows = Collections.synchronizedMap(new HashMap());

	// Column renderers
	private static final TableCellRenderer[] RENDERERS = 
		new TableCellRenderer[]{
		    null, // Label
		    null, // count
		    null, // Mean
		    null, // Min
		    null, // Max
		    new NumberRenderer("#0.00"), // Std Dev.
		    new NumberRenderer("#0.00%"), // Error %age
		    new RateRenderer("#.0"),      // Throughpur
		    new NumberRenderer("#0.00"),  // kB/sec
		    new NumberRenderer("#.0"),    // avg. pageSize
		};

    public SummaryReport() {
		super();
		model = new ObjectTableModel(COLUMNS,
				Calculator.class,// All rows have this class
                new Functor[] { 
                    new Functor("getLabel"),              //$NON-NLS-1$
                    new Functor("getCount"),              //$NON-NLS-1$
    				new Functor("getMeanAsNumber"),       //$NON-NLS-1$
                    new Functor("getMin"),                //$NON-NLS-1$
                    new Functor("getMax"),                //$NON-NLS-1$
                    new Functor("getStandardDeviation"),                //$NON-NLS-1$
                    new Functor("getErrorPercentage"),    //$NON-NLS-1$
                    new Functor("getRate"),               //$NON-NLS-1$
    				new Functor("getKBPerSecond"),        //$NON-NLS-1$
                    new Functor("getPageSize"),           //$NON-NLS-1$
                },
                new Functor[] { null, null, null, null, null, null, null, null , null, null }, 
                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class, 
                              String.class, String.class, String.class, String.class, String.class });
		clearData();
		init();
	}
    
	public static boolean testFunctors(){
		SummaryReport instance = new SummaryReport();
		return instance.model.checkFunctors(null,instance.getClass());
	}
	
	public String getLabelResource() {
		return "summary_report";  //$NON-NLS-1$
	}

	public void add(SampleResult res) {
		Calculator row = null;
		synchronized (tableRows) {
			row = (Calculator) tableRows.get(res.getSampleLabel());
			if (row == null) {
				row = new Calculator(res.getSampleLabel());
				tableRows.put(row.getLabel(), row);
				model.insertRow(row, model.getRowCount() - 1);
			}
		}
		row.addSample(res);
		Calculator tot = ((Calculator) tableRows.get(TOTAL_ROW_LABEL));
        tot.addSample(res);
		model.fireTableDataChanged();
	}

	/**
	 * Clears this visualizer and its model, and forces a repaint of the table.
	 */
	public void clearData() {
		model.clearData();
		tableRows.clear();
		tableRows.put(TOTAL_ROW_LABEL, new Calculator(TOTAL_ROW_LABEL));
		model.addRow(tableRows.get(TOTAL_ROW_LABEL));
	}

	/**
	 * Main visualizer setup.
	 */
	private void init() {
		this.setLayout(new BorderLayout());

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);

		mainPanel.setBorder(margin);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		mainPanel.add(makeTitlePanel());

		myJTable = new JTable(model);
		myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		RendererUtils.applyRenderers(myJTable, RENDERERS);
		myScrollPane = new JScrollPane(myJTable);
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(myScrollPane, BorderLayout.CENTER);
	}
}
