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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.jmeter.report.DataSet;
import org.apache.jmeter.visualizers.SamplingStatCalculator;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.DataSeries;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.BarChartProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.types.ChartType;


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
        }
        return dataset;
    }
    
	public JComponent renderChart(List data) {
        ArrayList dset = new ArrayList();
        ArrayList xlabels = new ArrayList();
        Iterator itr = data.iterator();
        while (itr.hasNext()) {
            DataSet ds = (DataSet)itr.next();
            SamplingStatCalculator ss = ds.getStatistics(this.getURL());
            if (ss != null) {
                // we add the entry
                dset.add(ss);
                xlabels.add(ds.getDataSource());
            }
        }
        double[][] dbset = convertToDouble(dset);
		return renderGraphics(dbset, (String[])xlabels.toArray(new String[xlabels.size()]));
	}
    
    public JComponent renderGraphics(double[][] data, String[] xAxisLabels) {
        String title = this.getTitle();
        String xAxisTitle = this.getXAxis();
        String yAxisTitle = this.getYAxis();
        String yAxisLabel = this.getYLabel();
        int width = 350;
        int height = 300;
        Graphics g;
        JPanel panel = new JPanel();
        try {
            
            DataSeries dataSeries = new DataSeries( xAxisLabels, xAxisTitle, yAxisTitle, title );
            
            String[] legendLabels= { yAxisLabel };
            Paint[] paints = new Paint[]{ Color.blue.darker() };
            BarChartProperties barChartProperties= new BarChartProperties();
            AxisChartDataSet axisChartDataSet =
                new AxisChartDataSet(
                        data, legendLabels, paints, ChartType.BAR, barChartProperties );
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties= new ChartProperties();
            AxisProperties axisProperties= new AxisProperties();
            axisProperties.setXAxisLabelsAreVertical(true);
            LegendProperties legendProperties= new LegendProperties();
            AxisChart axisChart = new AxisChart( 
                    dataSeries, chartProperties, axisProperties, 
                    legendProperties, width, height );
            axisChart.setGraphics2D((Graphics2D) panel.getGraphics());
            axisChart.render();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
