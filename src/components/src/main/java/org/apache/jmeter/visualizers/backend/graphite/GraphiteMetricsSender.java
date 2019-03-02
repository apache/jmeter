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

package org.apache.jmeter.visualizers.backend.graphite;

import java.nio.charset.StandardCharsets;

/**
 * @since 2.13
 */
interface GraphiteMetricsSender {
    int SOCKET_CONNECT_TIMEOUT_MS = 1000;
    int SOCKET_TIMEOUT = 1000;


    String CHARSET_NAME = StandardCharsets.UTF_8.name();

    final class MetricTuple {
        String name;
        long timestamp;
        String value;
        MetricTuple(String name, long timestamp, String value) {
            this.name = name;
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    /**
     * Convert the metric to a python tuple of the form:
     *      (timestamp, (prefix.contextName.metricName, metricValue))
     * And add it to the list of metrics.
     * @param timestamp in Seconds from 1970
     * @param contextName name of the context of this metric
     * @param metricName name of this metric
     * @param metricValue value of this metric
     */
    void addMetric(long timestamp, String contextName,
            String metricName, String metricValue);

    /**
     *
     * @param graphiteHost Host
     * @param graphitePort Port
     * @param prefix Root Data prefix
     */
    void setup(String graphiteHost, int graphitePort, String prefix);

    /**
     * Write metrics to Graphite using custom format
     */
    void writeAndSendMetrics();

    /**
     * Destroy sender
     */
    void destroy();

}
