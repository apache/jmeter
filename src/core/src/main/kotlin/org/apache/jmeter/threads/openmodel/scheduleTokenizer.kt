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

package org.apache.jmeter.threads.openmodel

import org.apiguardian.api.API
import java.util.Locale
import java.util.regex.Matcher

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class TokenizerException(public val input: String, public val position: Int, message: String) :
    Exception(
        "$message at position $position: " +
            input.substring(position until (position + 20).coerceAtMost(input.length))
    )

/**
 * Tokenizes string with schedule like `rate(5 min) ...` into tokens
 *   `rate`, `(`, `5`, `min`, `)`, so [ScheduleParser] can parse them and build [ThreadSchedule].
 */
internal object Tokenizer {
    private val WHITESPACE = Regex("""(?>\s+|/\*(?:(?!\*/).)*+\*/)|//[^\n\r]*+[\n\r]*+""")
    private val IDENTIFIER = Regex("""\p{Alpha}(?>\p{Alnum}|_)*+""")
    private val NUMBER = Regex("""(?>\d++(?:\.\d++)?|\.\d++)""")

    data class TokenPosition(val pos: Int, val token: Token) {
        override fun toString() = "$token:$pos"
    }

    sealed interface Token {
        val image: String
    }

    data class IdentifierToken(override val image: String) : Token {
        override fun equals(other: Any?) = other is IdentifierToken && image.equals(other.image, ignoreCase = true)
        override fun hashCode() = image.lowercase(Locale.ROOT).hashCode()
        override fun toString() = "Identifier($image)"
    }

    data class NumberToken(override val image: String) : Token {
        override fun toString() = "Number($image)"
    }

    object OpenParenthesisToken : Token {
        override val image: String get() = "("
        override fun toString() = "("
    }

    object CloseParenthesisToken : Token {
        override val image: String get() = ")"
        override fun toString() = ")"
    }

    object DivideToken : Token {
        override val image: String get() = "/"
        override fun toString() = "/"
    }

    private fun Regex.prepareMatcher(value: String) =
        toPattern().matcher(value).useAnchoringBounds(false).useTransparentBounds(true).region(0, value.length)

    private fun Matcher.lookingAt(pos: Int): Boolean =
        region(pos, regionEnd()).lookingAt()

    fun tokenize(value: String): List<TokenPosition> {
        val res = mutableListOf<TokenPosition>()
        var pos = 0
        val mWhitespace = WHITESPACE.prepareMatcher(value)
        val mIdentifier = IDENTIFIER.prepareMatcher(value)
        val mNumber = NUMBER.prepareMatcher(value)
        while (pos < value.length) {
            if (mWhitespace.lookingAt(pos)) {
                pos = mWhitespace.end()
                continue
            }
            val token = when {
                value[pos] == '(' -> OpenParenthesisToken
                value[pos] == ')' -> CloseParenthesisToken
                value[pos] == '/' -> DivideToken
                mNumber.lookingAt(pos) -> NumberToken(mNumber.group())
                mIdentifier.lookingAt(pos) -> IdentifierToken(mIdentifier.group())
                else -> throw TokenizerException(value, pos, "Unexpected input")
            }
            res += TokenPosition(pos, token)
            pos += token.image.length
        }
        return res
    }
}
