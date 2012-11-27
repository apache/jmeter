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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * This class implements a statistical analyser that takes samples to process a
 * Spline interpolated curve. Currently, it tries to look mostly like the
 * GraphVisualizer.
 *
 */
public class SplineVisualizer extends AbstractVisualizer implements ImageVisualizer, GraphListener {

    private static final long serialVersionUID = 240L;

    private static final String SUFFIX_MS = " ms";  //$NON-NLS-1$

    protected final Color BACKGROUND_COLOR = getBackground();

    protected final Color MINIMUM_COLOR = new Color(0F, 0.5F, 0F);

    protected final Color MAXIMUM_COLOR = new Color(0.9F, 0F, 0F);

    protected final Color AVERAGE_COLOR = new Color(0F, 0F, 0.75F);

    protected final Color INCOMING_COLOR = Color.black;

    protected final int NUMBERS_TO_DISPLAY = 4;

    protected final boolean FILL_UP_WITH_ZEROS = false;

    private transient SplineGraph graph = null;

    private JLabel minimumLabel = null;

    private JLabel maximumLabel = null;

    private JLabel averageLabel = null;

    private JLabel incomingLabel = null;

    private JLabel minimumNumberLabel = null;

    private JLabel maximumNumberLabel = null;

    private JLabel averageNumberLabel = null;

    private JLabel incomingNumberLabel = null;

    private transient SplineModel model;

    public SplineVisualizer() {
        super();
        model = new SplineModel();
        graph = new SplineGraph();
        this.model.setListener(this);
        setGUI();
    }

    @Override
    public void add(final SampleResult res) {
        JMeterUtils.runSafe(new Runnable() {            
            @Override
            public void run() {
                model.add(res);
            }
        });
    }

    @Override
    public String getLabelResource() {
        return "spline_visualizer_title"; //$NON-NLS-1$
    }

    @Override
    public void updateGui(Sample s) {
        updateGui();
    }

    @Override
    public void clearData() {
        model.clearData();
    }

