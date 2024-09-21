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

package org.apache.jmeter.engine.util

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.property.FunctionProperty
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.StringProperty
import org.apiguardian.api.API

/**
 * Converts a [StringProperty] containing functions to the corresponding [FunctionProperty] equivalent, example:
 * ${__time()}_${__threadNum()}_${__machineName()} will become a FunctionProperty of
 * a [CompoundVariable] containing  3 functions.
 *
 * Note: this implementation does not convert nested properties, use [TestElementPropertyTransformer.USE_FUNCTIONS]
 * if you need process all properties of a [TestElement].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public object TransformStringsIntoFunctions : PropertyTransformer {
    private val parser = CompoundVariable()

    override fun transform(input: JMeterProperty): JMeterProperty {
        if (input !is StringProperty) {
            return input
        }
        synchronized(parser) {
            // TODO: move parsing logic out of CompoundVariable
            parser.clear()
            parser.setParameters(input.stringValue)
            if (parser.hasFunction()) {
                return FunctionProperty(input.getName(), parser.function)
            }
        }
        return input
    }
}
