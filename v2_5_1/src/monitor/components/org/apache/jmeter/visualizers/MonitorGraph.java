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
 */
package org.apache.jmeter.visualizers;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.apache.jmeter.samplers.Clearable;

/**
 * MonitorGraph will draw the performance history of a given server. It displays
 * 4 lines:
 * <p>
 */
public class MonitorGraph extends JComponent implements MonitorGuiListener, Clearable {

    private static final long serialVersionUID = 240L;

    private final MonitorAccumModel model;

    private MonitorModel current;

    private boolean drawHealth = true;

    private boolean drawLoad = true;

    private boolean drawMemory = true;

    private boolean drawThread = true;

    private boolean drawYgrid = true;

    private boolean drawXgrid = true;

    /**
     * Needed for Serialization tests.
     * @deprecated Only for use in unit testing
     */
    @Deprecated
    public MonitorGraph() {
        // log.warn("Only for use in unit testing");
        model = null;
    }

    public MonitorGraph(MonitorAccumModel model) {
        this.model = model;
        repaint();
    }

    public void setHealth(boolean health) {
        this.drawHealth = health;
    }

    public void setLoad(boolean load) {
        this.drawLoad = load;
    }

    public void setMem(boolean mem) {
        this.drawMemory = mem;
    }

    public void setThread(boolean thread) {
        this.drawThread = thread;
    }

    /**
     * The method will first check to see if the graph is visible. If it is, it
     * will repaint the graph.
     */
    public void updateGui(final MonitorModel model) {
        if (this.isShowing()) {
            this.current = model;
            repaint();
        }
    }

    /**
     * painComponent is responsible for drawing the actual graph. This is
     * because of how screen works. Tried to use clipping, but I don't
     * understand it well enough to get the desired effect.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.current != null) {
            synchronized (model) {
                List<MonitorModel> samples = model.getAllSamples(this.current.getURL());
                int size = samples.size();
                synchronized (samples) {
                    Iterator<MonitorModel> e;
                    if (size > getWidth()) {
                        e = samples.listIterator(size - getWidth());
                    } else {
                        e = samples.iterator();
                    }
                    MonitorModel last = null;
                    for (int i = 0; e.hasNext(); i++) {
                        MonitorModel s = e.next();
                        if (last == null) {
                            last = s;
                        }
                        drawSample(i, s, g, last);
                        last = s;
                    }
                }
            }
        }
    }

    /**
     * updateGui() will call repaint
     */
    public void updateGui() {
        repaint();
    }

    /**
     * clear will repaint the graph
     */
    public void clearData() {
        paintComponent(getGraphics());
        this.repaint();
    }

    private void drawSample(int x, MonitorModel model, Graphics g, MonitorModel last) {
        double width = getWidth();
        double height = getHeight() - 10.0;
        int xaxis = (int) (width * (x / width));
        int lastx = (int) (width * ((x - 1) / width));

        // draw grid only when x is 1. If we didn't
        // the grid line would draw over the data
        // lines making it look bad.
        if (drawYgrid && x == 1) {
            int q1 = (int) (height * 0.25);
            int q2 = (int) (height * 0.50);
            int q3 = (int) (height * 0.75);
            g.setColor(Color.lightGray);
            g.drawLine(0, q1, getWidth(), q1);
            g.drawLine(0, q2, getWidth(), q2);
            g.drawLine(0, q3, getWidth(), q3);
        }
        if (drawXgrid && x == 1) {
            int x1 = (int) (width * 0.25);
            int x2 = (int) (width * 0.50);
            int x3 = (int) (width * 0.75);
            g.drawLine(x1, 0, x1, getHeight());
            g.drawLine(x2, 0, x2, getHeight());
            g.drawLine(x3, 0, x3, getHeight());
            g.drawLine(getWidth(), 0, getWidth(), getHeight());
        }

        if (drawHealth) {
            int hly = (int) (height - (height * (model.getHealth() / 3.0)));
            int lasty = (int) (height - (height * (last.getHealth() / 3.0)));

            g.setColor(Color.green);
            g.drawLine(lastx, lasty, xaxis, hly);
        }

        if (drawLoad) {
            int ldy = (int) (height - (height * (model.getLoad() / 100.0)));
            int lastldy = (int) (height - (height * (last.getLoad() / 100.0)));

            g.setColor(Color.blue);
            g.drawLine(lastx, lastldy, xaxis, ldy);
        }

        if (drawMemory) {
            int mmy = (int) (height - (height * (model.getMemload() / 100.0)));
            int lastmmy = (int) (height - (height * (last.getMemload() / 100.0)));

            g.setColor(Color.orange);
            g.drawLine(lastx, lastmmy, xaxis, mmy);
        }

        if (drawThread) {
            int thy = (int) (height - (height * (model.getThreadload() / 100.0)));
            int lastthy = (int) (height - (height * (last.getThreadload() / 100.0)));

            g.setColor(Color.red);
            g.drawLine(lastx, lastthy, xaxis, thy);
        }
    }

}
