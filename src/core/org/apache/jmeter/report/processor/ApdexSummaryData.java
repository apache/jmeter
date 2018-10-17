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
 * The class ApdexSummaryData provides information for ApdexSummaryConsumer.
 *
 * @since 3.0
 */
public class ApdexSummaryData {

    private final ApdexThresholdsInfo apdexThresholdInfo;

    private long satisfiedCount;
    private long toleratedCount;
    private long totalCount;

    public final ApdexThresholdsInfo getApdexThresholdInfo() {
        return apdexThresholdInfo;
    }

    public final long getSatisfiedCount() {
        return satisfiedCount;
    }

    public final void setSatisfiedCount(long satisfiedCount) {
        this.satisfiedCount = satisfiedCount;
    }

    public final long getToleratedCount() {
        return toleratedCount;
    }

    public final void setToleratedCount(long toleratedCount) {
        this.toleratedCount = toleratedCount;
    }

    public final long getTotalCount() {
        return totalCount;
    }

    public final void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Instantiates a new apdex summary data.
     *
     * @param info the threshold information
     */
    public ApdexSummaryData(ApdexThresholdsInfo info) {
        apdexThresholdInfo = info;
    }

    public void incSatisfiedCount() {
        satisfiedCount++;
    }

    public void incToleratedCount() {
        toleratedCount++;
    }

    public void incTotalCount() {
        totalCount++;
    }
}
