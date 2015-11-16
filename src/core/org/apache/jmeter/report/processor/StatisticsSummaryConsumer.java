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

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.util.JMeterUtils;

/**
 * <p>
 * The class ErrorSummaryConsumer provides a consumer that calculates error
 * statistics.
 * </p>
 * 
 * @since 2.14
 */
// TODO Add support of "TOTAL" statistics line
public class StatisticsSummaryConsumer extends AbstractSummaryConsumer {

    private static final int percentileIndex1 = JMeterUtils.getPropDefault(
	    "aggregate_rpt_pct1", 90);
    private static final int percentileIndex2 = JMeterUtils.getPropDefault(
	    "aggregate_rpt_pct2", 95);
    private static final int percentileIndex3 = JMeterUtils.getPropDefault(
	    "aggregate_rpt_pct3", 99);

    private class StatisticsInfo {
	long firstTime = Long.MAX_VALUE;
	long endTime = Long.MIN_VALUE;
	long bytes = 0L;
	long errors = 0L;
	long total = 0L;
	PercentileAggregator percentile1 = new PercentileAggregator(
	        percentileIndex1);
	PercentileAggregator percentile2 = new PercentileAggregator(
	        percentileIndex2);
	PercentileAggregator percentile3 = new PercentileAggregator(
	        percentileIndex3);
	long min = Long.MAX_VALUE;
	long max = Long.MIN_VALUE;

	public void clear() {
	    firstTime = Long.MAX_VALUE;
	    endTime = Long.MIN_VALUE;
	    bytes = 0L;
	    errors = 0L;
	    total = 0L;
	    percentile1.reset();
	    percentile2.reset();
	    percentile3.reset();
	    min = Long.MAX_VALUE;
	    max = Long.MIN_VALUE;
	}

	public long getElapsedTime() {
	    return endTime - firstTime;
	}

	public double getBytesPerSecond() {
	    return bytes / ((double) getElapsedTime() / 1000);
	}

	public double getKBytesPerSecond() {
	    return getBytesPerSecond() / 1024;
	}

	public double getThroughput() {
	    return (total / (double) getElapsedTime()) * 1000.0;
	}
    }

    private Map<String, StatisticsInfo> counts = new HashMap<String, StatisticsInfo>();
    private StatisticsInfo overallInfo = new StatisticsInfo();

    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();
	overallInfo.clear();

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    void aggregateSample(Sample sample, StatisticsInfo info) {
	info.total++;
	info.bytes += sample.getSentBytes();

	if (sample.getSuccess() == false) {
	    info.errors++;
	}

	long elapsedTime = sample.getElapsedTime();
	info.percentile1.addValue((double) elapsedTime);
	info.percentile2.addValue((double) elapsedTime);
	info.percentile3.addValue((double) elapsedTime);

	info.min = Math.min(info.min, elapsedTime);
	info.max = Math.max(info.max, elapsedTime);

	long startTime = sample.getStartTime();
	info.firstTime = Math.min(info.firstTime, startTime);

	long endTime = sample.getEndTime();
	info.endTime = Math.max(info.endTime, endTime);
    }

    @Override
    public void consume(Sample sample, int channel) {
	// Each result is defined by name of samples so get the name of the
	// sample
	String name = sample.getName();

	// Get the object to store counters or create it if it does not exist.
	StatisticsInfo info = counts.get(name);
	if (info == null) {
	    info = new StatisticsInfo();
	    counts.put(name, info);
	}

	aggregateSample(sample, info);
	aggregateSample(sample, overallInfo);

	super.produce(sample, channel);
    }

    @Override
    public void stopConsuming() {
	storeResult(counts.keySet());
	super.stopProducing();
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
	        JMeterUtils
	                .getResString("reportgenerator_summary_statistics_error_percent")));
	titles.addResult(new ValueResultData(
	        String.format(
	                JMeterUtils
	                        .getResString("reportgenerator_summary_statistics_percentile_fmt"),
	                percentileIndex1)));
	titles.addResult(new ValueResultData(
	        String.format(
	                JMeterUtils
	                        .getResString("reportgenerator_summary_statistics_percentile_fmt"),
	                percentileIndex2)));
	titles.addResult(new ValueResultData(
	        String.format(
	                JMeterUtils
	                        .getResString("reportgenerator_summary_statistics_percentile_fmt"),
	                percentileIndex3)));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_statistics_throughput")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_statistics_kbytes")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_statistics_min")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_statistics_max")));
	return titles;
    }

    private ListResultData createResultItem(String name, StatisticsInfo info) {
	ListResultData result = new ListResultData();
	result.addResult(new ValueResultData(name));
	result.addResult(new ValueResultData(info.total));
	result.addResult(new ValueResultData(info.errors));
	result.addResult(new ValueResultData(String.format("%.2f%%",
	        (double) info.errors * 100 / overallInfo.total)));
	result.addResult(new ValueResultData(String.format("%.2f",
	        info.percentile1.getResult())));
	result.addResult(new ValueResultData(String.format("%.2f",
	        info.percentile2.getResult())));
	result.addResult(new ValueResultData(String.format("%.2f",
	        info.percentile3.getResult())));
	result.addResult(new ValueResultData(String.format("%.2f",
	        info.getThroughput())));
	result.addResult(new ValueResultData(String.format("%.2f",
	        info.getKBytesPerSecond())));
	result.addResult(new ValueResultData(Long.toString(info.min)));
	result.addResult(new ValueResultData(Long.toString(info.max)));
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createResultItem
     * (java.lang.String)
     */
    @Override
    protected ListResultData createResultItem(String name) {
	StatisticsInfo info;
	if ("".equals(name)) {
	    name = JMeterUtils.getResString("reportgenerator_summary_total");
	    info = overallInfo;
	} else {
	    info = counts.get(name);
	}
	return createResultItem(name, info);
    }
}
