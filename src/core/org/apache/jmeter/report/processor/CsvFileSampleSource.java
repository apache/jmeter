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
package org.apache.jmeter.report.processor;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.jmeter.report.core.ArgumentNullException;
import org.apache.jmeter.report.core.CsvSampleReader;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.core.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read a csv source file and write its rows (samples) all the registered
 * <code>SampleConsumer</code>s.<br>
 * If there is several other source files with the same root name then those
 * files are produced on their corresponding channels.<br>
 * 
 * The root name of the files is determined by the source file name and is made
 * of its name without the file extension :<br>
 * <b>Example:</b> If <code>results.csv</code> is the source file name then
 * <code>results</code> is the root file name.<br>
 * <br>
 * The <code>CsvFileSampleSource</code> looks for all the files in the same
 * directory of the main source file that have the same root file name<br>
 * <b>Example</b> : if the directory contains results.csv, results-1.csv,
 * results-2.csv etc. then all these files will be read and produced on their
 * corresponding channels.<br>
 * The channel on which an input file will be produce is determined by its
 * suffix<br>
 * <li>If the input file is named results-1.csv then it will be produced on the
 * channel 1.</li><br>
 * <li>If the input file is named results-2.csv then it will be produced on the
 * channel 2.</li><br>
 * <li>If the input file is named results.csv then it will be produced on the
 * channel 0.</li><br>
 * <br>
 * 
 * @since 2.14
 */
public class CsvFileSampleSource extends AbstractSampleSource {

    /** File name whose sample are being produced on the channel */
    public static final String SOURCE_FILE_ATTRIBUTE = "samplesource.file";

    private static final Logger log = LoggerFactory
	    .getLogger(CsvFileSampleSource.class);

    /** input csv files to be produced */
    private File[] inputFiles;

    /** csv readers corresponding to the input files */
    private CsvSampleReader[] csvReaders;

    /** mock producer to produce samples to its consumers */
    private PrivateProducer producer;

    /**
     * Build a sample source from the specified input file and character
     * separator.
     * 
     * @param inputFile
     *            The input sample file (CSV file)
     * @param separator
     *            The character separator to be used for delimiting samples
     *            columns
     */
    public CsvFileSampleSource(final File inputFile, final char separator) {
	if (inputFile == null)
	    throw new ArgumentNullException("inputFile");

	final String inputRootName = getFileRootName(inputFile.getName());
	final String inputExtension = getFileExtension(inputFile.getName());

	// Find secondary inputs by regex match
	File[] secondaryInputs = null;
	try {
	    final Pattern pattern = Pattern.compile(inputRootName
		    + "-[0-9]+\\." + inputExtension);
	    secondaryInputs = inputFile.getAbsoluteFile().getParentFile()
		    .listFiles(new FileFilter() {

		        @Override
		        public boolean accept(File pathname) {
			    return pathname.isFile()
			            && pattern.matcher(pathname.getName())
			                    .matches();
		        }
		    });
	} catch (PatternSyntaxException e) {
	    throw new SampleException("Could not locate input sample files !",
		    e);
	}
	inputFiles = new File[secondaryInputs.length + 1];
	csvReaders = new CsvSampleReader[secondaryInputs.length + 1];
	int k = 0;
	// primary input file (ex. input.csv)
	csvReaders[k] = new CsvSampleReader(inputFile, separator, true);
	inputFiles[k] = inputFile;
	// secondary input files (ex. input-1.csv, input-2.csv, input-3.csv)
	for (File input : secondaryInputs) {
	    k++;
	    csvReaders[k] = new CsvSampleReader(input, separator, true);
	    inputFiles[k] = secondaryInputs[k - 1];
	}
	producer = new PrivateProducer();
    }

    private static String getFileRootName(String fName) {
	int idx = fName.lastIndexOf('.');
	if (idx < 0) {
	    return fName;
	}
	fName = fName.substring(0, idx);
	return fName;
    }

    private static String getFileExtension(String fName) {
	int idx = fName.lastIndexOf('.');
	if (idx < 0) {
	    return "";
	}
	if (idx < fName.length() - 1) {
	    return fName.substring(idx + 1);
	}
	return "";
    }

    /**
     * Get the current time in milliseconds
     */
    private final long now() {
	return System.currentTimeMillis();
    }

