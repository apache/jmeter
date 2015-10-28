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
package org.apache.jmeter.report.processor.graph;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.jmeter.report.config.GraphConfiguration;
import org.apache.jmeter.report.core.ArgumentNullException;
import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.JsonUtil;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.Aggregator;
import org.apache.jmeter.report.processor.AggregatorFactory;
import org.apache.jmeter.report.processor.graph.GraphConsumerResult.GroupResult;
import org.apache.jmeter.report.processor.graph.GraphConsumerResult.KeyResult;
import org.apache.jmeter.report.processor.graph.GraphConsumerResult.SeriesResult;

/**
 * <p>
 * The class AbstractGraphConsumer provides a consumer that build a sorted map
 * from samples. It uses a projection to define the key (x-axis coordinate) and
 * an aggregator to define the value (y-axis coordinate).
 * </p>
 * 
 * <p>
 * <b>About the seriesData :</b><br>
 * Series are defined by the seriesSelector, so they can be static or dynamic
 * (sample linked) depending on the implementation of the selector.
 * </p>
 * 
 * <p>
 * <b>About the groupData :</b><br>
 * The grapher build an aggregator for each seriesData/key pair using an
 * external factory. All groupData from a serie do the same aggregate
 * calculation.
 * <p>
 * 
 * <p>
 * <b>About the keys (x-axis coordinates) :</b><br>
 * Keys are defined by the keysSelector for each seriesData, so the keys can be
 * different depending on the seriesData
 * <p>
 * 
 * <p>
 * <b>About the values (y-axis coordinates) :</b><br>
 * Values are defined by the result aggregate produced by each aggregator.
 * During consumption values to add to the groupData are defined by the
 * valueSelector.
 * </p>
 *
 * @since 2.14
 */
public abstract class AbstractGraphConsumer extends AbstractSampleConsumer {

    protected static final String DEFAULT_GROUP = "Generic group";

    public static final String RESULT_KEY = "Result";
    public static final String RESULT_CTX_VALUES = "values";
    public static final String RESULT_CTX_MIN_X = "minX";
    public static final String RESULT_CTX_MAX_X = "maxX";
    public static final String RESULT_CTX_MIN_Y = "minY";
    public static final String RESULT_CTX_MAX_Y = "maxY";
    public static final String RESULT_CTX_TITLE = "title";

    /** The Constant DEFAULT_OVERALL_SERIES_NAME. */
    public static final String DEFAULT_OVERALL_SERIES_FORMAT = "Overall %s";

    /** The Constant DEFAULT_AGGREGATED_KEYS_SERIES_FORMAT. */
    public static final String DEFAULT_AGGREGATED_KEYS_SERIES_FORMAT = "%s-Aggregated";

    /** The map used to store group information. */
    private final HashMap<String, GroupInfo> groupInfos;

    /** The keys selector. */
    private final GraphKeysSelector keysSelector;

    /** The overall seriesData name. */
    private String overallSeriesFormat = DEFAULT_OVERALL_SERIES_FORMAT;

    /** The aggregated keys seriesData format. */
    private String aggregatedKeysSeriesFormat = DEFAULT_AGGREGATED_KEYS_SERIES_FORMAT;

    /** reverts keys and values in the result. */
    private boolean revertsKeysAndValues;

    /** Renders percentiles in the results. */
    private boolean renderPercentiles;

    /**
     * Gets the group information.
     *
     * @return the group information
     */
    protected final HashMap<String, GroupInfo> getGroupInfos() {
	return groupInfos;
    }

    /**
     * Reverts keys and values.
     *
     * @return the revertKeysAndValues
     */
    protected final boolean revertsKeysAndValues() {
	return revertsKeysAndValues;
    }

    /**
     * Reverts keys and values.
     *
     * @param revertsKeysAndValues
     *            the reverts keys and values
     */
    protected final void setRevertKeysAndValues(boolean revertsKeysAndValues) {
	this.revertsKeysAndValues = revertsKeysAndValues;
    }

    /**
     * Indicates if the graph renders percentiles.
     *
     * @return true if percentiles are rendered; false otherwise
     */
    public final boolean rendersPercentiles() {
	return renderPercentiles;
    }

    /**
     * Enables or disables the percentiles render.
     *
     * @param renderPercentiles
     *            the render mode to set
     */
    public final void setRenderPercentiles(boolean renderPercentiles) {
	this.renderPercentiles = renderPercentiles;
    }

