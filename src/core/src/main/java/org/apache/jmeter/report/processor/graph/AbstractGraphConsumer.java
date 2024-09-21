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

package org.apache.jmeter.report.processor.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.Aggregator;
import org.apache.jmeter.report.processor.AggregatorFactory;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ValueResultData;

/**
 * <p>
 * The class AbstractGraphConsumer provides a consumer that build a sorted map
 * from samples. It uses a projection to define the key (x-axis coordinate) and
 * an aggregator to define the value (y-axis coordinate).
 * </p>
 *
 * <strong>About the seriesData:</strong>
 * <p>
 * Series are defined by the seriesSelector, so they can be static or dynamic
 * (sample linked) depending on the implementation of the selector.
 * </p>
 *
 * <strong>About the groupData:</strong>
 * <p>
 * The grapher build an aggregator for each seriesData/key pair using an
 * external factory. All groupData from a series do the same aggregate
 * calculation.
 * </p>
 *
 * <strong>About the keys (x-axis coordinates):</strong>
 * <p>
 * Keys are defined by the keysSelector for each seriesData, so the keys can be
 * different depending on the seriesData
 * </p>
 *
 * <strong>About the values (y-axis coordinates):</strong>
 * <p>
 * Values are defined by the result aggregate produced by each aggregator.
 * During consumption, values to add to the groupData are defined by the
 * valueSelector.
 * </p>
 *
 * @since 3.0
 */
public abstract class AbstractGraphConsumer extends AbstractSampleConsumer {

    protected static final String DEFAULT_GROUP = "Generic group";

    public static final String RESULT_MIN_X = "minX";
    public static final String RESULT_MAX_X = "maxX";
    public static final String RESULT_MIN_Y = "minY";
    public static final String RESULT_MAX_Y = "maxY";
    public static final String RESULT_TITLE = "title";
    public static final String RESULT_SUPPORTS_CONTROLLERS_DISCRIMINATION = "supportsControllersDiscrimination";

    public static final String RESULT_SERIES = "series";
    public static final String RESULT_SERIES_NAME = "label";
    public static final String RESULT_SERIES_DATA = "data";
    public static final String RESULT_SERIES_IS_CONTROLLER = "isController";
    public static final String RESULT_SERIES_IS_OVERALL = "isOverall";

    public static final String DEFAULT_OVERALL_SERIES_FORMAT = "Overall %s";
    public static final String DEFAULT_AGGREGATED_KEYS_SERIES_FORMAT = "%s-Aggregated";

    private HashMap<String, GroupInfo> groupInfos;
    private GraphKeysSelector keysSelector;

    /** The overall seriesData name. */
    private String overallSeriesFormat = DEFAULT_OVERALL_SERIES_FORMAT;

    /** The aggregated keys seriesData format. */
    private String aggregatedKeysSeriesFormat = DEFAULT_AGGREGATED_KEYS_SERIES_FORMAT;

    /** Flag to indicate if we should swap keys and values in the result. */
    private boolean invertKeysAndValues;

    private boolean renderPercentiles;
    private String title;

    /**
     * Gets the group information.
     *
     * @return the group information
     */
    @SuppressWarnings("NonApiType")
    protected final HashMap<String, GroupInfo> getGroupInfos() {
        return groupInfos;
    }

    /**
     * Get flag to indicate we should swap keys and values.
     *
     * @return the invertKeysAndValues flag
     */
    protected final boolean getInvertsKeysAndValues() {
        return invertKeysAndValues;
    }

