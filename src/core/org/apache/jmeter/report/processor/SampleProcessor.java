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

/**
 * Defines a sample processor.
 * <p>
 * Basically a sample processor is meant to process samples. There is currently
 * 2 kinds of sample processors :</p>
 * <ul>
 * <li>SampleConsumer : sample consumers are sample processors meant to consume
 * samples</li> <li>SampleProducer : sample producers are sample processors
 * meant to produce samples</li> </ul>
 *
 * @since 3.0
 */
public interface SampleProcessor {

    /**
     * Gets the sample context.
     *
     * @return the sample context
     */
    SampleContext getSampleContext();

    /**
     * Set sample context that this consumer should rely on.
     *
     * @param ctx
     *            the new sample context
     */
    void setSampleContext(SampleContext ctx);

    /**
     * Associate an attribute to the specified channel for this sample processor<br>
     * If the attribute already exist, it is replaced.
     *
     * @param channel
     *            The channel number to associate the attribute on
     * @param key
     *            The attribute key
     * @param value
     *            The attribute value to be set
     */
    void setChannelAttribute(int channel, String key, Object value);

    /**
     * Return an attribute value associated on a channel on this sample
     * processor
     *
     * @param channel
     *            The channel on which the attribute is associated
     * @param key
     *            The attribute key to be retrieved
     * @return The attribute value or null if none is found for the specified
     *         key
     */
    Object getChannelAttribute(int channel, String key);

}
