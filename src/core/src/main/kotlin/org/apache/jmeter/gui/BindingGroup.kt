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

package org.apache.jmeter.gui

import org.apache.jmeter.testelement.TestElement
import org.apiguardian.api.API

/**
 * Manages a collection of [Binding]s.
 * It enables to update a [TestElement] from the UI and vice versa with a common implementation.
 * @since 5.6.3
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6.3")
public class BindingGroup() : Binding {
    private val bindings = mutableListOf<Binding>()

    public constructor(bindings: Collection<Binding>) : this() {
        addAll(bindings)
    }

    public fun add(binding: Binding) {
        bindings += binding
    }

    public fun addAll(bindings: Collection<Binding>) {
        this.bindings.addAll(bindings)
    }

    override fun updateElement(testElement: TestElement) {
        bindings.forEach { it.updateElement(testElement) }
    }

    override fun updateUi(testElement: TestElement) {
        bindings.forEach { it.updateUi(testElement) }
    }
}
