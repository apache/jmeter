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
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.Arrays;
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
//import javax.swing.table.AbstractTableModel;
//import javax.swing.table.TableModel;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 * 
 * @version $Revision$ on $Date$
 */
public class StatVisualizer extends AbstractVisualizer implements Clearable {
	private final String[] COLUMNS = { 
            JMeterUtils.getResString("sampler_label"),  //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_count"),  //$NON-NLS-1$
            JMeterUtils.getResString("average"),  //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_median"),  //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_90%_line"),  //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_min"),  //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_max"),  //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_error%"),  //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_rate"),  //$NON-NLS-1$
			JMeterUtils.getResString("aggregate_report_bandwidth") };  //$NON-NLS-1$

	private final String TOTAL_ROW_LABEL 
        = JMeterUtils.getResString("aggregate_report_total_label");  //$NON-NLS-1$

	protected JTable myJTable;

	protected JScrollPane myScrollPane;

	transient private ObjectTableModel model;

	Map tableRows = Collections.synchronizedMap(new HashMap());

	public StatVisualizer() {
		super();
		model = new ObjectTableModel(COLUMNS, 
				SamplingStatCalculator.class,
                new Functor[] { 
                    new Functor("getLabel"),   //$NON-NLS-1$
                    new Functor("getCount"),  //$NON-NLS-1$
    				new Functor("getMeanAsNumber"),   //$NON-NLS-1$
                    new Functor("getMedian"),  //$NON-NLS-1$
    				new Functor("getPercentPoint",  //$NON-NLS-1$
                            new Object[] { new Float(.900) }), 
                    new Functor("getMin"),  //$NON-NLS-1$
                    new Functor("getMax"),   //$NON-NLS-1$
                    new Functor("getErrorPercentage"),   //$NON-NLS-1$
                    new Functor("getRate"),  //$NON-NLS-1$
    				new Functor("getPageSize")   //$NON-NLS-1$
                },
                new Functor[] { null, null, null, null, null, null, null, null, null, null }, 
                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class, 
                              Long.class, Long.class, String.class, String.class, String.class });
		clear();
		init();
	}

	// Column renderers
	private static final TableCellRenderer[] RENDERERS = 
		new TableCellRenderer[]{
		    null, // Label
		    null, // count
		    null, // Mean
		    null, // median
		    null, // 90%
		    null, // Min
		    null, // Max
		    new NumberRenderer("#0.00%"), // Error %age
		    new RateRenderer("#.0"),      // Throughpur
		    new NumberRenderer("#.0"),    // pageSize
		};

	public static boolean testFunctors(){
		StatVisualizer instance = new StatVisualizer();
		return instance.model.checkFunctors(null,instance.getClass());
	}
	
	public String getLabelResource() {
		return "aggregate_report";  //$NON-NLS-1$
	}

	public void add(SampleResult res) {
		SamplingStatCalculator row = null;
		synchronized (tableRows) {
			row = (SamplingStatCalculator) tableRows.get(res.getSampleLabel());
			if (row == null) {
				row = new SamplingStatCalculator(res.getSampleLabel());
				tableRows.put(row.getLabel(), row);
				model.insertRow(row, model.getRowCount() - 1);
			}
		}
		row.addSample(res);
		((SamplingStatCalculator) tableRows.get(TOTAL_ROW_LABEL)).addSample(res);
		model.fireTableDataChanged();
	}

	/**
	 * Clears this visualizer and its model, and forces a repaint of the table.
	 */
	public void clear() {
		model.clearData();
		tableRows.clear();
		tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
		model.addRow(tableRows.get(TOTAL_ROW_LABEL));
	}

	// overrides AbstractVisualizer
	// forces GUI update after sample file has been read
	public TestElement createTestElement() {
		TestElement t = super.createTestElement();

		// sleepTill = 0;
		return t;
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

		// SortFilterModel mySortedModel =
		// new SortFilterModel(myStatTableModel);
		myJTable = new JTable(model);
		myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		RendererUtils.applyRenderers(myJTable, RENDERERS);
		myScrollPane = new JScrollPane(myJTable);
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(myScrollPane, BorderLayout.CENTER);
	}
}

/**
 * Pulled this mainly out of a Core Java book to implement a sorted table -
 * haven't implemented this yet, it needs some non-trivial work done to it to
 * support our dynamically-sizing TableModel for this visualizer.
 * 
 */

//class SortFilterModel extends AbstractTableModel {
//	private TableModel model;
//
//	private int sortColumn;
//
//	private Row[] rows;
//
//	public SortFilterModel(TableModel m) {
//		model = m;
//		rows = new Row[model.getRowCount()];
//		for (int i = 0; i < rows.length; i++) {
//			rows[i] = new Row();
//			rows[i].index = i;
//		}
//	}
//
//	public SortFilterModel() {
//	}
//
//	public void setValueAt(Object aValue, int r, int c) {
//		model.setValueAt(aValue, rows[r].index, c);
//	}
//
//	public Object getValueAt(int r, int c) {
//		return model.getValueAt(rows[r].index, c);
//	}
//
//	public boolean isCellEditable(int r, int c) {
//		return model.isCellEditable(rows[r].index, c);
//	}
//
//	public int getRowCount() {
//		return model.getRowCount();
//	}
//
//	public int getColumnCount() {
//		return model.getColumnCount();
//	}
//
//	public String getColumnName(int c) {
//		return model.getColumnName(c);
//	}
//
//	public Class getColumnClass(int c) {
//		return model.getColumnClass(c);
//	}
//
//	public void sort(int c) {
//		sortColumn = c;
//		Arrays.sort(rows);
//		fireTableDataChanged();
//	}
//
//	public void addMouseListener(final JTable table) {
//		table.getTableHeader().addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent event) {
//				if (event.getClickCount() < 2) {
//					return;
//				}
//				int tableColumn = table.columnAtPoint(event.getPoint());
//				int modelColumn = table.convertColumnIndexToModel(tableColumn);
//
//				sort(modelColumn);
//			}
//		});
//	}
//
//	private class Row implements Comparable {
//		public int index;
//
//		public int compareTo(Object other) {
//			Row otherRow = (Row) other;
//			Object a = model.getValueAt(index, sortColumn);
//			Object b = model.getValueAt(otherRow.index, sortColumn);
//
//			if (a instanceof Comparable) {
//				return ((Comparable) a).compareTo(b);
//			} else {
//				return index - otherRow.index;
//			}
//		}
//	}
//} // class SortFilterModel
