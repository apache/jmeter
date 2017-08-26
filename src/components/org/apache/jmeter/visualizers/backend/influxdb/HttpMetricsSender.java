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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Influxdb sender base on The Line Protocol. The Line Protocol is a text based
 * format for writing points to InfluxDB. Syntax : <measurement>[,<tag_key>=
 * <tag_value>[,<tag_key>=<tag_value>]] <field_key>=<field_value>[,<field_key>=
 * <field_value>] [<timestamp>] Each line, separated by the newline character,
 * represents a single point in InfluxDB. Line Protocol is whitespace sensitive.
 * 
 * @since 3.2
 */
class HttpMetricsSender extends AbstractInfluxdbMetricsSender {
    private static final Logger log = LoggerFactory.getLogger(HttpMetricsSender.class);

    private final Object lock = new Object();

    private List<MetricTuple> metrics = new ArrayList<>();

    private HttpPost httpRequest;

    private CloseableHttpAsyncClient httpClient;

    private URL url;

    private Future<HttpResponse> lastRequest;

    HttpMetricsSender() {
        super();
    }

    /**
     * The HTTP API is the primary means of writing data into InfluxDB, by
     * sending POST requests to the /write endpoint. Initiate the HttpClient
     * client with a HttpPost request from influxdb url
     * 
     * @param influxdbUrl
     *            example : http://localhost:8086/write?db=myd&rp=one_week
     * @see org.apache.jmeter.visualizers.backend.influxdb.InfluxdbMetricsSender#setup(java.lang.String)
     */
    @Override
    public void setup(String influxdbUrl) throws Exception {
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig
                .custom()
                .setIoThreadCount(1)
                .setConnectTimeout(JMeterUtils.getPropDefault("backend_influxdb.connection_timeout", 1000))
                .setSoTimeout(JMeterUtils.getPropDefault("backend_influxdb.socket_timeout", 3000))
                .build();
        // Create a custom I/O reactor
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

        // Create a connection manager with custom configuration.
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(
                ioReactor);
        
        httpClient = HttpAsyncClientBuilder.create()
                .setConnectionManager(connManager)
                .setMaxConnPerRoute(2)
                .setMaxConnTotal(2)
                .setUserAgent("ApacheJMeter"+JMeterUtils.getJMeterVersion())
                .disableCookieManagement()
                .disableConnectionState()
                .build();
        url = new URL(influxdbUrl);
        httpRequest = createRequest(url);
        httpClient.start();
    }

    /**
     * @param influxdbUrl
     * @return 
     * @throws URISyntaxException 
     */
    private HttpPost createRequest(URL url) throws URISyntaxException {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(JMeterUtils.getPropDefault("backend_influxdb.connection_timeout", 1000))
                .setSocketTimeout(JMeterUtils.getPropDefault("backend_influxdb.socket_timeout", 3000))
                .setConnectionRequestTimeout(JMeterUtils.getPropDefault("backend_influxdb.connection_request_timeout", 100))
                .build();
        
        HttpPost httpRequest = new HttpPost(url.toURI());
        httpRequest.setConfig(defaultRequestConfig);
        log.debug("Created InfluxDBMetricsSender with url: {}", url);
        return httpRequest;
    }

    @Override
    public void addMetric(String mesurement, String tag, String field) {
        synchronized (lock) {
            metrics.add(new MetricTuple(mesurement, tag, field, System.currentTimeMillis()));            
        }
    }

    /**
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
     *      writeAndSendMetrics()
     */
    @Override
    public void writeAndSendMetrics() {
        List<MetricTuple> tempMetrics;
        synchronized (lock) {
            if(metrics.isEmpty()) {
                return;
            }
            tempMetrics = metrics;
            metrics = new ArrayList<>(tempMetrics.size());            
        }
        final List<MetricTuple> copyMetrics = tempMetrics;
        if (!copyMetrics.isEmpty()) {
            try {
                if(httpRequest == null) {
                    httpRequest = createRequest(url);
                }
                StringBuilder sb = new StringBuilder(copyMetrics.size()*35);
                for (MetricTuple metric : copyMetrics) {
                    // Add TimeStamp in nanosecond from epoch ( default in InfluxDB )
                    sb.append(metric.measurement)
                        .append(metric.tag)
                        .append(" ") //$NON-NLS-1$
                        .append(metric.field)
                        .append(" ")
                        .append(metric.timestamp+"000000") 
                        .append("\n"); //$NON-NLS-1$
                }

                StringEntity entity = new StringEntity(sb.toString(), StandardCharsets.UTF_8);
                
                httpRequest.setEntity(entity);
                lastRequest = httpClient.execute(httpRequest, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(final HttpResponse response) {
                        int code = response.getStatusLine().getStatusCode();
                        /*
                         * HTTP response summary 2xx: If your write request received
                         * HTTP 204 No Content, it was a success! 4xx: InfluxDB
                         * could not understand the request. 5xx: The system is
                         * overloaded or significantly impaired.
                         */
                        switch (code) {
                        case 204:
                            if (log.isDebugEnabled()) {
                                log.debug("Success, number of metrics written: {}", copyMetrics.size());
                            }
                            break;
                        default:
                            log.debug("Error writing metrics to influxDB Url: {}, responseCode: {}", url, code);
                        }
                    }
                    @Override
                    public void failed(final Exception ex) {
                        log.error("failed to send data to influxDB server : {}", ex.getMessage());
                    }
                    @Override
                    public void cancelled() {
                        log.warn("Request to influxDB server was cancelled");
                    }
                });               
            }catch (URISyntaxException ex ) {
                log.error(ex.getMessage());
            } finally {
                // We drop metrics in all cases
                copyMetrics.clear();
            }
        }

    }

    /**
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
     *      destroy()
     */
    @Override
    public void destroy() {
        // Give some time to send last metrics before shutting down
        log.info("Destroying ");
        try {
            lastRequest.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error waiting for last request to be send to InfluxDB", e);
        }
        if(httpRequest != null) {
            httpRequest.abort();
        }
        IOUtils.closeQuietly(httpClient);
    }

}
