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
 * <p>
 * A factory for creating AbstractAggregator objects.
 * </p>
 * 
 * <p>
 * This abstract class creates the same kind of aggregator for each create
 * method.
 * </p>
 *
 * @since 3.0
 */
public abstract class AbstractAggregatorFactory implements AggregatorFactory {

    /**
     * Instantiates a new abstract aggregator factory.
     */
    protected AbstractAggregatorFactory() {
    }

    protected abstract Aggregator createAggregator();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregatorFactory#
     * createValueAggregator(double)
     */
    @Override
    public final Aggregator createValueAggregator() {
        return createAggregator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.core.GraphAggregatorFactory#createKeyAggregator
     * ()
     */
    @Override
    public final Aggregator createKeyAggregator() {
        return createAggregator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.core.GraphAggregatorFactory#
     * createAggregatedKeyValueAggregator()
     */
    @Override
    public final Aggregator createAggregatedKeyValueAggregator() {
        return createAggregator();
    }

}
