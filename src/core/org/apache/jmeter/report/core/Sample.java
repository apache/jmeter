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

import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Represents a sample read from a CSV source.
 * <p>
 * Getters with a string parameter are implemented for convenience but should be
 * avoided as they are inefficient
 * </p>
 * 
 * @since 2.14
 */
public class Sample {

    public final static String CONTROLLER_PATTERN = "Number of samples in transaction";

    private boolean storesStartTimeStamp;
    private SampleMetadata metadata;
    String[] data;
    private long row;

    /**
     * Build a sample from a string array
     * 
     * @param row
     *            the row number in the CSV source from which this sample is
     *            built
     * @param metadata
     *            The sample metadata (containes column names)
     * @param data
     *            The sample data as a string array
     */
    public Sample(long row, SampleMetadata metadata, String... data) {
        this.row = row;
        this.metadata = metadata;
        this.data = data;
        this.storesStartTimeStamp = JMeterUtils.getPropDefault(
                "sampleresult.timestamp.start", false);
    }

    /**
     * @return the row number from the CSV source from which this sample has been
     * built.
     */
    public long getSampleRow() {
        return row;
    }

    /**
     * Get the data whose column name is provided as a string. The use of this
     * method should be avoided because of its inefficiency.
     * 
     * @param name
     *            The data name from the sample metadata
     * @return The data value
     */
    public String getString(String name) {
        return data[metadata.indexOf(name)];
    }

    /**
     * Get the data of the ith column as a string. Very fast because it is a
     * single array access.
     * 
     * @param i
     *            The column number (0 based) of the data to be returned
     * @return The data as a string
     */
    public String getString(int i) {
        return data[i];
    }

