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
import org.junit.jupiter.params.provider.CsvSource

class BooleanUtilsTest {
    @ParameterizedTest
    @CsvSource(
        textBlock = """
        abc,
        ,
        true,true
        on,true
        y,true
        t,true
        yes,true
        1,true
        abc,
        false,false
        off,false
        n,false
        f,false
        no,false
        0,false
        TRUE,true
        ON,true
        Y,true
        T,true
        YES,true
        1,true
        ABC,
        FALSE,false
        OFF,false
        N,false
        F,false
        NO,false
        0,false"""
    )
    fun test(input: String?, expected: Boolean?) {
        assertEquals(expected, BooleanUtils.toBooleanObject(input)) {
            "BooleanUtils.toBooleanObject($input)"
        }
    }
}
