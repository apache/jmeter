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

import org.apache.jmeter.control.TestTransactionController
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.modifiers.CounterConfig
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.treebuilder.dsl.testTree
import org.apache.jorphan.test.JMeterSerialTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration

class OpenModelThreadGroupConfigElementTest : JMeterTestCase(), JMeterSerialTest {
    /**
     * Create Test Plan with Open Model Thread Group and Counter Config.
     */
    @Test
    // Un-comment if you want try running the test multiple times locally:
    // @RepeatedTest(value = 10)
    fun `ensure thread group initializes counter only once for each thread`() {
        val listener = TestTransactionController.TestSampleListener()

        val tree = testTree {
            TestPlan::class {
                OpenModelThreadGroup::class {
                    name = "Thread Group"
                    // 5 samples within 100ms
                    // Then 2 sec pause to let all the threads to finish, especially the ones that start at 99ms
                    scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
                    listener()
                    CounterConfig::class {
                        varName = "counter"
                        increment = 1
                    }
                    DebugSampler::class {
                        name = "\${counter}"
                        isDisplayJMeterProperties = false
                        isDisplayJMeterVariables = false
                        isDisplaySystemProperties = false
                    }
                }
            }
        }
        StandardJMeterEngine().apply {
            configure(tree)
            runTest()
            awaitTermination(Duration.ofSeconds(10))
        }

        // There's no guarantee that threads execute exactly in order, so we sort
        // the labels to avoid test failure in case the thread execute out of order.
        val actual = listener.events.map { it.result.sampleLabel }.sorted()

        // Use toString for better error message
        Assertions.assertEquals(
            "0\n1\n2\n3\n4",
            actual.joinToString("\n"),
            "Test should produce 5 iterations, so \${counter} should yield 0..4"
        )
    }
}
