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

import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.Sample;

/**
 * <p>
 * The class GrapherConsumer provides a consumer that count succeeded and failed
 * samples.
 * </p>
 * 
 * @since 2.14
 */
public class RequestsSummaryConsumer extends AbstractSummaryConsumer {

    private long succeededCount;
    private long failedCount;

    /**
     * Gets the number of succeeded samples.
     *
     * @return the number of succeeded samples
     */
    public final long getSucceededCount() {
	return succeededCount;
    }

    /**
     * Gets the number of failed samples.
     *
     * @return the number of failed samples
     */
    public final long getFailedCount() {
	return failedCount;
    }

    public final double getSucceededPercent() {
	return (double) succeededCount * 100 / (succeededCount + failedCount);
    }

    public final double getFailedPercent() {
	return (double) failedCount * 100 / (succeededCount + failedCount);
    }

    @Override
    public void startConsuming() {
	succeededCount = 0L;
	failedCount = 0L;

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    @Override
    public void consume(Sample sample, int channel) {
	if (sample.getSuccess()) {
	    succeededCount++;
	} else {
	    failedCount++;
	}
	super.produce(sample, channel);
    }

    @Override
    public void stopConsuming() {
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
	DataContext result = new DataContext();
	result.put("KoPercent", getFailedPercent());
	result.put("OkPercent", getSucceededPercent());
	return result;
    }
}
