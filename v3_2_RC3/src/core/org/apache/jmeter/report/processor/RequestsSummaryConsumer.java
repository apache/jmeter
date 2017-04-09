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

/**
 * <p>
 * The class GrapherConsumer provides a consumer that count succeeded and failed
 * samples.
 * </p>
 * 
 * @since 3.0
 */
public class RequestsSummaryConsumer extends AbstractSampleConsumer {

    private long count;
    private long errorCount;

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.processor.SampleConsumer#startConsuming()
     */
    @Override
    public void startConsuming() {
        count = 0L;
        errorCount = 0L;

        // Broadcast metadata to consumes for each channel
        int channelCount = getConsumedChannelCount();
        for (int i = 0; i < channelCount; i++) {
            super.setProducedMetadata(getConsumedMetadata(i), i);
        }

        super.startProducing();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.report.processor.SampleConsumer#consume(org.apache.jmeter.report.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {
        if(!sample.isController()) {
            count++;
            if (!sample.getSuccess()) {
                errorCount++;
            }
        }
        super.produce(sample, channel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.SampleConsumer#stopConsuming()
     */
    @Override
    public void stopConsuming() {
        MapResultData result = new MapResultData();
        result.setResult("KoPercent", new ValueResultData(Double.valueOf((double) errorCount
                * 100 / count)));
        result.setResult("OkPercent", new ValueResultData(
                Double.valueOf((double) (count - errorCount) * 100 / count)));
        setDataToContext(getName(), result);
        super.stopProducing();
    }
}
