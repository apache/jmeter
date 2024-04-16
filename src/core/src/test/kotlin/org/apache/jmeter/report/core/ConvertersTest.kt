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

package org.apache.jmeter.report.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ConvertersTest {
    data class NumberCase(val input: String, val type: Class<*>?, val expected: Number, val precision: Number)
    data class SimpleCase(val input: String, val type: Class<*>?, val expected: Any)
    companion object {
        @JvmStatic
        fun numberCases() = listOf(
            NumberCase(" 42", Int::class.javaObjectType, Integer.valueOf(42), 0),
            NumberCase("-5", Int::class.javaPrimitiveType, Integer.valueOf(-5), 0),
            NumberCase("5000000", Long::class.javaObjectType, java.lang.Long.valueOf(5_000_000), 0),
            NumberCase("500 ", Long::class.javaPrimitiveType, java.lang.Long.valueOf(500), 0),
            NumberCase("3.14", Double::class.javaObjectType, java.lang.Double.valueOf(3.14), 0.0001),
            NumberCase(" 100 ", Double::class.javaPrimitiveType, java.lang.Double.valueOf(100.0), 0.0001),
            NumberCase("+5", Float::class.javaObjectType, java.lang.Float.valueOf(5.0f), 0.0001),
            NumberCase(" 1.2E16 ", Float::class.javaPrimitiveType, java.lang.Float.valueOf(1.2E16f), 0.0001),
        )

        @JvmStatic
        fun simpleCases() = listOf(
            SimpleCase("FALSE", Boolean::class.javaObjectType, false),
            SimpleCase("true", Boolean::class.javaPrimitiveType, true),
            SimpleCase(" true", Boolean::class.javaPrimitiveType, false),
            SimpleCase("fAlSe ", Boolean::class.javaPrimitiveType, false),
            SimpleCase("a", Char::class.javaObjectType, 'a'),
            SimpleCase("ä", Char::class.javaPrimitiveType, 'ä'),
        )
    }

    @ParameterizedTest
    @MethodSource("numberCases")
    fun convertNumbers(case: NumberCase) {
        val result = Converters.getConverter(case.type).convert(case.input)
        if (result != case.expected) {
            assertEquals(case.expected.toDouble(), (result as Number).toDouble(), case.precision.toDouble()) {
                "Converters.getConverter(${case.type}).convert(${case.input}) should be within ${case.precision} of ${case.expected}"
            }
        }
        assertEquals(case.expected::class.java, result::class.java) {
            "type of Converters.getConverter(${case.type}).convert(${case.input})"
        }
    }

    @ParameterizedTest
    @MethodSource("simpleCases")
    fun convertSimple(case: SimpleCase) {
        val result = Converters.getConverter(case.type).convert(case.input)
        assertEquals(case.expected, result) {
            "Converters.getConverter(${case.type}).convert(${case.input})"
        }
    }
}
