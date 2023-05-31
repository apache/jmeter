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
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.MultiProperty
import org.apiguardian.api.API
import org.slf4j.LoggerFactory

/**
 * Transforms [JMeterProperty] and including the contents of [MultiProperty].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class DeepPropertyTransformer(
    private val simpleTransformer: PropertyTransformer
) : PropertyTransformer {
    private companion object {
        private val log = LoggerFactory.getLogger(DeepPropertyTransformer::class.java)
    }

    override fun transform(input: JMeterProperty): JMeterProperty {
        log.debug("Processing property {}", input)

        return when (input) {
            is MultiProperty ->
                processMultiProperty(input) ?: input

            else ->
                simpleTransformer.transform(input)
        }
    }

    /**
     * Transforms a [MultiProperty]. Currently, there's no way to "clone MultiProperty with new values",
     * so we use old approach of "clear and add all values again".
     * @return the modified property or null if no modifications were made
     */
    private fun processMultiProperty(input: MultiProperty): MultiProperty? {
        // TODO move this to JMeterProperty.accept(visitor) returns JMeter Property
        // so we can avoid mutating the properties in-place
        // Modifying input property makes it harder to understand if it was modified or not
        return getNewPropsOrNull(input)?.let { props ->
            log.debug("About to replace values in MultiProperty {} with new ones: {}", input, props)
            input.clear()
            for (prop in props) {
                input.addProperty(prop)
            }
            input
        }
    }

    /**
     * Return the new list of transformed properties, or null if no transformations were made.
     */
    private fun getNewPropsOrNull(
        input: MultiProperty,
    ): MutableList<JMeterProperty>? {
        // We don't know if there will be transformations, so we need to store
        // all the properties.
        val props = mutableListOf<JMeterProperty>()
        var hasTransformations = false
        val it = input.iterator()
        while (it.hasNext()) {
            val property = it.next()

            // MultiProperty is replaced in-place, so we ask the function to set hasModifications flag on changes
            val newValue = when (property) {
                is MultiProperty -> {
                    val res = processMultiProperty(property)
                    res?.also { hasTransformations = true } ?: property
                }

                else ->
                    when (property.name) {
                        // Avoid replacing vital properties
                        TestElement.GUI_CLASS, TestElement.TEST_CLASS -> property
                        else -> transform(property)
                    }
            }
            if (property !== newValue) {
                // If the value is different, it was transformed
                hasTransformations = true
            }
            props.add(newValue)
        }
        return props.takeIf { hasTransformations }
    }
}
