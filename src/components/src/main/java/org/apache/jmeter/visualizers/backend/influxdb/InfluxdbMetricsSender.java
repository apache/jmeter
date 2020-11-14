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

package org.apache.jmeter.visualizers.backend.influxdb;

/**
 * InfluxDB Sender interface
 *
 * @since 3.2
 */
interface InfluxdbMetricsSender {

    /**
     * One data point in InfluxDB is represented by a measurement name, a tag
     * set and a field set ( optionally a timestamp )
     */
    final class MetricTuple {
        String measurement;
        String tag;
        String field;
        long timestamp;
        MetricTuple(String measurement, String tag, String field, long timestamp) {
            this.measurement = measurement;
            this.tag = tag;
            this.field = field;
            this.timestamp = timestamp;
        }
    }

    /**
     * @param measurement name of the InfluxDB measurement
     * @param tag         tag set for InfluxDB (N.B. Needs to start with a comma)
     * @param field       field set for InfluxDB
     */
    public void addMetric(String measurement, String tag, String field);

    /**
     * @param measurement name of the InfluxDB measurement
     * @param tag         tag set for InfluxDB (N.B. Needs to start with a comma)
     * @param field       field set for InfluxDB
     * @param timestamp   timestamp for InfluxDB
     */
    public void addMetric(String measurement, String tag, String field, long timestamp);

    /**
     * Write metrics to InfluxDB with HTTP API with InfluxDB's Line Protocol
     */
    public void writeAndSendMetrics();

    /**
     * Setup sender using influxDBUrl
     *
     * @param influxDBUrl   url pointing to InfluxDB
     * @param influxDBToken authorization token to InfluxDB 2.0
     * @throws Exception when setup fails
     */
    public void setup(String influxDBUrl, String influxDBToken) throws Exception; // NOSONAR

    /**
     * Destroy sender
     */
    public void destroy();

}
