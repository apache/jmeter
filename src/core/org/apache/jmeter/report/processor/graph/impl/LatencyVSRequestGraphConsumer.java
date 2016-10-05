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
import org.apache.jmeter.report.processor.MedianAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractVersusRequestsGraphConsumer;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.LatencyValueSelector;
import org.apache.jmeter.report.processor.graph.StatusSeriesSelector;

/**
 * The class LatencyVSRequestGraphConsumer provides a graph to visualize
 * latency vs requests
 *
 * @since 3.0
 */
public class LatencyVSRequestGraphConsumer extends
        AbstractVersusRequestsGraphConsumer {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * createKeysSelector()
     */
    @Override
    protected GraphKeysSelector createKeysSelector() {
        return new GraphKeysSelector() {

            @Override
            public Double select(Sample sample) {
                return sample
                        .getData(Double.class, AbstractVersusRequestsGraphConsumer.TIME_INTERVAL_LABEL);
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
                new MedianAggregatorFactory(), new StatusSeriesSelector(),
                // We ignore Transaction Controller results
                new LatencyValueSelector(true), false, false));
        return groupInfos;
    }
}
