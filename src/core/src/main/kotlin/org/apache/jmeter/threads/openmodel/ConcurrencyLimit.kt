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

/**
 * The maximum number of concurrent threads over time, derived from the [ConcurrencyStep] entries
 * of a [ThreadSchedule].
 *
 * The limit is piecewise-constant: a [ConcurrencyStep] applies to the arrivals that follow it,
 * until the next [ConcurrencyStep]. The value is looked up by the time elapsed since the schedule
 * start rather than by the position of the arrivals generator, so the limit still increases even
 * while the generator is blocked waiting for a free thread.
 */
internal class ConcurrencyLimit private constructor(
    // Sorted by time. breakpointTimes[i] is the elapsed time in seconds when limits[i] takes effect.
    private val breakpointTimes: DoubleArray,
    private val limits: IntArray,
) {
    /** True when the schedule has no [ConcurrencyStep], so the number of threads is unlimited. */
    val isUnlimited: Boolean get() = breakpointTimes.isEmpty()

    /** A point in time (seconds since the schedule start) where the limit changes to [limit]. */
    data class Change(val timeSeconds: Double, val limit: Int)

    /** The points where the limit changes, in schedule order. Empty when [isUnlimited]. */
    val changes: List<Change> get() = breakpointTimes.indices.map { Change(breakpointTimes[it], limits[it]) }

    /**
     * The concurrency limit that applies at [elapsedSeconds] since the schedule start.
     * Returns [Int.MAX_VALUE] before the first [ConcurrencyStep] or when the schedule has none.
     */
    fun limitAt(elapsedSeconds: Double): Int {
        var res = Int.MAX_VALUE
        for (i in breakpointTimes.indices) {
            if (breakpointTimes[i] > elapsedSeconds) {
                break
            }
            res = limits[i]
        }
        return res
    }

    /**
     * The elapsed time in seconds of the earliest limit change strictly after [elapsedSeconds],
     * or [Double.POSITIVE_INFINITY] if the limit never changes again. Lets a blocked producer wake
     * up when the limit might increase, even if no thread has finished.
     */
    fun nextChangeAfter(elapsedSeconds: Double): Double {
        for (time in breakpointTimes) {
            if (time > elapsedSeconds) {
                return time
            }
        }
        return Double.POSITIVE_INFINITY
    }

    companion object {
        fun of(schedule: ThreadSchedule): ConcurrencyLimit {
            val times = mutableListOf<Double>()
            val values = mutableListOf<Int>()
            var time = 0.0
            for (step in schedule.steps) {
                when (step) {
                    is ThreadScheduleStep.ConcurrencyStep ->
                        // Several concurrency steps at the same time offset: the last one wins
                        if (times.isNotEmpty() && times.last() == time) {
                            values[values.size - 1] = step.limit
                        } else {
                            times += time
                            values += step.limit
                        }
                    is ThreadScheduleStep.ArrivalsStep -> time += step.duration
                    is ThreadScheduleStep.RateStep -> Unit
                }
            }
            return ConcurrencyLimit(times.toDoubleArray(), values.toIntArray())
        }
    }
}
