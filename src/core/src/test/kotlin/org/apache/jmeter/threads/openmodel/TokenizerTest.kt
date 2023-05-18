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

package org.apache.jmeter.threads.openmodel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class TokenizerTest {
    class Case(val input: String, val expected: String) {
        override fun toString() = input
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Case(
                "rate(0 per min)",
                "[Identifier(rate):0, (:4, Number(0):5, Identifier(per):7, Identifier(min):11, ):14]"
            ),
            Case(
                "rate(50/min) /* comment */ even_arrivals(50 min/**/) rate(60/min)",
                "[Identifier(rate):0, (:4, Number(50):5, /:7, Identifier(min):8, ):11, Identifier(even_arrivals):27, (:40, Number(50):41, Identifier(min):44, ):51, Identifier(rate):53, (:57, Number(60):58, /:60, Identifier(min):61, ):64]"
            )
        )
    }

    @ParameterizedTest
    @MethodSource("data")
    fun verifyTest(case: Case) {
        assertEquals(case.expected, Tokenizer.tokenize(case.input).toString(), case.input)
    }
}
