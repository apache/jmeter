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

@file:JvmName("ThreadScheduleUtils")

package org.apache.jmeter.threads.openmodel

import org.apache.jmeter.threads.openmodel.ThreadScheduleStep.ArrivalType
import org.apiguardian.api.API
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

/**
 * rate(2/sec) arrivals(1 min) rate(3/sec) ...
 *    means 2/sec..3/sec during 1min
 *
 * rate(2/sec) arrivals(1 min) arrivals(2 min) rate(3/sec) ...
 *   means 2/sec..2/sec during 1min, then 2/sec..3/sec during 2min
 *
 * rate(1/sec) rate(2/sec) arrivals(1 min) rate(3/sec) rate(4/sec) arrivals(2 min) rate(5/sec)...
 *   means 2/sec..3/sec during 1min, then 4/sec..5/sec during 2min
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public interface ThreadSchedule {
    /**
     * Schedule steps.
     */
    public val steps: List<ThreadScheduleStep>

    /**
     * Total schedule duration in seconds.
     */
    public val totalDuration: Double get() = steps.sumOf { (it as? ThreadScheduleStep.ArrivalsStep)?.duration ?: 0.0 }
}

/**
 * Parses schedule string into [ThreadSchedule] object.
 * @param schedule schedule DSL. See [ThreadSchedule]
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
@Throws(ParserException::class, TokenizerException::class)
public fun ThreadSchedule(schedule: String): ThreadSchedule =
    schedule.toIntOrNull()?.let {
        // If schedule string looks like a number, then pretend it is like "validation mode"
        // It is undocumented for now to gather feedback
        val rate = it.toDouble()
        // If schedule looks like
        DefaultThreadSchedule(
            listOf(
                // Launch N requests
                ThreadScheduleStep.RateStep(rate),
                ThreadScheduleStep.ArrivalsStep(ArrivalType.EVEN, 1.0),
                ThreadScheduleStep.RateStep(rate),
                // Let threads to complete
                ThreadScheduleStep.RateStep(0.0),
                ThreadScheduleStep.ArrivalsStep(ArrivalType.EVEN, TimeUnit.HOURS.asSeconds),
            )
        )
    } ?: ScheduleParser(schedule).parse()

private val FORMAT = DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ROOT))

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public sealed interface ThreadScheduleStep {
    @API(status = API.Status.EXPERIMENTAL, since = "5.5")
    public data class RateStep(val rate: Double) : ThreadScheduleStep {
        override fun toString(): String = "Rate(${FORMAT.format(rate)})"
    }

    @API(status = API.Status.EXPERIMENTAL, since = "5.5")
    public enum class ArrivalType {
        EVEN, RANDOM
    }

    @API(status = API.Status.EXPERIMENTAL, since = "5.5")
    public data class ArrivalsStep(val type: ArrivalType, val duration: Double) : ThreadScheduleStep {
        override fun toString(): String = "Arrivals(type=$type, duration=${FORMAT.format(duration)})"
    }
}

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class DefaultThreadSchedule(override val steps: List<ThreadScheduleStep>) : ThreadSchedule {
    override fun toString(): String = steps.toString()
}

@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class ParserException(public val input: String, public val position: Int, message: String) :
    Exception(
        "$message at position $position: '" +
            input.substring(position until (position + 20).coerceAtMost(input.length)) +
            "' in '$input'"
    )

/**
 * Parses schedule string into [ThreadSchedule] object.
 * @param schedule schedule string (e.g. `"rate(2/sec) arrivals(1 min) rate(3/sec)"`)
 */
internal class ScheduleParser(private val schedule: String) {
    val tokens = Tokenizer.tokenize(schedule)
    private var pos: Int = 0
    private val token: Tokenizer.Token? get() = if (pos >= tokens.size) null else tokens[pos].token
    private var lastRate = ThreadScheduleStep.RateStep(0.0)

    private fun throwParseException(message: String): Nothing =
        throw ParserException(schedule, if (pos >= tokens.size) 0 else tokens[pos].pos, message)

