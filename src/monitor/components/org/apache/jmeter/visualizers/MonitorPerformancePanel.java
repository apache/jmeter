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

import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

public class MonitorPerformancePanel extends JSplitPane implements TreeSelectionListener, MonitorListener, Clearable {

    private static final long serialVersionUID = 240L;

    private JScrollPane TREEPANE;

    private JPanel GRAPHPANEL;

    private JTree SERVERTREE;

    private DefaultTreeModel TREEMODEL;

    private final MonitorGraph GRAPH;

    private DefaultMutableTreeNode ROOTNODE;

    private final HashMap<String, DefaultMutableTreeNode> SERVERMAP;

    private final MonitorAccumModel MODEL;

    private SampleResult ROOTSAMPLE;

    // Don't make these static, otherwise language change does not work
    private final String LEGEND_HEALTH = JMeterUtils.getResString("monitor_legend_health"); //$NON-NLS-1$

    private final String LEGEND_LOAD = JMeterUtils.getResString("monitor_legend_load"); //$NON-NLS-1$

    private final String LEGEND_MEM = JMeterUtils.getResString("monitor_legend_memory_per"); //$NON-NLS-1$

    private final String LEGEND_THREAD = JMeterUtils.getResString("monitor_legend_thread_per"); //$NON-NLS-1$

    private final ImageIcon LEGEND_HEALTH_ICON = JMeterUtils.getImage("monitor-green-legend.gif"); //$NON-NLS-1$

    private final ImageIcon LEGEND_LOAD_ICON = JMeterUtils.getImage("monitor-blue-legend.gif"); //$NON-NLS-1$

    private final ImageIcon LEGEND_MEM_ICON = JMeterUtils.getImage("monitor-orange-legend.gif"); //$NON-NLS-1$

    private final ImageIcon LEGEND_THREAD_ICON = JMeterUtils.getImage("monitor-red-legend.gif"); //$NON-NLS-1$

    private final String GRID_LABEL_TOP = JMeterUtils.getResString("monitor_label_left_top"); //$NON-NLS-1$

    private final String GRID_LABEL_MIDDLE = JMeterUtils.getResString("monitor_label_left_middle"); //$NON-NLS-1$

    private final String GRID_LABEL_BOTTOM = JMeterUtils.getResString("monitor_label_left_bottom"); //$NON-NLS-1$

    private final String GRID_LABEL_HEALTHY = JMeterUtils.getResString("monitor_label_right_healthy"); //$NON-NLS-1$

//    private final String GRID_LABEL_ACTIVE = JMeterUtils.getResString("monitor_label_right_active"); //$NON-NLS-1$

//    private final String GRID_LABEL_WARNING = JMeterUtils.getResString("monitor_label_right_warning"); //$NON-NLS-1$

    private final String GRID_LABEL_DEAD = JMeterUtils.getResString("monitor_label_right_dead"); //$NON-NLS-1$

    private final String PERF_TITLE = JMeterUtils.getResString("monitor_performance_title"); //$NON-NLS-1$

    private final String SERVER_TITLE = JMeterUtils.getResString("monitor_performance_servers"); //$NON-NLS-1$

    private Font plaintext = new Font("plain", Font.TRUETYPE_FONT, 10); //$NON-NLS-1$

    /**
     *
     * @deprecated Only for use in unit testing
     */
    @Deprecated
    public MonitorPerformancePanel() {
        // log.warn("Only for use in unit testing");
        SERVERMAP = null;
        MODEL = null;
        GRAPH = null;
    }

    /**
     *
     */
    public MonitorPerformancePanel(MonitorAccumModel model, MonitorGraph graph) {
        super();
        this.SERVERMAP = new HashMap<String, DefaultMutableTreeNode>();
        this.MODEL = model;
        this.MODEL.addListener(this);
        this.GRAPH = graph;
        init();
    }

    /**
     * init() will create all the necessary swing panels, labels and icons for
     * the performance panel.
     */
    private void init() {// called from ctor, so must not be overridable
        ROOTSAMPLE = new SampleResult();
        ROOTSAMPLE.setSampleLabel(SERVER_TITLE);
        ROOTSAMPLE.setSuccessful(true);
        ROOTNODE = new DefaultMutableTreeNode(ROOTSAMPLE);
        TREEMODEL = new DefaultTreeModel(ROOTNODE);
        SERVERTREE = new JTree(TREEMODEL);
        SERVERTREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        SERVERTREE.addTreeSelectionListener(this);
        SERVERTREE.setShowsRootHandles(true);
        TREEPANE = new JScrollPane(SERVERTREE);
        TREEPANE.setPreferredSize(new Dimension(150, 200));
        this.add(TREEPANE, JSplitPane.LEFT);
        this.setDividerLocation(0.18);

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        JLabel title = new JLabel(" " + PERF_TITLE);
        title.setPreferredSize(new Dimension(200, 40));
        GRAPHPANEL = new JPanel();
        GRAPHPANEL.setLayout(new BorderLayout());
        GRAPHPANEL.setMaximumSize(new Dimension(MODEL.getBufferSize(), MODEL.getBufferSize()));
        GRAPHPANEL.setBackground(Color.white);
        GRAPHPANEL.add(GRAPH, BorderLayout.CENTER);
        right.add(GRAPHPANEL, BorderLayout.CENTER);

        right.add(title, BorderLayout.NORTH);
        right.add(createLegend(), BorderLayout.SOUTH);
        right.add(createLeftGridLabels(), BorderLayout.WEST);
        right.add(createRightGridLabels(), BorderLayout.EAST);
        this.add(right, JSplitPane.RIGHT);
    }

