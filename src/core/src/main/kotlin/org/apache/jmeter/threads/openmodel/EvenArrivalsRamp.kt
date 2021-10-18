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
import java.util.PrimitiveIterator
import kotlin.math.absoluteValue
import kotlin.math.sqrt

/**
 * Generates events that represent constant, linearly increasing or linearly decreasing load.
 *
 *  Sample usage:
 *
 *     val ramp = EvenArrivalsRamp()
 *     ramp.prepare(beginRate = 0.0, endRate = 1.0, duration = 10)
 *     while(ramp.hasNext()) {
 *         println(ramp.nextDouble()) // -> values from 0..duration
 *     }
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.5")
public class EvenArrivalsRamp : PrimitiveIterator.OfDouble {
    private var beginRate = 0.0
    private var endRate = 0.0
    private var meanRate = 0.0
    private var acceleration = 0.0
    private var n = 0L
    private var numEvents = 0L

    private companion object {
        private val log = LoggerFactory.getLogger(EvenArrivalsRamp::class.java)
    }

    override fun remove() {
        TODO("Element removal is not supported")
    }

    override fun hasNext(): Boolean = n < numEvents

    /**
     * Returns the time of the next event `0..duration`.
     * The first event happens at 0.0, and the rest are distributed evenly.
     */
    override fun nextDouble(): Double {
        val res = if (n == 0L) {
            0.0
        } else if (acceleration == 0.0) {
            // constant rate
            // The n is 0..(maxEvents-1), so the first event is always 0
            n / meanRate
        } else {
            // increasing or decreasing rate
            // Suppose
            //     the arrival rate at the beginning is beginRate,
            //     the arrival rate at the end is endRate
            //     interval duration is duration
            //     f(t) the number of arrivals during 0..t
            // Then
            //     f(t) = v0 * t + a * t^2 / 2, where v0 = beginRate, and a = (beginRate - endRate) / duration
            // What we need is to figure out the time for n-th arrival,
            // In other words, we need to solve the following quadratic equation for t
            //     n = v0 * t + a * t^2 / 2
            // The solution is
            //     D = v0^2 + 2 * a * n, t = (sqrt(D) - v0) / a
            (sqrt(beginRate * beginRate + 2 * acceleration * n) - beginRate) / acceleration
        }
        n++
        return res
    }

    /**
     * Prepares events for constant, linearly increasing or linearly decreasing load.
     * @param beginRate the load rate at the beginning of the interval, must be positive
     * @param endRate the load rate at the end of the interval, must be positive
     * @param duration the duration of the load interval, must be positive
     */
    public fun prepare(
        beginRate: Double,
        endRate: Double,
        duration: Double
    ) {
        this.beginRate = beginRate
        this.endRate = endRate
        meanRate = (beginRate + endRate) / 2
        // 3.7 events means 3, not 4. It ensures we always we do not overflow over duration interval
        numEvents = (meanRate * duration).toLong()
        n = 0 // position to the first event
        acceleration = if ((endRate - beginRate).absoluteValue < 0.0001 * meanRate) {
            // If the rate change is insignificant, assume the rate is equal to meanRate
            0.0
        } else {
            (endRate - beginRate) / duration
        }
        if (log.isInfoEnabled) {
            log.info(
                "Generating {} events, beginRate = {} / sec, endRate = {} / sec, duration = {} sec",
                numEvents,
                beginRate,
                endRate,
                duration
            )
        }
    }
}
