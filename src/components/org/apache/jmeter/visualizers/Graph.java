// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.util.JMeterColor;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Implements a simple graph for displaying performance results.
 *
 * @author     Michael Stover
 * Created      March 21, 2002
 * @version    $Revision$ Last updated: $Date$
 */
public class Graph
    extends JComponent
    implements Scrollable, GraphListener, Clearable
{
    private static Logger log = LoggingManager.getLoggerForClass();
    private boolean wantData = true;
    private boolean wantAverage = true;
    private boolean wantDeviation = true;
    private boolean wantThroughput = true;
    private boolean wantMedian = true;

    private GraphModel model;
    private static int width = 2000;

    /**
     * Constructor for the Graph object.
     */
    public Graph()
    {
       this.setPreferredSize(new Dimension(width, 100));
    }

    /**
     * Constructor for the Graph object.
     */
    public Graph(GraphModel model)
    {
        this();
        setModel(model);
    }

    /**
     * Sets the Model attribute of the Graph object.
     */
    private void setModel(Object model)
    {
        this.model = (GraphModel) model;
        this.model.addGraphListener(this);
        repaint();
    }

    /**
     * Gets the PreferredScrollableViewportSize attribute of the Graph object.
     *
     * @return the PreferredScrollableViewportSize value
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return this.getPreferredSize();
        // return new Dimension(width, 400);
    }

    /**
     * Gets the ScrollableUnitIncrement attribute of the Graph object.
     *@return              the ScrollableUnitIncrement value
     */
    public int getScrollableUnitIncrement(
        Rectangle visibleRect,
        int orientation,
        int direction)
    {
        return 5;
    }

    /**
     * Gets the ScrollableBlockIncrement attribute of the Graph object.
     * @return              the ScrollableBlockIncrement value
     */
    public int getScrollableBlockIncrement(
        Rectangle visibleRect,
        int orientation,
        int direction)
    {
        return (int) (visibleRect.width * .9);
    }

    /**
     * Gets the ScrollableTracksViewportWidth attribute of the Graph object.
     *
     * @return    the ScrollableTracksViewportWidth value
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    /**
     * Gets the ScrollableTracksViewportHeight attribute of the Graph object.
     *
     * @return    the ScrollableTracksViewportHeight value
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return true;
    }

    /**
     * Clears this graph.
     */
    public void clear()
    {}

    public void enableData(boolean value)
    {
        this.wantData = value;
    }

    public void enableAverage(boolean value)
    {
        this.wantAverage = value;
    }

    public void enableMedian(boolean value)
    {
        this.wantMedian = value;
    }

    public void enableDeviation(boolean value)
    {
        this.wantDeviation = value;
    }

    public void enableThroughput(boolean value)
    {
        this.wantThroughput = value;
    }

    public void updateGui()
    {
        repaint();
    }

    public void updateGui(final Sample oneSample)
    {
        final int xPos = model.getSampleCount();

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Graphics g = getGraphics();

                if (g != null)
                {
                    drawSample(xPos, oneSample, g);
                }
            }
        });
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        synchronized (model.getSamples())
        {
            Iterator e = model.getSamples().iterator();

            for (int i = 0; e.hasNext(); i++)
            {
                Sample s = (Sample) e.next();

                drawSample(i, s, g);
            }
        }
    }

    private void drawSample(int x, Sample oneSample, Graphics g)
    {
        //int width = getWidth();
        int height = getHeight();
        log.debug("Drawing a sample at " + x);
        if (wantData)
        {
            int data = (int) (oneSample.data * height / model.getGraphMax());

            if (!oneSample.error)
            {
                g.setColor(Color.black);
            }
            else
            {
                g.setColor(JMeterColor.YELLOW);
            }
            g.drawLine(x % width, height - data, x % width, height - data - 1);
            log.debug(
                "Drawing coords = " + (x % width) + "," + (height - data));
        }

        if (wantAverage)
        {
            int average =
                (int) (oneSample.average * height / model.getGraphMax());

            g.setColor(Color.blue);
            g.drawLine(
                x % width,
                height - average,
                x % width,
                (height - average - 1));
        }

        if (wantMedian)
        {
            int median =
                (int) (oneSample.median * height / model.getGraphMax());

            g.setColor(JMeterColor.purple);
            g.drawLine(
                x % width,
                height - median,
                x % width,
                (height - median - 1));
        }

        if (wantDeviation)
        {
            int deviation =
                (int) (oneSample.deviation * height / model.getGraphMax());

            g.setColor(Color.red);
            g.drawLine(
                x % width,
                height - deviation,
                x % width,
                (height - deviation - 1));
        }
        if (wantThroughput)
        {
            int throughput =
                (int) (oneSample.throughput
                    * height
                    / model.getThroughputMax());

            g.setColor(JMeterColor.dark_green);
            g.drawLine(
                x % width,
                height - throughput,
                x % width,
                (height - throughput - 1));
        }
    }
}
