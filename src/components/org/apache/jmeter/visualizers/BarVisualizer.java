/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.visualizers;


import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.jmeter.samplers.SampleResult;


/**
 * This class implements a scrolling bar visualizer with
 * user selectable scale.
 *
 * This visualizer emulates LiveSoftware ServletKiller.
 *
 * @author  <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision$ $Date$
 */
public class BarVisualizer extends JPanel
        implements ImageVisualizer, ChangeListener
{

    private static final int MAX_SCALE = 2000;

    private Graph graph;
    private JSlider slider;

    class Graph extends JComponent
    {

        private final int GRIDS = 10;

        private int limit;
        private int scale;
        private int counter;
        private Vector samples;
        private Color barColor;

        public Graph()
        {
            super();

            this.samples = new Vector();
            this.barColor = Color.lightGray;
            this.counter = 0;
            this.limit = 1000;
            this.scale = limit / GRIDS;
        }

        public synchronized void setLimit(int limit)
        {
            this.limit = limit;
            this.scale = limit / GRIDS;
        }

        public void add(int sample)
        {
            this.samples.addElement(new Integer(sample));
            this.counter++;
        }

        public void clear()
        {
            this.samples.removeAllElements();
            this.counter = 0;
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Dimension d = this.getSize();

            for (int i = 1, y; i < GRIDS; i++)
            {
                y = d.height - (i * d.height / GRIDS);
                g.setColor(Color.black);
                g.drawLine(5, y, d.width - 35, y);
                g.setColor(Color.blue);
                g.drawString(Integer.toString(i * scale), d.width - 30, y + this.getFont().getSize() / 2);
            }

            int bars = (d.width - 40) / 8;

            if (counter < bars) bars = counter;

            if (this.limit == 0)
            {
                this.limit = 1;
            }

            for (int i = bars; i > 0; i--)
            {
                int sample = ((Integer) this.samples.elementAt(this.counter - i)).intValue()
                        * d.height / this.limit;

                g.setColor(this.barColor);
                g.fill3DRect(d.width - 36 - 8 * i, d.height - sample, 5, sample, true);
            }
        }
    }

    public BarVisualizer()
    {
        super();

        this.graph = new Graph();
        this.slider = new JSlider(JSlider.VERTICAL, 0, MAX_SCALE, 1000);
        this.slider.addChangeListener(this);
        this.slider.setPaintLabels(false);
        this.slider.setPaintTicks(false);

        this.setLayout(new BorderLayout());
        this.add(slider, BorderLayout.WEST);
        this.add(graph, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(500, 100));
    }

    public void stateChanged(ChangeEvent e)
    {
        int limit = this.slider.getValue();

        this.graph.setLimit(limit);
        repaint();
    }

    public synchronized void add(SampleResult sampleResult)
    {
        long s = sampleResult.getTime();

        this.graph.add((int) s);
        repaint();
    }

    public synchronized void clear()
    {
        this.graph.clear();
        repaint();
    }

    public String toString()
    {
        return "Show the samples as scrolling bars";
    }

    public JPanel getControlPanel()
    {
        return this;
    }

    public Image getImage()
    {
        Image result = graph.createImage(graph.getWidth(), graph.getHeight());

        graph.paintComponent(result.getGraphics());

        return result;
    }
}
