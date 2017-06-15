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
package org.apache.jmeter.report.core;

/**
 * Defines a comparator for {@link Sample} instances
 * 
 * @since 3.0
 */
public interface SampleComparator {

    /**
     * Compares to sample
     * <p>
     * Must return an long integer that define the relational order of the 2
     * compared samples :</p>
     * <ul>
     * <li>Negative long integer : s1 is lower than s2</li>
     * <li>Zero long integer : s1 is strictly equal to s2</li>
     * <li>Positive long integer : s1 is greater than s2</li>
     * </ul>
     * 
     * @param s1
     *            The first sample to be compared
     * @param s2
     *            The second sample to compared
     * @return A negative is <code>s1 &lt; s2</code>, <code>0 if s1 = s2</code>,
     *         a positive integer if <code>s1 &gt; s2</code>
     */
    long compare(Sample s1, Sample s2);

    /**
     * Initializes the comparator with the {@link SampleMetadata} of the samples
     * to be compared.<br>
     * <p>
     * This function is invoked before any call to the<code>compare</code>
     * service.
     * </p>
     * <p>
     * Not that this function is the place to get sample column indexes for
     * better performance
     * </p>
     * 
     * @param metadata
     *            The sample metadata of the sample to be compared by this
     *            instance
     */
    void initialize(SampleMetadata metadata);

}
