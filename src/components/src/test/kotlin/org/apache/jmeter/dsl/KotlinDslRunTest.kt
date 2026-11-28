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
import org.apache.jmeter.junit.JMeterTestUtils
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.test.JMeterSerialTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class KotlinDslRunTest : JMeterSerialTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `documented Kotlin DSL tree matches the tested tree`() {
        val jmeterRoot = Path.of(JMeterTestUtils.setupJMeterHome()).toAbsolutePath().normalize().parent
        val documentedSource = Files.readString(
            jmeterRoot.resolve("xdocs/usermanual/build-programmatic-test-plan.xml")
        ).substringAfter("anchor=\"running_test_plan\">")
            .substringAfter("<source>")
            .substringBefore("</source>")
        val testSource = Files.readString(
            jmeterRoot.resolve("src/components/src/test/kotlin/org/apache/jmeter/dsl/KotlinDslRunTest.kt")
        )

        assertEquals(normalizeTree(documentedSource), normalizeTree(testSource))
    }

    @Test
    fun `a ResultCollector in a Kotlin DSL plan writes one JTL row per sample`() {
        val jmeterBin = Path.of(JMeterTestUtils.setupJMeterHome())
        JMeterUtils.loadJMeterProperties(jmeterBin.resolve("jmeter.properties").toString())
        val jtlFile = tempDir.resolve("results.jtl")

        val tree = testTree {
            TestPlan::class {
                ResultCollector::class {
                    filename = jtlFile.toString()
                }
                OpenModelThreadGroup::class {
                    name = "Thread Group"
                    // 5 samples within 100 ms, then a pause so all the threads can finish
                    scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
                    DebugSampler::class {
                        name = "Kotlin DSL sample"
                    }
                }
            }
        }
        val engine = StandardJMeterEngine()
        engine.apply {
            configure(tree)
            runTest()
        }
        try {
            engine.awaitTermination(Duration.ofSeconds(10))
        } finally {
            if (engine.isActive) {
                engine.stopTest()
                engine.awaitTermination(Duration.ofSeconds(10))
            }
        }

        assertEquals(
            5,
            Files.readAllLines(jtlFile).count { it.contains("Kotlin DSL sample") },
            "The JTL file should contain the 5 samples"
        )
    }

    private fun normalizeTree(source: String): String {
        val start = source.indexOf("val tree = testTree")
        require(start >= 0) { "Could not find testTree declaration" }

        val openingBrace = source.indexOf('{', start)
        require(openingBrace >= 0) { "Could not find testTree block" }

        var depth = 0
        for (index in openingBrace until source.length) {
            when (source[index]) {
                '{' -> depth++
                '}' -> depth--
            }
            if (depth == 0) {
                return source.substring(start, index + 1).replace(Regex("\\s+"), " ").trim()
            }
        }
        error("Unterminated testTree block")
    }
}
