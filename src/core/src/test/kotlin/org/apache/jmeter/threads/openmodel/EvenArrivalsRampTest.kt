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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class EvenArrivalsRampTest {
    class Case(val beginRate: Double, val endRate: Double, val duration: Double, val expected: String) {
        override fun toString() = "beginRate=$beginRate, endRate=$endRate, duration=$duration"
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Case(beginRate = 1.0, endRate = 1.0, duration = 1.0, "0"),
            Case(
                beginRate = 2.0, endRate = 2.0, duration = 1.0,
                """
                0
                0.5
                """.trimIndent()
            ),
            Case(
                beginRate = 0.0, endRate = 4.0, duration = 4.0,
                """
                0
                1.4142
                2
                2.4495
                2.8284
                3.1623
                3.4641
                3.7417
                """.trimIndent()
            ),
            Case(
                beginRate = 5.0, endRate = 1.0, duration = 4.0,
                """
                0
                0.2042
                0.4174
                0.6411
                0.8769
                1.127
                1.3944
                1.6834
                2
                2.3542
                2.7639
                3.2679
                """.trimIndent()
            ),
            Case(
                beginRate = 1.0, endRate = 5.0, duration = 4.0,
                """
                0
                0.7321
                1.2361
                1.6458
                2
                2.3166
                2.6056
                2.873
                3.1231
                3.3589
                3.5826
                3.7958
                """.trimIndent()
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("data")
    fun test(case: Case) {
        val gen = EvenArrivalsRamp()
        gen.prepare(beginRate = case.beginRate, endRate = case.endRate, duration = case.duration)
        val format = DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.ROOT))
        assertEquals(
            case.expected,
            gen.asSequence().joinToString("\n") { format.format(it) },
            case.toString()
        )
    }
}
