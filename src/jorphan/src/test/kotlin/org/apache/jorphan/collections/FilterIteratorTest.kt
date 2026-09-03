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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FilterIteratorTest {

    private fun <T> assertFilteredResults(collection: Collection<T>, predicate: (T) -> Boolean) {
        assertEquals(
            collection.filter(predicate).toList().toString(),
            FilterIterator(collection.iterator(), predicate).asSequence().toList().toString(),
        ) {
            "input: ${collection.toList()}"
        }
    }

    @Test
    fun `filter even numbers from list`() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        assertFilteredResults(numbers) { it % 2 == 0 }
    }

    @Test
    fun `filter odd numbers from list`() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        assertFilteredResults(numbers) { it % 2 != 0 }
    }

    @Test
    fun `filter strings by length`() {
        val words = listOf("a", "ab", "abc", "abcd", "abcde")
        assertFilteredResults(words) { it.length > 2 }
    }

    @Test
    fun `filter with no matches returns empty iterator`() {
        val numbers = listOf(1, 3, 5, 7, 9)
        assertFilteredResults(numbers) { it % 2 == 0 }
    }

    @Test
    fun `filter with all matches returns all elements`() {
        val numbers = listOf(2, 4, 6, 8, 10)
        assertFilteredResults(numbers) { it % 2 == 0 }
    }

    @Test
    fun `filter empty list returns empty iterator`() {
        val empty = emptyList<Int>()
        assertFilteredResults(empty) { it > 0 }
    }

    @Test
    fun `filter with null values`() {
        val items = listOf("a", null, "b", null, "c")
        assertFilteredResults(items) { it != null }
    }

    @Test
    fun `filter with always true predicate`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertFilteredResults(numbers) { true }
    }

    @Test
    fun `filter with always false predicate`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertFilteredResults(numbers) { false }
    }

    @Test
    fun `calling next without hasNext throws exception`() {
        val numbers = listOf(1, 3, 5)
        val filtered = FilterIterator(numbers.iterator()) { it % 2 == 0 }

        assertThrows(NoSuchElementException::class.java) {
            filtered.next()
        }
    }

    @Test
    fun `calling next after exhausted throws exception`() {
        val numbers = listOf(2)
        val filtered = FilterIterator(numbers.iterator()) { it % 2 == 0 }

        assertTrue(filtered.hasNext())
        assertEquals(2, filtered.next())
        assertFalse(filtered.hasNext())

        assertThrows(NoSuchElementException::class.java) {
            filtered.next()
        }
    }

    @Test
    fun `multiple calls to hasNext are idempotent`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val filtered = FilterIterator(numbers.iterator()) { it % 2 == 0 }

        assertTrue(filtered.hasNext())
        assertTrue(filtered.hasNext())
        assertTrue(filtered.hasNext())
        assertEquals(2, filtered.next())
    }

    @Test
    fun `filter chain with multiple predicates`() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        // Filter even numbers, then filter those > 5
        val filtered1 = FilterIterator(numbers.iterator()) { it % 2 == 0 }
        val filtered2 = FilterIterator(filtered1) { it > 5 }

        val result = filtered2.asSequence().toList()

        assertEquals(listOf(6, 8, 10), result)
    }

    @Test
    fun `filter with single element matching`() {
        val numbers = listOf(1, 2, 3)
        val filtered = FilterIterator(numbers.iterator()) { it == 2 }

        assertTrue(filtered.hasNext())
        assertEquals(2, filtered.next())
        assertFalse(filtered.hasNext())
    }
}
