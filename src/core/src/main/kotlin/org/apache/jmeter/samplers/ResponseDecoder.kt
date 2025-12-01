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

package org.apache.jmeter.samplers

import org.apache.jorphan.io.DirectAccessByteArrayOutputStream
import org.apache.jorphan.reflect.JMeterService
import org.apiguardian.api.API
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Interface for response data decoders that handle different content encodings.
 * Implementations can be automatically discovered via [java.util.ServiceLoader].
 *
 * To add a custom decoder:
 * 1. Implement this interface
 * 2. Create `META-INF/services/org.apache.jmeter.samplers.ResponseDecoder` file
 * 4. Add your implementation's fully qualified class name to the file
 *
 * Example decoders: gzip, deflate, brotli
 *
 * @since 6.0.0
 */
@JMeterService
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public interface ResponseDecoder {

    /**
     * Returns the content encodings handled by this decoder.
     * These should match Content-Encoding header values (case-insensitive).
     *
     * A decoder can handle multiple encoding names (e.g., "gzip" and "x-gzip").
     *
     * Examples: ["gzip", "x-gzip"], ["deflate"], ["br"]
     *
     * @return list of encoding names this decoder handles (must not be null or empty)
     */
    public val encodings: List<String>

    /**
     * Decodes (decompresses) the given compressed data.
     *
     * @param compressed the compressed data to decode
     * @return the decompressed data
     * @throws java.io.IOException if decompression fails
     */
    public fun decode(compressed: ByteArray): ByteArray {
        val out = DirectAccessByteArrayOutputStream()
        decodeStream(ByteArrayInputStream(compressed)).use {
            it.transferTo(out)
        }
        return out.toByteArray()
    }

    /**
     * Creates a decompressing InputStream that wraps the given compressed input stream.
     * This allows streaming decompression without buffering the entire response in memory.
     *
     * Used for scenarios like MD5 computation on decompressed data, where we want to
     * compute the hash on-the-fly without storing the entire decompressed response.
     *
     * @param input the compressed input stream to wrap
     * @return an InputStream that decompresses data as it's read
     * @throws java.io.IOException if the decompressing stream cannot be created
     */
    public fun decodeStream(input: InputStream): InputStream

    /**
     * Returns the priority of this decoder.
     * When multiple decoders are registered for the same encoding,
     * the one with the highest priority is used.
     *
     * Default priority is 0. Built-in decoders use priority 0.
     * Plugins can override built-in decoders by returning a higher priority.
     *
     * @return priority value (higher = preferred), default is 0
     */
    public val priority: Int
        get() = 0
}
