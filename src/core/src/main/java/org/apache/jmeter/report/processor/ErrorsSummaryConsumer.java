/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.report.processor;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

/**
 * <p>
 * The class ErrorSummaryConsumer provides a consumer that calculates error
 * statistics.
 * </p>
 *
 * @since 3.0
 */
public class ErrorsSummaryConsumer extends AbstractSummaryConsumer<Long> {

    static final boolean ASSERTION_RESULTS_FAILURE_MESSAGE =
            JMeterUtils
                .getPropDefault(
                        SampleSaveConfiguration.ASSERTION_RESULTS_FAILURE_MESSAGE_PROP,
                        true);

    private static final Long ZERO = 0L;
    private long errorCount = 0L;

    /**
     * Instantiates a new errors summary consumer.
     */
    public ErrorsSummaryConsumer() {
        super(false);
    }

    @Override
    protected ListResultData createDataResult(String key, Long data) {
        ListResultData result = new ListResultData();
        result.addResult(new ValueResultData(key != null ? key : JMeterUtils
                .getResString("reportgenerator_summary_total")));
        result.addResult(new ValueResultData(data));
        result.addResult(new ValueResultData((double) data * 100 / errorCount));
        result.addResult(new ValueResultData((double) data * 100
                / getOverallInfo().getData().doubleValue()));
        return result;
    }

    @Override
    protected String getKeyFromSample(Sample sample) {
        return getErrorKey(sample);
    }

    /**
     * @param sample {@link Sample}
     * @return String Error key for sample
     */
    static String getErrorKey(Sample sample) {
        if (sample.getSuccess()) {
            return "";
        }
        String responseCode = sample.getResponseCode();
        String responseMessage = sample.getResponseMessage();
        String key = responseCode + (StringUtilities.isNotEmpty(responseMessage) ?
                 "/" + escapeJson(responseMessage) : "");

        if (MetricUtils.isSuccessCode(responseCode) ||
                (StringUtilities.isEmpty(responseCode) &&
                   StringUtilities.isNotBlank(sample.getFailureMessage()))) {
            key = MetricUtils.ASSERTION_FAILED;
            if (ASSERTION_RESULTS_FAILURE_MESSAGE) {
                String msg = sample.getFailureMessage();
                if (StringUtilities.isNotBlank(msg)) {
                    key = escapeJson(msg);
                }
            }
        }
        return key;
    }

    private static String escapeJson(String responseMessage) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(HtmlEscape.escapeHtml5(responseMessage)));
    }

    @Override
    protected void updateData(SummaryInfo info, Sample sample) {
        // Initialize overall data if they don't exist
        SummaryInfo overallInfo = getOverallInfo();
        Long overallData = overallInfo.getData();
        if (overallData == null) {
            overallData = ZERO;
        }
        overallInfo.setData(overallData + 1);

        // Process only failed samples
        if (!sample.getSuccess()) {
            errorCount++;

            Long data = info.getData();
            if (data == null) {
                data = ZERO;
            }
            info.setData(data + 1);
        }
    }

    @Override
    public void stopConsuming() {
        super.stopConsuming();

        // Reset state
        errorCount = 0L;
    }

    @Override
    protected ListResultData createResultTitles() {
        ListResultData titles = new ListResultData();
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_errors_type")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_errors_count")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_errors_rate_error")));
        titles.addResult(new ValueResultData(JMeterUtils
                .getResString("reportgenerator_summary_errors_rate_all")));
        return titles;
    }
}
