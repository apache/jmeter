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
import org.apache.jmeter.report.processor.SumAggregatorFactory;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.processor.graph.GraphKeysSelector;
import org.apache.jmeter.report.processor.graph.GroupInfo;
import org.apache.jmeter.report.processor.graph.NameSeriesSelector;

/**
 * The class ResponseTimePerSampleGraphConsumer provides a graph to visualize
 * ...
 *
 * @since 2.14
 */
public class ResponseTimePercentilesGraphConsumer extends AbstractGraphConsumer {

    /**
     * Instantiates a new response time percentiles graph consumer.
     */
    public ResponseTimePercentilesGraphConsumer() {
	setRenderPercentiles(true);
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
		return (double) sample.getElapsedTime();
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
	HashMap<String, GroupInfo> groupInfos = new HashMap<String, GroupInfo>(
	        1);

	groupInfos.put(AbstractGraphConsumer.DEFAULT_GROUP, new GroupInfo(
	        new SumAggregatorFactory(), new NameSeriesSelector(),
	        new CountValueSelector(), false, false));

	return groupInfos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * getSeriesExtraAttibutes ()
     */
    @Override
    protected Map<String, String> getSeriesExtraAttibutes() {
	HashMap<String, String> extraAttributes = new HashMap<String, String>();
	extraAttributes.put("curvedLines", "{\"apply\": true, \"tension\": 1}");
	extraAttributes.put("threshold",
	        "{ \"below\": 0, \"color\": \"none\" }");
	return extraAttributes;
    }
}
