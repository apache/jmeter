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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.gui.util.HeaderAsPropertyRendererWrapper;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.ObjectTableSorter;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.gui.RightAlignRenderer;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.reflect.Functor;

/**
 * This class implements a statistical analyser that calculates both the average
 * and the standard deviation of the sampling process. The samples are displayed
 * in a JTable, and the statistics are displayed at the bottom of the table.
 *
 */
public class TableVisualizer extends AbstractVisualizer implements Clearable {

    private static final long serialVersionUID = 241L;

    private static final String ICON_SIZE = JMeterUtils.getPropDefault(JMeter.TREE_ICON_SIZE, JMeter.DEFAULT_TREE_ICON_SIZE);

    private static final int REFRESH_PERIOD = JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500);

    // Note: the resource string won't respond to locale-changes,
    // however this does not matter as it is only used when pasting to the clipboard
    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                                       "vrt/" + ICON_SIZE + "/security-high-2.png"),    //$NON-NLS-1$ $NON-NLS-2$
            JMeterUtils.getResString("table_visualizer_success")); //$NON-NLS-1$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                                       "vrt/" + ICON_SIZE + "/security-low-2.png"),    //$NON-NLS-1$ $NON-NLS-2$
            JMeterUtils.getResString("table_visualizer_warning")); //$NON-NLS-1$

    private static final String[] COLUMNS = new String[] {
            "table_visualizer_sample_num",  // $NON-NLS-1$
            "table_visualizer_start_time",  // $NON-NLS-1$
            "table_visualizer_thread_name", // $NON-NLS-1$
            "sampler_label",                // $NON-NLS-1$
            "table_visualizer_sample_time", // $NON-NLS-1$
            "table_visualizer_status",      // $NON-NLS-1$
            "table_visualizer_bytes",       // $NON-NLS-1$
            "table_visualizer_sent_bytes",       // $NON-NLS-1$
            "table_visualizer_latency",     // $NON-NLS-1$
            "table_visualizer_connect"};    // $NON-NLS-1$

    private ObjectTableModel model = null;

    private JTable table = null;

    private JTextField dataField = null;

    private JTextField averageField = null;

    private JTextField deviationField = null;

    private JTextField noSamplesField = null;

    private JScrollPane tableScrollPanel = null;

    private JCheckBox autoscroll = null;

    private JCheckBox childSamples = null;

    private final transient Calculator calc = new Calculator();

    private Format format = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$

    private Deque<SampleResult> newRows = new ConcurrentLinkedDeque<>();

    // Column renderers
    private static final TableCellRenderer[] RENDERERS =
        new TableCellRenderer[]{
            new RightAlignRenderer(), // Sample number (string)
            new RightAlignRenderer(), // Start Time
            null, // Thread Name
            null, // Label
            null, // Sample Time
            null, // Status
            null, // Bytes
        };

    /**
     * Constructor for the TableVisualizer object.
     */
    public TableVisualizer() {
        super();
        model = new ObjectTableModel(COLUMNS,
                TableSample.class,         // The object used for each row
                new Functor[] {
                new Functor("getSampleNumberString"),  // $NON-NLS-1$
                new Functor("getStartTimeFormatted",   // $NON-NLS-1$
                        new Object[]{format}),
                new Functor("getThreadName"),          // $NON-NLS-1$
                new Functor("getLabel"),               // $NON-NLS-1$
                new Functor("getElapsed"),             // $NON-NLS-1$
                new SampleSuccessFunctor("isSuccess"), // $NON-NLS-1$
                new Functor("getBytes"),               // $NON-NLS-1$
                new Functor("getSentBytes"),               // $NON-NLS-1$
                new Functor("getLatency"),             // $NON-NLS-1$
                new Functor("getConnectTime") },       // $NON-NLS-1$
                new Functor[] { null, null, null, null, null, null, null, null, null, null },
                new Class[] {
                String.class, String.class, String.class, String.class, Long.class, ImageIcon.class, Long.class, Long.class, Long.class, Long.class });
        init();
    }

    public static boolean testFunctors(){
        TableVisualizer instance = new TableVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }


    @Override
    public String getLabelResource() {
        return "view_results_in_table"; // $NON-NLS-1$
    }

    protected synchronized void updateTextFields(SampleResult res) {
        noSamplesField.setText(Long.toString(calc.getCount()));
        if(res.getSampleCount() > 0) {
            dataField.setText(Long.toString(res.getTime()/res.getSampleCount()));
        } else {
            dataField.setText("0");
        }
        averageField.setText(Long.toString((long) calc.getMean()));
        deviationField.setText(Long.toString((long) calc.getStandardDeviation()));
    }

    @Override
    public void add(final SampleResult res) {
        if (childSamples.isSelected()) {
            SampleResult[] subResults = res.getSubResults();
            if (subResults.length > 0) {
                for (SampleResult sr : subResults) {
                    add(sr);
                }
                return;
            }
        }
        newRows.add(res);

    }

    @Override
    public synchronized void clearData() {
        synchronized (calc) {
            model.clearData();
            calc.clear();
            newRows.clear();
            noSamplesField.setText("0"); // $NON-NLS-1$
            dataField.setText("0"); // $NON-NLS-1$
            averageField.setText("0"); // $NON-NLS-1$
            deviationField.setText("0"); // $NON-NLS-1$
        }
        repaint();
    }

    @Override
    public String toString() {
        return "Show the samples in a table";
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));

        // NAME
        mainPanel.add(makeTitlePanel());

        // Set up the table itself
        table = new JTable(model);
        table.setRowSorter(new ObjectTableSorter(model).setValueComparator(5,
                Comparator.nullsFirst(
                        (ImageIcon o1, ImageIcon o2) -> {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1 == imageSuccess) {
                                return -1;
                            }
                            if (o1 == imageFailure) {
                                return 1;
                            }
                            throw new IllegalArgumentException("Only success and failure images can be compared");
                        })));
        JMeterUtils.applyHiDPI(table);
        HeaderAsPropertyRendererWrapper.setupDefaultRenderer(table);
        RendererUtils.applyRenderers(table, RENDERERS);

        tableScrollPanel = new JScrollPane(table);
        tableScrollPanel.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        autoscroll = new JCheckBox(JMeterUtils.getResString("view_results_autoscroll")); //$NON-NLS-1$

        childSamples = new JCheckBox(JMeterUtils.getResString("view_results_childsamples")); //$NON-NLS-1$

        // Set up footer of table which displays numerics of the graphs
        JPanel dataPanel = new JPanel();
        JLabel dataLabel = new JLabel(JMeterUtils.getResString("graph_results_latest_sample")); // $NON-NLS-1$
        dataLabel.setForeground(Color.black);
        dataField = new JTextField(5);
        dataField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataField.setEditable(false);
        dataField.setForeground(Color.black);
        dataField.setBackground(getBackground());
        dataPanel.add(dataLabel);
        dataPanel.add(dataField);

        JPanel averagePanel = new JPanel();
        JLabel averageLabel = new JLabel(JMeterUtils.getResString("graph_results_average")); // $NON-NLS-1$
        averageLabel.setForeground(Color.blue);
        averageField = new JTextField(5);
        averageField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        averageField.setEditable(false);
        averageField.setForeground(Color.blue);
        averageField.setBackground(getBackground());
        averagePanel.add(averageLabel);
        averagePanel.add(averageField);

        JPanel deviationPanel = new JPanel();
        JLabel deviationLabel = new JLabel(JMeterUtils.getResString("graph_results_deviation")); // $NON-NLS-1$
        deviationLabel.setForeground(Color.red);
        deviationField = new JTextField(5);
        deviationField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deviationField.setEditable(false);
        deviationField.setForeground(Color.red);
        deviationField.setBackground(getBackground());
        deviationPanel.add(deviationLabel);
        deviationPanel.add(deviationField);

        JPanel noSamplesPanel = new JPanel();
        JLabel noSamplesLabel = new JLabel(JMeterUtils.getResString("graph_results_no_samples")); // $NON-NLS-1$

        noSamplesField = new JTextField(8);
        noSamplesField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noSamplesField.setEditable(false);
        noSamplesField.setForeground(Color.black);
        noSamplesField.setBackground(getBackground());
        noSamplesPanel.add(noSamplesLabel);
        noSamplesPanel.add(noSamplesField);

        JPanel tableInfoPanel = new JPanel();
        tableInfoPanel.setLayout(new FlowLayout());
        tableInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tableInfoPanel.add(noSamplesPanel);
        tableInfoPanel.add(dataPanel);
        tableInfoPanel.add(averagePanel);
        tableInfoPanel.add(deviationPanel);

        JPanel tableControlsPanel = new JPanel(new BorderLayout());
        tableControlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel jp = new HorizontalPanel();
        jp.add(autoscroll);
        jp.add(childSamples);
        tableControlsPanel.add(jp, BorderLayout.WEST);
        tableControlsPanel.add(tableInfoPanel, BorderLayout.CENTER);

        // Set up the table with footer
        JPanel tablePanel = new JPanel();

        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(tableScrollPanel, BorderLayout.CENTER);
        tablePanel.add(tableControlsPanel, BorderLayout.SOUTH);

        // Add the main panel and the graph
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(tablePanel, BorderLayout.CENTER);
        new Timer(REFRESH_PERIOD, e -> collectNewSamples()).start();
    }

    private void collectNewSamples() {
        synchronized (calc) {
            SampleResult res = null;
            while (!newRows.isEmpty()) {
                res = newRows.pop();
                calc.addSample(res);
                int count = calc.getCount();
                TableSample newS = new TableSample(
                        count,
                        res.getSampleCount(),
                        res.getStartTime(),
                        res.getThreadName(),
                        res.getSampleLabel(),
                        res.getTime(),
                        res.isSuccessful(),
                        res.getBytesAsLong(),
                        res.getSentBytes(),
                        res.getLatency(),
                        res.getConnectTime()
                        );
                model.addRow(newS);
            }
            if (res == null) {
                return;
            }
            updateTextFields(res);
            if (autoscroll.isSelected()) {
                table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
            }
        }
    }

    public static class SampleSuccessFunctor extends Functor {
        public SampleSuccessFunctor(String methodName) {
            super(methodName);
        }

        @Override
        public Object invoke(Object pInvokee) {
            Boolean success = (Boolean) super.invoke(pInvokee);

            if (success != null) {
                if (success.booleanValue()) {
                    return imageSuccess;
                } else {
                    return imageFailure;
                }
            } else {
                return null;
            }
        }
    }
}
