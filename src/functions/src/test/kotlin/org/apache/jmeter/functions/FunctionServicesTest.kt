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

package org.apache.jmeter.functions

import org.apache.jmeter.util.JMeterUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.ServiceLoader

class FunctionServicesTest {
    @Test
    fun `__counter loads`() {
        val functions = JMeterUtils.loadServicesAndScanJars(
            Function::class.java,
            ServiceLoader.load(Function::class.java),
            Thread.currentThread().contextClassLoader
        ) { service, className, throwable ->
            throw IllegalStateException(
                "Failed to load $service implementations, implementation: $className",
                throwable
            )
        }.map { it.referenceKey }

        Assertions.assertTrue("__counter" in functions) {
            "__counter function should be discoverable with ServiceLoader.load(Function), all found functions are $functions"
        }
    }
}