    fun parse(): ThreadSchedule {
        val steps = mutableListOf<ThreadScheduleStep>()
        while (pos < tokens.size) {
            val pauseSteps = parsePause()
            if (pauseSteps != null) {
                steps += pauseSteps
                continue
            }
            // Variable is needed for formatting: https://youtrack.jetbrains.com/issue/KTIJ-19984
            val step = parseRate()
                ?: parseArrivals("random_arrivals", ArrivalType.RANDOM)
                ?: parseArrivals("even_arrivals", ArrivalType.EVEN)
                ?: throwParseException(
                    "Unexpected input (expecting rate, random_arrivals, even_arrivals, or pause)"
                )
            steps += step
            if (step is ThreadScheduleStep.RateStep) {
                lastRate = step
            }
        }
        return DefaultThreadSchedule(steps)
    }

    private fun consume(vararg expected: Tokenizer.Token) {
        if (token !in expected) {
            throwParseException("Unexpected input. Expected ${expected.joinToString()} got $token")
        }
        pos += 1
    }

    private inline fun <reified T : Tokenizer.Token> consume(failMessage: String): T = (token as? T).also { pos += 1 }
        ?: throwParseException("Unexpected input. Expected $failMessage got $token")

    private fun parseRate(): ThreadScheduleStep? {
        if (!token?.image.equals("rate", ignoreCase = true)) {
            return null
        }
        pos += 1
        consume(Tokenizer.OpenParenthesisToken)
        val rate = consume<Tokenizer.NumberToken>("float number for rate")
        val rateValue = rate.image.toDouble()
        // rate(0) is OK without unit, as 0 is the same no matter what time unit is in use
        // If the upcoming token is ), then assume the unit is absent
        val timeUnit = if (rateValue == 0.0 && token == Tokenizer.CloseParenthesisToken) {
            TimeUnit.SECONDS
        } else {
            consume(Tokenizer.DivideToken, Tokenizer.IdentifierToken("per"))
            parseTimeUnit()
        }
        consume(Tokenizer.CloseParenthesisToken)
        return ThreadScheduleStep.RateStep(rateValue / timeUnit.asSeconds)
    }

    private fun parseArrivals(functionName: String, type: ArrivalType): ThreadScheduleStep? {
        if (!token?.image.equals(functionName, ignoreCase = true)) {
            return null
        }
        pos += 1
        consume(Tokenizer.OpenParenthesisToken)
        val duration = parseDuration()
        consume(Tokenizer.CloseParenthesisToken)
        return ThreadScheduleStep.ArrivalsStep(type, duration)
    }

    private fun parsePause(): List<ThreadScheduleStep>? {
        if (!token?.image.equals("pause", ignoreCase = true)) {
            return null
        }
        pos += 1
        consume(Tokenizer.OpenParenthesisToken)
        val duration = parseDuration()
        consume(Tokenizer.CloseParenthesisToken)
        val arrivals = ThreadScheduleStep.ArrivalsStep(ArrivalType.EVEN, duration)
        if (lastRate.rate == 0.0) {
            return listOf(arrivals, lastRate)
        }
        val zeroRate = ThreadScheduleStep.RateStep(0.0)
        return listOf(lastRate, zeroRate, arrivals, zeroRate, lastRate)
    }

    private fun parseDuration(): Double {
        var res = 0.0
        do {
            val duration = consume<Tokenizer.NumberToken>("float number for duration")
            val durationValue = duration.image.toDouble()
            // Allow 0 duration without time unit
            if (durationValue == 0.0 && token == Tokenizer.CloseParenthesisToken) {
                break
            }
            val timeUnit = parseTimeUnit()
            res += durationValue * timeUnit.asSeconds
        } while (token is Tokenizer.NumberToken)
        return res
    }

    private fun parseTimeUnit(): TimeUnit {
        val unit = consume<Tokenizer.IdentifierToken>("unit for duration: sec, min, hour, day")
        val image = unit.image
        val timeUnit = when {
            image.equals("ms", ignoreCase = true) -> TimeUnit.MILLISECONDS
            image.equals("s", ignoreCase = true) || image.startsWith("sec", ignoreCase = true) ->
                TimeUnit.SECONDS
            image.equals("m", ignoreCase = true) || image.startsWith("min", ignoreCase = true) ->
                TimeUnit.MINUTES
            image.equals("h", ignoreCase = true) || image.startsWith("hour", ignoreCase = true) ->
                TimeUnit.HOURS
            image.equals("d", ignoreCase = true) || image.startsWith("day", ignoreCase = true) ->
                TimeUnit.DAYS
            else -> throwParseException("Unexpected time unit $image")
        }
        return timeUnit
    }
}
