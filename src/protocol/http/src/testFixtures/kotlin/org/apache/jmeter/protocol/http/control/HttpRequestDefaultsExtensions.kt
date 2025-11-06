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

package org.apache.jmeter.protocol.http.control

import org.apache.jmeter.config.Arguments
import org.apache.jmeter.config.ConfigTestElement
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema
import org.apache.jmeter.testelement.property.TestElementProperty
import org.apache.jmeter.treebuilder.Action
import org.apache.jmeter.treebuilder.TreeBuilder

fun TreeBuilder.httpRequestDefaults(configure: Action<ConfigTestElement> = Action {}) {
    ConfigTestElement::class {
        props {
            it[name] = "HTTP Request Defaults"
            it[guiClass] = "org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui"
        }
        setProperty(
            TestElementProperty(
                HTTPSamplerBaseSchema.arguments.name,
                Arguments().apply {
                    props {
                        it[guiClass] = "org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel"
                        it[testClass] = "org.apache.jmeter.config.Arguments"
                    }
                }
            )
        )
        configure(this)
    }
}

val ConfigTestElement.arguments: Arguments
    get() = getProperty(HTTPSamplerBaseSchema.arguments.name) as Arguments

fun ConfigTestElement.arguments(configure: Action<Arguments> = Action {}) {
    configure((getProperty(HTTPSamplerBaseSchema.arguments.name) as TestElementProperty).element as Arguments)
}
