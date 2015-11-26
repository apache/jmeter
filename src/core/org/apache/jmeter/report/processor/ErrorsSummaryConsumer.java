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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;

/**
 * <p>
 * The class ErrorSummaryConsumer provides a consumer that calculates error
 * statistics.
 * </p>
 * 
 * @since 2.14
 */
public class ErrorsSummaryConsumer extends AbstractSummaryConsumer {

    private static final String ASSERTION_FAILED = "Assertion failed"; //$NON-NLS-1$
    private Map<String, Long> counts = new HashMap<String, Long>();
    private long errorCount = 0L;
    private long sampleCount = 0L;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.SampleConsumer#startConsuming()
     */
    @Override
    public void startConsuming() {
	// Reset maps
	counts.clear();

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.SampleConsumer#consume(org.apache.
     * jmeter.report.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {

	// Increment sample count
	sampleCount++;

	// Process only failed samples
	if (!sample.getSuccess()) {
	    errorCount++;

	    // Each result is defined by code of samples so get the code of the
	    // sample
	    String code = sample.getResponseCode();

	    if (JMeterUtils
		    .getPropDefault(
		            SampleSaveConfiguration.ASSERTION_RESULTS_FAILURE_MESSAGE_PROP,
		            false)
		    && isSuccessCode(code)) {
		code = ASSERTION_FAILED;
		if (!StringUtils.isEmpty(sample.getFailureMessage())) {
		    code = StringEscapeUtils.escapeJson(sample
			    .getFailureMessage());
		}
	    }

	    // Increment error count by code
	    Long count = counts.get(code);
	    if (count != null) {
		counts.put(code, count + 1);
	    } else {
		counts.put(code, Long.valueOf(1));
	    }
	}
	super.produce(sample, channel);
    }

    /**
     * Determine if the HTTP status code is successful or not i.e. in range 200
     * to 399 inclusive
     *
     * @param code
     *            status code to check
     * @return whether in range 200-399 or not FIXME Duplicates
     *         HTTPSamplerBase#isSuccessCode but it's in http protocol
     */
    protected boolean isSuccessCode(String codeAsString) {
        if(StringUtils.isNumeric(codeAsString)) {
            try {
                int code = Integer.parseInt(codeAsString);
                return (code >= 200 && code <= 399);
            } catch (NumberFormatException ex){
                return false;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.SampleConsumer#stopConsuming()
     */
    @Override
    public void stopConsuming() {
	storeResult(counts.keySet());
	super.stopProducing();

	// Reset state
	errorCount = 0L;
	sampleCount = 0L;
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
	        .getResString("reportgenerator_summary_errors_type")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_errors_count")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_errors_rate_error")));
	titles.addResult(new ValueResultData(JMeterUtils
	        .getResString("reportgenerator_summary_errors_rate_all")));
	return titles;
    }

    private ListResultData createResultItem(String name, long count) {
	ListResultData result = new ListResultData();
	result.addResult(new ValueResultData(name));
	result.addResult(new ValueResultData(count));
	result.addResult(new ValueResultData((double) count * 100 / errorCount));
	result.addResult(new ValueResultData((double) count * 100 / sampleCount));
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
	long count;
	if ("".equals(name)) {
	    name = JMeterUtils.getResString("reportgenerator_summary_total");
	    count = errorCount;
	} else {
	    count = counts.get(name);
	}
	return createResultItem(name, count);
    }
}
