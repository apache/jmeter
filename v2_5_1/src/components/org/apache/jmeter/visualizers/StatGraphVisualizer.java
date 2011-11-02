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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.SaveGraphics;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 */
public class StatGraphVisualizer extends AbstractVisualizer implements Clearable, ActionListener {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final String[] COLUMNS = { JMeterUtils.getResString("sampler_label"), //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_count"),         //$NON-NLS-1$
            JMeterUtils.getResString("average"),                        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_median"),        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_90%_line"),      //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_min"),           //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_max"),           //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_error%"),        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_rate"),          //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_bandwidth") };   //$NON-NLS-1$

    private final String[] GRAPH_COLUMNS = {JMeterUtils.getResString("average"),//$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_median"),        //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_90%_line"),      //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_min"),           //$NON-NLS-1$
            JMeterUtils.getResString("aggregate_report_max")};          //$NON-NLS-1$

    private final String TOTAL_ROW_LABEL =
        JMeterUtils.getResString("aggregate_report_total_label");       //$NON-NLS-1$

    private JTable myJTable;

    private JScrollPane myScrollPane;

    private transient ObjectTableModel model;

    private final Map<String, SamplingStatCalculator> tableRows =
        new ConcurrentHashMap<String, SamplingStatCalculator>();

    private AxisGraph graphPanel = null;

    private VerticalPanel graph = null;

    private JScrollPane graphScroll = null;

    private JSplitPane spane = null;

    private JLabeledChoice columns =
        new JLabeledChoice(JMeterUtils.getResString("aggregate_graph_column"),GRAPH_COLUMNS);//$NON-NLS-1$

    //NOT USED protected double[][] data = null;

    private JButton displayButton =
        new JButton(JMeterUtils.getResString("aggregate_graph_display"));                //$NON-NLS-1$

    private JButton saveGraph =
        new JButton(JMeterUtils.getResString("aggregate_graph_save"));                    //$NON-NLS-1$

