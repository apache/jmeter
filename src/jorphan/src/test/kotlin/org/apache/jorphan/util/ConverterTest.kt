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


import java.text.DateFormat

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ConverterSpec extends Specification {

    def 'convert #value to #type should give "" when value or type is null'() {
        expect:
            Converter.convert(value, type) == ""
        where:
            value      | type
            null       | null
            "anything" | null
            23         | null
            null       | String.class
            null       | Number.class
    }

    def "convert #value to #type should downcast gracefully and give [#expected]"() {
        expect:
            Converter.convert(value, type) == expected
        where:
            value      | type         | expected
            "anything" | Object.class | "anything"
            23         | Number.class | 23
    }

    def "convert #value to string should give [#expected]"() {
        expect:
            Converter.convert(value, String.class) == expected
        where:
            value      | expected
            "anything" | "anything"
            23         | "23"
            42L        | "42"
            64f        | "64.0"
    }

    def "convert #value to number #type should give number [#expected]"() {
        expect:
            Converter.convert(value, type) == expected
        where:
            value     | type          | expected
            23f       | float.class   | 23f
            42f       | Float.class   | 42f
            "42"      | Float.class   | 42f
            23f       | double.class  | 23d
            42f       | Double.class  | 42d
            "42"      | Double.class  | 42d
            23L       | int.class     | 23
            42        | Integer.class | 42
            "42"      | Integer.class | 42
            23L       | long.class    | 23L
            42        | Long.class    | 42L
            "42"      | Long.class    | 42L
            "invalid" | Float.class   | 0f
            "invalid" | float.class   | 0f
            "invalid" | double.class  | 0d
            "invalid" | Double.class  | 0d
            "invalid" | int.class     | 0
            "invalid" | Integer.class | 0
    }

    def "Convert #value to Class gives #expected"() {
        expect:
            Converter.convert(value, Class.class) == expected
        where:
            value               | expected
            "java.lang.String"  | String.class
            "not.a.valid.class" | "not.a.valid.class"
    }

    def "Convert '#value' to #type"() {
        expect:
            if (value == "OBJECT_WITH_BROKEN_TO_STRING") {
                value = new Object() { String toString() { 1/0 } }
            }
            Converter.convert(value, type) == expected
        where:
            value                | type            | expected
            ""                   | Boolean.class   | false
            "true"               | Boolean.class   | true
            true                 | Boolean.class   | true
            false                | Boolean.class   | false
            ""                   | boolean.class   | false
            "true"               | boolean.class   | true
            true                 | boolean.class   | true
            false                | boolean.class   | false
            "filename"           | File.class      | new File("filename")
            new File("filename") | File.class      | new File("filename")
            "c"                  | Character.class | 'c'
            "c"                  | char.class      | 'c'
            "char"               | char.class      | 'c'
            65                   | char.class      | 'A'
            ''                   | char.class      | ' '
            Character.valueOf((char) 65)       | char.class | 'A'
            Byte.valueOf((byte)65)             | char.class | 'A'
            "OBJECT_WITH_BROKEN_TO_STRING" | char.class | ' '
    }

    def "Convert to date from '#value'"() {
        expect:
            Math.abs(Converter.convert(value, Date.class).getTime() - expected.getTime()) < 1000
        where:
            value                  | expected
            ""                     | new Date()
            new Date(30000)   | new Date(30000)
            toLocalDateFormat("08/20/2019", DateFormat.SHORT) | toLocalDate("08/20/2019", DateFormat.SHORT)
    }

    def "Convert to Calendar from '#value'"() {
        expect:
            Math.abs(
                Converter.convert(value, Calendar.class).getTime().getTime() - expected.getTime()) < 1000
        where:
            value                  | expected
            ""                     | new Date()
            new Date(30000)   | new Date(30000)
            toLocalDateFormat("08/20/2019", DateFormat.SHORT) | toLocalDate("08/20/2019", DateFormat.SHORT)
    }

    def toLocalDateFormat(String dateString, int format) {
        def date = toLocalDate(dateString, format)
        return DateFormat.getDateInstance(format).format(date)
    }

    def toLocalDate(String dateString, int format) {
        def date = DateFormat
                .getDateInstance(format, Locale.forLanguageTag("en-US"))
                .parse(dateString)
        return date
    }

    def "line breaks should be replaced in '#source' to '#expected'"() {
        expect:
           Converter.insertLineBreaks(source, "foo") == expected
        where:
           source | expected
           null   | ""
           "bar"  | "bar"
           "\nbar"| "foobar"
    }
}
