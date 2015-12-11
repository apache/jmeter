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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
//import org.apache.jmeter.samplers.SampleResult;
//import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Reader class for reading CSV files.<reader>
 * <p>
 * Handles {@link SampleMetadata} reading and sample extraction.
 * </p>
 * 
 * @since 2.14
 */
public class CsvSampleReader implements Closeable{

    private static final int BUF_SIZE = 10000;

    private static final String CHARSET = "ISO8859-1";

    private File file;

    private BufferedReader reader;

    private char separator;

    private long row;

    private SampleMetadata metadata;

    private int columnCount;

    private Sample lastSampleRead;

    /**
     * Instantiates a new csv sample reader.
     *
     * @param inputFile
     *            the input file
     * @param separator
     *            the separator
     * @param useSaveSampleCfg
     *            indicates whether the reader uses jmeter
     *            SampleSaveConfiguration to define metadata
     */
    public CsvSampleReader(File inputFile, char separator, boolean useSaveSampleCfg) {
        this(inputFile, null, separator, useSaveSampleCfg);
    }

    /**
     * Instantiates a new csv sample reader.
     *
     * @param inputFile
     *            the input file
     * @param metadata
     *            the metadata
     */
    public CsvSampleReader(File inputFile, SampleMetadata metadata) {
        this(inputFile, metadata, null, false);
    }

    private CsvSampleReader(File inputFile, SampleMetadata metadata,
            Character separator, boolean useSaveSampleCfg) {
        if (inputFile == null) {
            throw new ArgumentNullException("inputFile");
        }

        if (inputFile.isFile() == false || inputFile.canRead() == false) {
            throw new IllegalArgumentException(inputFile.getAbsolutePath()
                    + "does not exist or is not readable");
        }
        this.file = inputFile;
        try {
            this.reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), CHARSET), BUF_SIZE);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            throw new SampleException("Could not create file reader !", ex);
        }
        if (metadata == null) {
            metadata = readMetadata(separator, useSaveSampleCfg);
        }
        this.metadata = metadata;
        this.columnCount = metadata.getColumnCount();
        this.separator = metadata.getSeparator();
        this.row = 0;
        this.lastSampleRead = nextSample();
    }

    private SampleMetadata readMetadata(char separator, boolean useSaveSampleCfg) {
        try {
            SampleMetadata metadata;
            // Read first line
            String line = reader.readLine();

            // When we can use sample save config and there is no header in csv
            // file
            if (useSaveSampleCfg
                    && CSVSaveService.getSampleSaveConfiguration(line,
                            file.getAbsolutePath()) == null) {
                // Build metadata from default save config
                metadata = new SampleMetadata(
                        SampleSaveConfiguration.staticConfig());

            } else {
                // Build metadata from headers
                metadata = new SampleMetaDataParser(separator).parse(line);
            }
            return metadata;
        } catch (Exception e) {
            throw new SampleException("Could not read metadata !", e);
        }
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public SampleMetadata getMetadata() {
        return metadata;
    }

    private Sample nextSample() {
        String[] data;
        try {
            data = CSVSaveService.csvReadFile(reader, separator);
            Sample sample = null;
            if (data.length > 0) {
                // TODO is it correct to use a filler ?
                if (data.length < columnCount) {
                    String[] filler = new String[columnCount];
                    System.arraycopy(data, 0, filler, 0, data.length);
                    for (int i = data.length; i < columnCount; i++) {
                        filler[i] = "";
                    }
                    data = filler;
                }
                sample = new Sample(row, metadata, data);
            }
            return sample;
        } catch (IOException e) {
            throw new SampleException("Could not read sample <" + row + ">", e);
        }
    }

    /**
     * Gets next sample from the file.
     *
     * @return the sample
     */
    public Sample readSample() {
        Sample out = lastSampleRead;
        lastSampleRead = nextSample();
        return out;
    }

    /**
     * Gets next sample from file but keep the reading file position.
     *
     * @return the sample
     */
    public Sample peek() {
        return lastSampleRead;
    }

    /**
     * Indicates whether the file contains more samples
     *
     * @return true, if the file contains more samples
     */
    public boolean hasNext() {
        return lastSampleRead != null;
    }

    /**
     * Close the reader.
     */
    @Override
    public void close() {
	JOrphanUtils.closeQuietly(reader);
    }
}
