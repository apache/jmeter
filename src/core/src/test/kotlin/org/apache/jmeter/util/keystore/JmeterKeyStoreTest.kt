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

package org.apache.jmeter.util.keystore

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.security.KeyStore

class JmeterKeyStoreTest {
    data class Case(val startIndex: Int, val endIndex: Int, val message: String? = null)
    companion object {
        @JvmStatic
        fun inputs() = listOf(
            Case(-1, 0),
            Case(0, -2, "-1 indicates to return the first alias only"),
            Case(1, 0),
        )
    }

    @ParameterizedTest
    @MethodSource("inputs")
    fun `throws IllegalArgumentException`(case: Case) {
        assertThrows<IllegalArgumentException> {
            JmeterKeyStore.getInstance(
                KeyStore.getDefaultType(), case.startIndex, case.endIndex, "defaultName"
            )
        }
    }
}
