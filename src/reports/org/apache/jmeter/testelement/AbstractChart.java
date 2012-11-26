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

import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;

import org.apache.jmeter.report.DataSet;
import org.apache.jmeter.report.ReportChart;
import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * The general idea of the chart graphs information for a table.
 * A chart can only be generated from a specific table, though more
 * than one chart can be generated from a single table.
  *
 */
public abstract class AbstractChart extends AbstractTestElement implements ReportChart {

    private static final long serialVersionUID = 240L;

    public static final String REPORT_CHART_X_AXIS = "ReportChart.chart.x.axis";
    public static final String REPORT_CHART_Y_AXIS = "ReportChart.chart.y.axis";
    public static final String REPORT_CHART_X_LABEL = "ReportChart.chart.x.label";
    public static final String REPORT_CHART_Y_LABEL = "ReportChart.chart.y.label";
    public static final String REPORT_CHART_TITLE = "ReportChart.chart.title";
    public static final String REPORT_CHART_CAPTION = "ReportChart.chart.caption";
    public static final String REPORT_CHART_WIDTH = "ReportChart.chart.width";
    public static final String REPORT_CHART_HEIGHT = "ReportChart.chart.height";

    public static final int DEFAULT_WIDTH = 350;
    public static final int DEFAULT_HEIGHT = 350;

    public static final String X_DATA_FILENAME_LABEL = "Filename";
    public static final String X_DATA_DATE_LABEL = "Date";
    public static final String[] X_LABELS = { X_DATA_FILENAME_LABEL, X_DATA_DATE_LABEL };
    protected BufferedImage image = null;

    public AbstractChart() {
        super();
    }

    public String getXAxis() {
        return getPropertyAsString(REPORT_CHART_X_AXIS);
    }

    public String getFormattedXAxis() {
        String text = getXAxis();
        if (text.indexOf('.') > -1) {
            text = text.substring(text.indexOf('.') + 1);
            text = JOrphanUtils.replaceAllChars(text,'_'," ");
        }
        return text;
    }
    public void setXAxis(String field) {
        setProperty(REPORT_CHART_X_AXIS,field);
    }

    public String getYAxis() {
        return getPropertyAsString(REPORT_CHART_Y_AXIS);
    }

    public void setYAxis(String scale) {
        setProperty(REPORT_CHART_Y_AXIS,scale);
    }

    public String getXLabel() {
        return getPropertyAsString(REPORT_CHART_X_LABEL);
    }

    /**
     * The X data labels should be either the filename, date or some
     * other series of values
     * @param label
     */
    public void setXLabel(String label) {
        setProperty(REPORT_CHART_X_LABEL,label);
    }

    public String getYLabel() {
        return getPropertyAsString(REPORT_CHART_Y_LABEL);
    }

    public void setYLabel(String label) {
        setProperty(REPORT_CHART_Y_LABEL,label);
    }

    /**
     * The title is a the name for the chart. A page link will
     * be generated using the title. The title will also be
     * used for a page index.
     * @return chart title
     */
    public String getTitle() {
        return getPropertyAsString(REPORT_CHART_TITLE);
    }

    /**
     * The title is a the name for the chart. A page link will
     * be generated using the title. The title will also be
     * used for a page index.
     * @param title
     */
    public void setTitle(String title) {
        setProperty(REPORT_CHART_TITLE,title);
    }

    /**
     * The caption is a description for the chart explaining
     * what the chart means.
     * @return caption
     */
    public String getCaption() {
        return getPropertyAsString(REPORT_CHART_CAPTION);
    }

    /**
     * The caption is a description for the chart explaining
     * what the chart means.
     * @param caption
     */
    public void setCaption(String caption) {
        setProperty(REPORT_CHART_CAPTION,caption);
    }

    /**
     * if no width is set, the default is returned
     * @return width
     */
    public int getWidth() {
        int w = getPropertyAsInt(REPORT_CHART_WIDTH);
        if (w <= 0) {
            return DEFAULT_WIDTH;
        } else {
            return w;
        }
    }

    /**
     * set the width of the graph
     * @param width
     */
    public void setWidth(String width) {
        setProperty(REPORT_CHART_WIDTH,String.valueOf(width));
    }

    /**
     * if the height is not set, the default is returned
     * @return height
     */
    public int getHeight() {
        int h = getPropertyAsInt(REPORT_CHART_HEIGHT);
        if (h <= 0) {
            return DEFAULT_HEIGHT;
        } else {
            return h;
        }
    }

    /**
     * set the height of the graph
     * @param height
     */
    public void setHeight(String height) {
        setProperty(REPORT_CHART_HEIGHT,String.valueOf(height));
    }

    /**
     * Subclasses will need to implement the method by doing the following:
     * 1. get the x and y axis
     * 2. filter the table data
     * 3. pass the data to the chart library
     * 4. return the generated chart
     */
    @Override
    public abstract JComponent renderChart(List<DataSet> data);

    /**
     * this makes it easy to get the bufferedImage
     * @return image
     */
    public BufferedImage getBufferedImage() {
        return this.image;
    }

    /**
     * in case an user wants set the bufferdImage
     * @param img
     */
    public void setBufferedImage(BufferedImage img) {
        this.image = img;
    }

    /**
     * convienance method for getting the selected value. Rather than use
     * Method.invoke(Object,Object[]), it's simpler to just check which
     * column is selected and call the method directly.
     * @param stat
     * @return value
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
            return stat.getKBPerSecond();
        } else {
            return Double.NaN;
        }
    }
}
