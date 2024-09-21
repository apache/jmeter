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

import org.apache.jmeter.control.LoopController
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.treebuilder.TreeBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class ConstantThroughputTimerKtTest : JMeterTestCase() {
    fun TreeBuilder.oneRequest(body: ThreadGroup.() -> Unit) {
        ThreadGroup::class {
            numThreads = 1
            rampUp = 0
            setSamplerController(
                LoopController().apply {
                    loops = 1
                }
            )
            body()
        }
    }

    @Test
    fun `throughput as variable`() {
        val events = executePlanAndCollectEvents(5.seconds) {
            oneRequest {
                DebugSampler::class {
                    // This initializes the variable during the test execution
                    name = "\${__groovy( vars.put(\"throughput\"\\, \"10000\") )}"
                }
                DebugSampler::class {
                    ConstantThroughputTimer::class {
                        setProperty(
                            "throughput",
                            "\${__groovy( vars.get(\"throughput\").toDouble() )}"
                        )
                        setProperty("calcMode", 0)
                    }
                }
            }
        }
        assertEquals(
            2,
            events.size,
            "Test should complete within reasonable time, and the test has 2 debug samplers, so we expect 2 events"
        )
    }
}
