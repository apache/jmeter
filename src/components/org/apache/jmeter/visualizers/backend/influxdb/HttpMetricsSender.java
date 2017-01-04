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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Influxdb sender base on The Line Protocol. 
 * The Line Protocol is a text based format for writing points to InfluxDB. 
 * Syntax : <measurement>[,<tag_key>=<tag_value>[,<tag_key>=<tag_value>]] <field_key>=<field_value>[,<field_key>=<field_value>] [<timestamp>]
 * Each line, separated by the newline character, represents a single point in InfluxDB.
 * Line Protocol is whitespace sensitive.
 */
class HttpMetricsSender extends AbstractInfluxdbMetricsSender {
	private static final Logger LOG = LoggingManager.getLoggerForClass();

	private List<MetricTuple> metrics = new ArrayList<>();

	private HttpPost httpRequest = null;

	private CloseableHttpClient httpClient = null;

	private HttpResponse response = null;

	private URL url;

	HttpMetricsSender() {
		super();
	}

    /**
	 * The HTTP API is the primary means of writing data into InfluxDB, by sending POST requests to the /write endpoint.
	 * Initiate the HttpClient client with a HttpPost request from influxdb url
	 * @param influxdbUrl example : http://localhost:8086/write?db=myd&rp=one_week
	 * @see org.apache.jmeter.visualizers.backend.influxdb.InfluxdbMetricsSender#setup(java.lang.String)
	 */
	@Override
	public void setup(String influxdbUrl) {

		try {
		    httpClient = HttpClients.createDefault();
		    url = new URL(influxdbUrl);
			httpRequest = new HttpPost(url.toString());
			httpRequest.setHeader("User-Agent", "JMeter/1.0");
			
			if (LOG.isInfoEnabled()) {
				LOG.info("Created InfluxDBMetricsSender with url:" + influxdbUrl);
			}

		} catch (MalformedURLException e) {
			LOG.error("Malformed URL Exception : " + influxdbUrl );
		}

		
	}

	@Override
	public void addMetric(String mesurment, String tag, String field) {

		metrics.add(new MetricTuple(mesurment, tag, field));

	}

    /**
	 * @see
	 * org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
	 * writeAndSendMetrics()
	 */
	@Override
    public void writeAndSendMetrics() {  
	
        if (metrics.size()>0 && httpRequest != null) {

            try {
                StringBuffer sb = new StringBuffer(); 
                for (MetricTuple metric: metrics) {
                	// We let the Influxdb server fill the timestamp so we don't add epoch time on each point
                	sb.append(metric.measurment + metric.tag + " "+  metric.field + "\n");
                }

                StringEntity entity = new StringEntity(sb.toString(), StandardCharsets.UTF_8);
                
                httpRequest.setEntity(entity);
                response = httpClient.execute(httpRequest);
    
                if(LOG.isDebugEnabled()) {
                    int code = response.getStatusLine().getStatusCode();
                    /* HTTP response summary
                     * 2xx: If your write request received HTTP 204 No Content, it was a success!
                     * 4xx: InfluxDB could not understand the request.
                     * 5xx: The system is overloaded or significantly impaired.
                     */
                    switch (code) {
                    	case 204 :
                    		LOG.debug("Success, number of metric wrote : "+ metrics.size());
                    		break;
	                	default :
	                		LOG.debug("Error : " + code  );
                    }
	                		 
                }
                EntityUtils.consumeQuietly(response.getEntity());
                
            } catch (Exception e) {
                LOG.error("Error writing to InfluxDB : " + e.getLocalizedMessage() );
            }
        }
        
        // We drop metrics in all cases
        metrics.clear();
    }

	/**
	 * @see
	 * org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
	 * destroy()
	 */
	@Override
	public void destroy() {
		try {
			httpClient.close();
		} catch (IOException e) {
			LOG.error("Error during close httpClient : " + e.getLocalizedMessage());

		}
	}

}
