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

package org.apache.jmeter.protocol.http.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ConversionUtilsTest {
    @ParameterizedTest
    @CsvSource(
        ignoreLeadingAndTrailingWhitespace = false,
        value = [
            "hello,hello",
            "%,%25",
            "\",%22",
            " ,%20",
            "😃,%F0%9F%98%83",
            "comment ça va?,comment%20%C3%A7a%20va%3F",
        ]
    )
    fun percentEncode(input: String, output: String) {
        assertEquals(output, ConversionUtils.percentEncode(input)) {
            "ConversionUtils.percentEncode($input)"
        }
    }
}