    /**
     * Get a readable time as hours, minutes and seconds from the specified time
     * in milliseconds
     * 
     * @returns A readable string that displays the time provided as
     *          milliseconds
     */
    private final String time(long t) {
	return TimeHelper.time(t);
    }

    /**
     * Read all input CSV files and produce their samples on registered sample
     * consumers
     */
    private void produce() {
	SampleContext context = getSampleContext();
	if (context == null)
	    throw new IllegalStateException(
		    "Set a sample context before produce samples.");

	for (int i = 0; i < csvReaders.length; i++) {
	    long sampleCount = 0;
	    long start = now();
	    CsvSampleReader csvReader = csvReaders[i];
	    producer.setSampleContext(context);
	    producer.setProducedMetadata(csvReader.getMetadata(), i);
	    producer.setChannelAttribute(i, SOURCE_FILE_ATTRIBUTE,
		    inputFiles[i]);
	    producer.startProducing();
	    try {
		Sample s = null;
		while ((s = csvReader.readSample()) != null) {
		    producer.produce(s, i);
		    sampleCount++;
		}
	    } finally {
		producer.stopProducing();
		csvReader.close();
	    }
	    long time = now() - start;
	    if (log.isInfoEnabled()) {
		log.info("produce(): " + sampleCount + " samples produced in "
		        + time(time) + " on channel " + i);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSampleSource#addSampleConsumers
     * (java.util.List)
     */
    @Override
    public void setSampleConsumers(List<SampleConsumer> consumers) {
	producer.setSampleConsumers(consumers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSampleSource#addSampleConsumer
     * (org.apache.jmeter.report.processor.SampleConsumer)
     */
    @Override
    public void addSampleConsumer(SampleConsumer consumer) {
	producer.addSampleConsumer(consumer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSampleSource#removeSampleConsumer
     * (org.apache.jmeter.report.processor.SampleConsumer)
     */
    @Override
    public void removeSampleConsumer(SampleConsumer consumer) {
	producer.removeSampleConsumer(consumer);
    }

    /**
     * Run this sample source.<br>
     * This sample source will start reading all inputs CSV files and produce
     * their samples to this sample source registered sample consumers.
     */
    @Override
    public void run() {
	produce();
    }

    private class PrivateProducer extends AbstractSampleProcessor implements
	    SampleProducer {

	private List<SampleConsumer> sampleConsumers = new ArrayList<>();

	public void setSampleConsumers(List<SampleConsumer> consumers) {
	    if (consumers == null)
		throw new ArgumentNullException("consumers");

	    this.sampleConsumers = consumers;
	}

	public void addSampleConsumer(SampleConsumer consumer) {
	    if (consumer == null) {
		return;
	    }
	    this.sampleConsumers.add(consumer);
	}

	public void removeSampleConsumer(SampleConsumer consumer) {
	    if (consumer == null) {
		return;
	    }
	    this.sampleConsumers.remove(consumer);
	}

	@Override
	public void setSampleContext(SampleContext context) {
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.setSampleContext(context);
		} catch (Exception e) {
		    log.error(
			    "produce(): Consumer failed with message :"
			            + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}

	@Override
	public void setProducedMetadata(SampleMetadata metadata, int channel) {
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.setConsumedMetadata(metadata, channel);
		} catch (Exception e) {
		    log.error(
			    "setProducedMetadata(): Consumer failed with message :"
			            + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}

	@Override
	public void setChannelAttribute(int channel, String key, Object value) {
	    super.setChannelAttribute(channel, key, value);
	    // propagate to this mock producer's consumers
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.setChannelAttribute(channel, key, value);
		} catch (Exception e) {
		    log.error(
			    "setChannelAttribute(): Consumer failed with message :"
			            + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}

	@Override
	public void startProducing() {
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.startConsuming();
		} catch (Exception e) {
		    log.error(
			    "startProducing(): Consumer failed with message :"
			            + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}

	@Override
	public void produce(Sample s, int channel) {
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.consume(s, channel);
		} catch (Exception e) {
		    log.error(
			    "produce(): Consumer failed with message :"
			            + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}

	@Override
	public void stopProducing() {
	    for (SampleConsumer consumer : this.sampleConsumers) {
		try {
		    consumer.stopConsuming();
		} catch (Exception e) {
		    log.error("stopProducing(): Consumer failed with message :"
			    + e.getMessage(), e);
		    throw new SampleException(e);
		}
	    }
	}
    }

}
