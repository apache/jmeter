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

package org.apache.jmeter.testelement.property

import org.apache.jmeter.engine.util.CompoundVariable
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FunctionPropertyTest {
    @Test
    fun `override value when running`() {
        JMeterContextService.getContext().apply {
            variables = JMeterVariables().apply {
                put("test_variable", "1")
            }
        }
        val p = FunctionProperty(
            "test",
            CompoundVariable("test_variable=\${test_variable}").function
        )
        p.isRunningVersion = true

        Assertions.assertEquals("test_variable=1", p.stringValue, "function should read value from the variables")

        p.objectValue = "overridden with setObjectValue"

        Assertions.assertEquals(
            "overridden with setObjectValue",
            p.stringValue,
            "function should read value passed with setObjectValue"
        )

        p.recoverRunningVersion(null)

        JMeterContextService.getContext().variables.put("test_variable", "2")

        Assertions.assertEquals(
            "test_variable=2",
            p.stringValue,
            "function should read value from the variables after recoverRunningVersion"
        )
    }
}
