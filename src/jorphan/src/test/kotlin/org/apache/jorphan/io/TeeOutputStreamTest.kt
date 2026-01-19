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

package org.apache.jorphan.io

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.ByteArrayOutputStream

class TeeOutputStreamTest {
    @Test
    fun writeByte() {
        val first = ByteArrayOutputStream()
        val second = ByteArrayOutputStream()
        val tee = TeeOutputStream(first, second)
        tee.write(42)
        assertAll(
            { assertArrayEquals(byteArrayOf(42), first.toByteArray()) },
            { assertArrayEquals(byteArrayOf(42), second.toByteArray()) },
        )
    }

    @Test
    fun writeArray() {
        val first = ByteArrayOutputStream()
        val second = ByteArrayOutputStream()
        val tee = TeeOutputStream(first, second)
        tee.write(byteArrayOf(42, 43))
        tee.write(byteArrayOf(44))
        assertAll(
            { assertArrayEquals(byteArrayOf(42, 43, 44), first.toByteArray()) },
            { assertArrayEquals(byteArrayOf(42, 43, 44), second.toByteArray()) },
        )
    }
}
