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

package org.apache.jmeter.visualizers.backend.influxdb;

/**
 * InfluxDB Sender interface 
 * @since 3.2
 *
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
     * @param measurement name of the influxdb measurement
     * @param tag tag set for influxdb
     * @param field field set for influxdb
     */
    public void addMetric(String measurement, String tag, String field);

    /**
     * Write metrics to Influxdb with HTTP API with InfluxDB's Line Protocol
     */
    public void writeAndSendMetrics();

    /**
     * Setup sender using influxDBUrl
     * @param influxDBUrl url pointing to influxdb
     * @throws Exception when setup fails
     */
    public void setup(String influxDBUrl) throws Exception; // NOSONAR

    /**
     * Destroy sender
     */
    public void destroy();

}
