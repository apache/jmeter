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
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.gui.layout.VerticalLayout;
/****************************************
 * This class implements a statistical analyser that plots the accumulated time
 * taken to load each set of pages. The number of plots is equivalent to the
 * number of times the set of pages is configured to load.
 *
 *@author    Khor Soon Hin
 *@created   2001/08/11
 *@version   $Revision$ $Date$
 ***************************************/
public class GraphAccumVisualizer extends AbstractVisualizer
		 implements ImageVisualizer, GraphAccumListener,Clearable
{

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected transient GraphAccumModel model;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected transient GraphAccum graph;

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	transient protected JPanel legendPanel;
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");


	/****************************************
	 * Constructor
	 ***************************************/
	public GraphAccumVisualizer()
	{
		super();
		model = new GraphAccumModel();
		model.addGraphAccumListener(this);
		init();
		log.debug("Start : GraphAccumVisualizer1");
		log.debug("End : GraphAccumVisualizer1");
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("graph_full_results_title");
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param res  !ToDo (Parameter description)
	 ***************************************/
	public void add(SampleResult res)
	{
		model.addNewSample(res);
	}

	/****************************************
	 * Returns the panel where labels can be added
	 *
	 *@return    !ToDo (Return description)
	 *@returns   a panel where labels can be added
	 ***************************************/
	public Object getWhiteCanvas()
	{
		return legendPanel;
	}


	/****************************************
	 * Gets the Image attribute of the GraphVisualizer object
	 *
	 *@return   The Image value
	 ***************************************/
	public Image getImage()
	{
		log.debug("Start : getImage1");
		Image result = graph.createImage(graph.getWidth(), graph.getHeight());
		graph.paintComponent(result.getGraphics());
		log.debug("End : getImage1");
		return result;
	}


	/****************************************
	 * Updates the gui to reflect changes
	 ***************************************/
	public void updateGui()
	{
		log.debug("Start : updateGui1");
		graph.updateGui();
		log.debug("End : updateGui1");
	}


	/****************************************
	 * Updates gui to reflect small changes
	 *
	 *@param s  sample to be added to plot
	 ***************************************/
	public void updateGui(SampleResult s)
	{
		log.debug("Start : updateGui2");
		log.debug("End : updateGui2");
	}


	/****************************************
	 * Clear this visualizer data
	 ***************************************/
	public synchronized void clear()
	{
		model.clear();
		graph.clear();
		log.debug("Start : clear1");
		repaint();
		log.debug("End : clear1");
	}


	/****************************************
	 * Returns a description of this instance
	 *
	 *@return   description of this instance
	 ***************************************/
	public String toString()
	{
		String toString = "Show the samples analysys as dot plots";
		log.debug("toString1 : Returning - " + toString);
		return toString;
	}


	/****************************************
	 * Setup all the swing components
	 ***************************************/
	private void init()
	{
		log.debug("Start : init1");
		graph = new GraphAccum(model);
		graph.setVisualizer(this);

		this.setLayout(new BorderLayout());

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("graph_full_results_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		mainPanel.add(this.getNamePanel());
		mainPanel.add(getFilePanel());

		JScrollPane graphScrollPanel = new
				JScrollPane(graph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		graphScrollPanel.setViewportBorder(
				BorderFactory.createEmptyBorder(2, 2, 2, 2));
		legendPanel = new JPanel();

		JScrollPane legendScrollPanel = new JScrollPane(legendPanel);
		JSplitPane graphSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				graphScrollPanel, legendScrollPanel);

		this.add(mainPanel, BorderLayout.NORTH);
		this.add(graphSplitPane, BorderLayout.CENTER);
		log.debug("End : init1");
	}
}
