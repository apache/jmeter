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
import org.apache.jmeter.testelement.property.CollectionProperty
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apiguardian.api.API
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass

/**
 * Enables type-safe access to [TestElement] properties.
 * For instance:
 *     testPlan.props[TestPlanClass.serializeThreadGroups] // succeeds
 *     testPlan.props[{ serializeThreadGroups }] // succeeds
 *     httpSampler.props[TestPlanClass.serializeThreadGroups] // fails as HTTP Sampler is not related to TestPlanClass
 *     httpSampler.props[{ enabled }] // succeeds since "enabled" property exists in the superclass of HTTP Sampler
 *
 * If you need configuring several properties, you might use `element.props { ... }` syntax:
 *
 *     testPlan.props {
 *         it[serializeThreadGroups] = true
 *         it[enabled] = false
 *     }
 *
 * The same in Java would be
 *    testPlan.getProps().invoke((it, schema) -> {
 *      it.set(schema.getSerializeThreadGroups(), true);
 *      it.set(schema.getEnabled(), false);
 *    });
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class PropertiesAccessor<out TestElementClass : TestElement, out Schema : BaseTestElementSchema>(
    public val target: TestElementClass,
    public val schema: Schema
) {
    /**
     * This interface makes `invoke` function easier to use from Java.
     * ` -> Unit` functions require to return `Unit.INSTANCE` from Java,
     * so we add an interface
     */
    public fun interface InstanceConfigurator<in TestElementClass : TestElement, in Schema : BaseTestElementSchema> {
        public fun configure(testElement: PropertiesAccessor<TestElementClass, Schema>, klass: Schema)
    }

    /**
     * Function types are idiomatic in Kotlin, however they require `return Unit.INSTANCE` in Java,
     * so we hide the method from Java.
     */
    @JvmSynthetic
    public inline operator fun invoke(body: Schema.(PropertiesAccessor<TestElementClass, Schema>) -> Unit) {
        body(schema, this)
    }

    /**
     * Function types are idiomatic in Kotlin, however they require `return Unit.INSTANCE` in Java,
     * so we hide the method from Java.
     * This is not intended to be used from Kotlin, however, there's no way to easily prevent it.
     * @see [KT-36439](https://youtrack.jetbrains.com/issue/KT-36439/PlatformOnly-annotation-to-hide-symbols-from-Kotlin)
     */
    public fun invoke(body: InstanceConfigurator<TestElementClass, Schema>) {
        body.configure(this, schema)
    }

    // All properties can be removed
    public fun remove(property: PropertyDescriptor<Schema, *>) {
        target.removeProperty(property.name)
    }

    // All properties can be set as strings
    public operator fun set(property: PropertyDescriptor<Schema, *>, value: String) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> PropertyDescriptor<Schema, *>,
        value: String
    ) {
        target[propertySelector(schema)] = value
    }

    public fun getString(property: PropertyDescriptor<Schema, *>): String =
        property.getString(target)

    public inline fun getString(propertySelector: Schema.() -> PropertyDescriptor<Schema, *>): String =
        propertySelector(schema).getString(target)

    /**
     * Returns a property or null if it is not set.
     */
    public fun getPropertyOrNull(property: PropertyDescriptor<Schema, *>): JMeterProperty? =
        target.getPropertyOrNull(property)

    // Boolean properties
    public operator fun get(property: BooleanPropertyDescriptor<Schema>): Boolean =
        target[property]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> BooleanPropertyDescriptor<Schema>): Boolean =
        target[propertySelector(schema)]

    public operator fun set(property: BooleanPropertyDescriptor<Schema>, value: Boolean) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> BooleanPropertyDescriptor<Schema>,
        value: Boolean
    ) {
        target[propertySelector(schema)] = value
    }

    // String properties
    public operator fun get(property: StringPropertyDescriptor<Schema>): String =
        property.getString(target)

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> StringPropertyDescriptor<Schema>): String =
        propertySelector(schema).getString(target)

    // Class properties
    public operator fun <ValueClass : Any> get(property: ClassPropertyDescriptor<Schema, ValueClass>): Class<out ValueClass> =
        target[property]

    public fun <ValueClass : Any> getOrNull(property: ClassPropertyDescriptor<Schema, ValueClass>): Class<out ValueClass>? =
        target.getOrNull(property)

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public operator fun <ValueClass : Any> get(propertySelector: Schema.() -> ClassPropertyDescriptor<Schema, ValueClass>): Class<out ValueClass> =
        get(propertySelector(schema))

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public fun <ValueClass : Any> getOrNull(propertySelector: Schema.() -> ClassPropertyDescriptor<Schema, ValueClass>): Class<out ValueClass>? =
        getOrNull(propertySelector(schema))

    public operator fun <ValueClass : Any> set(
        property: ClassPropertyDescriptor<Schema, ValueClass>,
        value: KClass<out ValueClass>
    ) {
        target[property] = value.java
    }

    public inline operator fun <ValueClass : Any> set(
        propertySelector: Schema.() -> ClassPropertyDescriptor<Schema, ValueClass>,
        value: KClass<ValueClass>?
    ) {
        set(propertySelector, value?.java)
    }

    public operator fun <ValueClass : Any> set(
        property: ClassPropertyDescriptor<Schema, ValueClass>,
        value: Class<out ValueClass>?
    ) {
        target[property] = value
    }

    public inline operator fun <ValueClass : Any> set(
        propertySelector: Schema.() -> ClassPropertyDescriptor<Schema, ValueClass>,
        value: Class<ValueClass>?
    ) {
        target[propertySelector(schema)] = value
    }

    // Integer properties
    public operator fun get(property: IntegerPropertyDescriptor<Schema>): Int =
        target[property]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> IntegerPropertyDescriptor<Schema>): Int =
        target[propertySelector(schema)]

    public operator fun set(property: IntegerPropertyDescriptor<Schema>, value: Int) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> IntegerPropertyDescriptor<Schema>,
        value: Int
    ) {
        target[propertySelector(schema)] = value
    }

    // Long properties
    public operator fun get(property: LongPropertyDescriptor<Schema>): Long =
        target[property]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> LongPropertyDescriptor<Schema>): Long =
        target[propertySelector(schema)]

    public operator fun set(property: LongPropertyDescriptor<Schema>, value: Long) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> LongPropertyDescriptor<Schema>,
        value: Long
    ) {
        target[propertySelector(schema)] = value
    }

    // Float properties
    public operator fun get(property: FloatPropertyDescriptor<Schema>): Float =
        target[property]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> FloatPropertyDescriptor<Schema>): Float =
        target[propertySelector(schema)]

    public operator fun set(property: FloatPropertyDescriptor<Schema>, value: Float) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> FloatPropertyDescriptor<Schema>,
        value: Float
    ) {
        target[propertySelector(schema)] = value
    }

    // Double properties
    public operator fun get(property: DoublePropertyDescriptor<Schema>): Double =
        target[property]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> DoublePropertyDescriptor<Schema>): Double =
        target[propertySelector(schema)]

    public operator fun set(property: DoublePropertyDescriptor<Schema>, value: Double) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> DoublePropertyDescriptor<Schema>,
        value: Double
    ) {
        target[propertySelector(schema)] = value
    }

    // TestElement properties
    public operator fun <ValueClass : TestElement> get(property: TestElementPropertyDescriptor<Schema, ValueClass>): ValueClass =
        target[property]

    public fun <ValueClass : TestElement> getOrNull(property: TestElementPropertyDescriptor<Schema, ValueClass>): ValueClass? =
        target.getOrNull(property)

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun <ValueClass : TestElement> get(propertySelector: Schema.() -> TestElementPropertyDescriptor<Schema, ValueClass>): ValueClass =
        target[propertySelector(schema)]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline fun <ValueClass : TestElement> getOrNull(propertySelector: Schema.() -> TestElementPropertyDescriptor<Schema, ValueClass>): ValueClass? =
        target.getOrNull(propertySelector(schema))

    public operator fun <ValueClass : TestElement> set(
        property: TestElementPropertyDescriptor<Schema, ValueClass>,
        value: ValueClass?
    ) {
        target[property] = value
    }

    public inline operator fun <ValueClass : TestElement> set(
        propertySelector: Schema.() -> TestElementPropertyDescriptor<Schema, ValueClass>,
        value: ValueClass?
    ) {
        target[propertySelector(schema)] = value
    }

    // Collection properties
    public operator fun get(property: CollectionPropertyDescriptor<Schema>): CollectionProperty =
        target[property]

    public fun getOrNull(property: CollectionPropertyDescriptor<Schema>): CollectionProperty? =
        target.getOrNull(property)

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline operator fun get(propertySelector: Schema.() -> CollectionPropertyDescriptor<Schema>): CollectionProperty =
        target[propertySelector(schema)]

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public inline fun getOrNull(propertySelector: Schema.() -> CollectionPropertyDescriptor<Schema>): CollectionProperty? =
        target.getOrNull(propertySelector(schema))

    public operator fun set(property: CollectionPropertyDescriptor<Schema>, value: Collection<*>?) {
        target[property] = value
    }

    public inline operator fun set(
        propertySelector: Schema.() -> CollectionPropertyDescriptor<Schema>,
        value: Collection<*>?
    ) {
        target[propertySelector(schema)] = value
    }
}
