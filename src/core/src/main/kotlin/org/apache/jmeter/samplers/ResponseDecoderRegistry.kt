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

import org.apache.jmeter.samplers.decoders.DeflateDecoder
import org.apache.jmeter.samplers.decoders.GzipDecoder
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler
import org.apiguardian.api.API
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for [ResponseDecoder] implementations.
 * Provides centralized management of response decoders for different content encodings.
 *
 * Decoders are discovered via:
 * - Built-in decoders (gzip, deflate)
 * - ServiceLoader mechanism (META-INF/services)
 *
 * Thread-safe singleton registry.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public object ResponseDecoderRegistry {

    private val log = LoggerFactory.getLogger(ResponseDecoderRegistry::class.java)

    /**
     * Map of encoding name (lowercase) to decoder implementation.
     * Uses ConcurrentHashMap for thread-safe access.
     */
    private val decoders = ConcurrentHashMap<String, ResponseDecoder>()

    init {
        // Register built-in decoders, this ensures the decoders are there even if service registration fails
        registerDecoder(GzipDecoder())
        registerDecoder(DeflateDecoder())

        // Load decoders via ServiceLoader
        loadServiceLoaderDecoders()
    }

    /**
     * Loads decoders using ServiceLoader mechanism.
     */
    private fun loadServiceLoaderDecoders() {
        try {
            JMeterUtils.loadServicesAndScanJars(
                ResponseDecoder::class.java,
                ServiceLoader.load(ResponseDecoder::class.java),
                Thread.currentThread().contextClassLoader,
                LogAndIgnoreServiceLoadExceptionHandler(log)
            ).forEach { registerDecoder(it) }
        } catch (e: Exception) {
            log.error("Error loading ResponseDecoder services", e)
        }
    }

    /**
     * Registers a decoder for all its encoding types.
     * If a decoder already exists for an encoding, the one with higher priority is kept.
     *
     * @param decoder the decoder to register
     */
    @JvmStatic
    public fun registerDecoder(decoder: ResponseDecoder) {
        val encodings = decoder.encodings
        if (encodings.isEmpty()) {
            log.warn("Decoder {} has null or empty encodings list, skipping registration", decoder.javaClass.name)
            return
        }

        for (encoding in encodings) {
            val key = encoding.lowercase(Locale.ROOT)

            decoders.merge(key, decoder) { existing, newDecoder ->
                // Keep the decoder with higher priority
                if (newDecoder.priority > existing.priority) {
                    log.info(
                        "Replacing decoder for '{}': {} (priority {}) -> {} (priority {})",
                        encoding,
                        existing.javaClass.simpleName, existing.priority,
                        newDecoder.javaClass.simpleName, newDecoder.priority
                    )
                    newDecoder
                } else {
                    log.debug(
                        "Keeping existing decoder for '{}': {} (priority {}) over {} (priority {})",
                        encoding,
                        existing.javaClass.simpleName, existing.priority,
                        newDecoder.javaClass.simpleName, newDecoder.priority
                    )
                    existing
                }
            }
        }
    }

    /**
     * Decodes the given data using the decoder registered for the specified encoding.
     * If no decoder is found for the encoding, returns the data unchanged.
     *
     * @param encoding the content encoding (e.g., "gzip", "deflate", "br")
     * @param data the data to decode
     * @return decoded data, or original data if no decoder found or encoding is null
     * @throws IOException if decoding fails
     */
    @JvmStatic
    @Throws(IOException::class)
    public fun decode(encoding: String?, data: ByteArray?): ByteArray {
        if (encoding.isNullOrEmpty() || data == null || data.isEmpty()) {
            return data ?: ByteArray(0)
        }

        val decoder = decoders[encoding] ?: decoders[encoding.lowercase(Locale.ROOT)]

        if (decoder == null) {
            log.debug("No decoder found for encoding '{}', returning data unchanged", encoding)
            return data
        }

        return decoder.decode(data)
    }

    /**
     * Creates a decompressing InputStream that wraps the given input stream using the decoder
     * registered for the specified encoding.
     *
     * This enables streaming decompression without buffering the entire response in memory,
     * which is useful for computing checksums on decompressed data or processing large responses.
     *
     * If no decoder is found for the encoding, returns the original input stream unchanged.
     *
     * @param encoding the content encoding (e.g., "gzip", "deflate", "br")
     * @param input the input stream to wrap with decompression
     * @return a decompressing InputStream, or the original stream if no decoder found or encoding is null
     * @throws IOException if the decompressing stream cannot be created
     * @since 6.0.0
     */
    @JvmStatic
    @Throws(IOException::class)
    public fun decodeStream(encoding: String?, input: InputStream): InputStream {
        if (encoding.isNullOrEmpty()) {
            return input
        }

        val decoder = decoders[encoding] ?: decoders[encoding.lowercase(Locale.ROOT)]

        if (decoder == null) {
            log.debug("No decoder found for encoding '{}', returning input stream unchanged", encoding)
            return input
        }

        return decoder.decodeStream(input)
    }

    /**
     * Checks if a decoder is registered for the given encoding.
     * Primarily for testing purposes.
     *
     * @param encoding the encoding to check
     * @return true if a decoder is registered for this encoding
     */
    @JvmStatic
    public fun hasDecoder(encoding: String): Boolean =
        decoders.containsKey(encoding.lowercase(Locale.ROOT))
}
