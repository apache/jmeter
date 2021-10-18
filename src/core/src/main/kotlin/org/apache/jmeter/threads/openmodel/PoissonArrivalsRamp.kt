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
import org.slf4j.LoggerFactory
import java.nio.DoubleBuffer
import java.util.PrimitiveIterator
import java.util.Random
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Generates events that represent constant, linearly increasing or linearly decreasing load.
 *
 *  Sample usage:
 *
 *     val ramp = PoissonArrivalsRamp()
 *     ramp.prepare(beginRate = 0.0, endRate = 1.0, duration = 10, random = ...)
 *     while(ramp.hasNext()) {
 *         println(ramp.nextDouble()) // -> values from 0..duration
 *     }
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class PoissonArrivalsRamp : PrimitiveIterator.OfDouble {
    private lateinit var events: DoubleBuffer

    private companion object {
        private val log = LoggerFactory.getLogger(PoissonArrivalsRamp::class.java)
    }

    override fun remove() {
        TODO("Element removal is not supported")
    }

    override fun hasNext(): Boolean = events.hasRemaining()

    /**
     * Returns the time of the next event `0..duration`.
     */
    override fun nextDouble(): Double = events.get()

    private fun ensureCapacity(len: Int) {
        if (::events.isInitialized && events.capacity() >= len) {
            events.clear()
            return
        }
        events = DoubleBuffer.allocate(len)
    }

    /**
     * Prepares events for constant, linearly increasing or linearly decreasing load.
     * @param beginRate the load rate at the beginning of the interval, must be positive
     * @param endRate the load rate at the end of the interval, must be positive
     * @param duration the duration of the load interval, must be positive
     * @param random random number generator
     */
    public fun prepare(
        beginRate: Double,
        endRate: Double,
        duration: Double,
        random: Random
    ) {
        val minRate = min(beginRate, endRate)
        // 3.7 events means 3, not 4. It ensures we always we do not overflow over duration interval
        val numEvents = ((beginRate + endRate) / 2 * duration).toInt()
        ensureCapacity(numEvents)
        val flatEvents = (minRate * duration).toInt()
        val events = events
        if (minRate > 0) {
            // Generate uniform process with minimal rate
            repeat(flatEvents) {
                events.put(random.nextDouble() * duration)
            }
        }
        val eventsLeft = numEvents - flatEvents
        if (log.isInfoEnabled) {
            log.info(
                "Generating {} events, beginRate = {} / sec, endRate = {} / sec, duration = {} sec",
                numEvents,
                beginRate,
                endRate,
                duration
            )
        }
        if (beginRate != endRate && eventsLeft != 0) {
            // The rest is either increasing from 0 or decreasing to 0 load
            // For "increasing from 0 to 1" load the cumulative distribution function is x*x
            // so sqrt(random) generates "linearly increasing load"
            // and 1-sqrt(random) generates "linearly decreasing load"
            // See https://en.wikipedia.org/wiki/Inverse_transform_sampling
            repeat(eventsLeft) {
                var time = sqrt(random.nextDouble())
                if (beginRate > endRate) {
                    time = 1 - time
                }
                events.put(time * duration)
            }
        }
        events.array().sort(0, events.position())
        events.flip()
    }
}
