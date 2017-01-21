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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

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
    private static final Logger LOG = LoggingManager.getLoggerForClass();

    private List<MetricTuple> metrics = new ArrayList<>();

    private HttpPost httpRequest;
    
    private CloseableHttpAsyncClient httpClient;

    private URL url;

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
        httpClient = HttpAsyncClients.createDefault();
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
                .setConnectTimeout(JMeterUtils.getPropDefault("backend_influxdb_connection_timeout", 1000))
                .setSocketTimeout(JMeterUtils.getPropDefault("backend_influxdb_socket_timeout", 3000))
                .setConnectionRequestTimeout(JMeterUtils.getPropDefault("backend_influxdb_connection_request_timeout", 100))
                .build();
        
        HttpPost httpRequest = new HttpPost(url.toURI());
        httpRequest.setConfig(defaultRequestConfig);
        httpRequest.setHeader("User-Agent", "JMeter/1.0");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created InfluxDBMetricsSender with url:" + url);
        }
        return httpRequest;
    }

    @Override
    public void addMetric(String mesurement, String tag, String field) {
        metrics.add(new MetricTuple(mesurement, tag, field));
    }

    /**
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
     *      writeAndSendMetrics()
     */
    @Override
    public void writeAndSendMetrics() {
        if (!metrics.isEmpty()) {
            try {
               
                if(httpRequest == null) {
                    httpRequest = createRequest(url);
                }
                StringBuilder sb = new StringBuilder(metrics.size()*20);
                String timestamp = System.currentTimeMillis()  + "000000";
                for (MetricTuple metric : metrics) {
                    // Add TimeStamp in nanosecond from epoch ( default in InfluxDB )
                    sb.append(metric.measurement + metric.tag + " " + metric.field + timestamp + "\n");
                }

                StringEntity entity = new StringEntity(sb.toString(), StandardCharsets.UTF_8);
                
                httpRequest.setEntity(entity);
                httpClient.execute(httpRequest, new FutureCallback<HttpResponse>() {

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
                            if(LOG.isDebugEnabled()) {
                                LOG.debug("Success, number of metrics written : " + metrics.size());
                            }
                            break;
                        default:
                            if(LOG.isDebugEnabled()) {
                                LOG.debug("Error writing metrics to influxDB Url: "+ url+", responseCode: " + code);
                            }
                        }
                    }

                    public void failed(final Exception ex) {
                        LOG.error("failed to connect to influxDB server : " + ex.getMessage());
                    }

                    public void cancelled() {

                    }

                });
               
            }catch (URISyntaxException ex ) {
                LOG.error(ex.getMessage());
            }
        }

        // We drop metrics in all cases
        metrics.clear();
    }

    /**
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
     *      destroy()
     */
    @Override
    public void destroy() {
        if(httpRequest != null) {
            httpRequest.abort();
        }
        IOUtils.closeQuietly(httpClient);
    }

}
