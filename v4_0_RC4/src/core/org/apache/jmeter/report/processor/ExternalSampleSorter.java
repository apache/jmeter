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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.report.core.CsvFile;
import org.apache.jmeter.report.core.CsvSampleReader;
import org.apache.jmeter.report.core.CsvSampleWriter;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleComparator;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R-way external sample sorter.
 * <p>
 * This SampleConsumer should be used to sort samples base on a
 * {@link SampleComparator}
 * </p>
 * <p>
 * Samples are sorted with the external sort algorithm. Thus, samples are not
 * all stored in memory to be sorted. Instead, they are sorted by chunk in
 * memory and then written to the disk before being merged at the end.
 * </p>
 * <p>
 * This sorter makes it possible to sort any number of samples with a fixed
 * amount of memory. Hard disk will be used instead of RAM, at the cost of
 * performance
 * </p>
 * <p>
 * When <b>parallel mode</b> is enabled and several CPU are available to the
 * JVM, this sorter uses multiple CPU to reduce sort time.<br>
 * The <b>parallel mode</b> can be disabled if some sort of concurrency issue is
 * encountered.
 * </p>
 * <p>
 * As a last note, this {@link SampleConsumer} can be used as normal class with
 * the different <code>sort()</code> methods
 * </p>
 * <p>
 * It is important to set the <b><code>chunkSize</code></b> property according
 * to the available memory as the algorithm does not take care of memory
 * allocation (samples sizes are not predictable)
 * </p>
 * <p>
 * Meanwhile, it is equally important to set a {@link SampleComparator} to
 * define sample ordering </p>
 * 
 * @since 3.0
 */
public class ExternalSampleSorter extends AbstractSampleConsumer {

    private static final String MUST_NOT_BE_NULL = "%s must not be null";

    private static final Logger LOG = LoggerFactory.getLogger(ExternalSampleSorter.class);

    private static final int DEFAULT_CHUNK_SIZE = 50000;

    private long chunkSize = DEFAULT_CHUNK_SIZE;

    private SampleComparator sampleComparator;

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor pool;

    private volatile int nbProcessors;

    private boolean parallelize;

    private final AtomicLong chunkedSampleCount = new AtomicLong();

    private final AtomicLong inputSampleCount = new AtomicLong();

    private LinkedList<File> chunks;

    private LinkedList<Sample> samples;

    private SampleMetadata sampleMetadata;

    private boolean revertedSort;
    
    private final AtomicInteger sequence = new AtomicInteger();


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
     * Set the number of samples that will be stored in memory. This
     * defines the number of samples that will be written in each chunk file
     * before merging step as well.
     * 
     * @param chunkSize
     *            The number of samples sorted in memory before they are written
     *            to disk. 5000 is the minimum and will be used if given
     *            chunkSize is less than 5000
     */
    public void setChunkSize(long chunkSize) {
        if (chunkSize < 50000) {
            chunkSize = 50000;
        }
        this.chunkSize = chunkSize;
    }

    /**
     * Set the sample comparator that will define sample ordering
     * 
     * @param sampleComparator comparator to define the ordering
     */
    // final because called from ctor
    public final void setSampleComparator(SampleComparator sampleComparator) {
        this.sampleComparator = sampleComparator;
    }

    /**
     * Enabled parallel mode
     * 
     * @param parallelize
     *            {@code true} to enable, {@code false} to disable
     */
    public void setParallelize(boolean parallelize) {
        this.parallelize = parallelize;
    }

    /**
     * @return {@code true} when parallel mode is enabled, {@code false} otherwise
     */
    public boolean isParallelize() {
        return parallelize;
    }

    /**
     * Sort an input CSV file to an sorted output CSV file.<br>
     * <p>
     * The input CSV <b>must</b> have a header otherwise sorting will give
     * unpredictable results
     * </p>
     * 
     * @param inputFile
     *            The CSV file to be sorted (must not be {@code null})
     * @param outputFile
     *            The sorted destination CSV file (must not be {@code null})
     * @param writeHeader
     *            Whether the CSV header should be written to the output CSV file
     */
    public void sort(CsvFile inputFile, File outputFile, boolean writeHeader) {
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
        
        try (CsvSampleReader csvReader = new CsvSampleReader(inputFile,
                inputFile.getSeparator(), false)){
            sort(csvReader, outputFile, writeHeader);
        } 
    }

