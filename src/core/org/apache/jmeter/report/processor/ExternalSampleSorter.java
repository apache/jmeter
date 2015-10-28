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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.report.core.CsvFile;
import org.apache.jmeter.report.core.CsvSampleReader;
import org.apache.jmeter.report.core.CsvSampleWriter;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleComparator;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.SampleProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R-way external sample sorter.<br>
 * <p>
 * This SampleConsumer should be used to sort samples base on a
 * {@link SampleComparator}
 * </p>
 * <p>
 * Samples are sorted with the external sort algorithm. Thus, samples are not
 * all stored in memory to be sorted. Instead, they are sorted by chunk in
 * memory and then written to the disk before being merged at the end.<br>
 * </p>
 * <p>
 * This sorter makes it possible to sort any number of samples with a fixed
 * amount of memory. Hard disk will be used instead of RAM, at the cost of
 * performance
 * </p>
 * <p>
 * When <b>parallel mode</b> is enabled and several CPU are available to the
 * JVM, this sorter uses 2 CPU to reduce sort time.<br>
 * The <b>parallel mode</b> can be disabled if some sort of concurrency issue is
 * encountered.
 * </p>
 * <p>
 * As a lest note, this {@link SampleConsumer} can be used as normal class with
 * the differences <code>sort()</code> methods
 * </p>
 * <p>
 * It is important to set the <b><code>chunkSize</code></b> property according
 * to the available memory as the algorithm does not take care of memory
 * allocation (samples sizes are not predictable)
 * </p>
 * <p>
 * Meanwhile, it is equally important to set a {@link SampleComparator} to
 * define sample ordering </>
 * 
 * @since 2.14
 */
public class ExternalSampleSorter extends AbstractSampleConsumer {

    private static final Logger log = LoggerFactory
	    .getLogger(ExternalSampleSorter.class);

    private static final int DEFAULT_CHUNK_SIZE = 50000;

    private long chunkSize = DEFAULT_CHUNK_SIZE;

    private SampleComparator sampleComparator;

    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

    private ThreadPoolExecutor pool;

    private volatile int nbProcessors;

    private boolean parallelize;

    private volatile long chunkedSampleCount;

    private volatile long inputSampleCount;

    private LinkedList<File> chunks;

    private LinkedList<Sample> samples;

    private SampleMetadata sampleMetadata;

    private boolean revertedSort;

    public ExternalSampleSorter() {
	chunkSize = DEFAULT_CHUNK_SIZE;
	this.nbProcessors = Runtime.getRuntime().availableProcessors();
	this.parallelize = nbProcessors > 1;
	this.pool = new ThreadPoolExecutor(nbProcessors, nbProcessors + 5, 10,
	        TimeUnit.SECONDS, workQueue);
	setRevertedSort(false);
    }

    public ExternalSampleSorter(SampleComparator comparator) {
	this();
	setSampleComparator(comparator);
    }

    /**
     * Set the number of samples that will be stored in memory. This as well
     * define the number of samples that will be written in each chunk file
     * before merging step
     * 
     * @param chunkSize
     *            The number of sample sorted in memory before they are written
     *            to disk. 5000 is the minimum and will be averwritten is
     *            provided chunkSize is <5000
     */
    public void setChunkSize(long chunkSize) {
	if (chunkSize < 50000) {
	    chunkSize = 50000;
	}
	this.chunkSize = chunkSize;
    }

    /**
     * Set the sample comparator that will define sample ordering
     */
    public void setSampleComparator(SampleComparator sampleComparator) {
	this.sampleComparator = sampleComparator;
    }

    /**
     * Enabled parallel mode
     * 
     * @param parallelize
     *            true to enable, false to disable
     */
    public void setParallelize(boolean parallelize) {
	this.parallelize = parallelize;
    }

    public boolean isParallelize() {
	return parallelize;
    }

