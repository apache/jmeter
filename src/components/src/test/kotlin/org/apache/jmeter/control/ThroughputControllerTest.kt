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

import org.apache.jmeter.junit.stubs.TestSampler
import org.apache.jmeter.threads.TestCompiler
import org.apache.jmeter.treebuilder.dsl.testTree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ThroughputControllerTest {
    val sut = ThroughputController()

    @BeforeEach
    fun setup() {
        sut.addTestElement(TestSampler("one"))
        sut.addTestElement(TestSampler("two"))
    }

    @Test
    fun `TC isDone is true`() {
        val newTC = ThroughputController()
        assertTrue(newTC.isDone, ".isDone")
    }

    @Test
    fun `2 maxThroughput runs samplers inside the TC only twice`() {
        sut.style = ThroughputController.BYNUMBER
        sut.setMaxThroughput(2)

        val expectedNames =
            listOf(
                "zero", "one", "two", "three",
                "zero", "one", "two", "three",
                "zero", "three",
                "zero", "three",
                "zero", "three",
            )

        val loop = createLoopController(5)

        val actualNames = expectedNames.map {
            loop.next()?.name
        }

        assertEquals(expectedNames, actualNames, "actualNames")
        assertNull(loop.next(), "loop.next()")
        assertTrue(sut.isDone, ".isDone")
        sut.testEnded()
    }

    @Test
    fun `0 maxThroughput does not run any samplers inside the TC`() {
        sut.style = ThroughputController.BYNUMBER
        sut.setMaxThroughput(0)
        val loops = 3
        val loop = createLoopController(loops)
        val expectedNames = (1..loops).flatMap { listOf("zero", "three") }

        val actualNames = expectedNames.map {
            loop.next().name
        }

        assertEquals(expectedNames, actualNames, "actualNames")
        assertNull(loop.next(), "loop.next()")
        sut.testEnded()
    }

    /**
     * <pre>
     *   - innerLoop
     *     - ThroughputController (sut)
     *        - sampler one
     *        - sampler two
     * </pre>
     */
    @Test
    fun `0 maxThroughput does not run any sampler inside the TC and does not cause StackOverFlowError`() {
        sut.style = ThroughputController.BYNUMBER
        sut.setMaxThroughput(0)

        val innerLoop = LoopController().apply {
            setLoops(10000)
            addTestElement(sut)
            addIterationListener(sut)
            initialize()
            isRunningVersion = true
        }
        sut.testStarted()
        sut.isRunningVersion = true

        assertNull(innerLoop.next(), "innerLoop.next()")
        assertNull(innerLoop.next(), "innerLoop.next(), second call")
        sut.testEnded()
    }

    /**
     * <pre>
     *   - innerLoop
     *     - ThroughputController (sut)
     *        - sampler one
     *        - sampler two
     * </pre>
     */
    @Test
    fun `0 percentThroughput does not run any sampler inside the TC and does not cause StackOverFlowError`() {
        sut.style = ThroughputController.BYPERCENT
        sut.percentThroughput = "0.0"

        val innerLoop = LoopController().apply {
            loops = 10000
            addTestElement(sut)
            addIterationListener(sut)
            initialize()
            isRunningVersion = true
        }
        sut.testStarted()
        sut.isRunningVersion = true

        assertNull(innerLoop.next(), "innerLoop.next()")
        assertNull(innerLoop.next(), "innerLoop.next(), second call")

        sut.testEnded()
    }

    @Test
    fun `33 percentThroughput will run all the samplers inside the TC every 3 iterations`() {
        sut.style = ThroughputController.BYPERCENT
        sut.setPercentThroughput(33.33f)

        val loop = createLoopController(9)
        // Expected results established using the DDA algorithm - see:
        // http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/drawline.htm
        val expectedNames = listOf(
            "zero", "three", // 0/1 vs. 1/1 -> 0 is closer to 33.33
            "zero", "one", "two", "three", // 0/2 vs. 1/2 -> 50.0 is closer to 33.33
            "zero", "three", // 1/3 vs. 2/3 -> 33.33 is closer to 33.33
            "zero", "three", // 1/4 vs. 2/4 -> 25.0 is closer to 33.33
            "zero", "one", "two", "three", // 1/5 vs. 2/5 -> 40.0 is closer to 33.33
            "zero", "three", // 2/6 vs. 3/6 -> 33.33 is closer to 33.33
            "zero", "three", // 2/7 vs. 3/7 -> 0.2857 is closer to 33.33
            "zero", "one", "two", "three", // 2/8 vs. 3/8 -> 0.375 is closer to 33.33
            "zero", "three", // ...
        )
        val actualNames = expectedNames.map {
            loop.next()?.name
        }
        assertEquals(expectedNames, actualNames, "actualNames")
        assertNull(loop.next(), "loop.next()")
        sut.testEnded()
    }

    @Test
    fun `0 percentThroughput does not run any samplers inside the TC`() {
        sut.style = ThroughputController.BYPERCENT
        sut.setPercentThroughput(0.0f)

        val loops = 3
        val loop = createLoopController(loops)

        val expectedNames = (1..loops).flatMap { listOf("zero", "three") }
        val actualNames = expectedNames.map {
            loop.next().name
        }

        assertEquals(expectedNames, actualNames, "actualNames")
        assertNull(loop.next(), "loop.next()")
        sut.testEnded()
    }

    @Test
    fun `100 percentThroughput always runs all samplers inside the TC`() {
        sut.style = ThroughputController.BYPERCENT
        sut.setPercentThroughput(100.0f)

        val loops = 3
        val loop = createLoopController(loops)

        val expectedNames = (1..loops).flatMap { listOf("zero", "one", "two", "three") }
        val actualNames = expectedNames.map {
            loop.next()?.name
        }
        assertEquals(expectedNames, actualNames, "actualNames")
        assertNull(loop.next(), "loop.next()")
        sut.testEnded()
    }

    /**
     * Create a LoopController, executed once, which contains an inner loop that
     * runs [innerLoops] times, this inner loop contains a TestSampler ("zero")
     * the [ThroughputController] which contains two test samplers
     * ("one" and "two") followed by a final TestSampler ("three"):
     *
     * <pre>
     * - outerLoop
     *   - innerLoop
     *     - sampler zero
     *     - ThroughputController (sut)
     *        - sampler one
     *        - sampler two
     *     - sampler three
     * </pre>
     *
     * @param innerLoops number of times to loop the [ThroughputController]
     * @return the [LoopController]
     */
    private fun createLoopController(innerLoops: Int): LoopController {
        sut.testStarted()
        val tree = testTree {
            LoopController::class {
                loops = 1
                LoopController::class {
                    loops = innerLoops
                    +TestSampler("zero")
                    +sut
                    +TestSampler("three")
                }
            }
        }
        val compiler = TestCompiler(tree)
        tree.traverse(compiler)

        val outerLoop = tree.list().first() as LoopController
        outerLoop.initialize()

        sut.isRunningVersion = true
        sut.testStarted()

        return outerLoop
    }
}
