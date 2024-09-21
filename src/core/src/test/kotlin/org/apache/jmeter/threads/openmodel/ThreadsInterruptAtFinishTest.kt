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

import org.apache.jmeter.control.LoopController
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.test.samplers.ThreadSleep
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.treebuilder.TreeBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class ThreadsInterruptAtFinishTest : JMeterTestCase() {
    @Test
    @Timeout(10, unit = TimeUnit.SECONDS)
    fun `openmodel interrupts sleep`() {
        assertInterruptionsPresent {
            OpenModelThreadGroup::class {
                scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(1 s)"
                ThreadSleep::class {
                    duration = 20.seconds
                }
            }
        }
    }

    @Test
    @Disabled("ThreadGroup does not interrupt threads automatically. TODO: make ThreadGroup stop threads on scheduler finish")
    @Timeout(5, unit = TimeUnit.SECONDS)
    fun `threadgroup interrupts sleep`() {
        assertInterruptionsPresent {
            ThreadGroup::class {
                numThreads = 5
                rampUp = 0
                scheduler = true
                delay = 0
                duration = 1
                setSamplerController(
                    LoopController().apply {
                        loops = 1
                        setContinueForever(false)
                    }
                )

                ThreadSleep::class {
                    duration = 20.seconds
                }
            }
        }
    }

    private fun assertInterruptionsPresent(testBody: TreeBuilder.() -> Unit) {
        val events = executePlanAndCollectEvents(10.seconds, testBody)
        val eventCodes = events.map { it.result.responseCode }

        if ("InterruptedException" !in eventCodes) {
            fail("There should be at least one result with InterruptedException code. Actual response codes were: $eventCodes")
        }
    }
}
