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

package org.apache.jmeter.assertions

import org.apache.jmeter.samplers.SampleResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CompareAssertionTest {
    val sut = CompareAssertion()

    data class AssertCase(
        val compareContent: Boolean,
        val compareTime: Long,
        val content: String,
        val elapsed: Long,
        val skip: String?,
        val isFailure: Boolean
    )

    companion object {
        fun simpleResult(data: String, elapsed: Long) =
            SampleResult(0, elapsed).apply {
                setResponseData(data, Charsets.UTF_8.name())
                sampleEnd()
            }

        @JvmStatic
        fun assertCases() = listOf(
            AssertCase(true, -1, "OK", 100, null, false),
            AssertCase(true, -1, "different", 100, null, true),
            AssertCase(false, -1, "different", 100, null, false),
            AssertCase(false, -1, "different OK", 100, "d\\w+\\s", false),
            AssertCase(true, 10, "OK", 120, null, true),
            AssertCase(true, 10, "OK", 110, null, false),
        )
    }

    @Test
    fun `Result is simple assertionResult when only one response is given`() {
        sut.name = "myName"
        sut.iterationStart(null)
        val assertionResult = sut.getResult(null)
        assertEquals("myName", assertionResult.name)
    }

    @ParameterizedTest
    @MethodSource("assertCases")
    fun isFailure(case: AssertCase) {
        sut.name = "myName"
        val firstResponse = simpleResult("OK", 100)
        sut.iterationStart(null)
        sut.getResult(firstResponse)
        sut.isCompareContent = case.compareContent
        sut.compareTime = case.compareTime
        case.skip?.let {
            val subst = SubstitutionElement()
            subst.regex = it
            sut.stringsToSkip = listOf(subst)
        }
        val assertionResult = sut.getResult(simpleResult(case.content, case.elapsed))
        assertEquals(case.isFailure, assertionResult.isFailure)
    }
}
