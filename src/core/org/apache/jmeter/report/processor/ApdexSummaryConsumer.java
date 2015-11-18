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

    private Map<String, ApdexCount> counts = new HashMap<String, ApdexCount>();
    private ThresholdSelector thresholdSelector;
    private ApdexCount overallCount;

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

    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();
	overallCount = new ApdexCount();

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

	// Process only succeeded samples
	if (sample.getSuccess()) {
	    long elapsedTime = sample.getElapsedTime();

	    ApdexThresholdsInfo info = getThresholdSelector().select(name);

	    // Increment the counters depending on the elapsed time.
	    if (elapsedTime <= info.getSatisfiedThreshold()) {
		apdexCount.satisfiedCount++;
	    } else if (elapsedTime <= info.getToleratedThreshold()) {
		apdexCount.toleratedCount++;
	    }

	    ApdexThresholdsInfo overallInfo = getThresholdSelector().select("");

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
	return (count.satisfiedCount + (double) count.toleratedCount / 2)
	        / count.totalCount;
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

    private ListResultData createResultItem(String name, ApdexCount count,
	    ApdexThresholdsInfo info) {
	ListResultData result = new ListResultData();

	result.addResult(new ValueResultData(getApdex(count)));

	result.addResult(new ValueResultData(info.getSatisfiedThreshold()));

	result.addResult(new ValueResultData(info.getToleratedThreshold()));

	result.addResult(new ValueResultData(name));
	
	return result;
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
     * org.apache.jmeter.report.processor.AbstractSummaryConsumer#createResultItem
     * (java.lang.String)
     */
    @Override
    protected ListResultData createResultItem(String name) {
	ApdexThresholdsInfo info = getThresholdSelector().select(name);
	ApdexCount count;
	if ("".equals(name)) {
	    name = JMeterUtils.getResString("reportgenerator_summary_total");
	    count = overallCount;
	} else {
	    count = counts.get(name);
	}
	return createResultItem(name, count, info);
    }

}
