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

package org.apache.jorphan.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class ConverterTest {
    data class ConvertCase(val input: Any?, val type: Class<*>?, val expected: Any?, val message: String? = null) {
        override fun toString(): String =
            "Case(input=${
            try {
                input?.toString()
            } catch (t: Throwable) {
                "exception from input.toString: ${t.message}"
            }
            }, type=${type?.name}, expected=$expected)"
    }
    data class InsertLineBreaksCase(val source: String?, val expected: String?, val message: String? = null)

    companion object {
        @JvmStatic
        fun insertLineBreaks() = listOf(
            InsertLineBreaksCase(null, ""),
            InsertLineBreaksCase("bar", "bar"),
            InsertLineBreaksCase("\nbar", "foobar"),
        )

        @JvmStatic
        fun conversionCases() = listOf(
            // Null input or type
            ConvertCase(null, null, "", "null input and null type"),
            ConvertCase("anything", null, "", "null type"),
            ConvertCase(23, null, "", "null type"),
            ConvertCase(null, String::class.java, "", "null input"),
            ConvertCase(null, Number::class.java, "", "null input"),
            // No change
            ConvertCase("anything", Any::class.java, "anything"),
            ConvertCase(23, Number::class.java, 23),
            // To string
            ConvertCase("anything", String::class.java, "anything"),
            ConvertCase(23, String::class.java, "23"),
            ConvertCase(42L, String::class.java, "42"),
            ConvertCase(64f, String::class.java, "64.0"),
            // To number
            ConvertCase(23f, Float::class.javaPrimitiveType, 23f),
            ConvertCase(42f, Float::class.javaObjectType, 42f),
            ConvertCase("42", Float::class.javaObjectType, 42f),
            ConvertCase(23f, Double::class.javaPrimitiveType, 23.0),
            ConvertCase(42f, Double::class.javaObjectType, 42.0),
            ConvertCase("42", Double::class.javaObjectType, 42.0),
            ConvertCase(23L, Int::class.javaPrimitiveType, 23),
            ConvertCase(42, Int::class.javaObjectType, 42),
            ConvertCase("42", Int::class.javaObjectType, 42),
            ConvertCase(23L, Long::class.javaPrimitiveType, 23L),
            ConvertCase(42, Long::class.javaObjectType, 42L),
            ConvertCase("42", Long::class.javaObjectType, 42L),
            ConvertCase("invalid", Float::class.javaObjectType, 0f),
            ConvertCase("invalid", Float::class.javaPrimitiveType, 0f),
            ConvertCase("invalid", Double::class.javaPrimitiveType, 0.0),
            ConvertCase("invalid", Double::class.javaObjectType, 0.0),
            ConvertCase("invalid", Int::class.javaPrimitiveType, 0),
            ConvertCase("invalid", Int::class.javaObjectType, 0),
            // To Class
            ConvertCase("java.lang.String", Class::class.java, String::class.java),
            ConvertCase("not.a.valid.class", Class::class.java, "not.a.valid.class"),
            // Other cases
            ConvertCase("", Boolean::class.javaObjectType, false),
            ConvertCase("true", Boolean::class.javaObjectType, true),
            ConvertCase(true, Boolean::class.javaObjectType, true),
            ConvertCase(false, Boolean::class.javaObjectType, false),
            ConvertCase("", Boolean::class.javaPrimitiveType, false),
            ConvertCase("true", Boolean::class.javaPrimitiveType, true),
            ConvertCase(true, Boolean::class.javaPrimitiveType, true),
            ConvertCase(false, Boolean::class.javaPrimitiveType, false),
            ConvertCase("filename", File::class.java, File("filename")),
            ConvertCase(File("filename"), File::class.java, File("filename")),
            ConvertCase("c", Character::class.javaObjectType, 'c'),
            ConvertCase("c", Character::class.javaPrimitiveType, 'c'),
            ConvertCase("char", Character::class.javaPrimitiveType, 'c'),
            ConvertCase(65, Character::class.javaPrimitiveType, 'A'),
            ConvertCase("", Character::class.javaPrimitiveType, ' '),
            ConvertCase(65.toChar(), Character::class.javaPrimitiveType, 'A'),
            ConvertCase(65.toByte(), Character::class.javaPrimitiveType, 'A'),
            ConvertCase(
                object {
                    override fun toString(): String =
                        throw Exception("deliberate exception to test Converter.convert()")
                },
                Character::class.javaPrimitiveType,
                ' ',
                "toString() throws exception"
            ),
            // Date
            ConvertCase(Date(30000), Date::class.java, Date(30000)),
            ConvertCase(
                toLocalDateFormat("08/20/2019", DateFormat.SHORT),
                Date::class.java,
                toLocalDate("08/20/2019", DateFormat.SHORT)
            )
        )

        private fun toLocalDateFormat(dateString: String, format: Int): String {
            val date = toLocalDate(dateString, format)
            return DateFormat.getDateInstance(format).format(date)
        }

        fun toLocalDate(dateString: String, format: Int): Date {
            val date = DateFormat
                .getDateInstance(format, Locale.forLanguageTag("en-US"))
                .parse(dateString)
            return date
        }
    }

    @ParameterizedTest
    @MethodSource("conversionCases")
    fun convert(case: ConvertCase) {
        assertEquals(case.expected, Converter.convert(case.input, case.type)) {
            "Converter.convert(${case.input}, ${case.type}): ${case.message}"
        }
    }

    @ParameterizedTest
    @MethodSource("insertLineBreaks")
    fun insertLineBreaks(case: InsertLineBreaksCase) {
        assertEquals(case.expected, Converter.insertLineBreaks(case.source, "foo")) {
            "Converter.insertLineBreaks(${case.source}, \"foo\")"
        }
    }
}
