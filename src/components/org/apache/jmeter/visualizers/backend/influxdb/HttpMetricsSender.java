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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

    private CloseableHttpClient httpClient;

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
        httpClient = HttpClients.createDefault();
        url = new URL(influxdbUrl);
        httpRequest = createRequest(url);
    }

    /**
     * @param influxdbUrl
     * @return 
     * @throws URISyntaxException 
     */
    private HttpPost createRequest(URL url) throws URISyntaxException {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setSocketTimeout(3000)
                .setConnectionRequestTimeout(100)
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
                for (MetricTuple metric : metrics) {
                    // We let the Influxdb server fill the timestamp so we don't
                    // add epoch time on each point
                    sb.append(metric.measurement + metric.tag + " " + metric.field + "\n");
                }

                StringEntity entity = new StringEntity(sb.toString(), StandardCharsets.UTF_8);

                httpRequest.setEntity(entity);
                HttpResponse response = httpClient.execute(httpRequest);
                if (LOG.isDebugEnabled()) {
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
                EntityUtils.consumeQuietly(response.getEntity());

            } catch (Exception e) {
                // A Failure occured we abort request
                if(httpRequest != null) {
                    httpRequest.abort();
                    httpRequest = null;
                }
                LOG.error("Error writing to InfluxDB : " + e.getMessage());
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
