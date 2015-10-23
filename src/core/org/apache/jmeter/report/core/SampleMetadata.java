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

import java.util.TreeMap;

/**
 * <p>
 * Describe samples structure by holding sample column names and theirs matching
 * indexes.
 * </p>
 * <br>
 * 
 * @since 2.14
 */
public class SampleMetadata {

    public static final String JMETER_TIMESTAMP = "timeStamp";

    public static final String JMETER_ELAPSED = "elapsed";

    public static final String JMETER_SAMPLE_NAME = "label";

    public static final String JMETER_RESPONSE_CODE = "responseCode";

    public static final String JMETER_RESPONSE_MESSAGE = "responseMessage";

    public static final String JMETER_THREAD_NAME = "threadName";

    public static final String JMETER_SUCCESS = "success";

    public static final String JMETER_BYTES = "bytes";

    public static final String JMETER_THREAD_GROUP = "grpThreads";

    public static final String JMETER_THREAD_COUNT = "allThreads";

    public static final String JMETER_LATENCY = "Latency";

    public static final String JMETER_SAMPLE_COUNT = "SampleCount";

    public static final String JMETER_ERROR_COUNT = "ErrorCount";

    public static final String JMETER_HOSTNAME = "Hostname";

    public static final String JMETER_JVM_ID = "jvmId";

    /** The column list : accessed by CSVSampleWriter */
    String[] column;

    /** Index to map column names to their corresponding indexes */
    private TreeMap<String, Integer> index;

    /** character separator used for separating columns */
    private char separator;

    /**
     * Builds metadata from separator character and a list of column names
     * 
     * @param separator
     *            The character used for column separation
     * @param columns
     *            The list of columns names
     */
    public SampleMetadata(char separator, String... columns) {
	if (columns == null) {
	    throw new ArgumentNullException("columns");
	}
	this.separator = separator;
	this.column = columns;
	index = new TreeMap<String, Integer>();
	for (int i = 0; i < columns.length; i++) {
	    index.put(columns[i].trim(), i);
	}
    }

    /**
     * Gets the character used for separating columns
     */
    public char getSeparator() {
	return separator;
    }

    /**
     * Returns the number of columns in the metadata
     */
    public int getColumnCount() {
	return column.length;
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
	return column[i];
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
	return column[i.intValue()];
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuffer out = new StringBuffer();
	for (int i = 0; i < column.length; i++) {
	    out.append(column[i]);
	    if (i < column.length - 1) {
		out.append(separator);
	    }
	}
	return out.toString();
    }

}
