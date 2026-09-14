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

import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jorphan.test.JMeterSerialTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sampler that records how many threads run [sample] at the same time, so a test can assert the
 * concurrency limit is respected.
 */
class ConcurrencyProbe : AbstractSampler() {
    abstract class Schema : TestElementSchema() {
        companion object INSTANCE : Schema()

        val sleep by long("ConcurrencyProbe.sleep")
    }

    var sleepMillis by Schema.sleep

    override fun sample(e: Entry?): SampleResult {
        val res = SampleResult()
        res.sampleStart()
        val concurrent = current.incrementAndGet()
        maxConcurrency.accumulateAndGet(concurrent) { a, b -> maxOf(a, b) }
        try {
            Thread.sleep(sleepMillis)
            res.responseCode = "OK"
        } catch (t: InterruptedException) {
            res.responseCode = "InterruptedException"
        } finally {
            current.decrementAndGet()
            res.sampleEnd()
        }
        return res
    }

    companion object {
        val current = AtomicInteger()
        val maxConcurrency = AtomicInteger()

        fun reset() {
            current.set(0)
            maxConcurrency.set(0)
        }
    }
}

class ConcurrencyLimitBehaviorTest : JMeterTestCase(), JMeterSerialTest {
    @Test
    @Timeout(30, unit = TimeUnit.SECONDS)
    fun `concurrency caps the number of simultaneous threads`() {
        ConcurrencyProbe.reset()
        var group: OpenModelThreadGroup? = null

        val tree = testTree {
            TestPlan::class {
                OpenModelThreadGroup::class {
                    group = this
                    name = "omtg"
                    // ~50 arrivals in one second, but only 2 threads may run at a time.
                    // The pause lets the delayed arrivals drain before the schedule ends.
                    scheduleString = "concurrency(2) rate(50/sec) random_arrivals(1 sec) pause(10 sec)"
                    ConcurrencyProbe::class {
                        sleepMillis = 200
                    }
                }
            }
        }
        StandardJMeterEngine().apply {
            configure(tree)
            runTest()
            awaitTermination(Duration.ofSeconds(30))
        }

        assertTrue(
            ConcurrencyProbe.maxConcurrency.get() <= 2,
            "At most 2 threads should run at a time, but observed ${ConcurrencyProbe.maxConcurrency.get()}"
        )
        assertTrue(
            group!!.getDelayedArrivals() > 0,
            "Some arrivals should be delayed because the rate exceeds the concurrency limit"
        )
    }
}
