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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.SaveGraphics;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jmeter.visualizers.utils.Colors;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.math.StatCalculatorLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RespTimeGraphVisualizer extends AbstractVisualizer implements ActionListener, Clearable {

    private static final long serialVersionUID = 281L;

    private static final Logger log = LoggerFactory.getLogger(RespTimeGraphVisualizer.class);

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font"); //$NON-NLS-1$

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8)); //$NON-NLS-1$

    //+ JMX property names; do not change

    public static final String INTERVAL = "RespTimeGraph.interval"; // $NON-NLS-1$

    public static final String SERIES_SELECTION = "RespTimeGraph.seriesselection"; // $NON-NLS-1$

    public static final String SERIES_SELECTION_MATCH_LABEL = "RespTimeGraph.seriesselectionmatchlabel"; // $NON-NLS-1$

    public static final String SERIES_SELECTION_CASE_SENSITIVE = "RespTimeGraph.seriesselectioncasesensitive"; // $NON-NLS-1$

    public static final String SERIES_SELECTION_REGEXP = "RespTimeGraph.seriesselectionregexp"; // $NON-NLS-1$
    
    public static final String GRAPH_TITLE = "RespTimeGraph.graphtitle"; // $NON-NLS-1$

    public static final String GRAPH_TITLE_FONT_NAME = "RespTimeGraph.graphtitlefontname"; // $NON-NLS-1$

    public static final String GRAPH_TITLE_FONT_SIZE = "RespTimeGraph.graphtitlefondsize"; // $NON-NLS-1$

    public static final String GRAPH_TITLE_FONT_STYLE = "RespTimeGraph.graphtitlefontstyle"; // $NON-NLS-1$

    public static final String LINE_STROKE_WIDTH = "RespTimeGraph.linestrockwidth"; // $NON-NLS-1$

    public static final String LINE_SHAPE_POINT = "RespTimeGraph.lineshapepoint"; // $NON-NLS-1$

    public static final String GRAPH_SIZE_DYNAMIC = "RespTimeGraph.graphsizedynamic"; // $NON-NLS-1$

    public static final String GRAPH_SIZE_WIDTH = "RespTimeGraph.graphsizewidth"; // $NON-NLS-1$

    public static final String GRAPH_SIZE_HEIGHT = "RespTimeGraph.graphsizeheight"; // $NON-NLS-1$

    public static final String XAXIS_TIME_FORMAT = "RespTimeGraph.xaxistimeformat"; // $NON-NLS-1$

    public static final String YAXIS_SCALE_MAX_VALUE = "RespTimeGraph.yaxisscalemaxvalue"; // $NON-NLS-1$

    public static final String YAXIS_INCREMENT_SCALE = "RespTimeGraph.yaxisscaleincrement"; // $NON-NLS-1$

    public static final String YAXIS_NUMBER_GROUPING = "RespTimeGraph.yaxisnumbergrouping"; // $NON-NLS-1$

    public static final String LEGEND_PLACEMENT = "RespTimeGraph.legendplacement"; // $NON-NLS-1$

    public static final String LEGEND_FONT = "RespTimeGraph.legendfont"; // $NON-NLS-1$

    public static final String LEGEND_SIZE = "RespTimeGraph.legendsize"; // $NON-NLS-1$

    public static final String LEGEND_STYLE = "RespTimeGraph.legendstyle"; // $NON-NLS-1$

    //- JMX property names

    public static final int DEFAULT_INTERVAL = 10000; // in milli-seconds // TODO: properties?

    public static final boolean DEFAULT_SERIES_SELECTION = false;
    
    public static final boolean DEFAULT_CASE_SENSITIVE = false;
    
    public static final boolean DEFAULT_REGEXP = true;
    
    public static final int DEFAULT_TITLE_FONT_NAME = 0; // default: sans serif
    
    public static final int DEFAULT_TITLE_FONT_SIZE = 6; // default: 16

    public static final int DEFAULT_TITLE_FONT_STYLE = 1; // default: bold

    public static final int DEFAULT_STROKE_WIDTH_LIST = 4; // default: 3.0f
    
    public static final int DEFAULT_LINE_SHAPE_POINT = 0; // default: circle

    public static final boolean DEFAULT_DYNAMIC_GRAPH_SIZE = true; // default: true

    public static final String DEFAULT_XAXIS_TIME_FORMAT = "HH:mm:ss"; // $NON-NLS-1$
    
    public static final boolean DEFAULT_NUMBER_SHOW_GROUPING = true;
    
    public static final int DEFAULT_LEGEND_PLACEMENT = 0; // default: bottom

    public static final int DEFAULT_LEGEND_FONT = 0; // default: sans serif
    
    public static final int DEFAULT_LEGEND_SIZE = 2; // default: 10

    public static final int DEFAULT_LEGEND_STYLE = 0; // default: normal

    private static final int DEFAULT_WIDTH = 400;

    private static final int DEFAULT_HEIGTH = 300;

    private static final String Y_AXIS_LABEL = JMeterUtils.getResString("aggregate_graph_response_time");//$NON-NLS-1$

    private static final String Y_AXIS_TITLE = JMeterUtils.getResString("aggregate_graph_ms"); //$NON-NLS-1$

    /**
     * Lock used to protect list update
     */
    private final transient Object lock = new Object();
    /**
     * Lock used to protect refresh interval
     */
    private final transient Object lockInterval = new Object();

    private RespTimeGraphChart graphPanel = null;

    private final JTabbedPane tabbedGraph = new JTabbedPane(SwingConstants.TOP);
    
    private boolean saveGraphToFile = false;
    
    private int intervalValue = DEFAULT_INTERVAL;

    private final JLabeledTextField intervalField =
            new JLabeledTextField(JMeterUtils.getResString("graph_resp_time_interval_label"), 7); //$NON-NLS-1$

    private final JButton intervalButton = new JButton(JMeterUtils.getResString("graph_resp_time_interval_reload")); // $NON-NLS-1$

    private final JButton displayButton =
            new JButton(JMeterUtils.getResString("aggregate_graph_display")); //$NON-NLS-1$
    
    private final JButton saveGraph =
            new JButton(JMeterUtils.getResString("aggregate_graph_save")); //$NON-NLS-1$

    private final JCheckBox samplerSelection = new JCheckBox(JMeterUtils.getResString("graph_resp_time_series_selection"), false); //$NON-NLS-1$

    private final JTextField samplerMatchLabel = new JTextField();

    private final JButton applyFilterBtn = new JButton(JMeterUtils.getResString("graph_apply_filter")); // $NON-NLS-1$

    private final JCheckBox caseChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false); // $NON-NLS-1$

    private final JCheckBox regexpChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), true); // $NON-NLS-1$

    private final JComboBox<String> titleFontNameList = new JComboBox<>(StatGraphProperties.getFontNameMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

    private final JComboBox<String> titleFontSizeList = new JComboBox<>(StatGraphProperties.getFontSize());

    private final JComboBox<String> titleFontStyleList = new JComboBox<>(StatGraphProperties.getFontStyleMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

    private final JComboBox<String> fontNameList = new JComboBox<>(StatGraphProperties.getFontNameMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

    private final JComboBox<String> fontSizeList = new JComboBox<>(StatGraphProperties.getFontSize());

    private final JComboBox<String> fontStyleList = new JComboBox<>(StatGraphProperties.getFontStyleMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

    private final JComboBox<String> legendPlacementList = new JComboBox<>(StatGraphProperties.getPlacementNameMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    
    private final JComboBox<String> pointShapeLine = new JComboBox<>(StatGraphProperties.getPointShapeMap().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

    private final JComboBox<String> strokeWidthList = new JComboBox<>(StatGraphProperties.getStrokeWidth());

    private final JCheckBox numberShowGrouping = new JCheckBox(JMeterUtils.getResString("aggregate_graph_number_grouping"), // $NON-NLS-1$
            DEFAULT_NUMBER_SHOW_GROUPING); // Default checked

    private final JButton syncWithName =
            new JButton(JMeterUtils.getResString("aggregate_graph_sync_with_name"));  //$NON-NLS-1$

    private final JLabeledTextField graphTitle =
            new JLabeledTextField(JMeterUtils.getResString("graph_resp_time_title_label")); //$NON-NLS-1$

    private final JLabeledTextField xAxisTimeFormat =
            new JLabeledTextField(JMeterUtils.getResString("graph_resp_time_xaxis_time_format"), 10); //$NON-NLS-1$ $NON-NLS-2$

    private final JLabeledTextField maxValueYAxisLabel =
            new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_yaxis_max_value"), 5); //$NON-NLS-1$

    /**
     * checkbox for use dynamic graph size
     */
    private final JCheckBox dynamicGraphSize = new JCheckBox(JMeterUtils.getResString("aggregate_graph_dynamic_size")); // $NON-NLS-1$

    private final JLabeledTextField graphWidth =
            new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_width"), 6); //$NON-NLS-1$
    private final JLabeledTextField graphHeight =
            new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_height"), 6); //$NON-NLS-1$

    private final JLabeledTextField incrScaleYAxis =
            new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_increment_scale"), 5); //$NON-NLS-1$

    private long minStartTime = Long.MAX_VALUE;

    private long maxStartTime = Long.MIN_VALUE;

    /**
     * We want to retain insertion order, so LinkedHashMap is necessary
     */
    private final Map<String, RespTimeGraphLineBean> seriesNames = new LinkedHashMap<>();

    /**
     * We want to retain insertion order, so LinkedHashMap is necessary
     */
    private final Map<String, Map<Long, StatCalculatorLong>> pList = new LinkedHashMap<>();

    private long durationTest = 0;
    
    private int colorIdx = 0;

    private Pattern pattern = null;

    private final List<Color> listColors = Colors.getColors();

    private final List<RespTimeGraphDataBean> internalList = new ArrayList<>(); // internal list of all results

    public RespTimeGraphVisualizer() {
        init();
    }

    @Override
    public void add(final SampleResult sampleResult) {
        final String sampleLabel = sampleResult.getSampleLabel();
        // Make a internal list of all results to allow reload data with filter or interval
        synchronized (lockInterval) {
            internalList.add(new RespTimeGraphDataBean(sampleResult.getStartTime(), sampleResult.getTime(), sampleLabel));
        }

        Matcher matcher = null;

        // Sampler selection
        if (samplerSelection.isSelected() && pattern != null) {
            matcher = pattern.matcher(sampleLabel);
        }
        if ((matcher == null) || (matcher.find())) {
            final long startTimeMS = sampleResult.getStartTime();
            final long startTimeInterval = startTimeMS / intervalValue;
            JMeterUtils.runSafe(false, new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        // Use for x-axis scale
                        if (startTimeInterval < minStartTime) {
                            minStartTime = startTimeInterval;
                        } else if (startTimeInterval > maxStartTime) {
                            maxStartTime = startTimeInterval;
                        }
                        // Generate x-axis label and associated color
                        if (!seriesNames.containsKey(sampleLabel)) {
                            seriesNames.put(sampleLabel, 
                                    new RespTimeGraphLineBean(sampleLabel, listColors.get(colorIdx++)));
                            // reset colors index
                            if (colorIdx >= listColors.size()) {
                                colorIdx = 0;
                            }
                        }
                        // List of value by sampler
                        Map<Long, StatCalculatorLong> subList = pList.get(sampleLabel);
                        final Long startTimeIntervalLong = Long.valueOf(startTimeInterval);
                        if (subList != null) {
                            long respTime = sampleResult.getTime();
                            StatCalculatorLong value = subList.get(startTimeIntervalLong);
                            if (value==null) {
                                value = new StatCalculatorLong();
                                subList.put(startTimeIntervalLong, value);
                            }
                            value.addValue(respTime, 1);
                        } else {
                            // We want to retain insertion order, so LinkedHashMap is necessary
                            Map<Long, StatCalculatorLong> newSubList = new LinkedHashMap<>(5);
                            StatCalculatorLong helper = new StatCalculatorLong();
                            helper.addValue(Long.valueOf(sampleResult.getTime()),1);
                            newSubList.put(startTimeIntervalLong,  helper);
                            pList.put(sampleLabel, newSubList);
                        }
                    }
                }
            });
        }
    }

    public void makeGraph() {
        Dimension size = graphPanel.getSize();
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

        String yAxisStr = maxValueYAxisLabel.getText();
        int maxYAxisScale = yAxisStr.length() == 0 ? 0 : Integer.parseInt(yAxisStr);

        graphPanel.setData(this.getData());
        graphPanel.setTitle(graphTitle.getText());
        graphPanel.setMaxYAxisScale(maxYAxisScale);

        graphPanel.setYAxisLabels(Y_AXIS_LABEL);
        graphPanel.setYAxisTitle(Y_AXIS_TITLE);
        graphPanel.setXAxisLabels(getXAxisLabels());
        graphPanel.setLegendLabels(getLegendLabels());
        graphPanel.setColor(getLinesColors());
        graphPanel.setShowGrouping(numberShowGrouping.isSelected());
        graphPanel.setLegendPlacement(StatGraphProperties.getPlacementNameMap()
                .get(legendPlacementList.getSelectedItem()).intValue());
        graphPanel.setPointShape(StatGraphProperties.getPointShapeMap().get(pointShapeLine.getSelectedItem()));
        graphPanel.setStrokeWidth(Float.parseFloat((String) strokeWidthList.getSelectedItem()));

        graphPanel.setTitleFont(new Font(StatGraphProperties.getFontNameMap().get(titleFontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(titleFontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) titleFontSizeList.getSelectedItem())));
        graphPanel.setLegendFont(new Font(StatGraphProperties.getFontNameMap().get(fontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(fontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) fontSizeList.getSelectedItem())));

        graphPanel.setHeight(height);
        graphPanel.setWidth(width);
        graphPanel.setIncrYAxisScale(getIncrScaleYAxis());
        // Draw the graph
        graphPanel.repaint();
    }

    /**
     * Generate the data for the jChart API
     * @return array of array of data to draw
     */
    public double[][] getData() {
        int size = pList.size();
        int max = (int) durationTest; // Test can't have a duration more than 2^31 secs (cast from long to int)

        double[][] data = new double[size][max];

        double nanLast = 0;
        double nanBegin = 0;
        List<Double> nanList = new ArrayList<>();
        int s = 0;
        for (Map<Long, StatCalculatorLong> subList : pList.values()) {
            int idx = 0;
            while (idx < durationTest) {
                long keyShift = minStartTime + idx;
                StatCalculatorLong value = subList.get(Long.valueOf(keyShift));
                if (value != null) {
                    nanLast = value.getMean();
                    data[s][idx] = nanLast;
                    // Calculate intermediate values (if needed)
                    int nlsize = nanList.size();
                    if (nlsize > 0) {
                        double valPrev = nanBegin;
                        for (int cnt = 0; cnt < nlsize; cnt++) {
                            int pos = idx - (nlsize - cnt);
                            if (pos < 0) { pos = 0; }
                            valPrev = valPrev + ((nanLast - nanBegin) / (nlsize + 2));
                            data[s][pos] = valPrev;
                        }
                        nanList.clear();
                    }
                } else {
                    nanList.add(Double.valueOf(Double.NaN));
                    nanBegin = nanLast;
                    data[s][idx] = Double.NaN;
                }
                idx++;
            }
            s++;
        }
        return data;
    }

    @Override
    public String getLabelResource() {
        return "graph_resp_time_title"; // $NON-NLS-1$
    }

    @Override
    public void clearData() {
        synchronized (lock) {
            internalList.clear();
            seriesNames.clear();
            pList.clear();
            minStartTime = Long.MAX_VALUE;
            maxStartTime = Long.MIN_VALUE;
            durationTest = 0;
            colorIdx = 0;
        }
        tabbedGraph.setSelectedIndex(0);
    }

    /**
     * Initialize the GUI.
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

        JPanel settingsPane = new VerticalPanel();
        settingsPane.setBorder(margin2);

        graphPanel = new RespTimeGraphChart();
        graphPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGTH));

        settingsPane.add(createGraphActionsPane());
        settingsPane.add(createGraphSettingsPane());
        settingsPane.add(createGraphTitlePane());
        settingsPane.add(createLinePane());
        settingsPane.add(createGraphDimensionPane());
        JPanel axisPane = new JPanel(new BorderLayout());
        axisPane.add(createGraphXAxisPane(), BorderLayout.WEST);
        axisPane.add(createGraphYAxisPane(), BorderLayout.CENTER);
        settingsPane.add(axisPane);
        settingsPane.add(createLegendPane());

        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_settings"), settingsPane); //$NON-NLS-1$
        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_graph"), graphPanel); //$NON-NLS-1$
        
        // If clic on the Graph tab, make the graph (without apply interval or filter)
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane srcTab = (JTabbedPane) changeEvent.getSource();
                int index = srcTab.getSelectedIndex();
                if (srcTab.getTitleAt(index).equals(JMeterUtils.getResString("aggregate_graph_tab_graph"))) { //$NON-NLS-1$
                    actionMakeGraph();
                }
            }
        };
        tabbedGraph.addChangeListener(changeListener);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(tabbedGraph, BorderLayout.CENTER);

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
                log.error(e.getMessage());
            }
        } else if (eventSource == syncWithName) {
            graphTitle.setText(namePanel.getName());
        } else if (eventSource == dynamicGraphSize) {
                enableDynamicGraph(dynamicGraphSize.isSelected());
        } else if (eventSource == samplerSelection) {
            enableSamplerSelection(samplerSelection.isSelected());
            if (!samplerSelection.isSelected()) {
                // Force reload data
                forceReloadData = true;
            }
        }
        // Not 'else if' because forceReloadData 
        if (eventSource == applyFilterBtn || eventSource == intervalButton || forceReloadData) {
            if (eventSource == intervalButton) {
                intervalValue = Integer.parseInt(intervalField.getText());
            }
            if (eventSource == applyFilterBtn && samplerSelection.isSelected() && samplerMatchLabel.getText() != null
                    && samplerMatchLabel.getText().length() > 0) {
                pattern = createPattern(samplerMatchLabel.getText());
            } else if (forceReloadData) {
                pattern = null;
            }
            if (getFile() != null && getFile().length() > 0) {
                // Reload data from file
                clearData();
                FilePanel filePanel = (FilePanel) getFilePanel();
                filePanel.actionPerformed(event);
            } else {
                // Reload data form internal list of results
                synchronized (lockInterval) {
                    if (internalList.size() >= 2) {
                        List<RespTimeGraphDataBean> tempList = new ArrayList<>();
                        tempList.addAll(internalList);
                        this.clearData();
                        for (RespTimeGraphDataBean data : tempList) {
                            SampleResult sr = new SampleResult(data.getStartTime(), data.getTime());
                            sr.setSampleLabel(data.getSamplerLabel());
                            this.add(sr);
                        }
                    }
                }
            }
        } 
    }

    private void actionMakeGraph() {
        String msgErr = null;
        // Calculate the test duration. Needs to xAxis Labels and getData.
        durationTest = maxStartTime - minStartTime;
        if (seriesNames.size() <= 0) {
            msgErr = JMeterUtils.getResString("aggregate_graph_no_values_to_graph"); // $NON-NLS-1$
        } else   if (durationTest < 1) {
            msgErr = JMeterUtils.getResString("graph_resp_time_not_enough_data"); // $NON-NLS-1$
        }
        if (msgErr == null) {
            makeGraph();
            tabbedGraph.setSelectedIndex(1);
        } else {
            tabbedGraph.setSelectedIndex(0);
            JOptionPane.showMessageDialog(null, msgErr, msgErr, JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public JComponent getPrintableComponent() {
        if (saveGraphToFile) {
            saveGraphToFile = false;
            // (re)draw the graph first to take settings into account (Bug 58329)
            if (getData().length > 0 && getData()[0].length>0) {
                makeGraph();
            }
            graphPanel.setBounds(graphPanel.getLocation().x,graphPanel.getLocation().y,
                    graphPanel.width,graphPanel.height);
            return graphPanel;
        }
        return this;
    }

    @Override
    public void configure(TestElement te) {
        super.configure(te);
        intervalField.setText(te.getPropertyAsString(INTERVAL, String.valueOf(DEFAULT_INTERVAL)));
        samplerSelection.setSelected(te.getPropertyAsBoolean(SERIES_SELECTION, DEFAULT_SERIES_SELECTION));
        samplerMatchLabel.setText(te.getPropertyAsString(SERIES_SELECTION_MATCH_LABEL, "")); //$NON-NLS-1$
        caseChkBox.setSelected(te.getPropertyAsBoolean(SERIES_SELECTION_CASE_SENSITIVE, DEFAULT_CASE_SENSITIVE));
        regexpChkBox.setSelected(te.getPropertyAsBoolean(SERIES_SELECTION_REGEXP, DEFAULT_REGEXP));
        graphTitle.setText(te.getPropertyAsString(GRAPH_TITLE, "")); //$NON-NLS-1$
        titleFontNameList.setSelectedIndex(te.getPropertyAsInt(GRAPH_TITLE_FONT_NAME, DEFAULT_TITLE_FONT_NAME));
        titleFontSizeList.setSelectedIndex(te.getPropertyAsInt(GRAPH_TITLE_FONT_SIZE, DEFAULT_TITLE_FONT_SIZE));
        titleFontStyleList.setSelectedIndex(te.getPropertyAsInt(GRAPH_TITLE_FONT_STYLE, DEFAULT_TITLE_FONT_STYLE));
        strokeWidthList.setSelectedIndex(te.getPropertyAsInt(LINE_STROKE_WIDTH, DEFAULT_STROKE_WIDTH_LIST));
        pointShapeLine.setSelectedIndex(te.getPropertyAsInt(LINE_SHAPE_POINT, DEFAULT_LINE_SHAPE_POINT));
        dynamicGraphSize.setSelected(te.getPropertyAsBoolean(GRAPH_SIZE_DYNAMIC, DEFAULT_DYNAMIC_GRAPH_SIZE));
        graphWidth.setText(te.getPropertyAsString(GRAPH_SIZE_WIDTH, "")); //$NON-NLS-1$
        graphHeight.setText(te.getPropertyAsString(GRAPH_SIZE_HEIGHT, "")); //$NON-NLS-1$
        xAxisTimeFormat.setText(te.getPropertyAsString(XAXIS_TIME_FORMAT, DEFAULT_XAXIS_TIME_FORMAT));
        maxValueYAxisLabel.setText(te.getPropertyAsString(YAXIS_SCALE_MAX_VALUE, "")); //$NON-NLS-1$
        incrScaleYAxis.setText(te.getPropertyAsString(YAXIS_INCREMENT_SCALE, "")); //$NON-NLS-1$
        numberShowGrouping.setSelected(te.getPropertyAsBoolean(YAXIS_NUMBER_GROUPING, DEFAULT_NUMBER_SHOW_GROUPING));
        legendPlacementList.setSelectedIndex(te.getPropertyAsInt(LEGEND_PLACEMENT, DEFAULT_LEGEND_PLACEMENT));
        fontNameList.setSelectedIndex(te.getPropertyAsInt(LEGEND_FONT, DEFAULT_LEGEND_FONT));
        fontSizeList.setSelectedIndex(te.getPropertyAsInt(LEGEND_SIZE, DEFAULT_LEGEND_SIZE));
        fontStyleList.setSelectedIndex(te.getPropertyAsInt(LEGEND_STYLE, DEFAULT_LEGEND_STYLE));
        
        enableSamplerSelection(samplerSelection.isSelected());
        enableDynamicGraph(dynamicGraphSize.isSelected());
    }

    @Override
    public void modifyTestElement(TestElement te) {
        super.modifyTestElement(te);
        te.setProperty(INTERVAL, intervalField.getText(), String.valueOf(DEFAULT_INTERVAL));
        te.setProperty(SERIES_SELECTION, samplerSelection.isSelected(), DEFAULT_SERIES_SELECTION);
        te.setProperty(SERIES_SELECTION_MATCH_LABEL, samplerMatchLabel.getText(), ""); //$NON-NLS-1$
        te.setProperty(SERIES_SELECTION_CASE_SENSITIVE, caseChkBox.isSelected(), DEFAULT_CASE_SENSITIVE);
        te.setProperty(SERIES_SELECTION_REGEXP, regexpChkBox.isSelected(), DEFAULT_REGEXP);
        te.setProperty(GRAPH_TITLE, graphTitle.getText(), ""); //$NON-NLS-1$
        te.setProperty(GRAPH_TITLE_FONT_NAME, titleFontNameList.getSelectedIndex(), DEFAULT_TITLE_FONT_NAME);
        te.setProperty(GRAPH_TITLE_FONT_SIZE, titleFontSizeList.getSelectedIndex(), DEFAULT_TITLE_FONT_SIZE);
        te.setProperty(GRAPH_TITLE_FONT_STYLE, titleFontStyleList.getSelectedIndex(), DEFAULT_TITLE_FONT_STYLE);
        te.setProperty(LINE_STROKE_WIDTH, strokeWidthList.getSelectedIndex(), DEFAULT_STROKE_WIDTH_LIST);
        te.setProperty(LINE_SHAPE_POINT, pointShapeLine.getSelectedIndex(), DEFAULT_LINE_SHAPE_POINT);
        te.setProperty(GRAPH_SIZE_DYNAMIC, dynamicGraphSize.isSelected(), DEFAULT_DYNAMIC_GRAPH_SIZE);
        te.setProperty(GRAPH_SIZE_WIDTH, graphWidth.getText(), ""); //$NON-NLS-1$
        te.setProperty(GRAPH_SIZE_HEIGHT, graphHeight.getText(), ""); //$NON-NLS-1$
        te.setProperty(XAXIS_TIME_FORMAT, xAxisTimeFormat.getText(), DEFAULT_XAXIS_TIME_FORMAT);
        te.setProperty(YAXIS_SCALE_MAX_VALUE, maxValueYAxisLabel.getText(), ""); //$NON-NLS-1$
        te.setProperty(YAXIS_INCREMENT_SCALE, incrScaleYAxis.getText(), ""); //$NON-NLS-1$
        te.setProperty(YAXIS_NUMBER_GROUPING, numberShowGrouping.isSelected(), DEFAULT_NUMBER_SHOW_GROUPING);
        te.setProperty(LEGEND_PLACEMENT, legendPlacementList.getSelectedIndex(), DEFAULT_LEGEND_PLACEMENT);
        te.setProperty(LEGEND_FONT, fontNameList.getSelectedIndex(), DEFAULT_LEGEND_FONT);
        te.setProperty(LEGEND_SIZE, fontSizeList.getSelectedIndex(), DEFAULT_LEGEND_SIZE);
        te.setProperty(LEGEND_STYLE, fontStyleList.getSelectedIndex(), DEFAULT_LEGEND_STYLE);
        
        // Update sub-element visibility and data reload if need
        enableSamplerSelection(samplerSelection.isSelected());
        enableDynamicGraph(dynamicGraphSize.isSelected());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        intervalField.setText(String.valueOf(DEFAULT_INTERVAL));
        samplerSelection.setSelected(DEFAULT_SERIES_SELECTION);
        samplerMatchLabel.setText( ""); //$NON-NLS-1$
        caseChkBox.setSelected(DEFAULT_CASE_SENSITIVE);
        regexpChkBox.setSelected(DEFAULT_REGEXP);
        graphTitle.setText(""); //$NON-NLS-1$
        titleFontNameList.setSelectedIndex(DEFAULT_TITLE_FONT_NAME);
        titleFontSizeList.setSelectedIndex(DEFAULT_TITLE_FONT_SIZE);
        titleFontStyleList.setSelectedIndex(DEFAULT_TITLE_FONT_STYLE);
        strokeWidthList.setSelectedIndex(DEFAULT_STROKE_WIDTH_LIST);
        pointShapeLine.setSelectedIndex(DEFAULT_LINE_SHAPE_POINT);
        dynamicGraphSize.setSelected(DEFAULT_DYNAMIC_GRAPH_SIZE);
        graphWidth.setText(""); //$NON-NLS-1$
        graphHeight.setText(""); //$NON-NLS-1$
        xAxisTimeFormat.setText(DEFAULT_XAXIS_TIME_FORMAT);
        maxValueYAxisLabel.setText(""); //$NON-NLS-1$
        incrScaleYAxis.setText(""); //$NON-NLS-1$
        numberShowGrouping.setSelected(DEFAULT_NUMBER_SHOW_GROUPING);
        legendPlacementList.setSelectedIndex(DEFAULT_LEGEND_PLACEMENT);
        fontNameList.setSelectedIndex(DEFAULT_LEGEND_FONT);
        fontSizeList.setSelectedIndex(DEFAULT_LEGEND_SIZE);
        fontStyleList.setSelectedIndex(DEFAULT_LEGEND_STYLE);
    }

    private JPanel createGraphActionsPane() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel displayPane = new JPanel();
        displayPane.add(displayButton);
        displayButton.addActionListener(this);
        buttonPanel.add(displayPane, BorderLayout.WEST);

        JPanel savePane = new JPanel();
        savePane.add(saveGraph);
        saveGraph.addActionListener(this);
        syncWithName.addActionListener(this);
        buttonPanel.add(savePane, BorderLayout.EAST);

        return buttonPanel;
    }

    public String[] getXAxisLabels() {
        SimpleDateFormat formatter = new SimpleDateFormat(xAxisTimeFormat.getText()); //$NON-NLS-1$ 
        String[] xAxisLabels = new String[(int) durationTest]; // Test can't have a duration more than 2^31 secs (cast from long to int)
        for (int j = 0; j < durationTest; j++) {
            xAxisLabels[j] = formatter.format(new Date((minStartTime + j) * intervalValue));
        }
        return xAxisLabels;
    }

    private String[] getLegendLabels() {
        String[] legends = new String[seriesNames.size()];
        int i = 0;
        for (Map.Entry<String, RespTimeGraphLineBean> entry : seriesNames.entrySet()) {
            RespTimeGraphLineBean val = entry.getValue();
            legends[i] = val.getLabel();
            i++;
        }
        return legends;
    }

    private Color[] getLinesColors() {
        Color[] linesColors = new Color[seriesNames.size()];
        int i = 0;
        for (Map.Entry<String, RespTimeGraphLineBean> entry : seriesNames.entrySet()) {
            RespTimeGraphLineBean val = entry.getValue();
            linesColors[i] = val.getLineColor();
            i++;
        }
        return linesColors;
    }

    private int getIncrScaleYAxis() {
        int incrYAxisScale = 0;
        String iyas = incrScaleYAxis.getText();
        if (iyas.length() != 0) {
            incrYAxisScale = Integer.parseInt(iyas);
        }
        return incrYAxisScale;
    }

    private JPanel createGraphSettingsPane() {
        JPanel settingsPane = new JPanel(new BorderLayout());
        settingsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("graph_resp_time_settings_pane"))); // $NON-NLS-1$
        
        JPanel intervalPane = new JPanel();
        intervalPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        intervalField.setText(String.valueOf(DEFAULT_INTERVAL));
        intervalPane.add(intervalField);
        
        // Button
        intervalButton.setFont(FONT_SMALL);
        intervalButton.addActionListener(this);
        intervalPane.add(intervalButton);

        settingsPane.add(intervalPane, BorderLayout.NORTH);
        settingsPane.add(createGraphSelectionSubPane(), BorderLayout.SOUTH);

        return settingsPane;
    }

    private JPanel createGraphSelectionSubPane() {
        // Search field
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        searchPanel.add(samplerSelection);
        samplerMatchLabel.setEnabled(false);
        applyFilterBtn.setEnabled(false);
        caseChkBox.setEnabled(false);
        regexpChkBox.setEnabled(false);
        samplerSelection.addActionListener(this);

        searchPanel.add(samplerMatchLabel);
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
        syncWithName.setFont(FONT_SMALL);
        titleNamePane.add(graphTitle, BorderLayout.CENTER);
        titleNamePane.add(syncWithName, BorderLayout.EAST);

        JPanel titleStylePane = new JPanel();
        titleStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                titleFontNameList));
        titleFontNameList.setSelectedIndex(DEFAULT_TITLE_FONT_NAME);
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                titleFontSizeList));
        titleFontSizeList.setSelectedItem(StatGraphProperties.getFontSize()[DEFAULT_TITLE_FONT_SIZE]);
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                titleFontStyleList));
        titleFontStyleList.setSelectedIndex(DEFAULT_TITLE_FONT_STYLE);

        JPanel titlePane = new JPanel(new BorderLayout());
        titlePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_title_group"))); // $NON-NLS-1$
        titlePane.add(titleNamePane, BorderLayout.NORTH);
        titlePane.add(titleStylePane, BorderLayout.SOUTH);
        return titlePane;
    }

    private JPanel createLinePane() {       
        JPanel lineStylePane = new JPanel();
        lineStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lineStylePane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("graph_resp_time_settings_line"))); // $NON-NLS-1$
        lineStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("graph_resp_time_stroke_width"), //$NON-NLS-1$
                strokeWidthList));
        strokeWidthList.setSelectedItem(StatGraphProperties.getStrokeWidth()[DEFAULT_STROKE_WIDTH_LIST]);
        lineStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("graph_resp_time_shape_label"), //$NON-NLS-1$
                pointShapeLine));
        pointShapeLine.setSelectedIndex(DEFAULT_LINE_SHAPE_POINT);
        return lineStylePane;
    }

    private JPanel createGraphDimensionPane() {
        JPanel dimensionPane = new JPanel();
        dimensionPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dimensionPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_dimension"))); // $NON-NLS-1$

        dimensionPane.add(dynamicGraphSize);
        dynamicGraphSize.setSelected(DEFAULT_DYNAMIC_GRAPH_SIZE);
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
        xAxisTimeFormat.setText(DEFAULT_XAXIS_TIME_FORMAT); // $NON-NLS-1$
        xAxisPane.add(xAxisTimeFormat);
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
        yAxisPane.add(incrScaleYAxis);
        yAxisPane.add(numberShowGrouping);
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
        legendPlacementList.setSelectedIndex(DEFAULT_LEGEND_PLACEMENT);
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                fontNameList));
        fontNameList.setSelectedIndex(DEFAULT_LEGEND_FONT);
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                fontSizeList));
        fontSizeList.setSelectedItem(StatGraphProperties.getFontSize()[DEFAULT_LEGEND_SIZE]);
        legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                fontStyleList));
        fontStyleList.setSelectedIndex(DEFAULT_LEGEND_STYLE);

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
    
    private void enableDynamicGraph(boolean enable) {
        // if use dynamic graph size is checked, we disable the dimension fields
        if (enable) {
            graphWidth.setEnabled(false);
            graphHeight.setEnabled(false);
        } else {
            graphWidth.setEnabled(true);
            graphHeight.setEnabled(true);
        }
    }

    private void enableSamplerSelection(boolean enable) {
        if (enable) {
            samplerMatchLabel.setEnabled(true);
            applyFilterBtn.setEnabled(true);
            caseChkBox.setEnabled(true);
            regexpChkBox.setEnabled(true);
        } else {
            samplerMatchLabel.setEnabled(false);
            applyFilterBtn.setEnabled(false);
            caseChkBox.setEnabled(false);
            regexpChkBox.setEnabled(false);
        }
    }
}
