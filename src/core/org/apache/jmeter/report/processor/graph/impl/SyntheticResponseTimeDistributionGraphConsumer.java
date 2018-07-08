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
package org.apache.jmeter.report.processor.graph.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.SumAggregatorFactory;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.SeriesData;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class SyntheticResponseTimeDistributionGraphConsumer provides a graph to visualize
 * the distribution of the response times on APDEX threshold
 *
 * @since 3.1
 */
public class SyntheticResponseTimeDistributionGraphConsumer extends
        AbstractGraphConsumer {
    private static final String FAILED_LABEL = JMeterUtils.getResString("response_time_distribution_failed_label");
    private static final MessageFormat SATISFIED_LABEL = new MessageFormat(
            JMeterUtils.getResString("response_time_distribution_satisfied_label"));
    private static final MessageFormat TOLERATED_LABEL = new MessageFormat(
            JMeterUtils.getResString("response_time_distribution_tolerated_label"));
    private static final MessageFormat UNTOLERATED_LABEL = new MessageFormat(
            JMeterUtils.getResString("response_time_distribution_untolerated_label"));
    private static final String SERIE_COLOR_PROPERTY = "color";
    private static final String SATISFIED_COLOR = "#9ACD32";
    private static final String TOLERATED_COLOR = "yellow";
    private static final String UNTOLERATED_COLOR = "orange";
    private static final String FAILED_COLOR = "#FF6347";

    private long satisfiedThreshold;
    private long toleratedThreshold;
    List<String> satisfiedLabels = Collections.emptyList();
    List<String> toleratedLabels = Collections.emptyList();
    List<String> untoleratedLabels = Collections.emptyList();

    private class SyntheticSeriesSelector extends AbstractSeriesSelector {
        @Override
        public Iterable<String> select(Sample sample) {
            if(!sample.getSuccess()) {
                return Arrays.asList(FAILED_LABEL);
            } else {
                long elapsedTime = sample.getElapsedTime();
                if(elapsedTime<=getSatisfiedThreshold()) {
                    return satisfiedLabels;
                } else if(elapsedTime <= getToleratedThreshold()) {
                    return toleratedLabels;
                } else {
                    return untoleratedLabels;
                }
            }
        }
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createKeysSelector()
     */
    @Override
    protected final GraphKeysSelector createKeysSelector() {
        return new GraphKeysSelector() {

            @Override
            public Double select(Sample sample) {
                if(sample.getSuccess()) {
                    long elapsedTime = sample.getElapsedTime();
                    if(elapsedTime<=satisfiedThreshold) {
                        return Double.valueOf(0);
                    } else if(elapsedTime <= toleratedThreshold) {
                        return Double.valueOf(1);
                    } else {
                        return Double.valueOf(2);
                    }
                } else {
                    return Double.valueOf(3);
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        Map<String, GroupInfo> groupInfos = new HashMap<>(1);
        SyntheticSeriesSelector syntheticSeriesSelector = new SyntheticSeriesSelector();
        groupInfos.put(AbstractGraphConsumer.DEFAULT_GROUP, new GroupInfo(
                new SumAggregatorFactory(), syntheticSeriesSelector,
                // We ignore Transaction Controller results
                new CountValueSelector(true), false, false));

        return groupInfos;
    }

    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        ListResultData listResultData = new ListResultData();
        String[] seriesLabels = new String[]{
                SATISFIED_LABEL.format(new Object[] {Long.valueOf(getSatisfiedThreshold())}),
                TOLERATED_LABEL.format(new Object[] {Long.valueOf(getSatisfiedThreshold()), Long.valueOf(getToleratedThreshold())}),
                UNTOLERATED_LABEL.format(new Object[] {Long.valueOf(getToleratedThreshold())}),
                FAILED_LABEL
        };
        String[] colors = new String[]{
                SATISFIED_COLOR, TOLERATED_COLOR, UNTOLERATED_COLOR, FAILED_COLOR 
        };
        for (int i = 0; i < seriesLabels.length; i++) {
            ListResultData array = new ListResultData();
            array.addResult(new ValueResultData(Integer.valueOf(i)));
            array.addResult(new ValueResultData(seriesLabels[i]));
            listResultData.addResult(array);
        }
        parentResult.setResult("ticks", listResultData);
        initializeSeries(parentResult, seriesLabels, colors);
    }

    private void initializeSeries(MapResultData parentResult, String[] series, String[] colors) {
        ListResultData listResultData = (ListResultData) parentResult.getResult("series");
        for (int i = 0; i < series.length; i++) {
            listResultData.addResult(create(series[i], colors[i]));
        }
    }
    
    private MapResultData create(String serie, String color) {
        GroupInfo groupInfo = getGroupInfos().get(AbstractGraphConsumer.DEFAULT_GROUP);
        SeriesData seriesData = new SeriesData(groupInfo.getAggregatorFactory(), 
                groupInfo.enablesAggregatedKeysSeries(), false,
                groupInfo.enablesOverallSeries()); 
        MapResultData seriesResult = createSerieResult(serie, seriesData);
        seriesResult.setResult(SERIE_COLOR_PROPERTY, new ValueResultData(color));
        return seriesResult;
    }

    /**
     * @return the satisfiedThreshold
     */
    public long getSatisfiedThreshold() {
        return satisfiedThreshold;
    }

    /**
     * @param satisfiedThreshold the satisfiedThreshold to set
     */
    public void setSatisfiedThreshold(long satisfiedThreshold) {
        this.satisfiedThreshold = satisfiedThreshold;
        formatLabels();
    }

    /**
     * @return the toleratedThreshold
     */
    public long getToleratedThreshold() {
        return toleratedThreshold;
    }

    /**
     * @param toleratedThreshold the toleratedThreshold to set
     */
    public void setToleratedThreshold(long toleratedThreshold) {
        this.toleratedThreshold = toleratedThreshold;
        formatLabels();
    }

    private void formatLabels() {
        this.satisfiedLabels = Collections
                .singletonList(SATISFIED_LABEL.format(new Object[] { Long.valueOf(this.satisfiedThreshold) }));
        this.toleratedLabels = Collections.singletonList(TOLERATED_LABEL
                .format(new Object[] { Long.valueOf(this.satisfiedThreshold), Long.valueOf(this.toleratedThreshold) }));
        this.untoleratedLabels = Collections
                .singletonList(UNTOLERATED_LABEL.format(new Object[] { Long.valueOf(this.toleratedThreshold) }));
    }
}
