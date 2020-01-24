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

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ConvertersSpec extends Specification {

    def "Convert number-like (#input) to instance of (#conversionDestClass)"() {
        given:
            def converter = Converters.getConverter(conversionDestClass)
        when:
            def result = converter.convert(input)
        then:
            result.class == expectedResult.class
            result == expectedResult || Math.abs(expectedResult - result) < precision
        where:
            input      | conversionDestClass | expectedResult          | precision
            " 42"      | Integer.class       | Integer.valueOf(42)     | 0
            "-5"       | int.class           | Integer.valueOf(-5)     | 0
            "5000000"  | Long.class          | Long.valueOf(5_000_000) | 0
            "500 "     | long.class          | Long.valueOf(500)       | 0
            "3.14"     | Double.class        | Double.valueOf(3.14)    | 0.0001
            " 100 "    | double.class        | Double.valueOf(100)     | 0.0001
            "+5"       | Float.class         | Float.valueOf(5)        | 0.0001
            " 1.2E16 " | float.class         | Float.valueOf(1.2E16)   | 0.0001
    }

    def "Convert (#input) to instance of (#conversionDestClass)"() {
        given:
            def converter = Converters.getConverter(conversionDestClass)
        when:
            def result = converter.convert(input)
        then:
            result == expectedResult
        where:
            input    | conversionDestClass | expectedResult
            "FALSE"  | Boolean.class       | Boolean.FALSE
            "true"   | boolean.class       | Boolean.TRUE
            " true"  | boolean.class       | Boolean.FALSE
            "fAlSe " | boolean.class       | Boolean.FALSE
            "a"      | Character.class     | 'a'
            "ä"      | char.class          | 'ä'
    }
}
