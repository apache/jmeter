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
package org.apache.jmeter.report.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Represents a sample read from a CSV source.
 * <p>
 * Getters with a string parameter are implemented for convenience but should be
 * avoided as they are inefficient
 * </p>
 * 
 * @since 3.0
 */
public class Sample {

    private static final String ERROR_ON_SAMPLE = "Error in sample at line:";

    private static final String CONTROLLER_PATTERN = "Number of samples in transaction";
    
    private static final String EMPTY_CONTROLLER_PATTERN = "Number of samples in transaction : 0";

    private final boolean storesStartTimeStamp;
    private final SampleMetadata metadata;
    private final String[] data;
    private final long row;

    /**
     * Build a sample from a string array
     * 
     * @param row
     *            the row number in the CSV source from which this sample is
     *            built
     * @param metadata
     *            The sample metadata (contains column names)
     * @param data
     *            The sample data as a string array
     */
    public Sample(long row, SampleMetadata metadata, String... data) {
        this.row = row;
        this.metadata = metadata;
        this.data = data;
        this.storesStartTimeStamp = JMeterUtils.getPropDefault("sampleresult.timestamp.start", false);
    }

    /**
     * @return the row number from the CSV source from which this sample has
     *         been built.
     */
    public long getSampleRow() {
        return row;
    }

    /**
     * Gets the data stored in the column with the specified rank.
     * 
     * @param index
     *            the rank of the column
     * @return the data of the column
     */
    public String getData(int index) {
        return data[index];
    }

    /**
     * Gets the data stored in the column with the specified name.
     * 
     * @param name
     *            the name of the column
     * @return the data of the column
     */
    public String getData(String name) {
        return data[metadata.ensureIndexOf(name)];
    }

    /**
     * Gets the data of the column matching the specified rank and converts it
     * to an alternative type.
     * 
     * @param clazz
     *            the target class of the data
     * @param index
     *            the rank of the column
     * @param fieldName
     *            Field name
     * @param <T>
     *            type of data to be fetched
     * @return the converted value of the data
     */
    public <T> T getData(Class<T> clazz, int index, String fieldName) {
        try {
            return Converters.convert(clazz, data[index]);
        } catch (ConvertException ex) {
            throw new SampleException(ERROR_ON_SAMPLE + (row + 1)
                    + " converting field:" + fieldName + " at column:" + index
                    + " to:" + clazz.getName() + ", fieldValue:'" + data[index]
                    + "'", ex);
        }
    }

