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

package org.apache.jmeter.functions

import org.apache.jmeter.engine.util.CompoundVariable
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class IterationCounterTest {

    @Test
    fun `Counter per thread counts for each thread`() {
        val context = JMeterContextService.getContext()
        context.variables = JMeterVariables()
        val counter = IterationCounter()
        counter.setParameters(listOf(CompoundVariable("true"), CompoundVariable("var")))

        thread(start = true) {
            repeat(7) { counter.execute(null, null) }
        }.join()

        repeat(10) { counter.execute(null, null) }

        Assertions.assertEquals("10", context.variables.get("var")) {
            "Only 10 executions happended in the current thread, so expecting 10. " +
                "Note there were 7 iterations in a different thread, however the counter should be per-thread"
        }
    }

    @Test
    fun `global Counter counts for all threads`() {
        val context = JMeterContextService.getContext()
        context.variables = JMeterVariables()
        val counter = IterationCounter()
        counter.setParameters(listOf(CompoundVariable("false"), CompoundVariable("var")))
        val nrOfThreads = 100
        val latch = CountDownLatch(nrOfThreads)

        repeat(nrOfThreads) {
            thread(start = true) {
                repeat(1000) { counter.execute(null, null) }
                latch.countDown()
            }
        }
        latch.await()
        counter.execute(null, null)

        Assertions.assertEquals("100001", context.variables.get("var")) {
            "$nrOfThreads should have increased the var by 1000, plus one in main thread"
        }
    }
}
