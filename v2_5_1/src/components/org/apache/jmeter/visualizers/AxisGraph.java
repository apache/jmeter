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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.math.BigDecimal;

import javax.swing.JPanel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.jCharts.axisChart.AxisChart;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelPosition;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelRenderer;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.BarChartProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.DataAxisProperties;
import org.jCharts.properties.LabelAxisProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.PropertyException;
import org.jCharts.types.ChartType;

/**
 *
 * Axis graph is used by StatGraphVisualizer, which generates bar graphs
 * from the statistical data.
 */
public class AxisGraph extends JPanel {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String ELLIPSIS = "..."; //$NON-NLS-1$
    private static final int ELLIPSIS_LEN = ELLIPSIS.length();

    protected double[][] data = null;
    protected String title, xAxisTitle, yAxisTitle, yAxisLabel;
    protected int maxLength;
    protected String[] xAxisLabels;
    protected int width, height;

    /**
     *
     */
    public AxisGraph() {
        super();
    }

    /**
     * @param layout
     */
    public AxisGraph(LayoutManager layout) {
        super(layout);
    }

    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public AxisGraph(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
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

    public void setYAxisLabels(String label) {
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
        if (data != null && this.title != null && this.xAxisLabels != null &&
                this.xAxisTitle != null && this.yAxisLabel != null &&
                this.yAxisTitle != null) {
            drawSample(this.title,this.maxLength,this.xAxisLabels,this.xAxisTitle,
                this.yAxisTitle,this.data,this.width,this.height,g);
        }
    }

    private double findMax(double _data[][]) {
        double max = 0;
        max = _data[0][0];
        for (int i = 0; i < _data.length; i++) {
            for (int j = 0; j < _data[i].length; j++) {
                if (_data[i][j] > max) {
                    max = _data[i][j];
                }
            }
        }
        return max;
    }

    private String squeeze (String input, int _maxLength){
        if (input.length()>_maxLength){
            String output=input.substring(0,_maxLength-ELLIPSIS_LEN)+ELLIPSIS;
            return output;
        }
        return input;
    }

    private void drawSample(String _title, int _maxLength, String[] _xAxisLabels, String _xAxisTitle,
            String _yAxisTitle, double[][] _data, int _width, int _height, Graphics g) {
        double max = findMax(_data);
        try {
            /** These controls are already done in StatGraphVisualizer
            if (_width == 0) {
                _width = 450;
            }
            if (_height == 0) {
                _height = 250;
            }
            **/
            if (_maxLength < 3) {
                _maxLength = 3;
            }
            // if the "Title of Graph" is empty, we can assume some default
            if (_title.length() == 0 ) {
                _title = "Graph";
            }
            // if the labels are too long, they'll be "squeezed" to make the chart viewable.
            for (int i = 0; i < _xAxisLabels.length; i++) {
                String label = _xAxisLabels[i];
                _xAxisLabels[i]=squeeze(label, _maxLength);
            }
            this.setPreferredSize(new Dimension(_width,_height));
            DataSeries dataSeries = new DataSeries( _xAxisLabels, _xAxisTitle, _yAxisTitle, _title );

            String[] legendLabels= { yAxisLabel };
            Paint[] paints= new Paint[] { Color.yellow };
            BarChartProperties barChartProperties= new BarChartProperties();
            ValueLabelRenderer valueLabelRenderer = new ValueLabelRenderer(false, false, true, 0);
            valueLabelRenderer.setValueLabelPosition(ValueLabelPosition.AT_TOP);
            valueLabelRenderer.useVerticalLabels(true);
            barChartProperties.addPostRenderEventListener(valueLabelRenderer);
            AxisChartDataSet axisChartDataSet =
                new AxisChartDataSet(
                        _data, legendLabels, paints, ChartType.BAR, barChartProperties );
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties= new ChartProperties();
            LabelAxisProperties xaxis = new LabelAxisProperties();
            DataAxisProperties yaxis = new DataAxisProperties();

            try {
                BigDecimal round = new BigDecimal(max / 1000d);
                round = round.setScale(0, BigDecimal.ROUND_UP);
                double topValue = round.doubleValue() * 1000;
                yaxis.setUserDefinedScale(0, 500);
                yaxis.setNumItems((int) (topValue / 500)+1);
                yaxis.setShowGridLines(1);
            } catch (PropertyException e) {
                log.warn("",e);
            }

            AxisProperties axisProperties= new AxisProperties(xaxis, yaxis);
            axisProperties.setXAxisLabelsAreVertical(true);
            LegendProperties legendProperties= new LegendProperties();
            AxisChart axisChart = new AxisChart(
                    dataSeries, chartProperties, axisProperties,
                    legendProperties, _width, _height );
            axisChart.setGraphics2D((Graphics2D) g);
            axisChart.render();
        } catch (ChartDataException e) {
            log.warn("",e);
        } catch (PropertyException e) {
            log.warn("",e);
        }
    }

}
