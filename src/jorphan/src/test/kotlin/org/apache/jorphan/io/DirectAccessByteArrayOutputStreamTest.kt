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

class DirectAccessByteArrayOutputStreamTest {
    @Test
    fun testWriteSingleByte() {
        val stream = DirectAccessByteArrayOutputStream()
        stream.write(65) // ASCII for 'A'
        val result = stream.toByteArray()
        assertArrayEquals(byteArrayOf(65), result)
    }

    @Test
    fun testWriteMultipleBytes() {
        val stream = DirectAccessByteArrayOutputStream()
        val data = byteArrayOf(65, 66, 67) // 'A', 'B', 'C'
        stream.write(data, 0, data.size)
        val result = stream.toByteArray()
        assertArrayEquals(data, result)
    }

    @Test
    fun testResizeBuffer() {
        val stream = DirectAccessByteArrayOutputStream(2)
        val data = ByteArray(10) { (it + 65).toByte() } // 'A', 'B', ..., 'J'
        stream.write(data, 0, data.size)
        val result = stream.toByteArray()
        assertArrayEquals(data, result)
    }

    @Test
    fun testReset() {
        val stream = DirectAccessByteArrayOutputStream()
        stream.write(65) // Write 'A'
        stream.reset()
        assertEquals(0, stream.size())
        assertArrayEquals(byteArrayOf(), stream.toByteArray())
    }

    @Test
    fun testToStringMethods() {
        val stream = DirectAccessByteArrayOutputStream()
        val data = "Hello, World!"
        stream.write(data.toByteArray(Charsets.UTF_8))
        assertEquals(data, stream.toString(Charsets.UTF_8))
        assertEquals(data, stream.toString("UTF-8"))
    }

    @Test
    fun testSize() {
        val stream = DirectAccessByteArrayOutputStream()
        stream.write(65) // Write 'A'
        stream.write(66) // Write 'B'
        assertEquals(2, stream.size())
    }
}