    /**
     * Sort an input CSV file to an output sorted CSV file.<br>
     * <p>
     * The input CSV <b>must</b> have a header otherwise the sorted will give
     * unprectable results
     * </p>
     * 
     * @param inputFile
     *            The CSV file to be sorted
     * @param outputFile
     *            The sorted destination CSV file
     * @param writeHeader
     *            Wether the CSV header should be written in the output CSV file
     */
    public void sort(CsvFile inputFile, File outputFile, boolean writeHeader) {
	if (inputFile == null) {
	    throw new NullPointerException("inputFile is null !");
	}
	if (outputFile == null) {
	    throw new NullPointerException("outputFile is null !");
	}
	if (!inputFile.isFile()) {
	    throw new SampleException(
		    inputFile.getAbsolutePath()
		            + " does not exist or is not a file. Please provide an existing samples file");
	}
	if (outputFile.isDirectory()) {
	    throw new SampleException(
		    outputFile.getAbsolutePath()
		            + " is a directory. Please provide a valid output sample file path (not a directory)");
	}
	CsvSampleReader csvReader = new CsvSampleReader(inputFile);
	try {
	    sort(csvReader, outputFile, writeHeader);
	} finally {
	    csvReader.close();
	}
    }

    /**
     * Sort an input CSV file whose metadata structure is provided. Use this
     * method when input CSV file has no header : header information is then
     * provided through the sampleMetadata parameter.
     * 
     * @param sampleMetadata
     *            The CSV metadata : header information + separator
     * @param inputFile
     *            The input file to be sorted
     * @param outputFile
     *            THe ouput sorted file
     * @param writeHeader
     *            Whether output CSV header should be written (based on provided
     *            sample metadata)
     */
    public void sort(SampleMetadata sampleMetadata, File inputFile,
	    File outputFile, boolean writeHeader) {
	if (sampleMetadata == null) {
	    throw new NullPointerException("sampleMetadata is null !");
	}
	if (inputFile == null) {
	    throw new NullPointerException("inputFile is null !");
	}
	if (outputFile == null) {
	    throw new NullPointerException("outputFile is null !");
	}
	if (!inputFile.isFile()) {
	    throw new SampleException(
		    inputFile.getAbsolutePath()
		            + " does not exist or is not a file. Please provide an existing samples file");
	}
	if (outputFile.isDirectory()) {
	    throw new SampleException(
		    outputFile.getAbsolutePath()
		            + " is a directory. Please provide a valid output sample file path (not a directory)");
	}
	CsvSampleReader csvReader = new CsvSampleReader(inputFile,
	        sampleMetadata);
	try {
	    sort(csvReader, outputFile, writeHeader);
	} finally {
	    csvReader.close();
	}
    }

    /**
     * Sort samples that are read from the provided csv sample reader to the
     * specified output file.
     * 
     * @param csvReader
     *            The that provide samples to be sorted
     * @param output
     *            The output file that will contains sorted samples
     * @param writeHeader
     *            Wether to writer CSV header on the output file
     */
    private void sort(CsvSampleReader csvReader, File output,
	    boolean writeHeader) {
	if (csvReader == null) {
	    throw new NullPointerException("csvReader is null !");
	}
	if (output == null) {
	    throw new NullPointerException("output is null !");
	}
	SampleMetadata sampleMetadata = csvReader.getMetadata();
	SampleWriterConsumer writerConsumer = new SampleWriterConsumer();
	writerConsumer.setOutputFile(output);
	writerConsumer.setWriteHeader(writeHeader);
	addSampleConsumer(writerConsumer);
	try {
	    super.setConsumedMetadata(sampleMetadata, 0);
	    startConsuming();
	    Sample s = null;
	    while ((s = csvReader.readSample()) != null) {
		consume(s, 0);
	    }
	    stopConsuming();
	} finally {
	    removeSampleConsumer(writerConsumer);
	}
    }

