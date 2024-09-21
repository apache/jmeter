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
 * Describes properties available for configuration of well-known [TestElement].
 * Style guide:
 * * avoid using "is" prefixes. The properties are declare [PropertyDescriptor] rather than property itself,
 *   so "is" is not needed
 * * declare default value when declaring a property. It would help users.
 * * declare schemas as abstract classes or make constructor protected. Users might need extending the schema,
 *   however, there's no much sense in creating several instances, so instances should be shared as singletones.
 *
 * TODO: decide if boolean, int, long, etc values must always have a default
 *
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class BaseTestElementSchema {
    private val propertyDescriptors: MutableMap<String, PropertyDescriptor<*, *>> = mutableMapOf()

    /**
     * Associates JMeter property name (e.g. `TestPlan.enabled`) with the corresponding [PropertyDescriptor]
     */
    public val properties: Map<String, PropertyDescriptor<*, *>> = propertyDescriptors

    private val groupDescriptors: MutableMap<String, BasePropertyGroupSchema<*>> = mutableMapOf()

    /**
     * Associates subgroup name with the corresponding group schema.
     */
    public val groups: Map<String, BasePropertyGroupSchema<*>> = groupDescriptors

    private val propertyGroups = mutableMapOf<PropertyDescriptor<*, *>, BasePropertyGroupSchema<*>>()

    private fun <Property : PropertyDescriptor<*, *>> Property.register(): Property {
        propertyDescriptors[name] = this
        return this
    }

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema, Property : PropertyDescriptor<Schema, *>> Property.getValue(
        that: Schema,
        property: KProperty<*>
    ): Property =
        this

    /**
     * It enables configuring a group of properties inside [TestElement.props] block:
     *
     *     httpSampler.props { // this: HttpSamplerBaseSchema, it: PropertiesAccessor<HttpSamplerBase, HttpSamplerBaseSchema>
     *         proxy { // <-- this is Group.invoke
     *           it[proxyPort] = 8080
     *         }
     *     }
     */
    public operator fun <Group : BasePropertyGroupSchema<*>> Group.invoke(body: Group.() -> Unit): Unit = run(body)

    protected fun <Schema : BaseTestElementSchema, Group : BasePropertyGroupSchema<Schema>> addSubgroup(name: String, group: Group): Group {
        group.parent = ParentSchemaReference(this, name)
        groupDescriptors[name] = group
        group.properties.values.forEach {
            it.register()
            // If the property is not declared in th the subgroup, assume it is declared in the group itself
            propertyGroups[it] = group.getSubgroup(it) ?: group
        }
        return group
    }

    public fun <Schema : BaseTestElementSchema> getSubgroup(property: PropertyDescriptor<Schema, *>): BasePropertyGroupSchema<Schema>? {
        @Suppress("UNCHECKED_CAST")
        return propertyGroups[property] as BasePropertyGroupSchema<Schema>?
    }

    /**
     * Returns a list of group names that are required to access the property by its short name.
     * If the property is declared in the current schema, the list is empty.
     */
    public fun getGroupPath(property: PropertyDescriptor<*, *>): List<String> {
        var group: ParentSchemaReference? = getSubgroup(property)?.parent ?: return listOf()
        val res = mutableListOf<String>()
        while (group != null && group.schema.groups[group.groupName] != this) {
            res += group.groupName
            group = (group.schema as? BasePropertyGroupSchema<*>)?.parent
        }
        res.reverse()
        return res
    }

    /**
     * It enables attach a group of properties to the current schema.
     */
    public operator fun <Schema : BaseTestElementSchema, Group : BasePropertyGroupSchema<Schema>> Group.provideDelegate(
        that: Schema,
        property: KProperty<*>,
    ): Group {
        return that.addSubgroup(property.name, this@provideDelegate)
    }

    /**
     * It enables attach a group of properties to the current group of properties
     */
    public operator fun <Schema : BaseTestElementSchema, Group : BasePropertyGroupSchema<Schema>> Group.provideDelegate(
        that: BasePropertyGroupSchema<Schema>,
        property: KProperty<*>,
    ): Group {
        return that.addSubgroup(property.name, this@provideDelegate)
    }

    public operator fun <Schema : BaseTestElementSchema, Group : BasePropertyGroupSchema<Schema>> Group.getValue(
        that: Schema,
        prop: KProperty<*>
    ): Group =
        this

    // String

    /**
     * Creates [StringPropertyDescriptor] that describes a string property.
     */
    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> stringDescriptor(
        shortName: String,
        name: String,
        default: String? = null
    ): StringPropertyDescriptor<Schema> =
        StringPropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.string(
        name: String,
        default: String? = null
    ): PropertyDescriptor.Builder<Schema, String> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, String>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): StringPropertyDescriptor<Schema> =
        that.stringDescriptor(property.name, name, default)

    // Class

    protected fun <Schema : BaseTestElementSchema, ValueClass : Any> classPropertyDescriptor(
        shortName: String,
        klass: Class<ValueClass>,
        name: String
    ): ClassPropertyDescriptor<Schema, ValueClass> =
        ClassPropertyDescriptor<Schema, ValueClass>(shortName, klass, name).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema, ValueClass : Any> Schema.classProperty(
        klass: KClass<ValueClass>,
        name: String
    ): ClassPropertyDescriptor.Builder<Schema, ValueClass> =
        classProperty(klass.java, name)

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema, ValueClass : Any> Schema.classProperty(
        klass: Class<ValueClass>,
        name: String
    ): ClassPropertyDescriptor.Builder<Schema, ValueClass> =
        ClassPropertyDescriptor.Builder(klass, name)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema, ValueClass : Any> ClassPropertyDescriptor.Builder<Schema, ValueClass>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): ClassPropertyDescriptor<Schema, ValueClass> = that.classPropertyDescriptor(property.name, klass, name)

    // TestElement

    protected fun <Schema : BaseTestElementSchema, TestElementClass : TestElement> testElementDescriptor(
        shortName: String,
        klass: Class<TestElementClass>,
        name: String
    ): TestElementPropertyDescriptor<Schema, TestElementClass> =
        TestElementPropertyDescriptor<Schema, TestElementClass>(shortName, klass, name).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema, reified TestElementClass : TestElement> Schema.testElement(
        name: String
    ): TestElementPropertyDescriptor.Builder<Schema, TestElementClass> =
        TestElementPropertyDescriptor.Builder(TestElementClass::class.java, name)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema, TestElementClass : TestElement> TestElementPropertyDescriptor.Builder<Schema, TestElementClass>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): TestElementPropertyDescriptor<Schema, TestElementClass> = that.testElementDescriptor(property.name, klass, name)

    // Collection

    protected fun <Schema : BaseTestElementSchema> collectionDescriptor(
        shortName: String,
        name: String
    ): CollectionPropertyDescriptor<Schema> =
        CollectionPropertyDescriptor<Schema>(shortName, name).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.collection(
        name: String
    ): PropertyDescriptor.Builder<Schema, Collection<JMeterProperty>> =
        PropertyDescriptor.Builder(name, null)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Collection<JMeterProperty>>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): CollectionPropertyDescriptor<Schema> = that.collectionDescriptor(property.name, name)

    // Boolean

    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> booleanDescriptor(
        shortName: String,
        name: String,
        default: Boolean? = null
    ): BooleanPropertyDescriptor<Schema> =
        BooleanPropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.boolean(
        name: String,
        default: Boolean? = false
    ): PropertyDescriptor.Builder<Schema, Boolean> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Boolean>.provideDelegate(
        that: Schema,
        property: KProperty<*>,
    ): BooleanPropertyDescriptor<Schema> = that.booleanDescriptor(property.name, name, default)

    // Integer

    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> intDescriptor(
        shortName: String,
        name: String,
        default: Int? = null,
    ): IntegerPropertyDescriptor<Schema> =
        IntegerPropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.int(
        name: String,
        default: Int? = null,
    ): PropertyDescriptor.Builder<Schema, Int> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Int>.provideDelegate(
        that: Schema,
        property: KProperty<*>,
    ): IntegerPropertyDescriptor<Schema> = that.intDescriptor(property.name, name, default)

    // Long

    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> longDescriptor(
        shortName: String,
        name: String,
        default: Long? = null,
    ): LongPropertyDescriptor<Schema> =
        LongPropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.long(
        name: String,
        default: Long? = null,
    ): PropertyDescriptor.Builder<Schema, Long> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Long>.provideDelegate(
        that: Schema,
        property: KProperty<*>,
    ): LongPropertyDescriptor<Schema> = that.longDescriptor(property.name, name, default)

    // Float

    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> floatDescriptor(
        shortName: String,
        name: String,
        default: Float? = null
    ): FloatPropertyDescriptor<Schema> =
        FloatPropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.float(
        name: String,
        default: Float? = null,
    ): PropertyDescriptor.Builder<Schema, Float> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Float>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): FloatPropertyDescriptor<Schema> = that.floatDescriptor(property.name, name, default)

    // Double

    @JvmOverloads
    protected fun <Schema : BaseTestElementSchema> doubleDescriptor(
        shortName: String,
        name: String,
        default: Double? = null,
    ): DoublePropertyDescriptor<Schema> =
        DoublePropertyDescriptor<Schema>(shortName, name, default).register()

    @JvmSynthetic
    protected inline fun <reified Schema : BaseTestElementSchema> Schema.double(
        name: String,
        default: Double? = null,
    ): PropertyDescriptor.Builder<Schema, Double> =
        PropertyDescriptor.Builder(name, default)

    @JvmSynthetic
    protected operator fun <Schema : BaseTestElementSchema> PropertyDescriptor.Builder<Schema, Double>.provideDelegate(
        that: Schema,
        property: KProperty<*>
    ): DoublePropertyDescriptor<Schema> = that.doubleDescriptor(property.name, name, default)
}
