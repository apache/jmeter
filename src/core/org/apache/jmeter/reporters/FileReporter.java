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

package org.apache.jmeter.reporters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * This class loads data from a saved file and displays
 * statistics about it.
 *
 *
 * @author  Tom Schneider
 */
public class FileReporter extends JPanel  {
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	Hashtable data = new Hashtable();
	/**  initalize a file reporter from a file */
	public void init(String file) throws IOException {
		File datafile = new File(file);
		BufferedReader reader = null;
		if( datafile.canRead() ) {
			reader = new BufferedReader(new FileReader(datafile));
		} else {
			JOptionPane.showMessageDialog(
				null,"The file you specified cannot be read.",
				"Information",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				line.trim();
				if (line.startsWith("#") || line.length() == 0)
					continue;
				int splitter = line.lastIndexOf(' ');
				String key = line.substring(0, splitter);
				int len = line.length() - 1;
				Integer value = null;
				if (line.charAt(len) == ',') {
					value = new Integer(
						line.substring(splitter + 1, len));
				} else {
					value = new Integer(
						line.substring(splitter + 1));
				}
				Vector v = getData(key);
				if (v == null) {
					v = new Vector();
					this.data.put(key, v);
				}
				v.addElement(value);
			} catch (NumberFormatException nfe) {
				log.error("This line could not be parsed: "
					+ line,nfe);
			} catch (Exception e) {
				log.error("This line caused a problem: "+line,e);
			}
		}
		reader.close();
		showPanel();
	}
	public Vector getData(String key) {
		return (Vector) data.get(key);
	}
	/**  show main panel with length, graph, and stats */
	public void showPanel() {
		JFrame f = new JFrame("Data File Report");
		setLayout(new BorderLayout());
		graphPanel gp = new graphPanel(data);
		add(gp, "Center");
		add(gp.getStats(), BorderLayout.EAST);
		add(gp.getLegend(), BorderLayout.NORTH);
		f.setSize(500, 300);
		f.getContentPane().add(this);
		f.show();
	}
}

/**  Graph panel generates all the panels for this reporter.
  *  Data is organized based on thread name in a hashtable.
  *  The data itself is a Vector of Integer objects
  */