    public void startConsuming() {
	if (sampleComparator == null) {
	    throw new NullPointerException(
		    "sampleComparator is not set, call setSampleComparator() first.");
	}

	File workDir = getWorkingDirectory();
	workDir.mkdir();
	this.pool.prestartAllCoreThreads();
	inputSampleCount = 0;
	chunkedSampleCount = 0;
	chunks = new LinkedList<File>();
	samples = new LinkedList<Sample>();
	sampleMetadata = getConsumedMetadata(0);
	sampleComparator.initialize(sampleMetadata);
    }

    public void consume(Sample s, int channel) {
	samples.add(s);
	inputSampleCount++;
	if (samples.size() >= chunkSize) {
	    chunks.add(sortAndDump(samples, sampleMetadata));
	    samples.clear();
	}
    }

    public void stopConsuming() {
	if (samples.size() > 0) {
	    chunks.add(sortAndDump(samples, sampleMetadata));
	}
	if (log.isDebugEnabled()) {
	    log.debug("sort(): " + inputSampleCount
		    + " samples read from input, " + chunkedSampleCount
		    + " samples written to chunk files");
	    if (inputSampleCount != chunkedSampleCount) {
		log.error("Failure !");
	    } else {
		log.info("chunked samples dumps succeeded.");
	    }
	}
	super.setProducedMetadata(sampleMetadata, 0);
	super.startProducing();
	sortFilesParallel(chunks, sampleMetadata, this);
	super.stopProducing();
	if (this.pool != null) {
	    this.pool.shutdown();
	}
	getWorkingDirectory().delete();
    }

    private File sortAndDump(final List<Sample> samples,
	    final SampleMetadata sampleMetadata) {
	long start = 0;
	long stop = 0;
	if (log.isDebugEnabled()) {
	    log.debug("sortAndDump(): Sorting " + samples.size()
		    + " samples...");
	    start = System.currentTimeMillis();
	}
	final List<Sample> sortedSamples = sortSamplesParallel(samples);
	if (sortedSamples.size() != samples.size()) {
	    throw new SampleException("sort failed ! " + sortedSamples.size()
		    + " != " + samples.size());
	}
	if (log.isDebugEnabled()) {
	    stop = System.currentTimeMillis();
	    log.debug("sortAndDump(): in " + (stop - start) / 1000f
		    + " s. Sorted  " + samples.size() + " samples.");
	}
	File out = getChunkFile();
	if (log.isDebugEnabled()) {
	    log.debug("sortAndDump(): Dumping chunk " + out);
	    start = System.currentTimeMillis();
	}
	CsvSampleWriter csvWriter = new CsvSampleWriter(out, sampleMetadata);
	try {
	    for (Sample sample : sortedSamples) {
		csvWriter.write(sample);
		chunkedSampleCount++;
	    }
	} finally {
	    csvWriter.close();
	}
	if (log.isDebugEnabled()) {
	    stop = System.currentTimeMillis();
	    log.debug("sortAndDump(): in " + (stop - start) / 1000f
		    + " s : Dumped chunk " + out.getAbsolutePath());
	}
	return out;
    }

    private List<Sample> sortSamplesParallel(final List<Sample> samples) {
	int sz = samples.size();
	if (sz <= 1) {
	    return samples;
	}
	int middle = sz / 2;
	final List<Sample> left = samples.subList(0, middle);
	final List<Sample> right = samples.subList(middle, sz);
	Job<List<Sample>> jobLeft = new Job<List<Sample>>() {
	    protected List<Sample> exec() {
		return sort(left);
	    }
	};
	Job<List<Sample>> jobRight = new Job<List<Sample>>() {
	    @Override
	    protected List<Sample> exec() {
		return sort(right);
	    }
	};

	List<Sample> newLeft = null;
	List<Sample> newRight = null;
	workQueue.add(jobLeft);
	workQueue.add(jobRight);
	if (parallelize) {
	    try {
		newLeft = jobLeft.getResult();
		newRight = jobRight.getResult();
	    } catch (InterruptedException ie) {
		throw new SampleException("Unexpected interruption !", ie);
	    }
	} else {
	    newLeft = sort(left);
	    newRight = sort(right);
	}
	return merge(newLeft, newRight);
    }

