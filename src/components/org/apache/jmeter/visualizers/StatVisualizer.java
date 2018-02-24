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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRendererWrapper;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.ObjectTableSorter;
import org.apache.jorphan.gui.RendererUtils;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter.
 */
@GUIMenuSortOrder(3)
public class StatVisualizer extends AbstractVisualizer implements Clearable, ActionListener {

    private static final long serialVersionUID = 241L;

    private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$
    private static final String SAVE_HEADERS = "saveHeaders"; //$NON-NLS-1$
    private static final String TOTAL_ROW_LABEL = JMeterUtils
            .getResString("aggregate_report_total_label"); //$NON-NLS-1$
    private static final int REFRESH_PERIOD = JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500); // $NON-NLS-1$

    private JTable myJTable;
    private JScrollPane myScrollPane;

    private final JButton saveTable = new JButton(
            JMeterUtils.getResString("aggregate_graph_save_table")); //$NON-NLS-1$

    private final JCheckBox saveHeaders = new JCheckBox(
            JMeterUtils.getResString("aggregate_graph_save_table_header"), true); //$NON-NLS-1$

    private final JCheckBox useGroupName = new JCheckBox(
            JMeterUtils.getResString("aggregate_graph_use_group_name")); //$NON-NLS-1$

    private transient ObjectTableModel model;

    /** Lock used to protect tableRows update + model update */
    private final transient Object lock = new Object();

    private final Map<String, SamplingStatCalculator> tableRows = new ConcurrentHashMap<>();

    private Deque<SamplingStatCalculator> newRows = new ConcurrentLinkedDeque<>();

    private volatile boolean dataChanged;

    public StatVisualizer() {
        super();
        model = StatGraphVisualizer.createObjectTableModel();
        clearData();
        init();
    }

    /**
     * @return <code>true</code> iff all functors can be found
     * @deprecated - only for use in testing
     * */
    @Deprecated
    public static boolean testFunctors(){
        StatVisualizer instance = new StatVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }

    @Override
    public String getLabelResource() {
        return "aggregate_report";  //$NON-NLS-1$
    }

    @Override
    public void add(final SampleResult res) {
        SamplingStatCalculator row = tableRows.computeIfAbsent(
                res.getSampleLabel(useGroupName.isSelected()),
                label -> {
                    SamplingStatCalculator newRow = new SamplingStatCalculator(label);
                    newRows.add(newRow);
                    return newRow;
                });
        synchronized (row) {
            /*
             * Synch is needed because multiple threads can update the counts.
             */
            row.addSample(res);
        }
        SamplingStatCalculator tot = tableRows.get(TOTAL_ROW_LABEL);
        synchronized(lock) {
            tot.addSample(res);
        }
        dataChanged = true;
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    @Override
    public void clearData() {
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
            newRows.clear();
            tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
            model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
    }

    /**
     * Main visualizer setup.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.setRowSorter(new ObjectTableSorter(model).fixLastRow());
        JMeterUtils.applyHiDPI(myJTable);
        HeaderAsPropertyRendererWrapper.setupDefaultRenderer(myJTable, StatGraphVisualizer.getColumnsMsgParameters());
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, StatGraphVisualizer.getRenderers());
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
        saveTable.addActionListener(this);
        JPanel opts = new JPanel();
        opts.add(useGroupName, BorderLayout.WEST);
        opts.add(saveTable, BorderLayout.CENTER);
        opts.add(saveHeaders, BorderLayout.EAST);
        this.add(opts,BorderLayout.SOUTH);

        new Timer(REFRESH_PERIOD, e -> {
            if (!dataChanged) {
                return;
            }
            dataChanged = false;
            synchronized (lock) {
                while (!newRows.isEmpty()) {
                    model.insertRow(newRows.pop(), model.getRowCount() - 1);
                }
            }
            model.fireTableDataChanged();
        }).start();
    }

    @Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(USE_GROUP_NAME, useGroupName.isSelected(), false);
        c.setProperty(SAVE_HEADERS, saveHeaders.isSelected(), true);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        useGroupName.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
        saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("aggregate.csv");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            try (FileOutputStream fo = new FileOutputStream(chooser.getSelectedFile());
                    OutputStreamWriter writer = new OutputStreamWriter(fo, Charset.forName("UTF-8"))){
                CSVSaveService.saveCSVStats(StatGraphVisualizer.getAllTableData(model, StatGraphVisualizer.getFormatters()),
                        writer,
                        saveHeaders.isSelected() ? StatGraphVisualizer.getLabels(StatGraphVisualizer.getColumns()) : null);
            } catch (IOException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), "Error saving data");
            }
        }
    }
}
