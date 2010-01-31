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
package org.apache.jmeter.testelement;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.apache.jmeter.report.DataSet;
import org.apache.jmeter.visualizers.LineGraph;
import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.jCharts.properties.PointChartProperties;

public class LineChart extends AbstractChart {

    private static final long serialVersionUID = 240L;

    private static final String URL_DELIM = ","; //$NON-NLS-1$
    private static final String REPORT_CHART_URLS = "ReportChart.chart.urls"; //$NON-NLS-1$
    private static final Shape[] SHAPE_ARRAY = {PointChartProperties.SHAPE_CIRCLE,
            PointChartProperties.SHAPE_DIAMOND,PointChartProperties.SHAPE_SQUARE,
            PointChartProperties.SHAPE_TRIANGLE};

    private int shape_counter = 0;

    public LineChart() {
        super();
    }

    public String getURLs() {
        return getPropertyAsString(REPORT_CHART_URLS);
    }

    public void setURLs(String urls) {
        setProperty(REPORT_CHART_URLS,urls);
    }

    private double[][] convertToDouble(List<DataSet> data) {
        String[] urls = this.getURLs().split(URL_DELIM);
        double[][] dataset = new double[urls.length][data.size()];
        for (int idx=0; idx < urls.length; idx++) {
            for (int idz=0; idz < data.size(); idz++) {
                DataSet dset = data.get(idz);
                SamplingStatCalculator ss = dset.getStatistics(urls[idx]);
                dataset[idx][idz] = getValue(ss);
            }
        }
        return dataset;
    }

    @Override
    public JComponent renderChart(List<DataSet> dataset) {
        ArrayList<DataSet> dset = new ArrayList<DataSet>();
        ArrayList<String> xlabels = new ArrayList<String>();
        Iterator<DataSet> itr = dataset.iterator();
        while (itr.hasNext()) {
            DataSet item = itr.next();
            if (item != null) {
                // we add the entry
                dset.add(item);
                if ( getXLabel().equals(X_DATA_FILENAME_LABEL) ) {
                    xlabels.add(item.getDataSourceName());
                } else {
                    xlabels.add(item.getMonthDayYearDate());
                }
            }
        }
        double[][] dbset = convertToDouble(dset);
        return renderGraphics(dbset, xlabels.toArray(new String[xlabels.size()]));
    }

    public JComponent renderGraphics(double[][] data, String[] xAxisLabels) {
        LineGraph panel = new LineGraph();
        panel.setTitle(this.getTitle());
        panel.setData(data);
        panel.setXAxisLabels(xAxisLabels);
        panel.setYAxisLabels(this.getURLs().split(URL_DELIM));
        panel.setXAxisTitle(this.getFormattedXAxis());
        panel.setYAxisTitle(this.getYAxis());
        // we should make this configurable eventually
        int _width = getWidth();
        int _height = getHeight();
        panel.setPreferredSize(new Dimension(_width,_height));
        panel.setSize(new Dimension(_width,_height));
        panel.setWidth(_width);
        panel.setHeight(_width);
        setBufferedImage(new BufferedImage(_width,_height,BufferedImage.TYPE_INT_RGB));
        panel.paintComponent(this.getBufferedImage().createGraphics());
        return panel;
    }

    /**
     * Since we only have 4 shapes, the method will start with the
     * first shape and keep cycling through the shapes in order.
     * @param count
     * @return array of shapes
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
     * @return the shape
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
     * @return array of strokes
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
