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

package org.apache.jmeter.report.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SampleMetadataParserTest {
    data class ParseCase(val separator: Char, val headers: String, val expected: List<String>)
    companion object {
        @JvmStatic
        fun headerCases() = listOf(
            ParseCase(';', "a;b;c;d;e", listOf("a", "b", "c", "d", "e")),
            ParseCase(',', "a|b|c|d|e", listOf("a", "b", "c", "d", "e")),
            ParseCase(',', "aa|bb|cc|dd|eef", listOf("aa", "bb", "cc", "dd", "eef")),
            ParseCase('&', "a&b&c&d&e", listOf("a", "b", "c", "d", "e")),
            ParseCase('\t', "a\tb c\td\te", listOf("a", "b c", "d", "e")),
            ParseCase(',', "abcdef", listOf("abcdef")),
            // Wrong separator
            ParseCase(',', "a;b;c;d;e", listOf("a", "b", "c", "d", "e")),
            ParseCase(',', "a|b|c|d|e", listOf("a", "b", "c", "d", "e")),
            ParseCase(',', "aa|bb|cc|dd|eef", listOf("aa", "bb", "cc", "dd", "eef")),
            ParseCase(',', "a&b&c&d&e", listOf("a", "b", "c", "d", "e")),
            ParseCase(',', "a\tb c\td\te", listOf("a", "b c", "d", "e")),
            ParseCase(',', "abcdef", listOf("abcdef")),
        )
    }

    @ParameterizedTest
    @MethodSource("headerCases")
    fun parseHeaders(case: ParseCase) {
        val result = SampleMetaDataParser(case.separator).parse(case.headers).columns
        Assertions.assertEquals(case.expected, result) {
            "SampleMetaDataParser(${case.separator}).parse(${case.headers})"
        }
    }
}
