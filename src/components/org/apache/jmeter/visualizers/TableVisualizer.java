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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * This class implements a statistical analyser that calculates both the average
 * and the standard deviation of the sampling process. The samples are displayed
 * in a JTable, and the statistics are displayed at the bottom of the table.
 *
 *@author    <a href="mailto:alf@i100.no">Alf Hogemark</a>
 *@created   March 10, 2002
 *@version   $Revision$
 ***************************************/
public class TableVisualizer extends AbstractVisualizer
		 implements GraphListener, Clearable
{
	private TableDataModel model = null;
	private JTable table = null;
	private JTextField dataField = null;
	private JTextField averageField = null;
	private JTextField deviationField = null;
	private JTextField noSamplesField = null;
	private JScrollPane tableScrollPanel = null;


	/****************************************
	 * Constructor for the TableVisualizer object
	 ***************************************/
	public TableVisualizer()
	{
		super();
		model = new TableDataModel();
		model.addGraphListener(this);
		init();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("view_results_in_table");
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void updateGui()
	{
		// Not completely sure if this is the correct way of updating the table
		table.tableChanged(new TableModelEvent(model));
		tableScrollPanel.revalidate();
		tableScrollPanel.repaint();
		noSamplesField.setText(Long.toString(model.getSampleCount()));
		dataField.setText(Long.toString(model.getCurrentData()));
		averageField.setText(Long.toString(model.getCurrentAverage()));
		deviationField.setText(Long.toString(model.getCurrentDeviation()));
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
	 * !ToDo (Method description)
	 *
	 *@param s  !ToDo (Parameter description)
	 ***************************************/
	public void updateGui(Sample s)
	{
		// We have received one more sample
		// Not completely sure if this is the correct way of updating the table
		table.tableChanged(new TableModelEvent(model));
		tableScrollPanel.revalidate();
		tableScrollPanel.repaint();
		noSamplesField.setText(Long.toString(model.getSampleCount()));
		dataField.setText(Long.toString(model.getCurrentData()));
		averageField.setText(Long.toString(model.getCurrentAverage()));
		deviationField.setText(Long.toString(model.getCurrentDeviation()));
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
		repaint();
	}

	/****************************************
	 * Description of the Method
	 *
	 *@return   Description of the Returned Value
	 ***************************************/
	public String toString()
	{
		return "Show the samples in a table";
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
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("view_results_in_table"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		mainPanel.add(getNamePanel());
		mainPanel.add(getFilePanel());

		// Set up the table itself
		table = new JTable(model);
		//table.getTableHeader().setReorderingAllowed(false);
		tableScrollPanel = new JScrollPane(table);
		tableScrollPanel.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set up footer of table which displays numerics of the graphs
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
		JPanel noSamplesPanel = new JPanel();
		JLabel noSamplesLabel = new JLabel(JMeterUtils.getResString("graph_results_no_samples"));
		noSamplesField = new JTextField(10);
		noSamplesField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		noSamplesField.setEditable(false);
		noSamplesField.setForeground(Color.black);
		noSamplesField.setBackground(getBackground());
		noSamplesPanel.add(noSamplesLabel);
		noSamplesPanel.add(noSamplesField);

		JPanel tableInfoPanel = new JPanel();
		tableInfoPanel.setLayout(new FlowLayout());
		tableInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		tableInfoPanel.add(noSamplesPanel);
		tableInfoPanel.add(dataPanel);
		tableInfoPanel.add(averagePanel);
		tableInfoPanel.add(deviationPanel);

		// Set up the table with footer
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(tableScrollPanel, BorderLayout.CENTER);
		tablePanel.add(tableInfoPanel, BorderLayout.SOUTH);

		// Add the main panel and the graph
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(tablePanel, BorderLayout.CENTER);
	}

}
