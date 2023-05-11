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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Implementation of {@link BackendListenerClient} to write the response times
 * of every sample to InfluxDB. If more "raw" information is required in InfluxDB
 * then this class can be extended or another BackendListener
 * {@link InfluxdbBackendListenerClient} can be used to send aggregate information
 * to InfluxDB.
 *
 * @since 5.3
 */
@AutoService(BackendListenerClient.class)
public class InfluxDBRawBackendListenerClient implements BackendListenerClient {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBRawBackendListenerClient.class);

    private static final Object LOCK = new Object();

    private static final String TAG_OK = "ok";
    private static final String TAG_KO = "ko";
    private static final String DEFAULT_MEASUREMENT = "jmeter";

    private static final Map<String, String> DEFAULT_ARGS = new LinkedHashMap<>();

    static {
        DEFAULT_ARGS.put("influxdbMetricsSender", HttpMetricsSender.class.getName());
        DEFAULT_ARGS.put("influxdbUrl", "http://host_to_change:8086/write?db=jmeter");
        DEFAULT_ARGS.put("influxdbToken", "");
        DEFAULT_ARGS.put("measurement", DEFAULT_MEASUREMENT);
    }

    private InfluxdbMetricsSender influxDBMetricsManager;
    private String measurement;

    public InfluxDBRawBackendListenerClient() {
        // default constructor
    }

    /**
     * Used for testing.
     *
     * @param sender the {@link InfluxdbMetricsSender} to use
     */
    public InfluxDBRawBackendListenerClient(InfluxdbMetricsSender sender) {
        influxDBMetricsManager = sender;
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        initInfluxDBMetricsManager(context);
        measurement = context.getParameter("measurement", DEFAULT_MEASUREMENT);
    }

    private void initInfluxDBMetricsManager(BackendListenerContext context) throws Exception {
        influxDBMetricsManager = Class
                .forName(context.getParameter("influxdbMetricsSender"))
                .asSubclass(InfluxdbMetricsSender.class)
                .getDeclaredConstructor()
                .newInstance();

        influxDBMetricsManager.setup(
                context.getParameter("influxdbUrl"),
                context.getParameter("influxdbToken"));
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
        influxDBMetricsManager.destroy();
    }

    @Override
    public void handleSampleResults(
            List<SampleResult> sampleResults, BackendListenerContext context) {
        log.debug("Handling {} sample results", sampleResults.size());
        synchronized (LOCK) {
            for (SampleResult sampleResult : sampleResults) {
                addMetricFromSampleResult(sampleResult);
            }
            influxDBMetricsManager.writeAndSendMetrics();
        }
    }

    private void addMetricFromSampleResult(SampleResult sampleResult) {
        String tags = "," + createTags(sampleResult);
        String fields = createFields(sampleResult);
        long timestamp = sampleResult.getTimeStamp();

        influxDBMetricsManager.addMetric(measurement, tags, fields, timestamp);
    }

    private static String createTags(SampleResult sampleResult) {
        boolean isError = sampleResult.getErrorCount() != 0;
        String status = isError ? TAG_KO : TAG_OK;
        // remove surrounding quotes and spaces from sample label
        String label = StringUtils.strip(sampleResult.getSampleLabel(), "\" ");
        String transaction = AbstractInfluxdbMetricsSender.tagToStringValue(label);
        String threadName = StringUtils.deleteWhitespace(sampleResult.getThreadName());
        return "status=" + status
                + ",transaction=" + transaction
                + ",threadName=" + threadName;
    }

    private static String createFields(SampleResult sampleResult) {
        long duration = sampleResult.getTime();
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        return "duration=" + duration
                + ",ttfb=" + latency
                + ",connectTime=" + connectTime;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        DEFAULT_ARGS.forEach(arguments::addArgument);
        return arguments;
    }
}
