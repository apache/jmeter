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

package org.apache.jmeter.testelement.schema

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.property.CollectionProperty
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apiguardian.api.API
import kotlin.reflect.KProperty

/**
 * Describes a [CollectionProperty]: name, default value, and provides accessors for properties.
 * Use [EmptyTestElementSchema.collection] for building the property descriptors.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class CollectionPropertyDescriptor<in Schema : TestElementSchema>(
    public override val name: String,
) : PropertyDescriptor<Schema, Collection<JMeterProperty>> {
    private companion object {
        private const val serialVersionUID: Long = 1
    }

    public override val defaultValue: Collection<JMeterProperty>? = null

    override fun toString(): String = "CollectionPropertyDescriptor(name='$name')"

    /**
     * Retrieve [Collection] property value, or throw [NoSuchElementException] in case the property is unset.
     * @throws NoSuchElementException if the property is unset
     */
    public operator fun get(target: TestElement): Collection<JMeterProperty> =
        target[this]

    /**
     * Retrieve [Collection] property value, or create one and set it the property is unset.
     */
    public fun getOrCreate(target: TestElement, ifMissing: () -> Collection<JMeterProperty>): Collection<JMeterProperty> =
        target.getOrCreate(this, ifMissing)

    public operator fun set(target: TestElement, value: Collection<*>?) {
        target[this] = value
    }

    @JvmSynthetic
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun getValue(testElement: TestElement, property: KProperty<*>): Collection<JMeterProperty> {
        return testElement[this]
    }

    @JvmSynthetic
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun setValue(testElement: TestElement, property: KProperty<*>, value: Collection<JMeterProperty>) {
        testElement[this] = value
    }
}