    private JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));            //$NON-NLS-1$

    private JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"));    //$NON-NLS-1$

    private JLabeledTextField graphTitle =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_user_title"));    //$NON-NLS-1$

    private JLabeledTextField maxLengthXAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_max_length_xaxis_label"));//$NON-NLS-1$

    private JLabeledTextField graphWidth =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_width"));        //$NON-NLS-1$
    private JLabeledTextField graphHeight =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_height"));        //$NON-NLS-1$

    private String yAxisLabel = JMeterUtils.getResString("aggregate_graph_response_time");//$NON-NLS-1$

    private String yAxisTitle = JMeterUtils.getResString("aggregate_graph_ms");        //$NON-NLS-1$

    private boolean saveGraphToFile = false;

    private int defaultWidth = 400;

    private int defaultHeight = 300;

    public StatGraphVisualizer() {
        super();
        model = new ObjectTableModel(COLUMNS,
                SamplingStatCalculator.class,
                new Functor[] {
                new Functor("getLabel"),                    //$NON-NLS-1$
                new Functor("getCount"),                    //$NON-NLS-1$
                new Functor("getMeanAsNumber"),                //$NON-NLS-1$
                new Functor("getMedian"),                    //$NON-NLS-1$
                new Functor("getPercentPoint",                //$NON-NLS-1$
                new Object[] { new Float(.900) }),
                new Functor("getMin"),                        //$NON-NLS-1$
                new Functor("getMax"),                         //$NON-NLS-1$
                new Functor("getErrorPercentage"),            //$NON-NLS-1$
                new Functor("getRate"),                        //$NON-NLS-1$
                new Functor("getKBPerSecond") },            //$NON-NLS-1$
                new Functor[] { null, null, null, null, null, null, null, null,    null, null },
                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class, Long.class,
                Long.class, String.class, String.class, String.class });
        clearData();
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
        StatGraphVisualizer instance = new StatGraphVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }

    public String getLabelResource() {
        return "aggregate_graph_title";                        //$NON-NLS-1$
    }

    public void add(SampleResult res) {
        SamplingStatCalculator row = null;
        final String sampleLabel = res.getSampleLabel();
        synchronized (tableRows) {
            row = tableRows.get(sampleLabel);
            if (row == null) {
                row = new SamplingStatCalculator(sampleLabel);
                tableRows.put(row.getLabel(), row);
                model.insertRow(row, model.getRowCount() - 1);
            }
        }
        row.addSample(res);
        tableRows.get(TOTAL_ROW_LABEL).addSample(res);
        model.fireTableDataChanged();
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    public void clearData() {
        model.clearData();
        tableRows.clear();
        tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
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
        Border margin2 = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 80));
        RendererUtils.applyRenderers(myJTable, RENDERERS);
        myScrollPane = new JScrollPane(myJTable);

        graph = new VerticalPanel();
        graph.setBorder(margin2);


        JLabel graphLabel = new JLabel(JMeterUtils.getResString("aggregate_graph")); //$NON-NLS-1$
        graphPanel = new AxisGraph();
        graphPanel.setPreferredSize(new Dimension(defaultWidth,defaultHeight));

        // horizontal panel for the buttons
        HorizontalPanel buttonpanel = new HorizontalPanel();
        buttonpanel.add(columns);
        buttonpanel.add(displayButton);
        buttonpanel.add(saveGraph);
        buttonpanel.add(saveTable);
        buttonpanel.add(saveHeaders);

        graph.add(graphLabel);
        graph.add(graphTitle);
        graph.add(maxLengthXAxisLabel);
        graph.add(graphWidth);
        graph.add(graphHeight);
        graph.add(buttonpanel);
        graph.add(graphPanel);

        displayButton.addActionListener(this);
        saveGraph.addActionListener(this);
        saveTable.addActionListener(this);
        graphScroll = new JScrollPane(graph);
        graphScroll.setAutoscrolls(true);

        spane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spane.setLeftComponent(myScrollPane);
        spane.setRightComponent(graphScroll);
        spane.setResizeWeight(.2);
        spane.setContinuousLayout(true);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(spane,BorderLayout.CENTER);
    }

    public void makeGraph() {
        String wstr = graphWidth.getText();
        String hstr = graphHeight.getText();
        String lstr = maxLengthXAxisLabel.getText();
        if (wstr.length() == 0) {
            wstr = "450";//$NON-NLS-1$
        }
        if (hstr.length() == 0) {
            hstr = "250";//$NON-NLS-1$
        }
        if (lstr.length() == 0) {
            lstr = "20";//$NON-NLS-1$
        }
        int width = Integer.parseInt(wstr);
        int height = Integer.parseInt(hstr);
        int maxLength = Integer.parseInt(lstr);

        graphPanel.setData(this.getData());
        graphPanel.setHeight(height);
        graphPanel.setWidth(width);
        graphPanel.setTitle(graphTitle.getText());
        graphPanel.setMaxLength(maxLength);
        graphPanel.setXAxisLabels(getAxisLabels());
        graphPanel.setXAxisTitle(columns.getText());
        graphPanel.setYAxisLabels(this.yAxisLabel);
        graphPanel.setYAxisTitle(this.yAxisTitle);

        graphPanel.setPreferredSize(new Dimension(width,height));
        graph.setSize(new Dimension(graph.getWidth(), height + 120));
        spane.repaint();
    }

    public double[][] getData() {
        if (model.getRowCount() > 1) {
            int count = model.getRowCount() -1;
            int col = model.findColumn(columns.getText());
            double[][] data = new double[1][count];
            for (int idx=0; idx < count; idx++) {
                data[0][idx] = ((Number)model.getValueAt(idx,col)).doubleValue();
            }
            return data;
        }
        return new double[][]{ { 250, 45, 36, 66, 145, 80, 55  } };
    }

    public String[] getAxisLabels() {
        if (model.getRowCount() > 1) {
            int count = model.getRowCount() -1;
            String[] labels = new String[count];
            for (int idx=0; idx < count; idx++) {
                labels[idx] = (String)model.getValueAt(idx,0);
            }
            return labels;
        }
        return new String[]{ "/", "/samples", "/jsp-samples", "/manager", "/manager/status", "/hello", "/world" };
    }

    /**
     * We use this method to get the data, since we are using
     * ObjectTableModel, so the calling getDataVector doesn't
     * work as expected.
     * @return the data from the model
     */
    public Vector<Vector<Object>> getAllTableData() {
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        if (model.getRowCount() > 0) {
            for (int rw=0; rw < model.getRowCount(); rw++) {
                int cols = model.getColumnCount();
                Vector<Object> column = new Vector<Object>();
                data.add(column);
                for (int idx=0; idx < cols; idx++) {
                    Object val = model.getValueAt(rw,idx);
                    column.add(val);
                }
            }
        }
        return data;
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == displayButton) {
            makeGraph();
        } else if (event.getSource() == saveGraph) {
            saveGraphToFile = true;
            try {
                ActionRouter.getInstance().getAction(
                        ActionNames.SAVE_GRAPHICS,SaveGraphics.class.getName()).doAction(
                                new ActionEvent(this,1,ActionNames.SAVE_GRAPHICS));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else if (event.getSource() == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("statistics.csv");    //$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile());
                CSVSaveService.saveCSVStats(getAllTableData(),writer,saveHeaders.isSelected() ? COLUMNS : null);
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        }
    }

    @Override
    public JComponent getPrintableComponent() {
        if (saveGraphToFile == true) {
            saveGraphToFile = false;
            graphPanel.setBounds(graphPanel.getLocation().x,graphPanel.getLocation().y,
                    graphPanel.width,graphPanel.height);
            return graphPanel;
        }
        return this;
    }
}
