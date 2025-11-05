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

import java.util.Random

/**
 * Random string generator with configurable character sets and proper Unicode support.
 *
 * @property random The random number generator to use
 * @property charSource The source of characters to use for generation
 */
public class RandomStringGenerator(
    private val random: Random,
    private val charSource: CharSource = CharSource.Alphanumeric
) {
    /**
     * Defines the source of characters for string generation
     */
    public sealed interface CharSource {
        /**
         * Generate from specific character ranges
         */
        public data class Ranges(val ranges: List<CharRange>) : CharSource

        /**
         * Generate from specific characters
         */
        public data class Chars(val chars: CharArray) : CharSource {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Chars) return false
                return chars.contentEquals(other.chars)
            }

            override fun hashCode(): Int = chars.contentHashCode()
        }

        /**
         * Generate any valid Unicode character (excluding surrogates when used alone)
         */
        public object AnyUnicode : CharSource

        /**
         * Generate alphanumeric characters (a-z, A-Z, 0-9)
         */
        public object Alphanumeric : CharSource

        /**
         * Generate ASCII printable characters (32-126)
         */
        public object AsciiPrintable : CharSource
    }

    /**
     * Generate a random string with the specified number of characters.
     *
     * For Unicode generation with surrogate pairs:
     * - When a high surrogate is generated, it's automatically paired with a low surrogate
     * - The pair counts as 2 chars in Java's String representation
     *
     * @param chars The number of Java chars to generate (not code points)
     * @return A random string of the specified length
     */
    public fun generate(chars: Int): String {
        if (chars < 0) {
            throw IllegalArgumentException("Number of chars must be non-negative, got: $chars")
        }
        if (chars == 0) {
            return ""
        }

        return when (charSource) {
            is CharSource.Ranges -> generateFromRanges(chars, charSource.ranges)
            is CharSource.Chars -> generateFromChars(chars, charSource.chars)
            is CharSource.AnyUnicode -> generateUnicode(chars)
            is CharSource.Alphanumeric -> generateFromRanges(chars, ALPHANUMERIC_RANGES)
            is CharSource.AsciiPrintable -> generateFromRange(chars, ' '..'~')
        }
    }

    private fun generateFromRanges(length: Int, ranges: List<CharRange>): String {
        if (ranges.isEmpty()) {
            throw IllegalStateException("No character ranges provided")
        }

        val allChars = ranges.flatMap { range -> range.toList() }
        if (allChars.isEmpty()) {
            throw IllegalStateException("Character ranges are empty")
        }

        return buildString(length) {
            repeat(length) {
                append(allChars[random.nextInt(allChars.size)])
            }
        }
    }

    private fun generateFromRange(length: Int, range: CharRange): String {
        val chars = range.toList()
        return buildString(length) {
            repeat(length) {
                append(chars[random.nextInt(chars.size)])
            }
        }
    }

    private fun generateFromChars(length: Int, chars: CharArray): String {
        if (chars.isEmpty()) {
            throw IllegalStateException("Character array is empty")
        }

        return buildString(length) {
            repeat(length) {
                append(chars[random.nextInt(chars.size)])
            }
        }
    }

    private fun generateUnicode(targetLength: Int): String {
        val result = StringBuilder(targetLength)
        var currentLength = 0

        while (currentLength < targetLength) {
            // Generate a random code point from the entire Unicode range
            // We'll generate from all valid Unicode code points
            val codePoint = generateValidUnicodeCodePoint()

            // Check if we have enough space for this code point
            val charCount = Character.charCount(codePoint)
            if (currentLength + charCount > targetLength) {
                // Not enough space for a surrogate pair, generate a BMP character instead
                val bmpCodePoint = generateBMPCodePoint()
                result.appendCodePoint(bmpCodePoint)
                currentLength++
            } else {
                result.appendCodePoint(codePoint)
                currentLength += charCount
            }
        }

        return result.toString()
    }

    /**
     * Generate a valid Unicode code point, including those requiring surrogate pairs
     */
    private fun generateValidUnicodeCodePoint(): Int {
        // Unicode code points range from 0x0000 to 0x10FFFF
        // But we need to exclude:
        // - Surrogate range: 0xD800 to 0xDFFF (these are not valid code points)
        // - Non-characters and undefined code points

        while (true) {
            val codePoint = when (random.nextInt(3)) {
                0 -> {
                    // Basic Multilingual Plane (0x0000 - 0xFFFF, excluding surrogates)
                    // More likely to generate BMP characters
                    val cp = random.nextInt(0xD800) // 0x0000 to 0xD7FF
                    if (Character.isDefined(cp)) cp else continue
                }
                1 -> {
                    // BMP after surrogates (0xE000 - 0xFFFF)
                    val cp = 0xE000 + random.nextInt(0x10000 - 0xE000)
                    if (Character.isDefined(cp)) cp else continue
                }
                else -> {
                    // Supplementary planes (0x10000 - 0x10FFFF)
                    // These require surrogate pairs
                    val cp = 0x10000 + random.nextInt(0x110000 - 0x10000)
                    if (Character.isDefined(cp)) cp else continue
                }
            }

            // Ensure it's a valid, defined code point
            if (Character.isValidCodePoint(codePoint) && Character.isDefined(codePoint)) {
                return codePoint
            }
        }
    }

    /**
     * Generate a Basic Multilingual Plane code point (no surrogate pair needed)
     */
    private fun generateBMPCodePoint(): Int {
        while (true) {
            val codePoint = if (random.nextBoolean()) {
                // Before surrogate range
                random.nextInt(0xD800)
            } else {
                // After surrogate range
                0xE000 + random.nextInt(0x10000 - 0xE000)
            }

            if (Character.isDefined(codePoint)) {
                return codePoint
            }
        }
    }

    public companion object {
        private val ALPHANUMERIC_RANGES = listOf(
            'a'..'z',
            'A'..'Z',
            '0'..'9'
        )

        /**
         * Create a generator for alphanumeric strings
         */
        @JvmStatic
        public fun alphanumeric(random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.Alphanumeric)

        /**
         * Create a generator for ASCII printable strings
         */
        @JvmStatic
        public fun asciiPrintable(random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.AsciiPrintable)

        /**
         * Create a generator for any Unicode strings (with proper surrogate pair handling)
         */
        @JvmStatic
        public fun unicode(random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.AnyUnicode)

        /**
         * Create a generator from character ranges
         */
        @JvmStatic
        public fun fromRanges(vararg ranges: CharRange, random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.Ranges(ranges.toList()))

        /**
         * Create a generator from specific characters
         */
        @JvmStatic
        public fun fromChars(chars: CharArray, random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.Chars(chars))

        /**
         * Create a generator from a string of characters
         */
        @JvmStatic
        public fun fromChars(chars: String, random: Random = Random()): RandomStringGenerator =
            RandomStringGenerator(random, CharSource.Chars(chars.toCharArray()))
    }
}
