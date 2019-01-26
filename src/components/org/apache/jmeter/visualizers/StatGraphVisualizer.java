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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.SaveGraphics;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.HeaderAsPropertyRendererWrapper;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.ObjectTableSorter;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 */
public class StatGraphVisualizer extends AbstractVisualizer implements Clearable, ActionListener {
    private static final long serialVersionUID = 242L;

    private static final String PCT1_LABEL = JMeterUtils.getPropDefault("aggregate_rpt_pct1", "90");
    private static final String PCT2_LABEL = JMeterUtils.getPropDefault("aggregate_rpt_pct2", "95");
    private static final String PCT3_LABEL = JMeterUtils.getPropDefault("aggregate_rpt_pct3", "99");

    private static final Float PCT1_VALUE = Float.valueOf(Float.parseFloat(PCT1_LABEL)/100);
    private static final Float PCT2_VALUE =  Float.valueOf(Float.parseFloat(PCT2_LABEL)/100);
    private static final Float PCT3_VALUE =  Float.valueOf(Float.parseFloat(PCT3_LABEL)/100);

    private static final Logger log = LoggerFactory.getLogger(StatGraphVisualizer.class);

    private static final String[] COLUMNS = { 
            "sampler_label",                        //$NON-NLS-1$
            "aggregate_report_count",               //$NON-NLS-1$
            "average",                              //$NON-NLS-1$
            "aggregate_report_median",              //$NON-NLS-1$
            "aggregate_report_xx_pct1_line",        //$NON-NLS-1$
            "aggregate_report_xx_pct2_line",        //$NON-NLS-1$
            "aggregate_report_xx_pct3_line",        //$NON-NLS-1$
            "aggregate_report_min",                 //$NON-NLS-1$
            "aggregate_report_max",                 //$NON-NLS-1$
            "aggregate_report_error%",              //$NON-NLS-1$
            "aggregate_report_rate",                //$NON-NLS-1$
            "aggregate_report_bandwidth",           //$NON-NLS-1$
            "aggregate_report_sent_bytes_per_sec"   //$NON-NLS-1$
    };

    private static final String[] GRAPH_COLUMNS = {
            "average",                          //$NON-NLS-1$
            "aggregate_report_median",          //$NON-NLS-1$
            "aggregate_report_xx_pct1_line",    //$NON-NLS-1$
            "aggregate_report_xx_pct2_line",    //$NON-NLS-1$
            "aggregate_report_xx_pct3_line",    //$NON-NLS-1$
            "aggregate_report_min",             //$NON-NLS-1$
            "aggregate_report_max"};            //$NON-NLS-1$

