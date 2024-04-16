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

import io.mockk.mockk
import org.apache.jmeter.junit.stubs.TestSampler
import org.apache.jmeter.testelement.TestElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RandomOrderControllerTest {
    val sut = RandomOrderController()

    @Test
    fun `next() on an empty controller returns null`() {
        sut.initialize()
        val nextSampler = sut.next()
        assertNull(nextSampler)
    }

    @Test
    fun `next() returns only provided sampler`() {
        val sampler = TestSampler("the one and only")
        sut.addTestElement(sampler)
        sut.initialize()

        val nextSampler = sut.next()
        val nextSamplerAfterEnd = sut.next()

        assertEquals(sampler, nextSampler, "there's only one sampler, so it should be returned from .next()")
        assertNull(nextSamplerAfterEnd, "nextSamplerAfterEnd")
    }

    @Test
    fun `next() returns exactly all added elements in random order`() {
        val samplerNames = (1..50).map { it.toString() }
        samplerNames.forEach {
            sut.addTestElement(TestSampler(it))
        }
        sut.initialize()

        val elements = sut.getAllTestElements()

        // then: "the same elements are returned but in a different order"
        // val
        val elementNames = elements.map { it.name }
        assertEquals(samplerNames.toSet(), elementNames.toSet(), "controller should return the same elements")
        assertNotEquals(samplerNames, elementNames, "The order of elements should be randomized")
    }

    @Test
    fun `next() is null if isDone() is true`() {
        sut.addTestElement(mockk<TestElement>())
        sut.initialize()
        sut.isDone = true

        val nextSampler = sut.next()
        assertTrue(sut.isDone, ".isDone()")
        assertNull(nextSampler, "nextSampler")
    }

    /**
     * Builds and returns a list by 'iterating' through the
     * [GenericController], using [GenericController.next()],
     * placing each item in a list until `null` is encountered.
     *
     * @param controller the [GenericController] to 'iterate' though
     * @return a list of all items (in order) returned from next()
     * method, excluding null
     */
    fun GenericController.getAllTestElements() =
        buildList {
            while (true) {
                val sampler = next() ?: break
                add(sampler)
            }
        }
}
