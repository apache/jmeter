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
import org.apache.jmeter.report.core.TimeHelper;

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
 * @since 2.14
 */
public class ApdexSummaryConsumer extends AbstractSummaryConsumer {

    private class ApdexCount {
	long satisfiedCount = 0L;
	long toleratedCount = 0L;
	long totalCount = 0L;
    }

    /**
     * The class ApdexResult provides a container for APDEX calculation result.
     */
    public class ApdexResult {
	private ApdexThresholdsInfo thresholdsInfo;
	private Double apdex;

	/**
	 * Gets the APDEX thresholds information.
	 *
	 * @return the thresholds information
	 */
	public final ApdexThresholdsInfo getThresholdsInfo() {
	    return thresholdsInfo;
	}

	/**
	 * Sets the APDEX thresholds information.
	 *
	 * @param thresholdsInfo
	 *            the APDEX thresholds information to set
	 */
	public final void setThresholdsInfo(ApdexThresholdsInfo thresholdsInfo) {
	    this.thresholdsInfo = thresholdsInfo;
	}

	/**
	 * Gets the apdex result.
	 *
	 * @return the apdex result
	 */
	public final Double getApdex() {
	    return apdex;
	}

	/**
	 * Sets the apdex result.
	 *
	 * @param apdex
	 *            the apdex result to set
	 */
	public final void setApdex(Double apdex) {
	    this.apdex = apdex;
	}
    }

    private Map<String, ApdexCount> counts = new HashMap<String, ApdexCount>();
    private ThresholdSelector thresholdSelector;
    private ApdexCount overallCount;
    private ApdexResult overallResult;
    private Map<String, ApdexResult> apdexValues = new TreeMap<String, ApdexResult>();

    /**
     * Gets the apdex result values.
     *
     * @return the apdex result values
     */
    public final Iterable<Map.Entry<String, ApdexResult>> getApdexValues() {
	return apdexValues.entrySet();
    }

    /**
     * Gets the APDEX threshold selector.
     *
     * @return the threshold selector
     */
    public final ThresholdSelector getThresholdSelector() {
	return thresholdSelector;
    }

    /**
     * Sets the APDEX threshold selector.
     *
     * @param thresholdSelector
     *            the APDEX threshold selector to set
     */
    public final void setThresholdSelector(ThresholdSelector thresholdSelector) {
	this.thresholdSelector = thresholdSelector;
    }

    /**
     * Append Apdex information to JSON builder.
     *
     * @param builder
     *            the JSON builder
     * @param sample
     *            the sample
     * @param result
     *            the apdex result
     * @param index
     *            the index of the line
     */
    private void appendLineToBuilder(JsonObjectBuilder builder, String sample,
	    ApdexResult result, int index) {
	JsonObjectBuilder seriesBuilder = Json.createObjectBuilder();
	ApdexThresholdsInfo info = result.getThresholdsInfo();
	seriesBuilder
	        .add("Apdex", String.format("%.3f", result.getApdex()))
	        .add("T",
	                TimeHelper.formatDuration(info.getSatisfiedThreshold(),
	                        false))
	        .add("F",
	                TimeHelper.formatDuration(info.getToleratedThreshold(),
	                        false)).add("Samplers", sample);
	builder.add(Integer.toString(index), seriesBuilder);
    }

    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();
	apdexValues.clear();
	overallCount = new ApdexCount();
	overallResult = new ApdexResult();
	overallResult.thresholdsInfo = thresholdSelector.select("");

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    @Override
    public void consume(Sample sample, int channel) {
	// Each result is define by name of samplers so get the name of the
	// sample
	String name = sample.getName();

	// Get the object to store counters or create it if it does not exist.
	ApdexCount apdexCount = counts.get(name);
	if (apdexCount == null) {
	    apdexCount = new ApdexCount();
	    counts.put(name, apdexCount);
	}

	// Increment the total count of samples with the current name
	apdexCount.totalCount++;

	// Increment the total count of samples
	overallCount.totalCount++;

	// Get the APDEX result for the current name or create it if it does not
	// exist
	ApdexResult result = apdexValues.get(name);
	if (result == null) {
	    result = new ApdexResult();
	    result.thresholdsInfo = thresholdSelector.select(name);
	    apdexValues.put(name, result);
	}

	ApdexThresholdsInfo overallInfo = overallResult.getThresholdsInfo();

	// Process only succeeded samples
	if (sample.getSuccess()) {
	    long elapsedTime = sample.getElapsedTime();
	    ApdexThresholdsInfo info = result.getThresholdsInfo();

	    // Increment the counters depending on the elapsed time.
	    if (elapsedTime <= info.getSatisfiedThreshold()) {
		apdexCount.satisfiedCount++;
	    } else if (elapsedTime <= info.getToleratedThreshold()) {
		apdexCount.toleratedCount++;
	    }

	    // Increment the overall counters depending on the elapsed time.
	    if (elapsedTime <= overallInfo.getSatisfiedThreshold()) {
		overallCount.satisfiedCount++;
	    } else if (elapsedTime <= overallInfo.getToleratedThreshold()) {
		overallCount.toleratedCount++;
	    }
	}
	super.produce(sample, channel);
    }

    private double getApdex(ApdexCount count) {
	return (count.satisfiedCount + count.toleratedCount / 2.0d)
	        / count.totalCount;
    }

    @Override
    public void stopConsuming() {
	// Calculate overall APDEX
	overallResult.setApdex(getApdex(overallCount));

	// Calculate APDEX for each sample name and complete the result map
	for (Map.Entry<String, ApdexCount> entry : counts.entrySet()) {
	    apdexValues.get(entry.getKey())
		    .setApdex(getApdex(entry.getValue()));
	}
	super.stopProducing();
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
	for (Map.Entry<String, ApdexResult> entry : apdexValues.entrySet()) {
	    index++;
	    appendLineToBuilder(builder, entry.getKey(), entry.getValue(),
		    index);
	}
	dataResult.put("values", JsonUtil.convertJsonToString(builder.build()));
	return dataResult;
    }
}