    /**
     * Sort an input CSV file whose metadata structure is provided. Use this
     * method when input CSV file has no header : header information is then
     * provided through the sampleMetadata parameter.
     * 
     * @param sampleMetadata
     *            The CSV metadata : header information + separator (must not be {@code null})
     * @param inputFile
     *            The input file to be sorted (must not be {@code null})
     * @param outputFile
     *            The output sorted file (must not be {@code null})
     * @param writeHeader
     *            Whether output CSV header should be written (based on provided
     *            sample metadata)
     */
    public void sort(SampleMetadata sampleMetadata, File inputFile,
            File outputFile, boolean writeHeader) {
        Validate.notNull(sampleMetadata, MUST_NOT_BE_NULL, "sampleMetadata");

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
        try (CsvSampleReader csvReader = new CsvSampleReader(inputFile,
                sampleMetadata)){
            sort(csvReader, outputFile, writeHeader);
        }
    }

    /**
     * Sort samples that are read from the provided csv sample reader to the
     * specified output file.
     * 
     * @param csvReader
     *            The reader that provides the samples to be sorted (must not be {@code null})
     * @param output
     *            The output file that will contain the sorted samples
     * @param writeHeader
     *            Whether to write CSV header to the output file
     */
    private void sort(CsvSampleReader csvReader, File output,
            boolean writeHeader) {
        Validate.notNull(output, MUST_NOT_BE_NULL, "output");

        SampleMetadata readSampleMetadata = csvReader.getMetadata();
        SampleWriterConsumer writerConsumer = new SampleWriterConsumer();
        writerConsumer.setOutputFile(output);
        writerConsumer.setWriteHeader(writeHeader);
        addSampleConsumer(writerConsumer);
        try {
            super.setConsumedMetadata(readSampleMetadata, 0);
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

    @Override
    public void startConsuming() {
        Validate.validState(sampleComparator != null,
                "sampleComparator is not set, call setSampleComparator() first.");

        File workDir = getWorkingDirectory();
        workDir.mkdir();
        this.pool.prestartAllCoreThreads();
        inputSampleCount.set(0);
        chunkedSampleCount.set(0);
        chunks = new LinkedList<>();
        samples = new LinkedList<>();
        sampleMetadata = getConsumedMetadata(0);
        sampleComparator.initialize(sampleMetadata);
    }

    @Override
    public void consume(Sample s, int channel) {
        samples.add(s);
        inputSampleCount.incrementAndGet();
        if (samples.size() >= chunkSize) {
            chunks.add(sortAndDump(samples, sampleMetadata));
            samples.clear();
        }
    }

    @Override
    public void stopConsuming() {
        if (!samples.isEmpty()) {
            chunks.add(sortAndDump(samples, sampleMetadata));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("sort(): " + inputSampleCount.longValue()
                    + " samples read from input, " + chunkedSampleCount.longValue()
                    + " samples written to chunk files");
            if (inputSampleCount.get() != chunkedSampleCount.get()) {
                LOG.error("Failure! Number of samples read from input and written to chunk files differ");
            } else {
                LOG.info("dumping of samples chunk succeeded.");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortAndDump(): Sorting " + samples.size()
                    + " samples...");
            start = System.currentTimeMillis();
        }
        final List<Sample> sortedSamples = sortSamplesParallel(samples);
        if (sortedSamples.size() != samples.size()) {
            throw new SampleException("sort failed ! " + sortedSamples.size()
                    + " != " + samples.size());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortAndDump(): in " + (System.currentTimeMillis() - start) / 1000f
                    + " s. Sorted  " + samples.size() + " samples.");
        }
        File out = getChunkFile();
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortAndDump(): Dumping chunk " + out);
            start = System.currentTimeMillis();
        }
        try (CsvSampleWriter csvWriter = new CsvSampleWriter(out, sampleMetadata)){
            for (Sample sample : sortedSamples) {
                csvWriter.write(sample);
                chunkedSampleCount.incrementAndGet();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortAndDump(): in " + (System.currentTimeMillis() - start) / 1000f
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

        List<Sample> newLeft;
        List<Sample> newRight;
        Job<List<Sample>> jobLeft = createSortJob(left);
        Job<List<Sample>> jobRight = createSortJob(right);
        if (parallelize) {
            workQueue.add(jobLeft);
            workQueue.add(jobRight);
            try {
                newLeft = jobLeft.getResult();
                newRight = jobRight.getResult();
            } catch (InterruptedException ie) { // NOSONAR we throw another exception
                throw new SampleException("Unexpected interruption !", ie);
            }
        } else {
            newLeft = jobLeft.exec();
            newRight = jobRight.exec();
        }
        return merge(newLeft, newRight);
    }

    private Job<List<Sample>> createSortJob(final List<Sample> samples) {
        return new Job<List<Sample>>() {
            @Override
            protected List<Sample> exec() {
                return sort(samples);
            }
        };
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
        ArrayList<Sample> out = new ArrayList<>();
        ListIterator<Sample> l = left.listIterator();
        ListIterator<Sample> r = right.listIterator();
        while (l.hasNext() || r.hasNext()) {
            if (l.hasNext() && r.hasNext()) {
                Sample firstLeft = l.next();
                Sample firstRight = r.next();
                if (!revertedSort
                        && sampleComparator.compare(firstLeft, firstRight) < 0
                        || revertedSort
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
            File leftFile;
            File rightFile;
            Job<File> leftJob = createMergeJob(left, metadata);
            Job<File> rightJob = createMergeJob(right, metadata);
            if (parallelize) {
                workQueue.add(leftJob);
                workQueue.add(rightJob);
                try {
                    leftFile = leftJob.getResult();
                    rightFile = rightJob.getResult();
                } catch (InterruptedException ie) { // NOSONAR We throw an exception
                    throw new SampleException("Unexpected interruption !", ie);
                }
            } else {
                leftFile = leftJob.exec();
                rightFile = rightJob.exec();
            }
            mergeFiles(metadata, leftFile, rightFile, out);
        } else {
            File f = chunks.get(0);
            try (CsvSampleReader reader = new CsvSampleReader(f, metadata)) {
                Sample sample;
                while ((sample = reader.readSample()) != null) {
                    out.produce(sample, 0);
                }
            }
        }
    }

    private Job<File> createMergeJob(final List<File> chunks,
            final SampleMetadata metadata) {
        return new Job<File>() {
            @Override
            protected File exec() {
                return mergeSortFiles(chunks, metadata);
            }
        };
    }

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

        try (CsvSampleWriter csvWriter = new CsvSampleWriter(out, metadata);
                CsvSampleReader l = new CsvSampleReader(left, metadata);
                CsvSampleReader r = new CsvSampleReader(right, metadata)) {
            if (writeHeader) {
                csvWriter.writeHeader();
            }
            while (l.hasNext() || r.hasNext()) {
                if (l.hasNext() && r.hasNext()) {
                    Sample firstLeft = l.peek();
                    Sample firstRight = r.peek();
                    if (leftBeforeRight(firstLeft, firstRight)) {
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
        }
    }

    private void mergeFiles(SampleMetadata metadata, File left, File right,
            SampleProducer out) {
        try (CsvSampleReader l = new CsvSampleReader(left, metadata);
                CsvSampleReader r = new CsvSampleReader(right, metadata)) {
            while (l.hasNext() || r.hasNext()) {
                if (l.hasNext() && r.hasNext()) {
                    Sample firstLeft = l.peek();
                    Sample firstRight = r.peek();
                    if (leftBeforeRight(firstLeft, firstRight)) {
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
        }
    }

    /**
     * Decides which sample should be written first given the configured
     * {@link SampleComparator} and the sort order.
     * 
     * @param leftSample
     *            the <em>left</em> sample
     * @param rightSample
     *            the <em>right</em> sample
     * @return {@code true} when {@code leftSample} should be written first,
     *         {@code false} otherwise
     */
    private boolean leftBeforeRight(Sample leftSample, Sample rightSample) {
        return !revertedSort
                && sampleComparator.compare(leftSample, rightSample) < 0
                || revertedSort
                && sampleComparator.compare(leftSample, rightSample) >= 0;
    }

    private File getChunkFile() {
        DecimalFormat df = new DecimalFormat("00000");
        File out = new File(getWorkingDirectory(), "chunk-"
                + df.format(sequence.incrementAndGet()) + ".csv");
        out.deleteOnExit();
        return out;
    }

    /**
     * @return flag, whether the order of the sort should be reverted
     */
    public final boolean isRevertedSort() {
        return revertedSort;
    }

    /**
     * @param revertedSort
     *            flag, whether the order of the sort should be reverted.
     *            {@code false} uses the order of the configured
     *            {@link SampleComparator}
     */
    // final because called from ctor
    public final void setRevertedSort(boolean revertedSort) {
        this.revertedSort = revertedSort;
    }
}
