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
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.test.samplers.CollectSamplesListener
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jorphan.test.JMeterSerialTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class KotlinDslRunTest : JMeterTestCase(), JMeterSerialTest {

    @Test
    fun `run Kotlin DSL plan and write results`(@TempDir tempDir: Path) {
        val jtlFile = tempDir.resolve("results.jtl")
        val listener = CollectSamplesListener()
        val collector = ResultCollector().apply {
            filename = jtlFile.toString()
        }

        val tree = testTree {
            TestPlan::class {
                +collector

                OpenModelThreadGroup::class {
                    name = "Thread Group"
                    scheduleString =
                        "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
                    +listener
                    DebugSampler::class {
                        name = "Kotlin DSL sample"
                        setDisplayJMeterProperties(false)
                        setDisplayJMeterVariables(false)
                        setDisplaySystemProperties(false)
                    }
                }
            }
        }

        val engine = StandardJMeterEngine()
        engine.configure(tree)
        engine.runTest()
        engine.awaitTermination(Duration.ofSeconds(10))
        val events = listener.events

        assertEquals(5, events.size, "Expected five samples")
        assertTrue(
            events.all { it.result.isSuccessful },
            "All samples should succeed"
        )
        assertTrue(Files.exists(jtlFile), "JTL file should be created")
        val results = Files.readString(jtlFile)
        assertTrue(
            results.contains("Kotlin DSL sample"),
            "JTL should contain the executed sample"
        )
    }
}
