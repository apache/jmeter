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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.MeanAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;
import org.apache.jmeter.report.processor.graph.GraphValueSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.TimeStampKeysSelector;

/**
 * The class ActiveThreadsGraphConsumer provides a graph to visualize active threads 
 * per time period (defined by granularity)
 *
 * @since 3.0
 */
public class ActiveThreadsGraphConsumer extends AbstractOverTimeGraphConsumer {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.impl.AbstractOverTimeGraphConsumer
     * #createTimeStampKeysSelector()
     */
    @Override
    protected TimeStampKeysSelector createTimeStampKeysSelector() {
        TimeStampKeysSelector keysSelector = new TimeStampKeysSelector();
        keysSelector.setSelectBeginTime(false);
        return keysSelector;
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
                new MeanAggregatorFactory(), new AbstractSeriesSelector() {

                    @Override
                    public Iterable<String> select(Sample sample) {
                        if(!sample.isEmptyController()) {
                            String threadName = sample.getThreadName();
                            int index = threadName.lastIndexOf(" ");
                            if (index >= 0) {
                                threadName = threadName.substring(0, index);
                            }
                            return Arrays.asList(new String[] { threadName });
                        } else {
                            return Collections.<String>emptyList();
                        }
                    }
                }, new GraphValueSelector() {

                    @Override
                    public Double select(String series, Sample sample) {
                        if(!sample.isEmptyController()) {
                            return Double.valueOf(sample.getGroupThreads());
                        } else {
                            return null;
                        }
                    }
                }, false, false));
        return groupInfos;
    }

}
