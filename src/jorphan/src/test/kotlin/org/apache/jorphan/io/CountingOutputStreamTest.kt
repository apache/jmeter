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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.ByteArrayOutputStream

class CountingOutputStreamTest {
    @Test
    fun writeByte() {
        val target = ByteArrayOutputStream()
        val counting = CountingOutputStream(target)
        counting.write(42)
        counting.write(43)
        assertAll(
            { assertArrayEquals(byteArrayOf(42, 43), target.toByteArray()) },
            { assertEquals(2L, counting.bytesWritten, "single byte writes should be counted as well") },
        )
    }

    @Test
    fun writeArray() {
        val target = ByteArrayOutputStream()
        val counting = CountingOutputStream(target)
        counting.write(byteArrayOf(42, 43))
        counting.write(byteArrayOf(44, 45, 46), 1, 2)
        assertAll(
            { assertArrayEquals(byteArrayOf(42, 43, 45, 46), target.toByteArray()) },
            { assertEquals(4L, counting.bytesWritten) },
        )
    }

    @Test
    fun countsNothingWhenNothingIsWritten() {
        assertEquals(0L, CountingOutputStream(ByteArrayOutputStream()).bytesWritten)
    }
}
