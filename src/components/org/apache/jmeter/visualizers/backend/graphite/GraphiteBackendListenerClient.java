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
 * @see http://graphite.readthedocs.org/en/latest/overview.html
 * @since 2.13
 */
public class GraphiteBackendListenerClient extends AbstractBackendListenerClient implements Runnable {
    private static final int DEFAULT_PICKLE_PORT = 2004;
    private static final String CUMULATED_CONTEXT_NAME = "cumulated";

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();
    private static final String DEFAULT_METRICS_PREFIX = "jmeter."; //$NON-NLS-1$
    private static final String CUMULATED_METRICS = "__cumulated__"; //$NON-NLS-1$
    private static final String METRIC_MAX_ACTIVE_THREADS = "maxActiveThreads"; //$NON-NLS-1$
    private static final String METRIC_MIN_ACTIVE_THREADS = "minActiveThreads"; //$NON-NLS-1$
    private static final String METRIC_MEAN_ACTIVE_THREADS = "meanActiveThreads"; //$NON-NLS-1$
    private static final String METRIC_STARTED_THREADS = "startedThreads"; //$NON-NLS-1$
    private static final String METRIC_STOPPED_THREADS = "stoppedThreads"; //$NON-NLS-1$
    private static final String METRIC_FAILED_REQUESTS = "failure"; //$NON-NLS-1$
    private static final String METRIC_SUCCESSFUL_REQUESTS = "success"; //$NON-NLS-1$
    private static final String METRIC_TOTAL_REQUESTS = "total"; //$NON-NLS-1$
    private static final String METRIC_MIN_RESPONSE_TIME = "min"; //$NON-NLS-1$
    private static final String METRIC_MAX_RESPONSE_TIME = "max"; //$NON-NLS-1$
    private static final String METRIC_PERCENTILE_PREFIX = "percentile"; //$NON-NLS-1$
    private static final long ONE_SECOND = 1L;
    private static final int MAX_POOL_SIZE = 1;
    private static final String DEFAULT_PERCENTILES = "90;95;99";
    private static final String SEPARATOR = ";"; //$NON-NLS-1$

    private String graphiteHost;
    private int graphitePort;
    private boolean summaryOnly;
    private String rootMetricsPrefix;
    private String samplersList = ""; //$NON-NLS-1$
    private Set<String> samplersToFilter;
    private Map<String, Float> percentiles;
    

    private GraphiteMetricsSender pickleMetricsManager;

    private ScheduledExecutorService scheduler;
    
    public GraphiteBackendListenerClient() {
        super();
    }    