    private static final String TOTAL_ROW_LABEL =
        JMeterUtils.getResString("aggregate_report_total_label");       //$NON-NLS-1$

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font"); //$NON-NLS-1$

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8)); //$NON-NLS-1$

    private static final int REFRESH_PERIOD = JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500);

    private JTable myJTable;

    private JScrollPane myScrollPane;

    private transient ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();

    private final Map<String, SamplingStatCalculator> tableRows = new ConcurrentHashMap<>();

    private AxisGraph graphPanel = null;

    private JPanel settingsPane = null;

    private JSplitPane spane = null;

    private JTabbedPane tabbedGraph = new JTabbedPane(SwingConstants.TOP);

    private JButton displayButton =
        new JButton(JMeterUtils.getResString("aggregate_graph_display"));                //$NON-NLS-1$

    private JButton saveGraph =
        new JButton(JMeterUtils.getResString("aggregate_graph_save"));                    //$NON-NLS-1$

    private JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));            //$NON-NLS-1$

    private JButton chooseForeColor =
        new JButton(JMeterUtils.getResString("aggregate_graph_choose_foreground_color"));            //$NON-NLS-1$

    private JButton syncWithName =
        new JButton(JMeterUtils.getResString("aggregate_graph_sync_with_name"));            //$NON-NLS-1$

    private JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"));    //$NON-NLS-1$

    private JLabeledTextField graphTitle =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_user_title"));    //$NON-NLS-1$

    private JLabeledTextField maxLengthXAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_max_length_xaxis_label"), 8);//$NON-NLS-1$

    private JLabeledTextField maxValueYAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_yaxis_max_value"), 8);//$NON-NLS-1$

    /**
     * checkbox for use dynamic graph size
     */
    private JCheckBox dynamicGraphSize = new JCheckBox(JMeterUtils.getResString("aggregate_graph_dynamic_size")); // $NON-NLS-1$

    private JLabeledTextField graphWidth =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_width"), 6);        //$NON-NLS-1$
    private JLabeledTextField graphHeight =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_height"), 6);        //$NON-NLS-1$

    private String yAxisLabel = JMeterUtils.getResString("aggregate_graph_response_time");//$NON-NLS-1$

    private String yAxisTitle = JMeterUtils.getResString("aggregate_graph_ms");        //$NON-NLS-1$

    private boolean saveGraphToFile = false;

    private int defaultWidth = 400;

    private int defaultHeight = 300;

    private JComboBox<String> columnsList = new JComboBox<>(getLabels(GRAPH_COLUMNS));

    private List<BarGraph> eltList = new ArrayList<>();

    private JCheckBox columnSelection = new JCheckBox(JMeterUtils.getResString("aggregate_graph_column_selection"), false); //$NON-NLS-1$

    private JTextField columnMatchLabel = new JTextField();

    private JButton applyFilterBtn = new JButton(JMeterUtils.getResString("graph_apply_filter")); // $NON-NLS-1$

    private JCheckBox caseChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false); // $NON-NLS-1$

    private JCheckBox regexpChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), true); // $NON-NLS-1$

    private JComboBox<String> titleFontNameList = new JComboBox<>(keys(StatGraphProperties.getFontNameMap()));

    private JComboBox<String> titleFontSizeList = new JComboBox<>(StatGraphProperties.getFontSize());

    private JComboBox<String> titleFontStyleList = new JComboBox<>(keys(StatGraphProperties.getFontStyleMap()));

    private JComboBox<String> valueFontNameList = new JComboBox<>(keys(StatGraphProperties.getFontNameMap()));

    private JComboBox<String> valueFontSizeList = new JComboBox<>(StatGraphProperties.getFontSize());

    private JComboBox<String> valueFontStyleList = new JComboBox<>(keys(StatGraphProperties.getFontStyleMap()));

    private JComboBox<String> fontNameList = new JComboBox<>(keys(StatGraphProperties.getFontNameMap()));

    private JComboBox<String> fontSizeList = new JComboBox<>(StatGraphProperties.getFontSize());

    private JComboBox<String> fontStyleList = new JComboBox<>(keys(StatGraphProperties.getFontStyleMap()));

    private JComboBox<String> legendPlacementList = new JComboBox<>(keys(StatGraphProperties.getPlacementNameMap()));

    // Default checked
    private JCheckBox drawOutlinesBar = new JCheckBox(JMeterUtils.getResString("aggregate_graph_draw_outlines"), true); // $NON-NLS-1$

    // Default checked
    private JCheckBox numberShowGrouping = new JCheckBox(JMeterUtils.getResString("aggregate_graph_number_grouping"), true); // $NON-NLS-1$

    // Default checked
    private JCheckBox valueLabelsVertical = new JCheckBox(JMeterUtils.getResString("aggregate_graph_value_labels_vertical"), true); // $NON-NLS-1$

    private Color colorBarGraph = Color.YELLOW;

    private Color colorForeGraph = Color.BLACK;
    
    private int nbColToGraph = 1;

    private Pattern pattern = null;

    private Deque<SamplingStatCalculator> newRows = new ConcurrentLinkedDeque<>();

    public StatGraphVisualizer() {
        super();
        model = createObjectTableModel();
        final Color red = new Color(202, 0, 0);
        final Color blue = new Color(49, 49, 181);
        final Color green = new Color(42, 121, 42);
        final Color yellow = new Color(242, 226, 8);
        final Color purple = new Color(202, 10, 232);
        eltList.add(new BarGraph(JMeterUtils.getResString("average"), true,
                red));
        eltList.add(new BarGraph(
                JMeterUtils.getResString("aggregate_report_median"), false,
                blue));
        eltList.add(
                new BarGraph(
                        MessageFormat.format(
                                JMeterUtils.getResString(
                                        "aggregate_report_xx_pct1_line"),
                                PCT1_LABEL),
                        false, green));
        eltList.add(
                new BarGraph(
                        MessageFormat.format(
                                JMeterUtils.getResString(
                                        "aggregate_report_xx_pct2_line"),
                                PCT2_LABEL),
                        false, yellow));
        
        eltList.add(
                new BarGraph(
                        MessageFormat.format(
                                JMeterUtils.getResString(
                                        "aggregate_report_xx_pct3_line"),
                                PCT3_LABEL),
                        false, purple));
        eltList.add(
                new BarGraph(JMeterUtils.getResString("aggregate_report_min"),
                        false, Color.LIGHT_GRAY));
        eltList.add(
                new BarGraph(JMeterUtils.getResString("aggregate_report_max"),
                        false, Color.DARK_GRAY));
        clearData();
        init();
    }
    
    static final Object[][] getColumnsMsgParameters() { 
        return new Object[][] { null, 
            null,
            null,
            null,
            new Object[]{PCT1_LABEL},
            new Object[]{PCT2_LABEL},
            new Object[]{PCT3_LABEL},
            null,
            null,
            null,
            null,
            null,
            null};
    }

    private String[] keys(Map<String, ?> map) {
        return map.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * @return array of String containing column names
     */
    public static final String[] getColumns() {
        String[] columns = new String[COLUMNS.length];
        System.arraycopy(COLUMNS, 0, columns, 0, COLUMNS.length);
        return columns;
    }

    /**
     * Creates that Table model 
     * @return ObjectTableModel
     */
    static ObjectTableModel createObjectTableModel() {
        return new ObjectTableModel(getLabels(COLUMNS),
                SamplingStatCalculator.class,
                new Functor[] {
                new Functor("getLabel"),                    //$NON-NLS-1$
                new Functor("getCount"),                    //$NON-NLS-1$
                new Functor("getMeanAsNumber"),                //$NON-NLS-1$
                new Functor("getMedian"),                    //$NON-NLS-1$
                new Functor("getPercentPoint",                //$NON-NLS-1$
                        new Object[] { PCT1_VALUE }),
                new Functor("getPercentPoint",                //$NON-NLS-1$
                        new Object[] { PCT2_VALUE }),
                new Functor("getPercentPoint",                //$NON-NLS-1$
                        new Object[] { PCT3_VALUE }),
                new Functor("getMin"),                        //$NON-NLS-1$
                new Functor("getMax"),                         //$NON-NLS-1$
                new Functor("getErrorPercentage"),            //$NON-NLS-1$
                new Functor("getRate"),                        //$NON-NLS-1$
                new Functor("getKBPerSecond"),                 //$NON-NLS-1$
                new Functor("getSentKBPerSecond") },            //$NON-NLS-1$
                new Functor[] { null, null, null, null, null, null, null, null, null, null, null, null, null },
                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class, 
                            Long.class, Long.class, Long.class, Long.class, Double.class,
                            Double.class, Double.class, Double.class});
    }

    // Column formats
    static final Format[] getFormatters() {
        return new Format[]{
            null, // Label
            null, // count
            null, // Mean
            null, // median
            null, // 90%
            null, // 95%
            null, // 99%
            null, // Min
            null, // Max
            new DecimalFormat("#0.000%"), // Error %age //$NON-NLS-1$
            new DecimalFormat("#.00000"),      // Throughput //$NON-NLS-1$
            new DecimalFormat("#0.00"),      // Throughput //$NON-NLS-1$
            new DecimalFormat("#0.00")    // pageSize   //$NON-NLS-1$
        };
    }
    
    // Column renderers
    static final TableCellRenderer[] getRenderers() {
        return new TableCellRenderer[]{
            null, // Label
            null, // count
            null, // Mean
            null, // median
            null, // 90%
            null, // 95%
            null, // 99%
            null, // Min
            null, // Max
            new NumberRenderer("#0.00%"), // Error %age //$NON-NLS-1$
            new RateRenderer("#.0"),      // Throughput //$NON-NLS-1$
            new NumberRenderer("#0.00"),      // Received bytes per sec //$NON-NLS-1$
            new NumberRenderer("#0.00"),    // Sent bytes per sec   //$NON-NLS-1$
        };
    }
    
    /**
     * 
     * @param keys I18N keys
     * @return labels
     */
    static String[] getLabels(String[] keys) {
        String[] labels = new String[keys.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i]=MessageFormat.format(JMeterUtils.getResString(keys[i]), getColumnsMsgParameters()[i]);
        }
        return labels;
    }
    
    /**
     * We use this method to get the data, since we are using
     * ObjectTableModel, so the calling getDataVector doesn't
     * work as expected.
     * @param model {@link ObjectTableModel}
     * @param formats Array of {@link Format} array can contain null formatters in this case value is added as is
     * @return the data from the model
     */
    public static List<List<Object>> getAllTableData(ObjectTableModel model, Format[] formats) {
        List<List<Object>> data = new ArrayList<>();
        if (model.getRowCount() > 0) {
            for (int rw=0; rw < model.getRowCount(); rw++) {
                int cols = model.getColumnCount();
                List<Object> column = new ArrayList<>();
                data.add(column);
                for (int idx=0; idx < cols; idx++) {
                    Object val = model.getValueAt(rw,idx);
                    if(formats[idx] != null) {
                        column.add(formats[idx].format(val));
                    } else {
                        column.add(val);
                    }
                }
            }
        }
        return data;
    }

    public static boolean testFunctors(){
        StatGraphVisualizer instance = new StatGraphVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }

    @Override
    public String getLabelResource() {
        return "aggregate_graph_title";                        //$NON-NLS-1$
    }

    @Override
    public void add(final SampleResult res) {
        final String sampleLabel = res.getSampleLabel();
        // Sampler selection
        Matcher matcher = null;
        if (columnSelection.isSelected() && pattern != null) {
            matcher = pattern.matcher(sampleLabel);
        }
        if ((matcher == null) || (matcher.find())) {
            SamplingStatCalculator row = tableRows.computeIfAbsent(sampleLabel, label -> {
                SamplingStatCalculator newRow = new SamplingStatCalculator(label);
                newRows.addLast(newRow);
                return newRow;
            });
            synchronized (row) {
                row.addSample(res);
            }
            synchronized (lock) {
                tableRows.get(TOTAL_ROW_LABEL).addSample(res);
            }
        }
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
        Border margin2 = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.setRowSorter(new ObjectTableSorter(model).fixLastRow());
        JMeterUtils.applyHiDPI(myJTable);
        // Fix centering of titles
        HeaderAsPropertyRendererWrapper.setupDefaultRenderer(myJTable);
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, getRenderers());
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

        // If clic on the Graph tab, make the graph (without apply interval or filter)
        tabbedGraph.addChangeListener(changeEvent -> {
            JTabbedPane srcTab = (JTabbedPane) changeEvent.getSource();
            int index = srcTab.getSelectedIndex();
            if (srcTab.getTitleAt(index).equals(JMeterUtils.getResString("aggregate_graph_tab_graph"))) { //$NON-NLS-1$
                actionMakeGraph();
            }
        });

        spane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spane.setOneTouchExpandable(true);
        spane.setLeftComponent(myScrollPane);
        spane.setRightComponent(tabbedGraph);
        spane.setResizeWeight(.2);
        spane.setBorder(null); // see bug jdk 4131528
        spane.setContinuousLayout(true);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(spane, BorderLayout.CENTER);
        new Timer(REFRESH_PERIOD, e -> {
                synchronized (lock) {
                    while (!newRows.isEmpty()) {
                        model.insertRow(newRows.pop(), model.getRowCount() - 1);
                    }
                }
                model.fireTableDataChanged();
        }).start();
    }

    public void makeGraph() {
        nbColToGraph = getNbColumns();
        Dimension size = graphPanel.getSize();
        String lstr = maxLengthXAxisLabel.getText();
        // canvas size
        int width = (int) size.getWidth();
        int height = (int) size.getHeight();
        if (!dynamicGraphSize.isSelected()) {
            String wstr = graphWidth.getText();
            String hstr = graphHeight.getText();
            if (wstr.length() != 0) {
                width = Integer.parseInt(wstr);
            }
            if (hstr.length() != 0) {
                height = Integer.parseInt(hstr);
            }
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
        graphPanel.setXAxisTitle(JMeterUtils.getResString((String) columnsList.getSelectedItem()));
        graphPanel.setYAxisLabels(this.yAxisLabel);
        graphPanel.setYAxisTitle(this.yAxisTitle);
        graphPanel.setLegendLabels(getLegendLabels());
        graphPanel.setColor(getBackColors());
        graphPanel.setForeColor(colorForeGraph);
        graphPanel.setOutlinesBarFlag(drawOutlinesBar.isSelected());
        graphPanel.setShowGrouping(numberShowGrouping.isSelected());
        graphPanel.setValueOrientation(valueLabelsVertical.isSelected());
        graphPanel.setLegendPlacement(StatGraphProperties.getPlacementNameMap()
                .get(legendPlacementList.getSelectedItem()).intValue());

        graphPanel.setTitleFont(new Font(StatGraphProperties.getFontNameMap().get(titleFontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(titleFontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) titleFontSizeList.getSelectedItem())));
        graphPanel.setLegendFont(new Font(StatGraphProperties.getFontNameMap().get(fontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(fontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) fontSizeList.getSelectedItem())));
        graphPanel.setValueFont(new Font(StatGraphProperties.getFontNameMap().get(valueFontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(valueFontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) valueFontSizeList.getSelectedItem())));

        graphPanel.setHeight(height);
        graphPanel.setWidth(width);
        spane.repaint();
    }

    public double[][] getData() {
        if (model.getRowCount() > 1) {
            int count = model.getRowCount() -1;
            
            int size = nbColToGraph;
            double[][] data = new double[size][count];
            int s = 0;
            int cpt = 0;
            for (BarGraph bar : eltList) {
                if (bar.getChkBox().isSelected()) {
                    int col = model.findColumn(columnsList.getItemAt(cpt));
                    for (int idx=0; idx < count; idx++) {
                        data[s][idx] = ((Number)model.getValueAt(idx,col)).doubleValue();
                    }
                    s++;
                }
                cpt++;
            }
            return data;
        }
        // API expects null, not empty array
        return null;
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
        // API expects null, not empty array
        return null;
    }

    private String[] getLegendLabels() {
        String[] legends = new String[nbColToGraph];
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                legends[i] = bar.getLabel();
                i++;
            }
        }
        return legends;
    }

    private Color[] getBackColors() {
        Color[] backColors = new Color[nbColToGraph];
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                backColors[i] = bar.getBackColor();
                i++;
            }
        }
        return backColors;
    }

    private int getNbColumns() {
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                i++;
            }
        }
        return i;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        boolean forceReloadData = false;
        final Object eventSource = event.getSource();
        if (eventSource == displayButton) {
            actionMakeGraph();
        } else if (eventSource == saveGraph) {
            saveGraphToFile = true;
            try {
                ActionRouter.getInstance().getAction(
                        ActionNames.SAVE_GRAPHICS,SaveGraphics.class.getName()).doAction(
                                new ActionEvent(this,event.getID(),ActionNames.SAVE_GRAPHICS));
            } catch (Exception e) {
                log.error("Error saving to file", e);
            }
        } else if (eventSource == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("statistics.csv");    //$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            try (FileOutputStream fo = new FileOutputStream(chooser.getSelectedFile()); 
                    OutputStreamWriter writer = new OutputStreamWriter(fo, Charset.forName("UTF-8"))){ 
                CSVSaveService.saveCSVStats(getAllTableData(model, getFormatters()),
                        writer,
                        saveHeaders.isSelected() ? getLabels(COLUMNS) : null);
            } catch (IOException e) { // NOSONAR Error is reported in GUI
                JMeterUtils.reportErrorToUser(e.getMessage(), "Error saving data");
            } 
        } else if (eventSource == chooseForeColor) {
            Color color = JColorChooser.showDialog(
                    null,
                    JMeterUtils.getResString("aggregate_graph_choose_color"), //$NON-NLS-1$
                    colorBarGraph);
            if (color != null) {
                colorForeGraph = color;
            }
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
                applyFilterBtn.setEnabled(true);
                caseChkBox.setEnabled(true);
                regexpChkBox.setEnabled(true);
            } else {
                columnMatchLabel.setEnabled(false);
                applyFilterBtn.setEnabled(false);
                caseChkBox.setEnabled(false);
                regexpChkBox.setEnabled(false);
                // Force reload data
                forceReloadData = true;
            }
        }
        // Not 'else if' because forceReloadData 
        if (eventSource == applyFilterBtn || forceReloadData) {
            if (columnSelection.isSelected() && columnMatchLabel.getText() != null
                    && columnMatchLabel.getText().length() > 0) {
                pattern = createPattern(columnMatchLabel.getText());
            } else if (forceReloadData) {
                pattern = null;
            }
            if (getFile() != null && getFile().length() > 0) {
                clearData();
                FilePanel filePanel = (FilePanel) getFilePanel();
                filePanel.actionPerformed(event);
            }
        } else if (eventSource instanceof JButton) {
            // Changing color for column
            JButton btn = (JButton) eventSource;
            if (btn.getName() != null) {
                try {
                    BarGraph bar = eltList.get(Integer.parseInt(btn.getName()));
                    Color color = JColorChooser.showDialog(null, bar.getLabel(), bar.getBackColor());
                    if (color != null) {
                        bar.setBackColor(color);
                        btn.setBackground(bar.getBackColor());
                    }
                } catch (NumberFormatException nfe) { 
                    // nothing to do
                } 
            }
        }
    }

    private void actionMakeGraph() {
        if (model.getRowCount() > 1) {
            makeGraph();
            tabbedGraph.setSelectedIndex(1);
        } else {
            JOptionPane.showMessageDialog(null, JMeterUtils
                    .getResString("aggregate_graph_no_values_to_graph"), // $NON-NLS-1$
                    JMeterUtils.getResString("aggregate_graph_no_values_to_graph"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    @Override
    public JComponent getPrintableComponent() {
        if (saveGraphToFile) {
            saveGraphToFile = false;
            
            // (re)draw the graph first to take settings into account (Bug 58329)
            if (model.getRowCount() > 1) {
                makeGraph();
            }
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
        JPanel colPanel = new JPanel();
        colPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JLabel label = new JLabel(JMeterUtils.getResString("aggregate_graph_columns_to_display")); //$NON-NLS-1$
        colPanel.add(label);
        for (BarGraph bar : eltList) {
            colPanel.add(bar.getChkBox());
            colPanel.add(createColorBarButton(bar, eltList.indexOf(bar)));
        }
        colPanel.add(Box.createRigidArea(new Dimension(5,0)));
        chooseForeColor.setFont(FONT_SMALL);
        colPanel.add(chooseForeColor);
        chooseForeColor.addActionListener(this);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        optionsPanel.add(createGraphFontValuePane());
        optionsPanel.add(drawOutlinesBar);
        optionsPanel.add(numberShowGrouping);
        optionsPanel.add(valueLabelsVertical);
        
        JPanel barPane = new JPanel(new BorderLayout());
        barPane.add(colPanel, BorderLayout.NORTH);
        barPane.add(Box.createRigidArea(new Dimension(0,3)), BorderLayout.CENTER);
        barPane.add(optionsPanel, BorderLayout.SOUTH);

        JPanel columnPane = new JPanel(new BorderLayout());
        columnPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_column_settings"))); // $NON-NLS-1$
        columnPane.add(barPane, BorderLayout.NORTH);
        columnPane.add(Box.createRigidArea(new Dimension(0,3)), BorderLayout.CENTER);
        columnPane.add(createGraphSelectionSubPane(), BorderLayout.SOUTH);
        
        return columnPane;
    }

    private JButton createColorBarButton(BarGraph barGraph, int index) {
        // Button
        JButton colorBtn = new JButton();
        colorBtn.setName(String.valueOf(index));
        colorBtn.setFont(FONT_SMALL);
        colorBtn.addActionListener(this);
        colorBtn.setBackground(barGraph.getBackColor());
        return colorBtn;
    }

    private JPanel createGraphSelectionSubPane() {
        // Search field
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        searchPanel.add(columnSelection);
        columnMatchLabel.setEnabled(false);
        applyFilterBtn.setEnabled(false);
        caseChkBox.setEnabled(false);
        regexpChkBox.setEnabled(false);
        columnSelection.addActionListener(this);

        searchPanel.add(columnMatchLabel);
        searchPanel.add(Box.createRigidArea(new Dimension(5,0)));

        // Button
        applyFilterBtn.setFont(FONT_SMALL);
        applyFilterBtn.addActionListener(this);
        searchPanel.add(applyFilterBtn);

        // checkboxes
        caseChkBox.setFont(FONT_SMALL);
        searchPanel.add(caseChkBox);
        regexpChkBox.setFont(FONT_SMALL);
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
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                titleFontNameList));
        titleFontNameList.setSelectedIndex(0); // default: sans serif
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                titleFontSizeList));
        titleFontSizeList.setSelectedItem(StatGraphProperties.getFontSize()[6]); // default: 16
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                titleFontStyleList));
        titleFontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.bold"));  // $NON-NLS-1$ // default: bold

        JPanel titlePane = new JPanel(new BorderLayout());
        titlePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_title_group"))); // $NON-NLS-1$
        titlePane.add(titleNamePane, BorderLayout.NORTH);
        titlePane.add(titleStylePane, BorderLayout.SOUTH);
        return titlePane;
    }

    private JPanel createGraphFontValuePane() {       
        JPanel fontValueStylePane = new JPanel();
        fontValueStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fontValueStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_value_font"), //$NON-NLS-1$
                valueFontNameList));
        valueFontNameList.setSelectedIndex(0); // default: sans serif
        fontValueStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                valueFontSizeList));
        valueFontSizeList.setSelectedItem(StatGraphProperties.getFontSize()[2]); // default: 10
        fontValueStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                valueFontStyleList));
        valueFontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.normal")); // default: normal //$NON-NLS-1$

        return fontValueStylePane;
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

        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_legend_placement"), //$NON-NLS-1$
                legendPlacementList));
        legendPlacementList.setSelectedItem(JMeterUtils.getResString("aggregate_graph_legend.placement.bottom"));  // $NON-NLS-1$ // default: bottom
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                fontNameList));
        fontNameList.setSelectedIndex(0); // default: sans serif
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                fontSizeList));
        fontSizeList.setSelectedItem(StatGraphProperties.getFontSize()[2]); // default: 10
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                fontStyleList));
        fontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.normal"));  // $NON-NLS-1$ // default: normal

        return legendPanel;
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
        Pattern result = null;
        try {
            if (caseChkBox.isSelected()) {
                result = Pattern.compile(textToFindQ);
            } else {
                result = Pattern.compile(textToFindQ, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException pse) {
            return null;
        }
        return result;
    }
}