    private void setGUI() {
        Color backColor = BACKGROUND_COLOR;

        this.setBackground(backColor);

        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));

        // NAME
        mainPanel.add(makeTitlePanel());

        maximumLabel = new JLabel(JMeterUtils.getResString("spline_visualizer_maximum")); //$NON-NLS-1$
        maximumLabel.setForeground(MAXIMUM_COLOR);
        maximumLabel.setBackground(backColor);

        averageLabel = new JLabel(JMeterUtils.getResString("spline_visualizer_average")); //$NON-NLS-1$
        averageLabel.setForeground(AVERAGE_COLOR);
        averageLabel.setBackground(backColor);

        incomingLabel = new JLabel(JMeterUtils.getResString("spline_visualizer_incoming")); //$NON-NLS-1$
        incomingLabel.setForeground(INCOMING_COLOR);
        incomingLabel.setBackground(backColor);

        minimumLabel = new JLabel(JMeterUtils.getResString("spline_visualizer_minimum")); //$NON-NLS-1$
        minimumLabel.setForeground(MINIMUM_COLOR);
        minimumLabel.setBackground(backColor);

        maximumNumberLabel = new JLabel("0 ms"); //$NON-NLS-1$
        maximumNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        maximumNumberLabel.setForeground(MAXIMUM_COLOR);
        maximumNumberLabel.setBackground(backColor);

        averageNumberLabel = new JLabel("0 ms"); //$NON-NLS-1$
        averageNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        averageNumberLabel.setForeground(AVERAGE_COLOR);
        averageNumberLabel.setBackground(backColor);

        incomingNumberLabel = new JLabel("0 ms"); //$NON-NLS-1$
        incomingNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        incomingNumberLabel.setForeground(INCOMING_COLOR);
        incomingNumberLabel.setBackground(backColor);

        minimumNumberLabel = new JLabel("0 ms"); //$NON-NLS-1$
        minimumNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        minimumNumberLabel.setForeground(MINIMUM_COLOR);
        minimumNumberLabel.setBackground(backColor);

        // description Panel
        JPanel labelPanel = new JPanel();

        labelPanel.setLayout(new GridLayout(0, 1));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        labelPanel.setBackground(backColor);
        labelPanel.add(maximumLabel);
        labelPanel.add(averageLabel);
        if (model.SHOW_INCOMING_SAMPLES) {
            labelPanel.add(incomingLabel);
        }
        labelPanel.add(minimumLabel);
        // number Panel
        JPanel numberPanel = new JPanel();

        numberPanel.setLayout(new GridLayout(0, 1));
        numberPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        numberPanel.setBackground(backColor);
        numberPanel.add(maximumNumberLabel);
        numberPanel.add(averageNumberLabel);
        if (model.SHOW_INCOMING_SAMPLES) {
            numberPanel.add(incomingNumberLabel);
        }
        numberPanel.add(minimumNumberLabel);
        // information display Panel
        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(labelPanel, BorderLayout.CENTER);
        infoPanel.add(numberPanel, BorderLayout.EAST);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(infoPanel, BorderLayout.WEST);
        this.add(graph, BorderLayout.CENTER);
        // everyone is free to swing on its side :)
        // add(infoPanel, BorderLayout.EAST);
    }

    @Override
    public void updateGui() {
        repaint();
        synchronized (this) {
            setMinimum(model.getMinimum());
            setMaximum(model.getMaximum());
            setAverage(model.getAverage());
            setIncoming(model.getCurrent());
        }
    }

    @Override
    public String toString() {
        return "Show the samples analysis as a Spline curve";
    }

    private String formatMeasureToDisplay(long measure) {
        String numberString = String.valueOf(measure);

        if (FILL_UP_WITH_ZEROS) {
            for (int i = numberString.length(); i < NUMBERS_TO_DISPLAY; i++) {
                numberString = "0" + numberString; //$NON-NLS-1$
            }
        }
        return numberString;
    }

    private void setMinimum(long n) {
        String text = this.formatMeasureToDisplay(n) + SUFFIX_MS;

        this.minimumNumberLabel.setText(text);
    }

    private void setMaximum(long n) {
        String text = this.formatMeasureToDisplay(n) + SUFFIX_MS;

        this.maximumNumberLabel.setText(text);
    }

    private void setAverage(long n) {
        String text = this.formatMeasureToDisplay(n) + SUFFIX_MS;

        this.averageNumberLabel.setText(text);
    }

    private void setIncoming(long n) {
        String text = this.formatMeasureToDisplay(n) + SUFFIX_MS;

        this.incomingNumberLabel.setText(text);
    }

    public JPanel getControlPanel() {// TODO - is this needed?
        return this;
    }

    @Override
    public Image getImage() {
        Image result = graph.createImage(graph.getWidth(), graph.getHeight());

        graph.paintComponent(result.getGraphics());

        return result;
    }

    /**
     * Component showing a Spline curve.
     *
     */
    public class SplineGraph extends JComponent {

        private static final long serialVersionUID = 240L;

        private final Color WAITING_COLOR = Color.darkGray;

        private int lastWidth = -1;

        private int lastHeight = -1;

        private int[] plot = null;

        public SplineGraph() {
        }

        /**
         * Clear the Spline graph and get ready for the next wave.
         */
        public void clear() {
            lastWidth = -1;
            lastHeight = -1;
            plot = null;
            this.repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Dimension dimension = this.getSize();
            int width = dimension.width;
            int height = dimension.height;

            if (model.getDataCurve() == null) {
                g.setColor(this.getBackground());
                g.fillRect(0, 0, width, height);
                g.setColor(WAITING_COLOR);
                g.drawString(JMeterUtils.getResString("spline_visualizer_waitingmessage"),  //$NON-NLS-1$
                        (width - 120) / 2, height - (height - 12) / 2);
                return;
            }

            // boolean resized = true;

            if (width == lastWidth && height == lastHeight) {
                // dimension of the SplineGraph is the same
                // resized = false;
            } else {
                // dimension changed
                // resized = true;
                lastWidth = width;
                lastHeight = height;
            }

            this.plot = model.getDataCurve().getPlots(width, height); // rounds!

            int n = plot.length;
            int curY = plot[0];

            for (int i = 1; i < n; i++) {
                g.setColor(Color.black);
                g.drawLine(i - 1, height - curY - 1, i, height - plot[i] - 1);
                curY = plot[i];
            }
        }
    }
}
