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

package org.apache.jmeter.visualizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Provides storage of samples in addition to calculations
 */
public class CachingStatCalculator extends SamplingStatCalculator {

    private final List<Sample> storedValues = Collections.synchronizedList(new ArrayList<Sample>());

    public CachingStatCalculator(String string) {
        super(string);
    }

    public List<Sample> getSamples() {
        return storedValues;
    }

    public Sample getSample(int index) {
        synchronized( storedValues ){
            if (index < storedValues.size()) {
                return storedValues.get(index);
            }
        }
        return null;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        storedValues.clear();
    }
    /**
     * Records a sample.
     *
     */
    @Override
    public Sample addSample(SampleResult res) {
        final Sample sample = super.addSample(res);
        storedValues.add(sample);
        return sample;
    }
}
