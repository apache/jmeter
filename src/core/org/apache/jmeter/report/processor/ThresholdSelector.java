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
 * The interface ThresholdSelector represents a projection from the samplers
 * with the same name to APDEX threshold information.
 *
 * @since 2.14
 */
public interface ThresholdSelector {

    /**
     * Do a projection from the specified samplers name to APDEX threshold
     * information.
     *
     * @param sampleName
     *            the name of samples, or empty string for overall thresholds
     * @return the apdex thresholds information
     */
    ApdexThresholdsInfo select(String sampleName);
}
