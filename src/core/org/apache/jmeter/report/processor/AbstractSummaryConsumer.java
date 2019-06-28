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
package org.apache.jmeter.report.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.Sample;

/**
 * The class AbstractSummaryConsumer provides a base class for data of the
 * dashboard page.
 *
 * @param <TData>
 *            the type of data to process
 * @since 3.0
 */
public abstract class AbstractSummaryConsumer<TData> extends
        AbstractSampleConsumer {

    /**
     * The class SummaryInfo stores intermediate results.
     */
    protected class SummaryInfo {
        final boolean isController;
        TData data;

        /**
         * Checks if these information implies controller sample.
         *
         * @return true, if is controller
         */
        public final boolean isController() {
            return isController;
        }

        /**
         * Gets the data to store.
         *
         * @return the data to store
         */
        public final TData getData() {
            return data;
        }

        /**
         * Sets the data to store.
         *
         * @param data
         *            the new data to store
         */
        public final void setData(TData data) {
            this.data = data;
        }

        /**
         * Instantiates a new summary info.
         *
         * @param isController
         *            true, if these information implies only controller
         *            samples; false otherwise
         */
        public SummaryInfo(boolean isController) {
            this.isController = isController;
        }

    }

    public static final String RESULT_VALUE_DATA = "data";
    public static final String RESULT_VALUE_IS_CONTROLLER = "isController";
    public static final String RESULT_VALUE_ITEMS = "items";
    public static final String RESULT_VALUE_OVERALL = "overall";
    public static final String RESULT_VALUE_SUPPORTS_CONTROLLERS_DISCRIMINATION = "supportsControllersDiscrimination";
    public static final String RESULT_VALUE_TITLES = "titles";

    private final Map<String, SummaryInfo> infos = new HashMap<>();
    private final SummaryInfo overallInfo = new SummaryInfo(false);
    private final boolean supportsControllersDiscrimination;

    private boolean hasOverallResult;

    /**
     * Defines whether the result contains an overall item.
     *
     * @return true, if the result contains an overall item
     */
    public final boolean hasOverallResult() {
        return hasOverallResult;
    }

    /**
     * Defines whether the result contains an overall item.
     *
     * @param hasOverallResult
     *            true, if the result contains an overall item; false otherwise
     */
    public final void setHasOverallResult(boolean hasOverallResult) {
        this.hasOverallResult = hasOverallResult;
    }

    /**
     * Indicates whether this summary can discriminate controller samples
     *
     * @return true, if this summary can discriminate controller samples; false
     *         otherwise.
     */
    public final boolean suppportsControllersDiscrimination() {
        return supportsControllersDiscrimination;
    }

    /**
     * Gets the overall info.
     *
     * @return the overall info
     */
    protected final SummaryInfo getOverallInfo() {
        return overallInfo;
    }

    /**
     * Gets the summary infos.
     *
     * @return the summary infos
     */
    protected final Map<String, SummaryInfo> getSummaryInfos() {
        return infos;
    }

    /**
     * Instantiates a new abstract summary consumer.
     *
     * @param supportsControllersDiscrimination
     *            indicates whether this summary can discriminate controller
     *            samples
     */
    protected AbstractSummaryConsumer(boolean supportsControllersDiscrimination) {
        this.supportsControllersDiscrimination = supportsControllersDiscrimination;
    }

    /**
     * Gets the identifier key from sample.<br>
     * This key is use identify the SummaryInfo linked with the sample
     *
     * @param sample
     *            the sample
     * @return the key identifying the sample
     */
    protected abstract String getKeyFromSample(Sample sample);

    /**
     * Creates a result item for information identified by the specified key.
     *
     * @param key
     *            the key
     * @param data
     *            the data
     * @return the list result data
     */
    protected abstract ListResultData createDataResult(String key, TData data);

    /**
     * Creates the result containing titles of columns.
     *
     * @return the list of titles
     */
    protected abstract ListResultData createResultTitles();

    /**
     * Update the stored data with the data from the specified sample.
     *
     * @param info SummaryInfo
     * @param sample
     *            the sample
     */
    protected abstract void updateData(SummaryInfo info, Sample sample);

    private MapResultData createResultFromKey(String key) {
        SummaryInfo info = (key == null) ? overallInfo : infos.get(key);
        MapResultData result = null;
        TData data = info.getData();
        if (data != null) {
            result = new MapResultData();
            result.setResult(RESULT_VALUE_IS_CONTROLLER, new ValueResultData(
                    Boolean.valueOf(info.isController())));
            result.setResult(RESULT_VALUE_DATA, createDataResult(key, data));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.SampleConsumer#startConsuming()
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
     * org.apache.jmeter.report.processor.SampleConsumer#consume(org.apache.
     * jmeter.report.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {
        String key = getKeyFromSample(sample);

        // Get the object to store counters or create it if it does not exist.
        SummaryInfo info = infos.get(key);
        if (info == null) {
            info = new SummaryInfo(supportsControllersDiscrimination
                    && sample.isController());
            infos.put(key, info);
        }
        updateData(info, sample);
        super.produce(sample, channel);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.processor.SampleConsumer#stopConsuming()
     */
    @Override
    public void stopConsuming() {
        MapResultData result = new MapResultData();

        // Push the support flag in the result
        result.setResult(RESULT_VALUE_SUPPORTS_CONTROLLERS_DISCRIMINATION,
                new ValueResultData(Boolean.valueOf(supportsControllersDiscrimination)));

        // Add headers
        result.setResult(RESULT_VALUE_TITLES, createResultTitles());

        // Add overall row if needed
        if (hasOverallResult) {
            MapResultData overallResult = createResultFromKey(null);
            if (overallResult != null) {
                result.setResult(RESULT_VALUE_OVERALL, overallResult);
            }
        }

        // Build rows from samples
        ListResultData itemsResult = new ListResultData();
        for (String key : infos.keySet()) {
            // Add result only if data exist
            MapResultData keyResult = createResultFromKey(key);
            if (keyResult != null) {
                itemsResult.addResult(keyResult);
            }
        }
        result.setResult(RESULT_VALUE_ITEMS, itemsResult);

        // Store the result in the context
        setDataToContext(getName(), result);

        super.stopProducing();

        // Reset infos
        infos.clear();
        overallInfo.setData(null);
    }
}
