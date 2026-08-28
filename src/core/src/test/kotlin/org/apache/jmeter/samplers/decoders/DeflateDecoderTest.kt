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
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class DeflateDecoderTest {
    private val decoder = DeflateDecoder()

    @Test
    fun testGetEncodings() {
        assertEquals(listOf("deflate"), decoder.encodings, "encodings")
    }

    @Test
    fun testGetPriority() {
        assertEquals(0, decoder.priority, "Default priority should be 0")
    }

    @Test
    fun testDecodeDeflateWithZlibWrapper() {
        val originalText = "Hello, World! This is a test message for deflate compression with ZLIB wrapper."
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Compress with ZLIB wrapper (default)
        val compressed = compressDeflate(originalData, nowrap = false)

        // Decode
        val decoded = decoder.decode(compressed)

        assertArrayEquals(originalData, decoded, "Decoded data should match original (ZLIB wrapper)")
    }

    @Test
    fun testDecodeDeflateRaw() {
        val originalText = "Testing raw deflate without ZLIB wrapper."
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Compress with NO_WRAP (raw deflate)
        val compressed = compressDeflate(originalData, nowrap = true)

        // Decode - should fallback to raw deflate
        val decoded = decoder.decode(compressed)

        assertArrayEquals(originalData, decoded, "Decoded data should match original (raw deflate)")
    }

    @Test
    fun testDecodeEmptyData() {
        val emptyCompressed = compressDeflate(ByteArray(0), nowrap = false)
        val decoded = decoder.decode(emptyCompressed)

        assertEquals(0, decoded.size, "Empty data should decode to empty array")
    }

    /**
     * Helper method to compress data with deflate
     * @param data the data to compress
     * @param nowrap if true, uses raw deflate (no ZLIB wrapper)
     */
    private fun compressDeflate(data: ByteArray, nowrap: Boolean): ByteArray {
        val baos = ByteArrayOutputStream()
        DeflaterOutputStream(baos, Deflater(Deflater.DEFAULT_COMPRESSION, nowrap)).use { deflaterOut ->
            deflaterOut.write(data)
        }
        return baos.toByteArray()
    }
}
