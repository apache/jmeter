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
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apiguardian.api.API
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Describes a [JMeterProperty] that contains class reference: name, default value, and provides accessors for properties.
 * Use [BaseTestElementSchema.classProperty] or [BaseTestElementSchema.classPropertyDescriptor] for building the property descriptors.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public data class ClassPropertyDescriptor<in Schema : BaseTestElementSchema, ValueClass : Any>(
    override val shortName: String,
    public val klass: Class<ValueClass>,
    override val name: String,
    /** Default value, null means there's no default */
    override val defaultValue: Class<out ValueClass>? = null
) : PropertyDescriptor<Schema, Class<out ValueClass>> {
    private companion object {
        private const val serialVersionUID: Long = 1
    }

    public class Builder<in Schema : BaseTestElementSchema, ValueClass : Any>(
        public val klass: Class<ValueClass>,
        name: String
    ) : PropertyDescriptor.Builder<Schema, Class<ValueClass>>(name, null)

    override val defaultValueAsString: String?
        get() = defaultValue?.name

    public operator fun get(target: TestElement): Class<out ValueClass> =
        target[this]

    /**
     * Retrieve [Class] property value, or return `null` in case the property is unset.
     */
    public fun getOrNull(target: TestElement): Class<out ValueClass>? =
        target.getOrNull(this)

    public operator fun set(target: TestElement, klass: Class<out ValueClass>?) {
        target[this] = klass
    }

    public operator fun set(target: TestElement, klass: KClass<out ValueClass>?) {
        set(target, klass?.java)
    }

    @JvmSynthetic
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun getValue(testElement: TestElement, property: KProperty<*>): Class<out ValueClass> {
        return testElement[this]
    }

    @JvmSynthetic
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun setValue(testElement: TestElement, property: KProperty<*>, value: Class<out ValueClass>?) {
        testElement[this] = value
    }
}
