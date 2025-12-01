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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.GZIPOutputStream

class ResponseDecoderRegistryTest {
    @Test
    fun testBuiltInDecodersAreRegistered() {
        assertTrue(ResponseDecoderRegistry.hasDecoder("gzip"), "gzip decoder should be registered")
        assertTrue(ResponseDecoderRegistry.hasDecoder("x-gzip"), "x-gzip decoder should be registered")
        assertTrue(ResponseDecoderRegistry.hasDecoder("deflate"), "deflate decoder should be registered")
    }

    @Test
    fun testDecodeWithGzip() {
        val originalText = "Hello, World! This is a test of gzip compression."
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Compress with gzip
        val compressed = compressGzip(originalData)

        // Decode using registry
        val decoded = ResponseDecoderRegistry.decode("gzip", compressed)

        assertArrayEquals(originalData, decoded, "Decoded data should match original")
    }

    @Test
    fun testDecodeWithXGzip() {
        val originalText = "Testing x-gzip encoding"
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Compress with gzip (x-gzip uses same compression)
        val compressed = compressGzip(originalData)

        // Decode using registry with x-gzip encoding
        val decoded = ResponseDecoderRegistry.decode("x-gzip", compressed)

        assertArrayEquals(originalData, decoded, "Decoded data should match original for x-gzip")
    }

    @Test
    fun testDecodeWithUnknownEncoding() {
        val originalData = "Test data".toByteArray(Charsets.UTF_8)

        // Decode with unknown encoding should return original data
        val result = ResponseDecoderRegistry.decode("unknown-encoding", originalData)

        assertArrayEquals(originalData, result, "Unknown encoding should return data unchanged")
    }

    @Test
    fun testDecodeWithNullEncoding() {
        val originalData = "Test data".toByteArray(Charsets.UTF_8)

        // Decode with null encoding should return original data
        val result = ResponseDecoderRegistry.decode(null, originalData)

        assertArrayEquals(originalData, result, "Null encoding should return data unchanged")
    }

    @Test
    fun testDecodeWithEmptyData() {
        val emptyData = ByteArray(0)

        val result = ResponseDecoderRegistry.decode("gzip", emptyData)

        assertArrayEquals(emptyData, result, "Empty data should return empty data")
    }

    @Test
    fun testCaseInsensitiveEncoding() {
        val originalText = "Case insensitive test"
        val originalData = originalText.toByteArray(Charsets.UTF_8)
        val compressed = compressGzip(originalData)

        // Test various case combinations
        val decoded1 = ResponseDecoderRegistry.decode("GZIP", compressed)
        val decoded2 = ResponseDecoderRegistry.decode("GZip", compressed)
        val decoded3 = ResponseDecoderRegistry.decode("gzip", compressed)

        assertArrayEquals(originalData, decoded1, "GZIP should decode correctly")
        assertArrayEquals(originalData, decoded2, "GZip should decode correctly")
        assertArrayEquals(originalData, decoded3, "gzip should decode correctly")
    }

    @Test
    fun testRegisterCustomDecoder() {
        // Create a custom decoder that reverses bytes (for testing)
        val reverseDecoder = object : ResponseDecoder {
            override val encodings: List<String>
                get() = listOf("test-reverse")

            override fun decode(compressed: ByteArray): ByteArray =
                compressed.reversedArray()

            override fun decodeStream(input: InputStream): InputStream {
                TODO("Not yet implemented")
            }
        }

        ResponseDecoderRegistry.registerDecoder(reverseDecoder)

        val data = "ABC".toByteArray(Charsets.UTF_8)
        val decoded = ResponseDecoderRegistry.decode("test-reverse", data)

        assertEquals("CBA", decoded.toString(Charsets.UTF_8), "Custom decoder should reverse bytes")
    }

    @Test
    fun testDecoderPriority() {
        // Register a low priority decoder
        val lowPriorityDecoder = object : ResponseDecoder {
            override val encodings: List<String>
                get() = listOf("priority-test")

            override fun decode(compressed: ByteArray): ByteArray =
                "low".toByteArray(Charsets.UTF_8)

            override fun decodeStream(input: InputStream): InputStream {
                TODO("Not yet implemented")
            }

            override val priority: Int
                get() = 1
        }

        // Register a high priority decoder for same encoding
        val highPriorityDecoder = object : ResponseDecoder {
            override val encodings: List<String>
                get() = listOf("priority-test")

            override fun decode(compressed: ByteArray): ByteArray =
                "high".toByteArray(Charsets.UTF_8)

            override fun decodeStream(input: InputStream): InputStream {
                TODO("Not yet implemented")
            }

            override val priority: Int
                get() = 10
        }

        ResponseDecoderRegistry.registerDecoder(lowPriorityDecoder)
        ResponseDecoderRegistry.registerDecoder(highPriorityDecoder)

        val result = ResponseDecoderRegistry.decode("priority-test", "test".toByteArray(Charsets.UTF_8))

        assertEquals("high", result.toString(Charsets.UTF_8), "Higher priority decoder should be used")
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
