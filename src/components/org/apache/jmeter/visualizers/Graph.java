/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" and
 *  "Apache JMeter" must not be used to endorse or promote products
 *  derived from this software without prior written permission. For
 *  written permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  "Apache JMeter", nor may "Apache" appear in their name, without
 *  prior written permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *  Title: Apache JMeter Description: Implements a simple graph for displaying
 *  performance results Copyright: Copyright (c) 2000 Company: Apache Foundation
 *
 *@author     Michael Stover
 *@created    March 21, 2002
 *@version    1.0
 */

public class Graph extends JComponent implements Scrollable, GraphListener, Clearable
{
    private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.GUI);
    private boolean data = true;
    private boolean average = true;
    private boolean deviation = true;
    private boolean throughput = true;
    private boolean median = true;

    private GraphModel model;
    private static int width = 2000;

    /**
     *  Constructor for the Graph object
     */
    public Graph()
    {
       this.setPreferredSize(new Dimension(width, 100));
    }

    /**
     *  Constructor for the Graph object
     *
     *@param  model  Description of Parameter
     */
    public Graph(GraphModel model)
    {
        this();
        setModel(model);
    }

    /**
     *  Sets the Model attribute of the Graph object
     *
     *@param  model  The new Model value
     */
    private void setModel(Object model)
    {
        this.model = (GraphModel) model;
        this.model.addGraphListener(this);
        repaint();
    }

    /**
     *  Gets the PreferredScrollableViewportSize attribute of the Graph object
     *
     *@return    The PreferredScrollableViewportSize value
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return this.getPreferredSize();
        // return new Dimension(width, 400);
    }

    /**
     *  Gets the ScrollableUnitIncrement attribute of the Graph object
     *
     *@param  visibleRect  Description of Parameter
     *@param  orientation  Description of Parameter
     *@param  direction    Description of Parameter
     *@return              The ScrollableUnitIncrement value
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 5;
    }

    /**
     *  Gets the ScrollableBlockIncrement attribute of the Graph object
     *
     *@param  visibleRect  Description of Parameter
     *@param  orientation  Description of Parameter
     *@param  direction    Description of Parameter
     *@return              The ScrollableBlockIncrement value
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return (int) (visibleRect.width * .9);
    }

    /**
     *  Gets the ScrollableTracksViewportWidth attribute of the Graph object
     *
     *@return    The ScrollableTracksViewportWidth value
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    /**
     *  Gets the ScrollableTracksViewportHeight attribute of the Graph object
     *
     *@return    The ScrollableTracksViewportHeight value
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return true;
    }

    /**
     *  Clears this graph
     */
    public void clear()
    {}

    /**
     *  Description of the Method
     *
     *@param  value  Description of Parameter
     */
    public void enableData(boolean value)
    {
        this.data = value;
    }

    /**
     *  Description of the Method
     *
     *@param  value  Description of Parameter
     */
    public void enableAverage(boolean value)
    {
        this.average = value;
    }

    public void enableMedian(boolean value)
    {
        this.median = value;
    }

    /**
     *  Description of the Method
     *
     *@param  value  Description of Parameter
     */
    public void enableDeviation(boolean value)
    {
        this.deviation = value;
    }

    public void enableThroughput(boolean value)
    {
        throughput = value;
    }

    /**
     *  Description of the Method
     */
    public void updateGui()
    {
        repaint();
    }

    /**
     *  Description of the Method
     *
     *@param  oneSample  Description of Parameter
     */
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

    /**
     *  Description of the Method
     *
     *@param  g  Description of Parameter
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Dimension d = this.getSize();

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

    /**
     *  Description of the Method
     *
     *@param  x          Description of Parameter
     *@param  oneSample  Description of Parameter
     *@param  g          Description of Parameter
     */
    private void drawSample(int x, Sample oneSample, Graphics g)
    {
        //int width = getWidth();
        int height = getHeight();
        log.debug("Drawing a sample at " + x);
        if (data)
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
            log.debug("Drawing coords = " + (x % width) + "," + (height - data));
        }

        if (average)
        {
            int average = (int) (oneSample.average * height / model.getGraphMax());

            g.setColor(Color.blue);
            g.drawLine(x % width, height - average, x % width, (height - average - 1));
        }

        if (median)
        {
            int median = (int) (oneSample.median * height / model.getGraphMax());

            g.setColor(JMeterColor.purple);
            g.drawLine(x % width, height - median, x % width, (height - median - 1));
        }

        if (deviation)
        {
            int deviation = (int) (oneSample.deviation * height / model.getGraphMax());

            g.setColor(Color.red);
            g.drawLine(x % width, height - deviation, x % width, (height - deviation - 1));
        }
        if (throughput)
        {
            int throughput = (int) (oneSample.throughput * height / model.getThroughputMax());

            g.setColor(JMeterColor.dark_green);
            g.drawLine(x % width, height - throughput, x % width, (height - throughput - 1));
        }
    }
}
