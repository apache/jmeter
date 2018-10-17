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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.util.JMeterColor;
import org.apache.jmeter.samplers.Clearable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a simple graph for displaying performance results.
 *
 */
public class Graph extends JComponent implements Scrollable, Clearable {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(Graph.class);

    private boolean wantData = true;

    private boolean wantAverage = true;

    private boolean wantDeviation = true;

    private boolean wantThroughput = true;

    private boolean wantMedian = true;

    private CachingStatCalculator model;

    private static final int WIDTH = 2000;

    private long graphMax = 1;

    private double throughputMax = 1;

    /**
     * Constructor for the Graph object.
     */
    public Graph() {
        this.setPreferredSize(new Dimension(WIDTH, 100));
    }

    /**
     * Constructor for the Graph object.
     * @param model The container for samples and statistics
     */
    public Graph(CachingStatCalculator model) {
        this();
        this.model = model;
    }

    /**
     * Gets the PreferredScrollableViewportSize attribute of the Graph object.
     *
     * @return the PreferredScrollableViewportSize value
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    /**
     * Gets the ScrollableUnitIncrement attribute of the Graph object.
     *
     * @return the ScrollableUnitIncrement value
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 5;
    }

    /**
     * Gets the ScrollableBlockIncrement attribute of the Graph object.
     *
     * @return the ScrollableBlockIncrement value
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (int) (visibleRect.width * .9);
    }

    /**
     * Gets the ScrollableTracksViewportWidth attribute of the Graph object.
     *
     * @return the ScrollableTracksViewportWidth value
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Gets the ScrollableTracksViewportHeight attribute of the Graph object.
     *
     * @return the ScrollableTracksViewportHeight value
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    /**
     * Clears this graph.
     */
    @Override
    public void clearData() {
        graphMax = 1;
        throughputMax = 1;
    }

    public void enableData(boolean value) {
        this.wantData = value;
    }

    public void enableAverage(boolean value) {
        this.wantAverage = value;
    }

    public void enableMedian(boolean value) {
        this.wantMedian = value;
    }

    public void enableDeviation(boolean value) {
        this.wantDeviation = value;
    }

    public void enableThroughput(boolean value) {
        this.wantThroughput = value;
    }

    public void updateGui(final Sample oneSample) {
        long h = model.getPercentPoint((float) 0.90).longValue();
        boolean repaint = false;
        if ((oneSample.getCount() % 20 == 0 || oneSample.getCount() < 20) && h > (graphMax * 1.2) || graphMax > (h * 1.2)) {
            if (h >= 1) {
                graphMax = h;
            } else {
                graphMax = 1;
            }
            repaint = true;
        }
        if (model.getMaxThroughput() > throughputMax) {
            throughputMax = model.getMaxThroughput() * 1.3;
            repaint = true;
        }
        if (repaint) {
            repaint();
            return;
        }
        final long xPos = model.getCount();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graphics g = getGraphics();

                if (g != null) {
                    drawSample(xPos, oneSample, g);
                }
            }
        });
    }

    /** {@inheritDoc}} */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        List<Sample> samples = model.getSamples();
        synchronized (samples ) {
            Iterator<Sample> e = samples.iterator();

            for (int i = 0; e.hasNext(); i++) {
                Sample s = e.next();

                drawSample(i, s, g);
            }
        }
    }

    private void drawSample(long x, Sample oneSample, Graphics g) {
        int height = getHeight();
        log.debug("Drawing a sample at {}", x);
        int adjustedWidth = (int)(x % WIDTH); // will always be within range of an int: as must be < width
        if (wantData) {
            int data = (int) (oneSample.getData() * height / graphMax);

            if (oneSample.isSuccess()) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.YELLOW);
            }
            g.drawLine(adjustedWidth, height - data, adjustedWidth, height - data - 1);
            if (log.isDebugEnabled()) {
                log.debug("Drawing coords = {}, {}", adjustedWidth, height - data);
            }
        }

        if (wantAverage) {
            int average = (int) (oneSample.getAverage() * height / graphMax);

            g.setColor(Color.blue);
            g.drawLine(adjustedWidth, height - average, adjustedWidth, height - average - 1);
        }

        if (wantMedian) {
            int median = (int) (oneSample.getMedian() * height / graphMax);

            g.setColor(JMeterColor.PURPLE);
            g.drawLine(adjustedWidth, height - median, adjustedWidth, height - median - 1);
        }

        if (wantDeviation) {
            int deviation = (int) (oneSample.getDeviation() * height / graphMax);

            g.setColor(Color.red);
            g.drawLine(adjustedWidth, height - deviation, adjustedWidth, height - deviation - 1);
        }
        if (wantThroughput) {
            int throughput = (int) (oneSample.getThroughput() * height / throughputMax);

            g.setColor(JMeterColor.DARK_GREEN);
            g.drawLine(adjustedWidth, height - throughput, adjustedWidth, height - throughput - 1);
        }
    }

    /**
     * @return Returns the graphMax.
     */
    public long getGraphMax() {
        return graphMax;
    }
}
