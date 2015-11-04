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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.save.CSVSaveService;

/**
 * Reader class for reading CSV files.<reader>
 * <p>
 * Handles {@link SampleMetadata} reading and sample extraction.
 * </p>
 * 
 * @since 2.14
 */
public class CsvSampleReader {

    private static final int BUF_SIZE = 10000;

    private static final String CHARSET = "ISO8859-1";

    private BufferedReader reader;

    private char separator;

    private long row;

    private SampleMetadata metadata;

    private int columnCount;

    private Sample lastSampleRead;

    public CsvSampleReader(File inputFile, char separator) {
	assertFile(inputFile);
	BufferedReader reader = createReader(inputFile);
	init(reader, readMetadata(reader, separator));
    }

    public CsvSampleReader(File inputFile, SampleMetadata metadata) {
	assertFile(inputFile);
	if (metadata == null)
	    throw new ArgumentNullException("metadata");

	init(createReader(inputFile), metadata);
    }

    private static void assertFile(File file) {
	if (file == null)
	    throw new ArgumentNullException("inputFile");

	if (!file.isFile())
	    throw new IllegalArgumentException(
		    file.getAbsolutePath()
		            + " does not exist or is not a file ! Please provide an existing input file.");
    }

    private static BufferedReader createReader(File file) {
	Reader reader = null;
	try {
	    reader = new InputStreamReader(new FileInputStream(file), CHARSET);
	} catch (Exception e) {
	    throw new RuntimeException("Could not create file reader !", e);
	}
	return new BufferedReader(reader, BUF_SIZE);
    }

    private static SampleMetadata readMetadata(BufferedReader reader,
	    char separator) {
	try {
	    return new SampleMetaDataParser(separator).parse(reader.readLine());
	} catch (Exception e) {
	    throw new SampleException("Could not read metadata !", e);
	}
	
//	String line = reader.readLine();
//        if (line == null) {
//            throw new IOException(filename + ": unable to read header line");
//        }
//        long lineNumber = 1;
//        SampleSaveConfiguration saveConfig = CSVSaveService
//                .getSampleSaveConfiguration(line, filename);
//        if (saveConfig == null) {// not a valid header
//            log.info(filename
//                    + " does not appear to have a valid header. Using default configuration.");
//            saveConfig = (SampleSaveConfiguration) resultCollector
//                    .getSaveConfig().clone(); // may change the format later
//            dataReader.reset(); // restart from beginning
//            lineNumber = 0;
//        }
    }

    private void init(BufferedReader reader, SampleMetadata metadata) {
	this.reader = reader;
	this.metadata = metadata;
	this.columnCount = metadata.getColumnCount();
	this.separator = metadata.getSeparator();
	this.row = 0;
	lastSampleRead = nextSample();
    }

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
		    for (int i = data.length; i < columnCount; i++)
			filler[i] = "";
		    data = filler;
		}
		sample = new Sample(row, metadata, data);
	    }
	    return sample;
	} catch (IOException e) {
	    throw new SampleException("Could not read sample <" + row + ">", e);
	}
    }

    public Sample readSample() {
	Sample out = lastSampleRead;
	lastSampleRead = nextSample();
	return out;
    }

    public Sample peek() {
	return lastSampleRead;
    }

    public boolean hasNext() {
	return lastSampleRead != null;
    }

    public void close() {
	try {
	    reader.close();
	} catch (Exception e) {
	    // ignore
	}
    }
}
