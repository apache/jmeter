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
import java.util.Random

class ThreadScheduleProcessGeneratorTest {
    class Case(val schedule: String, val expected: String) {
        override fun toString() = schedule
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Case(
                """
                // total duration is 2+3+1+2 => 8 sec
                // 1/sec * 2 sec => 2 events at 1 and 2
                rate(1/sec) even_arrivals(2 sec) rate(1/sec)
                // 2/sec * 3sec => 6 events at 2.5, 3, 3.5, 4, 4.5, 5
                rate(2/sec) even_arrivals(3 sec) rate(2/sec)
                // (2/sec + 5/sec)/2 * 1 sec => 3.5 events (truncated to 3) in 5..6
                even_arrivals(1 sec) rate(5/sec)
                // (5/sec + 3/sec)/2 * 2 sec => 8 events in 6..8
                even_arrivals(2 sec) rate(3/sec)
                """.trimIndent(),
                """
                totalDuration: 8
                events:
                  0
                  1
                  2
                  2.5
                  3
                  3.5
                  4
                  4.5
                  5
                  5.3874
                  5.6667
                  6
                  6.2042
                  6.4174
                  6.6411
                  6.8769
                  7.127
                  7.3944
                  7.6834
                """.trimIndent()
            ),
            Case(
                """
                // total duration is 2+3+10+4 => 19 sec
                // (0/sec + 2/sec)/2 * sec => 2 events at 0..2
                rate(0) random_arrivals(2 sec) rate(2/sec)
                // 2/sec * 3sec => 6 events at 2..5
                random_arrivals(3 sec)
                pause(10 sec)
                // (2/sec + 1/sec)/2 * 4 sec => 6 events at 15..19
                random_arrivals(4 sec) rate(1/sec)
                """.trimIndent(),
                """
                totalDuration: 19
                events:
                  0.9809
                  1.7099
                  2.9997
                  3.1556
                  3.6513
                  3.7926
                  3.9123
                  4.9545
                  15.5156
                  16.0998
                  17.4685
                  18.3902
                  18.5167
                  18.765
                """.trimIndent()
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("data")
    fun scheduleTest(case: Case) {
        val rnd = Random(0)
        val schedule = case.schedule
        val gen = ThreadScheduleProcessGenerator(rnd, ThreadSchedule(schedule))
        val format = DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.ROOT))
        assertEquals(
            case.expected,
            """
            totalDuration: ${format.format(gen.totalDuration)}
            events:
            """.trimIndent() + "\n" + gen.asSequence().joinToString("\n") { "  " + format.format(it) },
            schedule
        )
    }
}
