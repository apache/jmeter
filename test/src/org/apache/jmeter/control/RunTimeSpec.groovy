/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.control

import org.apache.jmeter.junit.stubs.TestSampler
import org.apache.jmeter.samplers.Sampler
import spock.lang.Specification

class RunTimeSpec extends Specification {

    def sut = new RunTime()

    def "RunTime stops within a tolerance after specified runtime"() {
        given:
            sut.setRuntime(1)

            def runTimeMillis = 1000
            def expectedLoops = 5
            def tolerance = Math.ceil(0.1f * expectedLoops)
            int samplerWaitTime = runTimeMillis / expectedLoops
            TestSampler samp1 = new TestSampler("Sample 1", samplerWaitTime)
            TestSampler samp2 = new TestSampler("Sample 2", samplerWaitTime)

            def sampler1Loops = 2
            LoopController loop1 = new LoopController()
            loop1.setLoops(sampler1Loops)
            loop1.setContinueForever(false)
            loop1.addTestElement(samp1)

            LoopController loop2 = new LoopController()
            loop2.setLoops(expectedLoops * 2)
            loop2.setContinueForever(false)
            loop2.addTestElement(samp2)

            sut.addTestElement(loop1)
            sut.addTestElement(loop2)
            sut.setRunningVersion(true)
            loop1.setRunningVersion(true)
            loop2.setRunningVersion(true)
            sut.initialize()
        when:
            def sampler
            int loopCount = 0
            long now = System.currentTimeMillis()
            while ((sampler = sut.next()) != null) {
                loopCount++
                sampler.sample(null)
            }
            long elapsed = System.currentTimeMillis() - now
        then:
            sut.getIterCount() == 1
            loopCount >= expectedLoops
            loopCount <= expectedLoops + tolerance
            elapsed >= runTimeMillis
            elapsed <= runTimeMillis + (tolerance * samplerWaitTime)
            samp1.getSamples() == sampler1Loops
            samp2.getSamples() >= expectedLoops - sampler1Loops
            samp2.getSamples() <= expectedLoops - sampler1Loops + tolerance
    }

    def "Immediately returns null when runtime is set to 0"() {
        given:
            sut.setRuntime(0)
            sut.addTestElement(Mock(Sampler))
        when:
            def sampler = sut.next()
        then:
            sampler == null
    }

    def "Immediately returns null if only Controllers are present"() {
        given:
            sut.setRuntime(10)
            sut.addTestElement(Mock(Controller))
            sut.addTestElement(Mock(Controller))
        when:
            def sampler = sut.next()
        then:
            sampler == null
    }
    def "within time limit samplers are returned until empty"() {
        given:
            def mockSampler = Mock(Sampler)
            sut.setRuntime(10)
            sut.addTestElement(mockSampler)
            sut.addTestElement(mockSampler)
        when:
            def samplers = [sut.next(), sut.next(), sut.next()]
        then:
            samplers == [mockSampler, mockSampler, null]
    }

    def "RunTime immediately returns null when there are no test elements"() {
        given:
            sut.setRuntime(10)
        when:
            def sampler = sut.next()
        then:
            sampler == null
    }

}
