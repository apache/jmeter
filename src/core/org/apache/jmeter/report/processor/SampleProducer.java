package org.apache.jmeter.report.processor;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;

/**
 * Defines a sample producer<br>
 * <p>
 * A sample producer is able to produce sample on different channels.
 * </p>
 * <br>
 * <p>
 * Typically, a {@link SampleProducer} can be connected to
 * {@link SampleConsumer} where it will produced sample that will be consumed by
 * the sample consumer.
 * </p>
 * <br>
 * <p>
 * A sample producer can produce samples of different metadata for reach
 * channel/
 * </p>
 * <br>
 * <p>
 * The following production sequence must be observed:<br>
 * <li>Call <code>setProducedMetadata()</code> for each produced channel</li><br>
 * <li>Call <code>startProducing()</code></li><br>
 * <li>Call <code>produce()</code> for each sample to produce for every channel</li>
 * <li>Call <code>stopProducing()</code></li><br>
 * </p>
 * <br>
 * 
 * @since 2.14
 */
public interface SampleProducer extends SampleProcessor {

    /**
     * Set the metadata associated woth the specified channel
     * 
     * @param metadata
     *            The metadata to be associated to the specified channel
     * @param channel
     *            The channel whoses metadata are beeing associated wih
     */
    public void setProducedMetadata(SampleMetadata metadata, int channel);

    /**
     * Start producing samples, must be invoked before any call to
     * <code>produce()</code>
     */
    public void startProducing();

    /**
     * Produce a single sample on the specified channel
     * 
     * @param s
     *            The sample produced
     * @param channel
     *            The channel on which is produced the sample
     */
    public void produce(Sample s, int channel);

    /**
     * Stop producing samples, no <code>produce()</code> call should occur after
     * this service has been called.
     */
    public void stopProducing();

}