    /**
     * Return the sample data whose column is specified as parameter. Use of
     * this method should be avoided beacause of its einneficiency
     * 
     * @param name
     *            The data colum name
     * @return The data as an integer
     */
    public int getInt(String name) {
        try {
            return Integer.parseInt(data[metadata.indexOf(name)]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data at the ith column as an integer.
     * 
     * @param i
     *            the column number (zero based) of the data to be returned
     * @return The ith columndata an an integer
     * @throws NumberFormatException
     *             if the data could not be parsed as an integer
     */
    public int getInt(int i) {
        try {
            return Integer.parseInt(data[i]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data whose column name is specified as a float. Use of
     * this method should be avoided because of it inefficiency.
     * 
     * @param name
     *            The column data name.
     * @return The data of the specified column as a long
     * @throws NumberFormatException
     *             if the data could not be parsed as a long
     */
    public long getLong(String name) {
        try {
            return Long.parseLong(data[metadata.indexOf(name)]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data at the ith column as an integer.
     * 
     * @param i
     *            The column number (zero based) of the data to be returned as
     *            an integer
     * @return The ith column data as a integer
     * @throws NumberFormatException
     *             if the data could not be parsed as an integer
     */
    public long getLong(int i) {
        try {
            return Long.parseLong(data[i]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data whose column name is specified as a float. Use of
     * this method should be avoided because of it inefficiency.
     * 
     * @param name
     *            The column data name.
     * @return The data of the specified column as a float
     * @throws NumberFormatException
     *             if the data could not be parsed as a float
     */
    public float getFloat(String name) {
        try {
            return Float.parseFloat(data[metadata.indexOf(name)]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data at the ith column as a float.
     * 
     * @param i
     *            The column number (zero based) of the data to be returned
     * @return The ith column data as a float
     * @throws NumberFormatException
     *             if the data could not be parsed as a float
     */
    public float getFloat(int i) {
        try {
            return Float.parseFloat(data[i]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data whose column name is specified as a double. Use of
     * this method should be avoided because of it inefficiency.
     * 
     * @param name
     *            The column data name.
     * @return The data of the specified column as a double
     * @throws NumberFormatException
     *             if the data could not be parsed as a double
     */
    public Double getDouble(String name) {
        try {
            return Double.valueOf(data[metadata.indexOf(name)]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data at the ith column as a double.
     * 
     * @param id
     *            The column number (zero based) of the data to be returned as a
     *            double
     * @return The ith column data as a double
     * @throws NumberFormatException
     *             if the data could not be parsed as a double
     */
    public double getDouble(int id) {
        try {
            return Double.parseDouble(data[id]);
        } catch (NumberFormatException ex) {
            throw new SampleException("Error on sample #" + row, ex);
        }
    }

    /**
     * Return the sample data whose column name is specified as a char. Use of
     * this method should be avoided because of it inefficiency.
     * 
     * @param name
     *            The column data name.
     * @return The data of the specified column as a char
     * @throws IndexOutOfBoundsException
     *             if the data could is an empty string
     */
    public char getChar(String name) {
        return data[metadata.indexOf(name)].charAt(0);
    }

    /**
     * Return the sample data at the ith column as a char.
     * 
     * @param id
     *            The column number (zero based) of the data to be returned as a
     *            char
     * @return The ith column data as a double
     * @throws NumberFormatException
     *             if the data could not be parsed as a double
     */
    public char getChar(int id) {
        return data[id].charAt(0);
    }

    /**
     * Return the sample data whose column name is specified as a boolean. Use
     * of this method should be avoided because of it inefficiency.
     * <p>
     * The boolean is assumed to be true if and only if the data string is
     * "true", in any other cases this method returns false
     * </p>
     * 
     * @param name
     *            The column data name.
     * @return The data of the specified column as a boolean
     */
    public boolean getBoolean(String name) {
        return "true".equalsIgnoreCase(data[metadata.indexOf(name)]);
    }

    /**
     * Return the sample data at the ith column as a boolean.
     * <p>
     * The boolean is assumed to be true if and only if the data string is
     * "true", in any other cases this method returns false
     * </p>
     * 
     * @param id
     *            The column number (zero based) of the data to be returned as a
     *            boolean
     * @return The ith column data as a double
     * @throws NumberFormatException
     *             if the data could not be parsed as a double
     */
    public boolean getBoolean(int id) {
        return "true".equalsIgnoreCase(data[id]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            out.append(data[i]);
            if (i < data.length - 1) {
                out.append(metadata.getSeparator());
            }
        }
        return out.toString();
    }

    /**
     * Gets the time stamp stored in the sample.
     *
     * @return the time stamp
     */
    public long getTimestamp() {
        return getLong(metadata.indexOf(CSVSaveService.TIME_STAMP));
    }

    /**
     * Gets the elapsed time stored in the sample.
     *
     * @return the elapsed time stored in the sample
     */
    public long getElapsedTime() {
        return getLong(metadata.indexOf(CSVSaveService.CSV_ELAPSED));
    }

    /**
     * <p>
     * Gets the start time of the sample.
     * </p>
     * <p>
     * Start time depends on sampleresult.timestamp.start property :
     * <ul>
     * <li>If the property is true, this method returns the time stamp stored in
     * the sample.</li>
     * <li>If the property is false, this method returns the time stamp stored
     * in the sample minus the elapsed time.</li>
     * </ul>
     * </p>
     * 
     * @return the start time
     */
    public long getStartTime() {
        return storesStartTimeStamp ? getTimestamp() : getTimestamp()
                - getElapsedTime();
    }

    /**
     * <p>
     * Gets the end time of the sample.
     * </p>
     * 
     * <p>
     * End time depends on jmeter.timestamp.start property :
     * <ul>
     * <li>If the property is true, this method returns the time stamp recorded
     * in the sample plus the elapsed time.</li>
     * <li>If the property is false, this method returns the time stamp
     * recorded.</li>
     * </ul>
     * </p>
     * 
     * @return the end time
     */
    public long getEndTime() {
        return storesStartTimeStamp ? getTimestamp() + getElapsedTime()
                : getTimestamp();
    }

    /**
     * Gets the response code stored in the sample.
     *
     * @return the response code stored in the sample
     */
    public String getResponseCode() {
        return getString(metadata.indexOf(CSVSaveService.RESPONSE_CODE));
    }

    /**
     * Gets the failure message stored in the sample.
     *
     * @return the failure message stored in the sample
     */
    public String getFailureMessage() {
        return getString(metadata.indexOf(CSVSaveService.FAILURE_MESSAGE));
    }

    /**
     * Gets the name stored in the sample.
     *
     * @return the name stored in the sample
     */
    public String getName() {
        return getString(metadata.indexOf(CSVSaveService.LABEL));
    }

    /**
     * Gets the response message stored in the sample.
     *
     * @return the response message stored in the sample
     */
    public String getResponseMessage() {
        return getString(metadata.indexOf(CSVSaveService.RESPONSE_MESSAGE));
    }

    /**
     * Gets the latency stored in the sample.
     *
     * @return the latency stored in the sample
     */
    public long getLatency() {
        return getLong(metadata.indexOf(CSVSaveService.CSV_LATENCY));
    }

    /**
     * Gets the success status stored in the sample.
     *
     * @return the success status stored in the sample
     */
    public boolean getSuccess() {
        return getBoolean(metadata.indexOf(CSVSaveService.SUCCESSFUL));
    }

    /**
     * Gets the number of sent bytes stored in the sample.
     *
     * @return the number of sent bytes stored in the sample
     */
    public int getSentBytes() {
        return getInt(metadata.indexOf(CSVSaveService.CSV_BYTES));
    }

    /**
     * Gets the number of threads in the group of this sample.
     *
     * @return the number of threads in the group of this sample
     */
    public int getGroupThreads() {
        return getInt(metadata.indexOf(CSVSaveService.CSV_THREAD_COUNT1));
    }

    /**
     * Gets the overall number of threads.
     *
     * @return the overall number of threads
     */
    public int getAllThreads() {
        return getInt(metadata.indexOf(CSVSaveService.CSV_THREAD_COUNT2));
    }

    /**
     * Gets the thread name stored in the sample.
     *
     * @return the thread name stored in the sample
     */
    public String getThreadName() {
        return getString(metadata.indexOf(CSVSaveService.THREAD_NAME));
    }

    /**
     * Checks if this sample is a controller.
     *
     * @return true, if this sample is a controller; otherwise false
     */
    public boolean isController() {
        String message = getResponseMessage();
        return message != null && message.startsWith(CONTROLLER_PATTERN);
    }
}
