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

class ThreadScheduleTest {
    class Case(val input: String, val expected: String) {
        override fun toString() = input
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Case("rate(0/min)", "[Rate(0)]"),
            Case("rate(36000/hour)", "[Rate(10)]"),
            Case("random_arrivals(0) /* 0 does not require time unit */", "[Arrivals(type=RANDOM, duration=0)]"),
            Case(
                "rate(1/sec) random_arrivals(2 min) pause(3 min) random_arrivals(4 sec)",
                "[Rate(1), Arrivals(type=RANDOM, duration=120), Rate(1), Rate(0), Arrivals(type=EVEN, duration=180), Rate(0), Rate(1), Arrivals(type=RANDOM, duration=4)]"
            ),
            Case("rate(50.1/sec)", "[Rate(50.1)]"),
            Case("even_arrivals(50 min)", "[Arrivals(type=EVEN, duration=3000)]"),
            Case("even_arrivals(2d 1m 30s)", "[Arrivals(type=EVEN, duration=${2 * 86400 + 60 + 30})]"),
            Case(
                "rate(50/min) even_arrivals(2 hour) rate(60/min)",
                "[Rate(0.8), Arrivals(type=EVEN, duration=7200), Rate(1)]"
            ),
            Case(
                "rate(0 per min) even_arrivals(30 min) rate(50 per min) random_arrivals(4 min) rate(200 per min) random_arrivals(10 sec)",
                "[Rate(0), Arrivals(type=EVEN, duration=1800), Rate(0.8), Arrivals(type=RANDOM, duration=240), Rate(3.3), Arrivals(type=RANDOM, duration=10)]"
            )
        )
    }

    @ParameterizedTest
    @MethodSource("data")
    fun test(case: Case) {
        assertEquals(case.expected, ThreadSchedule(case.input).toString(), case.input)
    }
}
