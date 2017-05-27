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

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.report.core.CsvSampleWriter;
import org.apache.jmeter.report.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample consumed by this consumer are written to a file<br>
 * <br>
 * 
 * @since 3.0
 */
public class SampleWriterConsumer extends AbstractSampleConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SampleWriterConsumer.class);

    private File outputFile;

    private CsvSampleWriter[] csvWriters;

    private boolean shouldWriteHeader;

    private int channelsCount;

    public void setOutputFile(String outputFile) {
        setOutputFile(new File(outputFile));
    }

    public void setOutputFile(File outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");
        this.outputFile = outputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public File getOutputFile(int channel) {
        String ext = null;
        String fName = getOutputFile().getName();
        int idx = fName.lastIndexOf('.');
        if (idx >= 0 && idx < fName.length() - 1) {
            String backedName = fName;
            fName = fName.substring(0, idx);
            ext = backedName.substring(idx + 1);
        } else {
            ext = "";
        }
        if (channel > 0) {
            fName += "-" + channel + "." + ext;
        } else {
            fName += "." + ext;
        }
        return new File(getOutputFile().getParentFile(), fName);
    }

    /**
     * Enables the CSV header on the output file (defaults to false)
     * 
     * @param writeHeader flag, whether CSV header should be written
     */
    public void setWriteHeader(boolean writeHeader) {
        this.shouldWriteHeader = writeHeader;
    }

    @Override
    public void startConsuming() {
        if (outputFile == null) {
            File wd = getWorkingDirectory();
            wd.mkdir();
            if (LOG.isInfoEnabled()) {
                LOG.info("startConsuming(): No output file set, writing to work directory :"
                    + wd.getAbsolutePath());
            }
            outputFile = new File(wd, "samples.csv");
        }
        outputFile.getParentFile().mkdirs();
        channelsCount = getConsumedChannelCount();
        csvWriters = new CsvSampleWriter[channelsCount];
        for (int i = 0; i < channelsCount; i++) {
            csvWriters[i] = new CsvSampleWriter(getOutputFile(i),getConsumedMetadata(i));
            if (shouldWriteHeader) {
                csvWriters[i].writeHeader();
            }
        }
    }

    @Override
    public void consume(Sample s, int channel) {
        csvWriters[channel].write(s);
    }

    @Override
    public void stopConsuming() {
        for (int i = 0; i < channelsCount; i++) {
            csvWriters[i].close();
        }
        getWorkingDirectory().delete();
    }
}