    /**
     * Gets the keys selector.
     *
     * @return the keys selector
     */
    protected final GraphKeysSelector getKeysSelector() {
	return keysSelector;
    }

    /**
     * Gets the format of the "overall" seriesData name.
     *
     * @return the format of the "overall" seriesData name
     */
    public final String getOverallSeriesFormat() {
	return overallSeriesFormat;
    }

    /**
     * Sets the format of the "overall" seriesData name.
     *
     * @param overallSeriesFormat
     *            the name of "overall" seriesData to set
     */
    public final void setOverallSeriesFormat(String overallSeriesFormat) {
	this.overallSeriesFormat = overallSeriesFormat;
    }

    /**
     * Gets the format for the name of aggregated keys seriesData.
     *
     * @return the format for the name of aggregated keys seriesData
     */
    public final String getAggregatedKeysSeriesFormat() {
	return aggregatedKeysSeriesFormat;
    }

    /**
     * Sets the format for the name of aggregated keys seriesData.
     *
     * @param aggregatedKeysSeriesFormat
     *            the format for the name of aggregated keys seriesData to set
     */
    public final void setAggregatedKeysSeriesFormat(
	    String aggregatedKeysSeriesFormat) {
	this.aggregatedKeysSeriesFormat = aggregatedKeysSeriesFormat;
    }

    /**
     * Instantiates a new abstract graph consumer.
     */
    protected AbstractGraphConsumer() {
	keysSelector = createKeysSelector();
	groupInfos = new HashMap<String, GroupInfo>(createGroupInfos());
    }

    protected abstract GraphKeysSelector createKeysSelector();

    protected abstract Map<String, GroupInfo> createGroupInfos();

    /**
     * Adds a value map build from specified parameters to the result map.
     *
     * @param seriesData
     *            the seriesData
     * @param map
     *            the groupData map
     */
    private void addKeyData(GraphConsumerResult result, String group,
	    String series, SeriesData seriesData, boolean aggregated) {
	// Create key data
	KeyResult keyResult = new KeyResult();

	// Populate it with data from groupData
	Map<Double, Aggregator> aggInfo;
	if (aggregated == false) {
	    aggInfo = seriesData.getAggregatorInfo();
	} else {
	    series = String.format(aggregatedKeysSeriesFormat, series);
	    aggInfo = new HashMap<Double, Aggregator>();
	    aggInfo.put(seriesData.getKeysAggregator().getResult(),
		    seriesData.getValuesAggregator());
	}
	if (renderPercentiles == false) {
	    for (Map.Entry<Double, Aggregator> entry : aggInfo.entrySet()) {
		// Init key and value depending on revertsKeysAndValues property
		Double key = entry.getKey();
		Double value = entry.getValue().getResult();
		if (revertsKeysAndValues == false) {
		    keyResult.put(key, value);
		    result.setMinX(key);
		    result.setMaxX(key);
		    result.setMinY(value);
		    result.setMaxY(value);
		} else {
		    keyResult.put(value, key);
		    result.setMinX(value);
		    result.setMaxX(value);
		    result.setMinY(key);
		    result.setMaxY(key);
		}
	    }
	} else {
	    long count = seriesData.getCount();
	    int rank = 0;
	    double percent = 0;
	    TreeMap<Double, Aggregator> sortedInfo = new TreeMap<Double, Aggregator>(
		    aggInfo);
	    if (revertsKeysAndValues == false) {
		for (Map.Entry<Double, Aggregator> entry : sortedInfo
		        .entrySet()) {
		    double value = entry.getKey();
		    percent += (double) 100 * entry.getValue().getCount()
			    / count;
		    double percentile = rank / 10d;
		    while (percentile < percent) {
			keyResult.put(percentile, value);
			percentile = ++rank / 10d;
		    }
		    result.setMinY(value);
		    result.setMaxY(value);
		}
		result.setMinX(0d);
		result.setMaxX(100d);
	    } else {
		for (Map.Entry<Double, Aggregator> entry : sortedInfo
		        .entrySet()) {
		    double value = entry.getKey();
		    percent += (double) 100 * entry.getValue().getCount()
			    / count;
		    double percentile = rank / 10d;
		    while (percentile < percent) {
			keyResult.put(value, percentile);
			percentile = ++rank / 10d;
		    }
		    result.setMinX(value);
		    result.setMaxX(value);
		}
		result.setMinY(0d);
		result.setMaxY(100d);
	    }
	}

	// Add to the result map
	SeriesResult seriesResult = result.getGroupResult().get(group);
	if (seriesResult == null) {
	    seriesResult = new SeriesResult();
	    result.getGroupResult().put(group, seriesResult);
	}
	seriesResult.put(series, keyResult);
    }

