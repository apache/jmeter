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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CollectionPropertyTest {
    @Test
    fun `creates with immutable list`() {
        val inputValues = listOf(1, 2)
        val prop = CollectionProperty("property_name", inputValues)
        Assertions.assertEquals(
            inputValues.toString(),
            (prop.objectValue as? List<*>).toString(),
            "CollectionProperty should contain the same elements as in the input"
        )
    }
}
