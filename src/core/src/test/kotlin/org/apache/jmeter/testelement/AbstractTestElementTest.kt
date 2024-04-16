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

package org.apache.jmeter.testelement

import io.mockk.mockk
import io.mockk.spyk
import org.apache.jmeter.testelement.property.CollectionProperty
import org.apache.jmeter.testelement.property.TestElementProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbstractTestElementTest {
    @Test
    fun `clone can remove properties`() {
        class ElementWithDefaultComment : AbstractTestElement() {
            init {
                set(TestElementSchema.comments, "initialized in constructor")
            }
        }

        val source = ElementWithDefaultComment().apply {
            removeProperty(TestElementSchema.comments.name)
        }

        val cloned = source.clone() as ElementWithDefaultComment

        cloned.propertyIterator().asSequence().toList()

        assertEquals(
            source.propertyIterator().asSequence().toList().joinToString("\n") { it.name + " = " + it.stringValue },
            cloned.propertyIterator().asSequence().toList().joinToString("\n") { it.name + " = " + it.stringValue },
        ) {
            "The properties after cloning the element should match the original properites. " +
                "Note that comments is added in constructor, however, we remove <<comments>> property before cloning"
        }

        assertEquals(source, cloned) {
            "The cloned element should be be equal the original element. " +
                "Note that comments is added in constructor, however, we remove <<comments>> property before cloning"
        }
    }

    @Test
    fun `set outer properties as temporary when using a TestElementProperty`() {
        val sut = spyk<AbstractTestElement>()
        val outerElement = mockk<TestElement>(relaxed = true)
        val innerElement = mockk<TestElement>(relaxed = true)
        val outerProp = TestElementProperty("outerProp", outerElement)
        val innerProp = TestElementProperty("innerProp", innerElement)
        outerProp.addProperty(innerProp)

        sut.setTemporary(outerProp)

        assertTrue(sut.isTemporary(outerProp)) {
            "isTemporary($outerProp)"
        }
        assertFalse(sut.isTemporary(innerProp)) {
            "isTemporary($innerProp)"
        }
    }

    @Test
    fun `set all properties as temporary when using a MultiProperty`() {
        val sut = spyk<AbstractTestElement>()
        val outerProp = CollectionProperty()
        val innerProp = CollectionProperty()

        outerProp.addProperty(innerProp)
        sut.setTemporary(outerProp)

        assertTrue(sut.isTemporary(outerProp)) {
            "isTemporary($outerProp)"
        }
        assertTrue(sut.isTemporary(innerProp)) {
            "isTemporary($innerProp)"
        }
    }
}
