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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for implementing sample writer.<br>
 * <p>
 * Handles buffering and output writer replacement.<br>
 * </p>
 * <p>
 * When a writer is set on the sample writer any previous writer is flushed and
 * closed before being replaced by the new one.
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractSampleWriter extends SampleWriter {

    private static final int BUF_SIZE = 10000;

    private static final String CHARSET = SaveService.getFileEncoding(StandardCharsets.UTF_8.displayName());
    
    private static Logger log = LoggerFactory.getLogger(AbstractSampleWriter.class);

    /** output writer to write samples to */
    protected PrintWriter writer;

    /**
     * Set the new writer on which samples will be written by this sample writer.<br>
     * If any writer exists on the sample writer, it is flushed and closed before
     * being replaced by the new one.
     * 
     * @param newWriter
     *            The destination writer where samples will be written by this
     *            sample writer
     */
    public void setWriter(Writer newWriter) {
        Validate.notNull(newWriter, "writer must not be null."); // NOSONAR

        if (this.writer != null) {
            // flush and close previous writer
            JOrphanUtils.closeQuietly(this.writer);
        }
        this.writer = new PrintWriter(new BufferedWriter(newWriter, BUF_SIZE), false);
    }

    /**
     * Instructs this sample writer to write samples on the specified output
     * with UTG-8 encoding. The encoding can be overridden by the user through
     * {@link SaveService#getFileEncoding(String)}
     * 
     * @param out
     *            The output stream on which sample should be written
     */
    public void setOutputStream(OutputStream out) {
        Validate.notNull(out, "out must not be null."); // NOSONAR

        try {
            setWriter(new OutputStreamWriter(out, CHARSET));
        } catch (UnsupportedEncodingException e) {
            log.warn("Unsupported CHARSET: {}", CHARSET, e);
        }
    }

    /**
     * Set the destination file in which this sample writer will write samples
     * 
     * @param output
     *            The output file that will receive samples written by this
     *            sample writer
     */
    public void setOutputFile(File output) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output); // NOSONAR
        } catch (Exception e) {
            throw new SampleException(e.getMessage(), e);
        }
        setOutputStream(fos);
    }

    /**
     * This method is guaranteed to not throw any exception. If writer is already
     * closed then does nothing.<br>
     * Any buffered data is flushed by this method.
     */
    @Override
    public void close() {
        JOrphanUtils.closeQuietly(writer);
        this.writer = null;
    }

    /**
     * flush writer.
     * Only used for Tests
     */
    void flush() {
        writer.flush();
    }
}
