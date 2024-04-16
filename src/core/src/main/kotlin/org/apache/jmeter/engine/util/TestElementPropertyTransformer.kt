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

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.property.TestElementProperty
import org.apiguardian.api.API

/**
 * Transforms properties of the input [TestElement] with given [PropertyTransformer].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class TestElementPropertyTransformer(
    propertyTransformer: PropertyTransformer
) {
    public companion object {
        @JvmField
        public val USE_FUNCTIONS: TestElementPropertyTransformer =
            TestElementPropertyTransformer(TransformStringsIntoFunctions)
    }

    private val transformer = DeepPropertyTransformer(propertyTransformer)

    public fun visit(testElement: TestElement) {
        // For now, DeepPropertyTransformer mutates MultiProperties inplace, so
        transformer.transform(TestElementProperty("temp", testElement))
    }
}