class graphPanel extends JPanel {
	boolean autoScale = true;
	Hashtable data;
	Vector keys = new Vector();
	Vector colorList = new Vector();
	public graphPanel(Hashtable data) {
		this.data = data;
		Enumeration e = data.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			keys.addElement(key);
		}
		for (int a = 0x33; a < 0xFF; a += 0x66) {
			for (int b = 0x33; b < 0xFF; b += 0x66) {
				for (int c = 0x33; c < 0xFF; c += 0x66) {
					colorList.addElement(new Color(a, b, c));
				}
			}
		}
	}
	/**  get the maximum for all the data */
	public float getMax() {
		float maxValue = 0;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			Vector temp = (Vector) data.get(key);
			for (int j = 0; j < temp.size(); j++) {
				float f = ((Integer) temp.elementAt(j)).intValue();
				maxValue = Math.max(f, maxValue);
			}
		}
		return (float) (maxValue + maxValue * 0.1);
	}
	/**  get the minimum for all the data */
	public float getMin() {
		float minValue = 9999999;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			Vector temp = (Vector) data.get(key);
			for (int j = 0; j < temp.size(); j++) {
				float f = ((Integer) temp.elementAt(j)).intValue();
				minValue = Math.min(f, minValue);
			}
		}
		return (float) (minValue - minValue * 0.1);
	}
	/**  get the legend panel */
	public JPanel getLegend() {
		JPanel main = new JPanel();
		GridBagLayout g = new GridBagLayout();
		main.setLayout(g);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3);
		c.fill = c.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			JLabel colorSwatch = new JLabel("  ");
			colorSwatch.setBackground((Color) colorList.elementAt(t % colorList.size()));
			colorSwatch.setOpaque(true);
			c.gridx = 1;
			c.gridy = t;
			g.setConstraints(colorSwatch, c);
			main.add(colorSwatch);
			JLabel name = new JLabel(key);
			c.gridx = 2;
			c.gridy = t;
			g.setConstraints(name, c);
			main.add(name);
		}
		return main;
	}
	/**  get the stats panel */
	public JPanel getStats() {
		int total = 0;
		float totalValue = 0;
		float maxValue = 0;
		float minValue = 999999;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			Vector temp = (Vector) data.get(key);
			for (int j = 0; j < temp.size(); j++) {
				float f = ((Integer) temp.elementAt(j)).intValue();
				minValue = Math.min(f, minValue);
				maxValue = Math.max(f, maxValue);
				totalValue += f;
				total++;
			}
		}
		float averageValue = totalValue / total;
		JPanel main = new JPanel();
		GridBagLayout g = new GridBagLayout();
		main.setLayout(g);
		DecimalFormat df = new DecimalFormat("#0.0");
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,6,3,6);
		c.fill = c.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		JLabel count = new JLabel("Count: " + total);
		c.gridx = 1;
		c.gridy = 1;
		g.setConstraints(count, c);
		JLabel min = new JLabel("Min: " + df.format(new Float(minValue)));
		c.gridx = 1;
		c.gridy = 2;
		g.setConstraints(min, c);
		JLabel max = new JLabel("Max: " + df.format(new Float(maxValue)));
		c.gridx = 1;
		c.gridy = 3;
		g.setConstraints(max, c);
		JLabel average = new JLabel("Average: " + df.format(new Float(averageValue)));
		c.gridx = 1;
		c.gridy = 4;
		g.setConstraints(average, c);
		main.add(count);
		main.add(min);
		main.add(max);
		main.add(average);
		return main;
	}
	/**  gets the size of the biggest Vector */
	public int getDataWidth() {
		int size = 0;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			Vector v = (Vector) data.get(key);
			size = Math.max(size, v.size());
		}
		return size;
	}
	/**  draws the graph */
	public void update(Graphics g) {
		// setup drawing area
	 int base = 10;
		g.setColor(Color.white);
		g.fillRect(0, 0, getSize().width, getSize().height);
		int width = getSize().width;
		int height = getSize().height;
		float maxValue = getMax();
		float minValue = getMin();
		// draw grid
		g.setColor(Color.gray);
		int dataWidth = getDataWidth();
		int increment = Math.round((width - 1) / (dataWidth - 1));
		/*for (int t = 0; t < dataWidth; t++) {
			g.drawLine(t * increment, 0, t * increment, height);
		}*/
		int yIncrement = Math.round(((float) height - (1+base)) / (10 - 1));
		/*for (int t = 0; t < 10; t++) {
			g.drawLine(0, height - t * yIncrement, width, height - t * yIncrement);
		}*/
		// draw axis
		for (int t = 1; t < dataWidth; t+=(dataWidth/25+1)) {
			g.drawString((new Integer(t)).toString(), t * increment + 2, height - 2);
		}
		float incrementValue = (maxValue - minValue) / (10 - 1);
		for (int t = 0; t < 10; t++) {
			g.drawString(new Integer(Math.round(minValue + (t * incrementValue))).toString(), 2,height - t * yIncrement - 2 - base);
		}
		// draw data lines
		int start = 0;
		for (int t = 0; t < keys.size(); t++) {
			String key = (String) keys.elementAt(t);
			Vector v = (Vector) data.get(key);
			start = 0;
			g.setColor((Color) colorList.elementAt(t % colorList.size()));
			for (int i = 0; i < v.size() - 1; i++) {
				float y1 = (float) ((Integer) v.elementAt(i)).intValue();
				float y2 = (float) ((Integer) v.elementAt(i + 1)).intValue();
				y1 = y1 - minValue;
				y2 = y2 - minValue;
				int Y1 = Math.round((height * y1) / (maxValue - minValue));
				int Y2 = Math.round((height * y2) / (maxValue - minValue));
				Y1 = height - Y1 - base;
				Y2 = height - Y2 - base;
							g.drawLine(start, Y1, start + increment, Y2);
				Integer value = (Integer) v.elementAt(i);
				start += increment;
			}
		}
	}
	public void paint(Graphics g) {
		update(g);
	}
}
