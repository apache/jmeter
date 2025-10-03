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

package org.apache.jmeter.threads

import org.apache.jmeter.control.LoopController
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.test.samplers.ThreadSleep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class ThreadGroupTest : JMeterTestCase() {
    @Test
    fun `threadNum with trailing whitespace`() {
        val events = executePlanAndCollectEvents(10.seconds) {
            ThreadGroup::class {
                props {
                    it[numThreads] = "1 "
                }
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
                    duration = 0.seconds
                }
            }
        }
        assertEquals(1, events.size) {
            "ThreadGroup.threadNum has trailing whitespace, it should be trimmed, so one event should be generated. " +
                "Actual events are $events"
        }
    }
}
