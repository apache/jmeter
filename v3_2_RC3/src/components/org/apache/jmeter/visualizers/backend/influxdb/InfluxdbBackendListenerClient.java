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

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link AbstractBackendListenerClient} to write in an InfluxDB using 
 * custom schema
 * @since 3.2
 */
public class InfluxdbBackendListenerClient extends AbstractBackendListenerClient implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InfluxdbBackendListenerClient.class);
    private ConcurrentHashMap<String, SamplerMetric> metricsPerSampler = new ConcurrentHashMap<>();
    // Name of the measurement
    private static final String EVENTS_FOR_ANNOTATION = "events";
    
    private static final String TAGS = ",tags=";
    private static final String TEXT = "text=\"";

    // Name of the measurement
    private static final String DEFAULT_MEASUREMENT = "jmeter";

    private static final String TAG_TRANSACTION = ",transaction=";

    private static final String TAG_STATUT = ",statut=";
    private static final String TAG_APPLICATION = ",application=";

    private static final String METRIC_COUNT = "count=";
    private static final String METRIC_COUNT_ERREUR = "countError=";
    private static final String METRIC_MIN = "min=";
    private static final String METRIC_MAX = "max=";
    private static final String METRIC_AVG = "avg=";

    private static final String METRIC_HIT = "hit=";
    private static final String METRIC_PCT = "pct";

    private static final String METRIC_MAXAT = "maxAT=";
    private static final String METRIC_MINAT = "minAT=";
    private static final String METRIC_MEANAT = "meanAT=";
    private static final String METRIC_STARTEDT = "startedT=";
    private static final String METRIC_ENDEDT = "endedT=";

    private static final String TAG_OK = "ok";
    private static final String TAG_KO = "ko";
    private static final String TAG_ALL = "all";

    private static final String CUMULATED_METRICS = "all";
    private static final long SEND_INTERVAL = JMeterUtils.getPropDefault("backend_influxdb.send_interval", 5);
    private static final int MAX_POOL_SIZE = 1;
    private static final String SEPARATOR = ";"; //$NON-NLS-1$
    private static final Object LOCK = new Object();

    private boolean summaryOnly;
    private String measurement = "DEFAULT_MEASUREMENT";
    private String influxdbUrl = "";
    private String samplersRegex = "";
    private Pattern samplersToFilter;
    private Map<String, Float> okPercentiles;
    private Map<String, Float> koPercentiles;
    private Map<String, Float> allPercentiles;
    private String testTitle;
    private String testTags;
    // Name of the application tested
    private String application = "";

    private InfluxdbMetricsSender influxdbMetricsManager;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerHandle;

    public InfluxdbBackendListenerClient() {
        super();
    }

    @Override
    public void run() {
        sendMetrics();
    }

    /**
     * Send metrics
     */
    protected void sendMetrics() {

        synchronized (LOCK) {
            for (Map.Entry<String, SamplerMetric> entry : getMetricsInfluxdbPerSampler().entrySet()) {
                SamplerMetric metric = entry.getValue();
                if (entry.getKey().equals(CUMULATED_METRICS)) {
                    addCumulatedMetrics(metric);
                } else {
                    addMetrics(AbstractInfluxdbMetricsSender.tagToStringValue(entry.getKey()), metric);
                }
                // We are computing on interval basis so cleanup
                metric.resetForTimeInterval();
            }
        }

        UserMetric userMetrics = getUserMetrics();
        // For JMETER context
        StringBuilder tag = new StringBuilder(60);
        tag.append(TAG_APPLICATION).append(application);
        tag.append(TAG_TRANSACTION).append("internal");
        StringBuilder field = new StringBuilder(80);
        field.append(METRIC_MINAT).append(userMetrics.getMinActiveThreads()).append(",");
        field.append(METRIC_MAXAT).append(userMetrics.getMaxActiveThreads()).append(",");
        field.append(METRIC_MEANAT).append(userMetrics.getMeanActiveThreads()).append(",");
        field.append(METRIC_STARTEDT).append(userMetrics.getStartedThreads()).append(",");
        field.append(METRIC_ENDEDT).append(userMetrics.getFinishedThreads());

        influxdbMetricsManager.addMetric(measurement, tag.toString(), field.toString());

        influxdbMetricsManager.writeAndSendMetrics();
    }

    /**
     * Add request metrics to metrics manager.
     * 
     * @param metric
     *            {@link SamplerMetric}
     */
    private void addMetrics(String transaction, SamplerMetric metric) {
        // FOR ALL STATUS
        addMetric(transaction, metric, metric.getTotal(), false, TAG_ALL, metric.getAllMean(), metric.getAllMinTime(),
                metric.getAllMaxTime(), allPercentiles.values());
        // FOR OK STATUS
        addMetric(transaction, metric, metric.getSuccesses(), false, TAG_OK, metric.getOkMean(), metric.getOkMinTime(),
                metric.getOkMaxTime(), Collections.<Float> emptySet());
        // FOR KO STATUS
        addMetric(transaction, metric, metric.getFailures(), true, TAG_KO, metric.getKoMean(), metric.getKoMinTime(),
                metric.getKoMaxTime(), Collections.<Float> emptySet());
    }

    private void addMetric(String transaction, SamplerMetric metric, int count, boolean includeResponseCode,
            String statut, double mean, double minTime, double maxTime, Collection<Float> pcts) {
        if (count > 0) {
            StringBuilder tag = new StringBuilder(70);
            tag.append(TAG_APPLICATION).append(application);
            tag.append(TAG_STATUT).append(statut);
            tag.append(TAG_TRANSACTION).append(transaction);
            StringBuilder field = new StringBuilder(80);
            field.append(METRIC_COUNT).append(count);
            if (!Double.isNaN(mean)) {
                field.append(",").append(METRIC_AVG).append(mean);
            }
            if (!Double.isNaN(minTime)) {
                field.append(",").append(METRIC_MIN).append(minTime);
            }
            if (!Double.isNaN(maxTime)) {
                field.append(",").append(METRIC_MAX).append(maxTime);
            }
            for (Float pct : pcts) {
                field.append(",").append(METRIC_PCT).append(pct).append("=").append(metric.getAllPercentile(pct));
            }
            influxdbMetricsManager.addMetric(measurement, tag.toString(), field.toString());
        }
    }

    private void addCumulatedMetrics(SamplerMetric metric) {
        int total = metric.getTotal();
        if (total > 0) {
            StringBuilder tag = new StringBuilder(70);
            StringBuilder field = new StringBuilder(100);
            Collection<Float> pcts = allPercentiles.values();
            tag.append(TAG_APPLICATION).append(application);
            tag.append(TAG_TRANSACTION).append(CUMULATED_METRICS);
            tag.append(TAG_STATUT).append(CUMULATED_METRICS);

            field.append(METRIC_COUNT).append(total);
            field.append(",").append(METRIC_COUNT_ERREUR).append(metric.getFailures());

            if (!Double.isNaN(metric.getOkMean())) {
                field.append(",").append(METRIC_AVG).append(Double.toString(metric.getOkMean()));
            }
            if (!Double.isNaN(metric.getOkMinTime())) {
                field.append(",").append(METRIC_MIN).append(Double.toString(metric.getOkMinTime()));
            }
            if (!Double.isNaN(metric.getOkMaxTime())) {
                field.append(",").append(METRIC_MAX).append(Double.toString(metric.getOkMaxTime()));
            }

            field.append(",").append(METRIC_HIT).append(metric.getHits());
            for (Float pct : pcts) {
                field.append(",").append(METRIC_PCT).append(pct).append("=").append(Double.toString(metric.getAllPercentile(pct)));
            }
            field.append(",").append(METRIC_HIT).append(metric.getHits());
            influxdbMetricsManager.addMetric(measurement, tag.toString(), field.toString());
        }
    }

    /**
     * @return the samplersList
     */
    public String getSamplersRegex() {
        return samplersRegex;
    }

    /**
     * @param samplersList
     *            the samplersList to set
     */
    public void setSamplersList(String samplersList) {
        this.samplersRegex = samplersList;
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        synchronized (LOCK) {
            UserMetric userMetrics = getUserMetrics();
            for (SampleResult sampleResult : sampleResults) {
                userMetrics.add(sampleResult);
                Matcher matcher = samplersToFilter.matcher(sampleResult.getSampleLabel());
                if (!summaryOnly && (matcher.find())) {
                    SamplerMetric samplerMetric = getSamplerMetricInfluxdb(sampleResult.getSampleLabel());
                    samplerMetric.add(sampleResult);
                }
                SamplerMetric cumulatedMetrics = getSamplerMetricInfluxdb(CUMULATED_METRICS);
                cumulatedMetrics.add(sampleResult);
            }
        }
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        String influxdbMetricsSender = context.getParameter("influxdbMetricsSender");
        influxdbUrl = context.getParameter("influxdbUrl");
        summaryOnly = context.getBooleanParameter("summaryOnly", false);
        samplersRegex = context.getParameter("samplersRegex", "");
        application = AbstractInfluxdbMetricsSender.tagToStringValue(context.getParameter("application", ""));
        measurement = AbstractInfluxdbMetricsSender
                .tagToStringValue(context.getParameter("measurement", DEFAULT_MEASUREMENT));
        testTitle = context.getParameter("testTitle", "Test");
        testTags = AbstractInfluxdbMetricsSender.tagToStringValue(context.getParameter("eventTags", ""));
        String percentilesAsString = context.getParameter("percentiles", "");
        String[] percentilesStringArray = percentilesAsString.split(SEPARATOR);
        okPercentiles = new HashMap<>(percentilesStringArray.length);
        koPercentiles = new HashMap<>(percentilesStringArray.length);
        allPercentiles = new HashMap<>(percentilesStringArray.length);
        DecimalFormat format = new DecimalFormat("0.##");
        for (int i = 0; i < percentilesStringArray.length; i++) {
            if (!StringUtils.isEmpty(percentilesStringArray[i].trim())) {
                try {
                    Float percentileValue = Float.valueOf(percentilesStringArray[i].trim());
                    okPercentiles.put(AbstractInfluxdbMetricsSender.tagToStringValue(format.format(percentileValue)),
                            percentileValue);
                    koPercentiles.put(AbstractInfluxdbMetricsSender.tagToStringValue(format.format(percentileValue)),
                            percentileValue);
                    allPercentiles.put(AbstractInfluxdbMetricsSender.tagToStringValue(format.format(percentileValue)),
                            percentileValue);

                } catch (Exception e) {
                    log.error("Error parsing percentile: '{}'", percentilesStringArray[i], e);
                }
            }
        }
        Class<?> clazz = Class.forName(influxdbMetricsSender);
        this.influxdbMetricsManager = (InfluxdbMetricsSender) clazz.newInstance();
        influxdbMetricsManager.setup(influxdbUrl);
        samplersToFilter = Pattern.compile(samplersRegex);
        addAnnotation(true);

        scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
        // Start immediately the scheduler and put the pooling ( 5 seconds by default )
        this.timerHandle = scheduler.scheduleAtFixedRate(this, 0, SEND_INTERVAL, TimeUnit.SECONDS);

    }

    protected SamplerMetric getSamplerMetricInfluxdb(String sampleLabel) {
        SamplerMetric samplerMetric = metricsPerSampler.get(sampleLabel);
        if (samplerMetric == null) {
            samplerMetric = new SamplerMetric();
            SamplerMetric oldValue = metricsPerSampler.putIfAbsent(sampleLabel, samplerMetric);
            if (oldValue != null) {
                samplerMetric = oldValue;
            }
        }
        return samplerMetric;
    }

    private Map<String, SamplerMetric> getMetricsInfluxdbPerSampler() {
        return metricsPerSampler;
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        boolean cancelState = timerHandle.cancel(false);
        log.debug("Canceled state: {}", cancelState);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error waiting for end of scheduler");
            Thread.currentThread().interrupt();
        }

        addAnnotation(false);

        // Send last set of data before ending
        log.info("Sending last metrics");
        sendMetrics();

        influxdbMetricsManager.destroy();
        super.teardownTest(context);
    }

    /**
     * Add Annotation at start or end of the run ( usefull with Grafana )
     * Grafana will let you send HTML in the "Text" such as a link to the release notes
     * Tags are separated by spaces in grafana
     * Tags is put as InfluxdbTag for better query performance on it
     * Never double or single quotes in influxdb except for string field
     * see : https://docs.influxdata.com/influxdb/v1.1/write_protocols/line_protocol_reference/#quoting-special-characters-and-additional-naming-guidelines
     * * @param startOrEnd boolean true for start, false for end
     */
    private void addAnnotation(boolean startOrEnd) {
        influxdbMetricsManager.addMetric(EVENTS_FOR_ANNOTATION, 
                TAG_APPLICATION + application + ",title=ApacheJMeter"+
                (StringUtils.isNotEmpty(testTags) ? TAGS+ testTags : ""), 
                TEXT +  
                        AbstractInfluxdbMetricsSender.fieldToStringValue(testTitle +
                                (startOrEnd ? " started" : " ended")) + "\"" );
    }
    
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("influxdbMetricsSender", HttpMetricsSender.class.getName());
        arguments.addArgument("influxdbUrl", "");
        arguments.addArgument("application", "application name");
        arguments.addArgument("measurement", DEFAULT_MEASUREMENT);
        arguments.addArgument("summaryOnly", "false");
        arguments.addArgument("samplersRegex", ".*");
        arguments.addArgument("percentiles", "99,95,90");
        arguments.addArgument("testTitle", "Test name");
        arguments.addArgument("eventTags", "");
        return arguments;
    }
}
