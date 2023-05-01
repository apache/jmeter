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

package org.apache.jmeter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArgumentTest {

    @ParameterizedTest
    @CsvSource(value = {
            "simple_name,simple_name",
            " with_spaces ,with_spaces",
            "\twith_tabs\t,with_tabs"
    },ignoreLeadingAndTrailingWhitespace = false)
    void setName(String name, String expectedName) {
        Argument arg = new Argument();
        arg.setName(name);
        assertEquals(expectedName, arg.getName());
    }
}
