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

package org.apache.jorphan.collections

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.concurrent.ConcurrentHashMap

class IdentityKeyTest {
    data class Key(val value: Int)

    @Test
    fun `concurrentHashMap with same keys`() {
        val map = ConcurrentHashMap<IdentityKey<Key?>, Int>()
        val k1 = IdentityKey(Key(1))
        map[k1] = 1
        val k2 = IdentityKey(Key(1))
        map[k2] = 2
        val kNull = IdentityKey<Key?>(null)
        map[kNull] = 3
        assertAll(
            {
                assertEquals(3, map.size) {
                    "Map should contain 3 elements, actual map is $map"
                }
            },
            { assertEquals(1, map[k1], "map[Key(1)]") },
            { assertEquals(2, map[k2], "map[Key(1)]") },
            { assertEquals(3, map[kNull], "map[null]") },
        )
    }

    @Test
    fun `toString test`() {
        assertEquals("IdentityKey{contents}", IdentityKey("contents").toString()) {
            """IdentityKey("contents").toString()"""
        }
    }
}
