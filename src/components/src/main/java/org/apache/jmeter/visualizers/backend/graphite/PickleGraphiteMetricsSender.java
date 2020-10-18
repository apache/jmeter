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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pickle Graphite format Partly based on
 * https://github.com/BrightcoveOS/metrics-graphite-pickle/blob/master/src/main/java/com/brightcove/metrics/reporting/GraphitePickleReporter.java
 * as per license
 * https://github.com/BrightcoveOS/metrics-graphite-pickle/blob/master/LICENSE.txt
 *
 * @since 2.13
 */
class PickleGraphiteMetricsSender extends AbstractGraphiteMetricsSender {
    private static final Logger log = LoggerFactory.getLogger(PickleGraphiteMetricsSender.class);

    /* Pickle opcodes needed for implementation */
    private static final char APPEND = 'a';
    private static final char LIST = 'l';
    private static final char LONG = 'L';
    private static final char MARK = '(';
    private static final char STOP = '.';
    private static final char STRING = 'S';
    private static final char TUPLE = 't';
    private static final char QUOTE = '\'';
    private static final char LF = '\n';

    private final Object lock = new Object();

    // graphite expects a python-pickled list of nested tuples.
    private List<MetricTuple> metrics = new ArrayList<>();

    private SocketConnectionInfos socketConnectionInfos;
    private GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> socketOutputStreamPool;
    private String prefix;

    PickleGraphiteMetricsSender() {
        super();
    }

    /**
     * @param graphiteHost Graphite Host
     * @param graphitePort Graphite Port
     * @param prefix       Common Metrics prefix
     */
    @Override
    public void setup(String graphiteHost, int graphitePort, String prefix) {
        this.prefix = prefix;
        this.socketConnectionInfos = new SocketConnectionInfos(graphiteHost, graphitePort);
        this.socketOutputStreamPool = createSocketOutputStreamPool();

        log.info("Created PickleGraphiteMetricsSender with host: {}, port: {}, prefix: {}",
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#
     * addMetric(long, java.lang.String, java.lang.String, java.lang.String)
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
            metrics = new ArrayList<>();
        }
        writeMetrics(currentMetrics);
    }

    private void writeMetrics(List<MetricTuple> currentMetrics) {
        SocketOutputStream out = null;
        try {
            String payload = convertMetricsToPickleFormat(currentMetrics);

            int length = payload.length();
            byte[] header = ByteBuffer.allocate(4).putInt(length).array();

            out = socketOutputStreamPool.borrowObject(socketConnectionInfos);
            out.write(header);
            // pickleWriter is not closed as it would close the underlying pooled out
            Writer pickleWriter = new OutputStreamWriter(out, CHARSET_NAME);
            pickleWriter.write(payload);
            pickleWriter.flush();
            socketOutputStreamPool.returnObject(socketConnectionInfos, out);
        } catch (Exception e) {
            // if there was an error, we might miss some data, for now, drop those and try to keep going.
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

        if (log.isDebugEnabled()) {
            log.debug("Wrote {} metrics", currentMetrics.size());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#destroy()
     */
    @Override
    public void destroy() {
        socketOutputStreamPool.close();
    }

    /** See: https://graphite.readthedocs.io/en/1.0.0/feeding-carbon.html */
    private static String convertMetricsToPickleFormat(List<MetricTuple> metrics) {
        StringBuilder pickled = new StringBuilder(metrics.size() * 75);
        pickled.append(MARK).append(LIST);

        for (MetricTuple tuple : metrics) {
            pickled.append(MARK) // begin outer tuple
                    .append(STRING) // the metric name is a string
                    // the single quotes are to match python's repr("abcd")
                    .append(QUOTE).append(tuple.name).append(QUOTE)
                    .append(LF)

                    // begin the inner tuple
                    .append(MARK)
                    // timestamp is a long
                    .append(LONG).append(tuple.timestamp)
                    // the trailing L is to match python's repr(long(1234))
                    .append(LONG).append(LF)
                    // and the value is a string.
                    .append(STRING).append(QUOTE).append(tuple.value).append(QUOTE).append(LF)
                    .append(TUPLE) // end inner tuple

                    .append(TUPLE) // end outer tuple
                    .append(APPEND);
        }

        // every pickle ends with STOP
        pickled.append(STOP);
        return pickled.toString();
    }
}
