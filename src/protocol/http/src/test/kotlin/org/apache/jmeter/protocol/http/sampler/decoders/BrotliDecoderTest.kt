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

package org.apache.jmeter.protocol.http.sampler.decoders

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Base64

/**
 * Basic tests for BrotliDecoder.
 * Full integration tests for brotli decompression are covered by HTTP sampler tests.
 */
class BrotliDecoderTest {
    private val decoder = BrotliDecoder()

    @Test
    fun testGetEncodings() {
        assertEquals(listOf("br"), decoder.encodings, "encodings")
    }

    @Test
    fun testGetPriority() {
        assertEquals(0, decoder.priority, "Default priority should be 0")
    }

    @Test
    fun testDecodeBrotliData() {
        // Pre-compressed "Hello World" with Brotli
        // Generated using: printf 'Hello World' | brotli | base64
        val compressed = Base64.getDecoder().decode("DwWASGVsbG8gV29ybGQD")

        val decoded = decoder.decode(compressed)

        assertEquals("Hello World", decoded.toString(Charsets.UTF_8), "Decoded text should match original")
    }

    @Test
    fun testDecodeInvalidData() {
        val invalidData = "This is not brotli compressed data".toByteArray(Charsets.UTF_8)

        assertThrows(IOException::class.java) {
            decoder.decode(invalidData)
        }
    }
}
