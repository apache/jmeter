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
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.JsonUtil;
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

    /**
     * The class StatisticsResult provides a container for statistics
     * calculation result.
     */
    public class StatisticsResult {
	private long totalCount;
	private long errorCount;
	private Double errorPercentage;
	private Double percentile1;
	private Double percentile2;
	private Double percentile3;
	private Double throughput;

	private Double byteRate;
	private long min;
	private long max;

	/**
	 * Gets the total number of samples.
	 *
	 * @return the total number of samples
	 */
	public final long getTotalCount() {
	    return totalCount;
	}

	/**
	 * Sets the total number of samples.
	 *
	 * @param totalCount
	 *            the total number of samples to set
	 */
	public final void setTotalCount(long totalCount) {
	    this.totalCount = totalCount;
	}

	/**
	 * Gets the number of errors.
	 *
	 * @return the number of errors
	 */
	public final long getErrorCount() {
	    return errorCount;
	}

	/**
	 * Sets the number of errors.
	 *
	 * @param errorCount
	 *            the number of errors to set
	 */
	public final void setErrorCount(long errorCount) {
	    this.errorCount = errorCount;
	}

	/**
	 * Gets the percentage of errors.
	 *
	 * @return the percentage of errors
	 */
	public final Double getErrorPercentage() {
	    return errorPercentage;
	}

	/**
	 * Sets the percentage of errors.
	 *
	 * @param errorPercentage
	 *            the percentage of errors to set
	 */
	public final void setErrorPercentage(Double errorPercentage) {
	    this.errorPercentage = errorPercentage;
	}

	/**
	 * Gets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct1.
	 *
	 * @return the percentile of elapsed time matching the property
	 *         aggregate_rpt_pct1
	 */
	public final Double getPercentile1() {
	    return percentile1;
	}

	/**
	 * Sets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct1.
	 *
	 * @param percentile1
	 *            the percentile of elapsed time matching the property
	 *            aggregate_rpt_pct1 to set
	 */
	public final void setPercentile1(Double percentile1) {
	    this.percentile1 = percentile1;
	}

	/**
	 * Gets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct2.
	 *
	 * @return the percentile of elapsed time matching the property
	 *         aggregate_rpt_pct2
	 */
	public final Double getPercentile2() {
	    return percentile2;
	}

	/**
	 * Sets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct2.
	 *
	 * @param percentile2
	 *            the percentile of elapsed time matching the property
	 *            aggregate_rpt_pct2 to set
	 */
	public final void setPercentile2(Double percentile2) {
	    this.percentile2 = percentile2;
	}

	/**
	 * Gets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct3.
	 *
	 * @return the percentile of elapsed time matching the property
	 *         aggregate_rpt_pct3
	 */
	public final Double getPercentile3() {
	    return percentile3;
	}

	/**
	 * Sets the percentile of elapsed time matching the property
	 * aggregate_rpt_pct3.
	 *
	 * @param percentile3
	 *            the percentile of elapsed time matching the property
	 *            aggregate_rpt_pct3 to set
	 */
	public final void setPercentile3(Double percentile3) {
	    this.percentile3 = percentile3;
	}

	/**
	 * Gets the throughput.
	 *
	 * @return the throughput
	 */
	public final Double getThroughput() {
	    return throughput;
	}

	/**
	 * Sets the throughput.
	 *
	 * @param throughput
	 *            the throughput to set
	 */
	public final void setThroughput(Double throughput) {
	    this.throughput = throughput;
	}

	/**
	 * Gets the byte rate.
	 *
	 * @return the byteRate
	 */
	public final Double getByteRate() {
	    return byteRate;
	}

	/**
	 * Sets the byte rate.
	 *
	 * @param byteRate
	 *            the byte rate to set
	 */
	public final void setByteRate(Double byteRate) {
	    this.byteRate = byteRate;
	}

	/**
	 * Gets the minimum elapsed time.
	 *
	 * @return the minimum elapsed time
	 */
	public final long getMin() {
	    return min;
	}

	/**
	 * Sets the minimum elapsed time.
	 *
	 * @param min
	 *            the minimum elapsed time to set
	 */
	public final void setMin(long min) {
	    this.min = min;
	}

	/**
	 * Gets the maximum elapsed time.
	 *
	 * @return the maximum elapsed time
	 */
	public final long getMax() {
	    return max;
	}

	/**
	 * Sets the maximum elapsed time.
	 *
	 * @param max
	 *            the maximum elapsed time to set
	 */
	public final void setMax(long max) {
	    this.max = max;
	}

    }

    private Map<String, StatisticsInfo> counts = new HashMap<String, StatisticsInfo>();
    private Map<String, StatisticsResult> results = new TreeMap<String, StatisticsResult>();
    private StatisticsInfo overallInfo = new StatisticsInfo();
    private StatisticsResult overallResult;

    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();
	results.clear();
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

    private StatisticsResult createResult(StatisticsInfo info, long total) {
	StatisticsResult result = new StatisticsResult();
	result.setTotalCount(info.total);
	result.setErrorCount(info.errors);
	result.setErrorPercentage((double) info.errors * 100 / total);
	result.setPercentile1(info.percentile1.getResult());
	result.setPercentile2(info.percentile2.getResult());
	result.setPercentile3(info.percentile3.getResult());
	result.setThroughput(info.getThroughput());
	result.setByteRate(info.getKBytesPerSecond());
	result.setMin(info.min);
	result.setMax(info.max);
	return result;
    }

    @Override
    public void stopConsuming() {
	// Calculate percentage for each sample name and build the result map
	for (Map.Entry<String, StatisticsInfo> entry : counts.entrySet()) {
	    StatisticsInfo info = entry.getValue();
	    results.put(entry.getKey(), createResult(info, overallInfo.total));
	}
	overallResult = createResult(overallInfo, overallInfo.total);
	super.stopProducing();
    }

    private void appendLineToBuilder(JsonObjectBuilder builder, String sample,
	    StatisticsResult result, int index) {
	JsonObjectBuilder seriesBuilder = Json.createObjectBuilder();
	seriesBuilder
	        .add("Label", sample)
	        .add("#Samples", Long.toString(result.getTotalCount()))
	        .add("KO", Long.toString(result.getErrorCount()))
	        .add("Error%",
	                String.format("%.2f%%", result.getErrorPercentage()))
	        .add(String.format("%d%% Line", percentileIndex1),
	                String.format("%.2f", result.getPercentile1()))
	        .add(String.format("%d%% Line", percentileIndex2),
	                String.format("%.2f", result.getPercentile2()))
	        .add(String.format("%d%% Line", percentileIndex3),
	                String.format("%.2f", result.getPercentile3()))
	        .add("Throughput",
	                String.format("%.2f", result.getThroughput()))
	        .add("KB/sec", String.format("%.2f", result.getByteRate()))
	        .add("Min", Long.toString(result.getMin()))
	        .add("Max", Long.toString(result.getMax()));
	builder.add(Integer.toString(index), seriesBuilder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.AbstractSummaryConsumer#exportData
     * ()
     */
    @Override
    public DataContext exportData() {
	DataContext dataResult = new DataContext();
	JsonObjectBuilder builder = Json.createObjectBuilder();
	int index = 1;
	appendLineToBuilder(builder, "TOTAL", overallResult, index);
	for (Map.Entry<String, StatisticsResult> entry : results.entrySet()) {
	    index++;
	    appendLineToBuilder(builder, entry.getKey(), entry.getValue(),
		    index);
	}
	dataResult.put("values", JsonUtil.convertJsonToString(builder.build()));
	return dataResult;
    }
}
