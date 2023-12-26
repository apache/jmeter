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

package org.apache.jmeter.control

import io.mockk.every
import io.mockk.mockk
import org.apache.jmeter.junit.stubs.TestSampler
import org.apache.jmeter.samplers.Sampler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

class RunTimeTest {
    val sut = RunTime()

    @Test
    @Disabled("It fails too often due to timing issues")
    fun `RunTime stops within a tolerance after specified runtime`() {
        sut.runtime = 1
        val runTimeMillis = 1000L
        val expectedLoops = 5
        val tolerance = ceil(0.1 * expectedLoops)
        val samplerWaitTime: Long = runTimeMillis / expectedLoops
        val samp1 = TestSampler("Sample 1", samplerWaitTime)
        val samp2 = TestSampler("Sample 2", samplerWaitTime)

        val sampler1Loops = 2
        val loop1 = LoopController().apply {
            loops = sampler1Loops
            setContinueForever(false)
            addTestElement(samp1)
        }

        val loop2 = LoopController().apply {
            loops = expectedLoops * 2
            setContinueForever(false)
            addTestElement(samp2)
        }

        sut.addTestElement(loop1)
        sut.addTestElement(loop2)
        sut.isRunningVersion = true
        loop1.isRunningVersion = true
        loop2.isRunningVersion = true
        sut.initialize()

        // when:
        var loopCount = 0
        val elapsed = measureTimeMillis {
            while (true) {
                val sampler = sut.next() ?: break
                loopCount++
                sampler.sample(null)
            }
        }
        // then:
        assertEquals(1, sut.iterCount, ".iterCount")
        assertEquals(expectedLoops.toDouble(), loopCount.toDouble(), tolerance, "loopCount")
        assertEquals(elapsed.toDouble(), runTimeMillis.toDouble(), tolerance * samplerWaitTime, "elapsedMillis")
        assertEquals(sampler1Loops, samp1.samples, "samp1.samples")
        assertEquals(
            (expectedLoops - sampler1Loops).toDouble(),
            samp2.samples.toDouble(),
            tolerance,
            "samp2.samples should be expectedLoops - sampler1Loops"
        )
    }

    @Test
    fun `Immediately returns null when runtime is set to 0`() {
        sut.runtime = 0
        sut.addTestElement(mockk<Sampler>())

        assertNull(sut.next(), ".next()")
    }

    @Test
    fun `Immediately returns null if only Controllers are present`() {
        sut.runtime = 10
        sut.addTestElement(
            mockk<Controller> {
                every { next() } returns null
                every { isDone } returns true
            }
        )
        sut.addTestElement(
            mockk<Controller> {
                every { next() } returns null
                every { isDone } returns true
            }
        )

        assertNull(sut.next(), ".next()")
    }

    @Test
    fun `within time limit samplers are returned until empty`() {
        val mockSampler = mockk<Sampler>()
        sut.runtime = 10
        sut.addTestElement(mockSampler)
        sut.addTestElement(mockSampler)

        assertEquals(
            listOf(mockSampler, mockSampler, null),
            listOf(sut.next(), sut.next(), sut.next()),
            "there are two elements, so first two .next() should return them, then null"
        )
    }

    @Test
    fun `RunTime immediately returns null when there are no test elements`() {
        sut.runtime = 10
        assertNull(sut.next(), ".next()")
    }
}
