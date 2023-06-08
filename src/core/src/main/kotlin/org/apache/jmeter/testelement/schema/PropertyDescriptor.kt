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

import org.apache.jmeter.testelement.JMeterPropertySchemaUnchecked
import org.apache.jmeter.testelement.TestElement
import org.apiguardian.api.API
import java.io.Serializable

/**
 * Describes a [JMeterProperty]: name, default value, and provides accessors for properties.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public interface PropertyDescriptor<in Schema : BaseTestElementSchema, Value> : Serializable {
    public open class Builder<in Schema : BaseTestElementSchema, DefaultType : Any>(
        public val name: String,
        public val default: DefaultType?
    )

    public val shortName: String
    public val name: String
    public val defaultValue: Value?

    public val defaultValueAsString: String?
        get() = defaultValue?.toString()

    @JMeterPropertySchemaUnchecked
    public fun remove(target: TestElement) {
        target.removeProperty(name)
    }

    public operator fun set(target: TestElement, value: String?) {
        target[this] = value
    }

    public fun getString(target: TestElement): String =
        target.getString(this)

    public val asString: StringPropertyDescriptor<Schema>
        get() = if (this is StringPropertyDescriptor) this else StringPropertyDescriptor(shortName, name, defaultValueAsString)
}
