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

package org.apache.jmeter.dsl

import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jorphan.test.JMeterSerialTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class KotlinDslRunTest : JMeterTestCase(), JMeterSerialTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `run Kotlin DSL plan and write results`() {
        val jtlFile = tempDir.resolve("results.jtl")

        val tree = testTree {
            TestPlan::class {
                ResultCollector::class {
                    filename = jtlFile.toString()
                }
                OpenModelThreadGroup::class {
                    name = "Thread Group"
                    // 5 requests within 100 ms, then a pause so all the threads can finish
                    scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
                    DebugSampler::class {
                        name = "Kotlin DSL sample"
                    }
                }
            }
        }
        StandardJMeterEngine().apply {
            configure(tree)
            runTest()
            awaitTermination(Duration.ofSeconds(10))
        }

        assertEquals(
            5,
            Files.readAllLines(jtlFile).count { it.contains("Kotlin DSL sample") },
            "The JTL file should contain the 5 samples"
        )
    }
}
