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

package org.apache.jmeter.timers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UniformRandomTimerTest {
    data class RangeCase(val delay: String, val range: Double, val min: Long, val max: Long)

    val sut = UniformRandomTimer()

    companion object {
        @JvmStatic
        fun rangeCases() = listOf(
            RangeCase("1", 10.5, 1, 11),
            RangeCase("1", 0.1, 1, 1),
            RangeCase("0", -50.0, 0, 50),
        )
    }

    @Test
    fun `default delay is 0`() {
        assertEquals(0L, sut.delay(), ".delay()")
    }

    @Test
    fun `default range is 0`() {
        assertEquals(0.0, sut.range, ".range")
    }

    @Test
    fun `delay can be set via a String`() {
        sut.delay = "1"
        assertEquals(1L, sut.delay(), ".delay()")
    }

    @ParameterizedTest
    @MethodSource("rangeCases")
    fun `computed delay should be within range`(case: RangeCase) {
        sut.delay = case.delay
        sut.range = case.range

        val computedDelay = sut.delay()

        if (computedDelay < case.min || computedDelay > case.max) {
            fail("computed delay $computedDelay should be within [${case.min}..${case.max}]")
        }
    }
}
