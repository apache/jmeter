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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConcurrencyLimitTest {
    @Test
    fun `no concurrency step is unlimited`() {
        val limit = ConcurrencyLimit.of(ThreadSchedule("rate(5/sec) random_arrivals(1 min)"))
        assertTrue(limit.isUnlimited, "isUnlimited")
        assertEquals(Int.MAX_VALUE, limit.limitAt(0.0))
        assertEquals(Double.POSITIVE_INFINITY, limit.nextChangeAfter(0.0))
    }

    @Test
    fun `constant concurrency`() {
        val limit = ConcurrencyLimit.of(ThreadSchedule("concurrency(10) rate(5/sec) random_arrivals(1 min)"))
        assertFalse(limit.isUnlimited, "isUnlimited")
        assertEquals(10, limit.limitAt(0.0))
        assertEquals(10, limit.limitAt(30.0))
        assertEquals(10, limit.limitAt(120.0))
    }

    @Test
    fun `concurrency changes on the arrival boundary regardless of the generator position`() {
        // 60 seconds at limit 10, then 120 seconds at limit 20
        val limit = ConcurrencyLimit.of(
            ThreadSchedule(
                "concurrency(10) rate(5/sec) random_arrivals(1 min) concurrency(20) random_arrivals(2 min)"
            )
        )
        assertEquals(10, limit.limitAt(0.0), "at 0s")
        assertEquals(10, limit.limitAt(59.9), "just before the boundary")
        assertEquals(20, limit.limitAt(60.0), "at the boundary")
        assertEquals(20, limit.limitAt(200.0), "after the boundary")
        assertEquals(60.0, limit.nextChangeAfter(0.0), "next change from the start")
        assertEquals(Double.POSITIVE_INFINITY, limit.nextChangeAfter(60.0), "no further changes")
    }

    @Test
    fun `last concurrency step at the same time wins`() {
        val limit = ConcurrencyLimit.of(ThreadSchedule("concurrency(10) concurrency(20) rate(1/sec) random_arrivals(1 min)"))
        assertEquals(20, limit.limitAt(0.0))
    }
}
