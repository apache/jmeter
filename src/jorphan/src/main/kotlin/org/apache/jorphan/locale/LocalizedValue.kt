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

package org.apache.jorphan.locale

import org.apiguardian.api.API

/**
 * Represents a localized value that combines an object implementing the [ResourceKeyed] interface
 * with a localization function. This allows the object's resource key to be localized dynamically
 * into a string representation.
 *
 * @param T The type of the value, which must implement the [ResourceKeyed] interface.
 * @property value The object implementing [ResourceKeyed], which holds a resource key for localization.
 * @property localizer A function that takes a resource key string and returns its localized string representation.
 *
 * The localized representation of this object is produced by applying the `localizer` function
 * to the resource key of the `value`.
 *
 * The class provides equality and hash code implementations based on the underlying `value`.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class LocalizedValue<T : ResourceKeyed>(
    public val value: T,
    private val localizer: ResourceLocalizer,
) : ResourceKeyed by value, ComboBoxValue {
    override fun toString(): String =
        localizer.localize(value.resourceKey)

    override fun equals(other: Any?): Boolean =
        other is LocalizedValue<*> && other.value == value

    override fun hashCode(): Int =
        value.hashCode()
}
