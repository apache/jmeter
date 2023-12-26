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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Top5ErrorsSummaryDataTest {
    val sut = Top5ErrorsSummaryData()

    @Test
    fun `error and total count start at 0`() {
        assertEquals(0, sut.errors, "errors")
        assertEquals(0, sut.total, "total")
    }

    @Test
    fun `incErrors increments errors`() {
        sut.incErrors()
        assertEquals(1, sut.errors, "errors")
    }

    @Test
    fun `incTotal increments total`() {
        sut.incTotal()
        assertEquals(1, sut.total, "total")
    }

    @Test
    fun `when no errors are registered an array with null values is returned`() {
        assertArrayEquals(arrayOf<Array<Any>>(), sut.getTop5ErrorsMetrics(), "getTop5ErrorsMetrics")
    }

    @Test
    fun `error messages with the same frequency are preserved up until the size limit`() {
        val input = listOf("A", "B", "C", "D", "E", "F")
        input.forEach { sut.registerError(it) }
        assertArrayEquals(
            input.take(5).map { arrayOf(it, 1L) }.toTypedArray(),
            sut.getTop5ErrorsMetrics(),
            "registerErrors $input, then call getTop5ErrorsMetrics"
        )
    }

    @Test
    fun `error messages are sorted by size, descending`() {
        val input = listOf("A", "A", "A", "B", "B", "C")
        input.forEach { sut.registerError(it) }
        assertArrayEquals(
            arrayOf(
                arrayOf("A", 3L),
                arrayOf("B", 2L),
                arrayOf("C", 1L)
            ),
            sut.top5ErrorsMetrics,
            "registerErrors $input, then call getTop5ErrorsMetrics"
        )
    }
}
