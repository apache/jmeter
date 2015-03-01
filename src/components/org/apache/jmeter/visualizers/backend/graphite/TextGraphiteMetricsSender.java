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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * PlainText Graphite sender
 * @since 2.13
 */
class TextGraphiteMetricsSender extends AbstractGraphiteMetricsSender {
    private static final Logger LOG = LoggingManager.getLoggerForClass();        
        
    private String prefix;

    private List<MetricTuple> metrics = new ArrayList<MetricTuple>();

    private GenericKeyedObjectPool<SocketConnectionInfos, SocketOutputStream> socketOutputStreamPool;

    private SocketConnectionInfos socketConnectionInfos;

    /**
     * @param graphiteHost Graphite Host
     * @param graphitePort Graphite Port
     * @param prefix Common Metrics prefix
     */
    TextGraphiteMetricsSender() {
        super();
    }
    
    @Override
    public void setup(String graphiteHost, int graphitePort, String prefix) {
        this.prefix = prefix;
        this.socketConnectionInfos = new SocketConnectionInfos(graphiteHost, graphitePort);
        this.socketOutputStreamPool = createSocketOutputStreamPool();

        if(LOG.isInfoEnabled()) {
            LOG.info("Created TextGraphiteMetricsSender with host:"+graphiteHost+", port:"+graphitePort+", prefix:"+prefix);
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

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.graphite.GraphiteMetricsSender#writeAndSendMetrics()
     */
    @Override
    public void writeAndSendMetrics() {        
        if (metrics.size()>0) {
            SocketOutputStream out = null;
            try {
                out = socketOutputStreamPool.borrowObject(socketConnectionInfos);
                // pw is not closed as it would close the underlying pooled out
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, CHARSET_NAME), false);
                for (MetricTuple metric: metrics) {
                    pw.printf("%s %s %d%n", metric.name, metric.value, Long.valueOf(metric.timestamp));
                }
                pw.flush();
                // if there was an error, we might miss some data. for now, drop those on the floor and
                // try to keep going.
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Wrote "+ metrics.size() +" metrics");
                }
                socketOutputStreamPool.returnObject(socketConnectionInfos, out);
            } catch (Exception e) {
                if(out != null) {
                    try {
                        socketOutputStreamPool.invalidateObject(socketConnectionInfos, out);
                    } catch (Exception e1) {
                        LOG.warn("Exception invalidating socketOutputStream connected to graphite server '"+
                                socketConnectionInfos.getHost()+"':"+socketConnectionInfos.getPort(), e1);
                    }
                }
                LOG.error("Error writing to Graphite:"+e.getMessage());
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
        socketOutputStreamPool.close();
    }

}
