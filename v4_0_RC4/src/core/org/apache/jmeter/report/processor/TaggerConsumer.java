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

import java.util.ArrayList;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleBuilder;
import org.apache.jmeter.report.core.SampleMetadata;

/**
 * The Class TimeCountConsumer adds a data field to the samples it consumes.
 *
 * The new field is identified by the value of tagLabel in the sample metadata.
 *
 * @param <TIndex>
 *            the generic type
 * 
 * @since 3.0
 */
public class TaggerConsumer<TIndex> extends AbstractSampleConsumer {

    public static final String DEFAULT_TAG_LABEL = "Tag";

    // Collection of sample builders for channels
    private ArrayList<SampleBuilder> builders = new ArrayList<>();
    private SampleIndexer<TIndex> sampleIndexer;
    private String tagLabel = DEFAULT_TAG_LABEL;

    /**
     * Gets the label of the tag used by this consumer.
     *
     * @return the label of the tag used by this consumer.
     */
    public final String getTagLabel() {
        return tagLabel;
    }

    public final void setTagLabel(String tagLabel) {
        // TODO what if tagLabel is null or empty ?
        this.tagLabel = tagLabel;
    }

    public final SampleIndexer<TIndex> getSampleIndexer() {
        return sampleIndexer;
    }

    public final void setSampleIndexer(SampleIndexer<TIndex> sampleIndexer) {
        this.sampleIndexer = sampleIndexer;
    }

    // Adds a new field in the sample metadata for each channel
    private void initProducedMetadata() {
        builders.clear();
        int channelCount = getConsumedChannelCount();
        for (int i = 0; i < channelCount; i++) {
            // Get the metadata for the current channel
            SampleMetadata consumedMetadata = getConsumedMetadata(i);

            // Copy metadata to an array
            int colCount = consumedMetadata.getColumnCount();
            String[] names = new String[colCount + 1];
            for (int j = 0; j < colCount; j++) {
                names[j] = consumedMetadata.getColumnName(j);
            }

            // Add the new field
            names[colCount] = tagLabel;

            // Build the produced metadata from the array
            SampleMetadata producedMetadata = new SampleMetadata(
                    consumedMetadata.getSeparator(), names);

            // Add a sample builder for the current channel
            builders.add(new SampleBuilder(producedMetadata));
            super.setProducedMetadata(producedMetadata, i);
        }
    }

    private Sample createIndexedSample(Sample sample, int channel, TIndex index) {
        SampleBuilder builder = builders.get(channel);
        SampleMetadata metadata = builder.getMetadata();
        int colCount = metadata.getColumnCount();
        for (int i = 0; i < colCount - 1; i++) {
            builder.add(sample.getData(i));
        }
        builder.add(String.valueOf(index));
        return builder.build();
    }

    @Override
    public void startConsuming() {
        if (sampleIndexer != null) {
            sampleIndexer.reset();
        }
        initProducedMetadata();
        super.startProducing();
    }

    @Override
    public void consume(Sample sample, int channel) {
        if (sample != null && sampleIndexer != null) {
            TIndex index = sampleIndexer.calculateIndex(sample);
            Sample indexedSample = createIndexedSample(sample, channel, index);
            super.produce(indexedSample, channel);
        }
    }

    @Override
    public void stopConsuming() {
        super.stopProducing();
    }
}
