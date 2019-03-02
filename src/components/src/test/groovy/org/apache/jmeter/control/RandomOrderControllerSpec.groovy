/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.control

import org.apache.jmeter.junit.stubs.TestSampler
import org.apache.jmeter.testelement.TestElement
import spock.lang.Specification

class RandomOrderControllerSpec extends Specification {

    def sut = new RandomOrderController()

    def "next() on an empty controller returns null"() {
        given:
            sut.initialize()
        when:
            def nextSampler = sut.next()
        then:
            nextSampler == null
    }

    def "next() returns only provided sampler"() {
        given:
            def sampler = new TestSampler("the one and only")
            sut.addTestElement(sampler)
            sut.initialize()
        when:
            def nextSampler = sut.next()
            def nextSamplerAfterEnd = sut.next()
        then:
            nextSampler == sampler
            nextSamplerAfterEnd == null
    }

    def "next() returns exactly all added elements in random order"() {
        given:
            def samplerNames = (1..50).collect { it.toString() }
            samplerNames.each {
                sut.addTestElement(new TestSampler(it))
            }
            sut.initialize()
        when:
            def elements = getAllTestElements(sut)
        then: "the same elements are returned but in a different order"
            def elementNames = elements.collect { it.getName() }
            elementNames.toSet() == samplerNames.toSet() // same elements
            elementNames != samplerNames                 // not the same order

    }

    def "next() is null if isDone() is true"() {
        given:
            sut.addTestElement(Mock(TestElement))
            sut.initialize()
            sut.setDone(true)
        when:
            def nextSampler = sut.next()
        then:
            sut.isDone()
            nextSampler == null
    }

    /**
     * Builds and returns a list by 'iterating' through the
     * {@link GenericController}, using {@link GenericController#next()},
     * placing each item in a list until <code>null</code> is encountered.
     *
     * @param controller the {@link GenericController} to 'iterate' though
     * @return a list of all items (in order) returned from next()
     * method, excluding null
     */
    def getAllTestElements(GenericController controller) {
        def sample
        def samplers = []
        while ((sample = controller.next()) != null) {
            samplers.add(sample)
        }
        return samplers
    }
}
