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
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class InfluxdbBackendListenerClient extends AbstractBackendListenerClient implements Runnable {

	private static final Logger LOGGER = LoggingManager.getLoggerForClass();
	private ConcurrentHashMap<String, SamplerMetric> metricsPerSampler = new ConcurrentHashMap<>();
	// Name of the measurement
	private static final String DEFAULT_MEASUREMENT = "jmeter";
	// Name of the application tested
	private static String application = "";

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
	private static final long FIVE_SECOND = 5L;
	private static final int MAX_POOL_SIZE = 1;
	private static final String SEPARATOR = ";"; //$NON-NLS-1$
	private static final Object LOCK = new Object();

	private boolean summaryOnly;
	private String measurement = "DEFAULT_MEASURMENT";
	private String influxdbUrl = "";
	private String samplersRegex = "";
	private Pattern samplersToFilter;
	private Map<String, Float> okPercentiles;
	private Map<String, Float> koPercentiles;
	private Map<String, Float> allPercentiles;
	private String testTitle;

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
					addMetrics(AbstractInfluxdbMetricsSender.toStringValue(entry.getKey()), metric);
				}
				// We are computing on interval basis so cleanup
				metric.resetForTimeInterval();
			}
		}

		StringBuilder tag = new StringBuilder();
		StringBuilder field = new StringBuilder();

		// For JMETER context
		tag.append(TAG_APPLICATION + application);
		tag.append(TAG_TRANSACTION + "internal");
		field.append(METRIC_MINAT + Integer.toString(getUserMetrics().getMinActiveThreads()) + ",");
		field.append(METRIC_MAXAT + Integer.toString(getUserMetrics().getMaxActiveThreads()) + ",");
		field.append(METRIC_MEANAT + Integer.toString(getUserMetrics().getMeanActiveThreads()) + ",");
		field.append(METRIC_STARTEDT + Integer.toString(getUserMetrics().getStartedThreads()) + ",");
		field.append(METRIC_ENDEDT + Integer.toString(getUserMetrics().getFinishedThreads()));

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
			StringBuilder tag = new StringBuilder();
			StringBuilder field = new StringBuilder();
			tag.append(TAG_APPLICATION + application);
			tag.append(TAG_STATUT + statut);
			tag.append(TAG_TRANSACTION + transaction);
			field.append(METRIC_COUNT + count);
			if (mean != Double.NaN) {
				field.append("," + METRIC_AVG + mean);
			}
			if (minTime != Double.NaN) {
				field.append("," + METRIC_MIN + minTime);
			}
			if (maxTime != Double.NaN) {
				field.append("," + METRIC_MAX + maxTime);
			}
			for (Float pct : pcts) {
				field.append("," + METRIC_PCT + pct + "=" + metric.getAllPercentile(pct));
			}
			influxdbMetricsManager.addMetric(measurement, tag.toString(), field.toString());
		}
	}

	private void addCumulatedMetrics(SamplerMetric metric) {
		int total = metric.getTotal();
		if (total > 0) {
			StringBuilder tag = new StringBuilder();
			StringBuilder field = new StringBuilder();
			Collection<Float> pcts = allPercentiles.values();
			tag.append(TAG_APPLICATION + application);
			tag.append(TAG_TRANSACTION + CUMULATED_METRICS);
			tag.append(TAG_STATUT + CUMULATED_METRICS);

			field.append(METRIC_COUNT + Integer.toString(total));
			field.append("," + METRIC_COUNT_ERREUR + Integer.toString(metric.getFailures()));

			if (metric.getOkMean() != Double.NaN) {
				field.append("," + METRIC_AVG + Double.toString(metric.getOkMean()));
			}
			if (metric.getOkMinTime() != Double.NaN) {
				field.append("," + METRIC_MIN + Double.toString(metric.getOkMinTime()));
			}
			if (metric.getOkMaxTime() != Double.NaN) {
				field.append("," + METRIC_MAX + Double.toString(metric.getOkMaxTime()));
			}

			field.append("," + METRIC_HIT + Integer.toString(metric.getHits()));
			for (Float pct : pcts) {
				field.append("," + METRIC_PCT + pct + "=" + Double.toString(metric.getAllPercentile(pct)));
			}
			field.append("," + METRIC_HIT + Integer.toString(metric.getHits()));
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
			for (SampleResult sampleResult : sampleResults) {
				getUserMetrics().add(sampleResult);
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
		application = AbstractInfluxdbMetricsSender.toStringValue(context.getParameter("application", ""));
		measurement = AbstractInfluxdbMetricsSender
				.toStringValue(context.getParameter("measurement", DEFAULT_MEASUREMENT));
		testTitle = AbstractInfluxdbMetricsSender.toStringValue(context.getParameter("testTitle", "Test"));
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
					okPercentiles.put(AbstractInfluxdbMetricsSender.toStringValue(format.format(percentileValue)),
							percentileValue);
					koPercentiles.put(AbstractInfluxdbMetricsSender.toStringValue(format.format(percentileValue)),
							percentileValue);
					allPercentiles.put(AbstractInfluxdbMetricsSender.toStringValue(format.format(percentileValue)),
							percentileValue);

				} catch (Exception e) {
					LOGGER.error("Error parsing percentile:'" + percentilesStringArray[i] + "'", e);
				}
			}
		}
		Class<?> clazz = Class.forName(influxdbMetricsSender);
		this.influxdbMetricsManager = (InfluxdbMetricsSender) clazz.newInstance();
		influxdbMetricsManager.setup(influxdbUrl);
		samplersToFilter = Pattern.compile(samplersRegex);

		// Annotation of the start of the run
		influxdbMetricsManager.addMetric("annotation", ",application=" + application + ",title=JMETER",
				" text=\"" + testTitle + " started\"");

		scheduler = Executors.newScheduledThreadPool(MAX_POOL_SIZE);
		// Start scheduler and put the pooling to 5 seconds
		this.timerHandle = scheduler.scheduleAtFixedRate(this, FIVE_SECOND, FIVE_SECOND, TimeUnit.SECONDS);

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Canceled state:" + cancelState);
		}
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.error("Error waiting for end of scheduler");
		}
		// Annotation of the end of the run ( usefull with Grafana )
		influxdbMetricsManager.addMetric("annotation", ",application=" + application + ",title=JMETER",
				" text=\"" + testTitle + " ended\"");
		// Send last set of data before ending
		sendMetrics();

		influxdbMetricsManager.destroy();
		super.teardownTest(context);
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
		return arguments;
	}
}