    public List<Sample> sort(List<Sample> samples) {
	int sz = samples.size();
	if (sz <= 1) {
	    return samples;
	}
	int middle = sz / 2;
	List<Sample> left = samples.subList(0, middle);
	List<Sample> right = samples.subList(middle, sz);
	left = sort(left);
	right = sort(right);
	return merge(left, right);
    }

    private List<Sample> merge(List<Sample> left, List<Sample> right) {
	ArrayList<Sample> out = new ArrayList<Sample>();
	ListIterator<Sample> l = left.listIterator();
	ListIterator<Sample> r = right.listIterator();
	while (l.hasNext() || r.hasNext()) {
	    if (l.hasNext() && r.hasNext()) {
		Sample firstLeft = l.next();
		Sample firstRight = r.next();
		if (revertedSort == false
		        && sampleComparator.compare(firstLeft, firstRight) < 0
		        || revertedSort == true
		        && sampleComparator.compare(firstLeft, firstRight) >= 0) {
		    out.add(firstLeft);
		    r.previous();
		} else {
		    out.add(firstRight);
		    l.previous();
		}
	    } else if (l.hasNext()) {
		out.add(l.next());
	    } else if (r.hasNext()) {
		out.add(r.next());
	    }
	}
	return out;
    }

    public void mergeFiles(List<File> chunks, SampleMetadata metadata,
	    SampleProducer producer) {
	sortFilesParallel(chunks, metadata, producer);
    }

    private void sortFilesParallel(List<File> chunks,
	    final SampleMetadata metadata, SampleProducer out) {
	int sz = chunks.size();
	if (sz > 1) {
	    int middle = sz / 2;
	    final List<File> left = chunks.subList(0, middle);
	    final List<File> right = chunks.subList(middle, sz);
	    File leftFile = null;
	    File rightFile = null;
	    Job<File> leftJob = new Job<File>() {
		@Override
		protected File exec() {
		    return mergeSortFiles(left, metadata);
		}
	    };
	    Job<File> rightJob = new Job<File>() {
		@Override
		protected File exec() {
		    return mergeSortFiles(right, metadata);
		}
	    };
	    if (parallelize) {
		workQueue.add(leftJob);
		workQueue.add(rightJob);
		try {
		    leftFile = leftJob.getResult();
		    rightFile = rightJob.getResult();
		} catch (InterruptedException ie) {
		    throw new SampleException("Unexpected interruption !", ie);
		}
	    } else {
		leftFile = leftJob.exec();
		rightFile = rightJob.exec();
	    }
	    mergeFiles(metadata, leftFile, rightFile, out);
	} else {
	    File f = chunks.get(0);
	    CsvSampleReader reader = new CsvSampleReader(f, metadata);
	    Sample s = null;
	    while ((s = reader.readSample()) != null) {
		out.produce(s, 0);
	    }
	}
    }

    // private static long countSamples(File f, SampleMetadata metadata) {
    // long out = 0;
    // CsvSampleReader reader = null;
    //
    // if (metadata != null) {
    // reader = new CsvSampleReader(f, metadata);
    // } else {
    // reader = new CsvSampleReader(f, ';');
    // }
    // while (reader.readSample() != null) {
    // out++;
    // }
    // return out;
    // }

    private File mergeSortFiles(List<File> chunks, SampleMetadata metadata) {
	int sz = chunks.size();
	if (sz == 1) {
	    return chunks.get(0);
	}
	int middle = sz / 2;
	List<File> left = chunks.subList(0, middle);
	List<File> right = chunks.subList(middle, sz);
	File leftFile = mergeSortFiles(left, metadata);
	File rightFile = mergeSortFiles(right, metadata);
	return mergeFiles(leftFile, rightFile, metadata);
    }

    private File mergeFiles(File left, File right, SampleMetadata metadata) {
	File out = getChunkFile();
	mergeFiles(metadata, left, right, out, false);
	return out;
    }

