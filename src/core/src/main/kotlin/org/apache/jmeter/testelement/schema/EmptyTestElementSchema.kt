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
import org.apiguardian.api.API
import kotlin.reflect.KClass

/**
 * Describes properties available for configuration of well-known [TestElement].
 * Style guide:
 * * avoid using "is" prefixes. The properties are declare [PropertyDescriptor] rather than property itself,
 *   so "is" is not needed
 * * declare default value when declaring a property. It would help users.
 *
 * TODO: decide if boolean, int, long, etc values must always have a default
 *
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class EmptyTestElementSchema {
    private val propertyDescriptors: MutableMap<String, PropertyDescriptor<*, *>> = mutableMapOf()

    public val properties: Map<String, PropertyDescriptor<*, *>> = propertyDescriptors

    @JvmOverloads
    protected fun <Schema : TestElementSchema> string(
        name: String,
        default: String? = null
    ): StringPropertyDescriptor<Schema> =
        StringPropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }

    protected fun <Schema : TestElementSchema, ValueClass : Any> classProperty(
        klass: KClass<ValueClass>,
        name: String
    ): ClassPropertyDescriptor<Schema, ValueClass> =
        classProperty(klass.java, name)

    protected fun <Schema : TestElementSchema, ValueClass : Any> classProperty(
        klass: Class<ValueClass>,
        name: String
    ): ClassPropertyDescriptor<Schema, ValueClass> =
        ClassPropertyDescriptor<Schema, ValueClass>(klass, name).also {
            propertyDescriptors[name] = it
        }

    protected inline fun <Schema : TestElementSchema, reified TestElementClass : TestElement> testElement(
        name: String
    ): TestElementPropertyDescriptor<Schema, TestElementClass> =
        testElement(TestElementClass::class.java, name)

    protected fun <Schema : TestElementSchema, TestElementClass : TestElement> testElement(
        klass: Class<TestElementClass>,
        name: String
    ): TestElementPropertyDescriptor<Schema, TestElementClass> =
        TestElementPropertyDescriptor<Schema, TestElementClass>(klass, name).also {
            propertyDescriptors[name] = it
        }

    protected fun <Schema : TestElementSchema, ValueClass : Any> collection(
        klass: Class<ValueClass>,
        name: String
    ): ClassPropertyDescriptor<Schema, ValueClass> =
        ClassPropertyDescriptor<Schema, ValueClass>(klass, name).also {
            propertyDescriptors[name] = it
        }

    @JvmOverloads
    protected fun <Schema : TestElementSchema> boolean(
        name: String,
        default: Boolean? = null
    ): BooleanPropertyDescriptor<Schema> =
        BooleanPropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }

    @JvmOverloads
    protected fun <Schema : TestElementSchema> integer(
        name: String,
        default: Int? = null
    ): IntegerPropertyDescriptor<Schema> =
        IntegerPropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }

    @JvmOverloads
    protected fun <Schema : TestElementSchema> long(
        name: String,
        default: Long? = null
    ): LongPropertyDescriptor<Schema> =
        LongPropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }

    @JvmOverloads
    protected fun <Schema : TestElementSchema> float(
        name: String,
        default: Float? = null
    ): FloatPropertyDescriptor<Schema> =
        FloatPropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }

    @JvmOverloads
    protected fun <Schema : TestElementSchema> double(
        name: String,
        default: Double? = null
    ): DoublePropertyDescriptor<Schema> =
        DoublePropertyDescriptor<Schema>(name, default).also {
            propertyDescriptors[name] = it
        }
}
