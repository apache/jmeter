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

import java.util.Arrays;

import org.apache.jmeter.report.core.Sample;

/**
 * The class StatusSeriesSelector provides a projection from a sample to a
 * string that defines its status (success or failure).
 *
 * @since 3.0
 */
public class StatusSeriesSelector extends AbstractSeriesSelector {

    private String failureLabel = "Failures";
    private String successLabel = "Successes";

    /**
     * Gets the failure label.
     *
     * @return the failureLabel
     */
    public final String getFailureLabel() {
        return failureLabel;
    }

    /**
     * Sets the failure label.
     *
     * @param failureLabel
     *            the failureLabel to set
     */
    public final void setFailureLabel(String failureLabel) {
        this.failureLabel = failureLabel;
    }

    /**
     * Gets the success label.
     *
     * @return the successLabel
     */
    public final String getSuccessLabel() {
        return successLabel;
    }

    /**
     * Sets the success label.
     *
     * @param successLabel
     *            the successLabel to set
     */
    public final void setSuccessLabel(String successLabel) {
        this.successLabel = successLabel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleSelector#select(org.apache
     * .jmeter.report.csv.core.Sample)
     */
    @Override
    public Iterable<String> select(Sample sample) {
        String label = sample.getSuccess() ? successLabel : failureLabel;
        return Arrays.asList(new String[] { label });
    }

}
