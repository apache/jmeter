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
package org.apache.jmeter.report.processor.graph.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.SumAggregatorFactory;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.NameSeriesSelector;

/**
 * The class ResponseTimeDistributionGraphConsumer provides a graph to visualize
 * the distribution of the average response time per sample
 *
 * @since 3.0
 */
public class ResponseTimeDistributionGraphConsumer extends
        AbstractGraphConsumer {

    private long granularity = 1L;

    /**
     * Gets the granularity.
     *
     * @return the granularity
     */
    public final long getGranularity() {
        return granularity;
    }

    /**
     * @param granularity
     *            the granularity to set
     */
    public final void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createKeysSelector()
     */
    @Override
    protected final GraphKeysSelector createKeysSelector() {
        return new GraphKeysSelector() {

            @Override
            public Double select(Sample sample) {
                long elapsed = sample.getElapsedTime();
                return Double.valueOf((double) elapsed - elapsed % granularity);
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.csv.processor.impl.AbstractGraphConsumer#
     * createGroupInfos()
     */
    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        HashMap<String, GroupInfo> groupInfos = new HashMap<>(1);

        groupInfos.put(AbstractGraphConsumer.DEFAULT_GROUP, new GroupInfo(
                new SumAggregatorFactory(), new NameSeriesSelector(),
                // We include Transaction Controller results
                new CountValueSelector(false), false, false));

        return groupInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * initializeExtraResults(org.apache.jmeter.report.processor.MapResultData)
     */
    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        parentResult.setResult(
                AbstractOverTimeGraphConsumer.RESULT_CTX_GRANULARITY,
                new ValueResultData(Long.valueOf(granularity)));
    }
}
