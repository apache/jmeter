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
package org.apache.jmeter.report.processor;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.util.JMeterUtils;

/**
 * <p>
 * The class ApdexSummaryConsumer provides a consumer that calculates Apdex for
 * each sample name.
 * </p>
 * 
 * <p>
 * T and F thresholds for each sample is defined by the thresholds selector
 * field.
 * </p>
 * 
 * @since 3.0
 */
public class ApdexSummaryConsumer extends
        AbstractSummaryConsumer<ApdexSummaryData> {

    private ThresholdSelector thresholdSelector;

    public final ThresholdSelector getThresholdSelector() {
        return thresholdSelector;
    }

    public final void setThresholdSelector(ThresholdSelector thresholdSelector) {
        this.thresholdSelector = thresholdSelector;
    }

    public ApdexSummaryConsumer() {
        super(true);
    }

    @Override
    protected ListResultData createDataResult(String key, ApdexSummaryData data) {
        Double apdex = Double.valueOf(getApdex(data));
        ApdexThresholdsInfo thresholdsInfo = data.getApdexThresholdInfo();
        Long satisfiedThreshold = Long.valueOf(thresholdsInfo.getSatisfiedThreshold());
        Long toleratedThreshold = Long.valueOf(thresholdsInfo.getToleratedThreshold());
        String keyOrDefault = ObjectUtils.defaultIfNull(
                key, JMeterUtils.getResString("reportgenerator_summary_total"));

        ListResultData result = new ListResultData();
        result.addResult(new ValueResultData(apdex));
        result.addResult(new ValueResultData(satisfiedThreshold));
        result.addResult(new ValueResultData(toleratedThreshold));
        result.addResult(new ValueResultData(keyOrDefault));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#getKeyFromSample
     * (org.apache.jmeter.report.core.Sample)
     */
    @Override
    protected String getKeyFromSample(Sample sample) {
        return sample.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#updateData
     * (org.apache.jmeter.report.processor.AbstractSummaryConsumer.SummaryInfo,
     * org.apache.jmeter.report.core.Sample)
     */
    @Override
    protected void updateData(SummaryInfo info, Sample sample) {
        if(sample.isEmptyController()) {
            return;
        }
        // Initialize overall data if they don't exist
        SummaryInfo overallInfo = getOverallInfo();
        ApdexSummaryData overallData = overallInfo.getData();
        if (overallData == null) {
            overallData = new ApdexSummaryData(getThresholdSelector().select(
                    null));
            overallInfo.setData(overallData);
        }

        // Initialize info data if they don't exist
        ApdexSummaryData data = info.getData();
        if (data == null) {
            data = new ApdexSummaryData(getThresholdSelector().select(
                    sample.getName()));
            info.setData(data);
        }

        // Increment the total count of samples with the current name
        data.incTotalCount();

        // Increment the total count of samples
        overallData.incTotalCount();

        // Process only succeeded samples
        if (sample.getSuccess()) {
            long elapsedTime = sample.getElapsedTime();

            // Increment the counters depending on the elapsed time.
            ApdexThresholdsInfo thresholdsInfo = data.getApdexThresholdInfo();
            if (elapsedTime <= thresholdsInfo.getSatisfiedThreshold()) {
                data.incSatisfiedCount();
            } else if (elapsedTime <= thresholdsInfo.getToleratedThreshold()) {
                data.incToleratedCount();
            }

            // Increment the overall counters depending on the elapsed time.
            ApdexThresholdsInfo overallThresholdsInfo = overallData
                    .getApdexThresholdInfo();
            if (elapsedTime <= overallThresholdsInfo.getSatisfiedThreshold()) {
                overallData.incSatisfiedCount();
            } else if (elapsedTime <= overallThresholdsInfo
                    .getToleratedThreshold()) {
                overallData.incToleratedCount();
            }
        }

    }

    private double getApdex(ApdexSummaryData data) {
        return (data.getSatisfiedCount() + (double) data.getToleratedCount() / 2)
                / data.getTotalCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createResultTitles
     * ()
     */
    @Override
    protected ListResultData createResultTitles() {
        ListResultData titles = new ListResultData();
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_apdex_apdex")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_apdex_satisfied")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_apdex_tolerated")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_apdex_samplers")));
        return titles;
    }

}
