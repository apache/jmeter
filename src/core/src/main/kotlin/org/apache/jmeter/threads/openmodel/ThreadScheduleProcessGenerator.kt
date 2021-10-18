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

import org.apache.jmeter.threads.openmodel.ThreadScheduleStep.ArrivalsStep
import org.apache.jmeter.threads.openmodel.ThreadScheduleStep.RateStep
import java.util.PrimitiveIterator
import java.util.Random

/**
 * Generates timestamps for events according to the schedule.
 * The resulting timestamps are in seconds: `0..totalDuration`.
 */
internal class ThreadScheduleProcessGenerator(
    private val random: Random,
    private val schedule: ThreadSchedule
) : PrimitiveIterator.OfDouble {
    // position of ArrivalStep
    private var pos = -1
    private val poissonRamp = PoissonArrivalsRamp()
    private val evenRamp = EvenArrivalsRamp()
    private var gen: PrimitiveIterator.OfDouble = poissonRamp

    // The times are in seconds
    private var timeOffset = 0.0
    private var lastDuration = 0.0
    private var beginRate = 0.0
    private var endRate = 0.0
    private lateinit var arrivalStep: ArrivalsStep

    /**
     * Total schedule duration in seconds.
     */
    val totalDuration = schedule.steps.sumOf { (it as? ArrivalsStep)?.duration ?: 0.0 }

    override fun remove() {
        TODO("Element removal is not supported")
    }

    /**
     * Returns the timestamp of the next event in seconds.
     */
    override fun nextDouble(): Double = timeOffset + gen.nextDouble()

    override fun hasNext(): Boolean {
        if (pos != -1 && gen.hasNext()) {
            return true
        }
        do {
            timeOffset += lastDuration
            lastDuration = 0.0
            // Find next arrivals
            if (!findNextArrivalStep()) {
                return false
            }
            lastDuration = arrivalStep.duration
            // Prepare next events
            gen = when (arrivalStep.type) {
                ThreadScheduleStep.ArrivalType.EVEN ->
                    evenRamp.apply {
                        prepare(beginRate = beginRate, endRate = endRate, duration = lastDuration)
                    }
                ThreadScheduleStep.ArrivalType.RANDOM ->
                    poissonRamp.apply {
                        prepare(beginRate = beginRate, endRate = endRate, duration = lastDuration, random = random)
                    }
            }
        } while (!gen.hasNext())
        return true
    }

    private fun findNextArrivalStep(): Boolean {
        val steps = schedule.steps
        // Find the next beginRate and arrivalStep
        while (true) {
            pos += 1
            if (pos == steps.size) {
                return false
            }
            when (val step = steps[pos]) {
                is RateStep -> beginRate = step.rate
                is ArrivalsStep -> {
                    arrivalStep = step
                    break
                }
            }
        }

        // If the next step is RateStep, then treat it as the endRate
        // Otherwise, use endRate==beginRate
        endRate = (steps.getOrNull(pos + 1) as? RateStep)?.rate ?: beginRate
        return true
    }
}
