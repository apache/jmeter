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
 * Reader class for reading CSV files.<br>
 * <p>
 * Handles {@link SampleMetadata} reading and sample extraction.
 * </p>
 * 
 * @since 2.14
 */
public class CsvSampleReader {

    private static final int BUF_SIZE = 10000;

    private static final String CHARSET = "ISO8859-1";

    private BufferedReader br;

    private SampleMetaDataParser parser;

    private char separator;

    // private String sep;

    private long row;

    private SampleMetadata metadata;

    private int columnCount;

    /*
     * The csv file to read. Can be null if the reader has been initialized from
     * a stream or a reader
     */
    private File inputFile;

    private Sample lastSampleRead;

    public CsvSampleReader(Reader in, char separator) {
	init(separator, in);
    }

    public CsvSampleReader(Reader in, SampleMetadata metadata) {
	init(metadata, in);
    }

    public CsvSampleReader(File in, char separator) {
	Reader r = null;
	try {
	    r = new InputStreamReader(new FileInputStream(in), CHARSET);
	} catch (Exception e) {
	    throw new RuntimeException("Could not create file reader !", e);
	}
	this.inputFile = in;
	init(separator, r);
    }

    public CsvSampleReader(File inputFile, SampleMetadata metadata) {
	if (inputFile == null)
	    throw new ArgumentNullException("inputFile");

	if (!inputFile.isFile()) {
	    throw new IllegalArgumentException(
		    inputFile.getAbsolutePath()
		            + " does not exist or is not a file ! Please provide an existing input file.");
	}
	if (metadata == null)
	    throw new ArgumentNullException("metadata");

	Reader r = null;
	try {
	    r = new InputStreamReader(new FileInputStream(inputFile), CHARSET);
	} catch (Exception e) {
	    throw new RuntimeException("Could not create file reader !", e);
	}
	this.inputFile = inputFile;
	init(metadata, r);
    }

    public CsvSampleReader(CsvFile inputFile) {
	if (inputFile == null)
	    throw new ArgumentNullException("inputFile");

	if (!inputFile.isFile()) {
	    throw new IllegalArgumentException(
		    inputFile.getAbsolutePath()
		            + " does not exist or is not a file ! Please provide an existing input file.");
	}
	Reader r = null;
	try {
	    r = new InputStreamReader(new FileInputStream(inputFile), CHARSET);
	} catch (Exception e) {
	    throw new RuntimeException("Could not create file reader !", e);
	}
	this.inputFile = inputFile;
	init(inputFile.getSeparator(), r);
    }

    private void init(SampleMetadata metadata, Reader in) {
	if (metadata == null)
	    throw new ArgumentNullException("metadata");

	this.br = new BufferedReader(in, BUF_SIZE);
	this.metadata = metadata;
	this.columnCount = metadata.getColumnCount();
	this.row = 0;
	this.separator = metadata.getSeparator();
	// this.sep = Character.toString(separator);
	lastSampleRead = nextSample();
    }

    private void init(char separator, Reader in) {
	this.br = new BufferedReader(in, BUF_SIZE);
	this.parser = new SampleMetaDataParser(separator);
	this.metadata = getMetadata();
	this.separator = separator;
	// this.sep = Character.toString(separator);
	this.row = 0;
	lastSampleRead = nextSample();
    }

    /**
     * Get the CVS file size in bytes. If the reader has not been initialized
     * from a file then the size is unknown and -1 will be returned
     * 
     * @return The CVS file size in bytes or -1 if size could be determined
     */
    public long getSize() {
	if (inputFile == null) {
	    return -1;
	}
	return inputFile.length();
    }

    public SampleMetadata getMetadata() {
	if (metadata == null) {
	    try {
		String line = br.readLine();
		metadata = parser.parse(line);
		columnCount = metadata.getColumnCount();
	    } catch (Exception e) {
		throw new SampleException("Could not read metadata !", e);
	    }
	}
	return metadata;
    }

