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

package org.apache.jorphan.collections

import java.util.concurrent.ConcurrentHashMap

/**
 * Wraps an object so [equals] and [hashCode] compare object identities.
 * One of the common usages is to create [ConcurrentHashMap] where
 * keys are distinguished by their identities instead of [Object.equals].
 */
public class IdentityKey<out T>(
    public val value: T
) {
    override fun equals(other: Any?): Boolean =
        other is IdentityKey<*> && other.value === value

    override fun hashCode(): Int =
        System.identityHashCode(value)

    override fun toString(): String =
        "IdentityKey{$value}"
}
