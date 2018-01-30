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

import org.apache.jmeter.report.core.Sample;

/**
 * The classes that implement interface SampleIndexer have to calculate an index
 * from a sample.
 *
 * @param <TIndex>
 *            the type of the index
 * 
 * @since 3.0
 */
public interface SampleIndexer<TIndex> {

    /**
     * Calculate index from the specified sample.
     *
     * @param sample
     *            the sample
     * @return the index
     */
    TIndex calculateIndex(Sample sample);

    /**
     * Reset the index calculation logic.
     */
    void reset();
}
