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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Graphite based Listener using Pickle Protocol
 * @see <a href="http://graphite.readthedocs.org/en/latest/overview.html">Graphite Overview</a>
 * @since 2.13
 */
public class GraphiteBackendListenerClient extends AbstractBackendListenerClient implements Runnable {
    private static final int DEFAULT_PLAINTEXT_PROTOCOL_PORT = 2003;
    private static final String TEST_CONTEXT_NAME = "test";
    private static final String ALL_CONTEXT_NAME = "all";

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();
    private static final String DEFAULT_METRICS_PREFIX = "jmeter."; //$NON-NLS-1$
    private static final String CUMULATED_METRICS = "__cumulated__"; //$NON-NLS-1$
    // User Metrics
    private static final String METRIC_MAX_ACTIVE_THREADS = "maxAT"; //$NON-NLS-1$
    private static final String METRIC_MIN_ACTIVE_THREADS = "minAT"; //$NON-NLS-1$
    private static final String METRIC_MEAN_ACTIVE_THREADS = "meanAT"; //$NON-NLS-1$
    private static final String METRIC_STARTED_THREADS = "startedT"; //$NON-NLS-1$
    private static final String METRIC_FINISHED_THREADS = "endedT"; //$NON-NLS-1$
    
    // Response time Metrics
    private static final String METRIC_SEPARATOR = "."; //$NON-NLS-1$
    private static final String METRIC_OK_PREFIX = "ok"; //$NON-NLS-1$
    private static final String METRIC_KO_PREFIX = "ko"; //$NON-NLS-1$
    private static final String METRIC_ALL_PREFIX = "a";

    
    private static final String METRIC_COUNT = "count"; //$NON-NLS-1$
    private static final String METRIC_MIN_RESPONSE_TIME = "min"; //$NON-NLS-1$
    private static final String METRIC_MAX_RESPONSE_TIME = "max"; //$NON-NLS-1$
    private static final String METRIC_PERCENTILE = "pct"; //$NON-NLS-1$
    
    private static final String METRIC_OK_COUNT             = METRIC_OK_PREFIX+METRIC_SEPARATOR+METRIC_COUNT;
    private static final String METRIC_OK_MIN_RESPONSE_TIME = METRIC_OK_PREFIX+METRIC_SEPARATOR+METRIC_MIN_RESPONSE_TIME;
    private static final String METRIC_OK_MAX_RESPONSE_TIME = METRIC_OK_PREFIX+METRIC_SEPARATOR+METRIC_MAX_RESPONSE_TIME;
    private static final String METRIC_OK_PERCENTILE_PREFIX = METRIC_OK_PREFIX+METRIC_SEPARATOR+METRIC_PERCENTILE;

    private static final String METRIC_KO_COUNT             = METRIC_KO_PREFIX+METRIC_SEPARATOR+METRIC_COUNT;
    private static final String METRIC_KO_MIN_RESPONSE_TIME = METRIC_KO_PREFIX+METRIC_SEPARATOR+METRIC_MIN_RESPONSE_TIME;
    private static final String METRIC_KO_MAX_RESPONSE_TIME = METRIC_KO_PREFIX+METRIC_SEPARATOR+METRIC_MAX_RESPONSE_TIME;
    private static final String METRIC_KO_PERCENTILE_PREFIX = METRIC_KO_PREFIX+METRIC_SEPARATOR+METRIC_PERCENTILE;

    private static final String METRIC_ALL_COUNT             = METRIC_ALL_PREFIX+METRIC_SEPARATOR+METRIC_COUNT;
    private static final String METRIC_ALL_MIN_RESPONSE_TIME = METRIC_ALL_PREFIX+METRIC_SEPARATOR+METRIC_MIN_RESPONSE_TIME;
    private static final String METRIC_ALL_MAX_RESPONSE_TIME = METRIC_ALL_PREFIX+METRIC_SEPARATOR+METRIC_MAX_RESPONSE_TIME;
    private static final String METRIC_ALL_PERCENTILE_PREFIX = METRIC_ALL_PREFIX+METRIC_SEPARATOR+METRIC_PERCENTILE;

    private static final long ONE_SECOND = 1L;
    private static final int MAX_POOL_SIZE = 1;
    private static final String DEFAULT_PERCENTILES = "90;95;99";
    private static final String SEPARATOR = ";"; //$NON-NLS-1$
    private static final Object LOCK = new Object();

