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
import org.apache.jmeter.report.core.AbstractSampleWriter;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.save.CSVSaveService;

/**
 * Class to be used to write samples to a csv destination (OutputStream, Writer
 * or a File).
 * <p>
 * This class handles csv header writting with the <code>writeHeader</code>
 * method. This method has to be called by user for the header to be written.
 * </p>
 * 
 * @since 2.14
 */
public class CsvSampleWriter extends AbstractSampleWriter {

    private int columnCount;

    private char separator;

    private SampleMetadata metadata;

    private StringBuilder row = new StringBuilder();

    private long sampleCount;

    public CsvSampleWriter(SampleMetadata metadata) {
        if (metadata == null) {
            throw new ArgumentNullException("metadata");
        }
        this.metadata = metadata;
        this.columnCount = metadata.getColumnCount();
        this.separator = metadata.getSeparator();
        this.sampleCount = 0;
    }

    public CsvSampleWriter(Writer output, SampleMetadata metadata) {
        this(metadata);
        if (output == null) {
            throw new ArgumentNullException("output");
        }
        setWriter(output);
    }

    public CsvSampleWriter(OutputStream output, SampleMetadata metadata) {
        this(metadata);
        if (output == null) {
            throw new ArgumentNullException("output");
        }
        setOutputStream(output);
    }

    public CsvSampleWriter(File output, SampleMetadata metadata) {
        this(metadata);
        if (output == null) {
            throw new ArgumentNullException("output");
        }
        setOutputFile(output);
    }

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
     * Write the csv header. If samples have alredy been written then a row with
     * header information will be written in the middle of the file.
     */
    public void writeHeader() {
        row.setLength(0);
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
        if (sample == null) {
            throw new ArgumentNullException("sample");
        }
        if (writer == null) {
            throw new IllegalStateException(
                    "No writer set! Call setWriter() first!");
        }

        row.setLength(0);
        char[] specials = new char[] { separator,
                CSVSaveService.QUOTING_CHAR, CharUtils.CR, CharUtils.LF };
        for (int i = 0; i < columnCount; i++) {
            String data = sample.getString(i);
            row.append(CSVSaveService.quoteDelimiters(data, specials))
                    .append(separator);
        }
        int rowLength = row.length() - 1;
        row.setLength(rowLength);
        writer.println(row.toString());
        sampleCount++;

        return sampleCount;
    }

}
