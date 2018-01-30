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

/**
 * Base for sample processor implementations<br>
 * Implements basic logic for setting sample context and handling channel
 * attributes. <br>
 * 
 * @since 3.0
 */
public class AbstractSampleProcessor implements SampleProcessor {

    private SampleContext sampleContext;

    private ArrayList<ChannelContext> channelContexts = new ArrayList<>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleProcessor#getSampleContext()
     */
    @Override
    public SampleContext getSampleContext() {
        return sampleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleProcessor#setSampleContext
     * (org.apache.jmeter.report.csv.processor.SampleContext)
     */
    @Override
    public void setSampleContext(SampleContext sampleContext) {
        this.sampleContext = sampleContext;
    }

    /**
     * Get the ChannelContext associated to the specified channel. If the
     * specified channel does not have a context associated to it then one will
     * be created and associated.
     * 
     * @param channel
     *            The channel number whose context is to be returned
     * @return The channel context associated to the specified channel.
     */
    private ChannelContext getChannelContext(int channel) {
        while (channelContexts.size() <= channel) {
            channelContexts.add(new ChannelContext());
        }
        return channelContexts.get(channel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleProcessor#setChannelAttribute
     * (int, java.lang.String, java.lang.Object)
     */
    @Override
    public void setChannelAttribute(int channel, String key, Object value) {
        getChannelContext(channel).put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleProcessor#getChannelAttribute
     * (int, java.lang.String)
     */
    @Override
    public Object getChannelAttribute(int channel, String key) {
        return getChannelContext(channel).get(key);
    }
}
