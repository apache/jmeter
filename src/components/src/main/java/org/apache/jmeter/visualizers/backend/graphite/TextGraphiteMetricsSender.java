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

package org.apache.jmeter.visualizers.backend.graphite;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlainText Graphite sender.
 *
 * @since 2.13
 */
class TextGraphiteMetricsSender extends AbstractGraphiteMetricsSender {
    private static final Logger log = LoggerFactory.getLogger(TextGraphiteMetricsSender.class);

    private final Object lock = new Object();

    private List<MetricTuple> metrics = new ArrayList<>();

    private SocketConnectionInfos socketConnectionInfos;
    private GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> socketOutputStreamPool;
    private String prefix;

    TextGraphiteMetricsSender() {
        super();
    }

    /**
     * @param graphiteHost Graphite Host
     * @param graphitePort Graphite Port
     * @param prefix       Common Metrics prefix
     */
    @Override
    public void setup(String graphiteHost, int graphitePort, String prefix) {
        this.socketConnectionInfos = new SocketConnectionInfos(graphiteHost, graphitePort);
        this.socketOutputStreamPool = createSocketOutputStreamPool();
        this.prefix = prefix;

        log.info("Created TextGraphiteMetricsSender with host: {}, port: {}, prefix: {}",
                graphiteHost, graphitePort, prefix);
    }

    /** Setup used for testing, or if explicit customisation is required. */
    public void setup(SocketConnectionInfos socketConnectionInfos,
                      GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> socketOutputStreamPool,
                      String prefix) {
        this.socketConnectionInfos = socketConnectionInfos;
        this.socketOutputStreamPool = socketOutputStreamPool;
        this.prefix = prefix;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#addMetric(long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void addMetric(long timestamp, String contextName, String metricName, String metricValue) {
        String name = new StringBuilder(50)
                .append(prefix)
                .append(contextName)
                .append(".")
                .append(metricName)
                .toString();
        synchronized (lock) {
            metrics.add(new MetricTuple(name, timestamp, metricValue));
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#writeAndSendMetrics()
     */
    @Override
    public void writeAndSendMetrics() {
        final List<MetricTuple> currentMetrics;
        synchronized (lock) {
            if (metrics.isEmpty()) {
                return;
            }
            // keep the current metrics to send outside sync block
            currentMetrics = metrics;
            metrics = new ArrayList<>(currentMetrics.size());
        }
        writeMetrics(currentMetrics);
    }

    private void writeMetrics(List<MetricTuple> currentMetrics) {
        SocketOutputStream out = null;
        try {
            out = socketOutputStreamPool.borrowObject(socketConnectionInfos);
            // pw is not closed as it would close the underlying pooled out
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, CHARSET_NAME), false);
            for (MetricTuple metric : currentMetrics) {
                pw.printf("%s %s %d%n", metric.name, metric.value, metric.timestamp);
            }
            pw.flush();
            if (log.isDebugEnabled()) {
                log.debug("Wrote {} metrics", currentMetrics.size());
            }
            if (pw.checkError()) {
                socketOutputStreamPool.invalidateObject(socketConnectionInfos, out);
                log.error("IO Errors writing to Graphite, some data will be lost");
            } else {
                socketOutputStreamPool.returnObject(socketConnectionInfos, out);
            }
        } catch (Exception e) {
            // if there was an error, we might miss some data. for now, drop those try to keep going.
            if (out != null) {
                try {
                    socketOutputStreamPool.invalidateObject(socketConnectionInfos, out);
                } catch (Exception e1) {
                    log.warn("Exception invalidating socketOutputStream connected to graphite server {}:{}",
                            socketConnectionInfos.getHost(), socketConnectionInfos.getPort(), e1);
                }
            }
            log.error("Error writing to Graphite: {}", e.getMessage(), e);
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
