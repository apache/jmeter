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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.math.BigDecimal;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.jmeter.util.JMeterUtils;
import org.jCharts.axisChart.AxisChart;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelPosition;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelRenderer;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.ClusteredBarChartProperties;
import org.jCharts.properties.DataAxisProperties;
import org.jCharts.properties.LabelAxisProperties;
import org.jCharts.properties.LegendAreaProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.PropertyException;
import org.jCharts.properties.util.ChartFont;
import org.jCharts.types.ChartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Axis graph is used by StatGraphVisualizer, which generates bar graphs
 * from the statistical data.
 */
public class AxisGraph extends JPanel {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(AxisGraph.class);

    private static final String ELLIPSIS = "..."; //$NON-NLS-1$
    private static final int ELLIPSIS_LEN = ELLIPSIS.length();

    protected double[][] data = null;
    protected String title;
    protected String xAxisTitle;
    protected String yAxisTitle;
    protected String yAxisLabel;
    protected int maxLength;
    protected String[] xAxisLabels;
    protected int width;
    protected int height;
    
    protected String[] legendLabels = { JMeterUtils.getResString("aggregate_graph_legend") }; // $NON-NLS-1$
    
    protected int maxYAxisScale;

    protected Font titleFont;

    protected Font legendFont;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    protected Font valueFont = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.6));

    protected Color[] color = { Color.YELLOW };

    protected Color foreColor = Color.BLACK;

    protected boolean outlinesBarFlag = false;

    protected boolean showGrouping = true;
    
    protected boolean valueOrientation = true;

    protected int legendPlacement = LegendAreaProperties.BOTTOM;

    /**
     *
     */
    public AxisGraph() {
        super();
    }

    /**
     * @param layout The {@link LayoutManager} to use
     */
    public AxisGraph(LayoutManager layout) {
        super(layout);
    }

    /**
     * @param layout The {@link LayoutManager} to use
     * @param isDoubleBuffered Flag whether double buffering should be used
     */
    public AxisGraph(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    /**
     * Expects null array when no data  not empty array
     * @param data The data to be drawn
     */
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

    /**
     * Expects null array when no labels not empty array
     * @param labels The labels for the x axis
     */
    public void setXAxisLabels(String[] labels) {
        this.xAxisLabels = labels;
    }

    public void setYAxisLabels(String label) {
        this.yAxisLabel = label;
    }

    public void setLegendLabels(String[] labels) {
        this.legendLabels = labels;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    /**
     * @return the maxYAxisScale
     */
    public int getMaxYAxisScale() {
        return maxYAxisScale;
    }

    /**
     * @param maxYAxisScale the maxYAxisScale to set
     */
    public void setMaxYAxisScale(int maxYAxisScale) {
        this.maxYAxisScale = maxYAxisScale;
    }

    /**
     * @return the color
     */
    public Color[] getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color[] color) {
        this.color = color;
    }

    /**
     * @return the foreColor
     */
    public Color getForeColor() {
        return foreColor;
    }

    /**
     * @param foreColor the foreColor to set
     */
    public void setForeColor(Color foreColor) {
        this.foreColor = foreColor;
    }

    /**
     * @return the titleFont
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * @param titleFont the titleFont to set
     */
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    /**
     * @return the legendFont
     */
    public Font getLegendFont() {
        return legendFont;
    }

    /**
     * @param legendFont the legendFont to set
     */
    public void setLegendFont(Font legendFont) {
        this.legendFont = legendFont;
    }

    /**
     * @return the valueFont
     */
    public Font getValueFont() {
        return valueFont;
    }

    /**
     * @param valueFont the valueFont to set
     */
    public void setValueFont(Font valueFont) {
        this.valueFont = valueFont;
    }

    /**
     * @return the legendPlacement
     */
    public int getLegendPlacement() {
        return legendPlacement;
    }

    /**
     * @param legendPlacement the legendPlacement to set
     */
    public void setLegendPlacement(int legendPlacement) {
        this.legendPlacement = legendPlacement;
    }

    /**
     * @return the outlinesBarFlag
     */
    public boolean isOutlinesBarFlag() {
        return outlinesBarFlag;
    }

    /**
     * @param outlinesBarFlag the outlinesBarFlag to set
     */
    public void setOutlinesBarFlag(boolean outlinesBarFlag) {
        this.outlinesBarFlag = outlinesBarFlag;
    }

    /**
     * @return the valueOrientation
     */
    public boolean isValueOrientation() {
        return valueOrientation;
    }

    /**
     * @param valueOrientation the valueOrientation to set
     */
    public void setValueOrientation(boolean valueOrientation) {
        this.valueOrientation = valueOrientation;
    }

    /**
     * @return the showGrouping
     */
    public boolean isShowGrouping() {
        return showGrouping;
    }

    /**
     * @param showGrouping the showGrouping to set
     */
    public void setShowGrouping(boolean showGrouping) {
        this.showGrouping = showGrouping;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (data != null && this.title != null && this.xAxisLabels != null &&
                this.yAxisLabel != null &&
                this.yAxisTitle != null) {
            drawSample(this.title, this.maxLength, this.xAxisLabels, 
                    this.yAxisTitle, this.legendLabels,
                    this.data, this.width, this.height, this.color,
                    this.legendFont, graphics);
        }
    }

    private double findMax(double[][] _data) {
        double max = _data[0][0];
        for (double[] dArray : _data) {
            for (double d : dArray) {
                if (d > max) {
                    max = d;
                }
            }
        }
        return max;
    }

    private String squeeze (String input, int _maxLength){
        if (input.length()>_maxLength){
            return input.substring(0,_maxLength-ELLIPSIS_LEN)+ELLIPSIS;
        }
        return input;
    }

    private void drawSample(String _title, int _maxLength, String[] _xAxisLabels,
            String _yAxisTitle, String[] _legendLabels, double[][] _data,
            int _width, int _height, Color[] _color,
            Font legendFont, Graphics g) {
        double max = maxYAxisScale > 0 ? maxYAxisScale : findMax(_data); // define max scale y axis
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
                _title = JMeterUtils.getResString("aggregate_graph_title"); //$NON-NLS-1$
            }
            // if the labels are too long, they'll be "squeezed" to make the chart viewable.
            for (int i = 0; i < _xAxisLabels.length; i++) {
                String label = _xAxisLabels[i];
                _xAxisLabels[i]=squeeze(label, _maxLength);
            }
            this.setPreferredSize(new Dimension(_width,_height));
            DataSeries dataSeries = new DataSeries( _xAxisLabels, null, _yAxisTitle, _title ); // replace _xAxisTitle to null (don't display x axis title)

            ClusteredBarChartProperties clusteredBarChartProperties= new ClusteredBarChartProperties();
            clusteredBarChartProperties.setShowOutlinesFlag(outlinesBarFlag);
            ValueLabelRenderer valueLabelRenderer = new ValueLabelRenderer(false, false, showGrouping, 0);
            valueLabelRenderer.setValueLabelPosition(ValueLabelPosition.AT_TOP);

            valueLabelRenderer.setValueChartFont(new ChartFont(valueFont, foreColor));
            valueLabelRenderer.useVerticalLabels(valueOrientation);

            clusteredBarChartProperties.addPostRenderEventListener(valueLabelRenderer);

            Paint[] paints = new Paint[_color.length];
            System.arraycopy(_color, 0, paints, 0, paints.length);
            
            AxisChartDataSet axisChartDataSet =
                new AxisChartDataSet(
                        _data, _legendLabels, paints, ChartType.BAR_CLUSTERED, clusteredBarChartProperties );
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties= new ChartProperties();
            LabelAxisProperties xaxis = new LabelAxisProperties();
            DataAxisProperties yaxis = new DataAxisProperties();
            yaxis.setUseCommas(showGrouping);

            if (legendFont != null) {
                yaxis.setAxisTitleChartFont(new ChartFont(legendFont, new Color(20)));
                yaxis.setScaleChartFont(new ChartFont(legendFont, new Color(20)));
                xaxis.setAxisTitleChartFont(new ChartFont(legendFont, new Color(20)));
                xaxis.setScaleChartFont(new ChartFont(legendFont, new Color(20)));
            }
            if (titleFont != null) {
                chartProperties.setTitleFont(new ChartFont(titleFont, new Color(0)));
            }

            // Y Axis
            try {
                BigDecimal round = BigDecimal.valueOf(max / 1000d);
                round = round.setScale(0, BigDecimal.ROUND_UP);
                double topValue = round.doubleValue() * 1000;
                yaxis.setUserDefinedScale(0, 500);
                yaxis.setNumItems((int) (topValue / 500)+1);
                yaxis.setShowGridLines(1);
            } catch (PropertyException e) {
                log.warn("Chart property exception occurred.", e);
            }

            AxisProperties axisProperties= new AxisProperties(xaxis, yaxis);
            axisProperties.setXAxisLabelsAreVertical(true);
            LegendProperties legendProperties= new LegendProperties();
            legendProperties.setBorderStroke(null);
            legendProperties.setPlacement(legendPlacement);
            legendProperties.setIconBorderPaint(Color.WHITE);
            if (legendPlacement == LegendAreaProperties.RIGHT || legendPlacement == LegendAreaProperties.LEFT) {
                legendProperties.setNumColumns(1);
            }
            if (legendFont != null) {
                legendProperties.setFont(legendFont); //new Font("SansSerif", Font.PLAIN, 10)
            }
            AxisChart axisChart = new AxisChart(
                    dataSeries, chartProperties, axisProperties,
                    legendProperties, _width, _height );
            axisChart.setGraphics2D((Graphics2D) g);
            axisChart.render();
        } catch (ChartDataException | PropertyException e) {
            log.warn("Exception occurred while rendering chart.", e);
        }
    }

}
