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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * This class implements a statistical analyser that takes samples to process a
 * Spline interpolated curve. Currently, it tries to look mostly like the
 * GraphVisualizer.
 *
 * @author    <a href="mailto:norguet@bigfoot.com">Jean-Pierre Norguet</a>
 * @version   $Revision$
 */
public class SplineVisualizer
    extends AbstractVisualizer
    implements ImageVisualizer, GraphListener, Clearable
{

    protected final Color BACKGROUND_COLOR = getBackground();
    protected final Color MINIMUM_COLOR = new Color(0F, 0.5F, 0F);
    protected final Color MAXIMUM_COLOR = new Color(0.9F, 0F, 0F);
    protected final Color AVERAGE_COLOR = new Color(0F, 0F, 0.75F);
    protected final Color INCOMING_COLOR = Color.black;
    protected final int NUMBERS_TO_DISPLAY = 4;

    protected final boolean FILL_UP_WITH_ZEROS = false;

    transient private SplineGraph graph = null;

    private JLabel minimumLabel = null;
    private JLabel maximumLabel = null;
    private JLabel averageLabel = null;
    private JLabel incomingLabel = null;

    private JLabel minimumNumberLabel = null;
    private JLabel maximumNumberLabel = null;
    private JLabel averageNumberLabel = null;
    private JLabel incomingNumberLabel = null;
    transient private SplineModel model;

    public SplineVisualizer()
    {
        super();
        model = new SplineModel();
        graph = new SplineGraph();
        this.model.setListener(this);
        setGUI();
    }

    public void add(SampleResult res)
    {
        model.add(res);
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("spline_visualizer_title");
    }

    public void updateGui(Sample s)
    {
        updateGui();
    }

    public void clear()
    {
        model.clear();
    }

    public void setGUI()
    {
        Color backColor = BACKGROUND_COLOR;

        this.setBackground(backColor);

        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

        // NAME
        mainPanel.add(makeTitlePanel());

        maximumLabel =
            new JLabel(JMeterUtils.getResString("spline_visualizer_maximum"));
        maximumLabel.setForeground(MAXIMUM_COLOR);
        maximumLabel.setBackground(backColor);

        averageLabel =
            new JLabel(JMeterUtils.getResString("spline_visualizer_average"));
        averageLabel.setForeground(AVERAGE_COLOR);
        averageLabel.setBackground(backColor);

        incomingLabel =
            new JLabel(JMeterUtils.getResString("spline_visualizer_incoming"));
        incomingLabel.setForeground(INCOMING_COLOR);
        incomingLabel.setBackground(backColor);

        minimumLabel =
            new JLabel(JMeterUtils.getResString("spline_visualizer_minimum"));
        minimumLabel.setForeground(MINIMUM_COLOR);
        minimumLabel.setBackground(backColor);

        maximumNumberLabel = new JLabel("0 ms");
        maximumNumberLabel.setHorizontalAlignment(JLabel.RIGHT);
        maximumNumberLabel.setForeground(MAXIMUM_COLOR);
        maximumNumberLabel.setBackground(backColor);

        averageNumberLabel = new JLabel("0 ms");
        averageNumberLabel.setHorizontalAlignment(JLabel.RIGHT);
        averageNumberLabel.setForeground(AVERAGE_COLOR);
        averageNumberLabel.setBackground(backColor);

        incomingNumberLabel = new JLabel("0 ms");
        incomingNumberLabel.setHorizontalAlignment(JLabel.RIGHT);
        incomingNumberLabel.setForeground(INCOMING_COLOR);
        incomingNumberLabel.setBackground(backColor);

        minimumNumberLabel = new JLabel("0 ms");
        minimumNumberLabel.setHorizontalAlignment(JLabel.RIGHT);
        minimumNumberLabel.setForeground(MINIMUM_COLOR);
        minimumNumberLabel.setBackground(backColor);

        // description Panel
        JPanel labelPanel = new JPanel();

        labelPanel.setLayout(new GridLayout(0, 1));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        labelPanel.setBackground(backColor);
        labelPanel.add(maximumLabel);
        labelPanel.add(averageLabel);
        if (model.SHOW_INCOMING_SAMPLES)
        {
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
        if (model.SHOW_INCOMING_SAMPLES)
        {
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

    public void updateGui()
    {
        repaint();
        synchronized (this)
        {
            setMinimum(model.getMinimum());
            setMaximum(model.getMaximum());
            setAverage(model.getAverage());
            setIncoming(model.getCurrent());
        }
    }

    public String toString()
    {
        return "Show the samples analysis as a Spline curve";
    }

    public String formatMeasureToDisplay(long measure)
    {
        String numberString = String.valueOf(measure);

        if (FILL_UP_WITH_ZEROS)
        {
            for (int i = numberString.length(); i < NUMBERS_TO_DISPLAY; i++)
            {
                numberString = "0" + numberString;
            }
        }
        return numberString;
    }

    public void setMinimum(long n)
    {
        String text = this.formatMeasureToDisplay(n) + " ms";

        this.minimumNumberLabel.setText(text);
    }

    public void setMaximum(long n)
    {
        String text = this.formatMeasureToDisplay(n) + " ms";

        this.maximumNumberLabel.setText(text);
    }

    public void setAverage(long n)
    {
        String text = this.formatMeasureToDisplay(n) + " ms";

        this.averageNumberLabel.setText(text);
    }

    public void setIncoming(long n)
    {
        String text = this.formatMeasureToDisplay(n) + " ms";

        this.incomingNumberLabel.setText(text);
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

    /**
     * Component showing a Spline curve.
     *
     * @version   $Revision$
     */
    public class SplineGraph extends JComponent
    {
        public boolean reinterpolated = false;
        protected final Color WAITING_COLOR = Color.darkGray;
        protected int lastWidth = -1;
        protected int lastHeight = -1;
        protected int[] plot = null;

        public SplineGraph()
        {
        }

        /**
         * Clear the Spline graph and get ready for the next wave.
         */
        public void clear()
        {
            lastWidth = -1;
            lastHeight = -1;
            plot = null;
            this.repaint();
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Dimension dimension = this.getSize();
            int width = dimension.width;
            int height = dimension.height;

            if (model.getDataCurve() == null)
            {
                g.setColor(this.getBackground());
                g.fillRect(0, 0, width, height);
                g.setColor(WAITING_COLOR);
                g.drawString(
                    JMeterUtils.getResString(
                        "spline_visualizer_waitingmessage"),
                    (width - 120) / 2,
                    height - (height - 12) / 2);
                return;
            }

            boolean resized = true;

            if (width == lastWidth && height == lastHeight)
            {
                // dimension of the SplineGraph is the same
                resized = false;
            }
            else
            {
                // dimension changed
                resized = true;
                lastWidth = width;
                lastHeight = height;
            }

            this.plot = model.getDataCurve().getPlots(width, height); // rounds!

            int n = plot.length;
            int curY = plot[0];

            for (int i = 1; i < n; i++)
            {
                g.setColor(Color.black);
                g.drawLine(i - 1, height - curY - 1, i, height - plot[i] - 1);
                curY = plot[i];
            }
        }
    }
}
