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

package org.apache.jmeter.functions.gui

import org.apache.jmeter.config.Argument
import org.apache.jmeter.config.Arguments
import org.apache.jmeter.test.gui.DisabledIfHeadless
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class FunctionHelperTest {
    data class BuildCallCase(val functionName: String, val parameters: List<String>, val combined: String)

    companion object {
        @JvmStatic
        fun buildCallCases() = listOf(
            BuildCallCase("fname", listOf(), "\${fname}"),
            BuildCallCase("fname", listOf("a"), "\${fname(a)}"),
            BuildCallCase("fname", listOf("a,b"), "\${fname(a\\,b)}"),
            BuildCallCase("fname", listOf("a,b,c"), "\${fname(a\\,b\\,c)}"),
            BuildCallCase("fname", listOf("a", "b"), "\${fname(a,b)}"),
            BuildCallCase("fname", listOf("a,b", "c"), "\${fname(a\\,b,c)}"),
            BuildCallCase("fname", listOf("\\\${f(a,b)}"), "\${fname(\\\${f(a\\,b)})}"),
            BuildCallCase("fname", listOf("\${f(a,b)},c,\${g(d,e)}", "h"), "\${fname(\${f(a,b)}\\,c\\,\${g(d,e)},h)}"),
            BuildCallCase("fname", listOf("a,\${f(b,\${g(c,d)},e)},f", "h"), "\${fname(a\\,\${f(b,\${g(c,d)},e)}\\,f,h)}"),
        )
    }

    @DisabledIfHeadless
    @ParameterizedTest
    @MethodSource("buildCallCases")
    fun `construct correct call string for parameters #parameters`(case: BuildCallCase) {
        val args = Arguments()
        args.setArguments(case.parameters.map { Argument("dummy$it", it) })

        assertEquals(
            case.combined,
            FunctionHelper.buildFunctionCallString(case.functionName, args).toString()
        ) {
            "buildFunctionCallString(${case.functionName}, ${case.parameters})"
        }
    }
}
