/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2001 The Apache Software Foundation.  All rights
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
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/**
 *  Draws the graph
 *
 *@author     Khor Soon Hin
 *@created    2001/08/11
 *@version    1.0
 */

public class GraphAccum extends JComponent implements Scrollable,
		GraphAccumListener
{
	/**
	 *  Description of the Field
	 */
	protected GraphAccumModel model;
	/**
	 *  Description of the Field
	 */
	protected GraphAccumVisualizer visualizer;
	// how far from each other to plot
	// the points
	/**
	 *  Description of the Field
	 */
	protected boolean noLegendYet = true;
	// ensure that the legends are only
	// drawn once
	/**
	 *  Description of the Field
	 */
	protected Point[] previousPts;
	// keep track of previous point
	// needed to draw a line joining
	// the previous point with the current
	// one
	/**
	 *  Description of the Field
	 */
	protected boolean previousPtsAlloc = false;
	/**
	 *  Description of the Field
	 */
	protected static int width = 2000;
	/**
	 *  Description of the Field
	 */
	protected final static int PLOT_X_WIDTH = 10;
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");

	// Ensure that previousPts is allocated

	// once only.  It'll be reused at each
	// drawSample.  It can't be allocated
	// outside drawSample 'cos the sample
	// is only passed in here.

	/**
	 *  Constructor
	 */
	public GraphAccum()
	{
		log.debug("Start : GraphAnnum1");
		this.setPreferredSize(new Dimension(width, 800));
		log.debug("End : GraphAnnum1");
	}


	/**
	 *  Constructor with model set
	 *
	 *@param  model  Description of Parameter
	 */
	public GraphAccum(GraphAccumModel model)
	{
		this();
		log.debug("Start : GraphAnnum2");
		setModel(model);
		log.debug("End : GraphAnnum2");
	}


	/**
	 *  Set model which this object represents
	 *
	 *@param  model  model which this object represents
	 */
	private void setModel(Object model)
	{
		log.debug("Start : setModel1");
		this.model = (GraphAccumModel) model;
		this.model.addGraphAccumListener(this);
		repaint();
		log.debug("End : setModel1");
	}


	/**
	 *  Set the visualizer
	 *
	 *@param  visualizer  visualizer of this object
	 */
	public void setVisualizer(Object visualizer)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setVisualizer1 : Setting visualizer - " + visualizer);
		}
		this.visualizer = (GraphAccumVisualizer) visualizer;
	}


	/**
	 *  The legend is only printed once during sampling. This sets the variable
	 *  that indicates whether the legend has been printed yet or not.
	 *
	 *@param  value  variable that indicates whether the legend has been printed
	 *      yet
	 */
	public void setNoLegendYet(boolean value)
	{
		noLegendYet = value;
	}


	/**
	 *  Gets the PreferredScrollableViewportSize attribute of the Graph object
	 *
	 *@return    The PreferredScrollableViewportSize value
	 */
	public Dimension getPreferredScrollableViewportSize()
	{
		return this.getPreferredSize();
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
		return false;
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
	 *  The legend is only printed once during sampling. This returns the variable
	 *  that indicates whether the legend has been printed yet or not.
	 *
	 *@return    value variable that indicates whether the legend has been printed
	 *      yet
	 */
	public boolean getNoLegendYet()
	{
		return noLegendYet;
	}


	/**
	 *  Redraws the gui
	 */
	public void updateGui()
	{
		log.debug("Start : updateGui1");
		repaint();
		log.debug("End : updateGui1");
	}


	/**
	 *  Redraws the gui if no rescaling of the graph is needed
	 *
	 *@param  oneSample  sample to be added
	 */
	public void updateGui(final SampleResult oneSample)
	{
		log.debug("Start : updateGui2");
		final int xPos = model.getSampleCount();
		SwingUtilities.invokeLater(
			new Runnable()
			{
				public void run()
				{
					Graphics g = getGraphics();
					if (g != null)
					{
						drawSample(xPos * PLOT_X_WIDTH, oneSample, g);
					}
				}
			}
				);
		log.debug("End : updateGui2");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of Parameter
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		log.debug("Start : paintComponent1");
		Dimension d = this.getSize();
		synchronized (model.getList())
		{
			// for repainting set this to false because all the points needs to be redrawn
			// so no need(shouldn't) use the previousPts
			previousPtsAlloc = false;
			Iterator e = model.getList().iterator();
			for (int i = 0; e.hasNext(); i++)
			{
				SampleResult s = (SampleResult) e.next();
				drawSample(i * PLOT_X_WIDTH, s, g);
			}
		}
		log.debug("End : paintComponent1");
	}


	/**
	 *  Clears this graph
	 */
	public void clear()
	{
		setNoLegendYet(true);
		((JPanel) visualizer.getWhiteCanvas()).removeAll();
		previousPts = null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x          Description of Parameter
	 *@param  oneSample  Description of Parameter
	 *@param  g          Description of Parameter
	 */
	private void drawSample(int x, SampleResult oneSample, Graphics g)
	{
		log.debug("Start : drawSample1");
		int lastLevel = 0;
		// used to keep track of accumulated load times of
		// components
		int compCount = 0;
		// number of components

		SampleResult[] resultList = oneSample.getSubResults();
		// allocate previousPts only the first time
		int resultListCount = 0;
		if (!previousPtsAlloc)
		{
			if (resultList != null)
			{
				resultListCount += resultList.length;
			}
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
		// set the total time to load the sample
		long totalTime = oneSample.getTime();
		// if the page has other components then set the total time to be that including
		// all its components' load time
		if (log.isDebugEnabled())
		{
			log.debug("drawSample1 : total time - " + totalTime);
		}
		int data = (int) (totalTime * d.height / model.getMax());
		g.setColor(currColor);
		if (!previousPtsAlloc)
		{
			// if first dot, just draw the point
			g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
		} else
		{
			// otherwise, draw from previous point
			g.drawLine((previousPts[0].x) % width, previousPts[0].y, x % width,
					d.height - data);
		}
		// store current total time point
		previousPts[0] = new Point(x % width, d.height - data);
		if (legendPanel != null && noLegendYet)
		{
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
		// plot the time of the page itself without all its components
		if (log.isDebugEnabled())
		{
			log.debug("drawSample1 : main page load time - "
					 + oneSample.getTime());
		}
		data = (int) (oneSample.getTime()
				 * d.height / model.getMax());
		currColor = ColorHelper.changeColorCyclicIncrement(currColor, 40);
		g.setColor(currColor);
		if (!previousPtsAlloc)
		{
			// if first dot, just draw the point
			g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
		} else
		{
			// otherwise, draw from previous point
			g.drawLine((previousPts[1].x) % width, previousPts[1].y, x % width,
					d.height - data);
		}
		// store load time without components
		previousPts[1] = new Point(x % width, d.height - data);
		if (legendPanel != null && noLegendYet)
		{
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
		// plot the times of the total times components
		int currPreviousPts = 2;
		if (resultList != null)
		{
			for(int i = 0;i < resultList.length;i++)
			{
				SampleResult componentRes = (SampleResult) resultList[i];
				if (log.isDebugEnabled())
				{
					log.debug("drawSample1 : componentRes - " +
							componentRes.getSampleLabel() + " loading time - " +
							componentRes.getTime());
				}
				data = (int) (componentRes.getTime()
						 * d.height / model.getMax());
				data += lastLevel;
				currColor = ColorHelper.changeColorCyclicIncrement(currColor, 100);
				g.setColor(currColor);
				if (!previousPtsAlloc)
				{
					// if first dot, just draw the point
					g.drawLine(x % width, d.height - data, x % width, d.height - data - 1);
				} else
				{
					// otherwise, draw from previous point
					g.drawLine((previousPts[currPreviousPts].x) % width,
							previousPts[currPreviousPts].y, x % width, d.height - data);
				}
				// store the current plot
				previousPts[currPreviousPts++] = new Point(x % width, d.height - data);
				if (legendPanel != null && noLegendYet)
				{
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
		if (noLegendYet)
		{
			noLegendYet = false;
			lPanel.repaint();
			lPanel.revalidate();
		}
		// set the previousPtsAlloc to true here and not after
		// allocation because the rest of the codes also depend
		// on previousPtsAlloc to be false if first time plotting
		// the graph i.e. there are no previous points
		if (!previousPtsAlloc)
		{
			previousPtsAlloc = true;
		}
		log.debug("End : drawSample1");
	}
}