    @Override
    public void run() {
        // Need to convert millis to seconds for Graphite
        long timestamp = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        for (Map.Entry<String, SamplerMetric> entry : getMetricsPerSampler().entrySet()) {
            SamplerMetric metric = entry.getValue();
            if(entry.getKey().equals(CUMULATED_METRICS)) {
                addMetrics(timestamp, CUMULATED_CONTEXT_NAME, metric);
            } else {
                addMetrics(timestamp, AbstractGraphiteMetricsSender.sanitizeString(entry.getKey()), metric);                
            }
            // We are computing on interval basis so cleanup
            metric.resetForTimeInterval();
        }
        
        pickleMetricsManager.addMetric(timestamp, CUMULATED_CONTEXT_NAME, METRIC_MIN_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMaxActiveThreads()));
        pickleMetricsManager.addMetric(timestamp, CUMULATED_CONTEXT_NAME, METRIC_MAX_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMinActiveThreads()));
        pickleMetricsManager.addMetric(timestamp, CUMULATED_CONTEXT_NAME, METRIC_MEAN_ACTIVE_THREADS, Integer.toString(getUserMetrics().getMeanActiveThreads()));
        pickleMetricsManager.addMetric(timestamp, CUMULATED_CONTEXT_NAME, METRIC_STARTED_THREADS, Integer.toString(getUserMetrics().getStartedThreads()));
        pickleMetricsManager.addMetric(timestamp, CUMULATED_CONTEXT_NAME, METRIC_STOPPED_THREADS, Integer.toString(getUserMetrics().getFinishedThreads()));

        pickleMetricsManager.writeAndSendMetrics();
    }


    /**
     * @param timestamp
     * @param contextName
     * @param metric
     */
    private void addMetrics(long timestamp, String contextName, SamplerMetric metric) {
        pickleMetricsManager.addMetric(timestamp, contextName, METRIC_FAILED_REQUESTS, Integer.toString(metric.getFailures()));
        pickleMetricsManager.addMetric(timestamp, contextName, METRIC_SUCCESSFUL_REQUESTS, Integer.toString(metric.getSuccesses()));
        pickleMetricsManager.addMetric(timestamp, contextName, METRIC_TOTAL_REQUESTS, Integer.toString(metric.getTotal()));
        pickleMetricsManager.addMetric(timestamp, contextName, METRIC_MIN_RESPONSE_TIME, Double.toString(metric.getMinTime()));
        pickleMetricsManager.addMetric(timestamp, contextName, METRIC_MAX_RESPONSE_TIME, Double.toString(metric.getMaxTime()));
        for (Map.Entry<String, Float> entry : percentiles.entrySet()) {
            pickleMetricsManager.addMetric(timestamp, contextName, 
                    entry.getKey(), 
                    Double.toString(metric.getPercentile(entry.getValue().floatValue())));            
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

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        String graphiteMetricsSenderClass = context.getParameter("graphiteMetricsSender");
        
        graphiteHost = context.getParameter("graphiteHost");
        graphitePort = context.getIntParameter("graphitePort", DEFAULT_PICKLE_PORT);
        summaryOnly = context.getBooleanParameter("summaryOnly", true);
        samplersList = context.getParameter("samplersList", "");
        rootMetricsPrefix = context.getParameter("rootMetricsPrefix", DEFAULT_METRICS_PREFIX);
        String percentilesAsString = context.getParameter("percentiles", DEFAULT_METRICS_PREFIX);
        String[]  percentilesStringArray = percentilesAsString.split(SEPARATOR);
        percentiles = new HashMap<String, Float>(percentilesStringArray.length);
        DecimalFormat format = new DecimalFormat("0.##");
        for (int i = 0; i < percentilesStringArray.length; i++) {
            if(!StringUtils.isEmpty(percentilesStringArray[i].trim())) {
                try {
                    Float percentileValue = Float.parseFloat(percentilesStringArray[i].trim());
                    percentiles.put(
                            METRIC_PERCENTILE_PREFIX+AbstractGraphiteMetricsSender.sanitizeString(format.format(percentileValue)),
                            percentileValue);
                } catch(Exception e) {
                    LOGGER.error("Error parsing percentile:'"+percentilesStringArray[i]+"'", e);
                }
            }
        }
        Class<?> clazz = Class.forName(graphiteMetricsSenderClass);
        this.pickleMetricsManager = (GraphiteMetricsSender) clazz.newInstance();
        pickleMetricsManager.setup(graphiteHost, graphitePort, rootMetricsPrefix);
        String[] samplers = samplersList.split(SEPARATOR);
        samplersToFilter = new HashSet<String>();
        for (String samplerName : samplers) {
            samplersToFilter.add(samplerName);
        }
        scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
        // Don't change this as metrics are per second
        scheduler.scheduleAtFixedRate(this, ONE_SECOND, ONE_SECOND, TimeUnit.SECONDS);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Error waiting for end of scheduler");
        }
        
        samplersToFilter.clear();
        pickleMetricsManager.destroy();
        super.teardownTest(context);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("graphiteMetricsSender", TextGraphiteMetricsSender.class.getName());
        arguments.addArgument("graphiteHost", "");
        arguments.addArgument("graphitePort", Integer.toString(DEFAULT_PICKLE_PORT));
        arguments.addArgument("rootMetricsPrefix", DEFAULT_METRICS_PREFIX);
        arguments.addArgument("summaryOnly", "true");
        arguments.addArgument("samplersList", "");
        arguments.addArgument("percentiles", DEFAULT_PERCENTILES);
        return arguments;
    }
}