    /**
     * Set flag to indicate we should swap keys and values.
     *
     * @param invertKeysAndValues the reverts keys and values
     */
    protected final void setRevertKeysAndValues(boolean invertKeysAndValues) {
        this.invertKeysAndValues = invertKeysAndValues;
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
     * @param renderPercentiles flag to render percentiles or not
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
     * @param overallSeriesFormat the name of "overall" seriesData to set
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
     * @param aggregatedKeysSeriesFormat the format for the name of aggregated keys seriesData to set
     */
    public final void setAggregatedKeysSeriesFormat(
            String aggregatedKeysSeriesFormat) {
        this.aggregatedKeysSeriesFormat = aggregatedKeysSeriesFormat;
    }

    /**
     * Gets the title of the graph.
     *
     * @return the title of the graph
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the title of the graph.
     *
     * @param title the title to set
     */
    public final void setTitle(String title) {
        this.title = title;
    }

    protected AbstractGraphConsumer() {
    }

    protected abstract GraphKeysSelector createKeysSelector();

    protected abstract Map<String, GroupInfo> createGroupInfos();

    private static void setMinResult(MapResultData result, String name, Double value) {
        ValueResultData valueResult = (ValueResultData) result.getResult(name);
        valueResult.setValue(Math.min((Double) valueResult.getValue(),
                value));
    }

    private static void setMaxResult(MapResultData result, String name, Double value) {
        ValueResultData valueResult = (ValueResultData) result.getResult(name);
        valueResult.setValue(Math.max((Double) valueResult.getValue(),
                value));
    }

    /**
     * Adds a value map build from specified parameters to the result map.
     *
     * @param result     {@link MapResultData}
     * @param group
     * @param series
     * @param seriesData
     * @param aggregated
     */
    private void addKeyData(MapResultData result, @SuppressWarnings("unused") String group,
                            String series,
                            SeriesData seriesData, boolean aggregated) {

        // Override series name when aggregated
        if (aggregated) {
            series = String.format(aggregatedKeysSeriesFormat, series);
        }
        // Add to the result map
        ListResultData seriesList = (ListResultData) result
                .getResult(RESULT_SERIES);

        // Looks for series result using its name
        MapResultData seriesResult = null;
        int index = 0;
        int size = seriesList.getSize();
        while (seriesResult == null && index < size) {
            MapResultData currSeries = (MapResultData) seriesList.get(index);
            String name = String.valueOf(
                    ((ValueResultData) currSeries.getResult(RESULT_SERIES_NAME))
                            .getValue());
            if (Objects.equals(name, series)) {
                seriesResult = currSeries;
            }
            index++;
        }

        // Create series result if not found
        if (seriesResult == null) {
            seriesResult = createSerieResult(series, seriesData);
            seriesList.addResult(seriesResult);
        }

        ListResultData dataResult = (ListResultData) seriesResult
                .getResult(RESULT_SERIES_DATA);

        // Populate it with data from groupData
        Map<Double, Aggregator> aggInfo;
        if (aggregated) {
            aggInfo = new HashMap<>();
            aggInfo.put(
                    seriesData.getKeysAggregator().getResult(),
                    seriesData.getValuesAggregator());
        } else {
            aggInfo = seriesData.getAggregatorInfo();
        }
        if (!renderPercentiles) {
            for (Map.Entry<Double, Aggregator> entry : aggInfo.entrySet()) {
                // Init key and value depending on invertKeysAndValues property
                Double key = entry.getKey();
                Double value = entry.getValue().getResult();

                if (invertKeysAndValues) {
                    key = entry.getValue().getResult();
                    value = entry.getKey();
                }

                // Create result storage for coordinates
                ListResultData coordResult = new ListResultData();
                coordResult.addResult(new ValueResultData(key));
                coordResult.addResult(new ValueResultData(value));
                dataResult.addResult(coordResult);
                setMinResult(result, RESULT_MIN_X, key);
                setMaxResult(result, RESULT_MAX_X, key);
                setMinResult(result, RESULT_MIN_Y, value);
                setMaxResult(result, RESULT_MAX_Y, value);
            }
        } else {
            long count = seriesData.getCount();
            int rank = 0;
            double percent = 0;
            TreeMap<Double, Aggregator> sortedInfo = new TreeMap<>(aggInfo);
            if (!invertKeysAndValues) {
                for (Map.Entry<Double, Aggregator> entry : sortedInfo
                        .entrySet()) {
                    Double value = entry.getKey();
                    percent += (double) 100 * entry.getValue().getCount()
                            / count;
                    double percentile = (double) rank / 10;
                    while (percentile < percent) {
                        ListResultData coordResult = new ListResultData();
                        coordResult.addResult(new ValueResultData(
                                percentile));
                        coordResult.addResult(new ValueResultData(value));
                        dataResult.addResult(coordResult);
                        percentile = (double) ++rank / 10;
                    }
                    setMinResult(result, RESULT_MIN_Y, value);
                    setMaxResult(result, RESULT_MAX_Y, value);
                }
                setMinResult(result, RESULT_MIN_X, 0d);
                setMaxResult(result, RESULT_MAX_X, 100d);
            } else {
                for (Map.Entry<Double, Aggregator> entry : sortedInfo
                        .entrySet()) {
                    Double value = entry.getKey();
                    percent += (double) 100 * entry.getValue().getCount()
                            / count;
                    double percentile = (double) rank / 10;
                    while (percentile < percent) {
                        ListResultData coordResult = new ListResultData();
                        coordResult.addResult(new ValueResultData(value));
                        coordResult.addResult(new ValueResultData(
                                percentile));
                        dataResult.addResult(coordResult);
                        percentile = (double) ++rank / 10;
                    }
                    setMinResult(result, RESULT_MIN_X, value);
                    setMaxResult(result, RESULT_MAX_X, value);
                }
                setMinResult(result, RESULT_MIN_Y, 0d);
                setMaxResult(result, RESULT_MAX_Y, 100d);
            }
        }
    }

    /**
     * @param series     The series name
     * @param seriesData {@link SeriesData}
     * @return MapResultData metadata for serie
     */
    protected MapResultData createSerieResult(String series, SeriesData seriesData) {
        MapResultData seriesResult = new MapResultData();
        seriesResult.setResult(RESULT_SERIES_NAME,
                new ValueResultData(series));
        seriesResult.setResult(RESULT_SERIES_IS_CONTROLLER,
                new ValueResultData(
                        seriesData.isControllersSeries()));
        seriesResult.setResult(RESULT_SERIES_IS_OVERALL,
                new ValueResultData(
                        seriesData.isOverallSeries()));
        seriesResult.setResult(RESULT_SERIES_DATA, new ListResultData());
        return seriesResult;
    }

    /**
     * Aggregate a value to the aggregator defined by the specified parameters.
     */
    private static void aggregateValue(AggregatorFactory factory, SeriesData data,
                                       Double key, double value) {
        Map<Double, Aggregator> aggInfo = data.getAggregatorInfo();

        // Get or create aggregator
        Aggregator aggregator = aggInfo.get(key);
        if (aggregator == null) {
            aggregator = factory.createValueAggregator();
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

    private MapResultData createResult() {
        MapResultData result = new MapResultData();
        result.setResult(RESULT_MIN_X,
                new ValueResultData(Double.MAX_VALUE));
        result.setResult(RESULT_MAX_X,
                new ValueResultData(Double.MIN_VALUE));
        result.setResult(RESULT_MIN_Y,
                new ValueResultData(Double.MAX_VALUE));
        result.setResult(RESULT_MAX_Y,
                new ValueResultData(Double.MIN_VALUE));
        result.setResult(RESULT_TITLE, new ValueResultData(getTitle()));
        result.setResult(RESULT_SERIES, new ListResultData());

        boolean supportsControllersDiscrimination = groupInfos.values()
                .stream()
                .map(GroupInfo::getSeriesSelector)
                .allMatch(GraphSeriesSelector::allowsControllersDiscrimination);

        result.setResult(RESULT_SUPPORTS_CONTROLLERS_DISCRIMINATION,
                new ValueResultData(
                        supportsControllersDiscrimination));

        initializeExtraResults(result);
        return result;
    }

    /**
     * Inherited classes can add properties to the result
     *
     * @param parentResult the parent result
     */
    protected abstract void initializeExtraResults(MapResultData parentResult);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.csv.processor.SampleConsumer#startConsuming()
     */
    @Override
    public void startConsuming() {

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

        // Get key from sample
        Double key = keysSelector.select(sample);

        // Build groupData maps
        for (Map.Entry<String, GroupInfo> entryGroup : groupInfos.entrySet()) {
            GroupInfo groupInfo = entryGroup.getValue();
            GroupData groupData = groupInfo.getGroupData();
            AggregatorFactory factory = groupInfo.getAggregatorFactory();
            boolean overallSeries = groupInfo.enablesOverallSeries();
            boolean aggregatedKeysSeries = groupInfo
                    .enablesAggregatedKeysSeries();

            for (String seriesName : groupInfo.getSeriesSelector()
                    .select(sample)) {
                Map<String, SeriesData> seriesInfo = groupData.getSeriesInfo();
                SeriesData seriesData = seriesInfo.get(seriesName);
                if (seriesData == null) {
                    boolean isControllersSeries =
                            groupInfo.getSeriesSelector()
                                    .allowsControllersDiscrimination()
                                    && sample.isController();

                    seriesData = new SeriesData(
                            factory,
                            aggregatedKeysSeries,
                            isControllersSeries,
                            false);
                    seriesInfo.put(seriesName, seriesData);
                }

                // Get the value to aggregate and dispatch it to the groupData
                Double value = groupInfo.getValueSelector().select(seriesName,
                        sample);
                if (value != null) {
                    aggregateValue(factory, seriesData, key, value);
                    if (overallSeries) {
                        SeriesData overallData = groupData.getOverallSeries();
                        aggregateValue(factory, overallData, key, value);
                    }
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

        MapResultData result = createResult();

        // Get the aggregate results from the map
        for (Map.Entry<String, GroupInfo> groupEntry : groupInfos.entrySet()) {
            String groupName = groupEntry.getKey();
            GroupInfo groupInfo = groupEntry.getValue();
            GroupData groupData = groupInfo.getGroupData();
            boolean overallSeries = groupInfo.enablesOverallSeries();
            boolean aggregatedKeysSeries = groupInfo
                    .enablesAggregatedKeysSeries();

            for (Map.Entry<String, SeriesData> seriesEntry : groupData
                    .getSeriesInfo().entrySet()) {
                String seriesName = seriesEntry.getKey();
                SeriesData seriesData = seriesEntry.getValue();
                addKeyData(result, groupName, seriesName, seriesData, false);
                if (aggregatedKeysSeries) {
                    addKeyData(result, groupName, seriesName, seriesData, true);
                }
            }

            // Add overall values if needed
            if (overallSeries) {
                SeriesData overallData = groupData.getOverallSeries();
                String overallSeriesName = String.format(overallSeriesFormat,
                        groupName);
                addKeyData(result, groupName, overallSeriesName, overallData,
                        false);
                if (aggregatedKeysSeries) {
                    addKeyData(result, groupName, overallSeriesName,
                            overallData, true);
                }
            }
        }

        // Store the result
        setDataToContext(getName(), result);

        for (GroupInfo groupInfo : groupInfos.values()) {
            groupInfo.getGroupData().clear();
        }
    }

    public void initialize() {
        keysSelector = createKeysSelector();
        groupInfos = new HashMap<>(createGroupInfos());
    }
}
