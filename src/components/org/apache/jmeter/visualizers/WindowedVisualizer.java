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

import org.apache.jmeter.samplers.SampleResult;

/**
 * This class implements a statistical analyser that
 * calculates both the average and the standard deviation
 * of the sampling process and outputs them as autoscaling
 * plots. Instead of performing a complete analysis, the values
 * are windowed to keep the analisys overhead to a minimum and to
 * remove sampling noise from the initial values.
 *
 * @author  <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision$ $Date$
 */
public class WindowedVisualizer extends JPanel implements ImageVisualizer {

	 public class Graph extends JComponent implements Scrollable {

		  private int counter = 0;
		  private int cursor = 0;
		  private int sum = 0;
		  private int window = 100;
		  private int samples = 2000;
		  private int max = 1;
		  private boolean active = true;
		  private int[] sample;
		  private int[] average;

		  public Graph() {
				this.sample = new int[window];
				this.average = new int[window];
				this.setPreferredSize(getPreferredScrollableViewportSize());
		  }

		  public int getWindowSize() {
				return window;
		  }

		  public int setWindowSize(int window) {
				if (window > 0) this.window = window;
				return this.window;
		  }

		  public int getSamples() {
				return samples;
		  }

		  public int setSamples(int samples) {
				if (samples > 0) this.samples = samples;
			return this.samples;
		  }

		  public int add(long sam) {
				if (++counter < samples) {
					 int s = (int) sam;

					if (cursor < window - 1) {
						cursor++;
					} else {
						cursor = 0;
					}

					 sum -= sample[cursor];
					 sample[cursor] = s;
					 sum += s;

					 if (s > max) max = s;

					 average[cursor] = sum / window;
				} else {
					 active = false;
				}

			return average[cursor];
		  }

		  public void clear() {
				sample = new int[window];
				average = new int[window];
				sum = 0;
				cursor = 0;
				counter = 0;
				max = 1;
				active = true;
		  }

		  public void paintComponent(Graphics g) {
				super.paintComponent(g);

				Dimension d = this.getSize();

				for (int i = 0, x = 0; i < window; i++, x++) {
					 int s = (int) (sample[i] * d.height / max);
					 g.setColor(Color.black);
					 g.fillRect(x, d.height - s - 1, 1, 2);
					 int a = (int) (average[i] * d.height / max);
					 g.setColor(Color.blue);
					 g.fillRect(x, d.height - a - 1, 1, 2);
				}
		  }

		  public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(400, 200);
		  }

		  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 1;
		  }

		  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 50;
		  }

		  public boolean getScrollableTracksViewportWidth() {
				return false;
		  }

		  public boolean getScrollableTracksViewportHeight() {
				return true;
		  }
	 }

	 private Graph graph;
	 private JTextField windowField;
	 private JTextField samplesField;
	 private JTextField dataField;
	 private JTextField averageField;
	 private JTextField deviationField;

	 public WindowedVisualizer() {
		  super();

		  graph = new Graph();
		  JScrollPane graphScrollPanel = new JScrollPane(graph);
		  graphScrollPanel.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		  //graphScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		  //graphScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		  JLabel windowLabel = new JLabel("Window");
		  windowField = new JTextField();
		  windowField.setEditable(true);
		windowField.setColumns(5);
		windowField.setText(Integer.toString(this.graph.getWindowSize()));
		  JLabel samplesLabel = new JLabel("Samples");
		  samplesField = new JTextField();
		  samplesField.setEditable(true);
		  samplesField.setColumns(5);
		samplesField.setText(Integer.toString(this.graph.getSamples()));

		  JPanel sidePanel = new JPanel();
		  sidePanel.setLayout(new GridLayout(0, 1));
		  sidePanel.add(windowLabel);
		  sidePanel.add(windowField);
		  sidePanel.add(samplesLabel);
		  sidePanel.add(samplesField);

		  dataField = new JTextField("0000ms");
		  dataField.setEditable(false);
		  dataField.setForeground(Color.black);
		  dataField.setBackground(getBackground());
		  averageField = new JTextField("0000ms");
		  averageField.setEditable(false);
		  averageField.setForeground(Color.blue);
		  averageField.setBackground(getBackground());

		  JPanel showPanel = new JPanel();
		  showPanel.setLayout(new GridLayout(0, 1));
		  showPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		  showPanel.add(dataField);
		  showPanel.add(averageField);

		  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		  setLayout(new BorderLayout());
		  add(sidePanel, BorderLayout.WEST);
		  add(graphScrollPanel, BorderLayout.CENTER);
		  add(showPanel, BorderLayout.EAST);
	 }

	 public synchronized void add(SampleResult sampleResult) {
		long sample = sampleResult.getTime();
		  int average = this.graph.add(sample);

		  dataField.setText(sample + "ms");
		  averageField.setText(average + "ms");

		  repaint();
	 }

	 public synchronized void clear() {

		try {
				windowField.setText(Integer.toString(this.graph.setWindowSize(Integer.parseInt(windowField.getText()))));
		  } catch (Exception ignored) {};

		  try {
				samplesField.setText(Integer.toString(this.graph.setSamples(Integer.parseInt(samplesField.getText()))));
		  } catch (Exception ignored) {};

		  this.graph.clear();

		  dataField.setText("0000ms");
		  averageField.setText("0000ms");

		  repaint();
	 }

	 public String toString() {
		  return "Show the samples analysys as windowed dot plots";
	 }

	 public JPanel getControlPanel() {
	return this;
	 }

	 public Image getImage() {
	Image result = graph.createImage(graph.getWidth(), graph.getHeight());
	graph.paintComponent(result.getGraphics());

	return result;
	 }
}
