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
package org.apache.jmeter.report.processor.graph;

import org.apache.jmeter.report.core.Sample;

/**
 * The class ElapsedTimeValueSelector provides a projection from a sample to its
 * elapsed time only if sample is not an empty transaction Controller and sample is successful
 *
 * @since 3.1
 */
public class SuccessfulElapsedTimeValueSelector extends ElapsedTimeValueSelector {

    /**
     *
     */
    public SuccessfulElapsedTimeValueSelector() {
        super(true);
    }

    /**
     * @see GraphValueSelector#select(String, Sample)
     */
    @Override
    public Double select(String series, Sample sample) {
        if(!sample.isController() && sample.getSuccess()) {
            return Double.valueOf(sample.getElapsedTime());
        } else {
            return null;
        }
    }
}
