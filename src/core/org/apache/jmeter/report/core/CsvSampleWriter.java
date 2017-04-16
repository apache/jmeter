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

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.Validate;
import org.apache.jmeter.save.CSVSaveService;

/**
 * Class to be used to write samples to a csv destination (OutputStream, Writer
 * or a File).
 * <p>
 * This class handles csv header writing with the <code>writeHeader</code>
 * method. This method has to be called by user for the header to be written.
 * </p>
 * 
 * @since 3.0
 */
public class CsvSampleWriter extends AbstractSampleWriter {

    /** The number of columns for each row */
    private int columnCount;

    /** The separator to be used in between data on each row */
    private char separator;

    /** Description of the columns */
    private SampleMetadata metadata;

    /** Number of samples written */
    private long sampleCount;

    /**
     * Constructor for a CsvSampleWriter.<br>
     * The newly created instance has to be supplied with a Writer to work
     * properly.
     * 
     * @param metadata
     *            the description for data that this writer will write. (
     *            {@code metadata} must not be {@code null}.)
     */
    public CsvSampleWriter(SampleMetadata metadata) {
        super();
        this.metadata = metadata;
        this.columnCount = metadata.getColumnCount();
        this.separator = metadata.getSeparator();
        this.sampleCount = 0;
    }

    /**
     * Constructor for a CsvSampleWriter.
     * 
     * @param output
     *            the writer to write data to. (Must not be {@code null})
     * @param metadata
     *            the description for data that this writer will write. (
     *            {@code metadata} must not be {@code null}.)
     */
    public CsvSampleWriter(Writer output, SampleMetadata metadata) {
        this(metadata);
        setWriter(output);
    }

    /**
     * Constructor for a CsvSampleWriter.
     * 
     * @param output
     *            the output stream to write data to. (Must not be {@code null})
     * @param metadata
     *            the description for data that this writer will write. (
     *            {@code metadata} must not be {@code null}.)
     */
    public CsvSampleWriter(OutputStream output, SampleMetadata metadata) {
        this(metadata);
        setOutputStream(output);
    }

    /**
     * Constructor for a CsvSampleWriter.
     * 
     * @param output
     *            the output file to write data to. (Must not be {@code null})
     * @param metadata
     *            the description for data that this writer will write. (
     *            {@code metadata} must not be {@code null}.)
     */
    public CsvSampleWriter(File output, SampleMetadata metadata) {
        this(metadata);
        setOutputFile(output);
    }

    /**
     * Set the char to use for separation of data in a line.
     * 
     * @param separator
     *            to use
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    private void reset() {
        sampleCount = 0;
    }

    @Override
    public void setWriter(Writer writer) {
        super.setWriter(writer);
        reset();
    }

    /**
     * Write the csv header. If samples have already been written then a row with
     * header information will be written in the middle of the file.
     */
    public void writeHeader() {
        Validate.validState(writer != null, "No writer set! Call setWriter() first!");
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            row.append(metadata.getColumnName(i));
            if (i < columnCount - 1) {
                row.append(separator);
            }
        }
        writer.println(row.toString());
    }

    @Override
    public long write(Sample sample) {
        Validate.validState(writer != null, "No writer set! Call setWriter() first!");
        StringBuilder row = new StringBuilder();
        char[] specials = new char[] { separator,
                CSVSaveService.QUOTING_CHAR, CharUtils.CR, CharUtils.LF };
        for (int i = 0; i < columnCount; i++) {
            String data = sample.getData(i);
            row.append(CSVSaveService.quoteDelimiters(data, specials))
                    .append(separator);
        }
        row.setLength(row.length() - 1);
        writer.println(row.toString());
        sampleCount++;

        return sampleCount;
    }

}
