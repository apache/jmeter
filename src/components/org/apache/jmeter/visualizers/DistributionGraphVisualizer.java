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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

/**
 * This class implements the visualizer for displaying the distribution graph.
 * Distribution graphs are useful for standard benchmarks and viewing the
 * distribution of data points. Results tend to clump together.
 *
 * Created May 25, 2004
 */
public class DistributionGraphVisualizer extends AbstractVisualizer implements ImageVisualizer, GraphListener,
        Clearable {
    private static final long serialVersionUID = 240L;

    private final SamplingStatCalculator model;

    private JPanel graphPanel = null;

    private final DistributionGraph graph;

    private JTextField noteField;

    private static final int DELAY = 10;

    private int counter = 0;

    /**
     * Constructor for the GraphVisualizer object.
     */
    public DistributionGraphVisualizer() {
        model = new SamplingStatCalculator("Distribution");
        graph = new DistributionGraph(model);
        graph.setBackground(Color.white);
        init();
    }

    /**
     * Gets the Image attribute of the GraphVisualizer object.
     *
     * @return the Image value
     */
    @Override
    public Image getImage() {
        Image result = graph.createImage(graph.getWidth(), graph.getHeight());

        graph.paintComponent(result.getGraphics());

        return result;
    }

    @Override
    public synchronized void updateGui() {
        if (graph.getWidth() < 10) {
            graph.setPreferredSize(new Dimension(getWidth() - 40, getHeight() - 160));
        }
        graphPanel.updateUI();
        graph.repaint();
    }

    @Override
    public synchronized void updateGui(Sample s) {
        // We have received one more sample
        if (DELAY == counter) {
            updateGui();
            counter = 0;
        } else {
            counter++;
        }
    }

    @Override
    public void add(final SampleResult res) {
        JMeterUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                // made currentSample volatile
                model.addSample(res);
                updateGui(model.getCurrentSample());                
            }
        });
    }

    @Override
    public String getLabelResource() {
        return "distribution_graph_title"; // $NON-NLS-1$
    }

    @Override
    public synchronized void clearData() {
        this.graph.clearData();
        model.clear();
        repaint();
    }

    @Override
    public String toString() {
        return "Show the samples in a distribution graph";
    }

    /**
     * Initialize the GUI.
     */
    private void init() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        Border margin = new EmptyBorder(10, 10, 5, 10);

        this.setBorder(margin);

        // Set up the graph with header, footer, Y axis and graph display
        JPanel lgraphPanel = new JPanel(new BorderLayout());
        lgraphPanel.add(createGraphPanel(), BorderLayout.CENTER);
        lgraphPanel.add(createGraphInfoPanel(), BorderLayout.SOUTH);

        // Add the main panel and the graph
        this.add(makeTitlePanel(), BorderLayout.NORTH);
        this.add(lgraphPanel, BorderLayout.CENTER);
    }

    // Methods used in creating the GUI

    /**
     * Creates a scroll pane containing the actual graph of the results.
     *
     * @return a scroll pane containing the graph
     */
    private Component createGraphPanel() {
        graphPanel = new JPanel();
        graphPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.darkGray));
        graphPanel.add(graph);
        graphPanel.setBackground(Color.white);
        return graphPanel;
    }

    // /**
    // * Creates one of the fields used to display the graph's current
    // * values.
    // *
    // * @param color the color used to draw the value. By convention
    // * this is the same color that is used to draw the
    // * graph for this value and in the choose panel.
    // * @param length the number of digits which the field should be
    // * able to display
    // *
    // * @return a text field configured to display one of the
    // * current graph values
    // */
    // private JTextField createInfoField(Color color, int length)
    // {
    // JTextField field = new JTextField(length);
    // field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    // field.setEditable(false);
    // field.setForeground(color);
    // field.setBackground(getBackground());
    //
    // // The text field should expand horizontally, but have
    // // a fixed height
    // field.setMaximumSize(new Dimension(
    // field.getMaximumSize().width,
    // field.getPreferredSize().height));
    // return field;
    // }

    /**
     * Creates a label for one of the fields used to display the graph's current
     * values. Neither the label created by this method or the
     * <code>field</code> passed as a parameter is added to the GUI here.
     *
     * @param labelResourceName
     *            the name of the label resource. This is used to look up the
     *            label text using {@link JMeterUtils#getResString(String)}.
     * @param field
     *            the field this label is being created for.
     */
    private JLabel createInfoLabel(String labelResourceName, JTextField field) {
        JLabel label = new JLabel(JMeterUtils.getResString(labelResourceName));
        label.setForeground(field.getForeground());
        label.setLabelFor(field);
        return label;
    }

    /**
     * Creates the information Panel at the bottom
     *
     * @return the box containing the panel
     */
    private Box createGraphInfoPanel() {
        Box graphInfoPanel = Box.createHorizontalBox();
        this.noteField = new JTextField();
        graphInfoPanel.add(this.createInfoLabel("distribution_note1", this.noteField)); // $NON-NLS-1$
        return graphInfoPanel;
    }

    /**
     * Method implements Printable, which is suppose to return the correct
     * internal component. The Action class can then print or save the graphics
     * to a file.
     */
    @Override
    public JComponent getPrintableComponent() {
        return this.graphPanel;
    }

}
