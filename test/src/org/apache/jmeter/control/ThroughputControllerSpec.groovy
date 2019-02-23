/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.control

import org.apache.jmeter.junit.stubs.TestSampler
import spock.lang.Specification

class ThroughputControllerSpec extends Specification {

    def sut = new ThroughputController()

    def setup() {
        sut.addTestElement(new TestSampler("one"))
        sut.addTestElement(new TestSampler("two"))
    }

    def "new TC isDone is true"() {
        given:
            def newTC = new ThroughputController()
        expect:
            newTC.isDone()
    }

    def "2 maxThroughput runs samplers inside the TC only twice"() {
        given:
            sut.setStyle(ThroughputController.BYNUMBER)
            sut.setMaxThroughput(2)

            def expectedNames =
                    ["zero", "one", "two", "three",
                     "zero", "one", "two", "three",
                     "zero", "three",
                     "zero", "three",
                     "zero", "three",]

            def loop = createLoopController(5)
        when:
            def actualNames = expectedNames.collect({
                loop.next().getName()
            })
        then:
            loop.next() == null
            actualNames == expectedNames
            sut.isDone()
            sut.testEnded()
    }

    def "0 maxThroughput does not run any samplers inside the TC"() {
        given:
            sut.setStyle(ThroughputController.BYNUMBER)
            sut.setMaxThroughput(0)

            def loops = 3
            def loop = createLoopController(loops)

            def expectedNames = ["zero", "three"] * loops
        when:
            def actualNames = expectedNames.collect({
                loop.next().getName()
            })
        then:
            loop.next() == null
            actualNames == expectedNames
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
    def "0 maxThroughput does not run any sampler inside the TC and does not cause StackOverFlowError"() {
        given:
            sut.setStyle(ThroughputController.BYNUMBER)
            sut.setMaxThroughput(0)

            LoopController innerLoop = new LoopController()
            innerLoop.setLoops(10000)
            innerLoop.addTestElement(sut)
            innerLoop.addIterationListener(sut)
            innerLoop.initialize()
            innerLoop.setRunningVersion(true)
            sut.testStarted()
            sut.setRunningVersion(true)

        when:
            innerLoop.next() == null
            innerLoop.next() == null
        then:
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
    def "0.0 percentThroughput does not run any sampler inside the TC and does not cause StackOverFlowError"() {
        given:
            sut.setStyle(ThroughputController.BYPERCENT)
            sut.setPercentThroughput("0.0")

            LoopController innerLoop = new LoopController()
            innerLoop.setLoops(10000)
            innerLoop.addTestElement(sut)
            innerLoop.addIterationListener(sut)
            innerLoop.initialize()
            innerLoop.setRunningVersion(true)
            sut.testStarted()
            sut.setRunningVersion(true)

        when:
            innerLoop.next() == null
            innerLoop.next() == null
        then:
            sut.testEnded()
    }

    def "33.33% will run all the samplers inside the TC every 3 iterations"() {
        given:
            sut.setStyle(ThroughputController.BYPERCENT)
            sut.setPercentThroughput(33.33f)

            def loop = createLoopController(9)
            // Expected results established using the DDA algorithm - see:
            // http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/drawline.htm
            def expectedNames =
                    ["zero", "three", // 0/1 vs. 1/1 -> 0 is closer to 33.33
                     "zero", "one", "two", "three", // 0/2 vs. 1/2 -> 50.0 is closer to 33.33
                     "zero", "three", // 1/3 vs. 2/3 -> 33.33 is closer to 33.33
                     "zero", "three", // 1/4 vs. 2/4 -> 25.0 is closer to 33.33
                     "zero", "one", "two", "three", // 1/5 vs. 2/5 -> 40.0 is closer to 33.33
                     "zero", "three", // 2/6 vs. 3/6 -> 33.33 is closer to 33.33
                     "zero", "three", // 2/7 vs. 3/7 -> 0.2857 is closer to 33.33
                     "zero", "one", "two", "three", // 2/8 vs. 3/8 -> 0.375 is closer to 33.33
                     "zero", "three", // ...
                    ]
        when:
            def actualNames = expectedNames.collect({
                loop.next().getName()
            })
        then:
            loop.next() == null
            actualNames == expectedNames
            sut.testEnded()
    }

    def "0% does not run any samplers inside the TC"() {
        given:
            sut.setStyle(ThroughputController.BYPERCENT)
            sut.setPercentThroughput(0.0f)

            def loops = 3
            def loop = createLoopController(loops)

            def expectedNames = ["zero", "three",] * loops
        when:
            def actualNames = expectedNames.collect({
                loop.next().getName()
            })
        then:
            loop.next() == null
            actualNames == expectedNames
            sut.testEnded()
    }

    def "100% always runs all samplers inside the TC"() {
        given:
            sut.setStyle(ThroughputController.BYPERCENT)
            sut.setPercentThroughput(100.0f)

            def loops = 3
            def loop = createLoopController(loops)

            def expectedNames = ["zero", "one", "two", "three",] * loops
        when:
            def actualNames = expectedNames.collect({
                loop.next().getName()
            })
        then:
            loop.next() == null
            actualNames == expectedNames
            sut.testEnded()
    }

    /**
     * Create a LoopController, executed once, which contains an inner loop that
     * runs {@code innerLoops} times, this inner loop contains a TestSampler ("zero")
     * the {@link ThroughputController} which contains two test samplers
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
     * @param innerLoops number of times to loop the {@link ThroughputController}
     * @return the{@link LoopController}
     */
    def createLoopController(int innerLoops) {
        LoopController innerLoop = new LoopController()
        innerLoop.setLoops(innerLoops)
        innerLoop.addTestElement(new TestSampler("zero"))
        innerLoop.addTestElement(sut)
        innerLoop.addIterationListener(sut)
        innerLoop.addTestElement(new TestSampler("three"))

        def outerLoop = new LoopController()
        outerLoop.setLoops(1)
        outerLoop.addTestElement(innerLoop)
        sut.testStarted()
        outerLoop.setRunningVersion(true)
        sut.setRunningVersion(true)
        innerLoop.setRunningVersion(true)
        outerLoop.initialize()
        outerLoop
    }
}
