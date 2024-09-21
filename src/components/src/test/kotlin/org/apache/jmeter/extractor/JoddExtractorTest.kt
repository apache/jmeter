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

package org.apache.jmeter.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class JoddExtractorTest {
    data class ExtractCase(
        val expression: String,
        val attribute: String,
        val matchNumber: Int,
        val expectedList: List<String>,
        val found: Int,
        val expected: Int,
        val cacheKey: String
    )

    companion object {
        @JvmStatic
        fun extractCases() = listOf(
            ExtractCase("p", "", 1, listOf("Some text"), -1, 0, "key"),
            ExtractCase("h1[class=title]", "class", 1, listOf("title"), -1, 0, "key"),
            ExtractCase("h1", "", 0, listOf("TestTitle", "AnotherTitle"), -1, 1, "key"),
            ExtractCase("notthere", "", 0, listOf(), -1, -1, "key"),
        )
    }

    @ParameterizedTest
    @MethodSource("extractCases")
    fun extract(case: ExtractCase) {
        val resultList = mutableListOf<String>()
        val input = /* language=xml */
            """
            <html>
              <head><title>Test</title></head>
              <body>
                <h1 class="title">TestTitle</h1>
                <p>Some text</p>
                <h1>AnotherTitle</h1>
              </body>
            </html>
            """.trimIndent()
        val foundCount = JoddExtractor().extract(
            case.expression,
            case.attribute,
            case.matchNumber,
            input,
            resultList,
            case.found,
            case.cacheKey
        )
        assertEquals(case.expectedList, resultList, "resultList")
        assertEquals(case.expected, foundCount, "foundCount")
    }
}
