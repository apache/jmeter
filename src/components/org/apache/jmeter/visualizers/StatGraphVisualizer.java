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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.SaveGraphics;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
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
    
    private final Border MARGIN = new EmptyBorder(0, 5, 0, 5);

    private JTable myJTable;

    private JScrollPane myScrollPane;

    private transient ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();
    
    private final Map<String, SamplingStatCalculator> tableRows =
        new ConcurrentHashMap<String, SamplingStatCalculator>();

    private AxisGraph graphPanel = null;

    private JPanel settingsPane = null;

    private JSplitPane spane = null;

    //NOT USED protected double[][] data = null;

    private JTabbedPane tabbedGraph = new JTabbedPane(JTabbedPane.TOP);

    private JButton displayButton =
        new JButton(JMeterUtils.getResString("aggregate_graph_display"));                //$NON-NLS-1$

    private JButton saveGraph =
        new JButton(JMeterUtils.getResString("aggregate_graph_save"));                    //$NON-NLS-1$

    private JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));            //$NON-NLS-1$

    private JButton chooseBarColor =
        new JButton(JMeterUtils.getResString("aggregate_graph_choose_bar_color"));            //$NON-NLS-1$

    private JButton chooseForeColor =
        new JButton(JMeterUtils.getResString("aggregate_graph_choose_foreground_color"));            //$NON-NLS-1$

    private JButton syncWithName =
        new JButton(JMeterUtils.getResString("aggregate_graph_sync_with_name"));            //$NON-NLS-1$

    private JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"));    //$NON-NLS-1$

    private JLabeledTextField graphTitle =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_user_title"));    //$NON-NLS-1$

    private JLabeledTextField maxLengthXAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_max_length_xaxis_label"));//$NON-NLS-1$

    private JLabeledTextField maxValueYAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_yaxis_max_value"));//$NON-NLS-1$

    /**
     * checkbox for use dynamic graph size
     */
    private JCheckBox dynamicGraphSize = new JCheckBox(JMeterUtils.getResString("aggregate_graph_dynamic_size")); // $NON-NLS-1$

    private JLabeledTextField graphWidth =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_width"));        //$NON-NLS-1$
    private JLabeledTextField graphHeight =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_height"));        //$NON-NLS-1$

    private String yAxisLabel = JMeterUtils.getResString("aggregate_graph_response_time");//$NON-NLS-1$

    private String yAxisTitle = JMeterUtils.getResString("aggregate_graph_ms");        //$NON-NLS-1$

    private boolean saveGraphToFile = false;

    private int defaultWidth = 400;

    private int defaultHeight = 300;

    private JLabel currentColor = new JLabel(JMeterUtils.getResString("aggregate_graph_current_colors"));   //$NON-NLS-1$

    private JComboBox columnsList = new JComboBox(GRAPH_COLUMNS);

    private JCheckBox columnSelection = new JCheckBox(JMeterUtils.getResString("aggregate_graph_column_selection"), false); //$NON-NLS-1$

    private JTextField columnMatchLabel = new JTextField();

    private JButton reloadButton = new JButton(JMeterUtils.getResString("aggregate_graph_reload_data")); // $NON-NLS-1$

    private JCheckBox caseChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false); // $NON-NLS-1$

    private JCheckBox regexpChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), true); // $NON-NLS-1$

    private JComboBox titleFontNameList = new JComboBox(StatGraphProperties.getFontNameMap().keySet().toArray());

    private JComboBox titleFontSizeList = new JComboBox(StatGraphProperties.fontSize);

    private JComboBox titleFontStyleList = new JComboBox(StatGraphProperties.getFontStyleMap().keySet().toArray());

    private JComboBox fontNameList = new JComboBox(StatGraphProperties.getFontNameMap().keySet().toArray());

    private JComboBox fontSizeList = new JComboBox(StatGraphProperties.fontSize);

    private JComboBox fontStyleList = new JComboBox(StatGraphProperties.getFontStyleMap().keySet().toArray());

    private JComboBox legendPlacementList = new JComboBox(StatGraphProperties.getPlacementNameMap().keySet().toArray());

    private JCheckBox drawOutlinesBar = new JCheckBox(JMeterUtils.getResString("aggregate_graph_draw_outlines"), true); // Default checked // $NON-NLS-1$

    private JCheckBox numberShowGrouping = new JCheckBox(JMeterUtils.getResString("aggregate_graph_number_grouping"), true); // Default checked // $NON-NLS-1$

    private Color colorBarGraph = Color.YELLOW;

    private Color colorForeGraph = Color.BLACK;

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
        Matcher matcher = null;
        if (columnSelection.isSelected() && columnMatchLabel.getText() != null && columnMatchLabel.getText().length() > 0) {
                Pattern pattern = createPattern(columnMatchLabel.getText());
                matcher = pattern.matcher(sampleLabel);
        }
        if ((matcher == null) || (matcher.find())) {
            synchronized (lock) {
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
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    public void clearData() {
        synchronized (lock) {
	        model.clearData();
	        tableRows.clear();
	        tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
	        model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
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

        settingsPane = new VerticalPanel();
        settingsPane.setBorder(margin2);

        graphPanel = new AxisGraph();
        graphPanel.setPreferredSize(new Dimension(defaultWidth, defaultHeight));

        settingsPane.add(createGraphActionsPane());
        settingsPane.add(createGraphColumnPane());
        settingsPane.add(createGraphTitlePane());
        settingsPane.add(createGraphDimensionPane());
        JPanel axisPane = new JPanel(new BorderLayout());
        axisPane.add(createGraphXAxisPane(), BorderLayout.WEST);
        axisPane.add(createGraphYAxisPane(), BorderLayout.CENTER);
        settingsPane.add(axisPane);
        settingsPane.add(createLegendPane());

        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_settings"), settingsPane); //$NON-NLS-1$
        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_graph"), graphPanel); //$NON-NLS-1$

        spane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spane.setLeftComponent(myScrollPane);
        spane.setRightComponent(tabbedGraph);
        spane.setResizeWeight(.2);
        spane.setContinuousLayout(true);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(spane, BorderLayout.CENTER);
    }

    public void makeGraph() {
        Dimension size = graphPanel.getSize();
        String wstr = graphWidth.getText();
        String hstr = graphHeight.getText();
        String lstr = maxLengthXAxisLabel.getText();
        int width = (int) size.getWidth();
        if (wstr.length() != 0) {
            width = Integer.parseInt(wstr);
        }
        int height = (int) size.getHeight();
        if (hstr.length() != 0) {
            height = Integer.parseInt(hstr);
        }
        if (lstr.length() == 0) {
            lstr = "20";//$NON-NLS-1$
        }
        int maxLength = Integer.parseInt(lstr);
        String yAxisStr = maxValueYAxisLabel.getText();
        int maxYAxisScale = yAxisStr.length() == 0 ? 0 : Integer.parseInt(yAxisStr);

        graphPanel.setData(this.getData());
        graphPanel.setTitle(graphTitle.getText());
        graphPanel.setMaxLength(maxLength);
        graphPanel.setMaxYAxisScale(maxYAxisScale);
        graphPanel.setXAxisLabels(getAxisLabels());
        graphPanel.setXAxisTitle((String) columnsList.getSelectedItem());
        graphPanel.setYAxisLabels(this.yAxisLabel);
        graphPanel.setYAxisTitle(this.yAxisTitle);
        graphPanel.setColor(colorBarGraph);
        graphPanel.setForeColor(colorForeGraph);
        graphPanel.setOutlinesBarFlag(drawOutlinesBar.isSelected());
        graphPanel.setShowGrouping(numberShowGrouping.isSelected());
        graphPanel.setLegendPlacement(StatGraphProperties.getPlacementNameMap()
                .get(legendPlacementList.getSelectedItem()).intValue());

        graphPanel.setTitleFont(new Font(StatGraphProperties.getFontNameMap().get(titleFontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(titleFontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) titleFontSizeList.getSelectedItem())));
        graphPanel.setLegendFont(new Font(StatGraphProperties.getFontNameMap().get(fontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(fontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) fontSizeList.getSelectedItem())));

        graphPanel.setHeight(height);
        graphPanel.setWidth(width);
        spane.repaint();
    }

    public double[][] getData() {
        if (model.getRowCount() > 1) {
            int count = model.getRowCount() -1;
            int col = model.findColumn((String) columnsList.getSelectedItem());
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
    public List<List<Object>> getAllTableData() {
        List<List<Object>> data = new ArrayList<List<Object>>();
        if (model.getRowCount() > 0) {
            for (int rw=0; rw < model.getRowCount(); rw++) {
                int cols = model.getColumnCount();
                List<Object> column = new ArrayList<Object>();
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
        final Object eventSource = event.getSource();
        if (eventSource == displayButton) {
            makeGraph();
            tabbedGraph.setSelectedIndex(1);
        } else if (eventSource == saveGraph) {
            saveGraphToFile = true;
            try {
                ActionRouter.getInstance().getAction(
                        ActionNames.SAVE_GRAPHICS,SaveGraphics.class.getName()).doAction(
                                new ActionEvent(this,1,ActionNames.SAVE_GRAPHICS));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else if (eventSource == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("statistics.csv");    //$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile()); // TODO Charset ?
                CSVSaveService.saveCSVStats(getAllTableData(),writer,saveHeaders.isSelected() ? COLUMNS : null);
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        } else if (eventSource == chooseBarColor) {
            colorBarGraph = JColorChooser.showDialog(
                    null,
                    JMeterUtils.getResString("aggregate_graph_choose_color"), //$NON-NLS-1$
                    colorBarGraph);
            currentColor.setBackground(colorBarGraph);
        } else if (eventSource == chooseForeColor) {
            colorForeGraph = JColorChooser.showDialog(
                    null,
                    JMeterUtils.getResString("aggregate_graph_choose_color"), //$NON-NLS-1$
                    colorBarGraph);
            currentColor.setForeground(colorForeGraph);
        } else if (eventSource == syncWithName) {
            graphTitle.setText(namePanel.getName());
        } else if (eventSource == dynamicGraphSize) {
            // if use dynamic graph size is checked, we disable the dimension fields
            if (dynamicGraphSize.isSelected()) {
                graphWidth.setEnabled(false);
                graphHeight.setEnabled(false);
            } else {
                graphWidth.setEnabled(true);
                graphHeight.setEnabled(true);
            }
        } else if (eventSource == columnSelection) {
            if (columnSelection.isSelected()) {
                columnMatchLabel.setEnabled(true);
                reloadButton.setEnabled(true);
                caseChkBox.setEnabled(true);
                regexpChkBox.setEnabled(true);
            } else {
                columnMatchLabel.setEnabled(false);
                reloadButton.setEnabled(false);
                caseChkBox.setEnabled(false);
                regexpChkBox.setEnabled(false);
            }
        } else if (eventSource == reloadButton) {
            if (getFile() != null && getFile().length() > 0) {
                clearData();
                FilePanel filePanel = (FilePanel) getFilePanel();
                filePanel.actionPerformed(event);
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

    private JPanel createGraphActionsPane() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel displayPane = new JPanel();
        displayPane.add(displayButton);
        displayButton.addActionListener(this);
        buttonPanel.add(displayPane, BorderLayout.WEST);

        JPanel savePane = new JPanel();
        savePane.add(saveGraph);
        savePane.add(saveTable);
        savePane.add(saveHeaders);
        saveGraph.addActionListener(this);
        saveTable.addActionListener(this);
        syncWithName.addActionListener(this);
        buttonPanel.add(savePane, BorderLayout.EAST);

        return buttonPanel;
    }

    private JPanel createGraphColumnPane() {
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        barPanel.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_column"), //$NON-NLS-1$
                columnsList));

        currentColor.setBorder(new EmptyBorder(2, 5, 2, 5));
        currentColor.setOpaque(true);
        currentColor.setBackground(colorBarGraph);

        barPanel.add(Box.createRigidArea(new Dimension(5,0)));
        barPanel.add(currentColor);
        barPanel.add(Box.createRigidArea(new Dimension(5,0)));
        barPanel.add(chooseBarColor);
        chooseBarColor.addActionListener(this);
        barPanel.add(Box.createRigidArea(new Dimension(5,0)));
        barPanel.add(chooseForeColor);
        chooseForeColor.addActionListener(this);

        barPanel.add(drawOutlinesBar);
        barPanel.add(numberShowGrouping);

        JPanel columnPane = new JPanel(new BorderLayout());
        columnPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_column_settings"))); // $NON-NLS-1$
        columnPane.add(barPanel, BorderLayout.NORTH);
        columnPane.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.CENTER);
        columnPane.add(createGraphSelectionSubPane(), BorderLayout.SOUTH);
        
        return columnPane;
    }
    
    private JPanel createGraphSelectionSubPane() {
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        // Search field
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        searchPanel.add(columnSelection);
        columnMatchLabel.setEnabled(false);
        reloadButton.setEnabled(false);
        caseChkBox.setEnabled(false);
        regexpChkBox.setEnabled(false);
        columnSelection.addActionListener(this);

        searchPanel.add(columnMatchLabel);
        searchPanel.add(Box.createRigidArea(new Dimension(5,0)));

        // Button
        reloadButton.setFont(font);
        reloadButton.addActionListener(this);
        searchPanel.add(reloadButton);

        // checkboxes
        caseChkBox.setFont(font);
        searchPanel.add(caseChkBox);
        regexpChkBox.setFont(font);
        searchPanel.add(regexpChkBox);

        return searchPanel;
    }

    private JPanel createGraphTitlePane() {
        JPanel titleNamePane = new JPanel(new BorderLayout());
        syncWithName.setFont(new Font("SansSerif", Font.PLAIN, 10));
        titleNamePane.add(graphTitle, BorderLayout.CENTER);
        titleNamePane.add(syncWithName, BorderLayout.EAST);
        
        JPanel titleStylePane = new JPanel();
        titleStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        titleStylePane.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                titleFontNameList));
        titleFontNameList.setSelectedIndex(0); // default: sans serif
        titleStylePane.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                titleFontSizeList));
        titleFontSizeList.setSelectedItem(StatGraphProperties.fontSize[6]); // default: 16
        titleStylePane.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                titleFontStyleList));
        titleFontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.bold")); // default: bold

        JPanel titlePane = new JPanel(new BorderLayout());
        titlePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_title_group"))); // $NON-NLS-1$
        titlePane.add(titleNamePane, BorderLayout.NORTH);
        titlePane.add(titleStylePane, BorderLayout.SOUTH);
        return titlePane;
    }

    private JPanel createGraphDimensionPane() {
        JPanel dimensionPane = new JPanel();
        dimensionPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dimensionPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_dimension"))); // $NON-NLS-1$

        dimensionPane.add(dynamicGraphSize);
        dynamicGraphSize.setSelected(true); // default option
        graphWidth.setEnabled(false);
        graphHeight.setEnabled(false);
        dynamicGraphSize.addActionListener(this);
        dimensionPane.add(Box.createRigidArea(new Dimension(10,0)));
        dimensionPane.add(graphWidth);
        dimensionPane.add(Box.createRigidArea(new Dimension(5,0)));
        dimensionPane.add(graphHeight);
        return dimensionPane;
    }

    /**
     * Create pane for X Axis options
     * @return X Axis pane
     */
    private JPanel createGraphXAxisPane() {
        JPanel xAxisPane = new JPanel();
        xAxisPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        xAxisPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_xaxis_group"))); // $NON-NLS-1$
        xAxisPane.add(maxLengthXAxisLabel);
        return xAxisPane;
    }

    /**
     * Create pane for Y Axis options
     * @return Y Axis pane
     */
    private JPanel createGraphYAxisPane() {
        JPanel yAxisPane = new JPanel();
        yAxisPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        yAxisPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_yaxis_group"))); // $NON-NLS-1$
        yAxisPane.add(maxValueYAxisLabel);
        return yAxisPane;
    }

    /**
     * Create pane for legend settings
     * @return Legend pane
     */
    private JPanel createLegendPane() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_legend"))); // $NON-NLS-1$

        legendPanel.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_legend_placement"), //$NON-NLS-1$
                legendPlacementList));
        legendPlacementList.setSelectedItem(JMeterUtils.getResString("aggregate_graph_legend.placement.right")); // default: right
        legendPanel.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                fontNameList));
        fontNameList.setSelectedIndex(0); // default: sans serif
        legendPanel.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                fontSizeList));
        fontSizeList.setSelectedItem(StatGraphProperties.fontSize[2]); // default: 10
        legendPanel.add(createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                fontStyleList));
        fontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.normal")); // default: normal

        return legendPanel;
    }

    private JComponent createLabelCombo(String label, JComboBox comboBox) {
        JPanel labelCombo = new JPanel();
        labelCombo.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel caption = new JLabel(label);//$NON-NLS-1$
        caption.setBorder(MARGIN);
        labelCombo.add(caption);
        labelCombo.add(comboBox);
        return labelCombo;
    }

    /**
     * @param textToFind
     * @return pattern ready to search
     */
    private Pattern createPattern(String textToFind) {
        String textToFindQ = Pattern.quote(textToFind);
        if (regexpChkBox.isSelected()) {
            textToFindQ = textToFind;
        }
        Pattern pattern = null;
        try {
            if (caseChkBox.isSelected()) {
                pattern = Pattern.compile(textToFindQ);
            } else {
                pattern = Pattern.compile(textToFindQ, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException pse) {
            return null;
        }
        return pattern;
    }
}
