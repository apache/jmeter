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
import org.apiguardian.api.API
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Decoder for gzip compressed response data.
 * Handles both "gzip" and "x-gzip" content encodings.
 *
 * @since 6.0.0
 */
@API(status = API.Status.INTERNAL, since = "6.0.0")
public class GzipDecoder : ResponseDecoder {
    override val encodings: List<String>
        get() = listOf("gzip", "x-gzip")

    override fun decodeStream(input: InputStream): InputStream {
        return GZIPInputStream(input)
    }
}
