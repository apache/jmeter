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

package org.apache.jmeter.engine.util

import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.testelement.property.CollectionProperty
import org.apache.jmeter.testelement.schema.CollectionPropertyDescriptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

/**
 * Verifies [TestElementPropertyTransformer.USE_FUNCTIONS].
 */
class UseFunctionsTest {
    @Test
    fun `property replaces with function`() {
        val input = TestPlan().apply {
            props {
                it[enabled] = "\${test}"
            }
        }

        TestElementPropertyTransformer.USE_FUNCTIONS.visit(input)

        val output = input.props.getPropertyOrNull(input.schema.enabled)
        assertFunctionProperty("\${test}", output) {
            "TestElementPropertyTransformer.USE_FUNCTIONS($input)"
        }
    }

    @Test
    fun `nested collection property replaces with function`() {
        val sampleProp = CollectionPropertyDescriptor<TestElementSchema>("sampleProp", "test.prop")

        val input = TestPlan().apply {
            props {
                it[sampleProp] = mutableListOf(mutableListOf("\${test}"))
            }
        }

        TestElementPropertyTransformer.USE_FUNCTIONS.visit(input)

        val output = input.props[ { sampleProp }]

        // These assertions verify CollectionProperty behavior rather than USE_FUNCTIONS behavior
        // However, we can't extract deeply-nested item right away
        assertEquals(1, output.size()) {
            "size of first level collection should be 1, actual: $output"
        }
        val nested = output.first()
        assertInstanceOf(CollectionProperty::class.java, nested) {
            "element of the top-level collection should be CollectionProperty as well, got $nested"
        }
        nested as CollectionProperty
        assertEquals(1, nested.size()) {
            "size of the nested collection should be 1, actual: $nested"
        }

        // Now is the real test:
        assertFunctionProperty("\${test}", nested.get(0)) {
            "the first element of collection $nested"
        }
    }
}
