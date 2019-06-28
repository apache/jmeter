/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.jmeter.visualizers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.JPanel;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.DataAxisProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.LineChartProperties;
import org.jCharts.properties.PointChartProperties;
import org.jCharts.types.ChartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Axis graph is used by StatGraphVisualizer, which generates bar graphs
 * from the statistical data.
 */
public class LineGraph extends JPanel {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(LineGraph.class);

    protected double[][] data = null;
    protected String title;
    protected String xAxisTitle;
    protected String yAxisTitle;
    protected String[] xAxisLabels;
    protected String[] yAxisLabel;
    protected int width;
    protected int height;

    private static final Shape[] SHAPE_ARRAY = {PointChartProperties.SHAPE_CIRCLE,
            PointChartProperties.SHAPE_DIAMOND,PointChartProperties.SHAPE_SQUARE,
            PointChartProperties.SHAPE_TRIANGLE};

    /**
     * 12 basic colors for line graphs. If we need more colors than this,
     * we can add more. Though more than 12 lines per graph will look
     * rather busy and be hard to read.
     */
    private static final Paint[] PAINT_ARRAY = {Color.black,
            Color.blue,Color.green,Color.magenta,Color.orange,
            Color.red,Color.yellow,Color.darkGray,Color.gray,Color.lightGray,
            Color.pink,Color.cyan};
    protected int shape_counter = 0;
    protected int paint_counter = -1;

    /**
     *
     */
    public LineGraph() {
        super();
    }

    /**
     * @param layout The {@link LayoutManager} to be used
     */
    public LineGraph(LayoutManager layout) {
        super(layout);
    }

    /**
     * @param layout The {@link LayoutManager} to be used
     * @param isDoubleBuffered Flag whether double buffering should be used
     */
    public LineGraph(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setXAxisTitle(String title) {
        this.xAxisTitle = title;
    }

    public void setYAxisTitle(String title) {
        this.yAxisTitle = title;
    }

    public void setXAxisLabels(String[] labels) {
        this.xAxisLabels = labels;
    }

    public void setYAxisLabels(String[] label) {
        this.yAxisLabel = label;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    @Override
    public void paintComponent(Graphics g) {
        // reset the paint counter
        this.paint_counter = -1;
        if (data != null && this.title != null && this.xAxisLabels != null &&
                this.xAxisTitle != null && this.yAxisLabel != null &&
                this.yAxisTitle != null) {
            drawSample(this.title,this.xAxisLabels,this.xAxisTitle,
                    this.yAxisTitle,this.data,this.width,this.height,g);
        }
    }

    private void drawSample(String _title, String[] _xAxisLabels, String _xAxisTitle,
            String _yAxisTitle, double[][] _data, int _width, int _height, Graphics g) {
        try {
            if (_width == 0) {
                _width = 450;
            }
            if (_height == 0) {
                _height = 250;
            }
            this.setPreferredSize(new Dimension(_width,_height));
            DataSeries dataSeries = new DataSeries( _xAxisLabels, _xAxisTitle, _yAxisTitle, _title );
            String[] legendLabels= yAxisLabel;
            Paint[] paints = this.createPaint(_data.length);
            Shape[] shapes = createShapes(_data.length);
            Stroke[] lstrokes = createStrokes(_data.length);
            LineChartProperties lineChartProperties= new LineChartProperties(lstrokes,shapes);
            AxisChartDataSet axisChartDataSet= new AxisChartDataSet( _data,
                    legendLabels,
                    paints,
                    ChartType.LINE,
                    lineChartProperties );
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties = new ChartProperties();
            AxisProperties axisProperties = new AxisProperties();
            // show the grid lines, to turn it off, set it to zero
            axisProperties.getYAxisProperties().setShowGridLines(1);
            axisProperties.setXAxisLabelsAreVertical(true);
            // set the Y Axis to round
            DataAxisProperties daxp = (DataAxisProperties)axisProperties.getYAxisProperties();
            daxp.setRoundToNearest(1);
            LegendProperties legendProperties = new LegendProperties();
            AxisChart axisChart = new AxisChart(
                    dataSeries, chartProperties, axisProperties,
                    legendProperties, _width, _height );
            axisChart.setGraphics2D((Graphics2D) g);
            axisChart.render();
        } catch (Exception e) {
            log.error("Error while rendering axis chart. {}", e.getMessage());
        }
    }

    /**
     * Since we only have 4 shapes, the method will start with the first shape
     * and keep cycling through the shapes in order.
     *
     * @param count
     *            The number of shapes to be created
     * @return the first n shapes
     */
    public Shape[] createShapes(int count) {
        Shape[] shapes = new Shape[count];
        for (int idx=0; idx < count; idx++) {
            shapes[idx] = nextShape();
        }
        return shapes;
    }

    /**
     * Return the next shape
     * @return the next shape
     */
    public Shape nextShape() {
        this.shape_counter++;
        if (shape_counter >= (SHAPE_ARRAY.length - 1)) {
            shape_counter = 0;
        }
        return SHAPE_ARRAY[shape_counter];
    }

    /**
     * Create a given number of {@link Stroke}s
     *
     * @param count
     *            The number of strokes to be created
     * @return the first <code>count</code> strokes
     */
    public Stroke[] createStrokes(int count) {
        Stroke[] str = new Stroke[count];
        for (int idx=0; idx < count; idx++) {
            str[idx] = nextStroke();
        }
        return str;
    }

    /**
     * method always return a new BasicStroke with 1.0f weight
     * @return a new BasicStroke with 1.0f weight
     */
    public Stroke nextStroke() {
        return new BasicStroke(1.0f);
    }

    /**
     * return an array of Paint with different colors. The current
     * implementation will cycle through 12 colors if a line graph has more than
     * 12 entries
     *
     * @param count
     *            The number of {@link Paint}s to be created
     * @return an array of Paint with different colors
     */
    public Paint[] createPaint(int count) {
        Paint[] pts = new Paint[count];
        for (int idx=0; idx < count; idx++) {
            pts[idx] = nextPaint();
        }
        return pts;
    }

    /**
     * The method will return the next paint color in the PAINT_ARRAY.
     * Rather than return a random color, we want it to always go through
     * the same sequence. This way, the same charts will always use the
     * same color and make it easier to compare side by side.
     * @return the next paint color in the PAINT_ARRAY
     */
    public Paint nextPaint() {
        this.paint_counter++;
        if (this.paint_counter == (PAINT_ARRAY.length - 1)) {
            this.paint_counter = 0;
        }
        return PAINT_ARRAY[this.paint_counter];
    }
}
