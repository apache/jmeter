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

package org.apache.jmeter.report.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ListResultDataTest {
    companion object {
        @JvmStatic
        fun addResultInputs() = listOf(
            null,
            ValueResultData(),
            ListResultData(),
        )
    }

    val sut = ListResultData()

    @Test
    fun `a new ListResultData is empty`() {
        assertEquals(0, sut.size)
        assertEquals(listOf<Any>(), sut.toList())
    }

    @ParameterizedTest
    @MethodSource("addResultInputs")
    fun addResult(input: ResultData?) {
        assertTrue(sut.addResult(input), "addResult should return true")
        assertEquals(listOf(input), sut.toList()) {
            "ListResultData().add($input).toList()"
        }
    }
}
