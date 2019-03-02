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

import org.apache.jmeter.samplers.SampleResult;

/**
 * Implement this method to be a Visualizer for JMeter. This interface defines a
 * single method, "add()", that provides the means by which
 * {@link org.apache.jmeter.samplers.SampleResult SampleResults} are passed to
 * the implementing visualizer for display/logging. The easiest way to create
 * the visualizer is to extend the
 * {@link org.apache.jmeter.visualizers.gui.AbstractVisualizer} class.
 *
 */
public interface Visualizer {
    /**
     * This method is called by sampling thread to inform the visualizer about
     * the arrival of a new sample.
     *
     * @param sample
     *            the newly arrived sample
     */
    void add(SampleResult sample);

    /**
     * This method is used to indicate a visualizer generates statistics.
     *
     * @return true if visualiser generates statistics
     */
    boolean isStats();
}
