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

/**
 * <p>
 * The class ErrorSummaryConsumer provides a consumer that calculates error
 * statistics.
 * </p>
 * 
 * @since 2.14
 */
public class ErrorsSummaryConsumer extends AbstractSummaryConsumer {

    /**
     * The class ErrorsResult provides a container for errors calculation
     * result.
     */
    public class ErrorsResult {
	private long errorCount;
	private Double errorPercent;
	private Double samplePercent;

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
	 * Gets the error rate over the overall error count.
	 *
	 * @return the error rate over the overall error count
	 */
	public final Double getErrorPercent() {
	    return errorPercent;
	}

	/**
	 * Sets the error rate over the overall error count.
	 *
	 * @param errorPercent
	 *            the error rate over the overall error count
	 */
	public final void setErrorPercent(Double errorPercent) {
	    this.errorPercent = errorPercent;
	}

	/**
	 * Gets the error rate over the sample count.
	 *
	 * @return the error rate over the sample count
	 */
	public final Double getSamplePercent() {
	    return samplePercent;
	}

	/**
	 * Sets the error rate over the sample count.
	 *
	 * @param samplePercent
	 *            the error rate over the sample count to set
	 */
	public final void setSamplePercent(Double samplePercent) {
	    this.samplePercent = samplePercent;
	}

    }

    private Map<String, Long> counts = new HashMap<String, Long>();
    private Map<String, ErrorsResult> errorValues = new TreeMap<String, ErrorsResult>();
    private long errorCount = 0L;
    private long sampleCount = 0L;

    /**
     * Gets the error result values.
     *
     * @return the error result values
     */
    public final Iterable<Map.Entry<String, ErrorsResult>> getErrorValues() {
	return errorValues.entrySet();
    }

    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();
	errorValues.clear();

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    @Override
    public void consume(Sample sample, int channel) {
	// Each result is defined by code of samples so get the code of the
	// sample
	String code = sample.getResponseCode();

	// TODO Do less static, get the assertion field
	if ("200".equals(code)) {
	    code = "Assertion failed";
	}

	// Increment sample count
	sampleCount++;

	// Process only failed samples
	if (sample.getSuccess() == false) {
	    errorCount++;

	    // Increment error count by code
	    if (counts.containsKey(code) == true) {
		counts.put(code, counts.get(code) + 1);
	    } else {
		counts.put(code, Long.valueOf(1));
	    }
	}
	super.produce(sample, channel);
    }

    @Override
    public void stopConsuming() {
	// Calculate percentage for each sample code and build the result map
	for (Map.Entry<String, Long> entry : counts.entrySet()) {
	    long count = entry.getValue();
	    ErrorsResult result = new ErrorsResult();
	    result.setErrorCount(count);
	    result.setErrorPercent((100.0d * count) / errorCount);
	    result.setSamplePercent((100.0d * count) / sampleCount);
	    errorValues.put(entry.getKey(), result);
	}

	super.stopProducing();

	// Reset state
	errorCount = 0L;
	sampleCount = 0L;
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
	int index = 0;
	for (Map.Entry<String, ErrorsResult> entry : errorValues.entrySet()) {
	    JsonObjectBuilder seriesBuilder = Json.createObjectBuilder();
	    ErrorsResult result = entry.getValue();
	    seriesBuilder
		    .add("Type of error", entry.getKey())
		    .add("Number of errors",
		            Long.toString(result.getErrorCount()))
		    .add("Error Rate",
		            String.format("%.2f%%", result.getErrorPercent()))
		    .add("Sample Rate",
		            String.format("%.2f%%", result.getSamplePercent()));
	    builder.add(Integer.toString(++index), seriesBuilder);
	}
	dataResult.put("values", JsonUtil.convertJsonToString(builder.build()));
	return dataResult;
    }
}
