/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An output stream that buffers data in memory up to a threshold, and spills to a temporary file
 * on disk if the threshold is exceeded. Small bodies therefore never touch the disk, while large
 * ones are streamed off heap instead of being materialized as a byte array.
 */
final class SpillOutputStream extends OutputStream {
    private static final Logger log = LoggerFactory.getLogger(SpillOutputStream.class);

    /** Size of the buffer used for the writes that go to the temporary file. */
    private static final int FILE_BUFFER_SIZE = 32 * 1024;

    private final int threshold;
    private final String tempFilePrefix;
    private final String tempFileSuffix;

    private ByteArrayOutputStream memoryBuffer;
    private Path tempFile;
    private OutputStream fileOutputStream;
    private long totalBytesWritten;
    private boolean closed;

    SpillOutputStream(int threshold, String tempFilePrefix, String tempFileSuffix) {
        this.threshold = threshold;
        this.tempFilePrefix = tempFilePrefix;
        this.tempFileSuffix = tempFileSuffix;
        this.memoryBuffer = new ByteArrayOutputStream(Math.min(threshold, 8192));
    }

    @Override
    public void write(int b) throws IOException {
        checkSpill(1);
        if (fileOutputStream != null) {
            fileOutputStream.write(b);
        } else {
            memoryBuffer.write(b);
        }
        totalBytesWritten++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }
        checkSpill(len);
        if (fileOutputStream != null) {
            fileOutputStream.write(b, off, len);
        } else {
            memoryBuffer.write(b, off, len);
        }
        totalBytesWritten += len;
    }

    private void checkSpill(int len) throws IOException {
        if (fileOutputStream == null && (totalBytesWritten + len) > threshold) {
            tempFile = Files.createTempFile(tempFilePrefix, tempFileSuffix);
            fileOutputStream = new BufferedOutputStream(Files.newOutputStream(tempFile), FILE_BUFFER_SIZE);
            memoryBuffer.writeTo(fileOutputStream);
            memoryBuffer = null;
        }
    }

    boolean isSpilled() {
        return tempFile != null;
    }

    /**
     * The buffered bytes, only meaningful while the stream has not spilled to disk.
     *
     * @see #isSpilled()
     */
    byte[] toByteArray() {
        return memoryBuffer != null ? memoryBuffer.toByteArray() : new byte[0];
    }

    Path getTempFile() {
        return tempFile;
    }

    /** Number of bytes written so far, regardless of whether they are held in memory or on disk. */
    long getLength() {
        return totalBytesWritten;
    }

    @Override
    public void flush() throws IOException {
        if (fileOutputStream != null) {
            fileOutputStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
    }

    /** Closes the stream and removes the temporary file, if the body was spilled to disk. */
    void releaseResources() {
        try {
            close();
        } catch (IOException e) {
            log.debug("Could not close the buffer of a spilled body", e);
        }
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Could not delete temporary body file {}", tempFile, e);
            }
            tempFile = null;
        }
    }
}
