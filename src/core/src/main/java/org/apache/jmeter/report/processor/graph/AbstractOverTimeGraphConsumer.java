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
package org.apache.jmeter.report.processor.graph;

import java.util.Map;

import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ValueResultData;

/**
 * The class AbstractOverTimeGraphConsumer provides a base class for over time
 * graphs.
 *
 * @since 3.0
 */
public abstract class AbstractOverTimeGraphConsumer extends
        AbstractGraphConsumer {

    public static final String RESULT_CTX_GRANULARITY = "granularity";

    private long granularity;

    /**
     * Gets the granularity.
     *
     * @return the granularity
     */
    public long getGranularity() {
        return granularity;
    }

    /**
     * Sets the granularity.
     *
     * @param granularity
     *            the granularity to set
     */
    public void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    /**
     * Instantiates a new abstract over time graph consumer.
     */
    protected AbstractOverTimeGraphConsumer() {
    }

    /**
     * Creates the time stamp keys selector.
     *
     * @return the time stamp keys selector
     */
    protected abstract TimeStampKeysSelector createTimeStampKeysSelector();

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createKeysSelector()
     */
    @Override
    protected final GraphKeysSelector createKeysSelector() {
        TimeStampKeysSelector keysSelector = createTimeStampKeysSelector();
        keysSelector.setGranularity(granularity);
        return keysSelector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected abstract Map<String, GroupInfo> createGroupInfos();

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * initializeExtraResults(org.apache.jmeter.report.processor.MapResultData)
     */
    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        parentResult.setResult(RESULT_CTX_GRANULARITY, new ValueResultData(
                Long.valueOf(granularity)));
    }

    @Override
    public void initialize() {
        super.initialize();
        ((TimeStampKeysSelector) getKeysSelector()).setGranularity(granularity);
    }
}
