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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.gui.util.JMeterColor;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * This class implements a statistical analyser that calculates both the average
 * and the standard deviation of the sampling process and outputs them as
 * autoscaling plots.
 *
 *@author    <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *@created   February 8, 2001
 *@version   $Revision$ $Date$
 ***************************************/
public class GraphVisualizer extends AbstractVisualizer
		 implements ImageVisualizer, ItemListener, GraphListener,Clearable
{
	GraphModel model;
	private JTextField maxYField = null;
	private JTextField minYField = null;
	private JTextField noSamplesField = null;
	String minute = JMeterUtils.getResString("minute");

	private Graph graph;
	private JCheckBox data;
	private JCheckBox average;
	private JCheckBox deviation;
	private JCheckBox throughput;
	private JTextField dataField;
	private JTextField averageField;
	private JTextField deviationField;
	private JTextField throughputField;
	private boolean perSecond = false;


	/****************************************
	 * Constructor for the GraphVisualizer object
	 ***************************************/
	public GraphVisualizer()
	{
		model = new GraphModel();
		model.addGraphListener(this);
		graph = new Graph(model);
		init();
	}


	/****************************************
	 * Gets the Image attribute of the GraphVisualizer object
	 *
	 *@return   The Image value
	 ***************************************/
	public Image getImage()
	{
		Image result = graph.createImage(graph.getWidth(), graph.getHeight());
		graph.paintComponent(result.getGraphics());

		return result;
	}


	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void updateGui()
	{
		graph.updateGui();
		noSamplesField.setText(Long.toString(model.getSampleCount()));
		dataField.setText(Long.toString(model.getCurrentData()));
		averageField.setText(Long.toString(model.getCurrentAverage()));
		deviationField.setText(Long.toString(model.getCurrentDeviation()));
		throughputField.setText(Float.toString(model.getCurrentThroughput())+"/"+minute);
		updateYAxis();
	}


	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param s  !ToDo (Parameter description)
	 ***************************************/
	public void updateGui(Sample s)
	{
		// We have received one more sample
		graph.updateGui(s);
		noSamplesField.setText(Long.toString(model.getSampleCount()));
		dataField.setText(Long.toString(s.data));
		averageField.setText(Long.toString(s.average));
		deviationField.setText(Long.toString(s.deviation));
		throughputField.setText(Float.toString(s.throughput)+"/"+minute);
		updateYAxis();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param res  !ToDo (Parameter description)
	 ***************************************/
	public void add(SampleResult res)
	{
		model.addSample(res);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("graph_results_title");
	}

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getItem() == data)
		{
			this.graph.enableData(e.getStateChange() == ItemEvent.SELECTED);
		}
		else if(e.getItem() == average)
		{
			this.graph.enableAverage(e.getStateChange() == ItemEvent.SELECTED);
		}
		else if(e.getItem() == deviation)
		{
			this.graph.enableDeviation(e.getStateChange() == ItemEvent.SELECTED);
		}
		else if(e.getItem() == throughput)
		{
			this.graph.enableThroughput(e.getStateChange() == ItemEvent.SELECTED);
		}
		this.graph.repaint();
	}


	/****************************************
	 * Description of the Method
	 ***************************************/
	public synchronized void clear()
	{
		//this.graph.clear();
		model.clear();
		dataField.setText("0000");
		averageField.setText("0000");
		deviationField.setText("0000");
		throughputField.setText("0/"+minute);
		updateYAxis();
		repaint();
	}


	/****************************************
	 * Description of the Method
	 *
	 *@return   Description of the Returned Value
	 ***************************************/
	public String toString()
	{
		return "Show the samples analysis as dot plots";
	}


	/****************************************
	 * Update the max and min value of the Y axis
	 ***************************************/
	private void updateYAxis()
	{
		maxYField.setText(Long.toString(model.getGraphMax()));
		minYField.setText("0");
	}


	/****************************************
	 * Description of the Method
	 ***************************************/
	private void init()
	{
		this.setLayout(new BorderLayout());

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("graph_results_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());
		mainPanel.add(this.getFilePanel());

		// Set up panel where user can choose which graphs to display
		JPanel chooseGraphsPanel = new JPanel();
		chooseGraphsPanel.setLayout(new FlowLayout());
		JLabel selectGraphsLabel = new JLabel(JMeterUtils.getResString("graph_choose_graphs"));
		data = new JCheckBox(JMeterUtils.getResString("graph_results_data"));
		data.setSelected(true);
		data.addItemListener(this);
		data.setForeground(Color.black);
		average = new JCheckBox(JMeterUtils.getResString("graph_results_average"));
		average.setSelected(true);
		average.addItemListener(this);
		average.setForeground(Color.blue);
		deviation = new JCheckBox(JMeterUtils.getResString("graph_results_deviation"));
		deviation.setSelected(true);
		deviation.addItemListener(this);
		deviation.setForeground(Color.red);
		throughput = new JCheckBox(JMeterUtils.getResString("graph_results_throughput"));
		throughput.setSelected(true);
		throughput.addItemListener(this);
		throughput.setForeground(JMeterColor.dark_green);

		chooseGraphsPanel.add(selectGraphsLabel);
		chooseGraphsPanel.add(data);
		chooseGraphsPanel.add(average);
		chooseGraphsPanel.add(deviation);
		chooseGraphsPanel.add(throughput);

		// Set up the graph itself
		JScrollPane graphScrollPanel = new JScrollPane(graph, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		graphScrollPanel.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//graphScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//graphScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);


		// Set up Y axis panel
		JPanel graphYAxisPanel = new JPanel();
		graphYAxisPanel.setLayout(new BorderLayout());
		JPanel maxYPanel = new JPanel(new FlowLayout());
		JLabel maxYLabel = new JLabel(JMeterUtils.getResString("graph_results_ms"));
		maxYField = new JTextField(5);
		maxYField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		maxYField.setEditable(false);
		maxYField.setForeground(Color.black);
		maxYField.setBackground(getBackground());
		maxYField.setHorizontalAlignment(JTextField.RIGHT);
		maxYPanel.add(maxYField);
		maxYPanel.add(maxYLabel);
		JPanel minYPanel = new JPanel(new FlowLayout());
		JLabel minYLabel = new JLabel(JMeterUtils.getResString("graph_results_ms"));
		minYField = new JTextField(3);
		minYField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		minYField.setEditable(false);
		minYField.setForeground(Color.black);
		minYField.setBackground(getBackground());
		minYField.setHorizontalAlignment(JTextField.RIGHT);
		minYPanel.add(minYField);
		minYPanel.add(minYLabel);
		graphYAxisPanel.add(maxYPanel, BorderLayout.NORTH);
		graphYAxisPanel.add(minYPanel, BorderLayout.SOUTH);

		// Set up footer of graph which displays numerics of the graphs
		JPanel dataPanel = new JPanel();
		JLabel dataLabel = new JLabel(JMeterUtils.getResString("graph_results_latest_sample"));
		dataLabel.setForeground(Color.black);
		dataField = new JTextField(5);
		dataField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		dataField.setEditable(false);
		dataField.setForeground(Color.black);
		dataField.setBackground(getBackground());
		dataPanel.add(dataLabel);
		dataPanel.add(dataField);
		JPanel averagePanel = new JPanel();
		JLabel averageLabel = new JLabel(JMeterUtils.getResString("graph_results_average"));
		averageLabel.setForeground(Color.blue);
		averageField = new JTextField(5);
		averageField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		averageField.setEditable(false);
		averageField.setForeground(Color.blue);
		averageField.setBackground(getBackground());
		averagePanel.add(averageLabel);
		averagePanel.add(averageField);
		JPanel deviationPanel = new JPanel();
		JLabel deviationLabel = new JLabel(JMeterUtils.getResString("graph_results_deviation"));
		deviationLabel.setForeground(Color.red);
		deviationField = new JTextField(5);
		deviationField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		deviationField.setEditable(false);
		deviationField.setForeground(Color.red);
		deviationField.setBackground(getBackground());
		deviationPanel.add(deviationLabel);
		deviationPanel.add(deviationField);
		JPanel throughputPanel = new JPanel();
		JLabel throughputLabel = new JLabel(JMeterUtils.getResString("graph_results_throughput"));
		throughputLabel.setForeground(JMeterColor.dark_green);
		throughputField = new JTextField(15);
		throughputField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		throughputField.setEditable(false);
		throughputField.setForeground(JMeterColor.dark_green);
		throughputField.setBackground(getBackground());
		throughputPanel.add(throughputLabel);
		throughputPanel.add(throughputField);
		JPanel noSamplesPanel = new JPanel();
		JLabel noSamplesLabel = new JLabel(JMeterUtils.getResString("graph_results_no_samples"));
		noSamplesField = new JTextField(6);
		noSamplesField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		noSamplesField.setEditable(false);
		noSamplesField.setForeground(Color.black);
		noSamplesField.setBackground(getBackground());
		noSamplesPanel.add(noSamplesLabel);
		noSamplesPanel.add(noSamplesField);

		JPanel graphInfoPanel = new JPanel();
		graphInfoPanel.setLayout(new GridLayout(2,3));
		graphInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		graphInfoPanel.add(noSamplesPanel);
		graphInfoPanel.add(dataPanel);
		graphInfoPanel.add(averagePanel);
		graphInfoPanel.add(deviationPanel);
		graphInfoPanel.add(throughputPanel);

		// Set up the graph with header, footer, Y axis and graph display
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(graphYAxisPanel, BorderLayout.WEST);
		graphPanel.add(chooseGraphsPanel, BorderLayout.NORTH);
		graphPanel.add(graphScrollPanel, BorderLayout.CENTER);
		graphPanel.add(graphInfoPanel, BorderLayout.SOUTH);

		// Add the main panel and the graph
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(graphPanel, BorderLayout.CENTER);
	}
}
