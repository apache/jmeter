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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.ColorHelper;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Draws the graph.
 *
 * Created 2001/08/11
 */
public class GraphAccum extends JComponent implements Scrollable, GraphAccumListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private GraphAccumModel model;

    private GraphAccumVisualizer visualizer;

    /** Ensure that the legends are only drawn once. */
    private boolean noLegendYet = true;

    /**
     * Keep track of previous point. Needed to draw a line joining the previous
     * point with the current one.
     */
    private Point[] previousPts;

    /**
     * Ensure that previousPts is allocated once only. It'll be reused at each
     * drawSample. It can't be allocated outside drawSample 'cos the sample is
     * only passed in here.
     */
    private boolean previousPtsAlloc = false;

    protected final static int width = 2000;

    private final static int PLOT_X_WIDTH = 10;

    /**
     * Constructor.
     */
    public GraphAccum() {
        log.debug("Start : GraphAnnum1");
        log.debug("End : GraphAnnum1");
    }

    /**
     * Constructor with model set.
     *
     * @param model
     *            model which this object represents
     */
    public GraphAccum(GraphAccumModel model) {
        this();
        log.debug("Start : GraphAnnum2");
        setModel(model);
        log.debug("End : GraphAnnum2");
    }

    /**
     * Set model which this object represents.
     *
     * @param model
     *            model which this object represents
     */
    private void setModel(Object model) {
        log.debug("Start : setModel1");
        this.model = (GraphAccumModel) model;
        this.model.addGraphAccumListener(this);
        repaint();
        log.debug("End : setModel1");
    }

    /**
     * Set the visualizer.
     *
     * @param visualizer
     *            visualizer of this object
     */
    public void setVisualizer(Object visualizer) {
        if (log.isDebugEnabled()) {
            log.debug("setVisualizer1 : Setting visualizer - " + visualizer);
        }
        this.visualizer = (GraphAccumVisualizer) visualizer;
    }

    /**
     * The legend is only printed once during sampling. This sets the variable
     * that indicates whether the legend has been printed yet or not.
     *
     * @param value
     *            variable that indicates whether the legend has been printed
     *            yet
     */
    public void setNoLegendYet(boolean value) {
        noLegendYet = value;
    }

    /**
     * Gets the PreferredScrollableViewportSize attribute of the Graph object.
     *
     * @return the PreferredScrollableViewportSize value
     */
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    /**
     * Gets the ScrollableUnitIncrement attribute of the Graph object.
     *
     * @return the ScrollableUnitIncrement value
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 5;
    }

    /**
     * Gets the ScrollableBlockIncrement attribute of the Graph object.
     *
     * @return the ScrollableBlockIncrement value
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (int) (visibleRect.width * .9);
    }

    /**
     * Gets the ScrollableTracksViewportWidth attribute of the Graph object.
     *
     * @return the ScrollableTracksViewportWidth value
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Gets the ScrollableTracksViewportHeight attribute of the Graph object.
     *
     * @return the ScrollableTracksViewportHeight value
     */
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    /**
     * The legend is only printed once during sampling. This returns the
     * variable that indicates whether the legend has been printed yet or not.
     *
     * @return value variable that indicates whether the legend has been printed
     *         yet
     */
    public boolean getNoLegendYet() {
        return noLegendYet;
    }

    /**
     * Redraws the gui.
     */
    public void updateGui() {
        log.debug("Start : updateGui1");
        repaint();
        log.debug("End : updateGui1");
    }

    /**
     * Redraws the gui if no rescaling of the graph is needed.
     *
     * @param oneSample
     *            sample to be added
     */
    public void updateGui(final SampleResult oneSample) {
        log.debug("Start : updateGui2");
        final int xPos = model.getSampleCount();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Graphics g = getGraphics();

                if (g != null) {
                    drawSample(xPos * PLOT_X_WIDTH, oneSample, g);
                }
            }
        });
        log.debug("End : updateGui2");
    }

    /** {@inheritDoc}} */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        log.debug("Start : paintComponent1");

        synchronized (model.getList()) {
            // For repainting set this to false because all the points needs to
            // be redrawn so no need(shouldn't) use the previousPts.
            previousPtsAlloc = false;
            Iterator<SampleResult> e = model.getList().iterator();

            for (int i = 0; e.hasNext(); i++) {
                SampleResult s = e.next();

                drawSample(i * PLOT_X_WIDTH, s, g);
            }
        }
        log.debug("End : paintComponent1");
    }

    /**
     * Clears this graph.
     */
    public void clearData() {
        setNoLegendYet(true);
        ((JPanel) visualizer.getWhiteCanvas()).removeAll();
        previousPts = null;
    }

    private void drawSample(int x, SampleResult oneSample, Graphics g) {
        log.debug("Start : drawSample1");

        // Used to keep track of accumulated load times of components.
        int lastLevel = 0;

        // Number of components
        int compCount = 0;

        SampleResult[] resultList = oneSample.getSubResults();
        int resultListCount = 0;

        // Allocate previousPts only the first time
        if (!previousPtsAlloc) {
            resultListCount += resultList.length;
            previousPts = new Point[resultListCount + 2];
        }

        Color currColor = Color.black;
        JPanel lPanel = (JPanel) visualizer.getWhiteCanvas();
        JPanel legendPanel = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        legendPanel.setLayout(gridBag);
        lPanel.add(legendPanel);
        Dimension d = this.getSize();

        // Set the total time to load the sample
        long totalTime = oneSample.getTime();

        // If the page has other components then set the total time to be that
        // including all its components' load time.
        if (log.isDebugEnabled()) {
            log.debug("drawSample1 : total time - " + totalTime);
        }
        int data = (int) (totalTime * d.height / model.getMax());

        g.setColor(currColor);
        if (!previousPtsAlloc) {
            // If first dot, just draw the point.
            g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
        } else {
            // Otherwise, draw from previous point.
            g.drawLine((previousPts[0].x) % width, previousPts[0].y, x % width, d.height - data);
        }

        // Store current total time point
        previousPts[0] = new Point(x % width, d.height - data);
        if (noLegendYet) {
            gbc.gridx = 0;
            gbc.gridy = compCount++;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 10, 0, 0);
            JLabel totalTimeLabel = new JLabel("Total time - " + oneSample.toString());

            totalTimeLabel.setForeground(currColor);
            gridBag.setConstraints(totalTimeLabel, gbc);
            legendPanel.add(totalTimeLabel);
        }

        // Plot the time of the page itself without all its components
        if (log.isDebugEnabled()) {
            log.debug("drawSample1 : main page load time - " + oneSample.getTime());
        }
        data = (int) (oneSample.getTime() * d.height / model.getMax());
        currColor = ColorHelper.changeColorCyclicIncrement(currColor, 40);
        g.setColor(currColor);
        if (!previousPtsAlloc) {
            // If first dot, just draw the point
            g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
        } else {
            // Otherwise, draw from previous point
            g.drawLine((previousPts[1].x) % width, previousPts[1].y, x % width, d.height - data);
        }
        // Store load time without components
        previousPts[1] = new Point(x % width, d.height - data);
        if (noLegendYet) {
            gbc.gridx = 0;
            gbc.gridy = compCount++;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 10, 0, 0);
            JLabel mainTimeLabel = new JLabel(oneSample.toString());

            mainTimeLabel.setForeground(currColor);
            gridBag.setConstraints(mainTimeLabel, gbc);
            legendPanel.add(mainTimeLabel);
        }
        lastLevel += data;
        // Plot the times of the total times components
        int currPreviousPts = 2;

        if (resultList != null) {
            for (int i = 0; i < resultList.length; i++) {
                SampleResult componentRes = resultList[i];

                if (log.isDebugEnabled()) {
                    log.debug("drawSample1 : componentRes - " + componentRes.getSampleLabel() + " loading time - "
                            + componentRes.getTime());
                }
                data = (int) (componentRes.getTime() * d.height / model.getMax());
                data += lastLevel;
                currColor = ColorHelper.changeColorCyclicIncrement(currColor, 100);
                g.setColor(currColor);
                if (!previousPtsAlloc) {
                    // If first dot, just draw the point
                    g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
                } else {
                    // Otherwise, draw from previous point
                    g.drawLine((previousPts[currPreviousPts].x) % width, previousPts[currPreviousPts].y, x % width,
                            d.height - data);
                }
                // Store the current plot
                previousPts[currPreviousPts++] = new Point(x % width, d.height - data);
                if (noLegendYet) {
                    gbc.gridx = 0;
                    gbc.gridy = compCount++;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 1.0;
                    gbc.insets = new Insets(0, 10, 0, 0);
                    JLabel compTimeLabel = new JLabel(componentRes.getSampleLabel());

                    compTimeLabel.setForeground(currColor);
                    gridBag.setConstraints(compTimeLabel, gbc);
                    legendPanel.add(compTimeLabel);
                }
                lastLevel = data;
            }
        }

        if (noLegendYet) {
            noLegendYet = false;
            lPanel.repaint();
            lPanel.revalidate();
        }

        // Set the previousPtsAlloc to true here and not after allocation
        // because the rest of the codes also depend on previousPtsAlloc to be
        // false if first time plotting the graph i.e. there are no previous
        // points.
        if (!previousPtsAlloc) {
            previousPtsAlloc = true;
        }
        log.debug("End : drawSample1");
    }
}