    /**
     * Aggregate a value to the aggregator defined by the specified parameters.
     *
     * @param groupData
     *            the map
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private void aggregateValue(AggregatorFactory factory, SeriesData data,
	    double key, double value) {
	Map<Double, Aggregator> aggInfo = data.getAggregatorInfo();

	// Get or create aggregator
	Aggregator aggregator = aggInfo.get(key);
	if (aggregator == null) {
	    aggregator = factory.createValueAggregator(key);
	    aggInfo.put(key, aggregator);
	}

	// Add the value to the aggregator
	aggregator.addValue(value);

	// Increment the count of sample for this series
	data.incrementCount();

	// Aggregate keys if needed (if aggregated keys series is set)
	Aggregator keysAgg = data.getKeysAggregator();
	if (keysAgg != null) {
	    keysAgg.addValue(key);
	}

	// Aggregate values if needed (if aggregated keys series is set)
	Aggregator valuesAgg = data.getValuesAggregator();
	if (valuesAgg != null) {
	    valuesAgg.addValue(value);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleConsumer#startConsuming()
     */
    @Override
    public void startConsuming() {
	// Store empty data structure in the current context to use it during
	// samples consumption.
	setLocalData(RESULT_KEY, new GraphConsumerResult());

	// Broadcast metadata to consumes for each channel
	int channelCount = getConsumedChannelCount();
	for (int i = 0; i < channelCount; i++) {
	    super.setProducedMetadata(getConsumedMetadata(i), i);
	}

	super.startProducing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleConsumer#consume(org.apache
     * .jmeter.report.csv.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {

	// Get key from sample and define min and max X
	Double key = keysSelector.select(sample);

	// Build groupData maps
	for (Map.Entry<String, GroupInfo> entryGroup : groupInfos.entrySet()) {
	    GroupInfo groupInfo = entryGroup.getValue();
	    GroupData groupData = groupInfo.getGroupData();
	    AggregatorFactory factory = groupInfo.getAggregatorFactory();
	    boolean overallSeries = groupInfo.enablesOverallSeries();
	    boolean aggregatedKeysSeries = groupInfo
		    .enablesAggregatedKeysSeries();

	    for (String seriesName : groupInfo.getSeriesSelector().select(
		    sample)) {
		Map<String, SeriesData> seriesInfo = groupData.getSeriesInfo();
		SeriesData seriesData = seriesInfo.get(seriesName);
		if (seriesData == null) {
		    seriesData = new SeriesData(factory, aggregatedKeysSeries);
		    seriesInfo.put(seriesName, seriesData);
		}

		// Get the value to aggregate and dispatch it to the groupData
		double value = groupInfo.getValueSelector().select(seriesName,
		        sample);

		aggregateValue(factory, seriesData, key, value);
		if (overallSeries == true) {
		    SeriesData overallData = groupData.getOverallSeries();
		    aggregateValue(factory, overallData, key, value);
		}

	    }
	}

	super.produce(sample, channel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.csv.processor.SampleConsumer#stopConsuming()
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.SampleConsumer#stopConsuming()
     */
    @Override
    public void stopConsuming() {
	super.stopProducing();

	GraphConsumerResult result = (GraphConsumerResult) getLocalData(RESULT_KEY);

	// Get the aggregate results from the map
	for (Map.Entry<String, GroupInfo> entryGroup : groupInfos.entrySet()) {
	    String groupName = entryGroup.getKey();
	    GroupInfo groupInfo = entryGroup.getValue();
	    GroupData groupData = groupInfo.getGroupData();
	    boolean overallSeries = groupInfo.enablesOverallSeries();
	    boolean aggregatedKeysSeries = groupInfo
		    .enablesAggregatedKeysSeries();

	    for (Map.Entry<String, SeriesData> entrySeries : groupData
		    .getSeriesInfo().entrySet()) {
		String seriesName = entrySeries.getKey();
		SeriesData seriesData = entrySeries.getValue();
		addKeyData(result, groupName, seriesName, seriesData, false);
		if (aggregatedKeysSeries == true) {
		    addKeyData(result, groupName, seriesName, seriesData, true);
		}
	    }

	    // Add overall values if needed
	    if (overallSeries == true) {
		SeriesData overallData = groupData.getOverallSeries();
		String overallSeriesName = String.format(overallSeriesFormat,
		        groupName);
		addKeyData(result, groupName, overallSeriesName, overallData,
		        false);
		if (aggregatedKeysSeries == true) {
		    addKeyData(result, groupName, overallSeriesName,
			    overallData, true);
		}
	    }
	}

	for (GroupInfo groupInfo : groupInfos.values()) {
	    groupInfo.getGroupData().clear();
	}
    }

    /**
     * <p>
     * Creates a json object used to produce flot charts data with the specified
     * seriesData data.
     * </p>
     * 
     * <p>
     * The keys of the map are used as seriesData label. The values of the map
     * are maps too where key is used as x-axis coordinates and values are used
     * as y-axis coordinates
     * </p>
     *
     * @param data
     *            the data used to populate the seriesData
     * @param attributes
     *            additional attributes to inject in the seriesData
     * @return the json object
     */
    private static JsonObject createJsonSeries(GroupResult data,
	    Map<String, String> attributes) {

	JsonObjectBuilder builder = Json.createObjectBuilder();

	for (Map.Entry<String, SeriesResult> entryGroup : data.entrySet()) {
	    for (Map.Entry<String, KeyResult> entrySeries : entryGroup
		    .getValue().entrySet()) {
		// Build the arrays
		JsonArrayBuilder dataBuilder = Json.createArrayBuilder();
		for (Map.Entry<Double, Double> dataSeries : entrySeries
		        .getValue().entrySet()) {
		    dataBuilder.add(Json.createArrayBuilder()
			    .add(dataSeries.getKey())
			    .add(dataSeries.getValue()));
		}

		// Build the seriesData and inject data inside
		String name = entrySeries.getKey();
		JsonObjectBuilder innerBuilder = Json.createObjectBuilder()
		        .add("label", name).add("data", dataBuilder);

		// Handle additional attributes
		if (attributes != null) {
		    for (Map.Entry<String, String> attributeEntry : attributes
			    .entrySet()) {
			JsonReader reader = Json.createReader(new StringReader(
			        attributeEntry.getValue()));
			try {

			    innerBuilder.add(attributeEntry.getKey(),
				    reader.readObject());
			} finally {
			    reader.close();
			}
		    }
		}
		builder.add(name, innerBuilder);

	    }
	}

	return builder.build();
    }

    /**
     * Export data including configuration items.
     *
     * @param configuration
     *            the configuration
     * @return the data context
     */
    public DataContext exportData(GraphConfiguration configuration) {
	if (configuration == null)
	    throw new ArgumentNullException("configuration");

	DataContext resultContext = new DataContext();
	GraphConsumerResult result = (GraphConsumerResult) getLocalData(RESULT_KEY);
	resultContext.put(
	        RESULT_CTX_VALUES,
	        JsonUtil.convertJsonToString(createJsonSeries(
	                result.getGroupResult(), getSeriesExtraAttibutes())));

	// Publish axis boundaries: configuration properties take precedence
	// over calculated boundaries
	Double fromConf;
	fromConf = configuration.getAbscissaMin();
	resultContext.put(RESULT_CTX_MIN_X, fromConf != null ? fromConf
	        : result.getMinX());

	fromConf = configuration.getAbscissaMax();
	resultContext.put(RESULT_CTX_MAX_X, fromConf != null ? fromConf
	        : result.getMaxX());

	fromConf = configuration.getOrdinateMin();
	resultContext.put(RESULT_CTX_MIN_Y, fromConf != null ? fromConf
	        : result.getMinY());

	fromConf = configuration.getOrdinateMax();
	resultContext.put(RESULT_CTX_MAX_Y, fromConf != null ? fromConf
	        : result.getMaxY());

	resultContext.put(RESULT_CTX_TITLE, configuration.getTitle());
	return resultContext;
    }

    protected Map<String, String> getSeriesExtraAttibutes() {
	return null;
    }

}