    private Sample nextSample() {
	String[] data;
	try {
	    data = CSVSaveService.csvReadFile(br, separator);
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

	// Sample out = null;
	// String line = null;
	// try {
	// line = br.readLine();
	// if (line == null) {
	// return null;
	// }
	// int size = line.length();
	// int p = 0;
	// int pp = p;
	// String[] data = new String[columnCount];
	// for (int i = 0; i < columnCount; i++) {
	// p = line.indexOf(separator, pp);
	// if (p < 0) {
	// data[i] = line.substring(pp);
	// while (++i < columnCount) {
	// data[i] = "";
	// }
	// break;
	// } else {
	// data[i] = line.substring(pp, p);
	// pp = p + 1;
	// if (pp >= size) {
	// while (++i < columnCount) {
	// data[i] = "";
	// }
	// break;
	// }
	// }
	// }
	// out = new Sample(row, metadata, data);
	// row++;
	// } catch (Exception e) {
	// throw new SampleException("Could not read sample <" + line +
	// ">, expected " + columnCount + " columns", e);
	// }
	// return out;
    }

    // private Sample nextSampleST() {
    // Sample out = null;
    // try {
    // String line = br.readLine();
    // if (line == null) {
    // return null;
    // }
    // StringTokenizer tokenizer = new StringTokenizer(line, sep);
    // String[] data = new String[columnCount];
    // for (int i = 0; i < columnCount; i++) {
    // try {
    // data[i] = tokenizer.nextToken();
    // } catch (Exception e) {
    // data[i] = "";
    // }
    // }
    // out = new Sample(row, metadata, data);
    // row++;
    // } catch (Exception e) {
    // throw new SampleException("Could not read sample !", e);
    // }
    // return out;
    // }

    // private Sample nextSamplePOS() {
    // Sample out = null;
    // String line = null;
    // try {
    // line = br.readLine();
    // if (line == null) {
    // return null;
    // }
    // int size = line.length();
    // int p = 0;
    // int pp = p;
    // String[] data = new String[columnCount];
    // for (int i = 0; i < columnCount; i++) {
    // p = line.indexOf(separator, pp);
    // if (p < 0) {
    // data[i] = line.substring(pp);
    // while (++i < columnCount) {
    // data[i] = "";
    // }
    // break;
    // } else {
    // data[i] = line.substring(pp, p);
    // pp = p + 1;
    // if (pp >= size) {
    // while (++i < columnCount) {
    // data[i] = "";
    // }
    // break;
    // }
    // }
    // }
    // out = new Sample(row, metadata, data);
    // row++;
    // } catch (Exception e) {
    // throw new SampleException("Could not read sample <" + line +
    // ">, expected " + columnCount + " columns", e);
    // }
    // return out;
    // }

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
	    br.close();
	} catch (Exception e) {
	    // ignore
	}
    }

    /**
     * Read the first line of the specified input file and build sample metadata
     * from this line.<br>
     * <p>
     * The fiel is immediately closed after the first lien has been read
     * </p>
     * 
     * @param input
     *            The input csv file
     * @return The CSV metadata (first line with column names)
     */
    public static SampleMetadata getSampleMetaData(CsvFile input) {
	CsvSampleReader csvReader = new CsvSampleReader(input);
	try {
	    return csvReader.getMetadata();
	} finally {
	    csvReader.close();
	}
    }

    /**
     * Read the first line of the specified input file and build sample metadata
     * from this line.<br>
     * <p>
     * The fiel is immediately closed after the first lien has been read
     * </p>
     * 
     * @param input
     *            The input csv file
     * @param separator
     *            The separator to be used for columns splitting
     * @return The CSV metadata (first line with column names)
     */
    public static SampleMetadata getSampleMetaData(File input, char separator) {
	return getSampleMetaData(new CsvFile(input.getParent(),
	        input.getName(), separator));
    }

}
