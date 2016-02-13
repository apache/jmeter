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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for sample consumer implementations.<br>
 * Every sample consumer should extends this class to support basic consumer
 * features.
 * 
 * @since 3.0
 */
public abstract class AbstractSampleConsumer extends AbstractSampleProcessor
        implements SampleConsumer, SampleProducer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSampleConsumer.class);

    /** sample consumer name, used for logging */
    private String name;

    /** number of samples produced by this consumer, all channels included */
    private long producedSampleCount;

    private File workingDir;

    /**
     * samples consumers that will consume sample produced by this sample
     * consumer (which is also a sample producer)
     */
    private List<SampleConsumer> sampleConsumers = new ArrayList<>();

    /**
     * index of sample metadata consumed by this consumer. Indexed by channel
     * numbers
     */
    private Map<Integer, SampleMetadata> consumedMetadata = new TreeMap<>();

    /**
     * Gets the data identified by the specified key from the current sample
     * context
     *
     * @param key
     *            the key
     * @return the data
     */
    protected final Object getDataFromContext(String key) {
        return getSampleContext().getData().get(key);
    }

    /**
     * Store data in the current sample context with the specified key
     * identifier.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    protected final void setDataToContext(String key, Object value) {
        getSampleContext().getData().put(key, value);
    }

    /**
     * Gets the name of the consumer.
     *
     * @return the name of the consumer
     */
    public String getName() {
        if (name == null) {
            return getClass().getSimpleName() + "-" + hashCode();
        } else {
            return name;
        }
    }

    /**
     * Sets the name of the consumer.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    public final File getWorkingDirectory() {
        return workingDir;
    }

    private void setWorkingDirectory(File baseDirectory) {
        this.workingDir = new File(baseDirectory, getName());
    }

    @Override
    public void setSampleContext(SampleContext sampleContext) {
        super.setSampleContext(sampleContext);
        initConsumers(sampleContext);
        setWorkingDirectory(sampleContext.getWorkingDirectory());
    }

    /**
     * Sets the consumers
     * 
     * @param consumers
     *            for the samples (must not be {@code null})
     */
    public void setSampleConsumers(List<SampleConsumer> consumers) {
        Validate.notNull(consumers, "consumers must not be null");

        this.sampleConsumers = consumers;
    }

    public void addSampleConsumer(SampleConsumer consumer) {
        if (consumer == null) {
            return;
        }
        this.sampleConsumers.add(consumer);
    }

    public void setSampleConsumer(SampleConsumer consumer) {
        addSampleConsumer(consumer);
    }

    public void removeSampleConsumer(SampleConsumer consumer) {
        if (consumer == null) {
            return;
        }
        this.sampleConsumers.remove(consumer);
    }

    @Override
    public void setConsumedMetadata(SampleMetadata sampleMetadata, int channel) {
        consumedMetadata.put(Integer.valueOf(channel), sampleMetadata);
    }

    public SampleMetadata getConsumedMetadata(int channel) {
        return consumedMetadata.get(Integer.valueOf(channel));
    }

    public int getConsumedChannelCount() {
        return consumedMetadata.size();
    }

    private void initConsumers(SampleContext context) {
        for (SampleConsumer consumer : this.sampleConsumers) {
            try {
                consumer.setSampleContext(context);
            } catch (Exception e) {
                throw new SampleException("Consumer failed with message :"
                        + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setChannelAttribute(int channel, String key, Object value) {
        super.setChannelAttribute(channel, key, value);
        // propagate attribute to all of this SampleConsumer consumers
        for (SampleConsumer consumer : sampleConsumers) {
            consumer.setChannelAttribute(channel, key, value);
        }
    }

    @Override
    public void setProducedMetadata(SampleMetadata metadata, int channel) {
        for (SampleConsumer consumer : this.sampleConsumers) {
            try {
                consumer.setConsumedMetadata(metadata, channel);
            } catch (Exception e) {
                throw new SampleException("Consumer failed with message :"
                        + e.getMessage(), e);
            }
        }
    }

    protected SampleConsumer getConsumer(int i) {
        if (i < sampleConsumers.size()) {
            return sampleConsumers.get(i);
        } else {
            return null;
        }
    }

    @Override
    public void startProducing() {
        producedSampleCount = 0;
        for (SampleConsumer consumer : this.sampleConsumers) {
            try {
                consumer.startConsuming();
            } catch (Exception e) {
                throw new SampleException("Consumer failed with message :"
                        + e.getMessage(), e);
            }
        }
    }

    @Override
    public void produce(Sample s, int channel) {
        for (SampleConsumer consumer : this.sampleConsumers) {
            try {
                consumer.consume(s, channel);
                producedSampleCount++;
            } catch (Exception e) {
                throw new SampleException("Consumer failed with message :"
                        + e.getMessage(), e);
            }
        }
    }

    @Override
    public void stopProducing() {
        for (SampleConsumer consumer : this.sampleConsumers) {
            try {
                consumer.stopConsuming();
            } catch (Exception e) {
                throw new SampleException("Consumer failed with message :"
                        + e.getMessage(), e);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(getClass()+"#stopProducing(): " + getName() + " produced "
                    + producedSampleCount + " samples");
        }
    }

}
