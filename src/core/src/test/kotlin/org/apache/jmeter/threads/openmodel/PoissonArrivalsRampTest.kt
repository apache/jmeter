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

package org.apache.jmeter.threads.openmodel

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Random
import kotlin.test.assertEquals

class PoissonArrivalsRampTest {
    class Case(
        val beginRate: Double,
        val endRate: Double,
        val duration: Double,
        val expected: String,
        val seed: Long = 0L,
    ) {
        override fun toString() = "beginRate=$beginRate, endRate=$endRate, duration=$duration, seed=$seed"
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Case(beginRate = 1.0, endRate = 1.0, duration = 1.0, "0.731"),
            Case(
                beginRate = 2.0, endRate = 2.0, duration = 1.0,
                """
                0.2405
                0.731
                """.trimIndent()
            ),
            Case(
                beginRate = 0.0, endRate = 4.0, duration = 4.0,
                """
                1.9618
                2.309
                2.4825
                2.9677
                3.092
                3.1935
                3.4199
                3.9696
                """.trimIndent()
            ),
            Case(
                beginRate = 5.0, endRate = 1.0, duration = 4.0,
                """
                0.0304
                0.1193
                0.2494
                0.908
                0.9621
                1.5175
                1.691
                1.9026
                2.2017
                2.5497
                2.5639
                2.9239
                """.trimIndent()
            ),
            Case(
                beginRate = 1.0, endRate = 5.0, duration = 4.0,
                """
                0.9621
                1.4361
                2.0974
                2.2017
                2.309
                2.4825
                2.5497
                2.9239
                3.092
                3.7506
                3.8807
                3.9696
                """.trimIndent()
            ),
            Case(
                beginRate = 1.0, endRate = 5.0, duration = 4.0, seed = 1L,
                expected = """
                0.3128
                0.8309
                1.3309
                1.6403
                2.358
                2.5209
                2.9235
                3.8721
                3.8779
                3.893
                3.9267
                3.935
                """.trimIndent()
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("data")
    fun test(case: Case) {
        val gen = PoissonArrivalsRamp()
        gen.prepare(beginRate = case.beginRate, endRate = case.endRate, duration = case.duration, random = Random(case.seed))
        val format = DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.ROOT))
        assertEquals(
            case.expected,
            gen.asSequence().joinToString("\n") { format.format(it) },
            case.toString()
        )
    }
}
