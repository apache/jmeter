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

package org.apache.jmeter.protocol.http.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class ConversionUtilsTest {
    companion object {
        @JvmStatic
        fun percentEncodeValues() =
            listOf(
                arguments("hello", "hello"),
                arguments("%", "%"),
                arguments("\"", "%22"),
                arguments(" ", " "),
                arguments("\r", "%0D"),
                arguments("\n", "%0A"),
                arguments("\r\r\n\n", "%0D%0D%0A%0A"),
                arguments("😃", "😃"),
                arguments("comment ça va?", "comment ça va?"),
                arguments("quoted \"content\"", "quoted %22content%22"),
            )

        @JvmStatic
        fun htmlEntityValues() =
            listOf(
                arguments("Hello, 😃, world", "Hello, 😃, world", StandardCharsets.UTF_8),
                arguments("Hello, 😃, world", "Hello, &#128515, world", StandardCharsets.ISO_8859_1),
                arguments("丈, 😃, and नि", "丈, 😃, and नि", StandardCharsets.UTF_8),
                arguments("丈, 😃, and नि", "&#19976, &#128515, and &#2344&#2367", StandardCharsets.ISO_8859_1),
            )
    }

    @ParameterizedTest
    @MethodSource("percentEncodeValues")
    fun percentEncode(input: String, output: String) {
        assertEquals(output, ConversionUtils.percentEncode(input)) {
            "ConversionUtils.percentEncode($input)"
        }
    }

    @ParameterizedTest
    @MethodSource("htmlEntityValues")
    fun htmlEntities(input: String, output: String, charset: Charset) {
        assertEquals(output, ConversionUtils.encodeWithEntities(input, charset))
    }
}
