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

package org.apache.jmeter.treebuilder

import java.util.function.Consumer

/**
 * Performs an action on a receiver.
 *
 * It enables to write DSL that works great in Java and Kotlin at the same time.
 * The issue with [Consumer] is that it does not declare type variance.
 * Kotlin's `T.() -> Unit` (function that returns `Unit`) is problematic to use from
 * Java since it requires explicit `return Unit.INSTANCE`.
 */
public fun interface Action<in T> : (T) -> Unit {
    public fun T.execute()

    override operator fun invoke(receiver: T) {
        receiver.run { execute() }
    }
}
