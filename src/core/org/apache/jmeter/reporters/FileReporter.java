/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This class loads data from a saved file and displays statistics about it.
 *
 *
 * @version $Revision$
 */
public class FileReporter extends JPanel {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Map<String, List<Integer>> data = new ConcurrentHashMap<String, List<Integer>>();

    /** initalize a file reporter from a file */
    public void init(String file) throws IOException {
        File datafile = new File(file);
        BufferedReader reader = null;

        try {
            if (datafile.canRead()) {
                reader = new BufferedReader(new FileReader(datafile));
            } else {
                JOptionPane.showMessageDialog(null, "The file you specified cannot be read.", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.startsWith("#") || line.length() == 0) {
                        continue;
                    }
                    int splitter = line.lastIndexOf(' ');
                    String key = line.substring(0, splitter);
                    int len = line.length() - 1;
                    Integer value = null;

                    if (line.charAt(len) == ',') {
                        value = Integer.valueOf(line.substring(splitter + 1, len));
                    } else {
                        value = Integer.valueOf(line.substring(splitter + 1));
                    }
                    List<Integer> v = getData(key);

                    if (v == null) {
                        v = Collections.synchronizedList(new ArrayList<Integer>());
                        this.data.put(key, v);
                    }
                    v.add(value);
                } catch (NumberFormatException nfe) {
                    log.error("This line could not be parsed: " + line, nfe);
                } catch (Exception e) {
                    log.error("This line caused a problem: " + line, e);
                }
            }
        } finally {
        	JOrphanUtils.closeQuietly(reader);
        }
        showPanel();
    }

    public List<Integer> getData(String key) {
        return data.get(key);
    }

    /**
     * Show main panel with length, graph, and stats.
     */
    public void showPanel() {
        JFrame f = new JFrame("Data File Report");

        setLayout(new BorderLayout());
        GraphPanel gp = new GraphPanel(data);

        add(gp, "Center");
        add(gp.getStats(), BorderLayout.EAST);
        add(gp.getLegend(), BorderLayout.NORTH);
        f.setSize(500, 300);
        f.getContentPane().add(this);
        f.setVisible(true);
    }

/**
 * Graph panel generates all the panels for this reporter. Data is organized
 * based on thread name in a hashtable. The data itself is a Vector of Integer
 * objects
 */
private static class GraphPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    // boolean autoScale = true;
    private final Map<String, List<Integer>> data;

    private final List<String> keys = Collections.synchronizedList(new ArrayList<String>());

    private final List<Color> colorList = Collections.synchronizedList(new ArrayList<Color>());

    public GraphPanel(Map<String, List<Integer>> data) {
        this.data = data;
        for (String key : data.keySet()) {
            keys.add(key);
        }
        for (int a = 0x33; a < 0xFF; a += 0x66) {
            for (int b = 0x33; b < 0xFF; b += 0x66) {
                for (int c = 0x33; c < 0xFF; c += 0x66) {
                    colorList.add(new Color(a, b, c));
                }
            }
        }
    }

    /**
     * Get the maximum for all the data.
     */
    public float getMax() {
        float maxValue = 0;

        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            List<Integer> temp = data.get(key);

            for (int j = 0; j < temp.size(); j++) {
                float f = temp.get(j).intValue();

                maxValue = Math.max(f, maxValue);
            }
        }
        return (float) (maxValue + maxValue * 0.1);
    }

    /**
     * Get the minimum for all the data.
     */
    public float getMin() {
        float minValue = 9999999;

        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            List<Integer> temp = data.get(key);

            for (int j = 0; j < temp.size(); j++) {
                float f = temp.get(j).intValue();

                minValue = Math.min(f, minValue);
            }
        }
        return (float) (minValue - minValue * 0.1);
    }

    /**
     * Get the legend panel.
     */
    public JPanel getLegend() {
        JPanel main = new JPanel();
        GridBagLayout g = new GridBagLayout();

        main.setLayout(g);
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.gridheight = 1;
        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            JLabel colorSwatch = new JLabel("  ");

            colorSwatch.setBackground(colorList.get(t % colorList.size()));
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

    /**
     * Get the stats panel.
     */
    public JPanel getStats() {
        int total = 0;
        float totalValue = 0;
        float maxValue = 0;
        float minValue = 999999;

        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            List<Integer> temp = data.get(key);

            for (int j = 0; j < temp.size(); j++) {
                float f = temp.get(j).intValue();

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

        c.insets = new Insets(3, 6, 3, 6);
        c.fill = GridBagConstraints.BOTH;
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

    /**
     * Gets the size of the biggest Vector.
     */
    public int getDataWidth() {
        int size = 0;

        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            size = Math.max(size, data.get(key).size());
        }
        return size;
    }

    /**
     * Draws the graph.
     */
    @Override
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
        int increment = Math.round((float)(width - 1) / (dataWidth - 1));

        /*
         * for (int t = 0; t < dataWidth; t++) { g.drawLine(t * increment, 0, t *
         * increment, height); }
         */
        int yIncrement = Math.round(((float) height - (1 + base)) / (10 - 1));

        /*
         * for (int t = 0; t < 10; t++) { g.drawLine(0, height - t * yIncrement,
         * width, height - t * yIncrement); }
         */
        // draw axis
        for (int t = 1; t < dataWidth; t += (dataWidth / 25 + 1)) {
            g.drawString((Integer.valueOf(t)).toString(), t * increment + 2, height - 2);
        }
        float incrementValue = (maxValue - minValue) / (10 - 1);

        for (int t = 0; t < 10; t++) {
            g.drawString(Integer.valueOf(Math.round(minValue + (t * incrementValue))).toString(), 2, height - t
                    * yIncrement - 2 - base);
        }
        // draw data lines
        int start = 0;

        for (int t = 0; t < keys.size(); t++) {
            String key = keys.get(t);
            List<Integer> v = data.get(key);

            start = 0;
            g.setColor(colorList.get(t % colorList.size()));
            for (int i = 0; i < v.size() - 1; i++) {
                float y1 = v.get(i).intValue();
                float y2 = v.get(i + 1).intValue();

                y1 = y1 - minValue;
                y2 = y2 - minValue;
                int Y1 = Math.round((height * y1) / (maxValue - minValue));
                int Y2 = Math.round((height * y2) / (maxValue - minValue));

                Y1 = height - Y1 - base;
                Y2 = height - Y2 - base;
                g.drawLine(start, Y1, start + increment, Y2);

                start += increment;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }
  }
}
