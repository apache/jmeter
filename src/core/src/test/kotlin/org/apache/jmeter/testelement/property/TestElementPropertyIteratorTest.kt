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

package org.apache.jmeter.testelement.property

import org.apache.jmeter.engine.util.NoThreadClone
import org.apache.jmeter.testelement.AbstractTestElement
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TestElementPropertyIteratorTest {
    class TempConfig : AbstractTestElement(), NoThreadClone

    @Test
    fun `iterator remove from nocloneelement`() {
        val a = TempConfig().apply {
            setProperty("present", "1")
        }
        val b = TempConfig().apply {
            setProperty("present", "1")
            setProperty("to_remove", "to be removed")
        }
        val it = b.propertyIterator()
        while (it.hasNext()) {
            val prop = it.next()
            if (prop.name == "to_remove") {
                it.remove()
            }
        }
        assertTrue(a == b) {
            "Elements should be equal after removal of property <<to_remove>> with iterator.remove(). " +
                "If properties look the same, it might mean propMap and propMapConcurrent diverged. " +
                "Properties of a default element: ${a.propertyIterator().asSequence().joinToString { "${it.name}=${it.stringValue}" }}, " +
                "Properties of element after removal: ${b.propertyIterator().asSequence().joinToString { "${it.name}=${it.stringValue}" }}"
        }
    }
}
