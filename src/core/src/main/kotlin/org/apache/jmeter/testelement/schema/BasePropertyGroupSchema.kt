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
 * Factors parameters into a group, so it can be reused or accessed in a grouped manner.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public open class BasePropertyGroupSchema<Schema : BaseTestElementSchema> : BaseTestElementSchema() {
    /**
     * A reference to a parent schema declaration.
     * It can be either [Schema] or [BasePropertyGroupSchema].
     */
    public lateinit var parent: ParentSchemaReference
        internal set

    public operator fun <Group : BasePropertyGroupSchema<Schema>> Group.getValue(
        that: BasePropertyGroupSchema<Schema>,
        prop: KProperty<*>
    ): Group =
        this

    @JvmSynthetic
    public operator fun <Property : PropertyDescriptor<Schema, *>> Property.getValue(
        that: BasePropertyGroupSchema<Schema>,
        prop: KProperty<*>
    ): Property =
        this

    // boolean

    @JvmSynthetic
    protected fun boolean(
        name: String,
        default: Boolean? = false
    ): PropertyDescriptor.Builder<Schema, Boolean> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Boolean>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): BooleanPropertyDescriptor<Schema> = that.booleanDescriptor(property.name, name, default)

    // string

    @JvmSynthetic
    protected fun string(
        name: String,
        default: String? = null
    ): PropertyDescriptor.Builder<Schema, String> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, String>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>
    ): StringPropertyDescriptor<Schema> =
        that.stringDescriptor(property.name, name, default)

    // int

    @JvmSynthetic
    protected fun int(
        name: String,
        default: Int? = null,
    ): PropertyDescriptor.Builder<Schema, Int> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Int>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): IntegerPropertyDescriptor<Schema> = that.intDescriptor(property.name, name, default)

    // long

    @JvmSynthetic
    protected fun long(
        name: String,
        default: Long? = null,
    ): PropertyDescriptor.Builder<Schema, Long> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Long>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): LongPropertyDescriptor<Schema> = that.longDescriptor(property.name, name, default)

    // float

    @JvmSynthetic
    protected fun float(
        name: String,
        default: Float? = null,
    ): PropertyDescriptor.Builder<Schema, Float> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Float>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): FloatPropertyDescriptor<Schema> = that.floatDescriptor(property.name, name, default)

    // float

    @JvmSynthetic
    protected fun double(
        name: String,
        default: Double? = null,
    ): PropertyDescriptor.Builder<Schema, Double> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Double>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): DoublePropertyDescriptor<Schema> = that.doubleDescriptor(property.name, name, default)

    // test element
    @JvmSynthetic
    protected inline fun <reified TestElementClass : TestElement> testElement(
        name: String
    ): TestElementPropertyDescriptor.Builder<Schema, TestElementClass> =
        TestElementPropertyDescriptor.Builder(TestElementClass::class.java, name)

    @JvmSynthetic
    protected operator fun <TestElementClass : TestElement> TestElementPropertyDescriptor.Builder<Schema, TestElementClass>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>
    ): TestElementPropertyDescriptor<Schema, TestElementClass> = that.testElementDescriptor(property.name, klass, name)

    // class

    @JvmSynthetic
    protected fun <ValueClass : Any> classProperty(
        klass: KClass<ValueClass>,
        name: String
    ): ClassPropertyDescriptor.Builder<Schema, ValueClass> =
        classProperty(klass.java, name)

    @JvmSynthetic
    protected fun <ValueClass : Any> classProperty(
        klass: Class<ValueClass>,
        name: String
    ): ClassPropertyDescriptor.Builder<Schema, ValueClass> =
        ClassPropertyDescriptor.Builder(klass, name)

    @JvmSynthetic
    protected operator fun <ValueClass : Any> ClassPropertyDescriptor.Builder<Schema, ValueClass>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>
    ): ClassPropertyDescriptor<Schema, ValueClass> = that.classPropertyDescriptor(property.name, klass, name)

    // collection

    @JvmSynthetic
    protected fun collection(
        name: String
    ): PropertyDescriptor.Builder<Schema, Collection<JMeterProperty>> =
        PropertyDescriptor.Builder(name, null)

    @JvmSynthetic
    protected operator fun PropertyDescriptor.Builder<Schema, Collection<JMeterProperty>>.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>
    ): CollectionPropertyDescriptor<Schema> = that.collectionDescriptor(property.name, name)
}