    /**
     * Gets the data of the column matching the specified name and converts it
     * to an alternative type.
     * 
     * @param clazz
     *            the target class of the data
     * @param name
     *            the name of the column
     * @param <T>
     *            type of data to be fetched
     * @return the converted value of the data
     */
    public <T> T getData(Class<T> clazz, String name) {
        return getData(clazz, metadata.ensureIndexOf(name), name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringUtils.join(data, metadata.getSeparator());
    }

    /**
     * Gets the time stamp stored in the sample.
     *
     * @return the time stamp
     */
    public long getTimestamp() {
        return getData(long.class, CSVSaveService.TIME_STAMP).longValue();
    }

    /**
     * Gets the elapsed time stored in the sample.
     *
     * @return the elapsed time stored in the sample
     */
    public long getElapsedTime() {
        return getData(long.class, CSVSaveService.CSV_ELAPSED).longValue();
    }

    /**
     * <p>
     * Gets the start time of the sample.
     * </p>
     * <p>
     * Start time depends on sampleresult.timestamp.start property :
     * </p>
     * <ul>
     * <li>If the property is true, this method returns the time stamp stored in
     * the sample.</li>
     * <li>If the property is false, this method returns the time stamp stored
     * in the sample minus the elapsed time.</li>
     * </ul>
     *
     * @return the start time
     */
    public long getStartTime() {
        return storesStartTimeStamp ? getTimestamp() : getTimestamp() - getElapsedTime();
    }

    /**
     * <p>
     * Gets the end time of the sample.
     * </p>
     * <p>
     * End time depends on jmeter.timestamp.start property :
     * </p>
     * <ul>
     * <li>If the property is true, this method returns the time stamp recorded
     * in the sample plus the elapsed time.</li>
     * <li>If the property is false, this method returns the time stamp
     * recorded.</li>
     * </ul>
     * 
     * @return the end time
     */
    public long getEndTime() {
        return storesStartTimeStamp ? getTimestamp() + getElapsedTime() : getTimestamp();
    }

    /**
     * Gets the response code stored in the sample.
     *
     * @return the response code stored in the sample
     */
    public String getResponseCode() {
        return getData(CSVSaveService.RESPONSE_CODE);
    }

    /**
     * Gets the failure message stored in the sample.
     *
     * @return the failure message stored in the sample
     */
    public String getFailureMessage() {
        return getData(CSVSaveService.FAILURE_MESSAGE);
    }

    /**
     * Gets the name stored in the sample.
     *
     * @return the name stored in the sample
     */
    public String getName() {
        return getData(CSVSaveService.LABEL);
    }

    /**
     * Gets the response message stored in the sample.
     *
     * @return the response message stored in the sample
     */
    public String getResponseMessage() {
        return getData(CSVSaveService.RESPONSE_MESSAGE);
    }

    /**
     * Gets the latency stored in the sample.
     *
     * @return the latency stored in the sample
     */
    public long getLatency() {
        return getData(long.class, CSVSaveService.CSV_LATENCY).longValue();
    }
    
    /**
     * Gets the connect time stored in the sample.
     *
     * @return the connect time stored in the sample or 0 is column is not in results
     */
    public long getConnectTime() {
        if(metadata.indexOf(CSVSaveService.CSV_CONNECT_TIME) >= 0) {
            return getData(long.class, CSVSaveService.CSV_CONNECT_TIME).longValue();
        } else {
            return 0L;
        }
    }

    /**
     * Gets the success status stored in the sample.
     *
     * @return the success status stored in the sample
     */
    public boolean getSuccess() {
        return getData(boolean.class, CSVSaveService.SUCCESSFUL).booleanValue();
    }

    /**
     * Gets the number of received bytes stored in the sample.
     *
     * @return the number of received bytes stored in the sample
     */
    public long getReceivedBytes() {
        return getData(long.class, CSVSaveService.CSV_BYTES).longValue();
    }

    /**
     * Gets the number of sent bytes stored in the sample.
     * If column is not in results, we return 0
     * @return the number of sent bytes stored in the sample
     */
    public long getSentBytes() {
        if(metadata.indexOf(CSVSaveService.CSV_SENT_BYTES) >= 0) {
            return getData(long.class, CSVSaveService.CSV_SENT_BYTES).longValue();
        } else {
            return 0L;
        }
    }

    /**
     * Gets the number of threads in the group of this sample.
     *
     * @return the number of threads in the group of this sample
     */
    public int getGroupThreads() {
        return getData(int.class, CSVSaveService.CSV_THREAD_COUNT1).intValue();
    }

    /**
     * Gets the overall number of threads.
     *
     * @return the overall number of threads
     */
    public int getAllThreads() {
        return getData(int.class, CSVSaveService.CSV_THREAD_COUNT2).intValue();
    }

    /**
     * Gets the thread name stored in the sample.
     *
     * @return the thread name stored in the sample
     */
    public String getThreadName() {
        return getData(CSVSaveService.THREAD_NAME);
    }

    /**
     * Checks if this sample is a controller.
     *
     * @return {@code true}, if this sample is a controller; otherwise
     *         {@code false}
     */
    public boolean isController() {
        String message = getResponseMessage();
        return message != null && message.startsWith(CONTROLLER_PATTERN);
    }
    
    /**
     * Checks if this sample is an empty controller.
     *
     * @return {@code true}, if this sample is a controller; otherwise
     *         {@code false}
     */
    public boolean isEmptyController() {
        String message = getResponseMessage();
        return message != null && message.startsWith(EMPTY_CONTROLLER_PATTERN);
    }
}
