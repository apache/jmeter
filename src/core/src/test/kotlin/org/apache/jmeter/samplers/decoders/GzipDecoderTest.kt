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

package org.apache.jmeter.samplers.decoders

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class GzipDecoderTest {
    private val decoder = GzipDecoder()

    @Test
    fun testGetEncodings() {
        assertEquals(listOf("gzip", "x-gzip"), decoder.encodings, "encodings")
    }

    @Test
    fun testGetPriority() {
        assertEquals(0, decoder.priority, "Default priority should be 0")
    }

    @Test
    fun testDecodeGzipData() {
        val originalText = "Hello, World! This is a test message for gzip compression."
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Compress data with gzip
        val compressed = compressGzip(originalData)

        // Decode
        val decoded = decoder.decode(compressed)

        assertArrayEquals(originalData, decoded, "Decoded data should match original")
        assertEquals(originalText, decoded.toString(Charsets.UTF_8), "Decoded text should match original")
    }

    @Test
    fun testDecodeEmptyData() {
        val emptyCompressed = compressGzip(ByteArray(0))
        val decoded = decoder.decode(emptyCompressed)

        assertEquals(0, decoded.size, "Empty data should decode to empty array")
    }

    @Test
    fun testDecodeInvalidData() {
        val invalidData = "This is not gzip compressed data".toByteArray(Charsets.UTF_8)

        assertThrows(Exception::class.java) {
            decoder.decode(invalidData)
        }
    }

    /**
     * Helper method to compress data with gzip
     */
    private fun compressGzip(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzipOut ->
            gzipOut.write(data)
        }
        return baos.toByteArray()
    }
}