    private String graphiteHost;
    private int graphitePort;
    private boolean summaryOnly;
    private String rootMetricsPrefix;
    private String samplersList = ""; //$NON-NLS-1$
    private Set<String> samplersToFilter;
    private Map<String, Float> okPercentiles;
    private Map<String, Float> koPercentiles;
    private Map<String, Float> allPercentiles;
    

    private GraphiteMetricsSender graphiteMetricsManager;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerHandle;
    
    public GraphiteBackendListenerClient() {
        super();
    }    

    @Override
    public void run() {
        sendMetrics();
    }

    /**
     * Send metrics to Graphite
     */
    protected void sendMetrics() {
        // Need to convert millis to seconds for Graphite
        long timestampInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        synchronized (LOCK) {
            for (Map.Entry<String, SamplerMetric> entry : getMetricsPerSampler().entrySet()) {
                SamplerMetric metric = entry.getValue();
                if(entry.getKey().equals(CUMULATED_METRICS)) {
                    addMetrics(timestampInSeconds, ALL_CONTEXT_NAME, metric);
                } else {
                    addMetrics(timestampInSeconds, AbstractGraphiteMetricsSender.sanitizeString(entry.getKey()), metric);                
                }
                // We are computing on interval basis so cleanup
                metric.resetForTimeInterval();
            }
        }        
        graphiteMetricsManager.addMetric(timestampInSeconds, TEST_CONTEXT_NAME, METRIC_MIN_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMinActiveThreads()));
        graphiteMetricsManager.addMetric(timestampInSeconds, TEST_CONTEXT_NAME, METRIC_MAX_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMaxActiveThreads()));
        graphiteMetricsManager.addMetric(timestampInSeconds, TEST_CONTEXT_NAME, METRIC_MEAN_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMeanActiveThreads()));
        graphiteMetricsManager.addMetric(timestampInSeconds, TEST_CONTEXT_NAME, METRIC_STARTED_THREADS, Integer.toString(getUserMetrics().getStartedThreads()));
        graphiteMetricsManager.addMetric(timestampInSeconds, TEST_CONTEXT_NAME, METRIC_FINISHED_THREADS, Integer.toString(getUserMetrics().getFinishedThreads()));

        graphiteMetricsManager.writeAndSendMetrics();
    }


    /**
     * Add request metrics to metrics manager.
     * Note if total number of requests is 0, no response time metrics are sent.
     * @param timestampInSeconds long
     * @param contextName String
     * @param metric {@link SamplerMetric}
     */
    private void addMetrics(long timestampInSeconds, String contextName, SamplerMetric metric) {
        graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_OK_COUNT, Integer.toString(metric.getSuccesses()));
        graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_KO_COUNT, Integer.toString(metric.getFailures()));
        graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_ALL_COUNT, Integer.toString(metric.getTotal()));
        // See https://issues.apache.org/bugzilla/show_bug.cgi?id=57350
        if(metric.getTotal() > 0) { 
            if(metric.getSuccesses()>0) {
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_OK_MIN_RESPONSE_TIME, Double.toString(metric.getOkMinTime()));
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_OK_MAX_RESPONSE_TIME, Double.toString(metric.getOkMaxTime()));
                for (Map.Entry<String, Float> entry : okPercentiles.entrySet()) {
                    graphiteMetricsManager.addMetric(timestampInSeconds, contextName, 
                            entry.getKey(), 
                            Double.toString(metric.getOkPercentile(entry.getValue().floatValue())));            
                }
            } 
            if(metric.getFailures()>0) {
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_KO_MIN_RESPONSE_TIME, Double.toString(metric.getKoMinTime()));
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_KO_MAX_RESPONSE_TIME, Double.toString(metric.getKoMaxTime()));
                for (Map.Entry<String, Float> entry : koPercentiles.entrySet()) {
                    graphiteMetricsManager.addMetric(timestampInSeconds, contextName, 
                            entry.getKey(), 
                            Double.toString(metric.getKoPercentile(entry.getValue().floatValue())));            
                }   
            }
            if(metric.getTotal()>0) {
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_ALL_MIN_RESPONSE_TIME, Double.toString(metric.getAllMinTime()));
                graphiteMetricsManager.addMetric(timestampInSeconds, contextName, METRIC_ALL_MAX_RESPONSE_TIME, Double.toString(metric.getAllMaxTime()));
                for (Map.Entry<String, Float> entry : allPercentiles.entrySet()) {
                    graphiteMetricsManager.addMetric(timestampInSeconds, contextName, 
                            entry.getKey(), 
                            Double.toString(metric.getAllPercentile(entry.getValue().floatValue())));            
                }   
                
            }
        }
    }

    /**
     * @return the samplersList
     */
    public String getSamplersList() {
        return samplersList;
    }

    /**
     * @param samplersList the samplersList to set
     */
    public void setSamplersList(String samplersList) {
        this.samplersList = samplersList;
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults,
            BackendListenerContext context) {
        synchronized (LOCK) {
            for (SampleResult sampleResult : sampleResults) {
                getUserMetrics().add(sampleResult);
                if(!summaryOnly && samplersToFilter.contains(sampleResult.getSampleLabel())) {
                    SamplerMetric samplerMetric = getSamplerMetric(sampleResult.getSampleLabel());
                    samplerMetric.add(sampleResult);
                }
                SamplerMetric cumulatedMetrics = getSamplerMetric(CUMULATED_METRICS);
                cumulatedMetrics.add(sampleResult);                    
            }
        }
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        String graphiteMetricsSenderClass = context.getParameter("graphiteMetricsSender");
        
        graphiteHost = context.getParameter("graphiteHost");
        graphitePort = context.getIntParameter("graphitePort", DEFAULT_PLAINTEXT_PROTOCOL_PORT);
        summaryOnly = context.getBooleanParameter("summaryOnly", true);
        samplersList = context.getParameter("samplersList", "");
        rootMetricsPrefix = context.getParameter("rootMetricsPrefix", DEFAULT_METRICS_PREFIX);
        String percentilesAsString = context.getParameter("percentiles", DEFAULT_METRICS_PREFIX);
        String[]  percentilesStringArray = percentilesAsString.split(SEPARATOR);
        okPercentiles = new HashMap<String, Float>(percentilesStringArray.length);
        koPercentiles = new HashMap<String, Float>(percentilesStringArray.length);
        allPercentiles = new HashMap<String, Float>(percentilesStringArray.length);
        DecimalFormat format = new DecimalFormat("0.##");
        for (int i = 0; i < percentilesStringArray.length; i++) {
            if(!StringUtils.isEmpty(percentilesStringArray[i].trim())) {
                try {
                    Float percentileValue = Float.parseFloat(percentilesStringArray[i].trim());
                    okPercentiles.put(
                            METRIC_OK_PERCENTILE_PREFIX+AbstractGraphiteMetricsSender.sanitizeString(format.format(percentileValue)),
                            percentileValue);
                    koPercentiles.put(
                            METRIC_KO_PERCENTILE_PREFIX+AbstractGraphiteMetricsSender.sanitizeString(format.format(percentileValue)),
                            percentileValue);
                    allPercentiles.put(
                            METRIC_ALL_PERCENTILE_PREFIX+AbstractGraphiteMetricsSender.sanitizeString(format.format(percentileValue)),
                            percentileValue);

                } catch(Exception e) {
                    LOGGER.error("Error parsing percentile:'"+percentilesStringArray[i]+"'", e);
                }
            }
        }
        Class<?> clazz = Class.forName(graphiteMetricsSenderClass);
        this.graphiteMetricsManager = (GraphiteMetricsSender) clazz.newInstance();
        graphiteMetricsManager.setup(graphiteHost, graphitePort, rootMetricsPrefix);
        String[] samplers = samplersList.split(SEPARATOR);
        samplersToFilter = new HashSet<String>();
        for (String samplerName : samplers) {
            samplersToFilter.add(samplerName);
        }
        scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
        // Don't change this as metrics are per second
        this.timerHandle = scheduler.scheduleAtFixedRate(this, ONE_SECOND, ONE_SECOND, TimeUnit.SECONDS);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        boolean cancelState = timerHandle.cancel(false);
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Canceled state:"+cancelState);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Error waiting for end of scheduler");
        }
        // Send last set of data before ending
        sendMetrics();
        
        samplersToFilter.clear();
        graphiteMetricsManager.destroy();
        super.teardownTest(context);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("graphiteMetricsSender", TextGraphiteMetricsSender.class.getName());
        arguments.addArgument("graphiteHost", "");
        arguments.addArgument("graphitePort", Integer.toString(DEFAULT_PLAINTEXT_PROTOCOL_PORT));
        arguments.addArgument("rootMetricsPrefix", DEFAULT_METRICS_PREFIX);
        arguments.addArgument("summaryOnly", "true");
        arguments.addArgument("samplersList", "");
        arguments.addArgument("percentiles", DEFAULT_PERCENTILES);
        return arguments;
    }
}
