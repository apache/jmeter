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

import org.apache.jmeter.engine.util.CompoundVariable
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

class ChangeCaseTest {
    data class ExecuteCase(val input: String, val mode: String, val output: String)

    companion object {
        @JvmStatic
        fun executeCases() = listOf(
            ExecuteCase("simple", "lower", "simple"),
            ExecuteCase("simple", "upper", "SIMPLE"),
            ExecuteCase("simple", "capitalize", "Simple"),
            ExecuteCase("simple", "", "SIMPLE"),
            ExecuteCase(" with space ", "lower", " with space "),
            ExecuteCase(" with space ", "upper", " WITH SPACE "),
            ExecuteCase(" with space ", "capitalize", " with space "),
            ExecuteCase("#_with-signs.", "lower", "#_with-signs."),
            ExecuteCase("#_with-signs.", "upper", "#_WITH-SIGNS."),
            ExecuteCase("#_with-signs.", "capitalize", "#_with-signs."),
            ExecuteCase("m4u file", "lower", "m4u file"),
            ExecuteCase("m4u file", "upper", "M4U FILE"),
            ExecuteCase("m4u file", "capitalize", "M4u file"),
            ExecuteCase("WITH Ümläuts", "lower", "with ümläuts"),
            ExecuteCase("WITH Ümläuts", "upper", "WITH ÜMLÄUTS"),
            ExecuteCase("WITH Ümläuts", "capitalize", "WITH Ümläuts"),
            ExecuteCase("+ - special space", "lower", "+ - special space"),
            ExecuteCase("+ - special space", "upper", "+ - SPECIAL SPACE"),
            ExecuteCase("+ - special space", "capitalize", "+ - special space"),
            ExecuteCase(" ", "lower", " "),
            ExecuteCase(" ", "upper", " "),
            ExecuteCase(" ", "capitalize", " "),
        ).also {
            assumeTrue(
                "i".uppercase(Locale.getDefault()) == "I" && "I".lowercase(Locale.getDefault()) == "i",
                "ChangeCase does not behave well in tr_TR locale, see https://github.com/apache/jmeter/issues/5723"
            )
        }
    }

    @ParameterizedTest
    @MethodSource("executeCases")
    fun changeCase(case: ExecuteCase) {
        val changeCase = ChangeCase()
        val jMCtx = JMeterContextService.getContext()
        val result = SampleResult()
        result.setResponseData("dummy data", null)
        jMCtx.variables = JMeterVariables()
        jMCtx.previousResult = result
        changeCase.setParameters(listOf(CompoundVariable(case.input), CompoundVariable(case.mode)))

        assertEquals(case.output, changeCase.execute(result, null))
    }
}
