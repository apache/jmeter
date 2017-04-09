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
package org.apache.jmeter.report.processor.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.report.core.CsvSampleReader;
import org.apache.jmeter.report.core.CsvSampleWriter;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleBuilder;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ValueResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class AbstractOverTimeGraphConsumer provides a base class for over time
 * graphs.
 *
 * @since 3.0
 */
public abstract class AbstractVersusRequestsGraphConsumer extends
        AbstractGraphConsumer {
    private static final Long ONE = Long.valueOf(1L);
    public static final String RESULT_CTX_GRANULARITY = "granularity";
    public static final String TIME_INTERVAL_LABEL = "Interval";

    private long granularity;

    /**
     * The embedded time count consumer is used to buffer (disk storage) and tag
     * samples with the number of samples in the same interval.
     */
    private final TimeCountConsumer embeddedConsumer;

    /**
     * Gets the granularity.
     *
     * @return the granularity
     */
    public long getGranularity() {
        return granularity;
    }

    /**
     * Sets the granularity.
     *
     * @param granularity
     *            the granularity to set
     */
    // Must be final because called by ctor
    public final void setGranularity(long granularity) {
        this.granularity = granularity;
    }

    /**
     * Instantiates a new abstract over time graph consumer.
     */
    protected AbstractVersusRequestsGraphConsumer() {
        embeddedConsumer = new TimeCountConsumer(this);
        setGranularity(1L);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#startConsuming
     * ()
     */
    @Override
    public void startConsuming() {
        embeddedConsumer.startConsuming();
    }

    private void startConsumingBase() {
        super.startConsuming();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.AbstractSampleConsumer#setConsumedMetadata
     * (org.apache.jmeter.report.core.SampleMetadata, int)
     */
    @Override
    public void setConsumedMetadata(SampleMetadata sampleMetadata, int channel) {
        embeddedConsumer.setConsumedMetadata(sampleMetadata, channel);
    }

    private void setConsumedMetadataBase(SampleMetadata sampleMetadata,
            int channel) {
        super.setConsumedMetadata(sampleMetadata, channel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#consume
     * (org.apache.jmeter.report.core.Sample, int)
     */
    @Override
    public void consume(Sample sample, int channel) {
        embeddedConsumer.consume(sample, channel);
    }

    private void consumeBase(Sample sample, int channel) {
        super.consume(sample, channel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#stopConsuming
     * ()
     */
    @Override
    public void stopConsuming() {
        embeddedConsumer.stopConsuming();
    }

    public void stopConsumingBase() {
        super.stopConsuming();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.report.processor.graph.AbstractGraphConsumer#
     * initializeExtraResults(org.apache.jmeter.report.processor.MapResultData)
     */
    @Override
    protected void initializeExtraResults(MapResultData parentResult) {
        parentResult.setResult(RESULT_CTX_GRANULARITY, new ValueResultData(
                Long.valueOf(granularity)));
    }

    private static class TimeCountConsumer extends AbstractSampleConsumer {

        private static final Logger log = LoggerFactory.getLogger(TimeCountConsumer.class);

        private class FileInfo {
            private final File file;
            private final CsvSampleWriter writer;

            /**
             * Instantiates a new file info.
             *
             * @param file
             *            the file
             * @param metadata
             *            the metadata
             */
            public FileInfo(File file, SampleMetadata metadata) {
                this.file = file;
                this.writer = new CsvSampleWriter(file, metadata);
            }

            /**
             * Gets the file.
             *
             * @return the file
             */
            public File getFile() {
                return file;
            }

            /**
             * Gets the sample writer.
             *
             * @return the sample writer
             */
            public CsvSampleWriter getWriter() {
                return writer;
            }
        }

        // Collection of sample builders for channels
        private ArrayList<SampleBuilder> builders = new ArrayList<>();
        private ArrayList<FileInfo> fileInfos = new ArrayList<>();
        private HashMap<Long, Long> counts = new HashMap<>();
        boolean createdWorkDir = false;
        private final AbstractVersusRequestsGraphConsumer parent;

        public TimeCountConsumer(AbstractVersusRequestsGraphConsumer parent) {
            this.parent = parent;
        }

        private Long getTimeInterval(Sample sample) {
            long time = sample.getEndTime();
            return Long.valueOf(time - (time % parent.getGranularity()));
        }

        // Adds a new field in the sample metadata for each channel
        private void initProducedMetadata() {
            builders.clear();
            int channelCount = getConsumedChannelCount();
            for (int i = 0; i < channelCount; i++) {
                // Get the metadata for the current channel
                SampleMetadata consumedMetadata = getConsumedMetadata(i);

                // Copy metadata to an array
                int colCount = consumedMetadata.getColumnCount();
                String[] names = new String[colCount + 1];
                for (int j = 0; j < colCount; j++) {
                    names[j] = consumedMetadata.getColumnName(j);
                }

                // Add the new field
                names[colCount] = TIME_INTERVAL_LABEL;

                // Build the produced metadata from the array
                SampleMetadata producedMetadata = new SampleMetadata(
                        consumedMetadata.getSeparator(), names);

                // Add a sample builder for the current channel
                builders.add(new SampleBuilder(producedMetadata));
                parent.setConsumedMetadataBase(producedMetadata, i);
            }
        }

        private Sample createIndexedSample(Sample sample, int channel,
                double count) {
            SampleBuilder builder = builders.get(channel);
            SampleMetadata metadata = builder.getMetadata();
            int colCount = metadata.getColumnCount();
            for (int i = 0; i < colCount - 1; i++) {
                builder.add(sample.getData(i));
            }
            builder.add(String.valueOf(count));
            return builder.build();
        }

        @Override
        public void startConsuming() {

            // Handle the working directory
            File workDir = parent.getWorkingDirectory();
            createdWorkDir = false;
            if (!workDir.exists()) {
                createdWorkDir = workDir.mkdir();
                if (!createdWorkDir) {
                    String message = String.format(
                            "Cannot create working directory \"%s\"",
                            workDir);
                    log.error(message);
                    throw new SampleException(message);
                }
            }

            // Create a temporary file by channel to buffer samples
            int channelsCount = getConsumedChannelCount();
            for (int i = 0; i < channelsCount; i++) {
                try {
                    File tmpFile = File.createTempFile(parent.getName(), "-"
                            + String.valueOf(i), workDir);
                    tmpFile.deleteOnExit();
                    fileInfos.add(new FileInfo(tmpFile, getConsumedMetadata(i)));
                } catch (IOException ex) {
                    String message = String.format(
                            "Cannot create temporary file for channel #%d", Integer.valueOf(i));
                    log.error(message, ex);
                    throw new SampleException(message, ex);
                }
            }

            // Override produced metadata
            initProducedMetadata();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.jmeter.report.processor.SampleConsumer#consume(org.apache.
         * jmeter.report.core.Sample, int)
         */
        @Override
        public void consume(Sample sample, int channel) {
            // Count sample depending on time interval
            Long time = getTimeInterval(sample);
            Long count = counts.get(time);
            if (count != null) {
                counts.put(time, Long.valueOf(count.longValue() + 1));
            } else {
                counts.put(time, ONE);
            }
            fileInfos.get(channel).getWriter().write(sample);
        }

        @Override
        public void stopConsuming() {

            // Ask parent to start consumption
            parent.startConsumingBase();

            // Propagate tagged samples to parent
            int channelsCount = getConsumedChannelCount();
            for (int i = 0; i < channelsCount; i++) {
                FileInfo fileInfo = fileInfos.get(i);

                // Clean the writer
                CsvSampleWriter writer = fileInfo.getWriter();
                writer.close();

                // Create a reader and use it to get the buffered samples
                File file = fileInfo.getFile();
                try (CsvSampleReader reader = new CsvSampleReader(file,
                        getConsumedMetadata(i))) {
                    while (reader.hasNext()) {
                        Sample sample = reader.readSample();
                        // Ask parent to consume the altered sample
                        Long requestsPerGranularity = counts.get(getTimeInterval(sample)).longValue()
                                % parent.getGranularity();
                        Long requestsPerSecond = requestsPerGranularity * 1000 / parent.getGranularity();
                        parent.consumeBase(
                                createIndexedSample(sample, i, requestsPerSecond), i);
                    }
                } finally {
                    file.delete();
                }
            }

            if (createdWorkDir) {
                File workingDir = parent.getWorkingDirectory();
                try {
                    FileUtils.deleteDirectory(workingDir);
                } catch (IOException e) {
                    log.warn("Cannot delete created temporary directory, '{}'", workingDir, e);
                }
            }

            // Ask parent to stop consumption
            parent.stopConsumingBase();
        }
    }
}