    /**
     * Method will create the legends at the bottom of the performance tab
     * explaining the meaning of each line.
     *
     * @return JPanel
     */
    private JPanel createLegend() {
        Dimension lsize = new Dimension(130, 18);

        JPanel legend = new JPanel();
        legend.setLayout(new FlowLayout());

        JLabel load = new JLabel(LEGEND_LOAD);
        load.setFont(plaintext);
        load.setPreferredSize(lsize);
        load.setIcon(LEGEND_LOAD_ICON);
        legend.add(load);

        JLabel mem = new JLabel(LEGEND_MEM);
        mem.setFont(plaintext);
        mem.setPreferredSize(lsize);
        mem.setIcon(LEGEND_MEM_ICON);
        legend.add(mem);

        JLabel thd = new JLabel(LEGEND_THREAD);
        thd.setFont(plaintext);
        thd.setPreferredSize(lsize);
        thd.setIcon(LEGEND_THREAD_ICON);
        legend.add(thd);

        JLabel health = new JLabel(LEGEND_HEALTH);
        health.setFont(plaintext);
        health.setPreferredSize(lsize);
        health.setIcon(LEGEND_HEALTH_ICON);
        legend.add(health);

        return legend;
    }

    /**
     * Method is responsible for creating the left grid labels.
     *
     * @return JPanel
     */
    private JPanel createLeftGridLabels() {
        Dimension lsize = new Dimension(33, 20);
        JPanel labels = new JPanel();
        labels.setLayout(new BorderLayout());

        JLabel top = new JLabel(" " + GRID_LABEL_TOP);
        top.setFont(plaintext);
        top.setPreferredSize(lsize);
        labels.add(top, BorderLayout.NORTH);

        JLabel mid = new JLabel(" " + GRID_LABEL_MIDDLE);
        mid.setFont(plaintext);
        mid.setPreferredSize(lsize);
        labels.add(mid, BorderLayout.CENTER);

        JLabel bottom = new JLabel(" " + GRID_LABEL_BOTTOM);
        bottom.setFont(plaintext);
        bottom.setPreferredSize(lsize);
        labels.add(bottom, BorderLayout.SOUTH);
        return labels;
    }

    /**
     * Method is responsible for creating the grid labels on the right for
     * "healthy" and "dead"
     *
     * @return JPanel
     */
    private JPanel createRightGridLabels() {
        JPanel labels = new JPanel();
        labels.setLayout(new BorderLayout());
        labels.setPreferredSize(new Dimension(40, GRAPHPANEL.getWidth() - 100));
        Dimension lsize = new Dimension(40, 20);
        JLabel h = new JLabel(GRID_LABEL_HEALTHY);
        h.setFont(plaintext);
        h.setPreferredSize(lsize);
        labels.add(h, BorderLayout.NORTH);

        JLabel d = new JLabel(GRID_LABEL_DEAD);
        d.setFont(plaintext);
        d.setPreferredSize(lsize);
        labels.add(d, BorderLayout.SOUTH);
        return labels;
    }

    /**
     * MonitorAccumModel will call this method to notify the component data has
     * changed.
     */
    public synchronized void addSample(MonitorModel model) {
        if (!SERVERMAP.containsKey(model.getURL())) {
            DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(model);
            newnode.setAllowsChildren(false);
            SERVERMAP.put(model.getURL(), newnode);
            ROOTNODE.add(newnode);
            this.TREEPANE.updateUI();
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) SERVERTREE.getLastSelectedPathComponent();
        if (node != null) {
            Object usrobj = node.getUserObject();
            if (usrobj instanceof MonitorModel) {
                GRAPH.updateGui((MonitorModel) usrobj);
            }
        }
    }

    /**
     * When the user selects a different node in the tree, we get the selected
     * node. From the node, we get the UserObject used to create the treenode in
     * the constructor.
     */
    public void valueChanged(TreeSelectionEvent e) {
        // we check to see if the lastSelectedPath is null
        // after we clear, it would return null
        if (SERVERTREE.getLastSelectedPathComponent() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) SERVERTREE.getLastSelectedPathComponent();
            Object usrobj = node.getUserObject();
            if (usrobj != null && usrobj instanceof MonitorModel) {
                MonitorModel mo = (MonitorModel) usrobj;
                GRAPH.updateGui(mo);
                this.updateUI();
            }
            TREEPANE.updateUI();
        }
    }

    /**
     * clear will remove all child nodes from the ROOTNODE, clear the HashMap,
     * update the graph and jpanel for the server tree.
     */
    public void clearData() {
        this.SERVERMAP.clear();
        ROOTNODE.removeAllChildren();
        SERVERTREE.updateUI();
        GRAPH.clearData();
    }
}
