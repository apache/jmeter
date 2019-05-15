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

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlainText Graphite sender
 * @since 2.13
 */
class TextGraphiteMetricsSender extends AbstractGraphiteMetricsSender {
    private static final Logger log = LoggerFactory.getLogger(TextGraphiteMetricsSender.class);
        
    private String prefix;

    private final Object lock = new Object();

    private List<MetricTuple> metrics = new ArrayList<>();

    private GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> socketOutputStreamPool;

    private SocketConnectionInfos socketConnectionInfos;


    TextGraphiteMetricsSender() {
        super();
    }

    /**
     * @param graphiteHost Graphite Host
     * @param graphitePort Graphite Port
     * @param prefix Common Metrics prefix
     */
    @Override
    public void setup(String graphiteHost, int graphitePort, String prefix) {
        this.prefix = prefix;
        this.socketConnectionInfos = new SocketConnectionInfos(graphiteHost, graphitePort);
        this.socketOutputStreamPool = createSocketOutputStreamPool();

        log.info("Created TextGraphiteMetricsSender with host: {}, port: {}, prefix: {}", graphiteHost, graphitePort,
                prefix);
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
        synchronized (lock) {
            metrics.add(new MetricTuple(sb.toString(), timestamp, metricValue));            
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#writeAndSendMetrics()
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
            SocketOutputStream out = null;
            try {
                out = socketOutputStreamPool.borrowObject(socketConnectionInfos);
                // pw is not closed as it would close the underlying pooled out
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, CHARSET_NAME), false);
                for (MetricTuple metric: copyMetrics) {
                    pw.printf("%s %s %d%n", metric.name, metric.value, Long.valueOf(metric.timestamp));
                }
                pw.flush();
                // if there was an error, we might miss some data. for now, drop those on the floor and
                // try to keep going.
                if (log.isDebugEnabled()) {
                    log.debug("Wrote {} metrics", copyMetrics.size());
                }
                if(pw.checkError()) {
                    socketOutputStreamPool.invalidateObject(socketConnectionInfos, out);
                    log.error("IO Errors writing to Graphite, some data will be lost");
                } else {
                    socketOutputStreamPool.returnObject(socketConnectionInfos, out);
                }
            } catch (Exception e) {
                if(out != null) {
                    try {
                        socketOutputStreamPool.invalidateObject(socketConnectionInfos, out);
                    } catch (Exception e1) {
                        log.warn("Exception invalidating socketOutputStream connected to graphite server '{}':{}",
                                socketConnectionInfos.getHost(), socketConnectionInfos.getPort(), e1);
                    }
                }
                log.error("Error writing to Graphite: {}", e.getMessage());
            }
            // We drop metrics in all cases
            copyMetrics.clear();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#destroy()
     */
    @Override
    public void destroy() {
        socketOutputStreamPool.close();
    }

}
