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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader class for reading CSV files.
 * <p>
 * Handles {@link SampleMetadata} reading and sample extraction.
 * </p>
 * 
 * @since 3.0
 */
public class CsvSampleReader implements Closeable{

    private static final Logger log = LoggerFactory.getLogger(CsvSampleReader.class);
    private static final int BUF_SIZE = 1024 * 1024;

    private static final String CHARSET = SaveService.getFileEncoding(StandardCharsets.UTF_8.displayName());

    private static final char DEFAULT_SEPARATOR =
            // We cannot use JMeterUtils#getPropDefault as it applies a trim on value
            JMeterUtils.getDelimiter(
                    JMeterUtils.getJMeterProperties().getProperty(SampleSaveConfiguration.DEFAULT_DELIMITER_PROP, SampleSaveConfiguration.DEFAULT_DELIMITER)).charAt(0);

    private File file;

    private InputStream fis;
    private Reader isr;
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
     *            the input file (must not be {@code null})
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
     *            the input file (must not be {@code null})
     * @param metadata
     *            the metadata
     */
    public CsvSampleReader(File inputFile, SampleMetadata metadata) {
        this(inputFile, metadata, DEFAULT_SEPARATOR, false);
    }

    private CsvSampleReader(File inputFile, SampleMetadata metadata,
            char separator, boolean useSaveSampleCfg) {
        if (!(inputFile.isFile() && inputFile.canRead())) {
            throw new IllegalArgumentException(inputFile.getAbsolutePath()
                    + " does not exist or is not readable");
        }
        this.file = inputFile;
        try {
            this.fis = new FileInputStream(file); 
            this.isr = new InputStreamReader(fis, CHARSET);
            this.reader = new BufferedReader(isr, BUF_SIZE);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            JOrphanUtils.closeQuietly(isr);
            JOrphanUtils.closeQuietly(fis);
            JOrphanUtils.closeQuietly(this.reader);
            throw new SampleException("Could not create file reader !", ex);
        }

        if (metadata == null) {
            this.metadata = readMetadata(separator, useSaveSampleCfg);
        } else {
            this.metadata = metadata;
        }
        this.columnCount = this.metadata.getColumnCount();
        this.separator = this.metadata.getSeparator();
        this.row = 0;
        this.lastSampleRead = nextSample();
    }

    private SampleMetadata readMetadata(char separator, boolean useSaveSampleCfg) {
        try {
            SampleMetadata result;
            // Read first line
            String line = reader.readLine();
            if(line == null) {
                throw new IllegalArgumentException("File is empty");
            }
            // When we can use sample save config and there is no header in csv
            // file
            if (useSaveSampleCfg
                    && CSVSaveService.getSampleSaveConfiguration(line,
                            file.getAbsolutePath()) == null) {
                // Build metadata from default save config
                if (log.isWarnEnabled()) {
                    log.warn(
                            "File '{}' does not contain the field names header, "
                                    + "ensure the jmeter.save.saveservice.* properties are the same as when the CSV file was created or the file may be read incorrectly",
                            file.getAbsolutePath());
                }
                System.err.println("File '"+file.getAbsolutePath()+"' does not contain the field names header, "
                        + "ensure the jmeter.save.saveservice.* properties are the same as when the CSV file was created or the file may be read incorrectly");
                result = new SampleMetadata(
                        SampleSaveConfiguration.staticConfig());

            } else {
                // Build metadata from headers
                result = new SampleMetaDataParser(separator).parse(line);
            }
            return result;
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
                if (data.length != columnCount) {
                    throw new SampleException("Mismatch between expected number of columns:"+columnCount+" and columns in CSV file:"+data.length+
                            ", check your jmeter.save.saveservice.* configuration");
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
        JOrphanUtils.closeQuietly(isr);
        JOrphanUtils.closeQuietly(fis);
        JOrphanUtils.closeQuietly(reader);
    }
}
