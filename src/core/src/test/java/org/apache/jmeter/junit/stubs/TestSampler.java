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

package org.apache.jmeter.junit.stubs;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

public class TestSampler extends AbstractSampler {

    private static final long serialVersionUID = 240L;

    private long wait = 0;

    private long samples = 0; // number of samples taken

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
        samples++;
        return null;
    }

    public TestSampler(String name, long wait) {
        setName(name);
        this.wait = wait;
    }

    public TestSampler(String name) {
        setName(name);
    }

    public TestSampler() {
    }

    @Override
    public String toString() {
        return getName();
    }

    public long getSamples() {
        return samples;
    }
}
