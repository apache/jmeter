//$Header$
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter.testelement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.LineChartProperties;
import org.jCharts.properties.PointChartProperties;
import org.jCharts.types.ChartType;

public class LineGraph extends AbstractChart {

    public static final String REPORT_CHART_URLS = "ReportChart.chart.urls";
    public static final Shape[] SHAPE_ARRAY = {PointChartProperties.SHAPE_CIRCLE,
            PointChartProperties.SHAPE_DIAMOND,PointChartProperties.SHAPE_SQUARE,
            PointChartProperties.SHAPE_TRIANGLE};
    
    protected int width = 350;
    protected int height = 250;
    
    protected int shape_counter = 0;

	public LineGraph() {
		super();
	}

    public String getURLs() {
        return getPropertyAsString(REPORT_CHART_URLS);
    }
    
    public void setURLs(String urls) {
        setProperty(REPORT_CHART_URLS,urls);
    }
    
	public JComponent renderChart(List dataset) {
        
        return renderGraphics(null);
	}

    public JComponent renderGraphics(double[][] data) {
        String title = this.getTitle();
        String xAxisTitle = this.getXAxis();
        String yAxisTitle = this.getYAxis();
        String yAxisLabel = this.getYLabel();
        String[] xAxisLabels = {this.getXLabel() };
        Graphics g;
        JPanel panel = new JPanel();
        
        DataSeries dataSeries = new DataSeries( xAxisLabels, xAxisTitle, yAxisTitle, title );
        
        String[] legendLabels= { yAxisLabel };
        Paint[] paints= new Paint[]{ Color.blue.darker() };
        Shape[] shapes = createShapes(data.length);
        Stroke[] lstrokes = createStrokes(data.length);
        LineChartProperties lineChartProperties= new LineChartProperties(lstrokes,shapes);

        try {
            AxisChartDataSet axisChartDataSet= new AxisChartDataSet( data, 
                    legendLabels, 
                    paints, 
                    ChartType.LINE, 
                    lineChartProperties );
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties= new ChartProperties();
            AxisProperties axisProperties= new AxisProperties();
            LegendProperties legendProperties= new LegendProperties();

            AxisChart axisChart = new AxisChart( dataSeries, 
                    chartProperties, 
                    axisProperties, 
                    legendProperties, 
                    width, 
                    height );
        } catch (Exception e) {
            
        }

        return panel;
    }
    
    /**
     * Since we only have 4 shapes, the method will start with the
     * first shape and keep cycling through the shapes in order.
     * @param count
     * @return
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
     * @return
     */
    public Shape nextShape() {
        if (shape_counter >= (SHAPE_ARRAY.length - 1)) {
            shape_counter = 0;
        }
        return SHAPE_ARRAY[shape_counter];
    }
    
    /**
     * 
     * @param count
     * @return
     */
    public Stroke[] createStrokes(int count) {
        Stroke[] str = new Stroke[count];
        for (int idx=0; idx < count; idx++) {
            str[idx] = nextStroke();
        }
        return str;
    }
    
    public Stroke nextStroke() {
        return new BasicStroke(1.5f);
    }
}
