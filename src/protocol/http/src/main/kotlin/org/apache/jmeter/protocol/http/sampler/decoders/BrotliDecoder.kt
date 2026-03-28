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

import com.google.auto.service.AutoService
import org.apache.jmeter.samplers.ResponseDecoder
import org.apiguardian.api.API
import org.brotli.dec.BrotliInputStream
import java.io.InputStream

/**
 * Decoder for Brotli compressed response data.
 * Handles "br" content encoding.
 *
 * @since 6.0.0
 */
@AutoService(ResponseDecoder::class)
@API(status = API.Status.INTERNAL, since = "6.0.0")
public class BrotliDecoder : ResponseDecoder {
    override val encodings: List<String>
        get() = listOf("br")

    override fun decodeStream(input: InputStream): InputStream {
        return BrotliInputStream(input)
    }
}
