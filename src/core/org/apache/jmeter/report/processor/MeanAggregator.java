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

import org.apache.commons.math3.stat.descriptive.moment.Mean;

/**
 * The class MeanAggregator is used to get mean from samples.
 * 
 * @since 3.0
 */
public class MeanAggregator implements Aggregator {

    private Mean mean = new Mean();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#getCount()
     */
    @Override
    public long getCount() {
        return mean.getN();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#getResult()
     */
    @Override
    public double getResult() {
        return mean.getResult();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#addValue(double)
     */
    @Override
    public void addValue(double value) {
        mean.increment(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregator#reset()
     */
    @Override
    public void reset() {
        mean.clear();
    }

}
