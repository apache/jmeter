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

import org.apache.jmeter.samplers.ResponseDecoder
import org.apache.jorphan.io.DirectAccessByteArrayOutputStream
import org.apiguardian.api.API
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * Decoder for deflate compressed response data.
 * Attempts decompression with ZLIB wrapper first, falls back to raw DEFLATE if that fails.
 *
 * @since 6.0.0
 */
@API(status = API.Status.INTERNAL, since = "6.0.0")
public class DeflateDecoder : ResponseDecoder {
    override val encodings: List<String>
        get() = listOf("deflate")

    override fun decode(compressed: ByteArray): ByteArray {
        // Try with ZLIB wrapper first
        return try {
            decompressWithInflater(compressed, nowrap = false)
        } catch (e: IOException) {
            // If that fails, try with NO_WRAP for raw DEFLATE
            decompressWithInflater(compressed, nowrap = true)
        }
    }

    override fun decodeStream(input: InputStream): InputStream {
        // For streaming, use ZLIB wrapper (nowrap=false) which is the most common case.
        // The fallback to raw DEFLATE is only available in the byte array version
        // since we cannot retry with a stream without buffering it first.
        return InflaterInputStream(input, Inflater(false))
    }

    /**
     * Decompresses data using Inflater with specified nowrap setting.
     *
     * @param compressed the compressed data
     * @param nowrap if true, uses raw DEFLATE (no ZLIB wrapper)
     * @return decompressed data
     * @throws IOException if decompression fails
     */
    private fun decompressWithInflater(compressed: ByteArray, nowrap: Boolean): ByteArray {
        val out = DirectAccessByteArrayOutputStream()
        InflaterInputStream(ByteArrayInputStream(compressed), Inflater(nowrap)).use {
            it.transferTo(out)
        }
        return out.toByteArray()
    }
}
