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

package org.apache.jorphan.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Random

class RandomStringGeneratorTest {

    @Test
    fun `alphanumeric generates only alphanumeric characters`() {
        val generator = RandomStringGenerator.alphanumeric(Random(42))
        val result = generator.generate(100)

        assertEquals(100, result.length)
        assertTrue(result.all { it.isLetterOrDigit() }) {
            "String should contain only alphanumeric characters, but got: $result"
        }
    }

    @Test
    fun `alphanumeric with seed produces reproducible results`() {
        val generator1 = RandomStringGenerator.alphanumeric(Random(12345))
        val generator2 = RandomStringGenerator.alphanumeric(Random(12345))

        val result1 = generator1.generate(50)
        val result2 = generator2.generate(50)

        assertEquals(result1, result2, "Same seed should produce same results")
    }

    @Test
    fun `ascii printable generates only printable ASCII characters`() {
        val generator = RandomStringGenerator.asciiPrintable(Random(42))
        val result = generator.generate(100)

        assertEquals(100, result.length)
        assertTrue(result.all { it in ' '..'~' }) {
            "String should contain only printable ASCII characters (32-126)"
        }
    }

    @Test
    fun `custom ranges generates only specified characters`() {
        val generator = RandomStringGenerator.fromRanges('0'..'9', 'a'..'f', random = Random(42))
        val result = generator.generate(100)

        assertEquals(100, result.length)
        assertTrue(result.all { it in '0'..'9' || it in 'a'..'f' }) {
            "String should contain only hex digits (0-9, a-f)"
        }
    }

    @Test
    fun `custom chars generates only specified characters`() {
        val vowels = "aeiouAEIOU"
        val generator = RandomStringGenerator.fromChars(vowels, Random(42))
        val result = generator.generate(100)

        assertEquals(100, result.length)
        assertTrue(result.all { it in vowels }) {
            "String should contain only vowels"
        }
    }

    @Test
    fun `custom chars from array generates only specified characters`() {
        val chars = charArrayOf('X', 'Y', 'Z')
        val generator = RandomStringGenerator.fromChars(chars, Random(42))
        val result = generator.generate(50)

        assertEquals(50, result.length)
        assertTrue(result.all { it in chars }) {
            "String should contain only X, Y, or Z"
        }
    }

    @Test
    fun `unicode generation has correct character length`() {
        val generator = RandomStringGenerator.unicode(Random(100))

        listOf(5, 10, 20, 50, 100).forEach { targetLength ->
            val result = generator.generate(targetLength)
            assertEquals(targetLength, result.length) {
                "Generated string should have exactly $targetLength chars"
            }
        }
    }

    @Test
    fun `unicode generation has no orphaned surrogates`() {
        val generator = RandomStringGenerator.unicode(Random(42))

        repeat(10) {
            val result = generator.generate(100)
            assertNoOrphanedSurrogates(result)
        }
    }

    @Test
    fun `unicode generation eventually produces surrogate pairs`() {
        val generator = RandomStringGenerator.unicode(Random(42))
        var foundSurrogatePair = false

        // Generate enough strings to likely encounter a surrogate pair
        repeat(50) {
            val result = generator.generate(50)
            if (containsSurrogatePair(result)) {
                foundSurrogatePair = true
                return@repeat
            }
        }

        assertTrue(foundSurrogatePair) {
            "Should generate at least one surrogate pair in 50 attempts with 50 chars each"
        }
    }

    @Test
    fun `empty string generation`() {
        val generator = RandomStringGenerator.alphanumeric(Random(42))
        val result = generator.generate(0)

        assertEquals("", result)
        assertEquals(0, result.length)
    }

    @Test
    fun `single character generation`() {
        val generator = RandomStringGenerator.alphanumeric(Random(42))
        val result = generator.generate(1)

        assertEquals(1, result.length)
        assertTrue(result[0].isLetterOrDigit())
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10, 50, 100, 500])
    fun `various lengths produce correct size strings`(length: Int) {
        val generator = RandomStringGenerator.alphanumeric(Random(42))
        val result = generator.generate(length)

        assertEquals(length, result.length)
    }

    @Test
    fun `negative length throws exception`() {
        val generator = RandomStringGenerator.alphanumeric(Random(42))

        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(-1)
        }
    }

    @Test
    fun `empty character array throws exception`() {
        val generator = RandomStringGenerator.fromChars(charArrayOf(), Random(42))

        assertThrows(IllegalStateException::class.java) {
            generator.generate(10)
        }
    }

    @Test
    fun `empty character ranges throws exception`() {
        val generator = RandomStringGenerator.fromRanges(random = Random(42))

        assertThrows(IllegalStateException::class.java) {
            generator.generate(10)
        }
    }

    @Test
    fun `unicode with odd length near end handles surrogate pairs correctly`() {
        val generator = RandomStringGenerator.unicode(Random(999))

        // Test odd lengths to ensure we handle the case where we might not have
        // enough space for a surrogate pair at the end
        listOf(1, 3, 5, 7, 9, 11, 13, 15).forEach { length ->
            val result = generator.generate(length)
            assertEquals(length, result.length) {
                "Generated string should have exactly $length chars"
            }
            assertNoOrphanedSurrogates(result)
        }
    }

    @Test
    fun `different random instances produce different results`() {
        val generator1 = RandomStringGenerator.alphanumeric(Random(111))
        val generator2 = RandomStringGenerator.alphanumeric(Random(222))

        val result1 = generator1.generate(50)
        val result2 = generator2.generate(50)

        assertFalse(result1 == result2) {
            "Different seeds should produce different results (unlikely but possible collision)"
        }
    }

    // Helper functions

    private fun assertNoOrphanedSurrogates(str: String) {
        var idx = 0
        while (idx < str.length) {
            val ch = str[idx]
            if (Character.isHighSurrogate(ch)) {
                assertTrue(idx + 1 < str.length) {
                    "High surrogate at index $idx must have a following character"
                }
                assertTrue(Character.isLowSurrogate(str[idx + 1])) {
                    "High surrogate at index $idx must be followed by low surrogate"
                }
                idx += 2
            } else {
                assertFalse(Character.isLowSurrogate(ch)) {
                    "Found orphaned low surrogate at index $idx"
                }
                idx++
            }
        }
    }

    private fun containsSurrogatePair(str: String): Boolean {
        var idx = 0
        while (idx < str.length) {
            if (Character.isHighSurrogate(str[idx]) &&
                idx + 1 < str.length &&
                Character.isLowSurrogate(str[idx + 1])
            ) {
                return true
            }
            idx++
        }
        return false
    }
}
