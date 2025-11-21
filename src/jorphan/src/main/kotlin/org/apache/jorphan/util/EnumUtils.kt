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

@file:JvmName("EnumUtils")
package org.apache.jorphan.util

import org.apiguardian.api.API
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableMap

private val VALUES = object : ClassValue<List<Enum<*>>>() {
    override fun computeValue(type: Class<*>): List<Enum<*>> {
        require(type.isEnum) {
            "Class $type is not an enum"
        }
        @Suppress("UNCHECKED_CAST")
        type as Class<Enum<*>>
        val enumConstants = type.getEnumConstants()
        return unmodifiableList(listOf(*enumConstants))
    }
}

private val VALUE_MAP = object : ClassValue<Map<String, Enum<*>>>() {
    override fun computeValue(type: Class<*>): Map<String, Enum<*>> {
        require(type.isEnum) {
            "Class $type is not an enum"
        }
        @Suppress("UNCHECKED_CAST")
        type as Class<Enum<*>>
        return unmodifiableMap(
            VALUES.get(type).associateBy { it.stringValue }
        )
    }
}

@Suppress("UNCHECKED_CAST")
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun <T : Enum<*>> values(klass: Class<out T>): List<T> =
    VALUES.get(klass) as List<T>

@Suppress("UNCHECKED_CAST")
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun <T : Enum<*>> valueMap(klass: Class<out T>): List<T> =
    VALUES.get(klass) as List<T>

@Suppress("UNCHECKED_CAST")
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public inline fun <reified T : Enum<*>> valueOf(value: String): T? =
    valueOf(T::class.java, value)

@Suppress("UNCHECKED_CAST")
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun <T : Enum<*>> valueOf(klass: Class<T>, value: String): T? =
    VALUE_MAP.get(klass)[value] as T?

@get:API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public val Enum<*>.stringValue: String
    get() = toString()
