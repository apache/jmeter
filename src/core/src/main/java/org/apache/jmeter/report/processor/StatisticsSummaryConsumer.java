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

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The class StatisticsSummaryConsumer provides a consumer that calculates:
 * <ul>
 *      <li>total number of samples</li>
 *      <li>errors</li>
 *      <li>error %</li>
 *      <li>mean response time</li>
 *      <li>percentile 1 (90% by default)</li>
 *      <li>percentile 2 (95% by default)</li>
 *      <li>percentile 3 (99% by default)</li>
 *      <li>throughput</li>
 *      <li>received bytes per second</li>
 *      <li>sent bytes per second</li>
 *      <li>min</li>
 *      <li>max</li>
 * </ul>
 *
 * @since 3.0
 */
public class StatisticsSummaryConsumer extends
        AbstractSummaryConsumer<StatisticsSummaryData> {

    private static final int PERCENTILE_INDEX1 = JMeterUtils.getPropDefault(
        "aggregate_rpt_pct1", 90);
    private static final int PERCENTILE_INDEX2 = JMeterUtils.getPropDefault(
        "aggregate_rpt_pct2", 95);
    private static final int PERCENTILE_INDEX3 = JMeterUtils.getPropDefault(
        "aggregate_rpt_pct3", 99);

    /**
     * Instantiates a new statistics summary consumer.
     */
    public StatisticsSummaryConsumer() {
        super(true);
    }

    /**
     *
     * @param sample {@link Sample}
     * @param data {@link StatisticsSummaryData}
     * @param isOverall boolean indicating if aggregation concerns the Overall results in which case we ignore Transaction Controller's SampleResult
     */
    private void aggregateSample(Sample sample, StatisticsSummaryData data, boolean isOverall) {
        if(isOverall && sample.isController()) {
            return;
        }
        data.incTotal();
        data.incBytes(sample.getReceivedBytes());
        data.incSentBytes(sample.getSentBytes());

        if (!sample.getSuccess()) {
            data.incErrors();
        }

        long elapsedTime = sample.getElapsedTime();
        data.getPercentile1().addValue(elapsedTime);
        data.getPercentile2().addValue(elapsedTime);
        data.getPercentile3().addValue(elapsedTime);
        data.getMean().addValue(elapsedTime);
        data.setMin(elapsedTime);
        data.setMax(elapsedTime);

        data.setFirstTime(sample.getStartTime());

        data.setEndTime(sample.getEndTime());
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
        SummaryInfo overallInfo = getOverallInfo();
        StatisticsSummaryData overallData = overallInfo.getData();
        if (overallData == null) {
            overallData = new StatisticsSummaryData(PERCENTILE_INDEX1,
                            PERCENTILE_INDEX2, PERCENTILE_INDEX3);
            overallInfo.setData(overallData);
        }

        StatisticsSummaryData data = info.getData();
        if (data == null) {
            data = new StatisticsSummaryData(PERCENTILE_INDEX1,
                        PERCENTILE_INDEX2, PERCENTILE_INDEX3);
            info.setData(data);
        }

        if(!sample.isEmptyController()) {
            aggregateSample(sample, data, false);
            aggregateSample(sample, overallData, true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createDataResult
     * (java.lang.String)
     */
    @Override
    protected ListResultData createDataResult(String key,
            StatisticsSummaryData data) {
        ListResultData result = new ListResultData();
        result.addResult(new ValueResultData(
                key != null ? key : JMeterUtils.getResString("reportgenerator_summary_total")));
        long total = data.getTotal();
        long errors = data.getErrors();
        result.addResult(new ValueResultData(Long.valueOf(total)));
        result.addResult(new ValueResultData(Long.valueOf(errors)));
        result.addResult(new ValueResultData(Double.valueOf((double) errors * 100 / total)));
        result.addResult(new ValueResultData(Double.valueOf(data.getMean().getResult())));
        result.addResult(new ValueResultData(Long.valueOf(data.getMin())));
        result.addResult(new ValueResultData(Long.valueOf(data.getMax())));
        result.addResult(new ValueResultData(Double.valueOf(data.getPercentile1().getResult())));
        result.addResult(new ValueResultData(Double.valueOf(data.getPercentile2().getResult())));
        result.addResult(new ValueResultData(Double.valueOf(data.getPercentile3().getResult())));
        result.addResult(new ValueResultData(Double.valueOf(data.getThroughput())));
        result.addResult(new ValueResultData(Double.valueOf(data.getKBytesPerSecond())));
        result.addResult(new ValueResultData(Double.valueOf(data.getSentKBytesPerSecond())));
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
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createResultTitles
     * ()
     */
    @Override
    protected ListResultData createResultTitles() {
        ListResultData titles = new ListResultData();
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_summary_statistics_label")));
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_summary_statistics_count")));
        titles.addResult(new ValueResultData(JMeterUtils
            .getResString("reportgenerator_summary_statistics_error_count")));
        titles.addResult(new ValueResultData(
            JMeterUtils.getResString("reportgenerator_summary_statistics_error_percent")));
        titles.addResult(new ValueResultData(
                JMeterUtils.getResString("reportgenerator_summary_statistics_mean")));
        titles.addResult(new ValueResultData(JMeterUtils.getResString("reportgenerator_summary_statistics_min")));
        titles.addResult(new ValueResultData(JMeterUtils.getResString("reportgenerator_summary_statistics_max")));
        titles.addResult(new ValueResultData(
            String.format(
                JMeterUtils.getResString("reportgenerator_summary_statistics_percentile_fmt"),
                Integer.valueOf(PERCENTILE_INDEX1))));
        titles.addResult(new ValueResultData(
            String.format(
                JMeterUtils.getResString("reportgenerator_summary_statistics_percentile_fmt"),
                Integer.valueOf(PERCENTILE_INDEX2))));
        titles.addResult(new ValueResultData(
            String.format(
                JMeterUtils.getResString("reportgenerator_summary_statistics_percentile_fmt"),
                Integer.valueOf(PERCENTILE_INDEX3))));
        titles.addResult(new ValueResultData(JMeterUtils.getResString("reportgenerator_summary_statistics_throughput")));
        titles.addResult(new ValueResultData(JMeterUtils.getResString("reportgenerator_summary_statistics_kbytes")));
        titles.addResult(new ValueResultData(JMeterUtils.getResString("reportgenerator_summary_statistics_sent_kbytes")));

        return titles;
    }

}
