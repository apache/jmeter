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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Locale;
import java.util.Random;
import static java.lang.String.format;
import java.io.UnsupportedEncodingException;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * StatsD Graphite sender
 */
class StatsdMetricsSender extends AbstractGraphiteMetricsSender {
    private static final Logger LOG = LoggingManager.getLoggerForClass();        
        
    private String prefix;

    private List<MetricTuple> metrics = new ArrayList<MetricTuple>();

    private InetSocketAddress address;
    private DatagramChannel channel;
    private String graphiteHost;
    private int graphitePort;

    /**
     * @param graphiteHost Graphite Host
     * @param graphitePort Graphite Port
     * @param prefix Common Metrics prefix
     */
    StatsdMetricsSender() {
        super();
    }
    
    @Override
    public void setup(String graphiteHost, int graphitePort, String prefix) {
        this.prefix = prefix;
        this.graphiteHost = graphiteHost;
        this.graphitePort = graphitePort;
        try {
        	this.address = new InetSocketAddress(InetAddress.getByName(this.graphiteHost), this.graphitePort);
        	channel = DatagramChannel.open();
        } catch (Exception e) {
        	LOG.error("Error initializing statsd socket " + e);
        }

        if(LOG.isInfoEnabled()) {
            LOG.info("Created StatsdMetricsSender with host:"+graphiteHost+", port:"+graphitePort+", prefix:"+prefix);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#addMetric(long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void addMetric(long timestamp, String contextName, String metricName, String metricValue) {
        StringBuilder sb = new StringBuilder(50);
        sb
            .append(prefix)
            .append(contextName)
            .append(".")
            .append(metricName);
        metrics.add(new MetricTuple(sb.toString(), timestamp, metricValue));
    }

    private void send(final String stat) throws UnsupportedEncodingException
    {
        
        final ByteBuffer bb = ByteBuffer.wrap(stat.getBytes(CHARSET_NAME));
        try
        {
            channel.send(bb, address);
        }
        catch (final IOException e)
        {
            LOG.error(format("Error sending stat '%s' to %s", stat, address), e);
        }
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#writeAndSendMetrics()
     */
    @Override
    public void writeAndSendMetrics() {        
        if (metrics.size()>0) {
            SocketOutputStream out = null;
            try {           	
                for (MetricTuple metric: metrics) {
                    send(format(Locale.ENGLISH, "%s:%s|g", metric.name, metric.value));
                }
                // if there was an error, we might miss some data. for now, drop those on the floor and
                // try to keep going.
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Wrote "+ metrics.size() +" metrics");
                }
            } catch (Exception e) {
                LOG.error("Error writing to Graphite: Host "+this.graphiteHost+" Port "+this.graphitePort+" Error "+ e.getMessage());
            }
            // We drop metrics in all cases
            metrics.clear();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#destroy()
     */
    @Override
    public void destroy() {
    	try {
    		channel.close();
    	} catch (IOException e) {
    		LOG.error("Error closing datagram socket: " + e);
    	}
    }

}
