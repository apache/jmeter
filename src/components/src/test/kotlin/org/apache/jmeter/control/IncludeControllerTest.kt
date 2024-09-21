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

package org.apache.jmeter.control

import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.sampler.DebugSampler
import org.apache.jmeter.save.SaveService
import org.apache.jmeter.test.assertions.executePlanAndCollectEvents
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.openmodel.OpenModelThreadGroup
import org.apache.jmeter.treebuilder.dsl.testTree
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.seconds

class IncludeControllerTest : JMeterTestCase() {
    @TempDir
    lateinit var tmpDir: Path

    @Test
    @Disabled("Variables in Include Controllers are not replaced yet")
    fun `include controller with variables`() {
        val includedTree = testTree {
            TestPlan::class {
                DebugSampler::class {
                    name = "imported sampler"
                }
            }
        }

        val includedFile = tmpDir.resolve("included.jmx")

        includedFile.outputStream().buffered().use {
            SaveService.saveTree(includedTree, it)
        }

        executePlanAndCollectEvents(5.seconds) {
            (currentElement as TestPlan).apply {
                arguments.addArgument("includePath", includedFile.absolutePathString())
            }

            OpenModelThreadGroup::class {
                scheduleString = "rate(50 / sec) random_arrivals(100 ms) pause(2 s)"
                IncludeController::class {
                    name = "Include Controller"
                    includePath = "\${includePath}"
                }
            }
        }

        // TODO: assert that includePath is replaced within IncludeController
    }
}