    private void mergeFiles(SampleMetadata metadata, File left, File right,
	    File out, boolean writeHeader) {
	if (out == null) {
	    out = getChunkFile();
	}
	CsvSampleWriter csvWriter = new CsvSampleWriter(out, metadata);
	CsvSampleReader l = new CsvSampleReader(left, metadata);
	CsvSampleReader r = new CsvSampleReader(right, metadata);
	try {
	    if (writeHeader) {
		csvWriter.writeHeader();
	    }
	    while (l.hasNext() || r.hasNext()) {
		if (l.hasNext() && r.hasNext()) {
		    Sample firstLeft = l.peek();
		    Sample firstRight = r.peek();
		    if (revertedSort == false
			    && sampleComparator.compare(firstLeft, firstRight) < 0
			    || revertedSort == true
			    && sampleComparator.compare(firstLeft, firstRight) >= 0) {
			csvWriter.write(firstLeft);
			l.readSample();
		    } else {
			csvWriter.write(firstRight);
			r.readSample();
		    }
		} else if (l.hasNext()) {
		    csvWriter.write(l.readSample());
		} else if (r.hasNext()) {
		    csvWriter.write(r.readSample());
		}
	    }
	} finally {
	    csvWriter.close();
	    l.close();
	    r.close();
	}
    }

    private void mergeFiles(SampleMetadata metadata, File left, File right,
	    SampleProducer out) {
	CsvSampleReader l = new CsvSampleReader(left, metadata);
	CsvSampleReader r = new CsvSampleReader(right, metadata);
	try {
	    while (l.hasNext() || r.hasNext()) {
		if (l.hasNext() && r.hasNext()) {
		    Sample firstLeft = l.peek();
		    Sample firstRight = r.peek();
		    if (revertedSort == false
			    && sampleComparator.compare(firstLeft, firstRight) < 0
			    || revertedSort == true
			    && sampleComparator.compare(firstLeft, firstRight) >= 0) {
			out.produce(firstLeft, 0);
			l.readSample();
		    } else {
			out.produce(firstRight, 0);
			r.readSample();
		    }
		} else if (l.hasNext()) {
		    out.produce(l.readSample(), 0);
		} else if (r.hasNext()) {
		    out.produce(r.readSample(), 0);
		}
	    }
	} finally {
	    l.close();
	    r.close();
	}
    }

    private AtomicInteger sequence = new AtomicInteger();

    private File getChunkFile() {
	DecimalFormat df = new DecimalFormat("00000");
	File out = new File(getWorkingDirectory(), "chunk-"
	        + df.format(sequence.incrementAndGet()) + ".csv");
	out.deleteOnExit();
	return out;
    }

    /**
     * @return the revertedSort
     */
    public final boolean isRevertedSort() {
	return revertedSort;
    }

    /**
     * @param revertedSort
     *            the revertedSort to set
     */
    public final void setRevertedSort(boolean revertedSort) {
	this.revertedSort = revertedSort;
    }

    // private static void test(String wd, String in, String out) {
    // File workDir = new File(wd);
    //
    // CsvFile input = new CsvFile(in, ';');
    // File output = new File(out);
    //
    // ElapsedSampleComparator comparator = new ElapsedSampleComparator();
    // ExternalSampleSorter sorter = new ExternalSampleSorter();
    // sorter.setWorkDir(workDir);
    // sorter.setChunkSize(800000);
    // sorter.setSampleComparator(comparator);
    // sorter.setParallelize(true);
    // for (int i = 0; i < 1; i++) {
    // long start = System.currentTimeMillis();
    // sorter.sort(input, output, true);
    // long stop = System.currentTimeMillis();
    // log.info((stop - start) / 1000f / 60f + " m");
    // log.debug("Checking output sample count...");
    // long sampleCount = countSamples(output, null);
    // log.debug("Counted " + sampleCount +
    // " samples in generated sorted file : output=" + output.length() +
    // " bytes, input=" + input.length() + " bytes");
    // if (input.length() != output.length()) {
    // log.error("Sort failed ! sizes differ.");
    // } else {
    // log.info("sort success !");
    // }
    // }
    // }

}
