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
 */
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Image;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * For performance reasons, I am using tabs for the visualizers. Since a
 * visualizer is heavy weight, I don not want to have two separate result
 * collectors rather the same information. Instead, I would rather have the
 * visualizer be the container for the data and simply pass the data to child
 * JComponents. In the future, we may want to add email alerts as a third tab.
 */
public class MonitorHealthVisualizer extends AbstractVisualizer implements ImageVisualizer, ItemListener,
        GraphListener, Clearable {

    private static final String CONNECTOR_PREFIX = "connector.prefix"; // $NON-NLS-1$
    private static final String CONNECTOR_PREFIX_DEFAULT = ""; // $NON-NLS-1$

    private MonitorTabPane TABPANE;

    private MonitorHealthPanel HEALTHPANE;

    private MonitorPerformancePanel PERFPANE;

    private MonitorAccumModel MODEL;

    private MonitorGraph GRAPH;

    private JLabeledTextField prefixField;

    public static final String BUFFER = "monitor.buffer.size"; // $NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Constructor for the GraphVisualizer object.
     */
    public MonitorHealthVisualizer() {
        this.isStats = true;
        initModel();
        init();
    }

    public void configure(TestElement el) {
        super.configure(el);
        prefixField.setText(el.getPropertyAsString(CONNECTOR_PREFIX, CONNECTOR_PREFIX_DEFAULT));
        MODEL.setPrefix(prefixField.getText());
    }

    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(CONNECTOR_PREFIX,prefixField.getText(),CONNECTOR_PREFIX_DEFAULT);
        MODEL.setPrefix(prefixField.getText());
    }
    
    private void initModel() {
        MODEL = new MonitorAccumModel();
        GRAPH = new MonitorGraph(MODEL);
        MODEL.setBufferSize(JMeterUtils.getPropDefault(BUFFER, 800));
    }

    public String getLabelResource() {
        return "monitor_health_title"; // $NON-NLS-1$
    }

    /**
     * Because of the unique requirements of a monitor We have to handle the
     * results differently than normal GUI components. A monitor should be able
     * to run for a very long time without eating up all the memory.
     */
    public void add(SampleResult res) {
        MODEL.addSample(res);
        try {
            collector.recordStats(this.MODEL.getLastSample().cloneMonitorStats());
        } catch (Exception e) {
            // for now just swallow the exception
            log.debug("StatsModel was null", e);
        }
    }

    public Image getImage() {
        Image result = GRAPH.createImage(this.getWidth(), this.getHeight());
        Graphics image = result.getGraphics();
        GRAPH.paintComponent(image);
        return result;
    }

    public void itemStateChanged(ItemEvent e) {
    }

    public synchronized void updateGui() {
        this.repaint();
    }

    public synchronized void updateGui(Sample s) {
        this.repaint();
    }

    /**
     * Initialize the GUI.
     */
    private void init() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        Border margin = new EmptyBorder(10, 10, 5, 10);
        this.setBorder(margin);

        // Add the main panel and the graph
        this.add(this.makeTitlePanel(), BorderLayout.NORTH);
        this.createTabs();
        prefixField = new JLabeledTextField(JMeterUtils.getResString("monitor_label_prefix")); // $NON-NLS-1$
        add(prefixField, BorderLayout.SOUTH);
    }

    private void createTabs() {
        TABPANE = new MonitorTabPane();
        createHealthPane(TABPANE);
        createPerformancePane(TABPANE);
        this.add(TABPANE, BorderLayout.CENTER);
    }

    /**
     * Create the JPanel
     *
     * @param pane
     */
    private void createHealthPane(MonitorTabPane pane) {
        HEALTHPANE = new MonitorHealthPanel(MODEL);
        pane.addTab(JMeterUtils.getResString("monitor_health_tab_title"), HEALTHPANE); // $NON-NLS-1$
    }

    /**
     * Create the JSplitPane for the performance history
     *
     * @param pane
     */
    private void createPerformancePane(MonitorTabPane pane) {
        PERFPANE = new MonitorPerformancePanel(MODEL, GRAPH);
        pane.addTab(JMeterUtils.getResString("monitor_performance_tab_title"), PERFPANE); // $NON-NLS-1$
    }

    /**
     * Clears the MonitorAccumModel.
     */
    public void clearData() {
        this.MODEL.clearData();
        this.HEALTHPANE.clearData();
        this.PERFPANE.clearData();
    }

}
