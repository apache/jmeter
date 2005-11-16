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
 * @author pete
 *
 */
public class BarChart extends AbstractChart {

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
     * @return
     */
    public double[][] convertToDouble(List data) {
        double[][] dataset = new double[1][data.size()];
        Iterator itr = data.iterator();
        for (int idx=0; idx < data.size(); idx++) {
            SamplingStatCalculator stat = (SamplingStatCalculator)data.get(idx);
            dataset[0][idx] = getValue(stat);
            // System.out.println("data=" + dataset[0][idx]);
        }
        return dataset;
    }
    
	public JComponent renderChart(List data) {
        ArrayList dset = new ArrayList();
        ArrayList xlabels = new ArrayList();
        Iterator itr = data.iterator();
        while (itr.hasNext()) {
            DataSet item = (DataSet)itr.next();
            SamplingStatCalculator ss = item.getStatistics(this.getURL());
            if (ss != null) {
                // we add the entry
                dset.add(ss);
                xlabels.add(item.getDataSource());
            }
        }
        double[][] dbset = convertToDouble(dset);
		return renderGraphics(dbset, (String[])xlabels.toArray(new String[xlabels.size()]));
	}
    
    public JComponent renderGraphics(double[][] data, String[] xAxisLabels) {
        String title = this.getTitle();
        AxisGraph panel = new AxisGraph();
        panel.setTitle(this.getTitle());
        panel.setData(data);
        panel.setXAxisLabels(xAxisLabels);
        panel.setYAxisLabels(this.getYLabel());
        panel.setXAxisTitle(this.getXAxis());
        panel.setYAxisTitle(this.getYAxis());
        // we should make this configurable eventually
        int width = 400;
        int height = 400;
        panel.setPreferredSize(new Dimension(width,height));
        panel.setSize(new Dimension(width,height));
        panel.setWidth(width);
        panel.setHeight(width);
        setBufferedImage(new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB));
        panel.paintComponent(this.getBufferedImage().createGraphics());
        return panel;
    }
    
    /**
     * convienance method for getting the selected value. Rather than use
     * Method.invoke(Object,Object[]), it's simpler to just check which
     * column is selected and call the method directly.
     * @param stat
     * @return
     */
    public double getValue(SamplingStatCalculator stat) {
        if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_50_PERCENT)) {
            return stat.getPercentPoint(.50).doubleValue();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_90_PERCENT)){
            return stat.getPercentPoint(.90).doubleValue();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_ERROR_RATE)) {
            return stat.getErrorPercentage();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_MAX)) {
            return stat.getMax().doubleValue();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_MEAN)) {
            return stat.getMean();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_MEDIAN)) {
            return stat.getMedian().doubleValue();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_MIN)) {
            return stat.getMin().doubleValue();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_RESPONSE_RATE)) {
            return stat.getRate();
        } else if (this.getXAxis().equals(AbstractTable.REPORT_TABLE_TRANSFER_RATE)) {
            // return the pagesize divided by 1024 to get kilobytes
            return stat.getPageSize()/1024;
        } else {
            return -1;
        }
    }
}
