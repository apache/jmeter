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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.descriptive.rank.Percentile.EstimationType;
import org.apache.jmeter.util.JMeterUtils;

public class DescriptiveStatisticsFactory {
    private static final int SLIDING_WINDOW_SIZE = JMeterUtils.getPropDefault("backend_metrics_window", 100); //$NON-NLS-1$
    private static final EstimationType ESTIMATION_TYPE = EstimationType
            .valueOf(JMeterUtils.getPropDefault("backend_metrics_percentile_estimator", "LEGACY")); //$NON-NLS-1$

    private DescriptiveStatisticsFactory() {
        // utility class -> hide the constructor
    }

    public static DescriptiveStatistics createDescriptiveStatistics() {
        return createDescriptiveStatistics(SLIDING_WINDOW_SIZE);
    }

    public static DescriptiveStatistics createDescriptiveStatistics(int windowSize) {
        DescriptiveStatistics statistics = new DescriptiveStatistics(windowSize);
        statistics.setPercentileImpl(new Percentile().withEstimationType(ESTIMATION_TYPE));
        return statistics;
    }
}
