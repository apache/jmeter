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

package org.apache.jorphan.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.charset.Charset

class TextFileTest {
    data class GetTextCase(val input: String, val charset: Charset?)
    companion object {
        @JvmStatic
        fun getTextCases() = listOf(
            GetTextCase("", Charsets.UTF_8),
            GetTextCase("a\nb\nc", null),
            GetTextCase("\"a\nb\nc\n", null),
            GetTextCase("a\nb\nc", Charsets.UTF_8),
            GetTextCase("\"a\nb\nc\n", Charsets.UTF_8),
            GetTextCase("a\nb\nc", Charsets.UTF_16),
            GetTextCase("\"a\nb\nc\n", Charsets.UTF_16),
            GetTextCase("a\nb\nc", Charsets.ISO_8859_1),
            GetTextCase("\"a\nb\nc\n", Charsets.ISO_8859_1),
            GetTextCase("丈, 😃, and नि", Charsets.UTF_8),
            GetTextCase("丈, 😃, and नि", Charsets.UTF_16),
        )
    }

    @TempDir
    lateinit var tempDir: File

    @ParameterizedTest
    @MethodSource("getTextCases")
    fun getText(case: GetTextCase) {
        val tmpPath = tempDir.resolve("file.txt")
        tmpPath.writeText(case.input, case.charset ?: Charsets.UTF_8)
        assertEquals(case.input, TextFile(tmpPath, case.charset?.name()).text) {
            "TextFile(tmpPath, encoding=${case.charset?.name()}).getText()"
        }
    }

    @ParameterizedTest
    @MethodSource("getTextCases")
    fun setText(case: GetTextCase) {
        val tmpPath = tempDir.resolve("file.txt")
        TextFile(tmpPath, case.charset?.name()).text = case.input
        assertEquals(case.input, tmpPath.readText(case.charset ?: Charsets.UTF_8)) {
            "contents of TextFile(tmpPath, encoding=${case.charset?.name()}).setText(${case.input})"
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "invalid encoding"])
    fun `getText throws exception with invalid encoding`(charset: String) {
        val tmpFile = tempDir.resolve("file.txt")
        tmpFile.writeBytes(byteArrayOf())
        val file = TextFile(tmpFile, charset)
        assertThrows<IllegalArgumentException> {
            file.text
        }
    }
}
