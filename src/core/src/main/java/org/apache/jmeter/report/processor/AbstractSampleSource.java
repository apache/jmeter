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

import java.util.List;

/**
 * @author Ubik
 *
 */
public abstract class AbstractSampleSource implements SampleSource {

    private SampleContext sampleContext;

    /**
     * Instantiates a new abstract sample source.
     */
    protected AbstractSampleSource() {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public abstract void run();

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.SampleSource#getSampleContext()
     */
    @Override
    public SampleContext getSampleContext() {
        return sampleContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.SampleSource#setSampleContext(org.
     * apache.jmeter.report.processor.SampleContext)
     */
    @Override
    public void setSampleContext(SampleContext sampleContext) {
        this.sampleContext = sampleContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.SampleSource#addSampleConsumers(java
     * .util.List)
     */
    @Override
    public abstract void setSampleConsumers(List<SampleConsumer> consumers);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.SampleSource#addSampleConsumer(org
     * .apache.jmeter.report.processor.SampleConsumer)
     */
    @Override
    public abstract void addSampleConsumer(SampleConsumer consumer);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.processor.SampleSource#removeSampleConsumer(
     * org.apache.jmeter.report.processor.SampleConsumer)
     */
    @Override
    public abstract void removeSampleConsumer(SampleConsumer consumer);

}
