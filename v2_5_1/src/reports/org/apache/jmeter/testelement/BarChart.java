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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.apache.jmeter.report.DataSet;
import org.apache.jmeter.visualizers.AxisGraph;
import org.apache.jmeter.visualizers.SamplingStatCalculator;

/**
 * The class is reponsible for returning
 *
 */
public class BarChart extends AbstractChart {

    private static final long serialVersionUID = 240L;

    public static final String REPORT_BAR_CHART_URL = "ReportChart.bar.chart.url";

    public BarChart() {
        super();
    }

    public String getURL() {
        return getPropertyAsString(REPORT_BAR_CHART_URL);
    }

    public void setURL(String url) {
        setProperty(REPORT_BAR_CHART_URL,url);
    }

    /**
     * Convert the data from SamplingStatCalculator to double array of array
     * @param data
     * @return data values
     */
    public double[][] convertToDouble(List<SamplingStatCalculator> data) {
        double[][] dataset = new double[1][data.size()];
        //Iterator itr = data.iterator();
        for (int idx=0; idx < data.size(); idx++) {
            SamplingStatCalculator stat = data.get(idx);
            dataset[0][idx] = getValue(stat);
        }
        return dataset;
    }

    @Override
    public JComponent renderChart(List<DataSet> data) {
        ArrayList<SamplingStatCalculator> dset = new ArrayList<SamplingStatCalculator>();
        ArrayList<String> xlabels = new ArrayList<String>();
        Iterator<DataSet> itr = data.iterator();
        while (itr.hasNext()) {
            DataSet item = itr.next();
            SamplingStatCalculator ss = item.getStatistics(this.getURL());
            if (ss != null) {
                // we add the entry
                dset.add(ss);
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
        AxisGraph panel = new AxisGraph();
        panel.setTitle(this.getTitle());
        panel.setData(data);
        panel.setXAxisLabels(xAxisLabels);
        panel.setYAxisLabels(this.getYLabel());
        panel.setXAxisTitle(this.getFormattedXAxis());
        panel.setYAxisTitle(this.getYAxis());
        // we should make this configurable eventually
        int width = getWidth();
        int height = getHeight();
        panel.setPreferredSize(new Dimension(width,height));
        panel.setSize(new Dimension(width,height));
        panel.setWidth(width);
        panel.setHeight(width);
        setBufferedImage(new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB));
        panel.paintComponent(this.getBufferedImage().createGraphics());
        return panel;
    }

}
