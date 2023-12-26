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

package org.apache.jmeter.assertions

import org.apache.jmeter.samplers.SampleResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MD5HexAssertionTest {
    val sut = MD5HexAssertion()

    data class MD5HexAssertionCase(
        val sampleData: String,
        val allowedHex: String? = null,
        val success: Boolean,
    )

    companion object {
        @JvmStatic
        fun md5Cases() = listOf(
            // success
            MD5HexAssertionCase("anything", "f0e166dc34d14d6c228ffac576c9a43c", true),
            MD5HexAssertionCase("anything", "F0e166Dc34D14d6c228ffac576c9a43c", true),
            // failure
            MD5HexAssertionCase("", "", false),
            MD5HexAssertionCase("anything", "anything", false),
            MD5HexAssertionCase("anything", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false),
        )
    }

    @ParameterizedTest
    @MethodSource("md5Cases")
    fun md5HexAssertion(case: MD5HexAssertionCase) {
        sut.allowedMD5Hex = case.allowedHex
        val result = sut.getResult(sampleResult(case.sampleData))

        if (case.success) {
            assertFalse(result.isFailure, ".isFailure()")
            assertFalse(result.isError, ".isError()")
            assertNull(result.failureMessage, "result.failureMessage")
        } else {
            assertTrue(result.isFailure, ".isFailure()")
            assertTrue(result.failureMessage.isNotBlank(), "result.failureMessage should not be blank for failure case")
        }
    }

    @Test
    fun `empty array has MD5 hash of D41D8CD98F00B204E9800998ECF8427E`() {
        val emptyByteArray = byteArrayOf()
        assertEquals(
            "D41D8CD98F00B204E9800998ECF8427E",
            MD5HexAssertion.md5Hex(emptyByteArray).uppercase()
        )
    }
}

private fun sampleResult(data: String) =
    SampleResult().apply {
        responseData = data.toByteArray()
    }
