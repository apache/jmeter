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
 * A factory for creating PercentileAggregator objects.
 *
 * @since 3.0
 */
public class PercentileAggregatorFactory extends AbstractAggregatorFactory {

    private double percentileIndex;
    private Aggregator lastAggregator;

    /**
     * Gets the percentile index.
     *
     * @return the percentile index
     */
    public final double getPercentileIndex() {
        return percentileIndex;
    }

    /**
     * Sets the percentile index.
     *
     * @param percentileIndex
     *            the index of the percentile to set
     */
    public void setPercentileIndex(double percentileIndex) {
        this.percentileIndex = percentileIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.core.AbstractAggregatorFactory#createAggregator
     * ()
     */
    @Override
    protected Aggregator createAggregator() {
        Aggregator newAggregator = null;
        if(lastAggregator != null) {
            newAggregator = new PercentileAggregator((PercentileAggregator)lastAggregator);
        } else {
            newAggregator = new PercentileAggregator(percentileIndex);
        }
        lastAggregator = newAggregator;
        return newAggregator;
    }

}
