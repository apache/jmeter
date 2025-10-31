/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.report.processor.graph.impl;

import java.util.Collections;
import java.util.Map;

import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.SumAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.NameSeriesSelector;

/**
 * The Class ResponseTimePercentilesGraphConsumer computes percentile
 * graph for each samples
 *
 * @since 3.0
 */
public class ResponseTimePercentilesGraphConsumer extends AbstractGraphConsumer {

    /**
     * Instantiates a new response time percentiles graph consumer.
     */
    public ResponseTimePercentilesGraphConsumer() {
    }

    @Override
    public void initialize() {
        super.initialize();
        setRenderPercentiles(true);
    }

    @Override
    protected final GraphKeysSelector createKeysSelector() {
        return sample -> (double) sample.getElapsedTime();
    }

    @Override
    protected Map<String, GroupInfo> createGroupInfos() {
        return Collections.singletonMap(
                AbstractGraphConsumer.DEFAULT_GROUP,
                new GroupInfo(
                        new SumAggregatorFactory(), new NameSeriesSelector(),
                        // We include Transaction Controller results
                        new CountValueSelector(false), false, false));
    }

    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        // do nothing
    }
}
