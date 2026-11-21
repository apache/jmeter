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
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class ConcurrencyGateTest {
    private val farFuture get() = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)

    private fun gateFor(schedule: String, hardLimit: Int = Int.MAX_VALUE) =
        ConcurrencyGate(ConcurrencyLimit.of(ThreadSchedule(schedule)), hardLimit, System.currentTimeMillis())

    @Test
    fun `unlimited gate never blocks`() {
        val gate = gateFor("rate(1/sec) random_arrivals(1 hour)")
        assertTrue(gate.isUnlimited, "isUnlimited")
        repeat(1000) {
            assertTrue(gate.acquire(farFuture), "acquire should not block when unlimited")
        }
    }

    @Test
    fun `hard limit applies without a concurrency step`() {
        val gate = gateFor("rate(1/sec) random_arrivals(1 hour)", hardLimit = 1)
        assertFalse(gate.isUnlimited, "isUnlimited")
        assertTrue(gate.acquire(farFuture), "first slot")
        assertFalse(gate.acquire(System.currentTimeMillis() - 1), "second slot is dropped past the deadline")
        assertEquals(1, gate.droppedArrivals)
    }

    @Test
    fun `arrival is dropped when the deadline passes`() {
        val gate = gateFor("concurrency(1) rate(1/sec) random_arrivals(1 hour)")
        assertTrue(gate.acquire(farFuture), "first slot is free")
        assertFalse(gate.acquire(System.currentTimeMillis() - 1), "second slot cannot be reserved")
        assertEquals(1, gate.droppedArrivals)
    }

    @Test
    fun `released slot unblocks a waiting producer`() {
        val gate = gateFor("concurrency(1) rate(1/sec) random_arrivals(1 hour)")
        assertTrue(gate.acquire(farFuture), "first slot")
        val acquired = AtomicReference<Boolean?>()
        val waiter = thread { acquired.set(gate.acquire(farFuture)) }
        Thread.sleep(200)
        assertEquals(null, acquired.get(), "the waiter should still be blocked")
        gate.release()
        waiter.join(2000)
        assertEquals(true, acquired.get(), "the waiter should acquire the freed slot")
        assertTrue(gate.delayedArrivals >= 1, "the delayed arrival should be counted")
    }

    @Test
    @Timeout(5, unit = TimeUnit.SECONDS)
    fun `blocked producer is interruptible`() {
        val gate = gateFor("concurrency(1) rate(1/sec) random_arrivals(1 hour)")
        assertTrue(gate.acquire(farFuture), "first slot")
        val error = AtomicReference<Throwable?>()
        val waiter = thread {
            try {
                gate.acquire(farFuture)
            } catch (t: InterruptedException) {
                error.set(t)
            }
        }
        Thread.sleep(200)
        waiter.interrupt()
        waiter.join(2000)
        assertTrue(error.get() is InterruptedException, "acquire should throw InterruptedException when interrupted")
    }
}
