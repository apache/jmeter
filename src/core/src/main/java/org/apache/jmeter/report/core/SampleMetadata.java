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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;

/**
 * <p>
 * Describe samples structure by holding sample column names and theirs matching
 * indexes.
 * </p>
 * <br>
 * 
 * @since 3.0
 */
public class SampleMetadata {

    private static final String METADATA_EXCEPTION_MSG_FMT = "No column <%s> found in sample metadata <%s>,"
            + " check #jmeter.save.saveservice.* properties to add the missing column";

    /** The column list : accessed by CSVSampleWriter */
    List<String> columns;

    /** Index to map column names to their corresponding indexes */
    private TreeMap<String, Integer> index = new TreeMap<>();

    /** character separator used for separating columns */
    private char separator;

    /**
     * Builds metadata from separator character and a list of column names
     * 
     * @param separator
     *            The character used for column separation
     * @param columns
     *            The list of columns names (must not be {@code null})
     */
    public SampleMetadata(char separator, String... columns) {
        Validate.notNull(columns, "columns must not be null");
        initialize(separator, Arrays.asList(columns));
    }

    /**
     * Construct SampleMetaData from {@link SampleSaveConfiguration}.
     *
     * @param saveConfig
     *            config from which metadata gets extracted (must not be
     *            {@code null})
     */
    public SampleMetadata(SampleSaveConfiguration saveConfig) {
        List<String> configuredColumns = new ArrayList<>();
        if (saveConfig.saveTimestamp()) {
            configuredColumns.add(CSVSaveService.TIME_STAMP);
        }
        if (saveConfig.saveTime()) {
            configuredColumns.add(CSVSaveService.CSV_ELAPSED);
        }
        if (saveConfig.saveLabel()) {
            configuredColumns.add(CSVSaveService.LABEL);
        }
        if (saveConfig.saveCode()) {
            configuredColumns.add(CSVSaveService.RESPONSE_CODE);
        }
        if (saveConfig.saveMessage()) {
            configuredColumns.add(CSVSaveService.RESPONSE_MESSAGE);
        }
        if (saveConfig.saveThreadName()) {
            configuredColumns.add(CSVSaveService.THREAD_NAME);
        }
        if (saveConfig.saveDataType()) {
            configuredColumns.add(CSVSaveService.DATA_TYPE);
        }
        if (saveConfig.saveSuccess()) {
            configuredColumns.add(CSVSaveService.SUCCESSFUL);
        }
        if (saveConfig.saveAssertionResultsFailureMessage()) {
            configuredColumns.add(CSVSaveService.FAILURE_MESSAGE);
        }
        if (saveConfig.saveBytes()) {
            configuredColumns.add(CSVSaveService.CSV_BYTES);
        }
        if (saveConfig.saveSentBytes()) {
            configuredColumns.add(CSVSaveService.CSV_SENT_BYTES);
        }
        if (saveConfig.saveThreadCounts()) {
            configuredColumns.add(CSVSaveService.CSV_THREAD_COUNT1);
            configuredColumns.add(CSVSaveService.CSV_THREAD_COUNT2);
        }
        if (saveConfig.saveUrl()) {
            configuredColumns.add(CSVSaveService.CSV_URL);
        }
        if (saveConfig.saveFileName()) {
            configuredColumns.add(CSVSaveService.CSV_FILENAME);
        }
        if (saveConfig.saveLatency()) {
            configuredColumns.add(CSVSaveService.CSV_LATENCY);
        }
        if (saveConfig.saveEncoding()) {
            configuredColumns.add(CSVSaveService.CSV_ENCODING);
        }
        if (saveConfig.saveSampleCount()) {
            configuredColumns.add(CSVSaveService.CSV_SAMPLE_COUNT);
            configuredColumns.add(CSVSaveService.CSV_ERROR_COUNT);
        }
        if (saveConfig.saveHostname()) {
            configuredColumns.add(CSVSaveService.CSV_HOSTNAME);
        }
        if (saveConfig.saveIdleTime()) {
            configuredColumns.add(CSVSaveService.CSV_IDLETIME);
        }
        if (saveConfig.saveConnectTime()) {
            configuredColumns.add(CSVSaveService.CSV_CONNECT_TIME);
        }
        initialize(saveConfig.getDelimiter().charAt(0), configuredColumns);
    }

    private void initialize(char separator, List<String> columns) {
        this.separator = separator;
        this.columns = columns;
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            index.put(this.columns.get(i).trim(), Integer.valueOf(i));
        }
    }

    /**
     * @return the character used for separating columns
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @return the number of columns in the metadata
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Gets the name of the ith column in this metadata
     * 
     * @param i
     *            The index of the column for which the name is requested (zero
     *            based)
     * @return The column name of the ith column
     * @throws IndexOutOfBoundsException
     *             If the requested columln does not exist (&lt; 0 or &gt;
     *             <code>getColumnCount()</code>)
     */
    public String getColumnName(int i) {
        return columns.get(i);
    }

    /**
     * Gets the name of the ith column in this metadata
     * 
     * @param i
     *            The index of the column for which the name is requested (zero
     *            based)
     * @return The column name of the ith column
     * @throws IndexOutOfBoundsException
     *             If the requested columln does not exist (&lt; 0 or &gt;
     *             <code>getColumnCount()</code>)
     */
    public String getColumnName(Integer i) {
        return columns.get(i.intValue());
    }

    /**
     * Returns the index of the column with the specified name.
     * 
     * @param col
     *            the column name for which the index is requested
     * @return The index of the requested column or -1 if the requested column
     *         does not exist in this metadata
     */
    public int indexOf(String col) {
        Integer out = index.get(col);
        return out == null ? -1 : out.intValue();
    }

    /**
     * Returns the index of the column with the specified name.
     * 
     * @param col
     *            the column name for which the index is requested
     * @return The index of the requested column
     * @throws SampleException
     *             when the column with the specified name is not found
     */
    public int ensureIndexOf(String col) {
        int index = indexOf(col);
        if (index < 0) {
            throw new SampleException(
                    String.format(METADATA_EXCEPTION_MSG_FMT, col, toString()));
        }
        return index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringUtils.join(columns, separator);
    }
}
