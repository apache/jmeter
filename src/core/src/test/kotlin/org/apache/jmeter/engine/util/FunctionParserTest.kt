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

import com.google.auto.service.AutoService
import org.apache.jmeter.functions.Function
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.samplers.Sampler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FunctionParserTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "\${__func()}",
            "\${ __func()}",
            "\${__func() }",
            "\${ __func() }",
        ]
    )
    fun `simple function call`(input: String) {
        val parser = FunctionParser()
        val result = parser.compileString(input)

        Assertions.assertEquals(arrayListOf(Func()), result) {
            "FunctionParser.compileString($input)"
        }
    }

    @AutoService(Function::class)
    class Func : Function {
        override fun execute(previousResult: SampleResult?, currentSampler: Sampler?): String {
            TODO()
        }

        override fun setParameters(parameters: MutableCollection<CompoundVariable>) {
        }

        override fun getReferenceKey(): String = "__func"

        override fun getArgumentDesc(): List<String> = listOf()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}
