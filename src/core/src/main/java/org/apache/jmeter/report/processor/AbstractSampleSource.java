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

    @Override
    public abstract void run();

    @Override
    public SampleContext getSampleContext() {
        return sampleContext;
    }

    @Override
    public void setSampleContext(SampleContext sampleContext) {
        this.sampleContext = sampleContext;
    }

    @Override
    public abstract void setSampleConsumers(List<SampleConsumer> consumers);

    @Override
    public abstract void addSampleConsumer(SampleConsumer consumer);

    @Override
    public abstract void removeSampleConsumer(SampleConsumer consumer);

}
