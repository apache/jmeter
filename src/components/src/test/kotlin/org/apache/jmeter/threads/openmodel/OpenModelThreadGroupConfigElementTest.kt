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
import org.apache.jmeter.testelement.property.TestElementProperty
import org.apache.jmeter.threads.AbstractThreadGroup
import org.apache.jorphan.collections.ListedHashTree
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.time.Duration

class OpenModelThreadGroupConfigElementTest : JMeterTestCase() {
    /**
     * Create Test Plan with Open Model Thread Group and Counter Config.
     */
    @Test
    @Ignore("Sometimes the listener gets no results for unknown reason")
    fun `ensure thread group initializes counter only once for each thread`() {
        val listener = TestTransactionController.TestSampleListener()

        val tree = ListedHashTree().apply {
            add(TestPlan()).apply {
                val threadGroup = OpenModelThreadGroup().apply {
                    name = "Thread Group"
                    scheduleString = "rate(5 / sec) random_arrivals(1 sec)"
                    setProperty(
                        TestElementProperty(
                            AbstractThreadGroup.MAIN_CONTROLLER, OpenModelThreadGroupController()
                        )
                    )
                }
                add(threadGroup).apply {
                    add(listener)
                    add(
                        CounterConfig().apply {
                            varName = "counter"
                            increment = 1
                        }
                    )
                    add(
                        DebugSampler().apply {
                            name = "\${counter}"
                            isDisplayJMeterProperties = false
                            isDisplayJMeterVariables = false
                            isDisplaySystemProperties = false
                        }
                    )
                }
            }
        }

        StandardJMeterEngine().apply {
            configure(tree)
            runTest()
            awaitTermination(Duration.ofSeconds(10))
        }

        // There's no guarantee that thread execute exactly in order, so we sort
        // the labels to avoid test failure in case the thread execute out of order.
        val actual = listener.events.map { it.result.sampleLabel }.sorted()

        // Use toString for better error message
        Assert.assertEquals(
            "Counter values should be consequent",
            "0\n1\n2\n3\n4",
            actual.joinToString("\n")
        )
    }
}
