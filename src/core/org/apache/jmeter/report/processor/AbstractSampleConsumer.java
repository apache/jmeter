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

import org.apache.jmeter.report.core.ArgumentNullException;
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
 * @since 2.14
 */
abstract public class AbstractSampleConsumer extends AbstractSampleProcessor
        implements SampleConsumer, SampleProducer {

    private static final Logger log = LoggerFactory
	    .getLogger(AbstractSampleConsumer.class);

    /** sample consumer name, used for logging */
    private String name;

    /** number of samples produced by this consumer, all channels included */
    private long producedSampleCount;

    private File workingDir;

    /**
     * samples consumers that will consume sample produced by this sample
     * consumer (which is also a sample producer)
     */
    private List<SampleConsumer> sampleConsumers = new ArrayList<SampleConsumer>();

    /**
     * index of sample metadata consumed by this consumer. Indexed by channel
     * numbers
     */
    private Map<Integer, SampleMetadata> consumedMetadata = new TreeMap<Integer, SampleMetadata>();

    /**
     * Store a data in the current sample context with a prefixed key.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    protected final void setLocalData(String key, Object value) {
	getSampleContext().setData(getAbsoluteKey(this, key), value);
    }

    /**
     * Gets the data stored in the current sample context prefixing the
     * specified key .
     *
     * @param key
     *            the key
     * @return the local data
     */
    protected final Object getLocalData(String key) {
	return getSampleContext().getData(getAbsoluteKey(this, key));
    }

    /**
     * Define an absolute key for the data store using the specified consumer
     * name and a relative key.
     *
     * @param consumer
     *            the consumer
     * @param relativeKey
     *            the relative key
     * @return the absolute key
     */
    public static final String getAbsoluteKey(AbstractSampleConsumer consumer,
	    String relativeKey) {
	if (consumer == null)
	    throw new ArgumentNullException("consumer");

	return consumer.getName() + relativeKey;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	if (name == null) {
	    return getClass().getSimpleName() + "-" + hashCode();
	} else {
	    return name;
	}
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

    public void setSampleConsumers(List<SampleConsumer> consumers) {
	if (consumers == null)
	    throw new ArgumentNullException("consumers");

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

    public void setConsumedMetadata(SampleMetadata sampleMetadata, int channel) {
	consumedMetadata.put(channel, sampleMetadata);
    }

    public SampleMetadata getConsumedMetadata(int channel) {
	return consumedMetadata.get(channel);
    }

    public int getConsumedChannelCount() {
	return consumedMetadata.size();
    }

    private void initConsumers(SampleContext context) {
	for (SampleConsumer consumer : this.sampleConsumers) {
	    try {
		consumer.setSampleContext(context);
	    } catch (Exception e) {
		log.error(
		        "produce(): Consumer failed with message :"
		                + e.getMessage(), e);
		throw new SampleException(e);
	    }
	}
    }

    @Override
    public void setChannelAttribute(int channel, String key, Object value) {
	super.setChannelAttribute(channel, key, value);
	// propagate attribute to all of this SampleConsumer consumers
	for (SampleConsumer c : sampleConsumers) {
	    c.setChannelAttribute(channel, key, value);
	}
    }

    public void setProducedMetadata(SampleMetadata metadata, int channel) {
	for (SampleConsumer consumer : this.sampleConsumers) {
	    try {
		consumer.setConsumedMetadata(metadata, channel);
	    } catch (Exception e) {
		log.error(
		        "setProducedMetadata(): Consumer failed with message :"
		                + e.getMessage(), e);
		throw new SampleException(e);
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

    public void startProducing() {
	producedSampleCount = 0;
	for (SampleConsumer consumer : this.sampleConsumers) {
	    try {
		consumer.startConsuming();
	    } catch (Exception e) {
		log.error("startProducing(): Consumer failed with message :"
		        + e.getMessage(), e);
		throw new SampleException(e);
	    }
	}
    }

    public void produce(Sample s, int channel) {
	for (SampleConsumer consumer : this.sampleConsumers) {
	    try {
		consumer.consume(s, channel);
		producedSampleCount++;
	    } catch (Exception e) {
		log.error(
		        "produce(): Consumer failed with message :"
		                + e.getMessage(), e);
		throw new SampleException(e);
	    }
	}
    }

    public void stopProducing() {
	for (SampleConsumer consumer : this.sampleConsumers) {
	    try {
		consumer.stopConsuming();
	    } catch (Exception e) {
		log.error(
		        "stopProducing(): Consumer failed with message :"
		                + e.getMessage(), e);
		throw new SampleException(e);
	    }
	}
	if (log.isInfoEnabled()) {
	    log.info("stopProducing(): " + getName() + " produced "
		    + producedSampleCount + " samples");
	}
    }

}
